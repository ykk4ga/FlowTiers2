package dev.fecl.flowtiers.client;

import net.minecraft.client.gui.DrawContext;

final class FlowTierHudScaleTransform {
	private FlowTierHudScaleTransform() {}

	static void push(DrawContext context, int x, int y, float scale) {
		context.getMatrices().push();
		context.getMatrices().translate(x, y, 0);
		context.getMatrices().scale(scale, scale, 1);
	}

	static void pop(DrawContext context) {
		context.getMatrices().pop();
	}
}
