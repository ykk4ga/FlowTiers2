package dev.fecl.flowtiers.client.config;

import dev.fecl.flowtiers.client.FlowTierClientConfig;
import dev.fecl.flowtiers.client.FlowTierFormatter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

class NametagLayoutScreenBase extends Screen {
	private static final int START_Y = 62;
	private static final int ROW_H = 28;
	private static final int CARD_W = 150;

	private final Screen parent;
	private final List<FlowTierClientConfig.NametagComponent> left = new ArrayList<>();
	private final List<FlowTierClientConfig.NametagComponent> right = new ArrayList<>();
	private FlowTierClientConfig.NametagComponent dragging;
	private FlowTierClientConfig.NametagComponent selected;

	NametagLayoutScreenBase(Screen parent) {
		super(Text.literal("Nametag Layout"));
		this.parent = parent;
		left.addAll(FlowTierClientConfig.nametagLeftOrder);
		right.addAll(FlowTierClientConfig.nametagRightOrder);
	}

	@Override
	protected void init() {
		clearChildren();
		addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> close())
				.dimensions(width / 2 - 50, height - 28, 100, 20).build());
		if (selected == null) return;

		int panelX = Math.max(8, width / 2 - 100);
		int panelY = Math.min(height - 96, START_Y + 96);
		ButtonWidget leftSeparator = addDrawableChild(ButtonWidget.builder(separatorLabel("Left", FlowTierClientConfig.Edge.LEFT), button -> {
			toggleSeparator(FlowTierClientConfig.Edge.LEFT);
			init();
		}).dimensions(panelX, panelY + 22, 96, 20).build());
		leftSeparator.active = canToggle(FlowTierClientConfig.Edge.LEFT);
		ButtonWidget rightSeparator = addDrawableChild(ButtonWidget.builder(separatorLabel("Right", FlowTierClientConfig.Edge.RIGHT), button -> {
			toggleSeparator(FlowTierClientConfig.Edge.RIGHT);
			init();
		}).dimensions(panelX + 104, panelY + 22, 96, 20).build());
		rightSeparator.active = canToggle(FlowTierClientConfig.Edge.RIGHT);
		addDrawableChild(ButtonWidget.builder(Text.literal("Visible: " + (isEnabled(selected) ? "ON" : "OFF")), button -> {
			toggleEnabled(selected);
			init();
		}).dimensions(panelX, panelY + 46, 96, 20).build());
		if (selected == FlowTierClientConfig.NametagComponent.ELO || selected == FlowTierClientConfig.NametagComponent.POSITION) {
			addDrawableChild(ButtonWidget.builder(Text.literal("Label: " + (labelEnabled(selected) ? "ON" : "OFF")), button -> {
				toggleLabel(selected);
				init();
			}).dimensions(panelX + 104, panelY + 46, 96, 20).build());
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		context.fill(0, 0, width, height, 0xE0101420);
		super.render(context, mouseX, mouseY, delta);
		int leftX = leftX();
		int rightX = rightX();
		context.drawCenteredTextWithShadow(textRenderer, "Nametag Layout", width / 2, 12, 0xFF00BFFF);
		context.drawCenteredTextWithShadow(textRenderer, "Drag modules between sides. Right-click a module for settings.", width / 2, 28, 0xFFAAAAAA);
		context.drawCenteredTextWithShadow(textRenderer, "LEFT OF NAME", leftX + CARD_W / 2, 46, 0xFF9CA3AF);
		context.drawCenteredTextWithShadow(textRenderer, "RIGHT OF NAME", rightX + CARD_W / 2, 46, 0xFF9CA3AF);
		drawZone(context, left, leftX);
		drawZone(context, right, rightX);
		context.drawCenteredTextWithShadow(textRenderer, FlowTierFormatter.previewCompact(), width / 2, START_Y + 68, 0xFFFFFFFF);
		if (selected != null) {
			context.drawCenteredTextWithShadow(textRenderer, componentName(selected) + " settings", width / 2, Math.min(height - 96, START_Y + 96), 0xFFFFFFFF);
		}
		if (dragging != null) drawCard(context, dragging, mouseX - CARD_W / 2, mouseY - 10, true);
	}

	protected boolean handleMouseClicked(double mouseX, double mouseY, int button) {
		FlowTierClientConfig.NametagComponent component = moduleAt(mouseX, mouseY);
		if (component != null && button == 0) {
			dragging = component;
			return true;
		}
		if (component != null && button == 1) {
			selected = component;
			init();
			return true;
		}
		return false;
	}

	protected boolean handleMouseReleased(double mouseX, double mouseY, int button) {
		if (dragging != null && button == 0) {
			List<FlowTierClientConfig.NametagComponent> destination = mouseX < width / 2 ? left : right;
			left.remove(dragging);
			right.remove(dragging);
			destination.add(dropIndex(destination, mouseY), dragging);
			dragging = null;
			sync();
			return true;
		}
		return false;
	}

	private void drawZone(DrawContext context, List<FlowTierClientConfig.NametagComponent> modules, int x) {
		context.fill(x - 4, START_Y - 4, x + CARD_W + 4, START_Y + Math.max(1, modules.size()) * ROW_H + 2, 0x332A3345);
		for (int i = 0; i < modules.size(); i++) drawCard(context, modules.get(i), x, START_Y + i * ROW_H, false);
	}

	private void drawCard(DrawContext context, FlowTierClientConfig.NametagComponent component, int x, int y, boolean floating) {
		int color = component == selected ? 0xFF185B78 : floating ? 0xEE256D85 : 0xCC263344;
		context.fill(x, y, x + CARD_W, y + 22, color);
		context.drawTextWithShadow(textRenderer, edgeMark(component, FlowTierClientConfig.Edge.LEFT) + componentName(component)
				+ edgeMark(component, FlowTierClientConfig.Edge.RIGHT), x + 6, y + 7, isEnabled(component) ? 0xFFFFFFFF : 0xFF888888);
	}

	private FlowTierClientConfig.NametagComponent moduleAt(double mouseX, double mouseY) {
		FlowTierClientConfig.NametagComponent component = moduleAt(left, leftX(), mouseX, mouseY);
		return component == null ? moduleAt(right, rightX(), mouseX, mouseY) : component;
	}

	private FlowTierClientConfig.NametagComponent moduleAt(List<FlowTierClientConfig.NametagComponent> modules, int x, double mouseX, double mouseY) {
		if (mouseX < x || mouseX > x + CARD_W || mouseY < START_Y) return null;
		int index = (int) ((mouseY - START_Y) / ROW_H);
		return index >= 0 && index < modules.size() && mouseY <= START_Y + index * ROW_H + 22 ? modules.get(index) : null;
	}

	private int dropIndex(List<FlowTierClientConfig.NametagComponent> destination, double mouseY) {
		return Math.max(0, Math.min(destination.size(), (int) ((mouseY - START_Y + ROW_H / 2.0) / ROW_H)));
	}

	private void sync() {
		FlowTierClientConfig.setNametagLayout(left, right);
		left.clear();
		left.addAll(FlowTierClientConfig.nametagLeftOrder);
		right.clear();
		right.addAll(FlowTierClientConfig.nametagRightOrder);
	}

	private boolean canToggle(FlowTierClientConfig.Edge edge) {
		return FlowTierClientConfig.hasSeparator(selected, edge) || FlowTierClientConfig.canEnableSeparator(selected, edge);
	}

	private void toggleSeparator(FlowTierClientConfig.Edge edge) {
		FlowTierClientConfig.setSeparator(selected, edge, !FlowTierClientConfig.hasSeparator(selected, edge));
	}

	private Text separatorLabel(String side, FlowTierClientConfig.Edge edge) {
		return Text.literal(side + " |: " + (FlowTierClientConfig.hasSeparator(selected, edge) ? "ON" : "OFF"));
	}

	private static String edgeMark(FlowTierClientConfig.NametagComponent component, FlowTierClientConfig.Edge edge) {
		if (!FlowTierClientConfig.hasSeparator(component, edge)) return "";
		return edge == FlowTierClientConfig.Edge.LEFT ? "| " : " |";
	}

	private int leftX() { return Math.max(8, width / 2 - CARD_W - 90); }
	private int rightX() { return Math.min(width - CARD_W - 8, width / 2 + 90); }

	private static boolean isEnabled(FlowTierClientConfig.NametagComponent component) {
		return switch (component) {
			case GAMEMODE_ICON -> FlowTierClientConfig.gamemodeIconEnabled;
			case TIER -> FlowTierClientConfig.tierEnabled;
			case ELO -> FlowTierClientConfig.eloEnabled;
			case POSITION -> FlowTierClientConfig.positionEnabled;
		};
	}

	private static void toggleEnabled(FlowTierClientConfig.NametagComponent component) {
		switch (component) {
			case GAMEMODE_ICON -> FlowTierClientConfig.gamemodeIconEnabled = !FlowTierClientConfig.gamemodeIconEnabled;
			case TIER -> FlowTierClientConfig.tierEnabled = !FlowTierClientConfig.tierEnabled;
			case ELO -> FlowTierClientConfig.eloEnabled = !FlowTierClientConfig.eloEnabled;
			case POSITION -> FlowTierClientConfig.positionEnabled = !FlowTierClientConfig.positionEnabled;
		}
	}

	private static boolean labelEnabled(FlowTierClientConfig.NametagComponent component) {
		return component == FlowTierClientConfig.NametagComponent.ELO ? FlowTierClientConfig.eloLabelEnabled : FlowTierClientConfig.positionLabelEnabled;
	}

	private static void toggleLabel(FlowTierClientConfig.NametagComponent component) {
		if (component == FlowTierClientConfig.NametagComponent.ELO) FlowTierClientConfig.eloLabelEnabled = !FlowTierClientConfig.eloLabelEnabled;
		if (component == FlowTierClientConfig.NametagComponent.POSITION) FlowTierClientConfig.positionLabelEnabled = !FlowTierClientConfig.positionLabelEnabled;
	}

	private static String componentName(FlowTierClientConfig.NametagComponent component) {
		return switch (component) {
			case GAMEMODE_ICON -> "Gamemode Icon";
			case TIER -> "Tier";
			case ELO -> "SR";
			case POSITION -> "Position";
		};
	}

	@Override
	public void close() {
		sync();
		FlowTierClientConfig.save();
		if (client != null) client.setScreen(parent);
	}

	@Override
	public boolean shouldPause() { return false; }
}
