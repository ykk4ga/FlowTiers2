package dev.fecl.flowtiers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.fecl.flowtiers.client.config.FlowTiersConfigScreen;
import dev.fecl.flowtiers.client.FlowTiersNeoForgeClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(FlowTiers.MOD_ID)
public final class FlowTiers {
	public static final String MOD_ID = "flowtiers";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public FlowTiers(ModContainer container) {
		LOGGER.info("FlowTiers initialized for NeoForge.");
		if (currentDist() == Dist.CLIENT) {
			container.registerExtensionPoint(IConfigScreenFactory.class,
					(IConfigScreenFactory) (modContainer, parent) -> FlowTiersConfigScreen.create(parent));
			FlowTiersNeoForgeClient.register();
		}
	}

	private static Dist currentDist() {
		try {
			return (Dist) FMLEnvironment.class.getMethod("getDist").invoke(null);
		} catch (ReflectiveOperationException ignored) {
			try {
				return (Dist) FMLEnvironment.class.getField("dist").get(null);
			} catch (ReflectiveOperationException ignoredAgain) {
				return Dist.CLIENT;
			}
		}
	}
}
