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

	private static final String[] LADDERS = {
			"SWORD", "AXE", "UHC", "VANILLA", "MACE",
			"DIAMOND_POT", "NETHERITE_OP", "SMP", "DIAMOND_SMP", "GLOBAL"
	};

	public static void register() {
		KeyBinding leaderboard = KeyBindingHelper.registerKeyBinding(FlowTierMinecraftCompat.keyBinding(
				"key.flowtiers.open_leaderboard",
				InputUtil.GLFW_KEY_L,
				"category.flowtiers"
		));

		KeyBinding cycleForward = KeyBindingHelper.registerKeyBinding(FlowTierMinecraftCompat.keyBinding(
				"key.flowtiers.cycle_mode",
				-1,
				"category.flowtiers"
		));

		KeyBinding cycleBack = KeyBindingHelper.registerKeyBinding(FlowTierMinecraftCompat.keyBinding(
				"key.flowtiers.cycle_mode_back",
				-1,
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

			while (cycleForward.wasPressed()) {
				cycleLadder(client, 1);
			}

			while (cycleBack.wasPressed()) {
				cycleLadder(client, -1);
			}
		});
	}

	private static void cycleLadder(net.minecraft.client.MinecraftClient client, int direction) {
		FlowTierClientConfig.displayMode = FlowTierClientConfig.DisplayMode.PREFERRED_LADDER;

		String current = FlowTierClientConfig.preferredLadder;
		int idx = 0;
		for (int i = 0; i < LADDERS.length; i++) {
			if (LADDERS[i].equals(current)) { idx = i; break; }
		}
		FlowTierClientConfig.preferredLadder = LADDERS[((idx + direction) % LADDERS.length + LADDERS.length) % LADDERS.length];
		FlowTierClientConfig.save();
		if (client.player != null) {
			client.player.sendMessage(
					net.minecraft.text.Text.literal("FlowTiers: " + FlowTierFormatter.displayName(FlowTierClientConfig.preferredLadder))
							.formatted(net.minecraft.util.Formatting.GREEN),
					true
			);
		}
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