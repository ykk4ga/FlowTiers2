package dev.decl.flowtiers.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.decl.flowtiers.client.FlowTierClientConfig;
import dev.decl.flowtiers.client.FlowTierFormatter;
import dev.decl.flowtiers.client.FlowTiersClientState;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {
	@Inject(method = "renderLabelIfPresent(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IF)V", at = @At("HEAD"), cancellable = true)
	private void flowtiers$appendNametagStats(AbstractClientPlayerEntity player, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float tickDelta, CallbackInfo ci) {
		if (!FlowTierClientConfig.nametagEnabled) {
			return;
		}

		FlowTiersClientState.cache().fetch(player.getUuid());
		FlowTiersClientState.cache().getIfFresh(player.getUuid()).ifPresent(stats -> {
			Text suffix = FlowTierFormatter.compact(stats);
			Text name = text.copy().append(Text.literal(" ")).append(suffix);
			((PlayerEntityRendererInvoker) this).flowtiers$renderLabelIfPresent(player, name, matrices, vertexConsumers, light, tickDelta);
			ci.cancel();
		});
	}
}
