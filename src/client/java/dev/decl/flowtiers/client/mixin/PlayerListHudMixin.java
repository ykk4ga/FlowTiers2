package dev.decl.flowtiers.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.decl.flowtiers.client.FlowTierClientConfig;
import dev.decl.flowtiers.client.FlowTierFormatter;
import dev.decl.flowtiers.client.FlowTierMinecraftCompat;
import dev.decl.flowtiers.client.FlowTiersClientState;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {
	@Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true)
	private void flowtiers$appendTabStats(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
		if (!FlowTierClientConfig.tabListEnabled) return;
		if (FlowTierClientConfig.suppressRankedDuplicates && dev.decl.flowtiers.client.RankedMatchDetector.isInRankedMatch()) return;

		FlowTiersClientState.cache().fetch(FlowTierMinecraftCompat.profileId(entry.getProfile()));
		FlowTiersClientState.cache().getIfFresh(FlowTierMinecraftCompat.profileId(entry.getProfile())).ifPresent(stats ->
				cir.setReturnValue(cir.getReturnValue().copy()
						.append(Text.literal(" "))
						.append(FlowTierFormatter.compact(stats))));
	}
}
