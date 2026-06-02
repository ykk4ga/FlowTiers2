package dev.fecl.flowtiers.client;

import net.minecraft.client.gui.GuiGraphics;

final class FlowTierHudScaleTransform {
	private FlowTierHudScaleTransform() {}

	static void push(GuiGraphics graphics, int x, int y, float scale) {
		graphics.pose().pushMatrix();
		graphics.pose().translate(x, y);
		graphics.pose().scale(scale, scale);
	}

	static void pop(GuiGraphics graphics) {
		graphics.pose().popMatrix();
	}
}
