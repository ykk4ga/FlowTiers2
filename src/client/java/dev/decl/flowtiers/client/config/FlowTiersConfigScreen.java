package dev.decl.flowtiers.client.config;

import dev.decl.flowtiers.client.FlowTierClientConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class FlowTiersConfigScreen {
	private FlowTiersConfigScreen() {
	}

	public static Screen create(Screen parent) {
		ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(Text.literal("FlowTiers"));
		ConfigEntryBuilder entries = builder.entryBuilder();

		ConfigCategory display = builder.getOrCreateCategory(Text.literal("Display"));
		display.addEntry(entries.startBooleanToggle(Text.literal("Show HUD"), FlowTierClientConfig.hudEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.hudEnabled = value)
				.build());
		display.addEntry(entries.startBooleanToggle(Text.literal("Show nametag stats"), FlowTierClientConfig.nametagEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.nametagEnabled = value)
				.build());
		display.addEntry(entries.startBooleanToggle(Text.literal("Show tab list stats"), FlowTierClientConfig.tabListEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.tabListEnabled = value)
				.build());

		ConfigCategory hud = builder.getOrCreateCategory(Text.literal("HUD"));
		hud.addEntry(new HudPlacementEntry());
		hud.addEntry(entries.startBooleanToggle(Text.literal("HUD background"), FlowTierClientConfig.hudBackground)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.hudBackground = value)
				.build());
		hud.addEntry(entries.startIntField(Text.literal("HUD X"), FlowTierClientConfig.hudX)
				.setDefaultValue(7)
				.setMin(0)
				.setSaveConsumer(value -> FlowTierClientConfig.hudX = value)
				.build());
		hud.addEntry(entries.startIntField(Text.literal("HUD Y"), FlowTierClientConfig.hudY)
				.setDefaultValue(8)
				.setMin(0)
				.setSaveConsumer(value -> FlowTierClientConfig.hudY = value)
				.build());

		ConfigCategory stats = builder.getOrCreateCategory(Text.literal("Stats"));
		stats.addEntry(entries.startEnumSelector(Text.literal("Display mode"), FlowTierClientConfig.DisplayMode.class, FlowTierClientConfig.displayMode)
				.setDefaultValue(FlowTierClientConfig.DisplayMode.PREFERRED_LADDER)
				.setSaveConsumer(value -> FlowTierClientConfig.displayMode = value)
				.build());
		stats.addEntry(entries.startStrField(Text.literal("Preferred ladder"), FlowTierClientConfig.preferredLadder)
				.setDefaultValue("SWORD")
				.setSaveConsumer(value -> FlowTierClientConfig.preferredLadder = FlowTierClientConfig.normalizeLadder(value))
				.build());
		stats.addEntry(entries.startBooleanToggle(Text.literal("Show icon and tier"), FlowTierClientConfig.rankSectionEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.rankSectionEnabled = value)
				.build());
		stats.addEntry(entries.startBooleanToggle(Text.literal("Short tier names"), FlowTierClientConfig.shortTierNames)
				.setDefaultValue(false)
				.setSaveConsumer(value -> FlowTierClientConfig.shortTierNames = value)
				.build());
		stats.addEntry(entries.startBooleanToggle(Text.literal("Show ELO"), FlowTierClientConfig.eloEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.eloEnabled = value)
				.build());
		stats.addEntry(entries.startBooleanToggle(Text.literal("Show ELO label"), FlowTierClientConfig.eloLabelEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.eloLabelEnabled = value)
				.build());
		stats.addEntry(entries.startBooleanToggle(Text.literal("Show position"), FlowTierClientConfig.positionEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.positionEnabled = value)
				.build());
		stats.addEntry(entries.startBooleanToggle(Text.literal("Show position label"), FlowTierClientConfig.positionLabelEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> FlowTierClientConfig.positionLabelEnabled = value)
				.build());

		builder.setSavingRunnable(FlowTierClientConfig::save);
		return builder.build();
	}
}
