package dev.decl.flowtiers.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.decl.flowtiers.client.FlowTierClientConfig;
import dev.decl.flowtiers.client.FlowTierFormatter;
import dev.decl.flowtiers.client.FlowTiersClientState;
import dev.decl.flowtiers.client.RankedMatchDetector;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {
	@Inject(method = "getNameForDisplay", at = @At("RETURN"), cancellable = true)
	private void flowtiers$appendTabStats(PlayerInfo entry, CallbackInfoReturnable<Component> cir) {
		if (!FlowTierClientConfig.tabListEnabled) return;

		if (FlowTierClientConfig.suppressRankedDuplicates && cir.getReturnValue() != null) {
			String rawName = cir.getReturnValue().getString();
			if (rawName != null && rawName.matches("^\\d{2,5}[\\s|].*")) return;
			if (RankedMatchDetector.nameAlreadyHasTierInfo(cir.getReturnValue())) return;
		}

		java.util.UUID pid = entry.getProfile().id();
		FlowTiersClientState.cache().fetch(pid);
		FlowTiersClientState.cache().getIfFresh(pid).ifPresent(stats -> {
			Component suffix = FlowTierFormatter.compact(stats);
			String suffixStr = suffix.getString();

			if (cir.getReturnValue() != null && cir.getReturnValue().getString().contains(suffixStr)) return;

			if (FlowTierClientConfig.nametagAlignment == FlowTierClientConfig.NametagAlignment.LEFT) {
				cir.setReturnValue(suffix.copy()
						.append(Component.literal(" "))
						.append(cir.getReturnValue() == null ? Component.empty() : cir.getReturnValue()));
			} else {
				cir.setReturnValue((cir.getReturnValue() == null ? Component.empty() : cir.getReturnValue().copy())
						.append(Component.literal(" "))
						.append(suffix));
			}
		});
	}
}