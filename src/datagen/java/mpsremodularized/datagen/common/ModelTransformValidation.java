package mpsremodularized.datagen.common;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import mpsremodularized.client.model.block.LuxCapacitorModelWrapper;
import mpsremodularized.client.model.item.PowerFistModelWrapper;
import mpsremodularized.client.model.obj.OBJBakedCompositeModel;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3f;

/** Verifies that decorators preserve the composite model's transforms and renderer identity. */
final class ModelTransformValidation {
    static void validate() {
        ItemTransform transform = new ItemTransform(new Vector3f(10, 20, 30),
                new Vector3f(0.1F, 0.2F, 0.3F), new Vector3f(0.75F, 1.25F, 0.9F));
        ItemTransforms transforms = new ItemTransforms(transform, transform, transform, transform,
                transform, transform, transform, transform, ImmutableMap.of());
        OBJBakedCompositeModel composite = new OBJBakedCompositeModel(true, true, true,
                null, ImmutableMap.of(), transforms, null);
        LuxCapacitorModelWrapper lux = new LuxCapacitorModelWrapper(composite);
        PowerFistModelWrapper fist = new PowerFistModelWrapper(composite);
        int checks = 0;
        for (ItemDisplayContext context : ItemDisplayContext.values()) {
            for (boolean leftHand : new boolean[]{false, true}) {
                PoseStack expectedPose = new PoseStack();
                transforms.getTransform(context).apply(leftHand, expectedPose);
                for (BakedModel model : new BakedModel[]{composite, lux, fist}) {
                    PoseStack actualPose = new PoseStack();
                    BakedModel rendered = model.applyTransform(context, actualPose, leftHand);
                    BakedModel expectedModel = model == fist && context == ItemDisplayContext.GUI ? composite : model;
                    if (rendered != expectedModel) {
                        throw new AssertionError("Lost decorated renderer: " + model.getClass().getSimpleName() + " / " + context);
                    }
                    if (!actualPose.last().pose().equals(expectedPose.last().pose(), 0.000001F)
                            || !actualPose.last().normal().equals(expectedPose.last().normal(), 0.000001F)) {
                        throw new AssertionError("Transform mismatch: " + model.getClass().getSimpleName() + " / " + context + " / leftHand=" + leftHand);
                    }
                    checks += 2;
                }
            }
        }
        System.out.println("Model transform and wrapper checks passed: " + checks);
    }
}
