package dev.decl.flowtiers.client.config;

import dev.decl.flowtiers.client.FlowTierClientConfig;
import dev.decl.flowtiers.client.FlowTierFormatter;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

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
				.setTitle(Component.literal("FlowTiers"));
		ConfigEntryBuilder entries = builder.entryBuilder();

		ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));
		general.addEntry(entries.startEnumSelector(Component.literal("Display mode"), FlowTierClientConfig.DisplayMode.class, FlowTierClientConfig.displayMode)
				.setDefaultValue(FlowTierClientConfig.DisplayMode.PREFERRED_LADDER)
				.setSaveConsumer(value -> FlowTierClientConfig.displayMode = value)
				.build());
		general.addEntry(entries.startSelector(Component.literal("Preferred gamemode"), LADDERS, FlowTierClientConfig.preferredLadder)
				.setDefaultValue("SWORD")
				.setNameProvider(value -> Component.literal(FlowTierFormatter.displayName(value)))
				.setSaveConsumer(value -> FlowTierClientConfig.preferredLadder = FlowTierClientConfig.normalizeLadder(value))
				.build());

		ConfigCategory hud = builder.getOrCreateCategory(Component.literal("HUD"));
		hud.addEntry(entries.startBooleanToggle(Component.literal("Show HUD"), FlowTierClientConfig.hudEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.hudEnabled = value)
				.build());
		hud.addEntry(new HudPlacementEntry());
		hud.addEntry(entries.startBooleanToggle(Component.literal("HUD background"), FlowTierClientConfig.hudBackground)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.hudBackground = value)
				.build());
		hud.addEntry(entries.startBooleanToggle(Component.literal("Show win/loss"), FlowTierClientConfig.hudRecordEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.hudRecordEnabled = value)
				.build());
		hud.addEntry(entries.startBooleanToggle(Component.literal("Show streak"), FlowTierClientConfig.hudStreakEnabled)
				.setDefaultValue(false)
				.setSaveConsumer(value -> FlowTierClientConfig.hudStreakEnabled = value)
				.build());

		ConfigCategory overlay = builder.getOrCreateCategory(Component.literal("Nametag & Tab"));
		overlay.addEntry(entries.startBooleanToggle(Component.literal("Show nametag stats"), FlowTierClientConfig.nametagEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.nametagEnabled = value)
				.build());
		overlay.addEntry(entries.startBooleanToggle(Component.literal("Show tab list stats"), FlowTierClientConfig.tabListEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.tabListEnabled = value)
				.build());
		overlay.addEntry(entries.startTextDescription(Component.literal("Preview: ").append(FlowTierFormatter.previewCompact())).build());

		ConfigCategory appearance = builder.getOrCreateCategory(Component.literal("Appearance"));
		appearance.addEntry(entries.startBooleanToggle(Component.literal("Show gamemode icon"), FlowTierClientConfig.gamemodeIconEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.gamemodeIconEnabled = value)
				.build());
		appearance.addEntry(entries.startBooleanToggle(Component.literal("Show tier"), FlowTierClientConfig.tierEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.tierEnabled = value)
				.build());
		appearance.addEntry(entries.startBooleanToggle(Component.literal("Short tier names"), FlowTierClientConfig.shortTierNames)
				.setDefaultValue(false)
				.setSaveConsumer(value -> FlowTierClientConfig.shortTierNames = value)
				.build());
		appearance.addEntry(entries.startBooleanToggle(Component.literal("Show ELO"), FlowTierClientConfig.eloEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.eloEnabled = value)
				.build());
		appearance.addEntry(entries.startBooleanToggle(Component.literal("Show ELO label"), FlowTierClientConfig.eloLabelEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.eloLabelEnabled = value)
				.build());
		appearance.addEntry(entries.startBooleanToggle(Component.literal("Color ELO by tier"), FlowTierClientConfig.coloredElo)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.coloredElo = value)
				.build());
		appearance.addEntry(entries.startBooleanToggle(Component.literal("Show position"), FlowTierClientConfig.positionEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.positionEnabled = value)
				.build());
		appearance.addEntry(entries.startBooleanToggle(Component.literal("Show position label"), FlowTierClientConfig.positionLabelEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.positionLabelEnabled = value)
				.build());

		builder.setSavingRunnable(FlowTierClientConfig::save);
		return builder.build();
	}
}
