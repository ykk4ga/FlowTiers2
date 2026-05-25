package dev.fecl.flowtiers.client.config;

import dev.fecl.flowtiers.client.FlowTierClientConfig;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;

final class HudPlacementEntry extends TooltipListEntry<Void> {
	private final ButtonWidget button;

	HudPlacementEntry() {
		super(Text.literal("HUD position"), null);
		this.button = ButtonWidget.builder(
				Text.literal("Place HUD"),
				btn -> {
					FlowTierClientConfig.save();
					Screen configScreen = MinecraftClient.getInstance().currentScreen;
					MinecraftClient.getInstance().setScreen(new HudPlacementScreen(configScreen));
				}
		).dimensions(0, 0, 150, 20).build();
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
	public List<? extends Element> children() {
		return List.of(button);
	}

	@Override
	public List<? extends Selectable> narratables() {
		return List.of(button);
	}

	@Override
	public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float delta) {
		button.setWidth(150);
		button.setX(x + entryWidth - 150);
		button.setY(y + 1);
		context.drawTextWithShadow(
				MinecraftClient.getInstance().textRenderer,
				Text.literal("HUD position"),
				x, y + 6, 0xFFFFFFFF
		);
		button.render(context, mouseX, mouseY, delta);
	}
}