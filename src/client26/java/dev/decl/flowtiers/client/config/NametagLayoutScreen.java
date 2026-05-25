package dev.decl.flowtiers.client.config;

import dev.decl.flowtiers.client.FlowTierClientConfig;
import dev.decl.flowtiers.client.FlowTierFormatter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class NametagLayoutScreen extends Screen {
    private final Screen parent;
    private static final int ROW_H = 24;
    private static final int START_Y = 55;

    private final List<FlowTierClientConfig.NametagComponent> order;

    public NametagLayoutScreen(Screen parent) {
        super(Component.literal("Nametag Layout"));
        this.parent = parent;
        this.order = new ArrayList<>(FlowTierClientConfig.nametagOrder);
    }

    @Override
    protected void init() {
        clearWidgets();
        int cx = width / 2;
        int rowW = Math.min(420, width - 40);
        int left = cx - rowW / 2;

        for (int i = 0; i < order.size(); i++) {
            final int idx = i;
            FlowTierClientConfig.NametagComponent comp = order.get(i);
            int y = START_Y + i * ROW_H;

            if (i > 0) {
                addRenderableWidget(Button.builder(Component.literal("↑"), btn -> {
                    swap(idx - 1, idx); init();
                }).bounds(left + 190, y, 20, 18).build());
            }

            if (i < order.size() - 1) {
                addRenderableWidget(Button.builder(Component.literal("↓"), btn -> {
                    swap(idx, idx + 1); init();
                }).bounds(left + 214, y, 20, 18).build());
            }

            addRenderableWidget(Button.builder(
                    Component.literal(isEnabled(comp) ? "ON" : "OFF"),
                    btn -> {
                        toggle(order.get(idx));
                        btn.setMessage(Component.literal(isEnabled(order.get(idx)) ? "ON" : "OFF"));
                    }
            ).bounds(left + 238, y, 50, 18).build());

            if (comp == FlowTierClientConfig.NametagComponent.ELO) {
                addRenderableWidget(Button.builder(
                        Component.literal("Label: " + (FlowTierClientConfig.eloLabelEnabled ? "ON" : "OFF")),
                        btn -> {
                            FlowTierClientConfig.eloLabelEnabled = !FlowTierClientConfig.eloLabelEnabled;
                            btn.setMessage(Component.literal("Label: " + (FlowTierClientConfig.eloLabelEnabled ? "ON" : "OFF")));
                        }
                ).bounds(left + 292, y, 80, 18).build());
            }

            if (comp == FlowTierClientConfig.NametagComponent.POSITION) {
                addRenderableWidget(Button.builder(
                        Component.literal("Label: " + (FlowTierClientConfig.positionLabelEnabled ? "ON" : "OFF")),
                        btn -> {
                            FlowTierClientConfig.positionLabelEnabled = !FlowTierClientConfig.positionLabelEnabled;
                            btn.setMessage(Component.literal("Label: " + (FlowTierClientConfig.positionLabelEnabled ? "ON" : "OFF")));
                        }
                ).bounds(left + 292, y, 80, 18).build());
            }
        }

        addRenderableWidget(Button.builder(Component.literal("Done"), btn -> onClose())
                .bounds(cx - 50, height - 28, 100, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xE0101420);
        super.extractRenderState(context, mouseX, mouseY, delta);

        int cx = width / 2;
        int rowW = Math.min(420, width - 40);
        int left = cx - rowW / 2;

        context.centeredText(font, "Nametag Layout", cx, 10, 0xFF00BFFF);
        context.centeredText(font, "↑↓ reorder  •  toggle ON/OFF", cx, 22, 0xFF888888);

        context.text(font, Component.literal("Component"), left,       START_Y - 14, 0xFFAAAAAA, false);
        context.text(font, Component.literal("Move"),      left + 190, START_Y - 14, 0xFFAAAAAA, false);
        context.text(font, Component.literal("Show"),      left + 244, START_Y - 14, 0xFFAAAAAA, false);
        context.text(font, Component.literal("Label"),     left + 298, START_Y - 14, 0xFFAAAAAA, false);
        context.fill(left - 4, START_Y - 4, left + rowW + 4, START_Y - 3, 0xFF444444);

        for (int i = 0; i < order.size(); i++) {
            FlowTierClientConfig.NametagComponent comp = order.get(i);
            int y = START_Y + i * ROW_H;
            if (i % 2 == 0) {
                context.fill(left - 4, y - 2, left + rowW + 4, y + ROW_H - 4, 0x22FFFFFF);
            }
            context.text(font, Component.literal((i + 1) + ". " + componentName(comp)),
                    left, y + 4, isEnabled(comp) ? 0xFFFFFFFF : 0xFF777777, false);
        }

        int previewY = START_Y + order.size() * ROW_H + 14;
        context.fill(left - 4, previewY - 4, left + rowW + 4, previewY + 14, 0x33FFFFFF);
        context.text(font, Component.literal("Preview:"), left, previewY + 2, 0xFFAAAAAA, false);
        context.text(font, FlowTierFormatter.previewCompact(), left + 65, previewY + 2, 0xFFFFFFFF, false);
    }

    private void swap(int a, int b) {
        FlowTierClientConfig.NametagComponent tmp = order.get(a);
        order.set(a, order.get(b));
        order.set(b, tmp);
        FlowTierClientConfig.nametagOrder = new ArrayList<>(order);
    }

    private static boolean isEnabled(FlowTierClientConfig.NametagComponent comp) {
        return switch (comp) {
            case GAMEMODE_ICON -> FlowTierClientConfig.gamemodeIconEnabled;
            case TIER -> FlowTierClientConfig.tierEnabled;
            case SEPARATOR -> FlowTierClientConfig.separatorEnabled;
            case ELO -> FlowTierClientConfig.eloEnabled;
            case POSITION -> FlowTierClientConfig.positionEnabled;
        };
    }

    private static void toggle(FlowTierClientConfig.NametagComponent comp) {
        switch (comp) {
            case GAMEMODE_ICON -> FlowTierClientConfig.gamemodeIconEnabled = !FlowTierClientConfig.gamemodeIconEnabled;
            case TIER -> FlowTierClientConfig.tierEnabled = !FlowTierClientConfig.tierEnabled;
            case SEPARATOR -> FlowTierClientConfig.separatorEnabled = !FlowTierClientConfig.separatorEnabled;
            case ELO -> FlowTierClientConfig.eloEnabled = !FlowTierClientConfig.eloEnabled;
            case POSITION -> FlowTierClientConfig.positionEnabled = !FlowTierClientConfig.positionEnabled;
        }
    }

    private static String componentName(FlowTierClientConfig.NametagComponent comp) {
        return switch (comp) {
            case GAMEMODE_ICON -> "Gamemode Icon";
            case TIER -> "Tier";
            case SEPARATOR -> "Separator";
            case ELO -> "SR";
            case POSITION -> "Position";
        };
    }

    @Override
    public void onClose() {
        FlowTierClientConfig.nametagOrder = new ArrayList<>(order);
        FlowTierClientConfig.normalizeNametagOrder();
        FlowTierClientConfig.save();
        if (minecraft != null) minecraft.setScreen(parent);
    }
}
