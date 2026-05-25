package dev.fecl.flowtiers.client;

import java.util.Locale;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

public final class FlowTierCommands {
	private FlowTierCommands() {
	}

	public static void register(RegisterClientCommandsEvent event, FlowTierCache cache) {
		event.getDispatcher().register(
				Commands.literal("flowtiers")
						.executes(context -> showSelf(context.getSource(), cache))
						.then(Commands.argument("player", StringArgumentType.word())
								.executes(context -> showPlayer(context.getSource(), cache, StringArgumentType.getString(context, "player"))))
						.then(Commands.literal("hud")
								.executes(context -> toggle(context.getSource(), "HUD", !FlowTierClientConfig.hudEnabled, value -> FlowTierClientConfig.hudEnabled = value)))
						.then(Commands.literal("nametag")
								.executes(context -> toggle(context.getSource(), "nametags", !FlowTierClientConfig.nametagEnabled, value -> FlowTierClientConfig.nametagEnabled = value)))
						.then(Commands.literal("tab")
								.executes(context -> toggle(context.getSource(), "tab list", !FlowTierClientConfig.tabListEnabled, value -> FlowTierClientConfig.tabListEnabled = value)))
						.then(Commands.literal("ladder")
								.then(Commands.argument("ladder", StringArgumentType.word())
										.executes(context -> setLadder(context.getSource(), StringArgumentType.getString(context, "ladder")))))
						.then(Commands.literal("refresh")
								.executes(context -> {
									Minecraft client = Minecraft.getInstance();
									if (client.player == null) {
										context.getSource().sendFailure(Component.literal("You need to be in-game."));
										return 0;
									}
									UUID uuid = client.player.getUUID();
									cache.invalidate(uuid);
									cache.fetch(uuid);
									sendFeedback(context.getSource(), Component.literal("FlowTiers cache refreshed.").withStyle(ChatFormatting.GREEN));
									return 1;
								}))
		);
	}

	private static int showSelf(CommandSourceStack source, FlowTierCache cache) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) {
			source.sendFailure(Component.literal("You need to be in-game to use FlowTiers."));
			return 0;
		}

		return showUuid(source, cache, client.player.getUUID());
	}

	private static int showPlayer(CommandSourceStack source, FlowTierCache cache, String player) {
		UUID uuid = resolveOnlineUuid(player);
		if (uuid != null) {
			return showUuid(source, cache, uuid);
		}

		if (looksLikeUuid(player)) {
			try {
				return showUuid(source, cache, parseUuid(player));
			} catch (IllegalArgumentException ignored) {
				source.sendFailure(Component.literal("That UUID is invalid."));
				return 0;
			}
		}

		sendFeedback(source, Component.literal("Resolving Minecraft username...").withStyle(ChatFormatting.GRAY));
		FlowTiersClientState.profileResolver().resolve(player).thenAccept(result -> Minecraft.getInstance().execute(() -> {
			if (result.status() == MojangProfileResolver.Status.NOT_FOUND) {
				source.sendFailure(Component.literal("Minecraft player '" + player + "' does not exist."));
				return;
			}

			if (result.status() == MojangProfileResolver.Status.ERROR) {
				source.sendFailure(Component.literal("Could not contact Mojang to resolve '" + player + "'. Try again later."));
				return;
			}

			sendFeedback(source, Component.literal("Resolved " + result.profile().name() + ".").withStyle(ChatFormatting.GRAY));
			showUuid(source, cache, result.profile().uuid());
		}));
		return 1;
	}

	private static int showUuid(CommandSourceStack source, FlowTierCache cache, UUID uuid) {
		sendFeedback(source, Component.literal("Fetching FlowPvP stats...").withStyle(ChatFormatting.GRAY));
		cache.fetch(uuid).thenAccept(stats -> Minecraft.getInstance().execute(() -> {
			if (stats == null) {
				sendFeedback(source, Component.literal("No FlowPvP ranked stats found.").withStyle(ChatFormatting.YELLOW));
				return;
			}

			sendFeedback(source, FlowTierFormatter.details(stats));
			for (Component line : FlowTierFormatter.ladderDetails(stats)) {
				sendFeedback(source, line);
			}
		}));
		return 1;
	}

	private static int toggle(CommandSourceStack source, String label, boolean value, BooleanSetter setter) {
		setter.set(value);
		FlowTierClientConfig.save();
		sendFeedback(source, Component.literal("FlowTiers " + label + " " + (value ? "enabled" : "disabled") + ".")
				.withStyle(value ? ChatFormatting.GREEN : ChatFormatting.RED));
		return 1;
	}

	private static int setLadder(CommandSourceStack source, String ladder) {
		FlowTierClientConfig.preferredLadder = FlowTierClientConfig.normalizeLadder(ladder);
		FlowTierClientConfig.save();
		sendFeedback(source, Component.literal("FlowTiers preferred ladder set to " + FlowTierClientConfig.preferredLadder + ".")
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
			GameProfile profile = entry.getProfile();
			String profileName = profileName(profile);
			if (profileName != null && profileName.toLowerCase(Locale.ROOT).equals(lowerName)) {
				return profileId(profile);
			}
		}

		return null;
	}

	private static String profileName(GameProfile profile) {
		try {
			return (String) GameProfile.class.getMethod("name").invoke(profile);
		} catch (ReflectiveOperationException ignored) {
			try {
				return (String) GameProfile.class.getMethod("getName").invoke(profile);
			} catch (ReflectiveOperationException ignoredAgain) {
				return null;
			}
		}
	}

	private static UUID profileId(GameProfile profile) {
		try {
			return (UUID) GameProfile.class.getMethod("id").invoke(profile);
		} catch (ReflectiveOperationException ignored) {
			try {
				return (UUID) GameProfile.class.getMethod("getId").invoke(profile);
			} catch (ReflectiveOperationException ignoredAgain) {
				return null;
			}
		}
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

	private static void sendFeedback(CommandSourceStack source, Component component) {
		source.sendSuccess(() -> component, false);
	}

	@FunctionalInterface
	private interface BooleanSetter {
		void set(boolean value);
	}
}
