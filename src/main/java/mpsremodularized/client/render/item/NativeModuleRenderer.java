package mpsremodularized.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import mpsremodularized.common.compat.NativeModuleItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Displays the actual donor, including its own model overrides and custom renderer. */
public final class NativeModuleRenderer extends BlockEntityWithoutLevelRenderer {
    public NativeModuleRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }
    @Override public void renderByItem(ItemStack module, ItemDisplayContext display, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (!(module.getItem() instanceof NativeModuleItem item)) return;
        var source = item.source(module);
        if (source.isEmpty() && net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(item.donor)) {
            // Recipe-book/JEI previews have no crafted state yet. Rendering a default donor is visual only.
            source = new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(item.donor));
        }
        if (source.isEmpty()) return;
        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        Minecraft.getInstance().getItemRenderer().renderStatic(source, display, light, overlay, pose, buffers, Minecraft.getInstance().level, 0);
        pose.popPose();
    }
}
