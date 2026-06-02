package dev.fecl.flowtiers.client.config;

import dev.fecl.flowtiers.client.FlowTierClientConfig;
import dev.fecl.flowtiers.client.FlowTierFormatter;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class FlowTiersConfigScreen {
	private static final String[] LADDERS = {
			"SWORD", "AXE", "UHC", "VANILLA", "MACE", "SPEAR_MACE", "CART",
			"DIAMOND_POT", "NETHERITE_OP", "SMP", "DIAMOND_SMP", "GLOBAL"
	};

	private FlowTiersConfigScreen() {}

	public static Screen create(Screen parent) {
		ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(Component.literal("FlowTiers"));
		ConfigEntryBuilder entries = builder.entryBuilder();

		ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));
		general.addEntry(entries.startEnumSelector(
						Component.literal("Display mode"),
						FlowTierClientConfig.DisplayMode.class,
						FlowTierClientConfig.displayMode)
				.setDefaultValue(FlowTierClientConfig.DisplayMode.PREFERRED_LADDER)
				.setEnumNameProvider(value -> Component.literal(displayModeName((FlowTierClientConfig.DisplayMode) value)))
				.setTooltip(Component.literal("Chooses which gamemode is used for your displayed FlowTiers stats."))
				.setSaveConsumer(value -> FlowTierClientConfig.displayMode = value)
				.build());
		general.addEntry(entries.startSelector(
						Component.literal("Preferred gamemode"), LADDERS, FlowTierClientConfig.preferredLadder)
				.setDefaultValue("SWORD")
				.setNameProvider(value -> Component.literal(FlowTierFormatter.displayName(value)))
				.setTooltip(Component.literal("The gamemode shown when Display mode is set to Preferred gamemode."))
				.setSaveConsumer(value -> FlowTierClientConfig.preferredLadder = FlowTierClientConfig.normalizeLadder(value))
				.build());
		general.addEntry(entries.startBooleanToggle(Component.literal("Check for updates"), FlowTierClientConfig.versionCheckEnabled)
				.setDefaultValue(true)
				.setTooltip(Component.literal("Checks Modrinth when you join a server and sends a chat message if an update is available."))
				.setSaveConsumer(value -> FlowTierClientConfig.versionCheckEnabled = value)
				.build());

		ConfigCategory overlay = builder.getOrCreateCategory(Component.literal("Nametag & Tab"));
		overlay.addEntry(new NametagLayoutButtonEntry(parent));
		overlay.addEntry(entries.startBooleanToggle(Component.literal("Show nametag stats"), FlowTierClientConfig.nametagEnabled)
				.setDefaultValue(true)
				.setTooltip(Component.literal("Shows FlowTiers stats above player names."))
				.setSaveConsumer(value -> FlowTierClientConfig.nametagEnabled = value)
				.build());
		overlay.addEntry(entries.startBooleanToggle(Component.literal("Show tab list stats"), FlowTierClientConfig.tabListEnabled)
				.setDefaultValue(true)
				.setTooltip(Component.literal("Shows FlowTiers stats beside player names in the tab list."))
				.setSaveConsumer(value -> FlowTierClientConfig.tabListEnabled = value)
				.build());
		overlay.addEntry(entries.startBooleanToggle(Component.literal("Hide nametag if Ranked System"), FlowTierClientConfig.suppressRankedDuplicates)
				.setDefaultValue(false)
				.setTooltip(Component.literal("Avoids showing duplicate tier information when a server already adds ranked tags."))
				.setSaveConsumer(value -> FlowTierClientConfig.suppressRankedDuplicates = value)
				.build());
		overlay.addEntry(entries.startBooleanToggle(Component.literal("Colored tier"), FlowTierClientConfig.coloredTier)
				.setDefaultValue(true)
				.setTooltip(Component.literal("Colors tier labels based on their FlowTiers rank."))
				.setSaveConsumer(value -> FlowTierClientConfig.coloredTier = value)
				.build());
		overlay.addEntry(entries.startBooleanToggle(Component.literal("Colored SR"), FlowTierClientConfig.coloredElo)
				.setDefaultValue(true)
				.setTooltip(Component.literal("Colors skill rating values based on their rating range."))
				.setSaveConsumer(value -> FlowTierClientConfig.coloredElo = value)
				.build());
		overlay.addEntry(entries.startBooleanToggle(Component.literal("Colored position"), FlowTierClientConfig.coloredPosition)
				.setDefaultValue(true)
				.setTooltip(Component.literal("Colors leaderboard positions using the player's tier color."))
				.setSaveConsumer(value -> FlowTierClientConfig.coloredPosition = value)
				.build());

		ConfigCategory hud = builder.getOrCreateCategory(Component.literal("HUD"));
		hud.addEntry(new HudPlacementEntry());
		hud.addEntry(entries.startBooleanToggle(Component.literal("Show HUD"), FlowTierClientConfig.hudEnabled)
				.setDefaultValue(true)
				.setTooltip(Component.literal("Shows your FlowTiers overview on screen while playing."))
				.setSaveConsumer(value -> FlowTierClientConfig.hudEnabled = value)
				.build());
		hud.addEntry(entries.startBooleanToggle(Component.literal("HUD background"), FlowTierClientConfig.hudBackground)
				.setDefaultValue(true)
				.setTooltip(Component.literal("Adds a dark background behind the HUD for readability."))
				.setSaveConsumer(value -> FlowTierClientConfig.hudBackground = value)
				.build());
		hud.addEntry(entries.startBooleanToggle(Component.literal("Show win/loss"), FlowTierClientConfig.hudRecordEnabled)
				.setDefaultValue(true)
				.setTooltip(Component.literal("Shows your wins and losses in the HUD."))
				.setSaveConsumer(value -> FlowTierClientConfig.hudRecordEnabled = value)
				.build());
		hud.addEntry(entries.startBooleanToggle(Component.literal("Show streak"), FlowTierClientConfig.hudStreakEnabled)
				.setDefaultValue(false)
				.setTooltip(Component.literal("Shows your current win streak in the HUD."))
				.setSaveConsumer(value -> FlowTierClientConfig.hudStreakEnabled = value)
				.build());

		builder.setSavingRunnable(FlowTierClientConfig::save);
		return builder.build();
	}

	private static String displayModeName(FlowTierClientConfig.DisplayMode mode) {
		return switch (mode) {
			case PREFERRED_LADDER -> "Preferred gamemode";
			case HIGHEST_TIER -> "Highest tier";
			case GLOBAL -> "Global";
		};
	}
}
