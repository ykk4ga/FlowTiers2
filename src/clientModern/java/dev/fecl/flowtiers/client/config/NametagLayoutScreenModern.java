package dev.fecl.flowtiers.client.config;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;

class NametagLayoutScreen extends NametagLayoutScreenBase {
	public NametagLayoutScreen(Screen parent) {
		super(parent);
	}

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		return handleMouseClicked(click.x(), click.y(), click.button()) || super.mouseClicked(click, doubled);
	}

	@Override
	public boolean mouseReleased(Click click) {
		return handleMouseReleased(click.x(), click.y(), click.button()) || super.mouseReleased(click);
	}
}
