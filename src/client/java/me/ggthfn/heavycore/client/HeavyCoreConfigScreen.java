package me.ggthfn.heavycore.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HeavyCoreConfigScreen extends Screen {
    private static final int MIN_WIDTH = 340;
    private static final int MIN_HEIGHT = 260;
    private static final int ROW_HEIGHT = 22;
    private static final int LIST_PADDING = 6;

    private final Screen parent;
    private TextFieldWidget searchField;
    private TextFieldWidget addItemField;
    private String searchQuery = "";
    private int listScroll = 0;
    private boolean scrollbarDragging = false;

    private int outerWidth, outerHeight;
    private int leftPanelWidth, rightPanelWidth, panelGap;
    private int leftPanelX, leftPanelY, rightPanelX, rightPanelY;
    private int listX, listY, listWidth, listHeight;
    private int scrollbarX;
    private int controlWidth, buttonX;

    // Toggle data — rendered manually, NOT as widgets
    private final List<ToggleEntry> toggles = new ArrayList<>();
    private int togglesStartY;

    private static class ToggleEntry {
        final String key;
        boolean value;
        final java.util.function.Consumer<Boolean> setter;
        boolean active;
        int x, y, w, h;

        ToggleEntry(String key, boolean value, java.util.function.Consumer<Boolean> setter, boolean active) {
            this.key = key;
            this.value = value;
            this.setter = setter;
            this.active = active;
        }

        boolean contains(int mx, int my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }
    }

    public HeavyCoreConfigScreen(Screen parent) {
        super(Text.translatable("tukangkunci.title"));
        this.parent = parent;
    }

    private void computeDimensions() {
        int availW = Math.max(MIN_WIDTH, this.width - 20);
        int availH = Math.max(MIN_HEIGHT, this.height - 30);
        outerWidth = Math.min(availW, 500);
        outerHeight = Math.min(availH, 380);
        panelGap = Math.max(6, outerWidth / 55);
        rightPanelWidth = Math.max(140, outerWidth * 36 / 100);
        leftPanelWidth = outerWidth - rightPanelWidth - panelGap;
    }

    @Override
    protected void init() {
        clearChildren();
        toggles.clear();
        computeDimensions();

        int startX = (this.width - outerWidth) / 2;
        int startY = (this.height - outerHeight) / 2;
        leftPanelX = startX;
        leftPanelY = startY;
        rightPanelX = startX + leftPanelWidth + panelGap;
        rightPanelY = startY + 28;

        // --- Search bar ---
        int searchWidth = Math.max(80, leftPanelWidth - 20);
        searchField = new TextFieldWidget(this.textRenderer, leftPanelX + 10, leftPanelY + 10,
                searchWidth, 18, Text.literal("search"));
        searchField.setMaxLength(64);
        searchField.setText(searchQuery);
        searchField.setPlaceholder(Text.literal("\u00a77Search item..."));
        searchField.setChangedListener(value -> { searchQuery = value; listScroll = 0; });
        addDrawableChild(searchField);

        // --- List area ---
        listX = leftPanelX + LIST_PADDING;
        listY = leftPanelY + 34;
        listWidth = leftPanelWidth - LIST_PADDING * 2 - 8;
        listHeight = outerHeight - 48;
        scrollbarX = leftPanelX + leftPanelWidth - 12;

        // --- Right panel controls ---
        controlWidth = Math.max(108, rightPanelWidth - 12);
        buttonX = rightPanelX + 6;
        int y = rightPanelY;

        addItemField = new TextFieldWidget(this.textRenderer, buttonX, y, controlWidth, 18,
                Text.literal("add item"));
        addItemField.setPlaceholder(Text.translatable("tukangkunci.gui.prompt_item_id"));
        addItemField.setMaxLength(64);
        addDrawableChild(addItemField);
        y += 24;

        addDrawableChild(ButtonWidget.builder(Text.translatable("tukangkunci.gui.add_item"), b -> {
            String raw = addItemField.getText().trim().toLowerCase();
            if (raw.isEmpty()) return;
            Identifier identifier = Identifier.tryParse(raw);
            if (identifier == null) return;
            HeavyCoreConfig.get().filter.add(identifier.toString());
            HeavyCoreConfig.save();
            addItemField.setText("");
            refresh();
        }).dimensions(buttonX, y, controlWidth, 20).build());
        y += 26;

        togglesStartY = y;

        // Build toggle entries (no widgets, rendered manually)
        boolean wbActive = HeavyCoreConfig.get().filter.contains("minecraft:enchanted_book");

        addToggle(y, "tukangkunci.gui.auto_vault", HeavyCoreConfig.get().enabled,
                v -> HeavyCoreConfig.get().enabled = v, true);
        y += 24;
        addToggle(y, "tukangkunci.gui.wind_burst_only", HeavyCoreConfig.get().requireWindBurstOnBook,
                v -> HeavyCoreConfig.get().requireWindBurstOnBook = v, wbActive);
        y += 24;
        addToggle(y, "tukangkunci.gui.normal_vaults", HeavyCoreConfig.get().openNormal,
                v -> HeavyCoreConfig.get().openNormal = v, true);
        y += 24;
        addToggle(y, "tukangkunci.gui.ominous_vaults", HeavyCoreConfig.get().openOminous,
                v -> HeavyCoreConfig.get().openOminous = v, true);
        y += 6;

        // Bottom buttons
        addDrawableChild(ButtonWidget.builder(Text.translatable("tukangkunci.gui.clear_list"), b -> {
            HeavyCoreConfig.get().filter.clear();
            HeavyCoreConfig.save();
            refresh();
        }).dimensions(buttonX, y, controlWidth, 20).build());
        y += 24;

        addDrawableChild(ButtonWidget.builder(Text.translatable("tukangkunci.gui.reset"), b -> {
            HeavyCoreConfig.resetToDefaults();
            refresh();
        }).dimensions(buttonX, y, controlWidth, 20).build());
        y += 24;

        addDrawableChild(ButtonWidget.builder(Text.translatable("tukangkunci.gui.done"), b -> close())
                .dimensions(buttonX, y, controlWidth, 20).build());
    }

    private void addToggle(int y, String key, boolean value,
                           java.util.function.Consumer<Boolean> setter, boolean active) {
        ToggleEntry t = new ToggleEntry(key, value, setter, active);
        t.x = buttonX;
        t.y = y;
        t.w = controlWidth;
        t.h = 20;
        toggles.add(t);
    }

    // --- List helpers ---
    private List<String> getFilteredItems() {
        String query = searchQuery == null ? "" : searchQuery.trim().toLowerCase();
        List<String> items = new ArrayList<>(HeavyCoreConfig.get().filter);
        items.sort(Comparator.naturalOrder());
        if (query.isEmpty()) return items;
        List<String> filtered = new ArrayList<>();
        for (String item : items) {
            if (item.toLowerCase().contains(query)) filtered.add(item);
        }
        return filtered;
    }

    private int getVisibleRowCount() { return Math.max(1, (listHeight - 4) / ROW_HEIGHT); }
    private int getMaxScroll(List<String> items) { return Math.max(0, items.size() - getVisibleRowCount()); }

    private String shortenItem(String id) {
        String text = id.replace("minecraft:", "");
        int maxChars = (listWidth - 30) / 6;
        if (text.length() > maxChars) return text.substring(0, Math.max(1, maxChars - 2)) + "..";
        return text;
    }

    private void refresh() { HeavyCoreConfig.save(); clearAndInit(); }

    @Override
    public void close() {
        HeavyCoreConfig.save();
        if (this.client != null) this.client.setScreen(parent);
    }

    // ===== RENDER =====
    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        this.renderBackground(ctx, mouseX, mouseY, delta);

        // Panels
        drawPanelBg(ctx, leftPanelX, leftPanelY, leftPanelWidth, outerHeight);
        drawPanelBg(ctx, rightPanelX, leftPanelY + 28, rightPanelWidth, outerHeight - 28);

        ctx.drawTextWithShadow(this.textRenderer, Text.translatable("tukangkunci.gui.filter_list"),
                leftPanelX + 12, leftPanelY - 10, 0xFFFFFF);
        ctx.drawTextWithShadow(this.textRenderer, Text.translatable("tukangkunci.gui.general"),
                rightPanelX + 10, leftPanelY + 12, 0xBBBBBB);

        // Draw widgets (search, addItem, bottom buttons)
        super.render(ctx, mouseX, mouseY, delta);

        // --- Custom toggle bars (drawn AFTER super so they're visible) ---
        for (ToggleEntry t : toggles) {
            boolean hovered = t.active && t.contains(mouseX, mouseY);
            int bg = t.active ? (hovered ? 0xFF555555 : 0xFF444444) : 0xFF2A2A2A;
            ctx.fill(t.x, t.y, t.x + t.w, t.y + t.h, bg);

            // Accent stripe
            int accent = t.active ? (t.value ? 0xFF4CAF50 : 0xFFE53935) : 0xFF555555;
            ctx.fill(t.x, t.y, t.x + 3, t.y + t.h, accent);

            // Label
            String label = Text.translatable(t.key).getString();
            int labelColor = t.active ? 0xFFFFFF : 0x666666;
            ctx.drawTextWithShadow(this.textRenderer, Text.literal(label), t.x + 8, t.y + 5, labelColor);

            // ON/OFF
            String onOff = t.value ? "\u00a7a\u25cf ON" : "\u00a7c\u25cb OFF";
            if (!t.active) onOff = "\u00a77\u25cb OFF";
            int ow = this.textRenderer.getWidth(onOff.replaceAll("\u00a7.", ""));
            ctx.drawTextWithShadow(this.textRenderer, Text.literal(onOff), t.x + t.w - ow - 8, t.y + 5, 0xFFFFFF);
        }

        // --- Item list ---
        List<String> items = getFilteredItems();
        int maxScroll = getMaxScroll(items);
        listScroll = MathHelper.clamp(listScroll, 0, maxScroll);

        int endIndex = Math.min(items.size(), listScroll + getVisibleRowCount());
        enableScissor(ctx, listX, listY, listWidth + 10, listHeight);
        int rowY = listY;

        for (int i = listScroll; i < endIndex; i++) {
            String itemId = items.get(i);
            boolean hovered = mouseX >= listX && mouseX < listX + listWidth
                    && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT;
            boolean xHovered = mouseX >= listX + listWidth - 18 && mouseX < listX + listWidth + 4
                    && mouseY >= rowY + 2 && mouseY < rowY + ROW_HEIGHT - 2;

            ctx.fill(listX, rowY, listX + listWidth, rowY + ROW_HEIGHT, hovered ? 0xFF505050 : 0xFF3A3A3A);
            ctx.drawTextWithShadow(this.textRenderer, Text.literal(shortenItem(itemId)),
                    listX + 6, rowY + 5, 0xFFFFFF);
            ctx.drawTextWithShadow(this.textRenderer, Text.literal("\u2715"),
                    listX + listWidth - 14, rowY + 5, xHovered ? 0xFF8888 : 0xFF5555);
            rowY += ROW_HEIGHT;
        }
        disableScissor(ctx);

        // Scrollbar
        if (items.size() > getVisibleRowCount()) {
            int trackH = listHeight;
            int thumbH = Math.max(12, trackH * getVisibleRowCount() / items.size());
            int thumbY = listY + (maxScroll == 0 ? 0 : (trackH - thumbH) * listScroll / maxScroll);
            ctx.fill(scrollbarX, listY, scrollbarX + 6, listY + trackH, 0xFF1A1A1A);
            ctx.fill(scrollbarX, thumbY, scrollbarX + 6, thumbY + thumbH, 0xFFAAAAAA);
        }

        // Title + footer
        ctx.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);
        int infoY = leftPanelY + outerHeight + 4;
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("tukangkunci.gui.click_to_remove"),
                this.width / 2, infoY, 0x888888);
        String filterText = HeavyCoreConfig.get().filter.isEmpty()
                ? Text.translatable("tukangkunci.gui.filter_empty").getString()
                : HeavyCoreConfig.get().filter.size() + " item(s) in filter";
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("\u00a77" + filterText),
                this.width / 2, infoY + 14, 0x888888);
    }

    // ===== Panel background =====
    private void drawPanelBg(DrawContext ctx, int x, int y, int w, int h) {
        ctx.fill(x, y, x + w, y + h, 0xC0000000);
        ctx.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xC0303030);
        ctx.fill(x + 1, y + 1, x + w - 1, y + h / 2, 0xC03A3A3A);
        ctx.fill(x + 1, y + 1, x + w - 1, y + 2, 0x40FFFFFF);
    }

    // ===== Scissor =====
    private void enableScissor(DrawContext ctx, int x, int y, int w, int h) {
        var win = this.client.getWindow();
        double s = win.getScaleFactor();
        ctx.enableScissor((int)(x * s), (int)(win.getHeight() - (y + h) * s), (int)(w * s), (int)(h * s));
    }
    private void disableScissor(DrawContext ctx) { ctx.disableScissor(); }

    // ===== Input =====
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        // Toggle bars
        if (button == 0) {
            for (ToggleEntry t : toggles) {
                if (t.active && t.contains((int)mx, (int)my)) {
                    t.value = !t.value;
                    t.setter.accept(t.value);
                    HeavyCoreConfig.save();
                    refresh();
                    return true;
                }
            }
        }
        // Delete item (✕)
        if (button == 0 && mx >= listX + listWidth - 18 && mx < listX + listWidth + 4
                && my >= listY && my < listY + listHeight) {
            List<String> items = getFilteredItems();
            int idx = listScroll + (int)((my - listY) / ROW_HEIGHT);
            if (idx >= 0 && idx < items.size()) {
                HeavyCoreConfig.get().filter.remove(items.get(idx));
                HeavyCoreConfig.save();
                refresh();
                return true;
            }
        }
        // Scrollbar
        if (button == 0 && mx >= scrollbarX && mx < scrollbarX + 6 && my >= listY && my < listY + listHeight) {
            List<String> items = getFilteredItems();
            if (getMaxScroll(items) > 0) {
                scrollbarDragging = true;
                updateScrollFromMouse(my, items);
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int b) {
        scrollbarDragging = false;
        return super.mouseReleased(mx, my, b);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int b, double dx, double dy) {
        if (scrollbarDragging) { updateScrollFromMouse(my, getFilteredItems()); return true; }
        return super.mouseDragged(mx, my, b, dx, dy);
    }

    private void updateScrollFromMouse(double my, List<String> items) {
        int max = getMaxScroll(items); if (max <= 0) return;
        int thumbH = Math.max(12, listHeight * getVisibleRowCount() / items.size());
        double r = (my - listY - thumbH / 2.0) / (listHeight - thumbH);
        listScroll = MathHelper.clamp((int)Math.round(r * max), 0, max);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double h, double v) {
        if (mx >= listX && mx < listX + listWidth + 10 && my >= listY && my < listY + listHeight) {
            List<String> items = getFilteredItems();
            int max = getMaxScroll(items);
            if (max > 0) { listScroll = MathHelper.clamp(listScroll - (int)Math.signum(v), 0, max); return true; }
        }
        return super.mouseScrolled(mx, my, h, v);
    }
}
