package dev.fecl.flowtiers.client.config;

import dev.fecl.flowtiers.client.FlowTierClientConfig;
import dev.fecl.flowtiers.client.FlowTierFormatter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

final class HudPlacementScreen extends Screen {
	private final Screen parent;
	private boolean dragging;
	private int dragOffsetX;
	private int dragOffsetY;

	HudPlacementScreen(Screen parent) {
		super(Component.literal("Place FlowTiers HUD"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		addRenderableWidget(Button.builder(Component.literal("-"), button -> adjustScale(-0.1F))
				.bounds(width / 2 - 58, 60, 24, 18).build());
		addRenderableWidget(Button.builder(Component.literal("+"), button -> adjustScale(0.1F))
				.bounds(width / 2 + 34, 60, 24, 18).build());
	}

	@Override
	public void onClose() {
		FlowTierClientConfig.save();
		if (minecraft != null) minecraft.setScreen(parent);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		super.extractRenderState(context, mouseX, mouseY, delta);
		context.centeredText(font, Component.literal("Drag the FlowTiers HUD"), width / 2, 18, 0xFFFFFFFF);
		context.centeredText(font, Component.literal("Release to save. Esc returns to config."), width / 2, 32, 0xFF9CA3AF);
		context.centeredText(font, Component.literal("X: " + FlowTierClientConfig.hudX + "  Y: " + FlowTierClientConfig.hudY + "  Scale: " + scalePercent() + "%"), width / 2, 46, 0xFF93C5FD);
		drawGuides(context, FlowTierClientConfig.hudX, FlowTierClientConfig.hudY);
		drawPreview(context, FlowTierClientConfig.hudX, FlowTierClientConfig.hudY);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		if (event.button() == 0 && insidePreview(event.x(), event.y())) {
			dragging = true;
			dragOffsetX = (int) event.x() - FlowTierClientConfig.hudX;
			dragOffsetY = (int) event.y() - FlowTierClientConfig.hudY;
			return true;
		}
		return super.mouseClicked(event, doubled);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (dragging) {
			setHudPosition((int) event.x() - dragOffsetX, (int) event.y() - dragOffsetY);
			return true;
		}
		return super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (dragging) {
			dragging = false;
			FlowTierClientConfig.save();
			return true;
		}
		return super.mouseReleased(event);
	}

	private void setHudPosition(int x, int y) {
		FlowTierClientConfig.hudX = clamp(x, 0, width - previewWidth());
		FlowTierClientConfig.hudY = clamp(y, 0, height - previewHeight());
	}

	private boolean insidePreview(double mouseX, double mouseY) {
		int x = FlowTierClientConfig.hudX;
		int y = FlowTierClientConfig.hudY;
		return mouseX >= x && mouseX <= x + previewWidth() && mouseY >= y && mouseY <= y + previewHeight();
	}

	private void drawPreview(GuiGraphicsExtractor context, int x, int y) {
		int w = previewWidth();
		int h = previewHeight();
		context.pose().pushMatrix();
		context.pose().translate(x, y);
		context.pose().scale(FlowTierClientConfig.hudScale, FlowTierClientConfig.hudScale);
		context.fill(0, 0, basePreviewWidth(), basePreviewHeight(), FlowTierClientConfig.hudBackground ? 0xAA000000 : 0x44000000);
		context.text(font, Component.literal("FlowTiers"), 4, 4, 0xFF00BFFF, true);
		context.text(font, FlowTierFormatter.icon("SWORD"), 72, 4, 0xFFFFFFFF, true);
		context.text(font, Component.literal("MT4  800 SR"), 4, 16, 0xFFC0C0C0, true);
		context.text(font, Component.literal("#120 Sword"), 4, 28, 0xFFFFD700, true);
		context.pose().popMatrix();
		drawBorder(context, x - 2, y - 2, w + 4, h + 4, dragging ? 0xFFFFFFFF : 0xFF93C5FD);
		drawBorder(context, x, y, w, h, dragging ? 0xFF93C5FD : 0xFF3B82F6);
		drawHandles(context, x, y, w, h);
	}

	private static int basePreviewWidth() { return 118; }
	private static int basePreviewHeight() { return 42; }
	private static int previewWidth() { return Math.round(basePreviewWidth() * FlowTierClientConfig.hudScale); }
	private static int previewHeight() { return Math.round(basePreviewHeight() * FlowTierClientConfig.hudScale); }
	private static int scalePercent() { return Math.round(FlowTierClientConfig.hudScale * 100); }

	private void adjustScale(float delta) {
		FlowTierClientConfig.hudScale = FlowTierClientConfig.clampHudScale(FlowTierClientConfig.hudScale + delta);
		setHudPosition(FlowTierClientConfig.hudX, FlowTierClientConfig.hudY);
		FlowTierClientConfig.save();
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(value, Math.max(min, max)));
	}

	private static void drawBorder(GuiGraphicsExtractor context, int x, int y, int width, int height, int color) {
		context.fill(x, y, x + width, y + 1, color);
		context.fill(x, y + height - 1, x + width, y + height, color);
		context.fill(x, y, x + 1, y + height, color);
		context.fill(x + width - 1, y, x + width, y + height, color);
	}

	private void drawGuides(GuiGraphicsExtractor context, int x, int y) {
		int cx = x + previewWidth() / 2;
		int cy = y + previewHeight() / 2;
		context.fill(cx, 58, cx + 1, height - 30, 0x6638BDF8);
		context.fill(0, cy, width, cy + 1, 0x6638BDF8);
	}

	private static void drawHandles(GuiGraphicsExtractor context, int x, int y, int width, int height) {
		context.fill(x - 3, y - 3, x + 2, y + 2, 0xFFFFFFFF);
		context.fill(x + width - 2, y - 3, x + width + 3, y + 2, 0xFFFFFFFF);
		context.fill(x - 3, y + height - 2, x + 2, y + height + 3, 0xFFFFFFFF);
		context.fill(x + width - 2, y + height - 2, x + width + 3, y + height + 3, 0xFFFFFFFF);
	}
}
