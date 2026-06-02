package dev.fecl.flowtiers.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.fecl.flowtiers.client.FlowTierClientConfig;
import dev.fecl.flowtiers.client.FlowTierFormatter;
import dev.fecl.flowtiers.client.FlowTiersClientState;
import dev.fecl.flowtiers.client.RankedMatchDetector;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.text.Text;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {
	@Inject(method = "updateRenderState(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V", at = @At("TAIL"))
	private void flowtiers$appendNametagStats(AbstractClientPlayerEntity player, PlayerEntityRenderState state, float tickProgress, CallbackInfo ci) {
		if (!FlowTierClientConfig.nametagEnabled) return;

		Text name = renderName(player, state);
		if (FlowTierClientConfig.suppressRankedDuplicates && name != null) {
			if (RankedMatchDetector.nameAlreadyHasTierInfo(name)) return;
		}

		FlowTiersClientState.cache().fetch(player.getUuid());
		FlowTiersClientState.cache().getIfFresh(player.getUuid()).ifPresent(stats -> {
			Text currentName = renderName(player, state);
			state.displayName = FlowTierFormatter.nametag(stats, currentName == null ? Text.empty() : currentName);
		});
	}

	private static Text renderName(AbstractClientPlayerEntity player, PlayerEntityRenderState state) {
		if (state.displayName != null) {
			return state.displayName;
		}
		if (state.playerName != null) {
			return state.playerName;
		}

		Text displayName = player.getDisplayName();
		return displayName == null ? player.getName() : displayName;
	}
}
