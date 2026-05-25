package dev.fecl.flowtiers.client;

import java.util.Locale;
import java.util.UUID;

import com.mojang.brigadier.arguments.StringArgumentType;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

public final class FlowTierCommands {
	private FlowTierCommands() {
	}

	public static void register(FlowTierCache cache) {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
				ClientCommands.literal("flowtiers")
						.executes(context -> showSelf(context.getSource(), cache))
						.then(ClientCommands.argument("player", StringArgumentType.word())
								.executes(context -> showPlayer(context.getSource(), cache, StringArgumentType.getString(context, "player"))))
						.then(ClientCommands.literal("hud")
								.executes(context -> toggle(context.getSource(), "HUD", !FlowTierClientConfig.hudEnabled, value -> FlowTierClientConfig.hudEnabled = value)))
						.then(ClientCommands.literal("nametag")
								.executes(context -> toggle(context.getSource(), "nametags", !FlowTierClientConfig.nametagEnabled, value -> FlowTierClientConfig.nametagEnabled = value)))
						.then(ClientCommands.literal("tab")
								.executes(context -> toggle(context.getSource(), "tab list", !FlowTierClientConfig.tabListEnabled, value -> FlowTierClientConfig.tabListEnabled = value)))
						.then(ClientCommands.literal("ladder")
								.then(ClientCommands.argument("ladder", StringArgumentType.word())
										.executes(context -> setLadder(context.getSource(), StringArgumentType.getString(context, "ladder")))))
						.then(ClientCommands.literal("refresh")
								.executes(context -> {
									FabricClientCommandSource source = context.getSource();
									Minecraft client = Minecraft.getInstance();
									if (client.player == null) {
										source.sendError(Component.literal("You need to be in-game."));
										return 0;
									}
									UUID uuid = client.player.getUUID();
									cache.invalidate(uuid);
									cache.fetch(uuid);
									source.sendFeedback(Component.literal("FlowTiers cache refreshed.").withStyle(ChatFormatting.GREEN));
									return 1;
								}))
		));
	}

	private static int showSelf(FabricClientCommandSource source, FlowTierCache cache) {
		if (source.getPlayer() == null) {
			source.sendError(Component.literal("You need to be in-game to use FlowTiers."));
			return 0;
		}

		return showUuid(source, cache, source.getPlayer().getUUID());
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
				source.sendError(Component.literal("That UUID is invalid."));
				return 0;
			}
		}

		source.sendFeedback(Component.literal("Resolving Minecraft username...").withStyle(ChatFormatting.GRAY));
		FlowTiersClientState.profileResolver().resolve(player).thenAccept(result -> Minecraft.getInstance().execute(() -> {
			if (result.status() == MojangProfileResolver.Status.NOT_FOUND) {
				source.sendError(Component.literal("Minecraft player '" + player + "' does not exist."));
				return;
			}

			if (result.status() == MojangProfileResolver.Status.ERROR) {
				source.sendError(Component.literal("Could not contact Mojang to resolve '" + player + "'. Try again later."));
				return;
			}

			source.sendFeedback(Component.literal("Resolved " + result.profile().name() + ".").withStyle(ChatFormatting.GRAY));
			showUuid(source, cache, result.profile().uuid());
		}));
		return 1;
	}

	private static int showUuid(FabricClientCommandSource source, FlowTierCache cache, UUID uuid) {
		source.sendFeedback(Component.literal("Fetching FlowPvP stats...").withStyle(ChatFormatting.GRAY));
		cache.fetch(uuid).thenAccept(stats -> Minecraft.getInstance().execute(() -> {
			if (stats == null) {
				source.sendFeedback(Component.literal("No FlowPvP ranked stats found.").withStyle(ChatFormatting.YELLOW));
				return;
			}

			source.sendFeedback(FlowTierFormatter.details(stats));
			for (Component line : FlowTierFormatter.ladderDetails(stats)) {
				source.sendFeedback(line);
			}
		}));
		return 1;
	}

	private static int toggle(FabricClientCommandSource source, String label, boolean value, BooleanSetter setter) {
		setter.set(value);
		FlowTierClientConfig.save();
		source.sendFeedback(Component.literal("FlowTiers " + label + " " + (value ? "enabled" : "disabled") + ".")
				.withStyle(value ? ChatFormatting.GREEN : ChatFormatting.RED));
		return 1;
	}

	private static int setLadder(FabricClientCommandSource source, String ladder) {
		FlowTierClientConfig.preferredLadder = FlowTierClientConfig.normalizeLadder(ladder);
		FlowTierClientConfig.save();
		source.sendFeedback(Component.literal("FlowTiers preferred ladder set to " + FlowTierClientConfig.preferredLadder + ".")
				.withStyle(ChatFormatting.GREEN));
		return 1;
	}

	private static UUID resolveOnlineUuid(String name) {
		Minecraft client = Minecraft.getInstance();
		if (client.getConnection() == null) {
			return null;
		}

		String lowerName = name.toLowerCase(Locale.ROOT);
		for (PlayerInfo entry : client.getConnection().getOnlinePlayers()) {
			if (entry.getProfile().name().toLowerCase(Locale.ROOT).equals(lowerName)) {
				return entry.getProfile().id();
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
