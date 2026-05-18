package dev.decl.flowtiers.client.config;

import java.util.function.Consumer;
import java.util.function.Supplier;

import dev.decl.flowtiers.client.FlowTierClientConfig;
import dev.decl.flowtiers.client.FlowTierFormatter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class FlowTiersConfigScreen extends Screen {
	private static final String[] LADDERS = {
			"SWORD", "AXE", "UHC", "VANILLA", "MACE", "SPEAR_MACE", "CART",
			"DIAMOND_POT", "NETHERITE_OP", "SMP", "DIAMOND_SMP", "GLOBAL"
	};
	private static final int PANEL_WIDTH = 340;
	private static final int BUTTON_WIDTH = 158;
	private static final int BUTTON_HEIGHT = 20;
	private static final int ROW_GAP = 24;
	private static final int SECTION_GAP = 14;
	private static final int PANEL_COLOR = 0xAA111318;
	private static final int SECTION_COLOR = 0x66303640;
	private static final int BORDER_COLOR = 0x663F8CFF;
	private static final int TEXT_MUTED = 0xA8B3C7;

	private final Screen parent;

	private FlowTiersConfigScreen(Screen parent) {
		super(Component.literal("FlowTiers"));
		this.parent = parent;
	}

	public static Screen create(Screen parent) {
		return new FlowTiersConfigScreen(parent);
	}

	@Override
	protected void init() {
		int x = this.width / 2 - PANEL_WIDTH / 2 + 12;
		int y = 74;

		y = section(y);
		addRenderableWidget(toggle(x, y, "HUD overlay", () -> FlowTierClientConfig.hudEnabled, value -> FlowTierClientConfig.hudEnabled = value));
		addRenderableWidget(toggle(x + 166, y, "HUD background", () -> FlowTierClientConfig.hudBackground, value -> FlowTierClientConfig.hudBackground = value));
		y += ROW_GAP;
		addRenderableWidget(toggle(x, y, "Win/loss line", () -> FlowTierClientConfig.hudRecordEnabled, value -> FlowTierClientConfig.hudRecordEnabled = value));
		addRenderableWidget(toggle(x + 166, y, "Streak line", () -> FlowTierClientConfig.hudStreakEnabled, value -> FlowTierClientConfig.hudStreakEnabled = value));
		y += ROW_GAP + SECTION_GAP;

		y = section(y);
		addRenderableWidget(toggle(x, y, "Nametag stats", () -> FlowTierClientConfig.nametagEnabled, value -> FlowTierClientConfig.nametagEnabled = value));
		addRenderableWidget(toggle(x + 166, y, "Tab list stats", () -> FlowTierClientConfig.tabListEnabled, value -> FlowTierClientConfig.tabListEnabled = value));
		y += ROW_GAP;

		addRenderableWidget(toggle(x, y, "Hide duplicates", () -> FlowTierClientConfig.suppressRankedDuplicates, value -> FlowTierClientConfig.suppressRankedDuplicates = value));
		addRenderableWidget(cycle(x + 166, y, BUTTON_WIDTH, () -> "Placement: " + pretty(FlowTierClientConfig.nametagAlignment.name()),
				() -> FlowTierClientConfig.nametagAlignment = FlowTierClientConfig.nametagAlignment == FlowTierClientConfig.NametagAlignment.LEFT
						? FlowTierClientConfig.NametagAlignment.RIGHT : FlowTierClientConfig.NametagAlignment.LEFT));
		y += ROW_GAP + SECTION_GAP;

		y = section(y);
		addRenderableWidget(toggle(x, y, "Gamemode icon", () -> FlowTierClientConfig.gamemodeIconEnabled, value -> FlowTierClientConfig.gamemodeIconEnabled = value));
		addRenderableWidget(toggle(x + 166, y, "Tier label", () -> FlowTierClientConfig.tierEnabled, value -> FlowTierClientConfig.tierEnabled = value));
		y += ROW_GAP;

		addRenderableWidget(toggle(x, y, "SR value", () -> FlowTierClientConfig.eloEnabled, value -> FlowTierClientConfig.eloEnabled = value));
		addRenderableWidget(toggle(x + 166, y, "Leaderboard #", () -> FlowTierClientConfig.positionEnabled, value -> FlowTierClientConfig.positionEnabled = value));
		y += ROW_GAP;

		addRenderableWidget(toggle(x, y, "Colored tier", () -> FlowTierClientConfig.coloredTier, value -> FlowTierClientConfig.coloredTier = value));
		addRenderableWidget(toggle(x + 166, y, "Colored SR", () -> FlowTierClientConfig.coloredElo, value -> FlowTierClientConfig.coloredElo = value));
		y += ROW_GAP + SECTION_GAP;

		y = section(y);
		addRenderableWidget(cycle(x, y, BUTTON_WIDTH, () -> "Mode: " + pretty(FlowTierClientConfig.displayMode.name()),
				() -> FlowTierClientConfig.displayMode = nextEnum(FlowTierClientConfig.displayMode, FlowTierClientConfig.DisplayMode.values())));
		addRenderableWidget(cycle(x + 166, y, BUTTON_WIDTH, () -> "Ladder: " + FlowTierFormatter.displayName(FlowTierClientConfig.preferredLadder),
				FlowTiersConfigScreen::nextLadder));

		addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
				.bounds(this.width / 2 - 75, this.height - 30, 150, BUTTON_HEIGHT)
				.build());
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(graphics, mouseX, mouseY, partialTick);
		int panelX = this.width / 2 - PANEL_WIDTH / 2;
		int panelY = 36;
		int panelBottom = this.height - 40;
		graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelBottom, PANEL_COLOR);
		drawBorder(graphics, panelX, panelY, PANEL_WIDTH, panelBottom - panelY, BORDER_COLOR);
		graphics.fill(panelX + 10, 58, panelX + PANEL_WIDTH - 10, 68, 0x55283444);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		int panelX = this.width / 2 - PANEL_WIDTH / 2;
		graphics.centeredText(this.font, this.title, this.width / 2, 18, 0xFFFFFF);
		graphics.centeredText(this.font, FlowTierFormatter.previewCompact(), this.width / 2, 58, 0xFFFFFF);
		drawSectionLabel(graphics, "HUD", panelX + 12, 76);
		drawSectionLabel(graphics, "Nameplates", panelX + 12, 138);
		drawSectionLabel(graphics, "Content", panelX + 12, 200);
		drawSectionLabel(graphics, "Ranking", panelX + 12, 310);
		graphics.centeredText(this.font, Component.literal("Changes save when you leave this screen"), this.width / 2, this.height - 44, TEXT_MUTED);
	}

	@Override
	public void onClose() {
		FlowTierClientConfig.save();
		this.minecraft.setScreen(this.parent);
	}

	private Button toggle(int x, int y, String label, Supplier<Boolean> getter, Consumer<Boolean> setter) {
		return cycle(x, y, BUTTON_WIDTH, () -> label + ": " + (getter.get() ? "On" : "Off"), () -> setter.accept(!getter.get()));
	}

	private Button cycle(int x, int y, int width, Supplier<String> label, Runnable action) {
		return Button.builder(Component.literal(label.get()), button -> {
			action.run();
			button.setMessage(Component.literal(label.get()));
		}).bounds(x, y, width, 20).build();
	}

	private static <T extends Enum<T>> T nextEnum(T current, T[] values) {
		return values[(current.ordinal() + 1) % values.length];
	}

	private int section(int y) {
		return y + 18;
	}

	private void drawSectionLabel(GuiGraphicsExtractor graphics, String label, int x, int y) {
		graphics.fill(x, y, x + PANEL_WIDTH - 24, y + 1, SECTION_COLOR);
		graphics.text(this.font, Component.literal(label), x, y - 10, TEXT_MUTED, false);
	}

	private static void drawBorder(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
		graphics.fill(x, y, x + width, y + 1, color);
		graphics.fill(x, y + height - 1, x + width, y + height, color);
		graphics.fill(x, y, x + 1, y + height, color);
		graphics.fill(x + width - 1, y, x + width, y + height, color);
	}

	private static String pretty(String value) {
		String lower = value.toLowerCase().replace('_', ' ');
		StringBuilder result = new StringBuilder(lower.length());
		boolean capitalize = true;
		for (int i = 0; i < lower.length(); i++) {
			char c = lower.charAt(i);
			if (capitalize && Character.isLetter(c)) {
				result.append(Character.toUpperCase(c));
				capitalize = false;
			} else {
				result.append(c);
			}
			if (c == ' ') capitalize = true;
		}
		return result.toString();
	}

	private static void nextLadder() {
		String current = FlowTierClientConfig.preferredLadder;
		for (int i = 0; i < LADDERS.length; i++) {
			if (LADDERS[i].equals(current)) {
				FlowTierClientConfig.preferredLadder = LADDERS[(i + 1) % LADDERS.length];
				return;
			}
		}
		FlowTierClientConfig.preferredLadder = LADDERS[0];
	}
}
