package mpsremodularized.common.registration;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mpsremodularized.common.capabilities.module.PortableCraftingCapability;
import mpsremodularized.common.capabilities.module.TinkerTableCap;
import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.powermodule.PowerModule;
import mpsremodularized.common.capabilities.render.hud.HudModule;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.container.slot.SlotBackgrounds;
import mpsremodularized.common.item.Battery;
import mpsremodularized.common.item.ComponentItem;
import mpsremodularized.common.item.MPSArmorStandItem;
import mpsremodularized.common.item.block.TinkerTableItem;
import mpsremodularized.common.item.electric.armor.AbstractElectricArmor;
import mpsremodularized.common.item.electric.tool.PowerFist;
import mpsremodularized.common.item.module.armor.ArmorPlatingModule;
import mpsremodularized.common.item.module.armor.EnergyShieldModule;
import mpsremodularized.common.item.module.cosmetic.TransparentArmorModule;
import mpsremodularized.common.item.module.energygeneration.heat.ThermalGeneratorModule;
import mpsremodularized.common.item.module.energygeneration.solar.SolarGeneratorModule;
import mpsremodularized.common.item.module.environmental.ActiveCamouflageModule;
import mpsremodularized.common.item.module.environmental.AutoFeederModule;
import mpsremodularized.common.item.module.environmental.MagnetModule;
import mpsremodularized.common.item.module.environmental.MobRepulsorModule;
import mpsremodularized.common.item.module.environmental.PiglinPacificationModule;
import mpsremodularized.common.item.module.environmental.WaterElectrolyzerModule;
import mpsremodularized.common.item.module.fluidstorage.CoolantTankModule;
import mpsremodularized.common.item.module.miningenchantment.AquaAffinityModule;
import mpsremodularized.common.item.module.miningenchantment.FortuneModule;
import mpsremodularized.common.item.module.miningenchantment.SilkTouchModule;
import mpsremodularized.common.item.module.miningenhancement.SelectiveMiner;
import mpsremodularized.common.item.module.miningenhancement.TunnelBoreModule;
import mpsremodularized.common.item.module.miningenhancement.VeinMinerModule;
import mpsremodularized.common.item.module.movement.BlinkDriveModule;
import mpsremodularized.common.item.module.movement.ClimbAssistModule;
import mpsremodularized.common.item.module.movement.DimensionalRiftModule;
import mpsremodularized.common.item.module.movement.FlightControlModule;
import mpsremodularized.common.item.module.movement.GliderModule;
import mpsremodularized.common.item.module.movement.JetBootsModule;
import mpsremodularized.common.item.module.movement.JetPackModule;
import mpsremodularized.common.item.module.movement.JumpAssistModule;
import mpsremodularized.common.item.module.movement.ShockAbsorberModule;
import mpsremodularized.common.item.module.movement.SprintAssistModule;
import mpsremodularized.common.item.module.movement.SwimAssistModule;
import mpsremodularized.common.item.module.tool.blockbreaking.AxeModule;
import mpsremodularized.common.item.module.tool.blockbreaking.HoeModule;
import mpsremodularized.common.item.module.tool.blockbreaking.PickaxeModule;
import mpsremodularized.common.item.module.tool.blockbreaking.ShearsModule;
import mpsremodularized.common.item.module.tool.blockbreaking.ShovelModule;
import mpsremodularized.common.item.module.tool.misc.FlintAndSteelModule;
import mpsremodularized.common.item.module.tool.misc.LeafBlowerModule;
import mpsremodularized.common.item.module.tool.misc.LuxCapacitorModule;
import mpsremodularized.common.item.module.tool.misc.UniversalWrenchModule;
import mpsremodularized.common.item.module.vision.BinocularsModule;
import mpsremodularized.common.item.module.vision.NightVisionModule;
import mpsremodularized.common.item.module.weapon.BladeLauncherModule;
import mpsremodularized.common.item.module.weapon.LightningModule;
import mpsremodularized.common.item.module.weapon.MeleeAssistModule;
import mpsremodularized.common.item.module.weapon.PlasmaCannonModule;
import mpsremodularized.common.item.module.weapon.RailgunModule;
import mpsremodularized.common.registration.ModuleRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = MPSConstants.MOD_ID)
public class MPSItems {
    public static final DeferredRegister.Items MPS_ITEMS = DeferredRegister.createItems(MPSConstants.MOD_ID);

    static { mpsremodularized.common.compat.IntermodModules.register(MPS_ITEMS); }

    // Block Items --------------------------------------------------------------------------------
    // Charging base
    public static final DeferredHolder<Item, BlockItem> CHARGING_BASE_ITEM = MPS_ITEMS.register(MPSConstants.CHARGING_BASE_REGNAME,
        () -> new BlockItem(MPSBlocks.CHARGING_BASE_BLOCK.get(), new Item.Properties().stacksTo(1)
        .component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
        .component(MPSDataComponents.ENERGY, 0)));

    // Armor Stand --------------------------------------------------------------------------------
    public static final DeferredHolder<Item, MPSArmorStandItem> ARMOR_STAND_ITEM = MPS_ITEMS.register(MPSConstants.ARMORSTAND_REGNAME,
        () -> new MPSArmorStandItem(new Item.Properties()));

    //.setISTER(() -> MPSArmorStandItemRenderer::new)));

    // Modules ------------------------------------------------------------------------------------
    // Energy Storage
    public static final DeferredHolder<Item, Battery> BATTERY_1 = registerBattery(MPSConstants.MODULE_BATTERY_1__REGNAME, 1);

    public static final DeferredHolder<Item, Battery> BATTERY_2 = registerBattery(MPSConstants.MODULE_BATTERY_2__REGNAME, 2);

    public static final DeferredHolder<Item, Battery> BATTERY_3 = registerBattery(MPSConstants.MODULE_BATTERY_3__REGNAME, 3);

    public static final DeferredHolder<Item, Battery> BATTERY_4 = registerBattery(MPSConstants.MODULE_BATTERY_4__REGNAME, 4);

    // Components ---------------------------------------------------------------------------------
    public static final DeferredHolder<Item, ComponentItem> ARTIFICIAL_MUSCLE = registerComponent(MPSConstants.COMPONENT__ARTIFICIAL_MUSCLE__REGNAME);

    public static final DeferredHolder<Item, ComponentItem> CARBON_MYOFIBER = registerComponent(MPSConstants.COMPONENT__CARBON_MYOFIBER__REGNAME);

    public static final DeferredHolder<Item, ComponentItem> CAPACITOR_1 = registerComponent(MPSConstants.COMPONENT__CAPACAITOR_1__REGNAME);

    public static final DeferredHolder<Item, ComponentItem> CAPACITOR_2 = registerComponent(MPSConstants.COMPONENT__CAPACAITOR_2__REGNAME);

    public static final DeferredHolder<Item, ComponentItem> CAPACITOR_3 = registerComponent(MPSConstants.COMPONENT__CAPACAITOR_3__REGNAME);

    public static final DeferredHolder<Item, ComponentItem> CAPACITOR_4 = registerComponent(MPSConstants.COMPONENT__CAPACAITOR_4__REGNAME);

    public static final DeferredHolder<Item, ComponentItem> COMPUTER_CHIP = registerComponent(MPSConstants.COMPONENT__COMPUTER_CHIP__REGNAME);

    public static final DeferredHolder<Item, ComponentItem> CONTROL_CIRCUIT_1 = registerComponent(MPSConstants.COMPONENT__CONTROL_CIRCUIT_1__REGNAME);

    public static final DeferredHolder<Item, ComponentItem> CONTROL_CIRCUIT_2 = registerComponent(MPSConstants.COMPONENT__CONTROL_CIRCUIT_2__REGNAME);

    public static final DeferredHolder<Item, ComponentItem> CONTROL_CIRCUIT_3 = registerComponent(MPSConstants.COMPONENT__CONTROL_CIRCUIT_3__REGNAME);

