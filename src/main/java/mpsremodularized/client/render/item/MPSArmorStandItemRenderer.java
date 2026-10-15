package mpsremodularized.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.math.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmorStandModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.Lazy;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class MPSArmorStandItemRenderer extends BlockEntityWithoutLevelRenderer {
    int WHITE = Color.WHITE.getAGBRInt();

    public MPSArmorStandItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), new EntityModelSet());
    }

    @Override
    public void renderByItem(ItemStack itemStack, ItemDisplayContext transformType, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int packedLightIn, int packedOverlayIn) {
        matrixStack.pushPose();
        matrixStack.scale(1.0F, -1.0F, -1.0F);
        VertexConsumer ivertexbuilder1 = ItemRenderer.getFoilBufferDirect(renderTypeBuffer,  MPSArmorStandModel.INSTANCE.renderType(/*ArmorStandRenderer.DEFAULT_SKIN_LOCATION*/ MPSConstants.TEXTURE_ARMOR_STAND), false, itemStack.hasFoil());
        MPSArmorStandModel.INSTANCE.renderToBuffer(matrixStack, ivertexbuilder1, packedLightIn, packedOverlayIn, WHITE);
        matrixStack.popPose();
    }

    private static class MPSArmorStandModel extends Model {
        private static final MPSArmorStandModel INSTANCE = new MPSArmorStandModel(RenderType::entityCutoutNoCull);
        private static final Lazy<ArmorStandModel> MODEL = Lazy.of(()-> new ArmorStandModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.ARMOR_STAND)));// Lazy.of(() -> new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.ARMOR_STAND)));

        private MPSArmorStandModel(Function<ResourceLocation, RenderType> renderTypeFunction) {
            super(renderTypeFunction);
        }

        @Override
        public void renderToBuffer(@Nonnull PoseStack poseStack, @Nonnull VertexConsumer consumer, int light, int overlay, int color) {
            if (MODEL.get() != null) {
                MODEL.get().renderToBuffer(poseStack, consumer, light, overlay, color);
            }
        }
    }
}
