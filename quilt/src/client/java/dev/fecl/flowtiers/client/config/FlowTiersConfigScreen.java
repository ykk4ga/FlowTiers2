package dev.fecl.flowtiers.client.config;

import java.util.function.Consumer;
import java.util.function.Supplier;

import dev.fecl.flowtiers.client.FlowTierClientConfig;
import dev.fecl.flowtiers.client.FlowTierFormatter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

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
	private static final int TEXT_MUTED = 0xFFA8B3C7;

	private final Screen parent;

	private FlowTiersConfigScreen(Screen parent) {
		super(Text.literal("FlowTiers"));
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
		addDrawableChild(toggle(x, y, "HUD overlay", () -> FlowTierClientConfig.hudEnabled, value -> FlowTierClientConfig.hudEnabled = value));
		addDrawableChild(toggle(x + 166, y, "HUD background", () -> FlowTierClientConfig.hudBackground, value -> FlowTierClientConfig.hudBackground = value));
		y += ROW_GAP;
		addDrawableChild(toggle(x, y, "Win/loss line", () -> FlowTierClientConfig.hudRecordEnabled, value -> FlowTierClientConfig.hudRecordEnabled = value));
		addDrawableChild(toggle(x + 166, y, "Streak line", () -> FlowTierClientConfig.hudStreakEnabled, value -> FlowTierClientConfig.hudStreakEnabled = value));
		y += ROW_GAP + SECTION_GAP;

		y = section(y);
		addDrawableChild(toggle(x, y, "Nametag stats", () -> FlowTierClientConfig.nametagEnabled, value -> FlowTierClientConfig.nametagEnabled = value));
		addDrawableChild(toggle(x + 166, y, "Tab list stats", () -> FlowTierClientConfig.tabListEnabled, value -> FlowTierClientConfig.tabListEnabled = value));
		y += ROW_GAP;

		addDrawableChild(toggle(x, y, "Hide duplicates", () -> FlowTierClientConfig.suppressRankedDuplicates, value -> FlowTierClientConfig.suppressRankedDuplicates = value));
		addDrawableChild(cycle(x + 166, y, BUTTON_WIDTH, () -> "Placement: " + pretty(FlowTierClientConfig.nametagAlignment.name()),
				() -> FlowTierClientConfig.nametagAlignment = FlowTierClientConfig.nametagAlignment == FlowTierClientConfig.NametagAlignment.LEFT
						? FlowTierClientConfig.NametagAlignment.RIGHT : FlowTierClientConfig.NametagAlignment.LEFT));
		y += ROW_GAP + SECTION_GAP;

		y = section(y);
		addDrawableChild(toggle(x, y, "Gamemode icon", () -> FlowTierClientConfig.gamemodeIconEnabled, value -> FlowTierClientConfig.gamemodeIconEnabled = value));
		addDrawableChild(toggle(x + 166, y, "Tier label", () -> FlowTierClientConfig.tierEnabled, value -> FlowTierClientConfig.tierEnabled = value));
		y += ROW_GAP;

		addDrawableChild(toggle(x, y, "SR value", () -> FlowTierClientConfig.eloEnabled, value -> FlowTierClientConfig.eloEnabled = value));
		addDrawableChild(toggle(x + 166, y, "Leaderboard #", () -> FlowTierClientConfig.positionEnabled, value -> FlowTierClientConfig.positionEnabled = value));
		y += ROW_GAP;

		addDrawableChild(toggle(x, y, "Colored tier", () -> FlowTierClientConfig.coloredTier, value -> FlowTierClientConfig.coloredTier = value));
		addDrawableChild(toggle(x + 166, y, "Colored SR", () -> FlowTierClientConfig.coloredElo, value -> FlowTierClientConfig.coloredElo = value));
		y += ROW_GAP + SECTION_GAP;

		y = section(y);
		addDrawableChild(cycle(x, y, BUTTON_WIDTH, () -> "Mode: " + pretty(FlowTierClientConfig.displayMode.name()),
				() -> FlowTierClientConfig.displayMode = nextEnum(FlowTierClientConfig.displayMode, FlowTierClientConfig.DisplayMode.values())));
		addDrawableChild(cycle(x + 166, y, BUTTON_WIDTH, () -> "Ladder: " + FlowTierFormatter.displayName(FlowTierClientConfig.preferredLadder),
				FlowTiersConfigScreen::nextLadder));

		addDrawableChild(ButtonWidget.builder(Text.literal("Layout"), button -> {
					if (client != null) client.setScreen(new NametagLayoutScreen(this));
				})
				.dimensions(x, y + ROW_GAP, BUTTON_WIDTH, BUTTON_HEIGHT)
				.build());
		addDrawableChild(ButtonWidget.builder(Text.literal("HUD position"), button -> {
					if (client != null) client.setScreen(new HudPlacementScreen(this));
				})
				.dimensions(x + 166, y + ROW_GAP, BUTTON_WIDTH, BUTTON_HEIGHT)
				.build());

		addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> close())
				.dimensions(this.width / 2 - 75, this.height - 30, 150, BUTTON_HEIGHT)
				.build());
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		super.renderBackground(context, mouseX, mouseY, delta);
		int panelX = this.width / 2 - PANEL_WIDTH / 2;
		int panelY = 36;
		int panelBottom = this.height - 40;
		context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelBottom, PANEL_COLOR);
		drawBorder(context, panelX, panelY, PANEL_WIDTH, panelBottom - panelY, BORDER_COLOR);
		context.fill(panelX + 10, 58, panelX + PANEL_WIDTH - 10, 68, 0x55283444);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		int panelX = this.width / 2 - PANEL_WIDTH / 2;
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 18, 0xFFFFFFFF);
		context.drawCenteredTextWithShadow(this.textRenderer, FlowTierFormatter.previewCompact(), this.width / 2, 58, 0xFFFFFFFF);
		drawSectionLabel(context, "HUD", panelX + 12, 76);
		drawSectionLabel(context, "Nameplates", panelX + 12, 138);
		drawSectionLabel(context, "Content", panelX + 12, 200);
		drawSectionLabel(context, "Ranking", panelX + 12, 310);
		context.drawCenteredTextWithShadow(this.textRenderer, "Changes save when you leave this screen", this.width / 2, this.height - 44, TEXT_MUTED);
	}

	@Override
	public void close() {
		FlowTierClientConfig.save();
		if (client != null) client.setScreen(parent);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	private ButtonWidget toggle(int x, int y, String label, Supplier<Boolean> getter, Consumer<Boolean> setter) {
		return cycle(x, y, BUTTON_WIDTH, () -> label + ": " + (getter.get() ? "On" : "Off"), () -> setter.accept(!getter.get()));
	}

	private ButtonWidget cycle(int x, int y, int width, Supplier<String> label, Runnable action) {
		return ButtonWidget.builder(Text.literal(label.get()), button -> {
			action.run();
			button.setMessage(Text.literal(label.get()));
		}).dimensions(x, y, width, BUTTON_HEIGHT).build();
	}

	private static <T extends Enum<T>> T nextEnum(T current, T[] values) {
		return values[(current.ordinal() + 1) % values.length];
	}

	private int section(int y) {
		return y + 18;
	}

	private void drawSectionLabel(DrawContext context, String label, int x, int y) {
		context.fill(x, y, x + PANEL_WIDTH - 24, y + 1, SECTION_COLOR);
		context.drawTextWithShadow(this.textRenderer, label, x, y - 10, TEXT_MUTED);
	}

	private static void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
		context.fill(x, y, x + width, y + 1, color);
		context.fill(x, y + height - 1, x + width, y + height, color);
		context.fill(x, y, x + 1, y + height, color);
		context.fill(x + width - 1, y, x + width, y + height, color);
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