    public static final DeferredHolder<Item, ComponentItem> CONTROL_CIRCUIT_4 = registerComponent(MPSConstants.COMPONENT__CONTROL_CIRCUIT_4__REGNAME);

    public static final DeferredHolder<Item, ComponentItem> FORCE_FIELD_EMITTER = registerComponent(MPSConstants.COMPONENT__FORCE_FIELD_EMITTER__REGNAME);

    public static final DeferredHolder<Item, ComponentItem> GLIDER_WING = registerComponent(MPSConstants.COMPONENT__GLIDER_WING__REGNAME);

    public static final DeferredHolder<Item, ComponentItem> ION_THRUSTER = registerComponent(MPSConstants.COMPONENT__ION_THRUSTER__REGNAME);

    public static final DeferredHolder<Item, ComponentItem> HOLOGRAM_EMITTER = registerComponent(MPSConstants.COMPONENT__HOLOGRAM_EMITTER__REGNAME);

    public static final DeferredHolder<Item, ComponentItem> MAGNET = registerComponent(MPSConstants.COMPONENT__MAGNET__REGNAME);

    public static final DeferredHolder<Item, ComponentItem> MYOFIBER_GEL = registerComponent(MPSConstants.COMPONENT__MYOFIBER_GEL__REGNAME);


    public static final DeferredHolder<Item, ComponentItem> PLATING_IRON = registerComponent(MPSConstants.COMPONENT__PLATING_IRON__REGNAME);

    public static final DeferredHolder<Item, ComponentItem> PLATING_DIAMOND = registerComponent(MPSConstants.COMPONENT__PLATING_DIAMOND__REGNAME);

    public static final DeferredHolder<Item, ComponentItem> PLATING_NETHERITE = registerComponent(MPSConstants.COMPONENT__PLATING_NETHERITE__REGNAME);

    public static final DeferredHolder<Item, ComponentItem> RUBBER_HOSE = registerComponent(MPSConstants.COMPONENT__RUBBER_HOSE__REGNAME);

    public static final DeferredHolder<Item, ComponentItem> SERVO = registerComponent(MPSConstants.COMPONENT__SERVO__REGNAME);
    public static final DeferredHolder<Item, ComponentItem> WRENCH_MODULE_FRAME = registerComponent("wrench_module_frame");
    public static final DeferredHolder<Item, ComponentItem> SHOCK_COIL = registerComponent("shock_coil");
    public static final DeferredHolder<Item, mpsremodularized.common.item.module.environmental.BlockReachModule> BLOCK_REACH_MODULE = registerModule(
            ResourceLocation.fromNamespaceAndPath(MPSConstants.MOD_ID, "block_reach"),
            mpsremodularized.common.item.module.environmental.BlockReachModule::new,
            mpsremodularized.common.item.module.environmental.BlockReachModule.Capability.class,
            mpsremodularized.common.item.module.environmental.BlockReachModule.Capability::new, 4);
    public static final DeferredHolder<Item, mpsremodularized.common.item.module.environmental.ShockModule> SHOCK_MODULE = registerModule(
            ResourceLocation.fromNamespaceAndPath(MPSConstants.MOD_ID, "shock_module"),
            mpsremodularized.common.item.module.environmental.ShockModule::new,
            mpsremodularized.common.item.module.environmental.ShockModule.Capability.class,
            mpsremodularized.common.item.module.environmental.ShockModule.Capability::new, 4);

    public static final DeferredHolder<Item, ComponentItem> SOLAR_PANEL = registerComponent(MPSConstants.COMPONENT__SOLAR_PANEL__REGNAME);

    public static final DeferredHolder<Item, ComponentItem> SOLENOID = registerComponent(MPSConstants.COMPONENT__SOLENOID__REGNAME);

    public static final DeferredHolder<Item, ComponentItem> WIRING_STANDARD = registerComponent(MPSConstants.COMPONENT__WIRING_STANDARD__REGNAME);

    public static final DeferredHolder<Item, ComponentItem> WIRING_HIGH_CONDUCTIVITY = registerComponent(MPSConstants.COMPONENT__WIRING_HIGH_CONDUCTIVITY__REGNAME);

    // Machine preparations have no installable module or equipment capabilities.
    public static final DeferredHolder<Item, ComponentItem> COMPONENT_MOLD = registerComponent("component_mold");
    public static final DeferredHolder<Item, ComponentItem> WIRING_TEMPLATE = registerComponent("component_wiring_template");
    public static final DeferredHolder<Item, ComponentItem> HIGH_CONDUCTIVITY_WIRING_TEMPLATE = registerComponent("component_high_conductivity_wiring_template");
    public static final DeferredHolder<Item, ComponentItem> SOLENOID_TEMPLATE = registerComponent("component_solenoid_template");
    public static final DeferredHolder<Item, ComponentItem> UNFINISHED_HOLOGRAM_EMITTER = registerComponent("unfinished_hologram_emitter");
    public static final DeferredHolder<Item, ComponentItem> UNFINISHED_PLASMA_CANNON = registerComponent("unfinished_plasma_cannon");
    public static final DeferredHolder<Item, ComponentItem> UNFINISHED_RAILGUN = registerComponent("unfinished_railgun");
    public static final DeferredHolder<Item, ComponentItem> PRIMITIVE_HELMET_FRAME = registerComponent("primitive_helmet_frame");
    public static final DeferredHolder<Item, ComponentItem> PRIMITIVE_CHESTPLATE_FRAME = registerComponent("primitive_chestplate_frame");
    public static final DeferredHolder<Item, ComponentItem> PRIMITIVE_LEGGINGS_FRAME = registerComponent("primitive_leggings_frame");
    public static final DeferredHolder<Item, ComponentItem> PRIMITIVE_BOOTS_FRAME = registerComponent("primitive_boots_frame");
    public static final DeferredHolder<Item, ComponentItem> PRIMITIVE_POWERFIST_FRAME = registerComponent("primitive_powerfist_frame");

    static {
        MPS_ITEMS.addAlias(ResourceLocation.fromNamespaceAndPath(MPSConstants.MOD_ID, "component_wiring_copper"), WIRING_STANDARD.getId());
        MPS_ITEMS.addAlias(ResourceLocation.fromNamespaceAndPath(MPSConstants.MOD_ID, "component_wiring_gold"), WIRING_HIGH_CONDUCTIVITY.getId());
    }

    // Smithing Upgrades ==============================================================================================
    public static final DeferredHolder<Item, SmithingTemplateItem> TIER_2_SMITHING_UPGRADE = MPS_ITEMS.register(MPSConstants.TIER_2_SMITHING_UPGRADE.getPath(),
        ()-> new SmithingTemplateItem(
        MPSConstants.TIER_2_UPGRADE_APPLIES_TO,
        MPSConstants.TIER_2_UPGRADE_INGREDIENTS,
        MPSConstants.TIER_2_UPGRADE,
        MPSConstants.TIER_2_UPGRADE_BASE_SLOT_DESCRIPTION,
        MPSConstants.TIER_2_UPGRADE_ADDITIONS_SLOT_DESCRIPTION,
        createItemUpgradeIconList(),
        createNetheriteUpgradeMaterialList()));

    public static final DeferredHolder<Item, SmithingTemplateItem> TIER_3_SMITHING_UPGRADE = MPS_ITEMS.register(MPSConstants.TIER_3_SMITHING_UPGRADE.getPath(),
        ()-> new SmithingTemplateItem(
        MPSConstants.TIER_3_UPGRADE_APPLIES_TO,
        MPSConstants.TIER_3_UPGRADE_INGREDIENTS,
        MPSConstants.TIER_3_UPGRADE,
        MPSConstants.TIER_3_UPGRADE_BASE_SLOT_DESCRIPTION,
        MPSConstants.TIER_3_UPGRADE_ADDITIONS_SLOT_DESCRIPTION,
        createItemUpgradeIconList(),
        createNetheriteUpgradeMaterialList()));

