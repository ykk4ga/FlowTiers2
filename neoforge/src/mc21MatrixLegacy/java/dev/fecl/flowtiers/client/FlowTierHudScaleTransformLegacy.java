package dev.fecl.flowtiers.client;

import net.minecraft.client.gui.GuiGraphics;

final class FlowTierHudScaleTransform {
	private FlowTierHudScaleTransform() {}

	static void push(GuiGraphics graphics, int x, int y, float scale) {
		graphics.pose().pushPose();
		graphics.pose().translate(x, y, 0);
		graphics.pose().scale(scale, scale, 1);
	}

	static void pop(GuiGraphics graphics) {
		graphics.pose().popPose();
	}
}
