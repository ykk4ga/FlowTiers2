package dev.fecl.flowtiers.client.config;

import net.minecraft.client.gui.DrawContext;

final class HudPlacementScaleTransform {
	private HudPlacementScaleTransform() {}

	static void push(DrawContext context, int x, int y, float scale) {
		context.getMatrices().pushMatrix();
		context.getMatrices().translate(x, y);
		context.getMatrices().scale(scale, scale);
	}

	static void pop(DrawContext context) {
		context.getMatrices().popMatrix();
	}
}
