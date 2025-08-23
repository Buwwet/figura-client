package org.figuramc.figura_client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemDisplayContext;
import org.figuramc.figura_client.game_data.GameDataProviderImpl;
import org.figuramc.figura_client.renderer.CompatibleRenderer;
import org.figuramc.figura_client.textures.TextureProviderImpl;
import org.figuramc.figura_core.manage.AvatarView;
import org.figuramc.figura_core.minecraft_interop.FiguraConnectionPoint;
import org.figuramc.figura_core.minecraft_interop.ItemRenderContext;
import org.figuramc.figura_core.minecraft_interop.game_data.entity.EntityKind;
import org.figuramc.figura_core.util.data_structures.NullEmptyStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FiguraClient implements ClientModInitializer {

	// Stack of views of currently-rendering avatars.
	// Do not worry about close()-ing the view when you peek this stack;
	// the person who pushed the view is responsible for closing it.
	public static final NullEmptyStack<AvatarView<?>> AVATAR_RENDERING_STACK = new NullEmptyStack<>();
	public static final NullEmptyStack<Boolean> IS_LIVING_ENTITY_STACK = new NullEmptyStack<>(); // TODO look for a maybe better way to do this?

	// Maps for important objects
	public static final Map<EntityType<?>, EntityKind> ENTITY_KINDS = new ConcurrentHashMap<>();
	public static final EnumMap<ItemDisplayContext, ItemRenderContext> RENDER_CONTEXTS = new EnumMap<>(ItemDisplayContext.class);
	static {
		RENDER_CONTEXTS.put(ItemDisplayContext.NONE, new ItemRenderContext("none", false, false, null));
		RENDER_CONTEXTS.put(ItemDisplayContext.THIRD_PERSON_LEFT_HAND, new ItemRenderContext("thirdperson_lefthand", true, false, RENDER_CONTEXTS.get(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)));
		RENDER_CONTEXTS.put(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, new ItemRenderContext("thirdperson_righthand", false, false, null));
		RENDER_CONTEXTS.put(ItemDisplayContext.FIRST_PERSON_LEFT_HAND, new ItemRenderContext("firstperson_lefthand", true, false, RENDER_CONTEXTS.get(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)));
		RENDER_CONTEXTS.put(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, new ItemRenderContext("firstperson_righthand", false, false, null));
		RENDER_CONTEXTS.put(ItemDisplayContext.HEAD, new ItemRenderContext("head", false, false, null));
		RENDER_CONTEXTS.put(ItemDisplayContext.GUI, new ItemRenderContext("gui", false, true, null));
		RENDER_CONTEXTS.put(ItemDisplayContext.GROUND, new ItemRenderContext("ground", false, true, null));
		RENDER_CONTEXTS.put(ItemDisplayContext.FIXED, new ItemRenderContext("fixed", false, true, null));
	}

	// Test boolean
	public static boolean LOADED_TEST_AVATARS = false;

	// Mod ID
	public static final String MOD_ID = "figura";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		FiguraConnectionPoint.MODEL_PART_RENDERER_FACTORY = CompatibleRenderer::new;
		FiguraConnectionPoint.TEXTURE_PROVIDER = new TextureProviderImpl();
		FiguraConnectionPoint.GAME_DATA_PROVIDER = new GameDataProviderImpl();
		FiguraConnectionPoint.ERROR_REPORTER = new ErrorReporterImpl();
		FiguraConnectionPoint.PATH_PROVIDER = new PathProviderImpl();

		FiguraConnectionPoint.finishInit();
	}
}