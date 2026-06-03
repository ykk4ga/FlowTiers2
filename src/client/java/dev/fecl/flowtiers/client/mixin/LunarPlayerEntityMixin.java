package dev.fecl.flowtiers.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.fecl.flowtiers.client.FlowTierClientConfig;
import dev.fecl.flowtiers.client.FlowTierNametagCache;
import dev.fecl.flowtiers.client.FlowTiersClientState;
import dev.fecl.flowtiers.client.RankedMatchDetector;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

@Mixin(PlayerEntity.class)
public class LunarPlayerEntityMixin {
	private static final String LUNAR_MOD_ID = "ichor";

	@Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true, require = 0)
	private void flowtiers$appendLunarNametagStats(CallbackInfoReturnable<Text> cir) {
		if (!FabricLoader.getInstance().isModLoaded(LUNAR_MOD_ID)) return;
		if (!FlowTierClientConfig.nametagEnabled) return;

		Text original = cir.getReturnValue();
		if (FlowTierClientConfig.suppressRankedDuplicates && RankedMatchDetector.nameAlreadyHasTierInfo(original)) return;

		PlayerEntity player = (PlayerEntity) (Object) this;
		FlowTiersClientState.cache().fetch(player.getUuid());
		FlowTiersClientState.cache().getIfFresh(player.getUuid()).ifPresent(stats -> {
			Text baseName = original == null ? player.getName() : original;
			cir.setReturnValue(FlowTierNametagCache.get(player.getUuid(), stats, baseName));
		});
	}
}
