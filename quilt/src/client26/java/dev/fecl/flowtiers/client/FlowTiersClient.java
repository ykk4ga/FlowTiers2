package dev.fecl.flowtiers.client;

import dev.fecl.flowtiers.FlowTiers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class FlowTiersClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		FlowTierClientConfig.load();
		FlowTierCache cache = FlowTiersClientState.cache();
		FlowTierCommands.register(cache);
		FlowTierHud.register(cache);
		FlowTierKeybinds.register();
		FlowTiers.LOGGER.info("FlowTiers 26.x client foundation initialized.");

		ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
			if (message.getString().contains("SR Change")) {
				Minecraft client = Minecraft.getInstance();
				if (client.player != null) {
					UUID uuid = client.player.getUUID();
					CompletableFuture.delayedExecutor(15, TimeUnit.SECONDS)
							.execute(() -> { cache.invalidate(uuid); cache.fetch(uuid); });
					CompletableFuture.delayedExecutor(30, TimeUnit.SECONDS)
							.execute(() -> { cache.invalidate(uuid); cache.fetch(uuid); });
				}
			}
		});
	}
}