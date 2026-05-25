package dev.fecl.flowtiers.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.fecl.flowtiers.client.FlowTierClientConfig;
import dev.fecl.flowtiers.client.FlowTierFormatter;
import dev.fecl.flowtiers.client.FlowTiersClientState;
import dev.fecl.flowtiers.client.RankedMatchDetector;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.text.Text;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {
	@Inject(method = "updateRenderState(Lnet/minecraft/entity/PlayerLikeEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V", at = @At("TAIL"))
	private void flowtiers$appendNametagStats(PlayerLikeEntity player, PlayerEntityRenderState state, float tickProgress, CallbackInfo ci) {
		if (!FlowTierClientConfig.nametagEnabled) return;

		Text name = renderName(player, state);
		if (FlowTierClientConfig.suppressRankedDuplicates && name != null) {
			if (RankedMatchDetector.nameAlreadyHasTierInfo(name)) return;
		}

		FlowTiersClientState.cache().fetch(player.getUuid());
		FlowTiersClientState.cache().getIfFresh(player.getUuid()).ifPresent(stats -> {
			Text suffix = FlowTierFormatter.compact(stats);
			String suffixStr = suffix.getString();
			if (suffixStr.isEmpty()) return;

			Text currentName = renderName(player, state);

			if (currentName != null && currentName.getString().contains(suffixStr)) return;

			if (FlowTierClientConfig.nametagAlignment == FlowTierClientConfig.NametagAlignment.LEFT) {
				state.displayName = suffix.copy()
						.append(Text.literal(" "))
						.append(currentName == null ? Text.empty() : currentName);
			} else {
				state.displayName = (currentName == null ? Text.empty() : currentName.copy())
						.append(Text.literal(" "))
						.append(suffix);
			}
		});
	}

	private static Text renderName(PlayerLikeEntity player, PlayerEntityRenderState state) {
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