    public static final DeferredHolder<Item, SmithingTemplateItem> TIER_4_SMITHING_UPGRADE = MPS_ITEMS.register(MPSConstants.TIER_4_SMITHING_UPGRADE.getPath(),
        ()-> new SmithingTemplateItem(
        MPSConstants.TIER_4_UPGRADE_APPLIES_TO,
        MPSConstants.TIER_4_UPGRADE_INGREDIENTS,
        MPSConstants.TIER_4_UPGRADE,
        MPSConstants.TIER_4_UPGRADE_BASE_SLOT_DESCRIPTION,
        MPSConstants.TIER_4_UPGRADE_ADDITIONS_SLOT_DESCRIPTION,
        createItemUpgradeIconList(),
        createNetheriteUpgradeMaterialList()));

    private static List<ResourceLocation> createItemUpgradeIconList() {
        return List.of(
            InventoryMenu.EMPTY_ARMOR_SLOT_HELMET,
            SlotBackgrounds.EMPTY_SLOT_SWORD,
            InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE,
            SlotBackgrounds.EMPTY_SLOT_PICKAXE,
            InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS,
            SlotBackgrounds.EMPTY_SLOT_AXE,
            InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS,
            SlotBackgrounds.EMPTY_SLOT_HOE,
            SlotBackgrounds.EMPTY_SLOT_SHOVEL);
    }

    private static List<ResourceLocation> createNetheriteUpgradeMaterialList() {
        return List.of(SlotBackgrounds.EMPTY_SLOT_INGOT);
    }

    static DeferredHolder<Item, ComponentItem> registerComponent(String name) {
        return MPS_ITEMS.register(name, ComponentItem::new);
    }

    static DeferredHolder<Item, Battery> registerBattery(String name, int tier) {
        return ModuleRegistry.INSTANCE.registerItem(MPS_ITEMS,
            ResourceLocation.fromNamespaceAndPath(MPSConstants.MOD_ID, name), Battery::new,
            Battery.BatteryPowerModule.class, stack -> new Battery.BatteryPowerModule(stack, tier), -1);
    }

    @Nonnull
    static ItemStack getChargedBattery(Item item) {
        ItemStack out = new ItemStack(item);
        IEnergyStorage energyStorage = out.getCapability(Capabilities.EnergyStorage.ITEM);
        if(energyStorage != null) {
            int max = energyStorage.getMaxEnergyStored();
            while (energyStorage.getEnergyStored() < energyStorage.getMaxEnergyStored()) {
                energyStorage.receiveEnergy(max, false);
            }
        }
        return out;
    }

    // Block Items ====================================================================================================
    // use directly as a module
    public static final DeferredHolder<Item, BlockItem> TINKER_TABLE_ITEM = registerModule(MPSConstants.TINKER_TABLE,
        () -> new TinkerTableItem(MPSBlocks.TINKER_TABLE_BLOCK.get()), TinkerTableCap.class,
        TinkerTableCap::new, 2);

    public static final DeferredHolder<Item, BlockItem> LUX_CAPACITOR_ITEM = MPS_ITEMS.register(MPSConstants.LUX_CAPACITOR.getPath(),
        () -> new BlockItem(MPSBlocks.LUX_CAPACITOR_BLOCK.get(), new Item.Properties()
        .stacksTo(64)
        .setNoRepair()));

    // Debug Item =====================================================================================================
    //    public static final DeferredHolder<Item, MPSDebugItem> DEBUG_ITEM = MPS_ITEMS.register("debugitem", MPSDebugItem::new);

    // Tools ==========================================================================================================
    public static final DeferredHolder<Item, PowerFist> POWER_FIST_1 = MPS_ITEMS.register(MPSConstants.POWER_FIST_1.getPath(),
        PowerFist::new);

    public static final DeferredHolder<Item, PowerFist> POWER_FIST_2 = MPS_ITEMS.register(MPSConstants.POWER_FIST_2.getPath(),
        PowerFist::new);

    public static final DeferredHolder<Item, PowerFist> POWER_FIST_3 = MPS_ITEMS.register(MPSConstants.POWER_FIST_3.getPath(),
        PowerFist::new);

    public static final DeferredHolder<Item, PowerFist> POWER_FIST_4 = MPS_ITEMS.register(MPSConstants.POWER_FIST_4.getPath(),
        PowerFist::new);

    // Armor ==========================================================================================================
    // Prototype Modular Armor
    public static final DeferredHolder<Item, AbstractElectricArmor> POWER_ARMOR_HELMET_1 = MPS_ITEMS.register(MPSConstants.POWER_ARMOR_HELMET_1.getPath(),
        () -> new AbstractElectricArmor(ArmorItem.Type.HELMET));

    public static final DeferredHolder<Item, AbstractElectricArmor> POWER_ARMOR_CHESTPLATE_1 = MPS_ITEMS.register(MPSConstants.POWER_ARMOR_CHESTPLATE_1.getPath(),
        () -> new AbstractElectricArmor(ArmorItem.Type.CHESTPLATE));

    public static final DeferredHolder<Item, AbstractElectricArmor> POWER_ARMOR_LEGGINGS_1 = MPS_ITEMS.register(MPSConstants.POWER_ARMOR_LEGGINGS_1.getPath(),
        () -> new AbstractElectricArmor(ArmorItem.Type.LEGGINGS));

    public static final DeferredHolder<Item, AbstractElectricArmor> POWER_ARMOR_BOOTS_1 = MPS_ITEMS.register(MPSConstants.POWER_ARMOR_BOOTS_1.getPath(),
        () -> new AbstractElectricArmor(ArmorItem.Type.BOOTS));

    // Prototype Power Armor Mk2
    public static final DeferredHolder<Item, AbstractElectricArmor> POWER_ARMOR_HELMET_2 = MPS_ITEMS.register(MPSConstants.POWER_ARMOR_HELMET_2.getPath(),
        () -> new AbstractElectricArmor(ArmorItem.Type.HELMET));

    public static final DeferredHolder<Item, AbstractElectricArmor> POWER_ARMOR_CHESTPLATE_2 = MPS_ITEMS.register(MPSConstants.POWER_ARMOR_CHESTPLATE_2.getPath(),
        () -> new AbstractElectricArmor(ArmorItem.Type.CHESTPLATE));

    public static final DeferredHolder<Item, AbstractElectricArmor> POWER_ARMOR_LEGGINGS_2 = MPS_ITEMS.register(MPSConstants.POWER_ARMOR_LEGGINGS_2.getPath(),
        () -> new AbstractElectricArmor(ArmorItem.Type.LEGGINGS));

    public static final DeferredHolder<Item, AbstractElectricArmor> POWER_ARMOR_BOOTS_2 = MPS_ITEMS.register(MPSConstants.POWER_ARMOR_BOOTS_2.getPath(),
        () -> new AbstractElectricArmor(ArmorItem.Type.BOOTS));

    // Power Armor
    public static final DeferredHolder<Item, AbstractElectricArmor> POWER_ARMOR_HELMET_3 = MPS_ITEMS.register(MPSConstants.POWER_ARMOR_HELMET_3.getPath(),
        () -> new AbstractElectricArmor(ArmorItem.Type.HELMET));

    public static final DeferredHolder<Item, AbstractElectricArmor> POWER_ARMOR_CHESTPLATE_3 = MPS_ITEMS.register(MPSConstants.POWER_ARMOR_CHESTPLATE_3.getPath(),
        () -> new AbstractElectricArmor(ArmorItem.Type.CHESTPLATE));

    public static final DeferredHolder<Item, AbstractElectricArmor> POWER_ARMOR_LEGGINGS_3 = MPS_ITEMS.register(MPSConstants.POWER_ARMOR_LEGGINGS_3.getPath(),
        () -> new AbstractElectricArmor(ArmorItem.Type.LEGGINGS));

