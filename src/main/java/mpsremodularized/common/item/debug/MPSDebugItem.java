package mpsremodularized.common.item.debug;

import mpsremodularized.common.capabilities.inventory.modechanging.IModeChangingItem;
import mpsremodularized.common.capabilities.inventory.modularitem.IModularItem;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.registration.MPSItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MPSDebugItem extends Item {
    public MPSDebugItem() {
        super(new Item.Properties().setNoRepair());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        IModularItem iModularItem = MPSCapabilities.getModularItem(helmet);
        if(iModularItem != null) {
            ItemStack armorModule = new ItemStack(MPSItems.IRON_PLATING_MODULE.get());
//            MPSLogger.logDebug("trying to insert");

            if (iModularItem.getStackInSlot(0).isEmpty()) {
//                MPSLogger.logDebug("ranged wrapper size: " + iModularItem.getRangedWrappers().size());
//
//                MPSLogger.logDebug("is armor module valid? : " + iModularItem.isModuleValid(armorModule));

                ItemStack x =  iModularItem.insertItem(0, armorModule, false);

//                MPSLogger.logDebug("result: " + x);
//
//                MPSLogger.logDebug("stack in slot: " + iModularItem.getStackInSlot(0));
            }
        }
        IModeChangingItem mci = MPSCapabilities.getModeChangingModularItem(player.getItemInHand(hand));
        if(mci!= null) {
//            MPSLogger.logDebug("itemhandler size: " + mci.getSlots());
            for (int i = 0; i < mci.getSlots(); i++) {
                if(mci.getStackInSlot(i).isEmpty()) {
                    mci.insertItem(i, new ItemStack(MPSItems.BATTERY_4.get()), false);
                } else {
//                    MPSLogger.logDebug("item already in slot: " + mci.getStackInSlot(i));
                }
            }
        }

        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.has(DataComponents.CONTAINER)) {
//            MPSLogger.logDebug("container stuff: " + heldItem.get(DataComponents.CONTAINER));
        } else {
//            MPSLogger.logDebug("not happening");
        }

        return super.use(level, player, hand);
    }
}
