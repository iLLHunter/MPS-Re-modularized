package mpsremodularized.common.capabilities.inventory.modechanging;

import mpsremodularized.common.capabilities.inventory.modularitem.ModularItem;
import mpsremodularized.common.capabilities.module.blockbreaking.IBlockBreakingModule;
import mpsremodularized.common.capabilities.module.enhancement.IMiningEnhancementModule;
import mpsremodularized.common.capabilities.module.enchantment.IEnchantmentModule;
import mpsremodularized.common.capabilities.module.externalitems.IOtherModItemsAsModules;
import mpsremodularized.common.capabilities.module.powermodule.IPowerModule;
import mpsremodularized.common.capabilities.module.rightclick.IRightClickModule;
import mpsremodularized.common.network.MPSPackets;
import mpsremodularized.common.network.serverbound.ModeChangeRequestPacketServerBound;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.utils.ElectricItemUtils;
import mpsremodularized.common.utils.ItemUtils;
import mpsremodularized.common.utils.TagUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ModeChangingModularItem extends ModularItem implements IModeChangingItem {
    protected int activeMode = -1;

    public ModeChangingModularItem(@Nonnull ItemStack modularItem, int tier, int size) {
        super(modularItem, tier, size, true);
        activeMode = TagUtils.getModularItemIntOrDefault(modularItem, TAG_MODE, -1);
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    @Override
    public BakedModel getInventoryModel() {
        return Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(getActiveModule());
    }

    @Override
    public List<Integer> getValidModes() {
        List<Integer>moduleIndexes = new ArrayList<>();

        for(int i=0; i < getSlots();  i++) {
            ItemStack module = getStackInSlot(i);
            if (isValidMode(module)) {
                moduleIndexes.add(i);
            }
        }
        return moduleIndexes;
    }

    @Override
    public boolean isValidMode(ResourceLocation mode) {
        return false;
    }

    @Override
    public int getActiveMode() {
        if (activeMode < 0 || activeMode >= getSlots() || !isValidMode(getStackInSlot(activeMode))) {
            List<Integer> validModes = getValidModes();
            activeMode = validModes.isEmpty() ? -1 : validModes.get(0);
        }
        return activeMode;
    }

    @Override
    public ItemStack getActiveModule() {
        int activeModeIndex = getActiveMode();
        if(activeModeIndex >=0 && activeModeIndex < getSlots()) {
            ItemStack module = getStackInSlot(activeModeIndex);
            if (isValidMode(module)) {
                return module;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setActiveMode(int newMode) {
        activeMode = newMode;
        TagUtils.setModularItemInt(getModularItemStack(), TAG_MODE, newMode);
    }

    @Override
    public boolean hasActiveModule(ResourceLocation regName) {
        ItemStack module = getActiveModule();
        return !module.isEmpty() && Objects.equals(regName, ItemUtils.getRegistryName(module))
                && isModuleOperational(getModuleCapability(module));
    }

    @Override
    public void cycleMode(Player player, int dMode) {
        List<Integer> modes = this.getValidModes();
        if (!modes.isEmpty()) {
            int newIndex = clampMode(modes.indexOf(this.getActiveMode()) + dMode, modes.size());
            int newMode = modes.get(newIndex);
            if(player.level().isClientSide()) {
                MPSPackets.sendToServer(new ModeChangeRequestPacketServerBound(newMode));
            }
        }
    }

    @Override
    public int nextMode() {
        List<Integer> modes = this.getValidModes();
        if (!modes.isEmpty()) {
            int newindex = clampMode(modes.indexOf(getActiveMode()) + 1, modes.size());
            return modes.get(newindex);
        }
        else return -1;
    }

    @Override
    public int prevMode() {
        List<Integer> modes = this.getValidModes();
        if (!modes.isEmpty()) {
            int newindex = clampMode(modes.indexOf(getActiveMode()) - 1, modes.size());
            return modes.get(newindex);
        }
        else return -1;
    }

    private static int clampMode(int selection, int modesSize) {
        return (selection > 0) ? (selection % modesSize) : ((selection + modesSize * -selection) % modesSize);
    }

    @Override
    public boolean isModuleActiveAndOnline(ResourceLocation moduleName) {
        int slot = findInstalledModule(moduleName);
        if (slot < 0) return false;
        ItemStack module = getStackInSlot(slot);
        return isModuleOnline(module) && (!isValidMode(module) || hasActiveModule(moduleName));
    }

    @Override
    public boolean isValidMode(@Nonnull ItemStack module) {
        if (module.isEmpty()) {
            return false;
        }

        IPowerModule pm = getModuleCapability(module);
        return (pm instanceof IRightClickModule || pm instanceof IOtherModItemsAsModules) && isModuleValid(pm);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand, InteractionResultHolder<ItemStack> fallback) {
        IPowerModule pm = getModuleCapability(getActiveModule());
        ItemStack fist = player.getItemInHand(hand);
        if(isModuleOperational(pm) && pm instanceof IRightClickModule clickie) {
            return clickie.use(fist, level, player, hand);
        }
        return fallback;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context, InteractionResult fallback) {
        IPowerModule pm = getModuleCapability(getActiveModule());
        if(isModuleOperational(pm) && pm instanceof IRightClickModule clickie) {
            return clickie.onItemUseFirst(itemStack, context);
        }
        return fallback;
    }

    @Override
    public InteractionResult useOn(UseOnContext context, InteractionResult fallback) {
        IPowerModule pm = getModuleCapability(getActiveModule());
        if(isModuleOperational(pm) && pm instanceof IRightClickModule clickie) {
            return clickie.useOn(context);
        } else {
//            MPSLogger.logDebug("not right click item? ");
        }
        return fallback;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft) {
        IPowerModule pm = getModuleCapability(getActiveModule());
//        MPSLogger.logDebug("releaseUsing mci, timeLeft " + timeLeft);


        if (isModuleOperational(pm) && pm instanceof IRightClickModule rightClickModule) {
//            MPSLogger.logDebug("releaseUsing mci, activeModule " + getActiveModule());
            rightClickModule.releaseUsing(stack, level, entityLiving, timeLeft);
        } else {
//            MPSLogger.logDebug(getActiveModule() + " mci not IRightClick?");
        }
    }

    // Fixme: maybe just call releaseUsing with 0 timeLeft?
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
//        MPSLogger.logDebug("finishUsing " + getActiveModule());

        IPowerModule pm = getModuleCapability(getActiveModule());
        if (isModuleOperational(pm) && pm instanceof IRightClickModule rightClickModule) {
            rightClickModule.finishUsingItem(stack, level, livingEntity);
        }
        return getModularItemStack();
    }



    //    @Override
    //    public boolean onUseTick(Level level, LivingEntity entity, int ticksRemaining) {
    //        return MPSCapabilities.getCapability(getActiveModule(), MPSCapabilities.Module.POWER_MODULE)
    //                .filter(IOtherModItemsAsModules.class::isInstance)
    //                .map(IOtherModItemsAsModules.class::cast).map(iOtherModItemsAsModules -> iOtherModItemsAsModules.onUseTick(level, entity, ticksRemaining)).orElse(true);
    //    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockHitResult hitResult, Player player, Level level) {
        IPowerModule pm = getModuleCapability(getActiveModule());
        if(isModuleOperational(pm) && pm instanceof IMiningEnhancementModule me) {
            return me.onBlockStartBreak(itemstack, hitResult,player, level);
        }
        return false;
    }

    @Override
    public boolean mineBlock(ItemStack powerFist, Level worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {
        var nativeTool = mpsremodularized.common.compat.PaxelIntegration.find(powerFist);
        double playerEnergy = ElectricItemUtils.getPlayerEnergy(entityLiving);
        double enchantmentEnergyUsage = 0;
        for (int i = 0; i < getSlots(); i++) {
            IPowerModule pm = getModuleCapability(getStackInSlot(i));
            if (isModuleOperational(pm) && pm instanceof IEnchantmentModule enchantment) {
                enchantmentEnergyUsage += Math.max(0, enchantment.getEnergyUsage());
            }
        }

        double energyAvailableForTool = playerEnergy - enchantmentEnergyUsage;
        if (energyAvailableForTool < 0) {
            return false;
        }

        if (nativeTool != null) {
            try {
                boolean result = nativeTool.source().getItem().mineBlock(nativeTool.source(), worldIn, state, pos, entityLiving);
                ElectricItemUtils.drainPlayerEnergy(entityLiving, enchantmentEnergyUsage, false);
                return result;
            } finally { if (!worldIn.isClientSide()) nativeTool.save(); }
        }

        for(int i = 0;i < getSlots(); i++) {
            IPowerModule pm = getModuleCapability(getStackInSlot(i));
            if (isModuleOperational(pm) && pm instanceof IBlockBreakingModule bb && bb.mineBlock(powerFist, worldIn, state, pos, entityLiving, energyAvailableForTool)) {
                ElectricItemUtils.drainPlayerEnergy(entityLiving, enchantmentEnergyUsage, false);
                return true;
            }
        }
        return false;
    }

    @Override
    public float getDestroySpeed(ItemStack pStack, BlockState state) {
        var nativeTool = mpsremodularized.common.compat.PaxelIntegration.find(pStack);
        if (nativeTool != null) return nativeTool.source().getDestroySpeed(state);
        float highest = 1.0F;
        for(int i = 0;i < getSlots(); i++) {
            IPowerModule pm = getModuleCapability(getStackInSlot(i));
            if (isModuleOperational(pm) && pm instanceof IBlockBreakingModule bb) {
                float speed = bb.getEmulatedTool().getDestroySpeed(state);
                if(speed > highest) {
                    highest = speed;
                }
            }
        }
        return highest;
    }

    @Override
    public boolean canPerformAction(ItemAbility toolAction) {
        var nativeTool = mpsremodularized.common.compat.PaxelIntegration.find(getModularItemStack());
        if (nativeTool != null) return nativeTool.source().canPerformAction(toolAction);
        for (int i = 0; i < getSlots(); i++) {
            ItemStack module = getStackInSlot(i);
            IPowerModule pm = module.getCapability(MPSCapabilities.Module.POWER_MODULE);
            if (isModuleOperational(pm) && pm instanceof IBlockBreakingModule blockBreakingModule) {
                if (blockBreakingModule.canPerformAction(toolAction)){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void setModuleFloat(ResourceLocation moduleName, String key, float value) {
        for (int i = 0; i < getSlots(); i++) {
            ItemStack module = getStackInSlot(i);
            if (!module.isEmpty() && Objects.equals(ItemUtils.getRegistryName(module), moduleName)) {
                IPowerModule pm = getModuleCapability(module);
                if (pm != null) {
                    TagUtils.setModuleFloat(module, key, value);
                    updateModuleInSlot(i, module);
                    break;
                }
            }
        }
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack itemStack, BlockState state) {
        var nativeTool = mpsremodularized.common.compat.PaxelIntegration.find(itemStack);
        if (nativeTool != null) return nativeTool.source().isCorrectToolForDrops(state);
        for (int i = 0; i < getSlots(); i++) {
            ItemStack module = getStackInSlot(i);
            IPowerModule pm = module.getCapability(MPSCapabilities.Module.POWER_MODULE);
            if (isModuleOperational(pm) && pm instanceof IBlockBreakingModule blockBreakingModule) {
                if (blockBreakingModule.getEmulatedTool().isCorrectToolForDrops(state)){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean returnForeignModuleToModularItem(@Nonnull ItemStack module) {
        int slot = findInstalledModule(module);
        IOtherModItemsAsModules foreignModuleCap = module.getCapability(MPSCapabilities.Module.EXTERNAL_MOD_ITEMS_AS_MODULES);
        if (slot > -1 && foreignModuleCap != null) {
            updateModuleInSlot(slot, module);
            return true;
        }
        return false;
    }
}