    public static final DeferredHolder<Item, AbstractElectricArmor> POWER_ARMOR_BOOTS_3 = MPS_ITEMS.register(MPSConstants.POWER_ARMOR_BOOTS_3.getPath(),
        () -> new AbstractElectricArmor(ArmorItem.Type.BOOTS));

    // Power Armor MK2
    public static final DeferredHolder<Item, AbstractElectricArmor> POWER_ARMOR_HELMET_4 = MPS_ITEMS.register(MPSConstants.POWER_ARMOR_HELMET_4.getPath(),
        () -> new AbstractElectricArmor(ArmorItem.Type.HELMET));

    public static final DeferredHolder<Item, AbstractElectricArmor> POWER_ARMOR_CHESTPLATE_4 = MPS_ITEMS.register(MPSConstants.POWER_ARMOR_CHESTPLATE_4.getPath(),
        () -> new AbstractElectricArmor(ArmorItem.Type.CHESTPLATE));

    public static final DeferredHolder<Item, AbstractElectricArmor> POWER_ARMOR_LEGGINGS_4 = MPS_ITEMS.register(MPSConstants.POWER_ARMOR_LEGGINGS_4.getPath(),
        () -> new AbstractElectricArmor(ArmorItem.Type.LEGGINGS));

    public static final DeferredHolder<Item, AbstractElectricArmor> POWER_ARMOR_BOOTS_4 = MPS_ITEMS.register(MPSConstants.POWER_ARMOR_BOOTS_4.getPath(),
        () -> new AbstractElectricArmor(ArmorItem.Type.BOOTS));

    // TODO:
    //    public static final DeferredHolder<Item, SmithingTemplateItem> MODULAR_ITEM_TIER_2_SMITHING_UPGRADE_TEMPLATE = MPS_ITEMS.register("test", SmithingTemplateItem::new)
    //    public static final DeferredHolder<Item, SmithingTemplateItem> MODULAR_ITEM_TIER_3_SMITHING_UPGRADE_TEMPLATE = MPS_ITEMS.register("test", SmithingTemplateItem::new)
    //    public static final DeferredHolder<Item, SmithingTemplateItem> MODULAR_ITEM_TIER_4_SMITHING_UPGRADE_TEMPLATE = MPS_ITEMS.register("test", SmithingTemplateItem::new)

    // Modules ========================================================================================================
    // Armor --------------------------------------------------------------------------------------
    public static final DeferredHolder<Item, ArmorPlatingModule> IRON_PLATING_MODULE = registerModule(MPSConstants.IRON_PLATING_MODULE,
        ArmorPlatingModule::new, ArmorPlatingModule.ArmorPlatingCapabilityWrapper.class,
        stack -> new ArmorPlatingModule.ArmorPlatingCapabilityWrapper(stack, 1));

    public static final DeferredHolder<Item, ArmorPlatingModule> DIAMOND_PLATING_MODULE = registerModule(MPSConstants.DIAMOND_PLATING_MODULE,
        ArmorPlatingModule::new, ArmorPlatingModule.ArmorPlatingCapabilityWrapper.class,
        stack -> new ArmorPlatingModule.ArmorPlatingCapabilityWrapper(stack, 2));

    public static final DeferredHolder<Item, ArmorPlatingModule> NETHERITE_PLATING_MODULE = registerModule(MPSConstants.NETHERITE_PLATING_MODULE,
        ArmorPlatingModule::new, ArmorPlatingModule.ArmorPlatingCapabilityWrapper.class,
        stack -> new ArmorPlatingModule.ArmorPlatingCapabilityWrapper(stack, 3));

    public static final DeferredHolder<Item, EnergyShieldModule> ENERGY_SHIELD_MODULE = registerModule(MPSConstants.ENERGY_SHIELD_MODULE,
        EnergyShieldModule::new, EnergyShieldModule.EnergyShieldCapabilityWrapper.class,
        EnergyShieldModule.EnergyShieldCapabilityWrapper::new);

    // Cosmetic -----------------------------------------------------------------------------------
    public static final DeferredHolder<Item, TransparentArmorModule> TRANSPARENT_ARMOR_MODULE = registerModule(MPSConstants.TRANSPARENT_ARMOR_MODULE,
        TransparentArmorModule::new, TransparentArmorModule.TransParentArmorCap.class,
        TransparentArmorModule.TransParentArmorCap::new);

    // Debug --------------------------------------------------------------------------------------
    //    public static final DeferredHolder<Item, DebugModule> DEBUG_MODULE = MPS_ITEMS.register("debugmodule", DebugModule::new);
    //

    // Energy Generation --------------------------------------------------------------------------

    // Heat Generators
    public static DeferredHolder<Item, ThermalGeneratorModule> THERMAL_GENERATOR_MODULE_1 = registerModule(MPSConstants.THERMAL_GENERATOR_MODULE_1,
        ThermalGeneratorModule::new, ThermalGeneratorModule.ThermalGeneratorTickingCapability.class,
        stack -> new ThermalGeneratorModule.ThermalGeneratorTickingCapability(stack, 1), 1);

    public static DeferredHolder<Item, ThermalGeneratorModule> THERMAL_GENERATOR_MODULE_2 = registerModule(MPSConstants.THERMAL_GENERATOR_MODULE_2,
        ThermalGeneratorModule::new, ThermalGeneratorModule.ThermalGeneratorTickingCapability.class,
        stack -> new ThermalGeneratorModule.ThermalGeneratorTickingCapability(stack, 2), 2);

    public static DeferredHolder<Item, ThermalGeneratorModule> THERMAL_GENERATOR_MODULE_3 = registerModule(MPSConstants.THERMAL_GENERATOR_MODULE_3,
        ThermalGeneratorModule::new, ThermalGeneratorModule.ThermalGeneratorTickingCapability.class,
        stack -> new ThermalGeneratorModule.ThermalGeneratorTickingCapability(stack, 3), 3);

    public static DeferredHolder<Item, ThermalGeneratorModule> THERMAL_GENERATOR_MODULE_4 = registerModule(MPSConstants.THERMAL_GENERATOR_MODULE_4,
        ThermalGeneratorModule::new, ThermalGeneratorModule.ThermalGeneratorTickingCapability.class,
        stack -> new ThermalGeneratorModule.ThermalGeneratorTickingCapability(stack, 4), 4);

    // Solar
    public static DeferredHolder<Item, SolarGeneratorModule> SOLAR_GENERATOR_MODULE_1 = registerModule(MPSConstants.SOLAR_GENERATOR_MODULE_1,
        SolarGeneratorModule::new, SolarGeneratorModule.SolarGeneratorTickingCapability.class,
        stack -> new SolarGeneratorModule.SolarGeneratorTickingCapability(stack, 1), 1);

    public static DeferredHolder<Item, SolarGeneratorModule> SOLAR_GENERATOR_MODULE_2 = registerModule(MPSConstants.SOLAR_GENERATOR_MODULE_2,
        SolarGeneratorModule::new, SolarGeneratorModule.SolarGeneratorTickingCapability.class,
        stack -> new SolarGeneratorModule.SolarGeneratorTickingCapability(stack, 2), 2);

    public static DeferredHolder<Item, SolarGeneratorModule> SOLAR_GENERATOR_MODULE_3 = registerModule(MPSConstants.SOLAR_GENERATOR_MODULE_3,
        SolarGeneratorModule::new, SolarGeneratorModule.SolarGeneratorTickingCapability.class,
        stack -> new SolarGeneratorModule.SolarGeneratorTickingCapability(stack, 3), 3);

    public static DeferredHolder<Item, SolarGeneratorModule> SOLAR_GENERATOR_MODULE_4 = registerModule(MPSConstants.SOLAR_GENERATOR_MODULE_4,
        SolarGeneratorModule::new, SolarGeneratorModule.SolarGeneratorTickingCapability.class,
        stack -> new SolarGeneratorModule.SolarGeneratorTickingCapability(stack, 4), 4);

