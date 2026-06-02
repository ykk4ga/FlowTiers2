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
				.setEnumNameProvider(value -> Text.literal(displayModeName((FlowTierClientConfig.DisplayMode) value)))
				.setTooltip(Text.literal("Chooses which gamemode is used for your displayed FlowTiers stats."))
				.setSaveConsumer(value -> FlowTierClientConfig.displayMode = value)
				.build());
		general.addEntry(entries.startSelector(
						Text.literal("Preferred gamemode"), LADDERS, FlowTierClientConfig.preferredLadder)
				.setDefaultValue("SWORD")
				.setNameProvider(value -> Text.literal(FlowTierFormatter.displayName(value)))
				.setTooltip(Text.literal("The gamemode shown when Display mode is set to Preferred gamemode."))
				.setSaveConsumer(value -> FlowTierClientConfig.preferredLadder = FlowTierClientConfig.normalizeLadder(value))
				.build());
		general.addEntry(entries.startBooleanToggle(Text.literal("Check for updates"), FlowTierClientConfig.versionCheckEnabled)
				.setDefaultValue(true)
				.setTooltip(Text.literal("Checks Modrinth when you join a server and sends a chat message if an update is available."))
				.setSaveConsumer(value -> FlowTierClientConfig.versionCheckEnabled = value)
				.build());

		ConfigCategory overlay = builder.getOrCreateCategory(Text.literal("Nametag & Tab"));
		overlay.addEntry(new NametagLayoutButtonEntry(parent));
		overlay.addEntry(entries.startBooleanToggle(Text.literal("Show nametag stats"), FlowTierClientConfig.nametagEnabled)
				.setDefaultValue(true)
				.setTooltip(Text.literal("Shows FlowTiers stats above player names."))
				.setSaveConsumer(value -> FlowTierClientConfig.nametagEnabled = value)
				.build());
		overlay.addEntry(entries.startBooleanToggle(Text.literal("Hide nametag if Ranked System"), FlowTierClientConfig.suppressRankedDuplicates)
				.setDefaultValue(true)
				.setTooltip(Text.literal("Avoids showing duplicate tier information when a server already adds ranked tags."))
				.setSaveConsumer(value -> FlowTierClientConfig.suppressRankedDuplicates = value)
				.build());
		overlay.addEntry(entries.startBooleanToggle(Text.literal("Show tab list stats"), FlowTierClientConfig.tabListEnabled)
				.setDefaultValue(true)
				.setTooltip(Text.literal("Shows FlowTiers stats beside player names in the tab list."))
				.setSaveConsumer(value -> FlowTierClientConfig.tabListEnabled = value)
				.build());
		overlay.addEntry(entries.startBooleanToggle(Text.literal("Colored tier in nametag"), FlowTierClientConfig.coloredTier)
				.setDefaultValue(true)
				.setTooltip(Text.literal("Colors tier labels based on their FlowTiers rank."))
				.setSaveConsumer(value -> FlowTierClientConfig.coloredTier = value)
				.build());
		overlay.addEntry(entries.startBooleanToggle(Text.literal("Colored SR in nametag"), FlowTierClientConfig.coloredElo)
				.setDefaultValue(true)
				.setTooltip(Text.literal("Colors skill rating values based on their rating range."))
				.setSaveConsumer(value -> FlowTierClientConfig.coloredElo = value)
				.build());
		overlay.addEntry(entries.startBooleanToggle(Text.literal("Colored position in nametag"), FlowTierClientConfig.coloredPosition)
				.setDefaultValue(true)
				.setTooltip(Text.literal("Colors leaderboard positions using the player's tier color."))
				.setSaveConsumer(value -> FlowTierClientConfig.coloredPosition = value)
				.build());

		ConfigCategory hud = builder.getOrCreateCategory(Text.literal("HUD"));
		hud.addEntry(new HudPlacementEntry());
		hud.addEntry(entries.startBooleanToggle(Text.literal("Show HUD"), FlowTierClientConfig.hudEnabled)
				.setDefaultValue(true)
				.setTooltip(Text.literal("Shows your FlowTiers overview on screen while playing."))
				.setSaveConsumer(value -> FlowTierClientConfig.hudEnabled = value)
				.build());
		hud.addEntry(entries.startBooleanToggle(Text.literal("HUD background"), FlowTierClientConfig.hudBackground)
				.setDefaultValue(true)
				.setTooltip(Text.literal("Adds a dark background behind the HUD for readability."))
				.setSaveConsumer(value -> FlowTierClientConfig.hudBackground = value)
				.build());
		hud.addEntry(entries.startBooleanToggle(Text.literal("Show win/loss"), FlowTierClientConfig.hudRecordEnabled)
				.setDefaultValue(true)
				.setTooltip(Text.literal("Shows your wins and losses in the HUD."))
				.setSaveConsumer(value -> FlowTierClientConfig.hudRecordEnabled = value)
				.build());
		hud.addEntry(entries.startBooleanToggle(Text.literal("Show streak"), FlowTierClientConfig.hudStreakEnabled)
				.setDefaultValue(false)
				.setTooltip(Text.literal("Shows your current win streak in the HUD."))
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
