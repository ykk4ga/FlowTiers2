package dev.decl.flowtiers.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.decl.flowtiers.client.FlowTierClientConfig;
import dev.decl.flowtiers.client.FlowTierFormatter;
import dev.decl.flowtiers.client.FlowTiersClientState;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.text.Text;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {
	@Inject(method = "updateRenderState(Lnet/minecraft/entity/PlayerLikeEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V", at = @At("TAIL"))
	private void flowtiers$appendNametagStats(PlayerLikeEntity player, PlayerEntityRenderState state, float tickProgress, CallbackInfo ci) {
		if (!FlowTierClientConfig.nametagEnabled) {
			return;
		}

		FlowTiersClientState.cache().fetch(player.getUuid());
		FlowTiersClientState.cache().getIfFresh(player.getUuid()).ifPresent(stats -> {
			Text suffix = FlowTierFormatter.compact(stats);
			state.displayName = state.displayName == null
					? suffix
					: state.displayName.copy().append(Text.literal(" ")).append(suffix);
			state.playerName = state.playerName == null
					? suffix
					: state.playerName.copy().append(Text.literal(" ")).append(suffix);
		});
	}
}
