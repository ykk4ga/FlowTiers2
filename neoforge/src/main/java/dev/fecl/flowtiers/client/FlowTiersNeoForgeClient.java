package dev.fecl.flowtiers.client;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class FlowTiersNeoForgeClient {
	private FlowTiersNeoForgeClient() {
	}

	public static void register() {
		FlowTierClientConfig.load();
		FlowTierCache cache = FlowTiersClientState.cache();

		NeoForge.EVENT_BUS.addListener((RegisterClientCommandsEvent event) -> FlowTierCommands.register(event, cache));
		NeoForge.EVENT_BUS.addListener((RenderGuiEvent.Post event) -> FlowTierHud.render(event, cache));
		FlowTierVersionChecker.register();
		NeoForge.EVENT_BUS.addListener((ClientChatReceivedEvent event) -> {
			if (!event.getMessage().getString().contains("SR Change")) return;
			Minecraft client = Minecraft.getInstance();
			if (client.player == null) return;

			UUID uuid = client.player.getUUID();
			CompletableFuture.delayedExecutor(15, TimeUnit.SECONDS)
					.execute(() -> {
						cache.invalidate(uuid);
						cache.fetch(uuid);
					});
		});
	}
}
