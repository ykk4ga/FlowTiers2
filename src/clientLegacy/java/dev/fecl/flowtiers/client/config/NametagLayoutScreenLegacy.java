package dev.fecl.flowtiers.client.config;

import net.minecraft.client.gui.screen.Screen;

class NametagLayoutScreen extends NametagLayoutScreenBase {
	public NametagLayoutScreen(Screen parent) {
		super(parent);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return handleMouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		return handleMouseReleased(mouseX, mouseY, button) || super.mouseReleased(mouseX, mouseY, button);
	}
}
