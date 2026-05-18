package dev.decl.flowtiers.client.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.decl.flowtiers.client.FlowTierClientConfig;
import dev.decl.flowtiers.client.FlowTierFormatter;
import dev.decl.flowtiers.client.FlowTiersClientState;
import dev.decl.flowtiers.client.RankedMatchDetector;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {

	private static final ThreadLocal<Boolean> RENDERING = ThreadLocal.withInitial(() -> false);

	@Inject(method = "renderLabelIfPresent(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IF)V", at = @At("HEAD"), cancellable = true)
	private void flowtiers$appendNametagStats(AbstractClientPlayerEntity player, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float tickDelta, CallbackInfo ci) {
		if (!FlowTierClientConfig.nametagEnabled) return;
		if (RENDERING.get()) return;

		if (FlowTierClientConfig.suppressRankedDuplicates) {
			if (RankedMatchDetector.nameAlreadyHasTierInfo(text)) return;
		}

		FlowTiersClientState.cache().fetch(player.getUuid());
		FlowTiersClientState.cache().getIfFresh(player.getUuid()).ifPresent(stats -> {
			Text suffix = FlowTierFormatter.compact(stats);
			Text name;
			if (FlowTierClientConfig.nametagAlignment == FlowTierClientConfig.NametagAlignment.LEFT) {
				name = suffix.copy().append(Text.literal(" ")).append(text);
			} else {
				name = text.copy().append(Text.literal(" ")).append(suffix);
			}
			RENDERING.set(true);
			try {
				((PlayerEntityRendererInvoker) this).flowtiers$renderLabelIfPresent(player, name, matrices, vertexConsumers, light, tickDelta);
			} finally {
				RENDERING.set(false);
			}
			ci.cancel();
		});
	}
}
