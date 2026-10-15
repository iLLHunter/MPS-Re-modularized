package mpsremodularized.common.registration;

import mpsremodularized.client.config.MPSClientConfig;
import mpsremodularized.common.config.MPSCommonConfig;
import mpsremodularized.common.config.module.ArmorModuleConfig;
import mpsremodularized.common.config.module.AxeModuleConfig;
import mpsremodularized.common.config.module.CosmeticModuleConfig;
import mpsremodularized.common.config.module.EnergyGenerationModuleConfig;
import mpsremodularized.common.config.module.EnvironmentalModuleConfig;
import mpsremodularized.common.config.module.FluidStorageConfig;
import mpsremodularized.common.config.module.HoeModuleConfig;
import mpsremodularized.common.config.module.MiningEnchantmentModuleConfig;
import mpsremodularized.common.config.module.MiningEnhancementModuleConfig;
import mpsremodularized.common.config.module.MovementModuleConfig;
import mpsremodularized.common.config.module.PickaxeModuleConfig;
import mpsremodularized.common.config.module.ShovelModuleConfig;
import mpsremodularized.common.config.module.ToolModuleConfig;
import mpsremodularized.common.config.module.VisionModuleConfig;
import mpsremodularized.common.config.module.WeaponModuleConfig;
import mpsremodularized.common.constants.MPSConstants;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = MPSConstants.MOD_ID)
public class MPSConfigurations {
    /** The sole filename/spec catalog for registration, datagen labels and validation. */
    public static java.util.Map<String, ModConfigSpec> getSpecs() {
        return java.util.Map.ofEntries(
                java.util.Map.entry("mpsremodularized-client-only.toml", MPSClientConfig.CLIENT_SPEC),
                java.util.Map.entry("mpsremodularized-server.toml", MPSCommonConfig.SERVER_SPEC),
                java.util.Map.entry("modules/armor.toml", ArmorModuleConfig.ARMOR_MODULE_CONFIG_SPEC),
                java.util.Map.entry("modules/cosmetic.toml", CosmeticModuleConfig.COSMETIC_MODULE_CONFIG_SPEC),
                java.util.Map.entry("modules/tool_axe.toml", AxeModuleConfig.MPS_AXE_MODULE_SPEC),
                java.util.Map.entry("modules/energy_generation.toml", EnergyGenerationModuleConfig.MPS_GENERATOR_MODULE_SPEC),
                java.util.Map.entry("modules/environmental.toml", EnvironmentalModuleConfig.ENVIRONMENTAL_MODULE_SPEC),
                java.util.Map.entry("modules/fluid_storage.toml", FluidStorageConfig.FLUID_STORAGE_MODULE_SPEC),
                java.util.Map.entry("modules/tool_rototiller.toml", HoeModuleConfig.MPS_HOE_MODULE_SPEC),
                java.util.Map.entry("modules/mining_enchantment.toml", MiningEnchantmentModuleConfig.MINING_ENCHANTMENT_MODULE_CONFIG_SPEC),
                java.util.Map.entry("modules/mining_enhancement.toml", MiningEnhancementModuleConfig.MINING_ENHANCEMENT_MODULE_SPEC),
                java.util.Map.entry("modules/movement.toml", MovementModuleConfig.MPS_MOVEMENGT_MODULE_SPEC),
                java.util.Map.entry("modules/tool_pickaxe.toml", PickaxeModuleConfig.MPS_PICKAXE_MODULE_SPEC),
                java.util.Map.entry("modules/tool_shovels.toml", ShovelModuleConfig.MPS_SHOVEL_MODULE_SPEC),
                java.util.Map.entry("modules/tool.toml", ToolModuleConfig.MPS_TOOL_MODULE_SPEC),
                java.util.Map.entry("modules/vision.toml", VisionModuleConfig.MPS_VISION_MODULE_SPEC),
                java.util.Map.entry("modules/weapon.toml", WeaponModuleConfig.MPS_WEAPON_MODULE_SPEC));
    }

    public static void setup(ModContainer modContainer) {
        getSpecs().forEach((path, spec) -> modContainer.registerConfig(
                getConfigType(spec), spec, getConfigFileName(path)));
    }

    public static ModConfig.Type getConfigType(ModConfigSpec spec) {
        return spec == MPSClientConfig.CLIENT_SPEC ? ModConfig.Type.CLIENT : ModConfig.Type.SERVER;
    }

    /** Relative names let NeoForge select the server file or a world's serverconfig override. */
    public static String getConfigFileName(String path) {
        return MPSConstants.MOD_ID + "/" + path;
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) {
        if (event instanceof ModConfigEvent.Unloading) {
            return;
        }
        MPSCommonConfig.onLoad(event);
        // Modules
        ArmorModuleConfig.onLoad(event);

        CosmeticModuleConfig.onLoad(event);

        EnergyGenerationModuleConfig.onLoad(event);

        EnvironmentalModuleConfig.onLoad(event);

        FluidStorageConfig.onLoad(event);

        MiningEnchantmentModuleConfig.onLoad(event);

        MiningEnhancementModuleConfig.onLoad(event);

        MovementModuleConfig.onLoad(event);

        AxeModuleConfig.onLoad(event);
        HoeModuleConfig.onLoad(event);
        PickaxeModuleConfig.onLoad(event);
        ShovelModuleConfig.onLoad(event);
        ToolModuleConfig.onLoad(event);

        VisionModuleConfig.onLoad(event);
        WeaponModuleConfig.onLoad(event);
    }
}
