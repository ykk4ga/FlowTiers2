package dev.fecl.flowtiers.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.fecl.flowtiers.client.FlowTierClientConfig;
import dev.fecl.flowtiers.client.FlowTierFormatter;
import dev.fecl.flowtiers.client.FlowTiersClientState;
import dev.fecl.flowtiers.client.RankedMatchDetector;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

@Mixin(Player.class)
public class LunarPlayerEntityMixin {
	private static final String LUNAR_MOD_ID = "ichor";

	@Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true, require = 0)
	private void flowtiers$appendLunarNametagStats(CallbackInfoReturnable<Component> cir) {
		if (!FabricLoader.getInstance().isModLoaded(LUNAR_MOD_ID)) return;
		if (!FlowTierClientConfig.nametagEnabled) return;

		Component original = cir.getReturnValue();
		if (FlowTierClientConfig.suppressRankedDuplicates && original != null) {
			String rawName = original.getString();
			if (rawName != null && rawName.matches("^\\d{2,5}[\\s|].*")) return;
			if (RankedMatchDetector.nameAlreadyHasTierInfo(original)) return;
		}

		Player player = (Player) (Object) this;
		FlowTiersClientState.cache().fetch(player.getUUID());
		FlowTiersClientState.cache().getIfFresh(player.getUUID()).ifPresent(stats -> {
			Component suffix = FlowTierFormatter.compact(stats);
			String suffixString = suffix.getString();
			if (suffixString.isEmpty()) return;

			String originalString = original == null ? "" : original.getString();
			if (originalString.contains(suffixString)) return;

			Component baseName = original == null ? player.getName() : original;
			Component name = FlowTierClientConfig.nametagAlignment == FlowTierClientConfig.NametagAlignment.LEFT
					? suffix.copy().append(joiner(suffix, baseName)).append(baseName)
					: baseName.copy().append(joiner(baseName, suffix)).append(suffix);
			cir.setReturnValue(name);
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
