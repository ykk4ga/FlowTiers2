package dev.fecl.flowtiers.client.mixin;

import dev.fecl.flowtiers.client.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {
	@Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true)
	private void flowtiers$appendTabStats(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
		if (!FlowTierClientConfig.tabListEnabled) return;

		String rawName = cir.getReturnValue().getString();
		if (rawName != null && rawName.matches("^\\d{2,5}[\\s|].*") && FlowTierClientConfig.suppressRankedDuplicates) return;
		if (FlowTierClientConfig.suppressRankedDuplicates && RankedMatchDetector.nameAlreadyHasTierInfo(cir.getReturnValue())) return;

		FlowTiersClientState.cache().fetch(FlowTierMinecraftCompat.profileId(entry.getProfile()));
		FlowTiersClientState.cache().getIfFresh(FlowTierMinecraftCompat.profileId(entry.getProfile())).ifPresent(stats -> {
			Text suffix = FlowTierFormatter.compact(stats);
			String suffixStr = suffix.getString();
			if (suffixStr.isEmpty()) return;
			if (cir.getReturnValue().getString().contains(suffixStr)) return;

			if (FlowTierClientConfig.nametagAlignment == FlowTierClientConfig.NametagAlignment.LEFT) {
				cir.setReturnValue(suffix.copy()
						.append(joiner(suffix, cir.getReturnValue()))
						.append(cir.getReturnValue()));
			} else {
				cir.setReturnValue(cir.getReturnValue().copy()
						.append(joiner(cir.getReturnValue(), suffix))
						.append(suffix));
			}
		});
	}

	private static Text joiner(Text left, Text right) {
		String leftValue = left.getString();
		String rightValue = right.getString();
		return hasSeparatorEdge(leftValue, rightValue) || leftValue.endsWith(" ") || rightValue.startsWith(" ")
				? Text.empty() : Text.literal(" ");
	}

	private static boolean hasSeparatorEdge(String left, String right) {
		return left.endsWith("|") || right.startsWith("|");
	}
}
