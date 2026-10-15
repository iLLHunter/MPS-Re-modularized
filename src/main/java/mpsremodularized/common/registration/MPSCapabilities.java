package mpsremodularized.common.registration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mpsremodularized.common.capabilities.armor.PowerArmorFluidWrapper;
import mpsremodularized.common.capabilities.armor.PowerArmorHeatWrapper;
import mpsremodularized.common.capabilities.armor.PowerArmorModularItemWrapper;
import mpsremodularized.common.capabilities.armor.PowerArmorRenderWrapper;
import mpsremodularized.common.capabilities.energy.ModularItemEnergyWrapper;
import mpsremodularized.common.capabilities.heat.IHeatStorage;
import mpsremodularized.common.capabilities.inventory.modechanging.IModeChangingItem;
import mpsremodularized.common.capabilities.inventory.modularitem.IModularItem;
import mpsremodularized.common.capabilities.module.externalitems.IOtherModItemsAsModules;
import mpsremodularized.common.capabilities.module.powermodule.IPowerModule;
import mpsremodularized.common.capabilities.player.keystates.IPlayerKeyStates;
import mpsremodularized.common.capabilities.render.color.ColorAttachmentStorage;
import mpsremodularized.common.capabilities.render.color.IColorTag;
import mpsremodularized.common.capabilities.render.modelspec.IModelSpec;
import mpsremodularized.common.capabilities.tool.PowerFistHeatWrapper;
import mpsremodularized.common.capabilities.tool.PowerFistModeChangingWrapper;
import mpsremodularized.common.capabilities.tool.PowerFistRenderWrapper;
import mpsremodularized.common.config.MPSCommonConfig;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.item.module.fluidstorage.CoolantTankModule;
import mpsremodularized.common.registration.MPSBlockEntities;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.ComponentEnergyStorage;
import net.neoforged.neoforge.items.ComponentItemHandler;

public class MPSCapabilities {
    public static final class Inventory {
        public static final ItemCapability<IModularItem, Void> MODULAR_ITEM = ItemCapability.createVoid(create("modular_item"), IModularItem.class);
        public static final ItemCapability<IModeChangingItem, Void> MODE_CHANGING_MODULAR_ITEM = ItemCapability.createVoid(create("mode_changing_modular_item"), IModeChangingItem.class);
    }

    public static final class Module {
        public static final ItemCapability<IPowerModule, Void> POWER_MODULE = ItemCapability.createVoid(create("powermodule"), IPowerModule.class);
        public static final ItemCapability<IOtherModItemsAsModules, Void> EXTERNAL_MOD_ITEMS_AS_MODULES = ItemCapability.createVoid(create("external_mod_items"), IOtherModItemsAsModules.class);
    }

    //    public static final ItemCapability<IHighlight, Void> HIGHLIGHT = ItemCapability.createVoid(create("highlight"), IHighlight.class);
    //    public static final ItemCapability<IChameleon, Void> CHAMELEON = ItemCapability.createVoid(create("chameleon"), IChameleon.class);

    public static final ItemCapability<IModelSpec, Void> RENDER = ItemCapability.createVoid(create("render"), IModelSpec.class);

    public static final EntityCapability<IPlayerKeyStates, Void> PLAYER_KEYSTATES = EntityCapability.createVoid(create("keystates"), IPlayerKeyStates.class);

    public static final ItemCapability<IHeatStorage, Void> HEAT = ItemCapability.createVoid(create("heat"), IHeatStorage.class);

    public static final class ColorCap {
        public static final ItemCapability<IColorTag, Void> COLOR_ITEM = ItemCapability.createVoid(create("color"), IColorTag.class);
        public static final BlockCapability<IColorTag, Void> COLOR_BLOCK = BlockCapability.createVoid(create("color"), IColorTag.class);
        public static final EntityCapability<IColorTag, Void> COLOR_ENTITY = EntityCapability.createVoid(create("color"), IColorTag.class);
    }

    public static IPowerModule getPowerModuleCapability(ItemStack module) {
        return module.getCapability(Module.POWER_MODULE, null);
    }

    @Nullable
    public static IModeChangingItem getModeChangingModularItem(ItemStack modularItem) {
        return modularItem.getCapability(Inventory.MODE_CHANGING_MODULAR_ITEM);
    }

    @Nullable
    public static IModularItem getModularItem(ItemStack modularItem) {
        return modularItem.getCapability(Inventory.MODULAR_ITEM);
    }

    public static boolean modularItemCapIsPresent(@Nonnull ItemStack itemStack) {
        return getModularItem(itemStack) != null;
    }

