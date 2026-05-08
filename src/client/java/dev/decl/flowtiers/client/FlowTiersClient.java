package dev.decl.flowtiers.client;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;

public class FlowTiersClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		FlowTierClientConfig.load();
		FlowTierCache cache = FlowTiersClientState.cache();
		FlowTierCommands.register(cache);
		FlowTierHud.register(cache);
		FlowTierKeybinds.register();

		ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
			if (message.getString().contains("SR Change")) {
				MinecraftClient client = MinecraftClient.getInstance();
				if (client.player != null) {
					UUID uuid = client.player.getUuid();
					CompletableFuture.delayedExecutor(15, TimeUnit.SECONDS)
							.execute(() -> {
								cache.invalidate(uuid);
								cache.fetch(uuid);
							});
				}
			}
		});
	}
}