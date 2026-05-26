package dev.fecl.flowtiers.client.mixin;

import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;

import dev.fecl.flowtiers.client.FlowTierClientConfig;
import dev.fecl.flowtiers.client.FlowTierFormatter;
import dev.fecl.flowtiers.client.FlowTiersClientState;
import dev.fecl.flowtiers.client.RankedMatchDetector;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {
	@Inject(method = "getNameForDisplay", at = @At("RETURN"), cancellable = true)
	private void flowtiers$appendTabStats(PlayerInfo entry, CallbackInfoReturnable<Component> cir) {
		if (!FlowTierClientConfig.tabListEnabled) return;

		Component current = cir.getReturnValue();
		if (FlowTierClientConfig.suppressRankedDuplicates && current != null) {
			String rawName = current.getString();
			if (rawName != null && rawName.matches("^\\d{2,5}[\\s|].*")) return;
			if (RankedMatchDetector.nameAlreadyHasTierInfo(current)) return;
		}

		UUID uuid = profileId(entry.getProfile());
		if (uuid == null) return;
		FlowTiersClientState.cache().fetch(uuid);
		FlowTiersClientState.cache().getIfFresh(uuid).ifPresent(stats -> {
			Component suffix = FlowTierFormatter.compact(stats);
			if (suffix.getString().isEmpty()) return;
			if (current != null && current.getString().contains(suffix.getString())) return;

			Component cleanName = stripLeadingSeparator(current == null ? Component.empty() : current);

			if (FlowTierClientConfig.nametagAlignment == FlowTierClientConfig.NametagAlignment.LEFT) {
				cir.setReturnValue(suffix.copy()
						.append(Component.literal(" "))
						.append(cleanName));
			} else {
				cir.setReturnValue(cleanName.copy()
						.append(Component.literal(" "))
						.append(suffix));
			}
		});
	}

	private static Component stripLeadingSeparator(Component component) {
		String raw = component.getString();
		String stripped = raw.replaceFirst("^\\s*\\|\\s*", "");
		return stripped.equals(raw) ? component : Component.literal(stripped);
	}

	private static UUID profileId(GameProfile profile) {
		try {
			return (UUID) GameProfile.class.getMethod("id").invoke(profile);
		} catch (ReflectiveOperationException ignored) {
			try {
				return (UUID) GameProfile.class.getMethod("getId").invoke(profile);
			} catch (ReflectiveOperationException ignoredAgain) {
				return null;
			}
		}
	}
}