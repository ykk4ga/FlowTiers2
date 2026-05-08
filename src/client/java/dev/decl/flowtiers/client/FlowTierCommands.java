package dev.decl.flowtiers.client;

import java.util.Locale;
import java.util.UUID;

import com.mojang.brigadier.arguments.StringArgumentType;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class FlowTierCommands {
	private FlowTierCommands() {
	}

	public static void register(FlowTierCache cache) {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
				ClientCommandManager.literal("flowtiers")
						.executes(context -> showSelf(context.getSource(), cache))
						.then(ClientCommandManager.argument("player", StringArgumentType.word())
								.executes(context -> showPlayer(context.getSource(), cache, StringArgumentType.getString(context, "player"))))
						.then(ClientCommandManager.literal("hud")
								.executes(context -> toggle(context.getSource(), "HUD", !FlowTierClientConfig.hudEnabled, value -> FlowTierClientConfig.hudEnabled = value)))
						.then(ClientCommandManager.literal("nametag")
								.executes(context -> toggle(context.getSource(), "nametags", !FlowTierClientConfig.nametagEnabled, value -> FlowTierClientConfig.nametagEnabled = value)))
						.then(ClientCommandManager.literal("tab")
								.executes(context -> toggle(context.getSource(), "tab list", !FlowTierClientConfig.tabListEnabled, value -> FlowTierClientConfig.tabListEnabled = value)))
						.then(ClientCommandManager.literal("ladder")
								.then(ClientCommandManager.argument("ladder", StringArgumentType.word())
										.executes(context -> setLadder(context.getSource(), StringArgumentType.getString(context, "ladder")))))
						.then(ClientCommandManager.literal("refresh")
								.executes(context -> {
									FabricClientCommandSource source = context.getSource();
									MinecraftClient client = MinecraftClient.getInstance();
									if (client.player == null) {
										source.sendError(Text.literal("You need to be in-game."));
										return 0;
									}
									UUID uuid = client.player.getUuid();
									cache.invalidate(uuid);
									cache.fetch(uuid);
									source.sendFeedback(Text.literal("FlowTiers cache refreshed.").formatted(Formatting.GREEN));
									return 1;
								}))
		));
	}

	private static int showSelf(FabricClientCommandSource source, FlowTierCache cache) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null) {
			source.sendError(Text.literal("You need to be in-game to use FlowTiers."));
			return 0;
		}

		return showUuid(source, cache, client.player.getUuid());
	}

	private static int showPlayer(FabricClientCommandSource source, FlowTierCache cache, String player) {
		UUID uuid = resolveOnlineUuid(player);
		if (uuid != null) {
			return showUuid(source, cache, uuid);
		}

		if (looksLikeUuid(player)) {
			try {
				return showUuid(source, cache, parseUuid(player));
			} catch (IllegalArgumentException ignored) {
				source.sendError(Text.literal("That UUID is invalid."));
				return 0;
			}
		}

		source.sendFeedback(Text.literal("Resolving Minecraft username...").formatted(Formatting.GRAY));
		FlowTiersClientState.profileResolver().resolve(player).thenAccept(result -> MinecraftClient.getInstance().execute(() -> {
			if (result.status() == MojangProfileResolver.Status.NOT_FOUND) {
				source.sendError(Text.literal("Minecraft player '" + player + "' does not exist."));
				return;
			}

			if (result.status() == MojangProfileResolver.Status.ERROR) {
				source.sendError(Text.literal("Could not contact Mojang to resolve '" + player + "'. Try again later."));
				return;
			}

			source.sendFeedback(Text.literal("Resolved " + result.profile().name() + ".").formatted(Formatting.GRAY));
			showUuid(source, cache, result.profile().uuid());
		}));
		return 1;
	}

	private static int showUuid(FabricClientCommandSource source, FlowTierCache cache, UUID uuid) {
		source.sendFeedback(Text.literal("Fetching FlowPvP stats...").formatted(Formatting.GRAY));
		cache.fetch(uuid).thenAccept(stats -> MinecraftClient.getInstance().execute(() -> {
			if (stats == null) {
				source.sendFeedback(Text.literal("No FlowPvP ranked stats found.").formatted(Formatting.YELLOW));
				return;
			}

			source.sendFeedback(FlowTierFormatter.details(stats));
			for (Text line : FlowTierFormatter.ladderDetails(stats)) {
				source.sendFeedback(line);
			}
		}));
		return 1;
	}

	private static int toggle(FabricClientCommandSource source, String label, boolean value, BooleanSetter setter) {
		setter.set(value);
		FlowTierClientConfig.save();
		source.sendFeedback(Text.literal("FlowTiers " + label + " " + (value ? "enabled" : "disabled") + ".")
				.formatted(value ? Formatting.GREEN : Formatting.RED));
		return 1;
	}

	private static int setLadder(FabricClientCommandSource source, String ladder) {
		FlowTierClientConfig.preferredLadder = FlowTierClientConfig.normalizeLadder(ladder);
		FlowTierClientConfig.save();
		source.sendFeedback(Text.literal("FlowTiers preferred ladder set to " + FlowTierClientConfig.preferredLadder + ".")
				.formatted(Formatting.GREEN));
		return 1;
	}

	private static UUID resolveOnlineUuid(String name) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.getNetworkHandler() == null) {
			return null;
		}

		String lowerName = name.toLowerCase(Locale.ROOT);
		for (PlayerListEntry entry : client.getNetworkHandler().getPlayerList()) {
			if (FlowTierMinecraftCompat.profileName(entry.getProfile()).toLowerCase(Locale.ROOT).equals(lowerName)) {
				return FlowTierMinecraftCompat.profileId(entry.getProfile());
			}
		}

		return null;
	}

	private static boolean looksLikeUuid(String player) {
		return player.indexOf('-') >= 0 || player.length() == 32 || player.length() == 36;
	}

	private static UUID parseUuid(String player) {
		if (player.length() != 32) {
			return UUID.fromString(player);
		}

		return UUID.fromString(player.replaceFirst(
				"([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{12})",
				"$1-$2-$3-$4-$5"
		));
	}

	@FunctionalInterface
	private interface BooleanSetter {
		void set(boolean value);
	}
}
