package dev.decl.flowtiers.client.config;

import me.shedaniel.clothconfig2.gui.entries.TextListEntry;
import dev.decl.flowtiers.client.FlowTierClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

final class HudPlacementEntry extends TextListEntry {
	private static final int BUTTON_WIDTH = 104;
	private static final int BUTTON_HEIGHT = 20;
	private static final int RESET_WIDTH = 50;
	private static final int GAP = 6;
	private int buttonX;
	private int buttonY;
	private int resetX;

	HudPlacementEntry() {
		super(Component.literal("HUD position"), Component.literal("Edit HUD position"));
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
		Minecraft client = Minecraft.getInstance();
		int controlsWidth = BUTTON_WIDTH + GAP + RESET_WIDTH;
		buttonX = x + entryWidth - controlsWidth - 8;
		resetX = buttonX + BUTTON_WIDTH + GAP;
		buttonY = y + (entryHeight - BUTTON_HEIGHT) / 2;
		boolean buttonHovered = isInsideButton(mouseX, mouseY);
		boolean resetHovered = isInsideReset(mouseX, mouseY);

		context.text(client.font, getFieldName(), x + 4, y + (entryHeight - client.font.lineHeight) / 2, 0xFFFFFFFF, true);
		context.fill(buttonX, buttonY, buttonX + BUTTON_WIDTH, buttonY + BUTTON_HEIGHT, buttonHovered ? 0xFF3B82F6 : 0xFF1F2937);
		drawBorder(context, buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT, buttonHovered ? 0xFF93C5FD : 0xFF4B5563);
		context.centeredText(client.font, Component.literal("Place HUD"), buttonX + BUTTON_WIDTH / 2, buttonY + (BUTTON_HEIGHT - client.font.lineHeight) / 2, 0xFFFFFFFF);
		context.fill(resetX, buttonY, resetX + RESET_WIDTH, buttonY + BUTTON_HEIGHT, resetHovered ? 0xFF374151 : 0xFF111827);
		drawBorder(context, resetX, buttonY, RESET_WIDTH, BUTTON_HEIGHT, resetHovered ? 0xFF9CA3AF : 0xFF4B5563);
		context.centeredText(client.font, Component.literal("Reset"), resetX + RESET_WIDTH / 2, buttonY + (BUTTON_HEIGHT - client.font.lineHeight) / 2, 0xFFFFFFFF);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		if (event.button() != 0 || (!isInsideButton(event.x(), event.y()) && !isInsideReset(event.x(), event.y()))) {
			return super.mouseClicked(event, doubled);
		}

		Minecraft client = Minecraft.getInstance();
		if (client != null) {
			if (isInsideReset(event.x(), event.y())) {
				FlowTierClientConfig.hudX = 7;
				FlowTierClientConfig.hudY = 8;
				FlowTierClientConfig.save();
				return true;
			}
			getConfigScreen().saveAll(false);
			client.setScreen(new HudPlacementScreen(getConfigScreen()));
			return true;
		}
		return super.mouseClicked(event, doubled);
	}

	private boolean isInsideButton(double mouseX, double mouseY) {
		return mouseX >= buttonX && mouseX <= buttonX + BUTTON_WIDTH && mouseY >= buttonY && mouseY <= buttonY + BUTTON_HEIGHT;
	}

	private boolean isInsideReset(double mouseX, double mouseY) {
		return mouseX >= resetX && mouseX <= resetX + RESET_WIDTH && mouseY >= buttonY && mouseY <= buttonY + BUTTON_HEIGHT;
	}

	private static void drawBorder(GuiGraphicsExtractor context, int x, int y, int width, int height, int color) {
		context.fill(x, y, x + width, y + 1, color);
		context.fill(x, y + height - 1, x + width, y + height, color);
		context.fill(x, y, x + 1, y + height, color);
		context.fill(x + width - 1, y, x + width, y + height, color);
	}
}
