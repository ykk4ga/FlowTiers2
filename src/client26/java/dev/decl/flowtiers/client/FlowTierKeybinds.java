package dev.decl.flowtiers.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.decl.flowtiers.client.leaderboard.FlowTierLeaderboardScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class FlowTierKeybinds {
	private FlowTierKeybinds() {
	}

	private static final String[] LADDERS = {
			"SWORD", "AXE", "UHC", "VANILLA", "MACE",
			"DIAMOND_POT", "NETHERITE_OP", "SMP", "DIAMOND_SMP", "GLOBAL"
	};

	private static KeyMapping.Category cachedCategory = null;

	private static KeyMapping.Category category() {
		if (cachedCategory == null) {
			cachedCategory = KeyMapping.Category.register(
					Identifier.fromNamespaceAndPath("flowtiers", "category")
			);
		}
		return cachedCategory;
	}

	public static void register() {
		KeyMapping leaderboard = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.flowtiers.open_leaderboard",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_L,
				category()
		));

		KeyMapping cycleForward = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.flowtiers.cycle_mode",
				InputConstants.Type.KEYSYM,
				-1,
				category()
		));

		KeyMapping cycleBack = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.flowtiers.cycle_mode_back",
				InputConstants.Type.KEYSYM,
				-1,
				category()
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			unbindAdvancementsIfConflicting(client, leaderboard);

			while (leaderboard.consumeClick()) {
				if (client.screen instanceof FlowTierLeaderboardScreen) {
					client.setScreen(null);
				} else {
					client.setScreen(new FlowTierLeaderboardScreen(FlowTiersClientState.leaderboardClient()));
				}
			}

			while (cycleForward.consumeClick()) {
				cycleLadder(client, 1);
			}

			while (cycleBack.consumeClick()) {
				cycleLadder(client, -1);
			}
		});
	}

	private static void cycleLadder(Minecraft client, int direction) {
		FlowTierClientConfig.displayMode = FlowTierClientConfig.DisplayMode.PREFERRED_LADDER;
		String current = FlowTierClientConfig.preferredLadder;
		int idx = 0;
		for (int i = 0; i < LADDERS.length; i++) {
			if (LADDERS[i].equals(current)) { idx = i; break; }
		}
		FlowTierClientConfig.preferredLadder = LADDERS[((idx + direction) % LADDERS.length + LADDERS.length) % LADDERS.length];
		FlowTierClientConfig.save();
		if (client.player != null) {
			client.player.sendSystemMessage(
					net.minecraft.network.chat.Component.literal("FlowTiers: " + FlowTierFormatter.displayName(FlowTierClientConfig.preferredLadder))
			);
		}
	}

	private static void unbindAdvancementsIfConflicting(Minecraft client, KeyMapping leaderboardKey) {
		if (KeyMappingHelper.getBoundKeyOf(leaderboardKey).getName().equals("key.keyboard.l")
				&& KeyMappingHelper.getBoundKeyOf(client.options.keyAdvancements).getName().equals("key.keyboard.l")) {
			client.options.keyAdvancements.setKey(InputConstants.UNKNOWN);
			client.options.save();
		}
	}
}