    // TODO
    // Trying to come up with ideas that make sense and fit, not just with this mod, but other mods

    // Environmental ------------------------------------------------------------------------------
    public static DeferredHolder<Item, ActiveCamouflageModule> ACTIVE_CAMOUFLAGE_MODULE = registerModule(MPSConstants.ACTIVE_CAMOUFLAGE_MODULE,
        ActiveCamouflageModule::new, ActiveCamouflageModule.Ticker.class,
        ActiveCamouflageModule.Ticker::new);

    public static DeferredHolder<Item, AutoFeederModule> AUTO_FEEDER_MODULE = registerModule(MPSConstants.AUTO_FEEDER_MODULE,
        AutoFeederModule::new, AutoFeederModule.Ticker.class,
        AutoFeederModule.Ticker::new, 2);

    public static DeferredHolder<Item, CoolantTankModule> COOLANT_TANK_MODULE_1 = registerModule(MPSConstants.COOLANT_TANK_MODULE_1,
        CoolantTankModule::new, CoolantTankModule.Ticker.class,
        stack -> new CoolantTankModule.Ticker(stack, 1), 1);

    public static DeferredHolder<Item, CoolantTankModule> COOLANT_TANK_MODULE_2 = registerModule(MPSConstants.COOLANT_TANK_MODULE_2,
        CoolantTankModule::new, CoolantTankModule.Ticker.class,
        stack -> new CoolantTankModule.Ticker(stack, 2), 2);

    public static DeferredHolder<Item, CoolantTankModule> COOLANT_TANK_MODULE_3 = registerModule(MPSConstants.COOLANT_TANK_MODULE_3,
        CoolantTankModule::new, CoolantTankModule.Ticker.class,
        stack -> new CoolantTankModule.Ticker(stack, 3), 3);

    public static DeferredHolder<Item, CoolantTankModule> COOLANT_TANK_MODULE_4 = registerModule(MPSConstants.COOLANT_TANK_MODULE_4,
        CoolantTankModule::new, CoolantTankModule.Ticker.class,
        stack -> new CoolantTankModule.Ticker(stack, 4), 4);

    public static DeferredHolder<Item, MagnetModule> MAGNET_MODULE = registerModule(MPSConstants.MAGNET_MODULE,
        MagnetModule::new, MagnetModule.Ticker.class,
        MagnetModule.Ticker::new);

    public static DeferredHolder<Item, MobRepulsorModule> MOB_REPULSOR_MODULE = registerModule(MPSConstants.MOB_REPULSOR_MODULE,
        MobRepulsorModule::new, MobRepulsorModule.Ticker.class,
        MobRepulsorModule.Ticker::new, 2);

    public static DeferredHolder<Item, PiglinPacificationModule> PIGLIN_PACIFICATION_MODULE = registerModule(MPSConstants.PIGLIN_PACIFICATION_MODULE,
        PiglinPacificationModule::new, PiglinPacificationModule.PiglinPacificationCap.class,
        PiglinPacificationModule.PiglinPacificationCap::new, 2);

    public static DeferredHolder<Item, WaterElectrolyzerModule> WATER_ELECTROLYZER_MODULE = registerModule(MPSConstants.WATER_ELECTROLYZER_MODULE,
        WaterElectrolyzerModule::new, WaterElectrolyzerModule.Ticker.class,
        WaterElectrolyzerModule.Ticker::new, 2);

    // Mining Enchantments ------------------------------------------------------------------------
    public static DeferredHolder<Item, AquaAffinityModule> AQUA_AFFINITY_MODULE = registerModule(MPSConstants.AQUA_AFFINITY_MODULE,
        AquaAffinityModule::new, AquaAffinityModule.TickingEnchantment.class,
        AquaAffinityModule.TickingEnchantment::new);

    public static DeferredHolder<Item, FortuneModule> FORTUNE_MODULE = registerModule(MPSConstants.FORTUNE_MODULE,
        FortuneModule::new, FortuneModule.TickingEnchantment.class,
        FortuneModule.TickingEnchantment::new);

    public static DeferredHolder<Item, SilkTouchModule> SILK_TOUCH_MODULE = registerModule(MPSConstants.SILK_TOUCH_MODULE,
        SilkTouchModule::new, SilkTouchModule.TickingEnchantment.class,
        SilkTouchModule.TickingEnchantment::new);

    // Mining Enhancements ------------------------------------------------------------------------
    public static DeferredHolder<Item, TunnelBoreModule> TUNNEL_BORE_MODULE = registerModule(MPSConstants.TUNNEL_BORE_MODULE,
        TunnelBoreModule::new, TunnelBoreModule.Enhancement.class,
        TunnelBoreModule.Enhancement::new, 2);

    public static DeferredHolder<Item, VeinMinerModule> VEIN_MINER_MODULE = registerModule(MPSConstants.VEIN_MINER_MODULE,
        VeinMinerModule::new, VeinMinerModule.Enhancement.class,
        VeinMinerModule.Enhancement::new, 2);

    public static DeferredHolder<Item, SelectiveMiner> SELECTIVE_MINER_MODULE = registerModule(MPSConstants.SELECTIVE_MINER_MODULE,
        SelectiveMiner::new, SelectiveMiner.Enhancement.class,
        SelectiveMiner.Enhancement::new, 2);

    // Movement -----------------------------------------------------------------------------------
    public static DeferredHolder<Item, BlinkDriveModule> BLINK_DRIVE_MODULE = registerModule(MPSConstants.BLINK_DRIVE_MODULE,
        BlinkDriveModule::new, BlinkDriveModule.RightClickie.class,
        BlinkDriveModule.RightClickie::new, 2);

    public static DeferredHolder<Item, ClimbAssistModule> UPHILL_STEP_ASSIST_MODULE = registerModule(MPSConstants.UPHILL_STEP_ASSIST_MODULE,
        ClimbAssistModule::new, ClimbAssistModule.ClimbAssistCap.class,
        ClimbAssistModule.ClimbAssistCap::new, 2);

    public static DeferredHolder<Item, DimensionalRiftModule> DIMENSIONAL_TEAR_MODULE = registerModule(MPSConstants.DIMENSIONAL_TEAR_MODULE,
        DimensionalRiftModule::new, DimensionalRiftModule.RightClickie.class,
        DimensionalRiftModule.RightClickie::new, 2);

    public static DeferredHolder<Item, FlightControlModule> FLIGHT_CONTROL_MODULE = registerModule(MPSConstants.FLIGHT_CONTROL_MODULE,
        FlightControlModule::new, FlightControlModule.Toggler.class,
        FlightControlModule.Toggler::new, 3);

    public static DeferredHolder<Item, GliderModule> GLIDER_MODULE = registerModule(MPSConstants.GLIDER_MODULE,
        GliderModule::new, GliderModule.Ticker.class,
        GliderModule.Ticker::new, 2);

    public static DeferredHolder<Item, JetBootsModule> JET_BOOTS_MODULE = registerModule(MPSConstants.JETBOOTS_MODULE,
        JetBootsModule::new, JetBootsModule.Ticker.class,
        JetBootsModule.Ticker::new, 3);

    public static DeferredHolder<Item, JetPackModule> JETPACK_MODULE = registerModule(MPSConstants.JETPACK_MODULE,
        JetPackModule::new, JetPackModule.Ticker.class,
        JetPackModule.Ticker::new, 3);

    public static DeferredHolder<Item, JumpAssistModule> JUMP_ASSIST_MODULE = registerModule(MPSConstants.JUMP_ASSIST_MODULE,
        JumpAssistModule::new, JumpAssistModule.Ticker.class,
        JumpAssistModule.Ticker::new, 2);


