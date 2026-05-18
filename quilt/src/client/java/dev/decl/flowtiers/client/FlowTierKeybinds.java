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

        KeyBinding viewStats = KeyBindingHelper.registerKeyBinding(FlowTierMinecraftCompat.keyBinding(
                "key.flowtiers.view_stats",
                InputUtil.GLFW_KEY_K,
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

            while (viewStats.wasPressed()) {
                if (client.player != null) {
                    client.setScreen(new dev.decl.flowtiers.client.leaderboard.FlowTierPlayerStatsScreen(
                            null,
                            client.player.getUuid().toString(),
                            client.player.getName().getString()
                    ));
                }
            }
		});
	}

	private static final String[] CYCLE = {
			"MODE:GLOBAL",
			"MODE:HIGHEST_TIER",
			"SWORD", "AXE", "UHC", "VANILLA", "MACE", "SPEAR_MACE", "CART",
			"DIAMOND_POT", "NETHERITE_OP", "SMP", "DIAMOND_SMP"
	};

	private static void cycleLadder(net.minecraft.client.MinecraftClient client, int direction) {
		// find current position in cycle
		String current;
		if (FlowTierClientConfig.displayMode == FlowTierClientConfig.DisplayMode.GLOBAL) {
			current = "MODE:GLOBAL";
		} else if (FlowTierClientConfig.displayMode == FlowTierClientConfig.DisplayMode.HIGHEST_TIER) {
			current = "MODE:HIGHEST_TIER";
		} else {
			current = FlowTierClientConfig.preferredLadder;
		}

		int idx = 0;
		for (int i = 0; i < CYCLE.length; i++) {
			if (CYCLE[i].equals(current)) { idx = i; break; }
		}

		String next = CYCLE[((idx + direction) % CYCLE.length + CYCLE.length) % CYCLE.length];

		if (next.equals("MODE:GLOBAL")) {
			FlowTierClientConfig.displayMode = FlowTierClientConfig.DisplayMode.GLOBAL;
		} else if (next.equals("MODE:HIGHEST_TIER")) {
			FlowTierClientConfig.displayMode = FlowTierClientConfig.DisplayMode.HIGHEST_TIER;
		} else {
			FlowTierClientConfig.displayMode = FlowTierClientConfig.DisplayMode.PREFERRED_LADDER;
			FlowTierClientConfig.preferredLadder = next;
		}

		FlowTierClientConfig.save();

		if (client.player != null) {
			String label = next.equals("MODE:GLOBAL") ? "Global"
					: next.equals("MODE:HIGHEST_TIER") ? "Highest Tier"
					: FlowTierFormatter.displayName(next);

			net.minecraft.text.MutableText msg = net.minecraft.text.Text.literal("FlowTiers: " + label + " ")
					.formatted(net.minecraft.util.Formatting.GREEN);

			if (next.equals("MODE:GLOBAL") || next.equals("MODE:HIGHEST_TIER")) {
				msg.append(FlowTierFormatter.icon("GLOBAL"));
			} else {
				msg.append(FlowTierFormatter.icon(next));
			}

			client.player.sendMessage(msg, true);
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