    public static boolean modularItemCapIsPresent(Player player, EquipmentSlot slot) {
        return modularItemCapIsPresent(player.getItemBySlot(slot));
    }

    public static boolean modeChangingModularItemCapIsPresent(@Nonnull ItemStack modularItem) {
        return getModeChangingModularItem(modularItem) != null;
    }

    @Nullable
    public static IModularItem getModularItemOrModeChangingCapability(ItemStack modularItem) {
        IModularItem cap = getModeChangingModularItem(modularItem);
        if (cap != null) {
            return cap;
        }
        return modularItem.getCapability(Inventory.MODULAR_ITEM);
    }

    // Note: path can only be lower case or underscore
    private static ResourceLocation create(String path) {
        return ResourceLocation.fromNamespaceAndPath(MPSConstants.MOD_ID, path);
    }

    public static HolderLookup.Provider getProvider(@Nonnull Level level) {
        return level.registryAccess();
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Batteries --------------------------------------------------------------------------------------------------
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, ctx) -> new ComponentEnergyStorage(stack, MPSDataComponents.ENERGY, MPSCommonConfig.batteryMaxEnergy1, MPSCommonConfig.batteryMaxTransfer1), MPSItems.BATTERY_1.get());
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, ctx) -> new ComponentEnergyStorage(stack, MPSDataComponents.ENERGY, MPSCommonConfig.batteryMaxEnergy2, MPSCommonConfig.batteryMaxTransfer2), MPSItems.BATTERY_2.get());
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, ctx) -> new ComponentEnergyStorage(stack, MPSDataComponents.ENERGY, MPSCommonConfig.batteryMaxEnergy3, MPSCommonConfig.batteryMaxTransfer3), MPSItems.BATTERY_3.get());
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, ctx) -> new ComponentEnergyStorage(stack, MPSDataComponents.ENERGY, MPSCommonConfig.batteryMaxEnergy4, MPSCommonConfig.batteryMaxTransfer4), MPSItems.BATTERY_4.get());

        ModuleRegistry.INSTANCE.registerCapabilities(event);
        mpsremodularized.common.compat.NativeAirCapability.register(event);

        event.registerItem(Capabilities.ItemHandler.ITEM, (stack, ctx)-> new ComponentItemHandler(stack, DataComponents.CONTAINER, 1), MPSBlocks.CHARGING_BASE_BLOCK.get().asItem());

        // Blocks -----------------------------------------------------------------------------------------------------
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, MPSBlockEntities.CHARGING_BASE_BLOCK_ENTITY.get(), (o, direction) -> o.getItemHandler());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, MPSBlockEntities.CHARGING_BASE_BLOCK_ENTITY.get(), (o, direction) -> o.getEnergyHandler());

        // Entities ---------------------------------------------------------------------------------------------------
        event.registerEntity(MPSCapabilities.PLAYER_KEYSTATES, EntityType.PLAYER, (player, context)-> player.getData(MPSDataComponents.KEYSTATE_HANDLER));
        // Block Entity ===============================================================================================
        event.registerBlockEntity(MPSCapabilities.ColorCap.COLOR_BLOCK, MPSBlockEntities.LUX_CAP_BLOCK_ENTITY_TYPE.get(), (o, direction)-> new ColorAttachmentStorage(o));

        //         event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, MPSBlockEntities.CHARGING_BASE_BLOCK_ENTITY.get(), (o, direction) -> o.getItemHandler());
        //        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, MPSBlockEntities.CHARGING_BASE_BLOCK_ENTITY.get(), (o, direction) -> o.getEnergyHandler());

        //        event.registerEntity(MPSCapabilities.PLAYER_KEYSTATES, EntityType.PLAYER, (player, context)-> player.getData(MPSDataComponents.KEYSTATE_HANDLER));

        // Todo?

        // Armor ======================================================================================================
        event.registerItem(MPSCapabilities.Inventory.MODULAR_ITEM, (stack, ctx)-> new PowerArmorModularItemWrapper(stack, 1),
            MPSItems.POWER_ARMOR_HELMET_1.get(),
            MPSItems.POWER_ARMOR_CHESTPLATE_1.get(),
            MPSItems.POWER_ARMOR_LEGGINGS_1.get(),
            MPSItems.POWER_ARMOR_BOOTS_1.get());

        event.registerItem(MPSCapabilities.Inventory.MODULAR_ITEM, (stack, ctx)-> new PowerArmorModularItemWrapper(stack, 2),
            MPSItems.POWER_ARMOR_HELMET_2.get(),
            MPSItems.POWER_ARMOR_CHESTPLATE_2.get(),
            MPSItems.POWER_ARMOR_LEGGINGS_2.get(),
            MPSItems.POWER_ARMOR_BOOTS_2.get());

        event.registerItem(MPSCapabilities.Inventory.MODULAR_ITEM, (stack, ctx)-> new PowerArmorModularItemWrapper(stack, 3),
            MPSItems.POWER_ARMOR_HELMET_3.get(),
            MPSItems.POWER_ARMOR_CHESTPLATE_3.get(),
            MPSItems.POWER_ARMOR_LEGGINGS_3.get(),
            MPSItems.POWER_ARMOR_BOOTS_3.get());

        event.registerItem(MPSCapabilities.Inventory.MODULAR_ITEM, (stack, ctx)-> new PowerArmorModularItemWrapper(stack, 4),
            MPSItems.POWER_ARMOR_HELMET_4.get(),
            MPSItems.POWER_ARMOR_CHESTPLATE_4.get(),
            MPSItems.POWER_ARMOR_LEGGINGS_4.get(),
            MPSItems.POWER_ARMOR_BOOTS_4.get());

        // Energy
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, ctx) -> new ModularItemEnergyWrapper(stack),
            MPSItems.POWER_ARMOR_HELMET_1.get(),
            MPSItems.POWER_ARMOR_CHESTPLATE_1.get(),
            MPSItems.POWER_ARMOR_LEGGINGS_1.get(),
            MPSItems.POWER_ARMOR_BOOTS_1.get(),

            MPSItems.POWER_ARMOR_HELMET_2.get(),
            MPSItems.POWER_ARMOR_CHESTPLATE_2.get(),
            MPSItems.POWER_ARMOR_LEGGINGS_2.get(),
            MPSItems.POWER_ARMOR_BOOTS_2.get(),

            MPSItems.POWER_ARMOR_HELMET_3.get(),
            MPSItems.POWER_ARMOR_CHESTPLATE_3.get(),
            MPSItems.POWER_ARMOR_LEGGINGS_3.get(),
            MPSItems.POWER_ARMOR_BOOTS_3.get(),

            MPSItems.POWER_ARMOR_HELMET_4.get(),
            MPSItems.POWER_ARMOR_CHESTPLATE_4.get(),
            MPSItems.POWER_ARMOR_LEGGINGS_4.get(),
            MPSItems.POWER_ARMOR_BOOTS_4.get());

        // Heat
        event.registerItem(MPSCapabilities.HEAT, (stack, context)-> new PowerArmorHeatWrapper(stack, 1),
            MPSItems.POWER_ARMOR_HELMET_1.get(),
            MPSItems.POWER_ARMOR_CHESTPLATE_1.get(),
            MPSItems.POWER_ARMOR_LEGGINGS_1.get(),
            MPSItems.POWER_ARMOR_BOOTS_1.get());

        event.registerItem(MPSCapabilities.HEAT, (stack, context)-> new PowerArmorHeatWrapper(stack, 2),
            MPSItems.POWER_ARMOR_HELMET_2.get(),
            MPSItems.POWER_ARMOR_CHESTPLATE_2.get(),
            MPSItems.POWER_ARMOR_LEGGINGS_2.get(),
            MPSItems.POWER_ARMOR_BOOTS_2.get());

        event.registerItem(MPSCapabilities.HEAT, (stack, context)-> new PowerArmorHeatWrapper(stack, 3),
            MPSItems.POWER_ARMOR_HELMET_3.get(),
            MPSItems.POWER_ARMOR_CHESTPLATE_3.get(),
            MPSItems.POWER_ARMOR_LEGGINGS_3.get(),
            MPSItems.POWER_ARMOR_BOOTS_3.get());

        event.registerItem(MPSCapabilities.HEAT, (stack, context)-> new PowerArmorHeatWrapper(stack, 4),
            MPSItems.POWER_ARMOR_HELMET_4.get(),
            MPSItems.POWER_ARMOR_CHESTPLATE_4.get(),
            MPSItems.POWER_ARMOR_LEGGINGS_4.get(),
            MPSItems.POWER_ARMOR_BOOTS_4.get());

        // Fluid
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, context) -> new PowerArmorFluidWrapper(stack),
            MPSItems.POWER_ARMOR_CHESTPLATE_1.get(),
            MPSItems.POWER_ARMOR_CHESTPLATE_2.get(),
            MPSItems.POWER_ARMOR_CHESTPLATE_3.get(),
            MPSItems.POWER_ARMOR_CHESTPLATE_4.get());

        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, context) ->new CoolantTankModule.FluidHandlerItemStackMPS(stack, 1), MPSItems.COOLANT_TANK_MODULE_1.get());
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, context) ->new CoolantTankModule.FluidHandlerItemStackMPS(stack, 2), MPSItems.COOLANT_TANK_MODULE_2.get());
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, context) ->new CoolantTankModule.FluidHandlerItemStackMPS(stack, 3), MPSItems.COOLANT_TANK_MODULE_3.get());
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, context) ->new CoolantTankModule.FluidHandlerItemStackMPS(stack, 4), MPSItems.COOLANT_TANK_MODULE_4.get());

        // Render
        event.registerItem(MPSCapabilities.RENDER, (stack, ctx) -> new PowerArmorRenderWrapper(stack),
            MPSItems.POWER_ARMOR_HELMET_1.get(),
            MPSItems.POWER_ARMOR_CHESTPLATE_1.get(),
            MPSItems.POWER_ARMOR_LEGGINGS_1.get(),
            MPSItems.POWER_ARMOR_BOOTS_1.get(),

            MPSItems.POWER_ARMOR_HELMET_2.get(),
            MPSItems.POWER_ARMOR_CHESTPLATE_2.get(),
            MPSItems.POWER_ARMOR_LEGGINGS_2.get(),
            MPSItems.POWER_ARMOR_BOOTS_2.get(),

            MPSItems.POWER_ARMOR_HELMET_3.get(),
            MPSItems.POWER_ARMOR_CHESTPLATE_3.get(),
            MPSItems.POWER_ARMOR_LEGGINGS_3.get(),
            MPSItems.POWER_ARMOR_BOOTS_3.get(),

            MPSItems.POWER_ARMOR_HELMET_4.get(),
            MPSItems.POWER_ARMOR_CHESTPLATE_4.get(),
            MPSItems.POWER_ARMOR_LEGGINGS_4.get(),
            MPSItems.POWER_ARMOR_BOOTS_4.get());

        // Power Fist =================================================================================================
        // Mode Changing
        // SERVER config loads after registration; provider values are read at query time.
        event.registerItem(MPSCapabilities.Inventory.MODE_CHANGING_MODULAR_ITEM, (stack, context) -> new PowerFistModeChangingWrapper(stack, 1, MPSCommonConfig.getPowerFistInventorySlots(1)), MPSItems.POWER_FIST_1.get());
        event.registerItem(MPSCapabilities.Inventory.MODE_CHANGING_MODULAR_ITEM, (stack, context) -> new PowerFistModeChangingWrapper(stack, 2, MPSCommonConfig.getPowerFistInventorySlots(2)), MPSItems.POWER_FIST_2.get());
        event.registerItem(MPSCapabilities.Inventory.MODE_CHANGING_MODULAR_ITEM, (stack, context) -> new PowerFistModeChangingWrapper(stack, 3, MPSCommonConfig.getPowerFistInventorySlots(3)), MPSItems.POWER_FIST_3.get());
        event.registerItem(MPSCapabilities.Inventory.MODE_CHANGING_MODULAR_ITEM, (stack, context) -> new PowerFistModeChangingWrapper(stack, 4, MPSCommonConfig.getPowerFistInventorySlots(4)), MPSItems.POWER_FIST_4.get());

        // Energy
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) -> new ModularItemEnergyWrapper(stack),
            MPSItems.POWER_FIST_1.get(),
            MPSItems.POWER_FIST_2.get(),
            MPSItems.POWER_FIST_3.get(),
            MPSItems.POWER_FIST_4.get());

        // Heat
        event.registerItem(MPSCapabilities.HEAT, (stack, context) ->
            new PowerFistHeatWrapper(stack, 1), MPSItems.POWER_FIST_1.get());

        event.registerItem(MPSCapabilities.HEAT, (stack, context) ->
            new PowerFistHeatWrapper(stack, 2), MPSItems.POWER_FIST_2.get());

        event.registerItem(MPSCapabilities.HEAT, (stack, context) ->
            new PowerFistHeatWrapper(stack, 3), MPSItems.POWER_FIST_3.get());

        event.registerItem(MPSCapabilities.HEAT, (stack, context) ->
            new PowerFistHeatWrapper(stack, 4), MPSItems.POWER_FIST_4.get());

        // Render
        event.registerItem(MPSCapabilities.RENDER, (stack, context) -> new PowerFistRenderWrapper(stack),
            MPSItems.POWER_FIST_1.get(),
            MPSItems.POWER_FIST_2.get(),
            MPSItems.POWER_FIST_3.get(),
            MPSItems.POWER_FIST_4.get());
    }
}