    public static DeferredHolder<Item, ShockAbsorberModule> SHOCK_ABSORBER_MODULE = registerModule(MPSConstants.SHOCK_ABSORBER_MODULE,
        ShockAbsorberModule::new, ShockAbsorberModule.Toggler.class,
        ShockAbsorberModule.Toggler::new, 2);

    public static DeferredHolder<Item, SprintAssistModule> SPRINT_ASSIST_MODULE = registerModule(MPSConstants.SPRINT_ASSIST_MODULE,
        SprintAssistModule::new, SprintAssistModule.Ticker.class,
        SprintAssistModule.Ticker::new, 3);

    public static DeferredHolder<Item, SwimAssistModule> SWIM_BOOST_MODULE = registerModule(MPSConstants.SWIM_BOOST_MODULE,
        SwimAssistModule::new, SwimAssistModule.Ticker.class,
        SwimAssistModule.Ticker::new, 2);

    // Tools --------------------------------------------------------------------------------------
    // Axe
    public static final DeferredHolder<Item, AxeModule> STONE_AXE_MODULE = registerModule(MPSConstants.STONE_AXE_MODULE,
        AxeModule::new, AxeModule.BlockBreaker.class,
        stack -> new AxeModule.BlockBreaker(stack, 1));

    public static final DeferredHolder<Item, AxeModule> IRON_AXE_MODULE = registerModule(MPSConstants.IRON_AXE_MODULE,
        AxeModule::new, AxeModule.BlockBreaker.class,
        stack -> new AxeModule.BlockBreaker(stack, 2));

    public static final DeferredHolder<Item, AxeModule> DIAMOND_AXE_MODULE = registerModule(MPSConstants.DIAMOND_AXE_MODULE,
        AxeModule::new, AxeModule.BlockBreaker.class,
        stack -> new AxeModule.BlockBreaker(stack, 3));

    public static final DeferredHolder<Item, AxeModule> NETHERITE_AXE_MODULE = registerModule(MPSConstants.NETHERITE_AXE_MODULE,
        AxeModule::new, AxeModule.BlockBreaker.class,
        stack -> new AxeModule.BlockBreaker(stack, 4));

    // Pickaxe
    public static final DeferredHolder<Item, PickaxeModule> STONE_PICKAXE_MODULE = registerModule(MPSConstants.STONE_PICKAXE_MODULE,
        PickaxeModule::new, PickaxeModule.BlockBreaker.class,
        stack -> new PickaxeModule.BlockBreaker(stack, 1));

    public static final DeferredHolder<Item, PickaxeModule> IRON_PICKAXE_MODULE = registerModule(MPSConstants.IRON_PICKAXE_MODULE,
        PickaxeModule::new, PickaxeModule.BlockBreaker.class,
        stack -> new PickaxeModule.BlockBreaker(stack, 2));

    public static final DeferredHolder<Item, PickaxeModule> DIAMOND_PICKAXE_MODULE = registerModule(MPSConstants.DIAMOND_PICKAXE_MODULE,
        PickaxeModule::new, PickaxeModule.BlockBreaker.class,
        stack -> new PickaxeModule.BlockBreaker(stack, 3));

    public static final DeferredHolder<Item, PickaxeModule> NETHERITE_PICKAXE_MODULE = registerModule(MPSConstants.NETHERITE_PICKAXE_MODULE,
        PickaxeModule::new, PickaxeModule.BlockBreaker.class,
        stack -> new PickaxeModule.BlockBreaker(stack, 4));

    // Shovel
    public static final DeferredHolder<Item, ShovelModule> STONE_SHOVEL_MODULE = registerModule(MPSConstants.STONE_SHOVEL_MODULE,
        ShovelModule::new, ShovelModule.BlockBreaker.class,
        stack -> new ShovelModule.BlockBreaker(stack, 1));

    public static final DeferredHolder<Item, ShovelModule> IRON_SHOVEL_MODULE = registerModule(MPSConstants.IRON_SHOVEL_MODULE,
        ShovelModule::new, ShovelModule.BlockBreaker.class,
        stack -> new ShovelModule.BlockBreaker(stack, 2));

    public static final DeferredHolder<Item, ShovelModule> DIAMOND_SHOVEL_MODULE = registerModule(MPSConstants.DIAMOND_SHOVEL_MODULE,
        ShovelModule::new, ShovelModule.BlockBreaker.class,
        stack -> new ShovelModule.BlockBreaker(stack, 3));

    public static final DeferredHolder<Item, ShovelModule> NETHERITE_SHOVEL_MODULE = registerModule(MPSConstants.NETHERITE_SHOVEL_MODULE,
        ShovelModule::new, ShovelModule.BlockBreaker.class,
        stack -> new ShovelModule.BlockBreaker(stack, 4));

    // Farming
    public static final DeferredHolder<Item, HoeModule> STONE_ROTOTILLER_MODULE = registerModule(MPSConstants.ROTOTILLER_MODULE_1,
        HoeModule::new, HoeModule.RightClickie.class,
        stack -> new HoeModule.RightClickie(stack, 1));

    public static final DeferredHolder<Item, HoeModule> IRON_ROTOTILLER_MODULE = registerModule(MPSConstants.ROTOTILLER_MODULE_2,
        HoeModule::new, HoeModule.RightClickie.class,
        stack -> new HoeModule.RightClickie(stack, 2));

    public static final DeferredHolder<Item, HoeModule> DIAMOND_ROTOTILLER_MODULE = registerModule(MPSConstants.ROTOTILLER_MODULE_3,
        HoeModule::new, HoeModule.RightClickie.class,
        stack -> new HoeModule.RightClickie(stack, 3));

    public static final DeferredHolder<Item, HoeModule> NETHERITE_ROTOTILLER_MODULE = registerModule(MPSConstants.ROTOTILLER_MODULE_4,
        HoeModule::new, HoeModule.RightClickie.class,
        stack -> new HoeModule.RightClickie(stack, 4));

    public static final DeferredHolder<Item, ShearsModule> SHEARS_MODULE = registerModule(MPSConstants.SHEARS_MODULE,
        ShearsModule::new, ShearsModule.BlockBreaker.class,
        ShearsModule.BlockBreaker::new);

    // Misc
    public static final DeferredHolder<Item, FlintAndSteelModule> FLINT_AND_STEEL_MODULE = registerModule(MPSConstants.FLINT_AND_STEEL_MODULE,
        FlintAndSteelModule::new, FlintAndSteelModule.RightClickie.class,
        FlintAndSteelModule.RightClickie::new);

    public static final DeferredHolder<Item, UniversalWrenchModule> UNIVERSAL_WRENCH_MODULE = registerModule(MPSConstants.UNIVERSAL_WRENCH_MODULE,
        UniversalWrenchModule::new, UniversalWrenchModule.WrenchCapability.class,
        UniversalWrenchModule.WrenchCapability::new, 2);

    public static final DeferredHolder<Item, LeafBlowerModule> LEAF_BLOWER_MODULE = registerModule(MPSConstants.LEAF_BLOWER_MODULE,
        LeafBlowerModule::new, LeafBlowerModule.Ticker.class,
        LeafBlowerModule.Ticker::new);

    public static final DeferredHolder<Item, LuxCapacitorModule> LUX_CAPACITOR_MODULE = registerModule(MPSConstants.LUX_CAPACITOR_MODULE,
        LuxCapacitorModule::new, LuxCapacitorModule.RightClickie.class,
        LuxCapacitorModule.RightClickie::new, 2);

    // Vision ------------------------------------------------------------------------------------
    public static final DeferredHolder<Item, BinocularsModule> BINOCULARS_MODULE = registerModule(MPSConstants.BINOCULARS_MODULE,
        BinocularsModule::new, BinocularsModule.BinocularCap.class,
        BinocularsModule.BinocularCap::new);

    public static final DeferredHolder<Item, NightVisionModule> NIGHTVISION_MODULE = registerModule(MPSConstants.NIGHT_VISION_MODULE,
        NightVisionModule::new, NightVisionModule.Ticker.class,
        NightVisionModule.Ticker::new, 2);

