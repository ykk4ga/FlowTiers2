package dev.fecl.flowtiers.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.fecl.flowtiers.client.FlowTierClientConfig;
import dev.fecl.flowtiers.client.FlowTierFormatter;
import dev.fecl.flowtiers.client.FlowTiersClientState;
import dev.fecl.flowtiers.client.RankedMatchDetector;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.injection.Coerce;

@Mixin(targets = "net.minecraft.client.renderer.entity.player.AvatarRenderer")
public class AvatarRendererMixin {
	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
	private void flowtiers$appendNametagStats(@Coerce Object player, @Coerce Object state, float partialTick, CallbackInfo ci) {
		if (!FlowTierClientConfig.nametagEnabled) return;

		Component current = nameTag(state);
		if (current == null) current = playerName(player);
		if (FlowTierClientConfig.suppressRankedDuplicates && RankedMatchDetector.nameAlreadyHasTierInfo(current)) return;

		java.util.UUID uuid = playerUuid(player);
		if (uuid == null) return;

		FlowTiersClientState.cache().fetch(uuid);
		FlowTiersClientState.cache().getIfFresh(uuid).ifPresent(stats -> {
			Component suffix = FlowTierFormatter.compact(stats);
			Component liveName = nameTag(state);
			if (liveName == null) liveName = playerName(player);
			if (liveName != null && liveName.getString().contains(suffix.getString())) return;

			if (FlowTierClientConfig.nametagAlignment == FlowTierClientConfig.NametagAlignment.LEFT) {
				setNameTag(state, suffix.copy()
						.append(Component.literal(" "))
						.append(liveName == null ? Component.empty() : liveName));
			} else {
				setNameTag(state, (liveName == null ? Component.empty() : liveName.copy())
						.append(Component.literal(" "))
						.append(suffix));
			}
		});
	}

	private static java.util.UUID playerUuid(Object player) {
		try {
			return (java.util.UUID) player.getClass().getMethod("getUUID").invoke(player);
		} catch (ReflectiveOperationException ignored) {
			return null;
		}
	}

	private static Component playerName(Object player) {
		try {
			return (Component) player.getClass().getMethod("getName").invoke(player);
		} catch (ReflectiveOperationException ignored) {
			return Component.empty();
		}
	}

	private static Component nameTag(Object state) {
		try {
			Object value = state.getClass().getField("nameTag").get(state);
			return value instanceof Component component ? component : null;
		} catch (ReflectiveOperationException ignored) {
			return null;
		}
	}

	private static void setNameTag(Object state, Component nameTag) {
		try {
			state.getClass().getField("nameTag").set(state, nameTag);
		} catch (ReflectiveOperationException ignored) {
		}
	}
}
