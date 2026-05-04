package dev.decl.flowtiers.client.config;

import dev.decl.flowtiers.client.FlowTierClientConfig;
import dev.decl.flowtiers.client.FlowTierFormatter;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class FlowTiersConfigScreen {
	private static final String[] LADDERS = {
			"SWORD", "AXE", "UHC", "VANILLA", "MACE",
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
		general.addEntry(entries.startEnumSelector(Text.literal("Display mode"), FlowTierClientConfig.DisplayMode.class, FlowTierClientConfig.displayMode)
				.setDefaultValue(FlowTierClientConfig.DisplayMode.PREFERRED_LADDER)
				.setSaveConsumer(value -> FlowTierClientConfig.displayMode = value)
				.build());
		general.addEntry(entries.startSelector(Text.literal("Preferred gamemode"), LADDERS, FlowTierClientConfig.preferredLadder)
				.setDefaultValue("SWORD")
				.setNameProvider(value -> Text.literal(FlowTierFormatter.displayName(value)))
				.setSaveConsumer(value -> FlowTierClientConfig.preferredLadder = FlowTierClientConfig.normalizeLadder(value))
				.build());

		ConfigCategory hud = builder.getOrCreateCategory(Text.literal("HUD"));
		hud.addEntry(entries.startBooleanToggle(Text.literal("Show HUD"), FlowTierClientConfig.hudEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.hudEnabled = value)
				.build());
		hud.addEntry(new HudPlacementEntry());
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

		ConfigCategory overlay = builder.getOrCreateCategory(Text.literal("Nametag & Tab"));
		overlay.addEntry(entries.startBooleanToggle(Text.literal("Show nametag stats"), FlowTierClientConfig.nametagEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.nametagEnabled = value)
				.build());
		overlay.addEntry(entries.startBooleanToggle(Text.literal("Show tab list stats"), FlowTierClientConfig.tabListEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.tabListEnabled = value)
				.build());
		overlay.addEntry(entries.startTextDescription(Text.literal("Preview: ").append(FlowTierFormatter.previewCompact())).build());
		overlay.addEntry(entries.startBooleanToggle(Text.literal("Show gamemode icon"), FlowTierClientConfig.gamemodeIconEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.gamemodeIconEnabled = value)
				.build());
		overlay.addEntry(entries.startBooleanToggle(Text.literal("Show tier"), FlowTierClientConfig.tierEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.tierEnabled = value)
				.build());
		overlay.addEntry(entries.startBooleanToggle(Text.literal("Short tier names"), FlowTierClientConfig.shortTierNames)
				.setDefaultValue(false)
				.setSaveConsumer(value -> FlowTierClientConfig.shortTierNames = value)
				.build());
		overlay.addEntry(entries.startBooleanToggle(Text.literal("Show ELO"), FlowTierClientConfig.eloEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.eloEnabled = value)
				.build());
		overlay.addEntry(entries.startBooleanToggle(Text.literal("Show ELO label"), FlowTierClientConfig.eloLabelEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.eloLabelEnabled = value)
				.build());
		overlay.addEntry(entries.startBooleanToggle(Text.literal("Color ELO by tier"), FlowTierClientConfig.coloredElo)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.coloredElo = value)
				.build());
		overlay.addEntry(entries.startBooleanToggle(Text.literal("Show position"), FlowTierClientConfig.positionEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.positionEnabled = value)
				.build());
		overlay.addEntry(entries.startBooleanToggle(Text.literal("Show position label"), FlowTierClientConfig.positionLabelEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.positionLabelEnabled = value)
				.build());
		builder.setSavingRunnable(FlowTierClientConfig::save);
		return builder.build();
	}
}
