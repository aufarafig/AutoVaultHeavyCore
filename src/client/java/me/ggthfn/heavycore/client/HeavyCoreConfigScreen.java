package me.ggthfn.heavycore.client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class HeavyCoreConfigScreen extends Screen {
    private final Screen parent;

    private TextFieldWidget customItemField;

    private ButtonWidget enabledBtn;
    private ButtonWidget filterBtn;
    private ButtonWidget windBurstBtn;
    private ButtonWidget ominousBtn;
    private ButtonWidget normalBtn;
    private ButtonWidget tridentBtn;
    private ButtonWidget maceBtn;
    private ButtonWidget heavyCoreBtn;
    private ButtonWidget bookBtn;

    private final List<String> customItemsToDraw = new ArrayList<>();
    private int extraCustomCount = 0;

    public HeavyCoreConfigScreen(Screen parent) {
        super(Text.translatable("tukangkunci.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;

        int leftX = cx - 180;
        int colW = 110;

        enabledBtn = ButtonWidget.builder(buildOnOff("tukangkunci.gui.auto_vault", HeavyCoreConfig.get().enabled),
                b -> {
                    HeavyCoreConfig.get().enabled = !HeavyCoreConfig.get().enabled;
                    refresh();
                })
                .dimensions(leftX, 55, colW, 20)
                .tooltip(Tooltip.of(Text.translatable("tukangkunci.gui.tooltip.enabled")))
                .build();
        addDrawableChild(enabledBtn);

        normalBtn = ButtonWidget.builder(buildOnOff("tukangkunci.gui.normal_vaults", HeavyCoreConfig.get().openNormal),
                b -> { HeavyCoreConfig.get().openNormal = !HeavyCoreConfig.get().openNormal; refresh(); })
                .dimensions(leftX, 80, colW, 20)
                .tooltip(Tooltip.of(Text.translatable("tukangkunci.gui.tooltip.normal")))
                .build();
        addDrawableChild(normalBtn);

        ominousBtn = ButtonWidget.builder(buildOnOff("tukangkunci.gui.ominous_vaults", HeavyCoreConfig.get().openOminous),
                b -> { HeavyCoreConfig.get().openOminous = !HeavyCoreConfig.get().openOminous; refresh(); })
                .dimensions(leftX, 105, colW, 20)
                .tooltip(Tooltip.of(Text.translatable("tukangkunci.gui.tooltip.ominous")))
                .build();
        addDrawableChild(ominousBtn);

        int midX = cx - 55;
        int midW = 110;

        filterBtn = ButtonWidget.builder(buildOnOff("tukangkunci.gui.use_filter", HeavyCoreConfig.get().useFilter),
                b -> { HeavyCoreConfig.get().useFilter = !HeavyCoreConfig.get().useFilter; refresh(); })
                .dimensions(midX, 55, midW, 20)
                .tooltip(Tooltip.of(Text.translatable("tukangkunci.gui.tooltip.use_filter")))
                .build();
        addDrawableChild(filterBtn);

        int itemW = 53;
        tridentBtn = ButtonWidget.builder(buildItemLabel("tukangkunci.gui.trident", "minecraft:trident"),
                b -> { toggleItem("minecraft:trident"); refresh(); })
                .dimensions(midX, 80, itemW, 20)
                .tooltip(Tooltip.of(Text.translatable("tukangkunci.gui.tooltip.trident")))
                .build();
        addDrawableChild(tridentBtn);

        maceBtn = ButtonWidget.builder(buildItemLabel("tukangkunci.gui.mace", "minecraft:mace"),
                b -> { toggleItem("minecraft:mace"); refresh(); })
                .dimensions(midX + 57, 80, itemW, 20)
                .tooltip(Tooltip.of(Text.translatable("tukangkunci.gui.tooltip.mace")))
                .build();
        addDrawableChild(maceBtn);

        heavyCoreBtn = ButtonWidget.builder(buildItemLabel("tukangkunci.gui.core", "minecraft:heavy_core"),
                b -> { toggleItem("minecraft:heavy_core"); refresh(); })
                .dimensions(midX, 105, itemW, 20)
                .tooltip(Tooltip.of(Text.translatable("tukangkunci.gui.tooltip.core")))
                .build();
        addDrawableChild(heavyCoreBtn);

        bookBtn = ButtonWidget.builder(buildItemLabel("tukangkunci.gui.book", "minecraft:enchanted_book"),
                b -> { toggleItem("minecraft:enchanted_book"); refresh(); })
                .dimensions(midX + 57, 105, itemW, 20)
                .tooltip(Tooltip.of(Text.translatable("tukangkunci.gui.tooltip.book")))
                .build();
        addDrawableChild(bookBtn);

        boolean hasBook = HeavyCoreConfig.get().filter.contains("minecraft:enchanted_book");
        windBurstBtn = ButtonWidget.builder(buildOnOff("tukangkunci.gui.wind_burst_only", HeavyCoreConfig.get().requireWindBurstOnBook),
                b -> { HeavyCoreConfig.get().requireWindBurstOnBook = !HeavyCoreConfig.get().requireWindBurstOnBook; refresh(); })
                .dimensions(midX, 130, midW, 20)
                .tooltip(Tooltip.of(Text.translatable("tukangkunci.gui.tooltip.wind_burst")))
                .build();
        windBurstBtn.active = hasBook && HeavyCoreConfig.get().useFilter;
        addDrawableChild(windBurstBtn);

        int rightX = cx + 70;
        int rightW = 115;

        customItemField = new TextFieldWidget(this.textRenderer, rightX, 55, rightW, 20,
                Text.literal("custom item id"));
        customItemField.setPlaceholder(Text.literal("minecraft:diamond"));
        customItemField.setMaxLength(64);
        addDrawableChild(customItemField);

        addDrawableChild(ButtonWidget.builder(Text.translatable("tukangkunci.gui.add_remove"), b -> {
            String idStr = customItemField.getText().trim().toLowerCase();
            if (idStr.isEmpty()) return;
            Identifier identifier = Identifier.tryParse(idStr);
            if (identifier != null) {
                String id = identifier.toString();
                if (!HeavyCoreConfig.get().filter.add(id)) {
                    HeavyCoreConfig.get().filter.remove(id);
                }
                HeavyCoreConfig.save();
            }
            customItemField.setText("");
            refresh();
        }).dimensions(rightX, 80, rightW, 20)
                .tooltip(Tooltip.of(Text.translatable("tukangkunci.gui.tooltip.add_remove_btn")))
                .build());

        customItemsToDraw.clear();
        extraCustomCount = 0;
        int customY = 105;
        int displayedCount = 0;

        for (String id : HeavyCoreConfig.get().filter) {
            if (id.equals("minecraft:trident") || id.equals("minecraft:mace") ||
                id.equals("minecraft:heavy_core") || id.equals("minecraft:enchanted_book")) {
                continue;
            }

            if (displayedCount < 3) {
                customItemsToDraw.add(id);
                String shortName = id.replace("minecraft:", "");
                if (shortName.length() > 11) {
                    shortName = shortName.substring(0, 9) + "..";
                }

                final String itemId = id;
                ButtonWidget removeBtn = ButtonWidget.builder(Text.literal("\u00a7c\u2716 \u00a77" + shortName), btn -> {
                    HeavyCoreConfig.get().filter.remove(itemId);
                    HeavyCoreConfig.save();
                    refresh();
                }).dimensions(rightX, customY, rightW, 18).build();
                addDrawableChild(removeBtn);

                customY += 20;
                displayedCount++;
            } else {
                extraCustomCount++;
            }
        }

        addDrawableChild(ButtonWidget.builder(Text.translatable("tukangkunci.gui.clear_list"), b -> {
            HeavyCoreConfig.get().filter.removeIf(id ->
                !id.equals("minecraft:trident") && !id.equals("minecraft:mace") &&
                !id.equals("minecraft:heavy_core") && !id.equals("minecraft:enchanted_book")
            );
            HeavyCoreConfig.save();
            refresh();
        }).dimensions(rightX, 170, rightW, 20)
                .tooltip(Tooltip.of(Text.translatable("tukangkunci.gui.tooltip.clear_list")))
                .build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("tukangkunci.gui.reset"), b -> {
            HeavyCoreConfig.resetToDefaults();
            refresh();
        }).dimensions(cx - 110, 205, 105, 20)
                .tooltip(Tooltip.of(Text.translatable("tukangkunci.gui.tooltip.reset")))
                .build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("tukangkunci.gui.done"), b -> close())
                .dimensions(cx + 5, 205, 105, 20)
                .tooltip(Tooltip.of(Text.translatable("tukangkunci.gui.tooltip.done")))
                .build());
    }

    private void toggleItem(String id) {
        if (!HeavyCoreConfig.get().filter.add(id)) {
            HeavyCoreConfig.get().filter.remove(id);
        }
        HeavyCoreConfig.save();
    }

    private Text buildOnOff(String key, boolean value) {
        String state = value ? "\u00a7aON" : "\u00a7cOFF";
        return Text.translatable(key).copy().append(": " + state);
    }

    private Text buildItemLabel(String key, String id) {
        boolean has = HeavyCoreConfig.get().filter.contains(id);
        Text label = Text.translatable(key);
        return Text.literal(has ? "\u00a7a\u2714\u00a7r " : "\u00a7c\u2716\u00a7r ").append(label);
    }

    private void refresh() {
        HeavyCoreConfig.save();
        clearAndInit();
    }

    @Override
    public void close() {
        HeavyCoreConfig.save();
        if (this.client != null) this.client.setScreen(parent);
    }

    @Override
    public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int cx = this.width / 2;

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, cx, 12, 0xFFFFFF);

        context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("tukangkunci.gui.general"), cx - 125, 42, 0xAAAAAA);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("tukangkunci.gui.presets"), cx, 42, 0xAAAAAA);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("tukangkunci.gui.custom_list"), cx + 127, 42, 0xAAAAAA);

        if (extraCustomCount > 0) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable("tukangkunci.gui.more_items", String.valueOf(extraCustomCount)),
                    cx + 127, 160, 0x888888);
        }

        String filterText = HeavyCoreConfig.get().filter.isEmpty()
                ? Text.translatable("tukangkunci.gui.filter_empty").getString()
                : String.join(", ", HeavyCoreConfig.get().filter);
        Text hintText = Text.translatable("tukangkunci.gui.active_filter", filterText);

        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("\u00a77").append(hintText),
                cx, this.height - 18, 0xAAAAAA);
    }
}
