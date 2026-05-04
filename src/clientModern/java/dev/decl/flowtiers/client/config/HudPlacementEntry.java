package dev.decl.flowtiers.client.config;

import me.shedaniel.clothconfig2.gui.entries.TextListEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

final class HudPlacementEntry extends TextListEntry {
	private static final int BUTTON_WIDTH = 112;
	private static final int BUTTON_HEIGHT = 18;
	private static final int RESET_COLUMN_WIDTH = 42;

	HudPlacementEntry() {
		super(Text.literal("HUD position"), Text.literal("Edit HUD position"));
	}

	@Override
	public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
		MinecraftClient client = MinecraftClient.getInstance();
		int buttonX = x + entryWidth - BUTTON_WIDTH - RESET_COLUMN_WIDTH - 8;
		int buttonY = y + (entryHeight - BUTTON_HEIGHT) / 2;
		boolean buttonHovered = mouseX >= buttonX && mouseX <= buttonX + BUTTON_WIDTH && mouseY >= buttonY && mouseY <= buttonY + BUTTON_HEIGHT;

		context.drawTextWithShadow(client.textRenderer, getFieldName(), x + 4, y + 6, 0xFFFFFFFF);
		context.fill(buttonX, buttonY, buttonX + BUTTON_WIDTH, buttonY + BUTTON_HEIGHT, buttonHovered ? 0xFF3B82F6 : 0xFF1F2937);
		drawBorder(context, buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT, buttonHovered ? 0xFF93C5FD : 0xFF4B5563);
		context.drawCenteredTextWithShadow(client.textRenderer, "Place HUD", buttonX + BUTTON_WIDTH / 2, buttonY + 5, 0xFFFFFFFF);
	}

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client != null) {
			getConfigScreen().saveAll(false);
			client.setScreen(new HudPlacementScreen(getConfigScreen()));
			return true;
		}
		return super.mouseClicked(click, doubled);
	}

	private static void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
		context.fill(x, y, x + width, y + 1, color);
		context.fill(x, y + height - 1, x + width, y + height, color);
		context.fill(x, y, x + 1, y + height, color);
		context.fill(x + width - 1, y, x + width, y + height, color);
	}
}
