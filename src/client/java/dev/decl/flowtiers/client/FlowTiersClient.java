package dev.decl.flowtiers.client;

import net.fabricmc.api.ClientModInitializer;

public class FlowTiersClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		FlowTierClientConfig.load();
		FlowTierCache cache = FlowTiersClientState.cache();
		FlowTierCommands.register(cache);
		FlowTierHud.register(cache);
		FlowTierKeybinds.register();
	}
}
