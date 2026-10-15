package mpsremodularized.common.capabilities.render.modelspec;

import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.utils.TagUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public interface IModelSpec {
    @Nonnull
    ItemStack getItemStack();

    @Nonnull
    default NonNullList<SpecBase> getSpecList() {
        CompoundTag renderTag = getRenderTag();
        if (renderTag.isEmpty()) {
            renderTag = getDefaultRenderTag();
        }
        NonNullList<SpecBase> specs = NonNullList.create();
        for (String key : renderTag.getAllKeys()) {
            if (key.equals(MPSConstants.COLORS)) {
                continue;
            }
            if (renderTag.get(key) instanceof CompoundTag) {
                SpecBase testSpec = MPSModelSpecRegistry.getInstance().getModel(renderTag.getCompound(key));
                if (isSpecValid(testSpec)) {
                    specs.add(testSpec);
                }
            }
        }
        return specs;
    }

    default boolean isSpecValid(SpecBase spec) {
        return true;
    }

    default CompoundTag getRenderTag() {
        return TagUtils.getRenderTag(getItemStack());
    }

    ItemStack setRenderTag(CompoundTag renderDataIn, String tagName);

    /**
     * Primarrily used for getting a default tag for rendering without setting anything
     * @return
     */
    default CompoundTag getRenderTagOrDefault() {
        CompoundTag renderTag = getRenderTag();
        if (renderTag.isEmpty()) {
            renderTag = getDefaultRenderTag();
        }
        return renderTag;
    }

    CompoundTag getDefaultRenderTag();

    List<Integer> addNewColorstoList(List<Integer> colors, List<Integer> colorsToAdd);

    int[] getColorArray();

    default int[] getColorArrayOrDefault() {
        return getRenderTagOrDefault().getIntArray(MPSConstants.COLORS);
    }

    int getNewColorIndex(List<Integer> colors, List<Integer> oldColors, Integer index);

    ItemStack setColorArray(int[] colors);
}
