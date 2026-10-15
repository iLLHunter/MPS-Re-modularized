package mpsremodularized.common.item.electric.tool;

import mpsremodularized.common.capabilities.inventory.modechanging.IModeChangingItem;
import mpsremodularized.common.capabilities.module.powermodule.IPowerModule;
import mpsremodularized.common.capabilities.module.rightclick.IRightClickModule;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.utils.ElectricItemUtils;
import mpsremodularized.common.utils.ItemUtils;
import mpsremodularized.common.constants.MPSConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.SimpleTier;

public class PowerFist extends AbstractElectricTool {
    @Override public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity owner, int slot, boolean selected) {
        super.inventoryTick(stack, level, owner, slot, selected);
        mpsremodularized.common.compat.MekanismToolIntegration.tick(stack, level, owner);
        mpsremodularized.common.compat.PaxelIntegration.tick(stack, level, owner, slot, selected);
        mpsremodularized.common.compat.PortableBrewingIntegration.tick(stack, level, owner);
        mpsremodularized.common.compat.NativeToolIntegration.tick(stack, level, owner, slot);
        mpsremodularized.common.compat.ConstructionStickIntegration.tick(stack, level, owner, slot);
        mpsremodularized.common.compat.PoweredDiamondAnvilIntegration.tick(stack, level, owner);
        mpsremodularized.common.compat.GascraftIntegration.tick(stack, level, owner);
    }
    public PowerFist() {
        super(new SimpleTier(BlockTags.BUTTONS, 0,0,0, 0, () -> Ingredient.of(Items.AIR)), BlockTags.BUTTONS, new Properties().setNoRepair().stacksTo(1));
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    /**
     * Note ItemStack returned in the InteractionResultHolder becomes the held item
     * @param level
     * @param player
     * @param hand
     * @return
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
//        MPSLogger.logDebug("use()... ");
        ItemStack fist = ItemUtils.getItemFromEntityHand(player, hand);
        var selectedHost = MPSCapabilities.getModeChangingModularItem(fist);
        if (selectedHost != null) {
            var selected = selectedHost.getModuleCapability(selectedHost.getActiveModule());
            if (selectedHost.isModuleOperational(selected) && (selected.getCategory() == mpsremodularized.common.capabilities.module.powermodule.ModuleCategory.WEAPON
                    || selected.getCategory() == mpsremodularized.common.capabilities.module.powermodule.ModuleCategory.CONSTRUCTION_STICK)) {
                return new InteractionResultHolder<>(selectedHost.use(level, player, hand, InteractionResultHolder.pass(fist)).getResult(), fist);
            }
        }
        var nativeTool = mpsremodularized.common.compat.PaxelIntegration.find(fist);
        if (nativeTool != null) {
            var result = mpsremodularized.common.compat.PaxelIntegration.use(nativeTool, level, player, hand);
            if (result != InteractionResult.PASS) return new InteractionResultHolder<>(result, fist);
        }
        final InteractionResultHolder<ItemStack> fallback = new InteractionResultHolder<>(InteractionResult.PASS, fist);
//        if (hand != InteractionHand.MAIN_HAND) {
//            return fallback;
//        }
        IModeChangingItem mci = MPSCapabilities.getModeChangingModularItem(fist);
        if(mci!= null) {
            return new InteractionResultHolder<>(mci.use(level, player, hand, fallback).getResult(), fist);
        }
        return fallback;
    }

    /**
     * Called server side when a Block is destroyed using this Item. Return true to trigger the "Use Item" statistic.
     *
     * @param powerFist
     * @param worldIn
     * @param state
     * @param pos
     * @param entityLiving
     * @return
     */
    @Override
    public boolean mineBlock(ItemStack powerFist, Level worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {
        IModeChangingItem mci = MPSCapabilities.getModeChangingModularItem(powerFist);
        if(mci != null) {
            return mci.mineBlock(powerFist, worldIn, state, pos, entityLiving);
        }
        return false;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        var nativeTool = mpsremodularized.common.compat.NativeToolIntegration.find(player, InteractionHand.MAIN_HAND, null);
        if (nativeTool != null && nativeTool.fist() == stack
                && nativeTool.item().kind == mpsremodularized.common.compat.NativeModuleItem.Kind.TOOL) {
            return nativeTool.source().doesSneakBypassUse(level, pos, player);
        }
        return true;
    }

//    @Override
//    public void onUseTick(Level level, LivingEntity entity, ItemStack itemStack, int ticksRemaining) {
//        if(MPSCapabilities.getModeChangingModularItemCapability(itemStack)
//                .map(handler-> handler.onUseTick(level, entity, ticksRemaining)).orElse(false)) {
//            MPSLogger.logDebug("onUseTick remaining: " + ticksRemaining);
//            super.onUseTick(level, entity, itemStack, ticksRemaining);
//        }
//    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        final InteractionResult fallback = InteractionResult.PASS;

        final InteractionHand hand = context.getHand();
        if (hand != InteractionHand.MAIN_HAND) {
            return fallback;
        }
        IModeChangingItem mci = MPSCapabilities.getModeChangingModularItem(itemStack);
        if(mci != null) {
            if (mpsremodularized.common.compat.PaxelIntegration.find(itemStack) != null
                    && mci.getModuleCapability(mci.getActiveModule()) instanceof mpsremodularized.common.capabilities.module.blockbreaking.IBlockBreakingModule) return fallback;
            return mci.onItemUseFirst(itemStack, context, fallback);
        }
        return fallback;
    }


    // sets a base block breaking speed based on the module's emulated tool
    // FIXME:
    @Override
    public float getDestroySpeed(ItemStack itemStack, BlockState state) {
        IModeChangingItem mci = MPSCapabilities.getModeChangingModularItem(itemStack);
        if(mci != null) {
            return mci.getDestroySpeed(itemStack, state);
        }
        return 1F;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity entity, InteractionHand hand) {
        var selectedHost = MPSCapabilities.getModeChangingModularItem(itemStack);
        if (selectedHost != null) {
            var selected = selectedHost.getModuleCapability(selectedHost.getActiveModule());
            if (selectedHost.isModuleOperational(selected) && selected.getCategory() == mpsremodularized.common.capabilities.module.powermodule.ModuleCategory.WEAPON
                    && selected instanceof IRightClickModule rightClick) return rightClick.interactLivingEntity(itemStack, player, entity, hand).getResult();
        }
        var nativeTool = mpsremodularized.common.compat.PaxelIntegration.find(itemStack);
        if (nativeTool != null && hand == InteractionHand.MAIN_HAND) {
            InteractionResult result;
            try { result = nativeTool.source().interactLivingEntity(player, entity, hand); }
            finally { if (!player.level().isClientSide()) nativeTool.save(); }
            if (result != InteractionResult.PASS) return result;
        }
        IModeChangingItem mci = MPSCapabilities.getModeChangingModularItem(itemStack);
        if(mci != null) {
            ItemStack module = mci.getActiveModule();
            IPowerModule pm = mci.getModuleCapability(module);
            if(mci.isModuleOperational(pm) && pm instanceof IRightClickModule) {
                return ((IRightClickModule) pm).interactLivingEntity(itemStack, player, entity, hand).getResult();
            }
        }
        return super.interactLivingEntity(itemStack, player, entity, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
//        MPSLogger.logDebug("useOn");

        final InteractionResult fallback = InteractionResult.PASS;

        final InteractionHand hand = context.getHand();
        if (hand != InteractionHand.MAIN_HAND) {
            return fallback;
        }
        var selectedHost = MPSCapabilities.getModeChangingModularItem(context.getItemInHand());
        if (selectedHost != null) {
            var selected = selectedHost.getModuleCapability(selectedHost.getActiveModule());
            if (selectedHost.isModuleOperational(selected) && (selected instanceof mpsremodularized.common.item.module.tool.misc.NativeToolModule
                    || selected.getCategory() == mpsremodularized.common.capabilities.module.powermodule.ModuleCategory.WEAPON
                    || selected.getCategory() == mpsremodularized.common.capabilities.module.powermodule.ModuleCategory.CONSTRUCTION_STICK))
                return selectedHost.useOn(context, fallback);
        }
        var nativeTool = mpsremodularized.common.compat.PaxelIntegration.find(context.getItemInHand());
        if (nativeTool != null) {
            var result = mpsremodularized.common.compat.PaxelIntegration.useOn(nativeTool, context);
            if (result != InteractionResult.PASS) return result;
        }

        IModeChangingItem mci = MPSCapabilities.getModeChangingModularItem(context.getItemInHand());
        if(mci != null) {
            if (nativeTool != null && mci.getModuleCapability(mci.getActiveModule()) instanceof mpsremodularized.common.capabilities.module.blockbreaking.IBlockBreakingModule) return fallback;
            return mci.useOn(context, fallback);
        }
        return fallback;
    }

    /**
     * Needed for overriding behaviour with modules
     * Note: also called by mods like TheOneProbe to display whether the required tool is equipped
     * @param itemStack
     * @param state
     * @return
     */
    @Override
    public boolean isCorrectToolForDrops(ItemStack itemStack, BlockState state) {
        IModeChangingItem mci = MPSCapabilities.getModeChangingModularItem(itemStack);
        return mci != null && mci.isCorrectToolForDrops(itemStack, state);
    }

    /**
     * Current implementations of this method in child classes do not use the
     * entry argument beside stack. They just raise the damage on the stack.
     */
    @Override
    public boolean hurtEnemy(ItemStack itemStack, LivingEntity target, LivingEntity attacker) {
        var nativeTool = mpsremodularized.common.compat.PaxelIntegration.find(itemStack);
        if (nativeTool != null && !attacker.level().isClientSide()) {
            try { nativeTool.source().getItem().hurtEnemy(nativeTool.source(), target, attacker); }
            finally { nativeTool.save(); }
        }
        if (!attacker.level().isClientSide && attacker instanceof Player) {
            IModeChangingItem mci = MPSCapabilities.getModeChangingModularItem(itemStack);
            if(mci != null) {
                int index = mci.findInstalledModule(MPSConstants.MELEE_ASSIST_MODULE);
                if(index > -1) {
                    IPowerModule pm = mci.getModuleCapability(mci.getStackInSlot(index));
                    if(mci.isModuleOperational(pm)) {
                        Player player = (Player) attacker;
                        double drain = pm.applyPropertyModifiers(MPSConstants.ENERGY_CONSUMPTION);
                        if (ElectricItemUtils.getPlayerEnergy(player) >= drain) {
                            ElectricItemUtils.drainPlayerEnergy(player, (int) drain, false);
                            double damage = pm.applyPropertyModifiers(MPSConstants.PUNCH_DAMAGE);
                            double knockback = pm.applyPropertyModifiers(MPSConstants.PUNCH_KNOCKBACK);
                            DamageSource damageSource = attacker.damageSources().playerAttack(player);
                            if (target.hurt(damageSource, (float) (int) damage)) {
                                Vec3 lookVec = player.getLookAngle();
                                double resistance = 1 - Math.clamp(target.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE), 0, 1);
                                target.push(lookVec.x * knockback * resistance, Math.abs(lookVec.y + 0.2f) * knockback * resistance, lookVec.z * knockback * resistance);
                                target.hurtMarked = true;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

//    /**
//     * Called before a block is broken.  Return true to prevent default block harvesting.
//     *
//     * Note: In SMP, this is called on both client and server sides!
//     *
//     * @param itemstack The current ItemStack
//     * @param pos Block's position in world
//     * @param player The Player that is wielding the item
//     * @return True to prevent harvesting, false to continue as normal
//     */
//    @Override
//    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {
//        return MPSCapabilities.getCapability(itemstack, MPSCapabilities.Inventory.MODE_CHANGING_MODULAR_ITEM)
//                .map(iItemHandler -> iItemHandler.onBlockStartBreak(itemstack, pos, player)).orElse(false);
//    }

//    @Override
//    public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
//        MPSLogger.logDebug("can attack Block :" +super.canAttackBlock(pState, pLevel, pPos, pPlayer) );
//
//        return super.canAttackBlock(pState, pLevel, pPos, pPlayer);
//    }



    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility toolAction) {
        var configurator = mpsremodularized.common.compat.MekanismToolIntegration.configurator(stack);
        if (!configurator.isEmpty() && configurator.canPerformAction(toolAction)) return true;
        IModeChangingItem mci = MPSCapabilities.getModeChangingModularItem(stack);
        return mci != null && mci.canPerformAction(toolAction);
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return false;
    }

    @Override
    public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
        IModeChangingItem mci = MPSCapabilities.getModeChangingModularItem(oldStack);
        if(mci != null) {
            return mci.canContinueUsing(newStack);
        }
        return false;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BOW;
    }
}
