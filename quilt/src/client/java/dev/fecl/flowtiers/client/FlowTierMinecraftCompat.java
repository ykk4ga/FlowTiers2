package dev.fecl.flowtiers.client;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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

		for (Method method : Style.class.getMethods()) {
			if (!method.getReturnType().isAssignableFrom(Style.class)) continue;
			if (method.getParameterCount() != 1) continue;

			Class<?> paramType = method.getParameterTypes()[0];

			if (paramType.isAssignableFrom(fontId.getClass())) {
				try {
					Object result = method.invoke(Style.EMPTY, fontId);
					if (result instanceof Style s) return s;
				} catch (Exception ignored) {}
			}

			try {
				Constructor<?> ctor = paramType.getConstructor(fontId.getClass());
				Object wrapped = ctor.newInstance(fontId);
				Object result = method.invoke(Style.EMPTY, wrapped);
				if (result instanceof Style s) return s;
			} catch (Exception ignored) {}

			for (Class<?> inner : paramType.getDeclaredClasses()) {
				try {
					Constructor<?> ctor = inner.getConstructor(fontId.getClass());
					Object wrapped = ctor.newInstance(fontId);
					Object result = method.invoke(Style.EMPTY, wrapped);
					if (result instanceof Style s) return s;
				} catch (Exception ignored) {}
			}
		}

		return Style.EMPTY;
	}

	private static Object cachedCategory = null;

	private static Object getMiscCategory() {
		if (cachedCategory != null) return cachedCategory;

		for (Class<?> categoryClass : KeyBinding.class.getDeclaredClasses()) {
			try {
				Field misc = categoryClass.getField("MISC");
				if (Modifier.isStatic(misc.getModifiers())) {
					cachedCategory = misc.get(null);
					if (cachedCategory != null) return cachedCategory;
				}
			} catch (ReflectiveOperationException ignored) {}

			try {
				if (categoryClass.isEnum()) {
					for (Object constant : (Object[]) categoryClass.getMethod("values").invoke(null)) {
						String name = constant.toString();
						if (name.equals("MISC") || name.contains("misc") || name.contains("MISC")) {
							cachedCategory = constant;
							return cachedCategory;
						}
					}
				}
			} catch (ReflectiveOperationException ignored) {}

			for (Method create : categoryClass.getDeclaredMethods()) {
				if (Modifier.isStatic(create.getModifiers())
						&& create.getParameterCount() == 1
						&& create.getParameterTypes()[0] == String.class
						&& create.getReturnType() == categoryClass) {
					try {
						create.setAccessible(true);
						cachedCategory = create.invoke(null, "key.categories.misc");
						if (cachedCategory != null) return cachedCategory;
					} catch (ReflectiveOperationException ignored) {}
				}
			}
		}
		return null;
	}

	public static KeyBinding keyBinding(String translationKey, int code, String categoryTranslationKey) {
		Object category = getMiscCategory();

		if (category != null) {
			try {
				Constructor<KeyBinding> ctor = KeyBinding.class.getConstructor(
						String.class, InputUtil.Type.class, int.class, category.getClass());
				return ctor.newInstance(translationKey, InputUtil.Type.KEYSYM, code, category);
			} catch (ReflectiveOperationException ignored) {}

			try {
				Constructor<KeyBinding> ctor = KeyBinding.class.getConstructor(
						String.class, int.class, category.getClass());
				return ctor.newInstance(translationKey, code, category);
			} catch (ReflectiveOperationException ignored) {}

			try {
				Constructor<KeyBinding> ctor = KeyBinding.class.getConstructor(
						String.class, int.class, category.getClass().getSuperclass());
				return ctor.newInstance(translationKey, code, category);
			} catch (ReflectiveOperationException ignored) {}
		}

		try {
			return categorizedKeyBinding(translationKey, code,
					FabricLoader.getInstance().getMappingResolver());
		} catch (ReflectiveOperationException ignored) {}

		for (Constructor<?> ctor : KeyBinding.class.getConstructors()) {
			Class<?>[] params = ctor.getParameterTypes();
			try {
				if (params.length == 3 && params[0] == String.class && params[1] == int.class) {
					return (KeyBinding) ctor.newInstance(translationKey, code, params[2].getField("MISC").get(null));
				}
				if (params.length == 4 && params[0] == String.class && params[2] == int.class) {
					return (KeyBinding) ctor.newInstance(translationKey, InputUtil.Type.KEYSYM, code,
							params[3].getField("MISC").get(null));
				}
			} catch (Exception ignored) {}
		}

		throw new IllegalStateException("Could not create FlowTiers keybinding.");
	}

	private static KeyBinding categorizedKeyBinding(String translationKey, int code, MappingResolver mappings) throws ReflectiveOperationException {
		Object category = getMiscCategory();
		if (category == null) {
			try {
				Class<?> categoryClass = Class.forName(mappings.mapClassName("named", "net.minecraft.client.option.KeyBinding$Category"));
				String registerName = mappings.mapMethodName(
						"named",
						"net.minecraft.client.option.KeyBinding$Category",
						"register",
						"(Ljava/lang/String;)Lnet/minecraft/client/option/KeyBinding$Category;"
				);
				Method register = categoryClass.getMethod(registerName, String.class);
				register.setAccessible(true);
				category = register.invoke(null, "misc");
				cachedCategory = category;
			} catch (ReflectiveOperationException e) {
				throw new ReflectiveOperationException("Could not create keybinding category.", e);
			}
		}

		try {
			Constructor<KeyBinding> ctor = KeyBinding.class.getConstructor(
					String.class, InputUtil.Type.class, int.class, category.getClass());
			return ctor.newInstance(translationKey, InputUtil.Type.KEYSYM, code, category);
		} catch (ReflectiveOperationException ignored) {}

		Constructor<KeyBinding> ctor = KeyBinding.class.getConstructor(
				String.class, int.class, category.getClass());
		return ctor.newInstance(translationKey, code, category);
	}
}