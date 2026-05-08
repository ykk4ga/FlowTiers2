package dev.decl.flowtiers.client;

import dev.decl.flowtiers.FlowTiers;
import net.fabricmc.api.ClientModInitializer;

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
