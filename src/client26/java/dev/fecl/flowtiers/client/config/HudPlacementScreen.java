package dev.fecl.flowtiers.client.config;

import dev.fecl.flowtiers.client.FlowTierClientConfig;
import dev.fecl.flowtiers.client.FlowTierFormatter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
	public void onClose() {
		FlowTierClientConfig.save();
		if (minecraft != null) {
			minecraft.setScreen(parent);
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		context.fill(0, 0, width, height, 0xC0101420);
		super.extractRenderState(context, mouseX, mouseY, delta);
		context.centeredText(font, Component.literal("Drag the FlowTiers HUD"), width / 2, 18, 0xFFFFFFFF);
		context.centeredText(font, Component.literal("Esc saves and returns"), width / 2, 32, 0xFF9CA3AF);
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
		context.fill(x, y, x + w, y + h, FlowTierClientConfig.hudBackground ? 0xAA000000 : 0x44000000);
		drawBorder(context, x, y, w, h, dragging ? 0xFF93C5FD : 0xFF4B5563);
		context.text(font, Component.literal("FlowTiers"), x + 4, y + 4, 0xFF00BFFF, true);
		context.text(font, FlowTierFormatter.icon("SWORD"), x + 72, y + 4, 0xFFFFFFFF, true);
		context.text(font, Component.literal("MT4  800 SR"), x + 4, y + 16, 0xFFC0C0C0, true);
		context.text(font, Component.literal("#120 Sword"), x + 4, y + 28, 0xFFFFD700, true);
	}

	private static int previewWidth() {
		return 118;
	}

	private static int previewHeight() {
		return 42;
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
}
