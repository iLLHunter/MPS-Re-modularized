package mpsremodularized.common.capabilities.tool;

import mpsremodularized.common.capabilities.render.modelspec.IHandHeldEntitySpecNBT;
import mpsremodularized.common.capabilities.render.modelspec.ModelSpecStorage;
import mpsremodularized.common.capabilities.render.modelspec.MPSModelSpecRegistry;
import mpsremodularized.common.capabilities.render.modelspec.PartSpecBase;
import mpsremodularized.common.capabilities.render.modelspec.SpecBase;
import mpsremodularized.common.capabilities.render.modelspec.SpecType;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.item.electric.tool.PowerFist;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class PowerFistRenderWrapper extends ModelSpecStorage implements IHandHeldEntitySpecNBT {
    public PowerFistRenderWrapper(@Nonnull ItemStack itemStackIn) {
        super(itemStackIn);
    }

    @Override
    public CompoundTag getDefaultRenderTag() {
        if (getItemStack().isEmpty())
            return new CompoundTag();

        List<CompoundTag> prefArray = new ArrayList<>();

        // ModelPartSpecs
        ListTag specList = new ListTag();

        // TextureSpecBase (only one texture visible at a time)
        CompoundTag texSpecTag = new CompoundTag();

        // List of EnumColor indexes
        List<Integer> colors = new ArrayList<>();

        // temp data holder
        CompoundTag tempNBT;

        EquipmentSlot slot = getItemStack().getEquipmentSlot();

        for (SpecBase spec : MPSModelSpecRegistry.getInstance().getSpecs()) {
            // Only generate NBT data from Specs marked as "default"
            if (spec.isDefault()) {
                if (getItemStack().getItem() instanceof PowerFist && (spec.getSpecType().equals(SpecType.HANDHELD_OBJ_MODEL) || spec.getSpecType().equals(SpecType.HANDHELD_JAVA_MODEL))) {
                    colors = addNewColorstoList(colors, spec.getColors()); // merge new color int arrays in

                    for (PartSpecBase partSpec : spec.getPartSpecs()) {
//                        if (partSpec instanceof ModelPartSpec) {
                        prefArray.add(partSpec.multiSet(new CompoundTag(),
                                getNewColorIndex(colors, spec.getColors(), partSpec.getDefaultColorIndex()),
                                partSpec.getGlow()));
//                        }
                    }
                }
            }
        }

        CompoundTag nbt = new CompoundTag();
        for (CompoundTag elem : prefArray) {
            nbt.put(elem.getString(MPSConstants.MODEL) + "." + elem.getString(MPSConstants.PART), elem);
        }

        if (!specList.isEmpty())
            nbt.put(MPSConstants.SPECLIST, specList);

//        if (!texSpecTag.isEmpty())
//            nbt.put(MPSConstants.TEXTURESPEC, texSpecTag);

        nbt.put(MPSConstants.COLORS, new IntArrayTag(colors));
        return nbt;
    }
}
