package dev.decl.flowtiers.client.config;

import dev.decl.flowtiers.client.FlowTierClientConfig;
import dev.decl.flowtiers.client.FlowTierFormatter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

final class HudPlacementScreen extends Screen {
	private static final int PADDING = 3;
	private static final int LINE_HEIGHT = 10;
	private static final int FLOW_BLUE = 0xFF00BFFF;

	private final Screen parent;
	private boolean dragging;
	private int dragOffsetX;
	private int dragOffsetY;

	HudPlacementScreen(Screen parent) {
		super(Text.literal("Place FlowTiers HUD"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> close())
				.dimensions(width / 2 - 42, height - 26, 84, 18)
				.build());
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		if (dragging) {
			if (leftMouseReleased()) {
				dragging = false;
				FlowTierClientConfig.save();
			} else {
				setHudPosition(mouseX - dragOffsetX, mouseY - dragOffsetY);
			}
		}

		context.fill(0, 0, width, height, 0xD0101420);
		context.drawCenteredTextWithShadow(textRenderer, "Drag the FlowTiers HUD", width / 2, 18, 0xFFFFFFFF);
		context.drawCenteredTextWithShadow(textRenderer, "Release to save. Esc or Done returns to config.", width / 2, 32, 0xFF9CA3AF);
		drawPreview(context, FlowTierClientConfig.hudX, FlowTierClientConfig.hudY);
		super.render(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		if (click.button() == 0 && isInsidePreview((int) click.x(), (int) click.y())) {
			dragging = true;
			dragOffsetX = (int) click.x() - FlowTierClientConfig.hudX;
			dragOffsetY = (int) click.y() - FlowTierClientConfig.hudY;
			return true;
		}

		return super.mouseClicked(click, doubled);
	}

	@Override
	public void close() {
		FlowTierClientConfig.save();
		if (client != null) {
			client.setScreen(parent);
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	private void setHudPosition(int x, int y) {
		FlowTierClientConfig.hudX = clamp(x, 0, width - previewWidth());
		FlowTierClientConfig.hudY = clamp(y, 0, height - previewHeight());
	}

	private void drawPreview(DrawContext context, int x, int y) {
		TextRenderer renderer = textRenderer;
		int previewWidth = previewWidth();
		int previewHeight = previewHeight();

		context.fill(x, y, x + previewWidth, y + previewHeight, 0xAA000000);
		drawBorder(context, x, y, previewWidth, previewHeight, dragging ? 0xFF93C5FD : 0xFF3B82F6);

		int tx = x + PADDING;
		int ty = y + PADDING;
		Text title = Text.literal("FlowTiers");
		context.drawTextWithShadow(renderer, title, tx, ty, FLOW_BLUE);
		context.drawTextWithShadow(renderer, FlowTierFormatter.icon("SWORD"), tx + renderer.getWidth(title) + renderer.getWidth("  "), ty, 0xFFFFFFFF);
		ty += LINE_HEIGHT;
		context.drawTextWithShadow(renderer, "MT4  800 SR", tx, ty, 0xFFC0C0C0);
		ty += LINE_HEIGHT;
		context.drawTextWithShadow(renderer, "#123 Sword", tx, ty, 0xFFFFD700);
		ty += LINE_HEIGHT;
		context.drawTextWithShadow(renderer, "12W 4L", tx, ty, 0xFFAAAAAA);
	}

	private boolean isInsidePreview(int mouseX, int mouseY) {
		int x = FlowTierClientConfig.hudX;
		int y = FlowTierClientConfig.hudY;
		return mouseX >= x && mouseX <= x + previewWidth() && mouseY >= y && mouseY <= y + previewHeight();
	}

	private int previewWidth() {
		MinecraftClient client = MinecraftClient.getInstance();
		TextRenderer renderer = client.textRenderer;
		int width = renderer.getWidth("FlowTiers  ") + renderer.getWidth(FlowTierFormatter.icon("SWORD"));
		width = Math.max(width, renderer.getWidth("MT4  800 SR"));
		width = Math.max(width, renderer.getWidth("#123 Sword"));
		width = Math.max(width, renderer.getWidth("12W 4L"));
		return width + PADDING * 2;
	}

	private int previewHeight() {
		return 4 * LINE_HEIGHT + PADDING * 2;
	}

	private boolean leftMouseReleased() {
		MinecraftClient client = MinecraftClient.getInstance();
		return client == null || GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_RELEASE;
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(value, Math.max(min, max)));
	}

	private static void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
		context.fill(x, y, x + width, y + 1, color);
		context.fill(x, y + height - 1, x + width, y + height, color);
		context.fill(x, y, x + 1, y + height, color);
		context.fill(x + width - 1, y, x + width, y + height, color);
	}
}
