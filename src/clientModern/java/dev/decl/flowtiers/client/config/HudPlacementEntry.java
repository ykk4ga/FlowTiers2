package dev.decl.flowtiers.client.config;

import me.shedaniel.clothconfig2.gui.entries.TextListEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

final class HudPlacementEntry extends TextListEntry {
	private static final int BUTTON_WIDTH = 128;
	private static final int BUTTON_HEIGHT = 20;
	private static final int RESET_COLUMN_WIDTH = 74;
	private int buttonX;
	private int buttonY;

	HudPlacementEntry() {
		super(Text.literal("HUD position"), Text.literal("Edit HUD position"));
	}

	@Override
	public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
		MinecraftClient client = MinecraftClient.getInstance();
		buttonX = x + entryWidth - BUTTON_WIDTH - RESET_COLUMN_WIDTH;
		buttonY = y + (entryHeight - BUTTON_HEIGHT) / 2;
		boolean buttonHovered = isInsideButton(mouseX, mouseY);

		context.drawTextWithShadow(client.textRenderer, getFieldName(), x + 4, y + (entryHeight - client.textRenderer.fontHeight) / 2, 0xFFFFFFFF);
		context.fill(buttonX, buttonY, buttonX + BUTTON_WIDTH, buttonY + BUTTON_HEIGHT, buttonHovered ? 0xFF3B82F6 : 0xFF1F2937);
		drawBorder(context, buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT, buttonHovered ? 0xFF93C5FD : 0xFF4B5563);
		context.drawCenteredTextWithShadow(client.textRenderer, "Place HUD", buttonX + BUTTON_WIDTH / 2, buttonY + (BUTTON_HEIGHT - client.textRenderer.fontHeight) / 2, 0xFFFFFFFF);
	}

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		if (!isInsideButton(click.x(), click.y())) {
			return super.mouseClicked(click, doubled);
		}

		MinecraftClient client = MinecraftClient.getInstance();
		if (client != null) {
			getConfigScreen().saveAll(false);
			client.setScreen(new HudPlacementScreen(getConfigScreen()));
			return true;
		}
		return super.mouseClicked(click, doubled);
	}

	private boolean isInsideButton(double mouseX, double mouseY) {
		return mouseX >= buttonX && mouseX <= buttonX + BUTTON_WIDTH && mouseY >= buttonY && mouseY <= buttonY + BUTTON_HEIGHT;
	}

	private static void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
		context.fill(x, y, x + width, y + 1, color);
		context.fill(x, y + height - 1, x + width, y + height, color);
		context.fill(x, y, x + 1, y + height, color);
		context.fill(x + width - 1, y, x + width, y + height, color);
	}
}
