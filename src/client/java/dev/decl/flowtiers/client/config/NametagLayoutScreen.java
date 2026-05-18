package dev.decl.flowtiers.client.config;

import dev.decl.flowtiers.client.FlowTierClientConfig;
import dev.decl.flowtiers.client.FlowTierFormatter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class NametagLayoutScreen extends Screen {
    private final Screen parent;
    private static final int ROW_H = 24;
    private static final int START_Y = 55;

    private final List<FlowTierClientConfig.NametagComponent> order;

    public NametagLayoutScreen(Screen parent) {
        super(Text.literal("Nametag Layout"));
        this.parent = parent;
        this.order = new ArrayList<>(FlowTierClientConfig.nametagOrder);
    }

    @Override
    protected void init() {
        clearChildren();
        int cx = width / 2;
        int rowW = Math.min(420, width - 40);
        int left = cx - rowW / 2;

        for (int i = 0; i < order.size(); i++) {
            final int idx = i;
            FlowTierClientConfig.NametagComponent comp = order.get(i);
            int y = START_Y + i * ROW_H;

            if (i > 0) {
                addDrawableChild(ButtonWidget.builder(Text.literal("↑"), btn -> {
                    swap(idx - 1, idx); init();
                }).dimensions(left + 190, y, 20, 18).build());
            }

            if (i < order.size() - 1) {
                addDrawableChild(ButtonWidget.builder(Text.literal("↓"), btn -> {
                    swap(idx, idx + 1); init();
                }).dimensions(left + 214, y, 20, 18).build());
            }

            addDrawableChild(ButtonWidget.builder(
                    Text.literal(isEnabled(comp) ? "ON" : "OFF"),
                    btn -> {
                        toggle(order.get(idx));
                        btn.setMessage(Text.literal(isEnabled(order.get(idx)) ? "ON" : "OFF"));
                    }
            ).dimensions(left + 238, y, 50, 18).build());

            if (comp == FlowTierClientConfig.NametagComponent.ELO) {
                addDrawableChild(ButtonWidget.builder(
                        Text.literal("Label: " + (FlowTierClientConfig.eloLabelEnabled ? "ON" : "OFF")),
                        btn -> {
                            FlowTierClientConfig.eloLabelEnabled = !FlowTierClientConfig.eloLabelEnabled;
                            btn.setMessage(Text.literal("Label: " + (FlowTierClientConfig.eloLabelEnabled ? "ON" : "OFF")));
                        }
                ).dimensions(left + 292, y, 80, 18).build());
            }

            if (comp == FlowTierClientConfig.NametagComponent.POSITION) {
                addDrawableChild(ButtonWidget.builder(
                        Text.literal("Label: " + (FlowTierClientConfig.positionLabelEnabled ? "ON" : "OFF")),
                        btn -> {
                            FlowTierClientConfig.positionLabelEnabled = !FlowTierClientConfig.positionLabelEnabled;
                            btn.setMessage(Text.literal("Label: " + (FlowTierClientConfig.positionLabelEnabled ? "ON" : "OFF")));
                        }
                ).dimensions(left + 292, y, 80, 18).build());
            }
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), btn -> close())
                .dimensions(cx - 50, height - 28, 100, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xE0101420);
        super.render(context, mouseX, mouseY, delta);

        int cx = width / 2;
        int rowW = Math.min(420, width - 40);
        int left = cx - rowW / 2;

        context.drawCenteredTextWithShadow(textRenderer, "Nametag Layout", cx, 10, 0xFF00BFFF);
        context.drawCenteredTextWithShadow(textRenderer, "↑↓ reorder  •  toggle ON/OFF", cx, 22, 0xFF888888);

        context.drawTextWithShadow(textRenderer, "Component", left,       START_Y - 14, 0xFFAAAAAA);
        context.drawTextWithShadow(textRenderer, "Move",      left + 190, START_Y - 14, 0xFFAAAAAA);
        context.drawTextWithShadow(textRenderer, "Show",      left + 244, START_Y - 14, 0xFFAAAAAA);
        context.drawTextWithShadow(textRenderer, "Label",     left + 298, START_Y - 14, 0xFFAAAAAA);
        context.fill(left - 4, START_Y - 4, left + rowW + 4, START_Y - 3, 0xFF444444);

        for (int i = 0; i < order.size(); i++) {
            FlowTierClientConfig.NametagComponent comp = order.get(i);
            int y = START_Y + i * ROW_H;
            if (i % 2 == 0) {
                context.fill(left - 4, y - 2, left + rowW + 4, y + ROW_H - 4, 0x22FFFFFF);
            }
            context.drawTextWithShadow(textRenderer,
                    (i + 1) + ". " + componentName(comp),
                    left, y + 4,
                    isEnabled(comp) ? 0xFFFFFFFF : 0xFF777777);
        }

        int previewY = START_Y + order.size() * ROW_H + 14;
        context.fill(left - 4, previewY - 4, left + rowW + 4, previewY + 14, 0x33FFFFFF);
        context.drawTextWithShadow(textRenderer, "Preview:", left, previewY + 2, 0xFFAAAAAA);
        context.drawTextWithShadow(textRenderer, FlowTierFormatter.previewCompact(), left + 65, previewY + 2, 0xFFFFFFFF);
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
            case ELO -> FlowTierClientConfig.eloEnabled;
            case POSITION -> FlowTierClientConfig.positionEnabled;
        };
    }

    private static void toggle(FlowTierClientConfig.NametagComponent comp) {
        switch (comp) {
            case GAMEMODE_ICON -> FlowTierClientConfig.gamemodeIconEnabled = !FlowTierClientConfig.gamemodeIconEnabled;
            case TIER -> FlowTierClientConfig.tierEnabled = !FlowTierClientConfig.tierEnabled;
            case ELO -> FlowTierClientConfig.eloEnabled = !FlowTierClientConfig.eloEnabled;
            case POSITION -> FlowTierClientConfig.positionEnabled = !FlowTierClientConfig.positionEnabled;
        }
    }

    private static String componentName(FlowTierClientConfig.NametagComponent comp) {
        return switch (comp) {
            case GAMEMODE_ICON -> "Gamemode Icon";
            case TIER -> "Tier";
            case ELO -> "SR";
            case POSITION -> "Position";
        };
    }

    @Override
    public void close() {
        FlowTierClientConfig.nametagOrder = new ArrayList<>(order);
        FlowTierClientConfig.save();
        if (client != null) client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() { return false; }
}