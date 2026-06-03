package dev.fecl.flowtiers.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.fecl.flowtiers.client.FlowTierClientConfig;
import dev.fecl.flowtiers.client.FlowTierNametagCache;
import dev.fecl.flowtiers.client.FlowTiersClientState;
import dev.fecl.flowtiers.client.RankedMatchDetector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Avatar;

@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {
	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
	private void flowtiers$appendNametagStats(Avatar player, AvatarRenderState state, float tickProgress, CallbackInfo ci) {
		if (!FlowTierClientConfig.nametagEnabled) return;

		if (FlowTierClientConfig.suppressRankedDuplicates && state.nameTag != null) {
			String rawName = state.nameTag.getString();
			if (rawName != null && rawName.matches("^\\d{2,5}[\\s|].*")) return;
			if (RankedMatchDetector.nameAlreadyHasTierInfo(state.nameTag)) return;
		}

		FlowTiersClientState.cache().fetch(player.getUUID());
		FlowTiersClientState.cache().getIfFresh(player.getUUID()).ifPresent(stats -> {
			state.nameTag = FlowTierNametagCache.get(player.getUUID(), stats, state.nameTag == null ? Component.empty() : state.nameTag);
		});
	}
}
