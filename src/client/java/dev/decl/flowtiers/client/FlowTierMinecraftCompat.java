package dev.decl.flowtiers.client;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;

public final class FlowTierMinecraftCompat {
	private FlowTierMinecraftCompat() {
	}

	public static UUID profileId(GameProfile profile) {
		try {
			return (UUID) GameProfile.class.getMethod("id").invoke(profile);
		} catch (ReflectiveOperationException ignored) {
			try {
				return (UUID) GameProfile.class.getMethod("getId").invoke(profile);
			} catch (ReflectiveOperationException exception) {
				throw new IllegalStateException("Could not read Minecraft profile UUID.", exception);
			}
		}
	}

	public static String profileName(GameProfile profile) {
		try {
			return (String) GameProfile.class.getMethod("name").invoke(profile);
		} catch (ReflectiveOperationException ignored) {
			try {
				return (String) GameProfile.class.getMethod("getName").invoke(profile);
			} catch (ReflectiveOperationException exception) {
				throw new IllegalStateException("Could not read Minecraft profile name.", exception);
			}
		}
	}

	public static Style fontStyle(Identifier fontId) {
		MappingResolver mappings = FabricLoader.getInstance().getMappingResolver();
		try {
			String methodName = mappings.mapMethodName(
					"named",
					"net.minecraft.text.Style",
					"withFont",
					"(Lnet/minecraft/util/Identifier;)Lnet/minecraft/text/Style;"
			);
			Method withFont = Style.class.getMethod(methodName, Identifier.class);
			return (Style) withFont.invoke(Style.EMPTY, fontId);
		} catch (ReflectiveOperationException ignored) {
			try {
				Class<?> sourceClass = Class.forName(mappings.mapClassName("named", "net.minecraft.text.StyleSpriteSource"));
				Class<?> fontClass = Class.forName(mappings.mapClassName("named", "net.minecraft.text.StyleSpriteSource$Font"));
				Object font = fontClass.getConstructor(Identifier.class).newInstance(fontId);
				String methodName = mappings.mapMethodName(
						"named",
						"net.minecraft.text.Style",
						"withFont",
						"(Lnet/minecraft/text/StyleSpriteSource;)Lnet/minecraft/text/Style;"
				);
				Method withFont = Style.class.getMethod(methodName, sourceClass);
				return (Style) withFont.invoke(Style.EMPTY, font);
			} catch (ReflectiveOperationException exception) {
				return Style.EMPTY;
			}
		}
	}

	public static KeyBinding keyBinding(String translationKey, int code, String categoryTranslationKey) {
		try {
			Constructor<KeyBinding> constructor = KeyBinding.class.getConstructor(String.class, InputUtil.Type.class, int.class, String.class);
			return constructor.newInstance(translationKey, InputUtil.Type.KEYSYM, code, categoryTranslationKey);
		} catch (ReflectiveOperationException ignored) {
			try {
				Class<?> categoryClass = Class.forName("net.minecraft.client.option.KeyBinding$Category");
				Method create = categoryClass.getMethod("create", Identifier.class);
				Object category = create.invoke(null, Identifier.of("flowtiers", "category"));
				Constructor<KeyBinding> constructor = KeyBinding.class.getConstructor(String.class, InputUtil.Type.class, int.class, categoryClass);
				return constructor.newInstance(translationKey, InputUtil.Type.KEYSYM, code, category);
			} catch (ReflectiveOperationException exception) {
				throw new IllegalStateException("Could not create FlowTiers keybinding.", exception);
			}
		}
	}
}
