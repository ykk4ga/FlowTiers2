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

	private FlowTiersConfigScreen() {}

	public static Screen create(Screen parent) {
		ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(Component.literal("FlowTiers"));
		ConfigEntryBuilder entries = builder.entryBuilder();

		// ── General ──────────────────────────────────────────────────────────
		ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));
		general.addEntry(entries.startEnumSelector(
						Component.literal("Display mode"),
						FlowTierClientConfig.DisplayMode.class,
						FlowTierClientConfig.displayMode)
				.setDefaultValue(FlowTierClientConfig.DisplayMode.PREFERRED_LADDER)
				.setSaveConsumer(value -> FlowTierClientConfig.displayMode = value)
				.build());
		general.addEntry(entries.startSelector(
						Component.literal("Preferred gamemode"), LADDERS, FlowTierClientConfig.preferredLadder)
				.setDefaultValue("SWORD")
				.setNameProvider(value -> Component.literal(FlowTierFormatter.displayName(value)))
				.setSaveConsumer(value -> FlowTierClientConfig.preferredLadder = FlowTierClientConfig.normalizeLadder(value))
				.build());

		// ── HUD ──────────────────────────────────────────────────────────────
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

		// ── Nametag & Tab ─────────────────────────────────────────────────────
		ConfigCategory overlay = builder.getOrCreateCategory(Component.literal("Nametag & Tab"));
		overlay.addEntry(entries.startBooleanToggle(Component.literal("Show nametag stats"), FlowTierClientConfig.nametagEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.nametagEnabled = value)
				.build());
		overlay.addEntry(entries.startBooleanToggle(Component.literal("Show tab list stats"), FlowTierClientConfig.tabListEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.tabListEnabled = value)
				.build());
		overlay.addEntry(entries.startBooleanToggle(Component.literal("Hide stats in ranked matches"), FlowTierClientConfig.suppressRankedDuplicates)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.suppressRankedDuplicates = value)
				.build());
		overlay.addEntry(entries.startEnumSelector(
						Component.literal("Stats position"),
						FlowTierClientConfig.NametagAlignment.class,
						FlowTierClientConfig.nametagAlignment)
				.setDefaultValue(FlowTierClientConfig.NametagAlignment.LEFT)
				.setSaveConsumer(value -> FlowTierClientConfig.nametagAlignment = value)
				.build());
		overlay.addEntry(new NametagLayoutButtonEntry(parent));

		builder.setSavingRunnable(FlowTierClientConfig::save);
		return builder.build();
	}
}