    // Weapons ------------------------------------------------------------------------------------
    public static final DeferredHolder<Item, BladeLauncherModule> BLADE_LAUNCHER_MODULE = registerModule(MPSConstants.BLADE_LAUNCHER_MODULE,
        BladeLauncherModule::new, BladeLauncherModule.RightClickie.class,
        BladeLauncherModule.RightClickie::new);

    public static final DeferredHolder<Item, LightningModule> LIGHTNING_SUMMONER_MODULE = registerModule(MPSConstants.LIGHTNING_MODULE,
        LightningModule::new, LightningModule.RightClickie.class,
        LightningModule.RightClickie::new, 3);

    public static final DeferredHolder<Item, MeleeAssistModule> MELEE_ASSIST_MODULE = registerModule(MPSConstants.MELEE_ASSIST_MODULE,
        MeleeAssistModule::new, MeleeAssistModule.PMCap.class,
        MeleeAssistModule.PMCap::new);

    public static final DeferredHolder<Item, PlasmaCannonModule> PLASMA_CANNON_MODULE = registerModule(MPSConstants.PLASMA_CANNON_MODULE,
        PlasmaCannonModule::new, PlasmaCannonModule.RightClickie.class,
        PlasmaCannonModule.RightClickie::new, 3);

    public static final DeferredHolder<Item, RailgunModule> RAILGUN_MODULE = registerModule(MPSConstants.RAILGUN_MODULE,
        RailgunModule::new, RailgunModule.Ticker.class,
        RailgunModule.Ticker::new, 4);

    //    public static final DeferredHolder<Item, SonicWeaponModule> SONIC_WEAPON_MODULE = MPS_ITEMS.register(MPSConstants.SONIC_WEAPON_MODULE.getPath(),
    //            SonicWeaponModule::new);

    // Creative Mode Tab --------------------------------------------------------------------------
    static {
        ModuleRegistry.INSTANCE.register(ResourceLocation.withDefaultNamespace("clock"), () -> Items.CLOCK,
            HudModule.class, stack -> new HudModule(stack, ModuleCategory.SPECIAL, ModuleTarget.HEADONLY, true), 2);
        ModuleRegistry.INSTANCE.register(ResourceLocation.withDefaultNamespace("compass"), () -> Items.COMPASS,
            HudModule.class, stack -> new HudModule(stack, ModuleCategory.SPECIAL, ModuleTarget.HEADONLY, true), -1);
        ModuleRegistry.INSTANCE.register(ResourceLocation.withDefaultNamespace("recovery_compass"), () -> Items.RECOVERY_COMPASS,
            HudModule.class, stack -> new HudModule(stack, ModuleCategory.SPECIAL, ModuleTarget.HEADONLY, true), 2);
        ModuleRegistry.INSTANCE.register(ResourceLocation.withDefaultNamespace("crafting_table"), () -> Items.CRAFTING_TABLE,
            PortableCraftingCapability.RightClickie.class, stack -> new PortableCraftingCapability.RightClickie(stack), -1);
    }

    private static <I extends Item, P extends PowerModule> DeferredHolder<Item, I> registerModule(
        ResourceLocation id, Supplier<I> itemFactory, Class<P> capabilityType, Function<ItemStack, P> capabilityFactory) {
        return registerModule(id, itemFactory, capabilityType, capabilityFactory, -1);
    }

    private static <I extends Item, P extends PowerModule> DeferredHolder<Item, I> registerModule(
        ResourceLocation id, Supplier<I> itemFactory, Class<P> capabilityType,
        Function<ItemStack, P> capabilityFactory, int minimumTierOverride) {
        return ModuleRegistry.INSTANCE.registerItem(MPS_ITEMS, id, itemFactory, capabilityType, capabilityFactory, minimumTierOverride);
    }

