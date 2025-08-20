package org.figuramc.figura_client.mixin.item_render;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.figuramc.figura_client.FiguraClient;
import org.figuramc.figura_client.ducks.ItemStackRenderStateAccess;
import org.figuramc.figura_client.game_data.MinecraftItemStackImpl;
import org.figuramc.figura_client.renderer.CompatibleRenderer;
import org.figuramc.figura_core.avatars.Avatar;
import org.figuramc.figura_core.avatars.components.CustomItems;
import org.figuramc.figura_core.manage.AvatarView;
import org.figuramc.figura_core.minecraft_interop.ItemRenderContext;
import org.figuramc.figura_core.minecraft_interop.game_data.item.MinecraftItemStack;
import org.figuramc.figura_core.model.part.CustomItemModelPart;
import org.figuramc.figura_core.model.part.FiguraModelPart;
import org.figuramc.figura_core.model.renderers.Renderable;
import org.figuramc.figura_core.util.FiguraTransformStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Add a field containing the ItemStack itself, so we can access it
@Mixin(ItemStackRenderState.class)
public class ItemStackRenderStateMixin implements ItemStackRenderStateAccess {

    // New field for storing the item stack itself, and getter/setter
    @Unique public ItemStack itemStack;
    @Override public ItemStack figura_client$getItemStack() { return itemStack; }
    @Override public void figura_client$setItemStack(ItemStack itemStack) { this.itemStack = itemStack; }

    // Shadowed
    @Shadow ItemDisplayContext displayContext;

    @Shadow private ItemStackRenderState.LayerRenderState[] layers;
    // Static math classes for not allocating
    @Unique private static final FiguraTransformStack MATRIX_STACK = new FiguraTransformStack();
    @Unique private static final Quaternionf TEMP_QUAT = new Quaternionf(); // Temporary for calculations

    @WrapMethod(method = "render")
    public void renderWrap(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, Operation<Void> original) {
        AvatarView<?> peekedAvatar = FiguraClient.AVATAR_RENDERING_STACK.peek();
        // Push null, so that model parts inside this item are not considered part of the enclosing avatar's vanilla model...
        FiguraClient.AVATAR_RENDERING_STACK.push(null);
        try {
            // Try to override with a figura custom part:
            boolean overrode = tryFiguraOverride(peekedAvatar, itemStack, displayContext, poseStack, multiBufferSource, i, j);
            // If we didn't override, then call the original method
            if (!overrode) original.call(poseStack, multiBufferSource, i, j);
        } finally {
            FiguraClient.AVATAR_RENDERING_STACK.pop();
        }
    }

    @Inject(method = "clear", at = @At("HEAD"))
    public void alsoClearItemStack(CallbackInfo ci) {
        itemStack = null;
    }

    // Try figura override. Return true if successfully overrode with a figura part, false otherwise.
    @Unique
    private boolean tryFiguraOverride(@Nullable AvatarView<?> avatar, ItemStack itemStack, ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, int overlay) {
        // Look for an overriding model part
        CustomItems itemsComponent;
        if (avatar == null || (itemsComponent = avatar.get().getComponent(CustomItems.TYPE)) == null) return false;
        MinecraftItemStack stackImpl = new MinecraftItemStackImpl(itemStack);
        ItemRenderContext renderContext = FiguraClient.RENDER_CONTEXTS.get(itemDisplayContext);
        Renderable<? extends FiguraModelPart> renderablePart = itemsComponent.getModelPart(stackImpl, renderContext);
        if (renderablePart == null) return false;
        FiguraModelPart modelPart = renderablePart.part;

        // If we found one, render it:
        MATRIX_STACK.peekPosition().set(poseStack.last().pose());
        MATRIX_STACK.peekNormal().set(poseStack.last().normal());
        if (modelPart instanceof CustomItemModelPart customModel) {
            // Custom model, apply its own transform if it has one.
            // Otherwise, if no custom transform is specified, fall back to the vanilla one.
            @Nullable Matrix4f customOrFallbackTransform = customModel.itemTransforms.get(renderContext);
            if (customOrFallbackTransform == null && this.layers.length > 0) {
                applyVanillaTransform(this.layers[0].transform, displayContext.leftHand());
            } else if (customOrFallbackTransform != null) {
                MATRIX_STACK.multiply(customOrFallbackTransform);
            }
            MATRIX_STACK.translate(0, -0.5f, 0);
        } else {
            // PNG model. If possible, copy transforms from the first layer.
            if (this.layers.length > 0) {
                applyVanillaTransform(this.layers[0].transform, displayContext.leftHand());
                MATRIX_STACK.translate(-1, -1, -1);
            } else if (itemDisplayContext == ItemDisplayContext.GROUND) {
                MATRIX_STACK.scale(0.5f, 0.5f, 0.5f);
                MATRIX_STACK.translate(-0.5f, -0.25f, -0.5f);
            }
        }
        float tickDelta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
        if (renderablePart.renderer instanceof CompatibleRenderer renderer) {
            renderer.setup(multiBufferSource, MATRIX_STACK, tickDelta, light, overlay);
            avatar.get().tryRenderModelPart(renderer);
        }
        // Successfully overrode it!
        return true;
    }

    /**
     * Copy-pasted from ItemTransform class, changed to use our custom matrix stack to avoid some allocations
     * Removes the "translate by -0.5" feature, however.
     */
    @Unique
    private static void applyVanillaTransform(ItemTransform transform, boolean isLeftHanded) {
        if (transform != ItemTransform.NO_TRANSFORM) {
            float tx = transform.translation().x();
            float ty = transform.translation().y();
            float tz = transform.translation().z();

            float rx = transform.rotation().x() * Mth.DEG_TO_RAD;
            float ry = transform.rotation().y() * Mth.DEG_TO_RAD;
            float rz = transform.rotation().z() * Mth.DEG_TO_RAD;

            float sx = transform.scale().x();
            float sy = transform.scale().y();
            float sz = transform.scale().z();

            if (isLeftHanded) {
                tx = -tx;
                ry = -ry;
                rz = -rz;
            }

            MATRIX_STACK.translate(tx, ty, tz);
            MATRIX_STACK.rotate(TEMP_QUAT.rotationXYZ(rx, ry, rz));
            MATRIX_STACK.scale(sx, sy, sz);
        }
    }


}
