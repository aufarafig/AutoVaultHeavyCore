package me.ggthfn.heavycore.client;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ClothConfigFactory {

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("tukangkunci.title"))
                .setSavingRunnable(HeavyCoreConfig::save);

        // -- General --
        ConfigCategory general = builder.getOrCreateCategory(
                Text.translatable("tukangkunci.gui.general"));

        ConfigEntryBuilder eb = builder.entryBuilder();

        general.addEntry(eb.startBooleanToggle(
                        Text.translatable("tukangkunci.gui.auto_vault"),
                        HeavyCoreConfig.get().enabled)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("tukangkunci.gui.tooltip.enabled"))
                .setSaveConsumer(v -> HeavyCoreConfig.get().enabled = v)
                .build());

        general.addEntry(eb.startBooleanToggle(
                        Text.translatable("tukangkunci.gui.wind_burst_only"),
                        HeavyCoreConfig.get().requireWindBurstOnBook)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("tukangkunci.gui.tooltip.wind_burst"))
                .setSaveConsumer(v -> HeavyCoreConfig.get().requireWindBurstOnBook = v)
                .build());

        // -- Vault Types --
        ConfigCategory vaults = builder.getOrCreateCategory(
                Text.translatable("tukangkunci.gui.vault_types"));

        vaults.addEntry(eb.startBooleanToggle(
                        Text.translatable("tukangkunci.gui.normal_vaults"),
                        HeavyCoreConfig.get().openNormal)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("tukangkunci.gui.tooltip.normal"))
                .setSaveConsumer(v -> HeavyCoreConfig.get().openNormal = v)
                .build());

        vaults.addEntry(eb.startBooleanToggle(
                        Text.translatable("tukangkunci.gui.ominous_vaults"),
                        HeavyCoreConfig.get().openOminous)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("tukangkunci.gui.tooltip.ominous"))
                .setSaveConsumer(v -> HeavyCoreConfig.get().openOminous = v)
                .build());

        // -- Filter List --
        ConfigCategory filterCat = builder.getOrCreateCategory(
                Text.translatable("tukangkunci.gui.filter_list"));

        filterCat.addEntry(eb.startStrList(
                        Text.translatable("tukangkunci.gui.filter_items"),
                        new ArrayList<>(HeavyCoreConfig.get().filter))
                .setDefaultValue(List.of("minecraft:trident", "minecraft:mace",
                        "minecraft:heavy_core", "minecraft:enchanted_book"))
                .setTooltip(Text.translatable("tukangkunci.gui.tooltip.filter_list"))
                .setExpanded(true)
                .setSaveConsumer(entries -> {
                    HeavyCoreConfig.get().filter.clear();
                    // Only add valid identifiers
                    for (String entry : entries) {
                        Identifier id = Identifier.tryParse(entry.trim().toLowerCase());
                        if (id != null) {
                            HeavyCoreConfig.get().filter.add(id.toString());
                        }
                    }
                })
                .build());

        return builder.build();
    }
}
