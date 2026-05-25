package dev.fecl.flowtiers.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.fecl.flowtiers.client.FlowTierClientConfig;
import dev.fecl.flowtiers.client.FlowTierFormatter;
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
			Component suffix = FlowTierFormatter.compact(stats);
			String suffixStr = suffix.getString();
			if (suffixStr.isEmpty()) return;

			if (state.nameTag != null && state.nameTag.getString().contains(suffixStr)) return;

			if (FlowTierClientConfig.nametagAlignment == FlowTierClientConfig.NametagAlignment.LEFT) {
				state.nameTag = suffix.copy()
						.append(joiner(suffix, state.nameTag == null ? Component.empty() : state.nameTag))
						.append(state.nameTag == null ? Component.empty() : state.nameTag);
			} else {
				state.nameTag = (state.nameTag == null ? Component.empty() : state.nameTag.copy())
						.append(joiner(state.nameTag == null ? Component.empty() : state.nameTag, suffix))
						.append(suffix);
			}
		});
	}

	private static Component joiner(Component left, Component right) {
		String leftValue = left.getString();
		String rightValue = right.getString();
		return hasSeparatorEdge(leftValue, rightValue) || leftValue.endsWith(" ") || rightValue.startsWith(" ")
				? Component.empty() : Component.literal(" ");
	}

	private static boolean hasSeparatorEdge(String left, String right) {
		return left.endsWith("|") || right.startsWith("|");
	}
}
