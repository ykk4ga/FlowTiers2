package dev.decl.flowtiers.client;

import dev.decl.flowtiers.client.leaderboard.FlowTierLeaderboardScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public final class FlowTierKeybinds {
	private FlowTierKeybinds() {
	}

	public static void register() {
		KeyBinding leaderboard = KeyBindingHelper.registerKeyBinding(FlowTierMinecraftCompat.keyBinding(
				"key.flowtiers.open_leaderboard",
				InputUtil.GLFW_KEY_L,
				"category.flowtiers"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			unbindAdvancementsIfConflicting(client.options, leaderboard);
			while (leaderboard.wasPressed()) {
				if (client.currentScreen instanceof FlowTierLeaderboardScreen) {
					client.setScreen(null);
				} else {
					client.setScreen(new FlowTierLeaderboardScreen(FlowTiersClientState.leaderboardClient()));
				}
			}
		});
	}

	private static void unbindAdvancementsIfConflicting(GameOptions options, KeyBinding leaderboardKey) {
		KeyBinding advancementsKey = options.advancementsKey;
		if (leaderboardKey.getBoundKeyTranslationKey().equals("key.keyboard.l")
				&& advancementsKey.getBoundKeyTranslationKey().equals("key.keyboard.l")) {
			advancementsKey.setBoundKey(InputUtil.UNKNOWN_KEY);
			KeyBinding.updateKeysByCode();
			options.write();
		}
	}
}
