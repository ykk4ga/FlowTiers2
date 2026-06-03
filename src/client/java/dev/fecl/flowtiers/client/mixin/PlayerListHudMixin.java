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
			Text cleanName = stripLeadingSeparator(cir.getReturnValue());
			cir.setReturnValue(FlowTierNametagCache.get(FlowTierMinecraftCompat.profileId(entry.getProfile()), stats, cleanName));
		});
	}

	private static Text stripLeadingSeparator(Text text) {
		String raw = text.getString();
		String stripped = raw.replaceFirst("^\\s*\\|\\s*", "");
		return stripped.equals(raw) ? text : Text.literal(stripped);
	}
}
