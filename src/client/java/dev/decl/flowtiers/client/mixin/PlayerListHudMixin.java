package dev.decl.flowtiers.client.mixin;

import dev.decl.flowtiers.client.*;
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
		if (rawName != null && rawName.matches("^\\d{2,5}[\\s|].*")) return;
		if (FlowTierClientConfig.suppressRankedDuplicates && RankedMatchDetector.nameAlreadyHasTierInfo(cir.getReturnValue())) return;

		FlowTiersClientState.cache().fetch(FlowTierMinecraftCompat.profileId(entry.getProfile()));
		FlowTiersClientState.cache().getIfFresh(FlowTierMinecraftCompat.profileId(entry.getProfile())).ifPresent(stats -> {
			Text suffix = FlowTierFormatter.compact(stats);
			if (FlowTierClientConfig.nametagAlignment == FlowTierClientConfig.NametagAlignment.LEFT) {
				cir.setReturnValue(suffix.copy()
						.append(Text.literal(" "))
						.append(cir.getReturnValue()));
			} else {
				cir.setReturnValue(cir.getReturnValue().copy()
						.append(Text.literal(" "))
						.append(suffix));
			}
		});
	}
}
