package dev.fecl.flowtiers.client.config;

import dev.fecl.flowtiers.client.FlowTierClientConfig;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;

final class HudPlacementEntry extends TooltipListEntry<Void> {
	private final Button button;

	HudPlacementEntry() {
		super(Component.literal("HUD position"), () -> Optional.of(new Component[] {
				Component.literal("Opens a placement screen where you can drag the FlowTiers HUD.")
		}));
		this.button = Button.builder(
				Component.literal("Place HUD"),
				btn -> {
					FlowTierClientConfig.save();
					Screen configScreen = Minecraft.getInstance().screen;
					Minecraft.getInstance().setScreen(new HudPlacementScreen(configScreen));
				}
		).width(150).build();
	}

	@Override
	public Void getValue() { return null; }

	@Override
	public Optional<Void> getDefaultValue() { return Optional.empty(); }

	@Override
	public void save() {}

	@Override
	public boolean isEdited() { return false; }

	@Override
	public List<? extends net.minecraft.client.gui.components.events.GuiEventListener> children() {
		return List.of(button);
	}

	@Override
	public List<? extends NarratableEntry> narratables() {
		return List.of(button);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float delta) {
		int bx = x + entryWidth - 154;
		int by = y + 1;
		int bw = 150;
		int bh = 20;
		button.setX(bx);
		button.setY(by);
		button.setWidth(bw);

		context.text(Minecraft.getInstance().font, Component.literal("HUD position"), x, y + 6, 0xFFFFFFFF, true);

		boolean buttonHovered = mouseX >= bx && mouseX <= bx + bw && mouseY >= by && mouseY <= by + bh;
		context.fill(bx, by, bx + bw, by + bh, buttonHovered ? 0xFF3B82F6 : 0xFF1F2937);
		context.fill(bx, by, bx + bw, by + 1, buttonHovered ? 0xFF93C5FD : 0xFF4B5563);
		context.fill(bx, by + bh - 1, bx + bw, by + bh, buttonHovered ? 0xFF93C5FD : 0xFF4B5563);
		context.fill(bx, by, bx + 1, by + bh, buttonHovered ? 0xFF93C5FD : 0xFF4B5563);
		context.fill(bx + bw - 1, by, bx + bw, by + bh, buttonHovered ? 0xFF93C5FD : 0xFF4B5563);
		context.centeredText(Minecraft.getInstance().font, Component.literal("Place HUD"),
				bx + bw / 2, by + (bh - Minecraft.getInstance().font.lineHeight) / 2, 0xFFFFFFFF);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		int bx = button.getX();
		int by = button.getY();
		int bw = button.getWidth();
		int bh = button.getHeight();
		if (event.button() == 0 && event.x() >= bx && event.x() <= bx + bw && event.y() >= by && event.y() <= by + bh) {
			FlowTierClientConfig.save();
			Minecraft.getInstance().setScreen(new HudPlacementScreen(Minecraft.getInstance().screen));
			return true;
		}
		return super.mouseClicked(event, doubled);
	}
}
