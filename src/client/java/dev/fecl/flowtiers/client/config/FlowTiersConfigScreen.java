package dev.fecl.flowtiers.client.config;

import dev.fecl.flowtiers.client.FlowTierClientConfig;
import dev.fecl.flowtiers.client.FlowTierFormatter;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class FlowTiersConfigScreen {
	private static final String[] LADDERS = {
			"SWORD", "AXE", "UHC", "VANILLA", "MACE", "SPEAR_MACE", "CART",
			"DIAMOND_POT", "NETHERITE_OP", "SMP", "DIAMOND_SMP", "GLOBAL"
	};

	private FlowTiersConfigScreen() {
	}

	public static Screen create(Screen parent) {
		ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(Text.literal("FlowTiers"));
		ConfigEntryBuilder entries = builder.entryBuilder();

		ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));
		general.addEntry(entries.startEnumSelector(
						Text.literal("Display mode"),
						FlowTierClientConfig.DisplayMode.class,
						FlowTierClientConfig.displayMode)
				.setDefaultValue(FlowTierClientConfig.DisplayMode.PREFERRED_LADDER)
				.setSaveConsumer(value -> FlowTierClientConfig.displayMode = value)
				.build());
		general.addEntry(entries.startSelector(
						Text.literal("Preferred gamemode"), LADDERS, FlowTierClientConfig.preferredLadder)
				.setDefaultValue("SWORD")
				.setNameProvider(value -> Text.literal(FlowTierFormatter.displayName(value)))
				.setSaveConsumer(value -> FlowTierClientConfig.preferredLadder = FlowTierClientConfig.normalizeLadder(value))
				.build());
		general.addEntry(entries.startBooleanToggle(Text.literal("Check for updates"), FlowTierClientConfig.versionCheckEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.versionCheckEnabled = value)
				.build());

		ConfigCategory overlay = builder.getOrCreateCategory(Text.literal("Nametag & Tab"));
		overlay.addEntry(new NametagLayoutButtonEntry(parent));
		overlay.addEntry(entries.startBooleanToggle(Text.literal("Show nametag stats"), FlowTierClientConfig.nametagEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.nametagEnabled = value)
				.build());
		overlay.addEntry(entries.startBooleanToggle(Text.literal("Hide nametag if Ranked System"), FlowTierClientConfig.suppressRankedDuplicates)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.suppressRankedDuplicates = value)
				.build());
		overlay.addEntry(entries.startBooleanToggle(Text.literal("Show tab list stats"), FlowTierClientConfig.tabListEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.tabListEnabled = value)
				.build());
		overlay.addEntry(entries.startEnumSelector(
						Text.literal("Stats position"),
						FlowTierClientConfig.NametagAlignment.class,
						FlowTierClientConfig.nametagAlignment)
				.setDefaultValue(FlowTierClientConfig.NametagAlignment.LEFT)
				.setSaveConsumer(value -> FlowTierClientConfig.nametagAlignment = value)
				.build());
		overlay.addEntry(entries.startBooleanToggle(Text.literal("Colored tier in nametag"), FlowTierClientConfig.coloredTier)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.coloredTier = value)
				.build());
		overlay.addEntry(entries.startBooleanToggle(Text.literal("Colored SR in nametag"), FlowTierClientConfig.coloredElo)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.coloredElo = value)
				.build());
		overlay.addEntry(entries.startBooleanToggle(Text.literal("Colored position in nametag"), FlowTierClientConfig.coloredPosition)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.coloredPosition = value)
				.build());

		ConfigCategory hud = builder.getOrCreateCategory(Text.literal("HUD"));
		hud.addEntry(new HudPlacementEntry());
		hud.addEntry(entries.startBooleanToggle(Text.literal("Show HUD"), FlowTierClientConfig.hudEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.hudEnabled = value)
				.build());
		hud.addEntry(entries.startBooleanToggle(Text.literal("HUD background"), FlowTierClientConfig.hudBackground)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.hudBackground = value)
				.build());
		hud.addEntry(entries.startBooleanToggle(Text.literal("Show win/loss"), FlowTierClientConfig.hudRecordEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.hudRecordEnabled = value)
				.build());
		hud.addEntry(entries.startBooleanToggle(Text.literal("Show streak"), FlowTierClientConfig.hudStreakEnabled)
				.setDefaultValue(false)
				.setSaveConsumer(value -> FlowTierClientConfig.hudStreakEnabled = value)
				.build());

		builder.setSavingRunnable(FlowTierClientConfig::save);
		return builder.build();
	}
}
