package dev.fecl.flowtiers.client.config;

import dev.fecl.flowtiers.client.FlowTierClientConfig;
import dev.fecl.flowtiers.client.FlowTierFormatter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
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
		addDrawableChild(ButtonWidget.builder(Text.literal("-"), button -> adjustScale(-0.1F))
				.dimensions(width / 2 - 58, 60, 24, 18).build());
		addDrawableChild(ButtonWidget.builder(Text.literal("+"), button -> adjustScale(0.1F))
				.dimensions(width / 2 + 34, 60, 24, 18).build());
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		// suppress default blur background
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

		context.drawCenteredTextWithShadow(textRenderer, "Drag the FlowTiers HUD", width / 2, 18, 0xFFFFFFFF);
		context.drawCenteredTextWithShadow(textRenderer, "Release to save. Esc or Done returns to config.", width / 2, 32, 0xFF9CA3AF);
		context.drawCenteredTextWithShadow(textRenderer, "X: " + FlowTierClientConfig.hudX + "  Y: " + FlowTierClientConfig.hudY + "  Scale: " + scalePercent() + "%", width / 2, 46, 0xFF93C5FD);
		drawGuides(context, FlowTierClientConfig.hudX, FlowTierClientConfig.hudY);
		drawPreview(context, FlowTierClientConfig.hudX, FlowTierClientConfig.hudY);
		super.render(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 && isInsidePreview((int) mouseX, (int) mouseY)) {
			dragging = true;
			dragOffsetX = (int) mouseX - FlowTierClientConfig.hudX;
			dragOffsetY = (int) mouseY - FlowTierClientConfig.hudY;
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public void close() {
		FlowTierClientConfig.save();
		if (client != null) client.setScreen(parent);
	}

	@Override
	public boolean shouldPause() { return false; }

	private void setHudPosition(int x, int y) {
		FlowTierClientConfig.hudX = clamp(x, 0, width - previewWidth());
		FlowTierClientConfig.hudY = clamp(y, 0, height - previewHeight());
	}

	private void drawPreview(DrawContext context, int x, int y) {
		TextRenderer renderer = textRenderer;
		int pw = basePreviewWidth();
		int ph = basePreviewHeight();
		int scaledWidth = previewWidth();
		int scaledHeight = previewHeight();
		HudPlacementScaleTransform.push(context, x, y, FlowTierClientConfig.hudScale);
		context.fill(0, 0, pw, ph, FlowTierClientConfig.hudBackground ? 0xAA000000 : 0x44000000);
		HudPlacementScaleTransform.pop(context);
		drawBorder(context, x - 2, y - 2, scaledWidth + 4, scaledHeight + 4, dragging ? 0xFFFFFFFF : 0xFF93C5FD);
		drawBorder(context, x, y, scaledWidth, scaledHeight, dragging ? 0xFF93C5FD : 0xFF3B82F6);
		drawHandles(context, x, y, scaledWidth, scaledHeight);
		HudPlacementScaleTransform.push(context, x, y, FlowTierClientConfig.hudScale);
		int tx = PADDING;
		int ty = PADDING;
		Text title = Text.literal("FlowTiers");
		context.drawTextWithShadow(renderer, title, tx, ty, FLOW_BLUE);
		context.drawTextWithShadow(renderer, FlowTierFormatter.icon("SWORD"), tx + renderer.getWidth(title) + renderer.getWidth("  "), ty, 0xFFFFFFFF);
		ty += LINE_HEIGHT;
		context.drawTextWithShadow(renderer, "MT4  800 SR", tx, ty, 0xFFC0C0C0);
		ty += LINE_HEIGHT;
		context.drawTextWithShadow(renderer, "#123 Sword", tx, ty, 0xFFFFD700);
		ty += LINE_HEIGHT;
		context.drawTextWithShadow(renderer, "12W 4L", tx, ty, 0xFFAAAAAA);
		HudPlacementScaleTransform.pop(context);
	}

	private boolean isInsidePreview(int mouseX, int mouseY) {
		int x = FlowTierClientConfig.hudX;
		int y = FlowTierClientConfig.hudY;
		return mouseX >= x && mouseX <= x + previewWidth() && mouseY >= y && mouseY <= y + previewHeight();
	}

	private int basePreviewWidth() {
		MinecraftClient client = MinecraftClient.getInstance();
		TextRenderer r = client.textRenderer;
		int w = r.getWidth("FlowTiers  ") + r.getWidth(FlowTierFormatter.icon("SWORD"));
		w = Math.max(w, r.getWidth("MT4  800 SR"));
		w = Math.max(w, r.getWidth("#123 Sword"));
		w = Math.max(w, r.getWidth("12W 4L"));
		return w + PADDING * 2;
	}

	private int basePreviewHeight() {
		return 4 * LINE_HEIGHT + PADDING * 2;
	}

	private int previewWidth() { return Math.round(basePreviewWidth() * FlowTierClientConfig.hudScale); }
	private int previewHeight() { return Math.round(basePreviewHeight() * FlowTierClientConfig.hudScale); }
	private int scalePercent() { return Math.round(FlowTierClientConfig.hudScale * 100); }
	private void adjustScale(float delta) {
		FlowTierClientConfig.hudScale = FlowTierClientConfig.clampHudScale(FlowTierClientConfig.hudScale + delta);
		setHudPosition(FlowTierClientConfig.hudX, FlowTierClientConfig.hudY);
		FlowTierClientConfig.save();
	}

	private boolean leftMouseReleased() {
		MinecraftClient client = MinecraftClient.getInstance();
		return client == null || GLFW.glfwGetMouseButton(
				client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_RELEASE;
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

	private void drawGuides(DrawContext context, int x, int y) {
		int cx = x + previewWidth() / 2;
		int cy = y + previewHeight() / 2;
		context.fill(cx, 58, cx + 1, height - 30, 0x6638BDF8);
		context.fill(0, cy, width, cy + 1, 0x6638BDF8);
	}

	private static void drawHandles(DrawContext context, int x, int y, int width, int height) {
		context.fill(x - 3, y - 3, x + 2, y + 2, 0xFFFFFFFF);
		context.fill(x + width - 2, y - 3, x + width + 3, y + 2, 0xFFFFFFFF);
		context.fill(x - 3, y + height - 2, x + 2, y + height + 3, 0xFFFFFFFF);
		context.fill(x + width - 2, y + height - 2, x + width + 3, y + height + 3, 0xFFFFFFFF);
	}
}
