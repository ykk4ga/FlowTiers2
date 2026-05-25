package dev.fecl.flowtiers.client;

import java.lang.reflect.Field;
import java.util.Arrays;

import com.mojang.blaze3d.platform.InputConstants;
import dev.fecl.flowtiers.client.leaderboard.FlowTierLeaderboardScreen;
import dev.fecl.flowtiers.client.leaderboard.FlowTierPlayerStatsScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class FlowTierKeybinds {
    private FlowTierKeybinds() {
    }

    private static final String[] CYCLE = {
            "MODE:GLOBAL", "MODE:HIGHEST_TIER",
			"SWORD", "AXE", "UHC", "VANILLA", "MACE", "SPEAR_MACE", "CART",
			"DIAMOND_POT", "NETHERITE_OP", "SMP", "DIAMOND_SMP"
    };

    private static KeyMapping.Category category() {
        return KeyMapping.Category.MISC;
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

        KeyMapping viewStats = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.flowtiers.view_stats",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                category()
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            installIntoOptions(client.options, leaderboard, cycleForward, cycleBack, viewStats);
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

            while (viewStats.consumeClick()) {
                if (client.player != null) {
                    client.setScreen(new FlowTierPlayerStatsScreen(
                            null,
                            client.player.getUUID().toString(),
                            client.player.getName().getString()
                    ));
                }
            }
        });
    }

    private static void cycleLadder(Minecraft client, int direction) {
        String current;
        if (FlowTierClientConfig.displayMode == FlowTierClientConfig.DisplayMode.GLOBAL) {
            current = "MODE:GLOBAL";
        } else if (FlowTierClientConfig.displayMode == FlowTierClientConfig.DisplayMode.HIGHEST_TIER) {
            current = "MODE:HIGHEST_TIER";
        } else {
            current = FlowTierClientConfig.preferredLadder;
        }

        int idx = 0;
        for (int i = 0; i < CYCLE.length; i++) {
            if (CYCLE[i].equals(current)) {
                idx = i;
                break;
            }
        }

        String next = CYCLE[((idx + direction) % CYCLE.length + CYCLE.length) % CYCLE.length];

        if (next.equals("MODE:GLOBAL")) {
            FlowTierClientConfig.displayMode = FlowTierClientConfig.DisplayMode.GLOBAL;
        } else if (next.equals("MODE:HIGHEST_TIER")) {
            FlowTierClientConfig.displayMode = FlowTierClientConfig.DisplayMode.HIGHEST_TIER;
        } else {
            FlowTierClientConfig.displayMode = FlowTierClientConfig.DisplayMode.PREFERRED_LADDER;
            FlowTierClientConfig.preferredLadder = next;
        }

        FlowTierClientConfig.save();

        if (client.player != null) {
            String label = next.equals("MODE:GLOBAL") ? "Global"
                    : next.equals("MODE:HIGHEST_TIER") ? "Highest Tier"
                    : FlowTierFormatter.displayName(next);

            Component icon = next.startsWith("MODE:")
                    ? FlowTierFormatter.icon("GLOBAL")
                    : FlowTierFormatter.icon(next);

            Component msg = Component.literal("FlowTiers: " + label + " ")
                    .withStyle(ChatFormatting.GREEN)
                    .copy()
                    .append(icon);

            client.gui.setOverlayMessage(msg, false);
        }
    }

    private static void unbindAdvancementsIfConflicting(Minecraft client, KeyMapping leaderboardKey) {
        if (KeyMappingHelper.getBoundKeyOf(leaderboardKey).getName().equals("key.keyboard.l")
                && KeyMappingHelper.getBoundKeyOf(client.options.keyAdvancements).getName().equals("key.keyboard.l")) {
            client.options.keyAdvancements.setKey(InputConstants.UNKNOWN);
            client.options.save();
        }
    }

    private static boolean installedIntoOptions = false;

    private static void installIntoOptions(net.minecraft.client.Options options, KeyMapping... bindings) {
        if (installedIntoOptions || options == null) return;

        for (Field field : net.minecraft.client.Options.class.getDeclaredFields()) {
            if (field.getType() != KeyMapping[].class) continue;

            try {
                field.setAccessible(true);
                KeyMapping[] existing = (KeyMapping[]) field.get(options);
                if (existing == null || existing.length < 20) continue;
                if (contains(existing, bindings[0])) {
                    installedIntoOptions = true;
                    return;
                }

                KeyMapping[] expanded = Arrays.copyOf(existing, existing.length + bindings.length);
                System.arraycopy(bindings, 0, expanded, existing.length, bindings.length);
                field.set(options, expanded);
                KeyMapping.resetMapping();
                installedIntoOptions = true;
                return;
            } catch (ReflectiveOperationException ignored) {
            }
        }
    }

    private static boolean contains(KeyMapping[] existing, KeyMapping binding) {
        String name = binding.getName();
        for (KeyMapping key : existing) {
            if (key != null && name.equals(key.getName())) return true;
        }
        return false;
    }
}
