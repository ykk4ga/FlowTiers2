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

	public static void register() {
		KeyMapping leaderboard = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.flowtiers.open_leaderboard",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_L,
				KeyMapping.Category.register(Identifier.fromNamespaceAndPath("flowtiers", "category"))
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
		});
	}

	private static void unbindAdvancementsIfConflicting(Minecraft client, KeyMapping leaderboardKey) {
		if (KeyMappingHelper.getBoundKeyOf(leaderboardKey).getName().equals("key.keyboard.l")
				&& KeyMappingHelper.getBoundKeyOf(client.options.keyAdvancements).getName().equals("key.keyboard.l")) {
			client.options.keyAdvancements.setKey(InputConstants.UNKNOWN);
			client.options.save();
		}
	}
}
