package mpsremodularized.datagen.common;

import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.render.hud.HudModule;
import mpsremodularized.common.capabilities.module.PortableCraftingCapability;
import mpsremodularized.common.capabilities.module.TinkerTableCap;
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
import mpsremodularized.common.registration.MPSItems;
import mpsremodularized.common.registration.ModuleRegistry;
import mpsremodularized.common.capabilities.module.powermodule.PowerModule;
import mpsremodularized.common.capabilities.module.toggleable.IToggleableModule;
import mpsremodularized.common.capabilities.module.tickable.IPlayerTickModule;
import mpsremodularized.common.capabilities.module.rightclick.IRightClickModule;
import mpsremodularized.common.capabilities.module.blockbreaking.IBlockBreakingModule;
import mpsremodularized.common.capabilities.render.hud.IHudModule;
import mpsremodularized.common.item.Battery;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Fixed pre-registry provider matrix: catches missing providers or accidental factory/tier changes. */
final class ModuleRegistryValidation {
    static void validate() {
        Map<ResourceLocation, Function<ItemStack, PowerModule>> expected = new LinkedHashMap<>();
        expect(expected, MPSItems.IRON_PLATING_MODULE.get(), stack -> new ArmorPlatingModule.ArmorPlatingCapabilityWrapper(stack, 1));
        expect(expected, MPSItems.DIAMOND_PLATING_MODULE.get(), stack -> new ArmorPlatingModule.ArmorPlatingCapabilityWrapper(stack, 2));
        expect(expected, MPSItems.NETHERITE_PLATING_MODULE.get(), stack -> new ArmorPlatingModule.ArmorPlatingCapabilityWrapper(stack, 3));
        expect(expected, MPSItems.ENERGY_SHIELD_MODULE.get(), stack -> new EnergyShieldModule.EnergyShieldCapabilityWrapper(stack));
        expect(expected, MPSItems.TRANSPARENT_ARMOR_MODULE.get(), stack -> new TransparentArmorModule.TransParentArmorCap(stack));
        expect(expected, MPSItems.THERMAL_GENERATOR_MODULE_1.get(), stack -> new ThermalGeneratorModule.ThermalGeneratorTickingCapability(stack, 1).withMinimumTier(1));
        expect(expected, MPSItems.THERMAL_GENERATOR_MODULE_2.get(), stack -> new ThermalGeneratorModule.ThermalGeneratorTickingCapability(stack, 2).withMinimumTier(2));
        expect(expected, MPSItems.THERMAL_GENERATOR_MODULE_3.get(), stack -> new ThermalGeneratorModule.ThermalGeneratorTickingCapability(stack, 3).withMinimumTier(3));
        expect(expected, MPSItems.THERMAL_GENERATOR_MODULE_4.get(), stack -> new ThermalGeneratorModule.ThermalGeneratorTickingCapability(stack, 4).withMinimumTier(4));
        expect(expected, MPSItems.SOLAR_GENERATOR_MODULE_1.get(), stack -> new SolarGeneratorModule.SolarGeneratorTickingCapability(stack, 1).withMinimumTier(1));
        expect(expected, MPSItems.SOLAR_GENERATOR_MODULE_2.get(), stack -> new SolarGeneratorModule.SolarGeneratorTickingCapability(stack, 2).withMinimumTier(2));
        expect(expected, MPSItems.SOLAR_GENERATOR_MODULE_3.get(), stack -> new SolarGeneratorModule.SolarGeneratorTickingCapability(stack, 3).withMinimumTier(3));
        expect(expected, MPSItems.SOLAR_GENERATOR_MODULE_4.get(), stack -> new SolarGeneratorModule.SolarGeneratorTickingCapability(stack, 4).withMinimumTier(4));
        expect(expected, MPSItems.AUTO_FEEDER_MODULE.get(), stack -> new AutoFeederModule.Ticker(stack).withMinimumTier(2));
        expect(expected, MPSItems.ACTIVE_CAMOUFLAGE_MODULE.get(), stack -> new ActiveCamouflageModule.Ticker(stack));
        expect(expected, MPSItems.COOLANT_TANK_MODULE_1.get(), stack -> new CoolantTankModule.Ticker(stack, 1).withMinimumTier(1));
        expect(expected, MPSItems.COOLANT_TANK_MODULE_2.get(), stack -> new CoolantTankModule.Ticker(stack, 2).withMinimumTier(2));
        expect(expected, MPSItems.COOLANT_TANK_MODULE_3.get(), stack -> new CoolantTankModule.Ticker(stack, 3).withMinimumTier(3));
        expect(expected, MPSItems.COOLANT_TANK_MODULE_4.get(), stack -> new CoolantTankModule.Ticker(stack, 4).withMinimumTier(4));
        expect(expected, MPSItems.MAGNET_MODULE.get(), stack -> new MagnetModule.Ticker(stack));
        expect(expected, MPSItems.MOB_REPULSOR_MODULE.get(), stack -> new MobRepulsorModule.Ticker(stack).withMinimumTier(2));
        expect(expected, MPSItems.PIGLIN_PACIFICATION_MODULE.get(), stack -> new PiglinPacificationModule.PiglinPacificationCap(stack).withMinimumTier(2));
        expect(expected, MPSItems.WATER_ELECTROLYZER_MODULE.get(), stack -> new WaterElectrolyzerModule.Ticker(stack).withMinimumTier(2));
        expect(expected, MPSItems.AQUA_AFFINITY_MODULE.get(), stack -> new AquaAffinityModule.TickingEnchantment(stack));
        expect(expected, MPSItems.FORTUNE_MODULE.get(), stack -> new FortuneModule.TickingEnchantment(stack));
        expect(expected, MPSItems.SILK_TOUCH_MODULE.get(), stack -> new SilkTouchModule.TickingEnchantment(stack));
        expect(expected, MPSItems.SELECTIVE_MINER_MODULE.get(), stack -> new SelectiveMiner.Enhancement(stack).withMinimumTier(2));
        expect(expected, MPSItems.TUNNEL_BORE_MODULE.get(), stack -> new TunnelBoreModule.Enhancement(stack).withMinimumTier(2));
        expect(expected, MPSItems.VEIN_MINER_MODULE.get(), stack -> new VeinMinerModule.Enhancement(stack).withMinimumTier(2));
        expect(expected, Items.CLOCK, stack -> new HudModule(stack, ModuleCategory.SPECIAL, ModuleTarget.HEADONLY, true).withMinimumTier(2));
        expect(expected, Items.COMPASS, stack -> new HudModule(stack, ModuleCategory.SPECIAL, ModuleTarget.HEADONLY, true));
        expect(expected, Items.RECOVERY_COMPASS, stack -> new HudModule(stack, ModuleCategory.SPECIAL, ModuleTarget.HEADONLY, true).withMinimumTier(2));
        expect(expected, MPSItems.BLINK_DRIVE_MODULE.get(), stack -> new BlinkDriveModule.RightClickie(stack).withMinimumTier(2));
        expect(expected, MPSItems.UPHILL_STEP_ASSIST_MODULE.get(), stack -> new ClimbAssistModule.ClimbAssistCap(stack).withMinimumTier(2));
        expect(expected, MPSItems.DIMENSIONAL_TEAR_MODULE.get(), stack -> new DimensionalRiftModule.RightClickie(stack).withMinimumTier(2));
        expect(expected, MPSItems.FLIGHT_CONTROL_MODULE.get(), stack -> new FlightControlModule.Toggler(stack).withMinimumTier(3));
        expect(expected, MPSItems.GLIDER_MODULE.get(), stack -> new GliderModule.Ticker(stack).withMinimumTier(2));
        expect(expected, MPSItems.JET_BOOTS_MODULE.get(), stack -> new JetBootsModule.Ticker(stack).withMinimumTier(3));
        expect(expected, MPSItems.JETPACK_MODULE.get(), stack -> new JetPackModule.Ticker(stack).withMinimumTier(3));
        expect(expected, MPSItems.JUMP_ASSIST_MODULE.get(), stack -> new JumpAssistModule.Ticker(stack).withMinimumTier(2));
        expect(expected, MPSItems.SHOCK_ABSORBER_MODULE.get(), stack -> new ShockAbsorberModule.Toggler(stack).withMinimumTier(2));
        expect(expected, MPSItems.SPRINT_ASSIST_MODULE.get(), stack -> new SprintAssistModule.Ticker(stack).withMinimumTier(3));
        expect(expected, MPSItems.SWIM_BOOST_MODULE.get(), stack -> new SwimAssistModule.Ticker(stack).withMinimumTier(2));
        expect(expected, MPSItems.TINKER_TABLE_ITEM.get(), stack -> new TinkerTableCap(stack).withMinimumTier(2));
        expect(expected, MPSItems.STONE_AXE_MODULE.get(), stack -> new AxeModule.BlockBreaker(stack, 1));
        expect(expected, MPSItems.IRON_AXE_MODULE.get(), stack -> new AxeModule.BlockBreaker(stack, 2));
        expect(expected, MPSItems.DIAMOND_AXE_MODULE.get(), stack -> new AxeModule.BlockBreaker(stack, 3));
        expect(expected, MPSItems.NETHERITE_AXE_MODULE.get(), stack -> new AxeModule.BlockBreaker(stack, 4));
        expect(expected, MPSItems.STONE_PICKAXE_MODULE.get(), stack -> new PickaxeModule.BlockBreaker(stack, 1));
        expect(expected, MPSItems.IRON_PICKAXE_MODULE.get(), stack -> new PickaxeModule.BlockBreaker(stack, 2));
        expect(expected, MPSItems.DIAMOND_PICKAXE_MODULE.get(), stack -> new PickaxeModule.BlockBreaker(stack, 3));
        expect(expected, MPSItems.NETHERITE_PICKAXE_MODULE.get(), stack -> new PickaxeModule.BlockBreaker(stack, 4));
        expect(expected, MPSItems.STONE_ROTOTILLER_MODULE.get(), stack -> new HoeModule.RightClickie(stack, 1));
        expect(expected, MPSItems.IRON_ROTOTILLER_MODULE.get(), stack -> new HoeModule.RightClickie(stack, 2));
        expect(expected, MPSItems.DIAMOND_ROTOTILLER_MODULE.get(), stack -> new HoeModule.RightClickie(stack, 3));
        expect(expected, MPSItems.NETHERITE_ROTOTILLER_MODULE.get(), stack -> new HoeModule.RightClickie(stack, 4));
        expect(expected, MPSItems.STONE_SHOVEL_MODULE.get(), stack -> new ShovelModule.BlockBreaker(stack, 1));
        expect(expected, MPSItems.IRON_SHOVEL_MODULE.get(), stack -> new ShovelModule.BlockBreaker(stack, 2));
        expect(expected, MPSItems.DIAMOND_SHOVEL_MODULE.get(), stack -> new ShovelModule.BlockBreaker(stack, 3));
        expect(expected, MPSItems.NETHERITE_SHOVEL_MODULE.get(), stack -> new ShovelModule.BlockBreaker(stack, 4));
        expect(expected, Items.CRAFTING_TABLE, stack -> new PortableCraftingCapability.RightClickie(stack));
        expect(expected, MPSItems.FLINT_AND_STEEL_MODULE.get(), stack -> new FlintAndSteelModule.RightClickie(stack));
        expect(expected, MPSItems.UNIVERSAL_WRENCH_MODULE.get(), stack -> new UniversalWrenchModule.WrenchCapability(stack).withMinimumTier(2));
        expect(expected, MPSItems.LEAF_BLOWER_MODULE.get(), stack -> new LeafBlowerModule.Ticker(stack));
        expect(expected, MPSItems.LUX_CAPACITOR_MODULE.get(), stack -> new LuxCapacitorModule.RightClickie(stack).withMinimumTier(2));
        expect(expected, MPSItems.SHEARS_MODULE.get(), stack -> new ShearsModule.BlockBreaker(stack));
        expect(expected, MPSItems.BINOCULARS_MODULE.get(), stack -> new BinocularsModule.BinocularCap(stack));
        expect(expected, MPSItems.NIGHTVISION_MODULE.get(), stack -> new NightVisionModule.Ticker(stack).withMinimumTier(2));
        expect(expected, MPSItems.BLADE_LAUNCHER_MODULE.get(), stack -> new BladeLauncherModule.RightClickie(stack));
        expect(expected, MPSItems.LIGHTNING_SUMMONER_MODULE.get(), stack -> new LightningModule.RightClickie(stack).withMinimumTier(3));
        expect(expected, MPSItems.MELEE_ASSIST_MODULE.get(), stack -> new MeleeAssistModule.PMCap(stack));
        expect(expected, MPSItems.PLASMA_CANNON_MODULE.get(), stack -> new PlasmaCannonModule.RightClickie(stack).withMinimumTier(3));
        expect(expected, MPSItems.RAILGUN_MODULE.get(), stack -> new RailgunModule.Ticker(stack).withMinimumTier(4));
        expect(expected, MPSItems.BATTERY_1.get(), stack -> new Battery.BatteryPowerModule(stack, 1));
        expect(expected, MPSItems.BATTERY_2.get(), stack -> new Battery.BatteryPowerModule(stack, 2));
        expect(expected, MPSItems.BATTERY_3.get(), stack -> new Battery.BatteryPowerModule(stack, 3));
        expect(expected, MPSItems.BATTERY_4.get(), stack -> new Battery.BatteryPowerModule(stack, 4));
        ModuleRegistry isolated = new ModuleRegistry();
        var holder = isolated.registerItem(DeferredRegister.createItems("registry_test"), ResourceLocation.parse("registry_test:unbound"),
                () -> { throw new AssertionError("Resolved item during discovery"); },
                PowerModule.class, stack -> { throw new AssertionError("Created capability during discovery"); }, -1);
        check(!holder.isBound() && isolated.freeze().getFirst().item() == holder, "Discovery does not resolve deferred items");
        var definitions = ModuleRegistry.INSTANCE.freeze();
        var actualIds = definitions.stream().map(definition -> definition.id()).collect(Collectors.toSet());
        for (var entry : mpsremodularized.common.compat.IntermodModules.ENTRIES) {
            if (entry.kind() == mpsremodularized.common.compat.NativeModuleItem.Kind.GRENADE_LAUNCHER) {
                expect(expected, entry.item().get(), stack -> new mpsremodularized.common.item.module.tool.misc.GascraftLauncherModule(stack).withMinimumTier(entry.tier()));
            } else if (entry.kind() == mpsremodularized.common.compat.NativeModuleItem.Kind.STATION) {
                expect(expected, entry.item().get(), stack -> new mpsremodularized.common.item.module.tool.misc.PortableStationModule(stack).withMinimumTier(entry.tier()));
            } else if (entry.kind() == mpsremodularized.common.compat.NativeModuleItem.Kind.TOOL || entry.kind() == mpsremodularized.common.compat.NativeModuleItem.Kind.CONSTRUCTION_STICK) {
                expect(expected, entry.item().get(), stack -> new mpsremodularized.common.item.module.tool.misc.NativeToolModule(stack).withMinimumTier(entry.tier()));
            } else {
                expect(expected, entry.item().get(), stack -> new mpsremodularized.common.compat.IntermodModules.PassiveNativeModule(stack, entry.kind()).withMinimumTier(entry.tier()));
            }
        }
        expect(expected, MPSItems.SHOCK_MODULE.get(), stack -> new mpsremodularized.common.item.module.environmental.ShockModule.Capability(stack).withMinimumTier(4));
        expect(expected, MPSItems.BLOCK_REACH_MODULE.get(), stack -> new mpsremodularized.common.item.module.environmental.BlockReachModule.Capability(stack).withMinimumTier(4));
        check(definitions.size() == 161 && actualIds.equals(expected.keySet()), "Complete pre-registry provider matrix");
        check(definitions.stream().filter(definition -> definition.isToggleable()).count() == 114, "114 user toggles; generators/coolant are automatic");
        int checks = 3;
        for (var definition : definitions) {
            Item item = definition.item().get();
            check(definition.id().equals(BuiltInRegistries.ITEM.getKey(item)), "Deferred item identity: " + definition.id());
            ItemStack stack = new ItemStack(item);
            PowerModule actual = definition.createCapability(stack);
            PowerModule baseline = expected.get(definition.id()).apply(new ItemStack(item));
            check(actual.getModule() == stack, "Capability belongs to supplied stack: " + definition.id());
            check(actual.getClass() == baseline.getClass(), "Capability type: " + definition.id());
            check(actual.getTier() == baseline.getTier(), "Minimum tier: " + definition.id());
            check(actual.getTarget() == baseline.getTarget() && actual.getCategory() == baseline.getCategory(), "Target/category: " + definition.id());
            check(actual.isAllowed() == baseline.isAllowed(), "Allowed state: " + definition.id());
            for (Class<?> capabilityInterface : new Class<?>[]{IToggleableModule.class, IPlayerTickModule.class,
                    IRightClickModule.class, IBlockBreakingModule.class, IHudModule.class}) {
                check(definition.supports(capabilityInterface) == capabilityInterface.isInstance(baseline),
                        "Capability discovery: " + definition.id() + " / " + capabilityInterface.getSimpleName());
            }
            checks += 11;
        }
        System.out.println("Module provider matrix checks passed: " + checks);
    }

    private static void expect(Map<ResourceLocation, Function<ItemStack, PowerModule>> expected,
                               Item item, Function<ItemStack, PowerModule> factory) {
        expected.put(BuiltInRegistries.ITEM.getKey(item), factory);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