    public static final DeferredRegister<CreativeModeTab> MPS_CREATIVE_MODE_TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MPSConstants.MOD_ID);

    public static final Supplier<CreativeModeTab> MPS_TAB = MPS_CREATIVE_MODE_TAB.register("creative_tab",
        ()-> CreativeModeTab.builder().icon(()->new ItemStack(POWER_ARMOR_HELMET_1.get()))
        .title(Component.translatable(MPSConstants.CREATIVE_TAB_TRANSLATION_KEY))
        .displayItems((parameters, output) -> {

            // Components ------------------------------------------------------------------
            output.accept(ARTIFICIAL_MUSCLE.get());
            output.accept(CARBON_MYOFIBER.get());
            output.accept(CAPACITOR_1.get());
            output.accept(CAPACITOR_2.get());
            output.accept(CAPACITOR_3.get());
            output.accept(CAPACITOR_4.get());
            output.accept(COMPUTER_CHIP.get());
            output.accept(CONTROL_CIRCUIT_1.get());
            output.accept(CONTROL_CIRCUIT_2.get());
            output.accept(CONTROL_CIRCUIT_3.get());
            output.accept(CONTROL_CIRCUIT_4.get());
            output.accept(FORCE_FIELD_EMITTER.get());
            output.accept(GLIDER_WING.get());
            output.accept(ION_THRUSTER.get());
            output.accept(HOLOGRAM_EMITTER.get());
            output.accept(MAGNET.get());
            output.accept(MYOFIBER_GEL.get());
            output.accept(PLATING_IRON.get());
            output.accept(PLATING_DIAMOND.get());
            output.accept(PLATING_NETHERITE.get());
            output.accept(RUBBER_HOSE.get());
            output.accept(SERVO.get());
            output.accept(WRENCH_MODULE_FRAME.get());
            output.accept(SHOCK_COIL.get());
            output.accept(SHOCK_MODULE.get());
            for (var entry : mpsremodularized.common.compat.IntermodModules.ENTRIES) {
                if (entry.item().get().available()) {
                    var module = entry.item().get().creativeModule(parameters.holders());
                    if (!module.isEmpty()) output.accept(module);
                }
            }
            output.accept(SOLAR_PANEL.get());
            output.accept(SOLENOID.get());
            output.accept(WIRING_STANDARD.get());
            output.accept(WIRING_HIGH_CONDUCTIVITY.get());
            output.accept(COMPONENT_MOLD.get());
            output.accept(WIRING_TEMPLATE.get());
            output.accept(HIGH_CONDUCTIVITY_WIRING_TEMPLATE.get());
            output.accept(SOLENOID_TEMPLATE.get());
            output.accept(UNFINISHED_HOLOGRAM_EMITTER.get());
            output.accept(UNFINISHED_PLASMA_CANNON.get());
            output.accept(UNFINISHED_RAILGUN.get());
            output.accept(PRIMITIVE_HELMET_FRAME.get());
            output.accept(PRIMITIVE_CHESTPLATE_FRAME.get());
            output.accept(PRIMITIVE_LEGGINGS_FRAME.get());
            output.accept(PRIMITIVE_BOOTS_FRAME.get());
            output.accept(PRIMITIVE_POWERFIST_FRAME.get());

            // Smithing Templates -------------------------------------------------------------------------
            output.accept(TIER_2_SMITHING_UPGRADE.get());
            output.accept(TIER_3_SMITHING_UPGRADE.get());
            output.accept(TIER_4_SMITHING_UPGRADE.get());

            // Modules ------------------------------------------------------------------------------------
            // Energy Storage
            output.accept(BATTERY_1.get());
            output.accept(getChargedBattery(BATTERY_1.get()));

            output.accept(BATTERY_2.get());
            output.accept(getChargedBattery(BATTERY_2.get()));

            output.accept(BATTERY_3.get());
            output.accept(getChargedBattery(BATTERY_3.get()));

            output.accept(BATTERY_4.get());
            output.accept(getChargedBattery(BATTERY_4.get()));

            // Armor Stand --------------------------------------------------------------------------------
            output.accept(ARMOR_STAND_ITEM.get());

            // Block Items --------------------------------------------------------------------------------
            // Charging base
            output.accept(CHARGING_BASE_ITEM.get());

            //                output.accept(DEBUG_ITEM.get());
            // Block Items ============================================================
            output.accept(TINKER_TABLE_ITEM.get());
            output.accept(LUX_CAPACITOR_ITEM.get());

            // Power Fist =============================================================
            output.accept(POWER_FIST_1.get());
            output.accept(POWER_FIST_2.get());
            output.accept(POWER_FIST_3.get());
            output.accept(POWER_FIST_4.get());

            // Armor ==================================================================
            output.accept(POWER_ARMOR_HELMET_1.get());
            output.accept(POWER_ARMOR_CHESTPLATE_1.get());
            output.accept(POWER_ARMOR_LEGGINGS_1.get());
            output.accept(POWER_ARMOR_BOOTS_1.get());

            output.accept(POWER_ARMOR_HELMET_2.get());
            output.accept(POWER_ARMOR_CHESTPLATE_2.get());
            output.accept(POWER_ARMOR_LEGGINGS_2.get());
            output.accept(POWER_ARMOR_BOOTS_2.get());

            output.accept(POWER_ARMOR_HELMET_3.get());
            output.accept(POWER_ARMOR_CHESTPLATE_3.get());
            output.accept(POWER_ARMOR_LEGGINGS_3.get());
            output.accept(POWER_ARMOR_HELMET_4.get());

            output.accept(POWER_ARMOR_CHESTPLATE_4.get());
            output.accept(POWER_ARMOR_LEGGINGS_4.get());
            output.accept(POWER_ARMOR_BOOTS_3.get());
            output.accept(POWER_ARMOR_BOOTS_4.get());

            // Modules ================================================================
            // Armor ----------------------------------------------
            output.accept(IRON_PLATING_MODULE.get());
            output.accept(DIAMOND_PLATING_MODULE.get());
            output.accept(NETHERITE_PLATING_MODULE.get());
            output.accept(ENERGY_SHIELD_MODULE.get());

            //Cosmetic --------------------------------------------
            output.accept(TRANSPARENT_ARMOR_MODULE.get());

            // Debug ----------------------------------------------
            //                output.accept(DEBUG_MODULE.get());

            // Energy Generation ----------------------------------
            // Energy Generation --------------------------------------------------------------------------
            // Thermal
            output.accept(THERMAL_GENERATOR_MODULE_1.get());
            output.accept(THERMAL_GENERATOR_MODULE_2.get());
            output.accept(THERMAL_GENERATOR_MODULE_3.get());
            output.accept(THERMAL_GENERATOR_MODULE_4.get());

            // Solar
            output.accept(SOLAR_GENERATOR_MODULE_1.get());
            output.accept(SOLAR_GENERATOR_MODULE_2.get());
            output.accept(SOLAR_GENERATOR_MODULE_3.get());
            output.accept(SOLAR_GENERATOR_MODULE_4.get());

            // Environmental --------------------------------------
            output.accept(ACTIVE_CAMOUFLAGE_MODULE.get());
            output.accept(AUTO_FEEDER_MODULE.get());

            output.accept(COOLANT_TANK_MODULE_1.get());
            output.accept(COOLANT_TANK_MODULE_2.get());
            output.accept(COOLANT_TANK_MODULE_3.get());
            output.accept(COOLANT_TANK_MODULE_4.get());

            output.accept(MAGNET_MODULE.get());
            output.accept(MOB_REPULSOR_MODULE.get());
            output.accept(PIGLIN_PACIFICATION_MODULE.get());
            output.accept(WATER_ELECTROLYZER_MODULE.get());

            // Mining Enchantment ---------------------------------
            output.accept(AQUA_AFFINITY_MODULE.get());
            output.accept(FORTUNE_MODULE.get());
            output.accept(SILK_TOUCH_MODULE.get());

            // Mining Enhancement ---------------------------------
            output.accept(TUNNEL_BORE_MODULE.get());
            output.accept(VEIN_MINER_MODULE.get());
            output.accept(SELECTIVE_MINER_MODULE.get());

            // Movement -------------------------------------------
            output.accept(BLINK_DRIVE_MODULE.get());
            output.accept(UPHILL_STEP_ASSIST_MODULE.get());
            output.accept(DIMENSIONAL_TEAR_MODULE.get());
            output.accept(FLIGHT_CONTROL_MODULE.get());
            output.accept(GLIDER_MODULE.get());
            output.accept(JET_BOOTS_MODULE.get());
            output.accept(JETPACK_MODULE.get());
            output.accept(JUMP_ASSIST_MODULE.get());
            output.accept(SHOCK_ABSORBER_MODULE.get());
            output.accept(SPRINT_ASSIST_MODULE.get());
            output.accept(SWIM_BOOST_MODULE.get());

            // Tools ----------------------------------------------
            // Axe
            output.accept(STONE_AXE_MODULE.get());
            output.accept(IRON_AXE_MODULE.get());
            output.accept(DIAMOND_AXE_MODULE.get());
            output.accept(NETHERITE_AXE_MODULE.get());

            // Shovel
            output.accept(STONE_SHOVEL_MODULE.get());
            output.accept(IRON_SHOVEL_MODULE.get());
            output.accept(DIAMOND_SHOVEL_MODULE.get());
            output.accept(NETHERITE_SHOVEL_MODULE.get());

            // Farming --------------------------------------------
            output.accept(STONE_ROTOTILLER_MODULE.get());
            output.accept(IRON_ROTOTILLER_MODULE.get());
            output.accept(DIAMOND_ROTOTILLER_MODULE.get());
            output.accept(NETHERITE_ROTOTILLER_MODULE.get());

            // shears
            output.accept(SHEARS_MODULE.get());

            // PickAxe --------------------------------------------
            output.accept(STONE_PICKAXE_MODULE.get());
            output.accept(IRON_PICKAXE_MODULE.get());
            output.accept(DIAMOND_PICKAXE_MODULE.get());
            output.accept(NETHERITE_PICKAXE_MODULE.get());

            // Misc -----------------------------------------------
            output.accept(FLINT_AND_STEEL_MODULE.get());
            output.accept(UNIVERSAL_WRENCH_MODULE.get());
            output.accept(LEAF_BLOWER_MODULE.get());
            output.accept(LUX_CAPACITOR_MODULE.get());

            // Vision ---------------------------------------------
            output.accept(BINOCULARS_MODULE.get());
            output.accept(NIGHTVISION_MODULE.get());

            // Weapon ---------------------------------------------
            output.accept(BLADE_LAUNCHER_MODULE.get());
            output.accept(LIGHTNING_SUMMONER_MODULE.get());
            output.accept(MELEE_ASSIST_MODULE.get());
            output.accept(PLASMA_CANNON_MODULE.get());
            output.accept(RAILGUN_MODULE.get());
            //                        output.accept(SONIC_WEAPON_MODULE.get());
    })
        .build());

    //    static ItemStack getLuxCapModule(Item item) {
    //        ItemStack itemStack = new ItemStack(item);
    //        IPowerModule pm = MPSCapabilities.getPowerModuleCapability(itemStack);
    //        if(pm != null){
    //            itemStack = TagUtils.setModuleDouble(itemStack, MPSConstants.RED_HUE, 0.6);
    //            itemStack = TagUtils.setModuleDouble(itemStack, MPSConstants.GREEN_HUE, 0.7);
    //            itemStack = TagUtils.setModuleDouble(itemStack, MPSConstants.BLUE_HUE, 0.5);
    //            itemStack = TagUtils.setModuleDouble(itemStack, MPSConstants.OPACITY, 0.75);
    //        }
    //
    //        return itemStack;
    //    }

    @SubscribeEvent
    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        //        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
        //            event.accept(EXAMPLE_BLOCK_ITEM);
    }
}
