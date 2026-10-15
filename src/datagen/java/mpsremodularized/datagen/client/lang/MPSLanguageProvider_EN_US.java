package mpsremodularized.datagen.client.lang;

import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.registration.MPSBlocks;
import mpsremodularized.common.registration.MPSEntities;
import mpsremodularized.common.registration.MPSItems;
import mpsremodularized.common.registration.MPSConfigurations;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;

public class MPSLanguageProvider_EN_US extends AbstractEquipmentLanguageProvider {
    private boolean addModuleKeybindings;

    public MPSLanguageProvider_EN_US(PackOutput output) {
        super(output, MPSConstants.MOD_ID, "en_us");
    }

    public static Map<String, String> configTranslations() {
        Map<String, String> labels = new TreeMap<>();
        MPSConfigurations.getSpecs().values().forEach(spec -> collectConfigLabels(spec.getSpec(), labels));
        return labels;
    }

    private static void collectConfigLabels(UnmodifiableConfig config, Map<String, String> labels) {
        for (var entry : config.entrySet()) {
            if (entry.getValue() instanceof UnmodifiableConfig nested) {
                collectConfigLabels(nested, labels);
            } else if (entry.getValue() instanceof ModConfigSpec.ValueSpec value && value.getTranslationKey() != null) {
                String key = value.getTranslationKey();
                if (!key.startsWith("config." + MPSConstants.MOD_ID + ".")
                        && !key.startsWith(MPSConstants.MOD_ID + ".")) {
                    throw new IllegalStateException("External config translation: " + key);
                }
                String label = switch (entry.getKey()) {
                    case "fovFixDefaultState" -> "Enable Field of View Fix on Login";
                    case "maximumFlyingSpeedmps" -> "Maximum Flying Speed (m/s)";
                    case "keybind_HUD_on" -> "Show Keybindings on HUD";
                    case "keybindHUDx" -> "Keybinding HUD X Position";
                    case "keybindHUDy" -> "Keybinding HUD Y Position";
                    case "use24hClock" -> "Use 24 Hour Clock";
                    case "useFOVNormalize" -> "Normalize Field of View";
                    case "useFOVFix" -> "Use Field of View Fix";
                    default -> {
                        String words = entry.getKey().replaceAll("([A-Z]+)([A-Z][a-z])", "$1 $2")
                                .replaceAll("([a-z0-9])([A-Z])", "$1 $2").replace('_', ' ');
                        yield Character.toUpperCase(words.charAt(0)) + words.substring(1);
                    }
                };
                String previous = labels.putIfAbsent(key, label);
                if (previous != null && !previous.equals(label)) {
                    throw new IllegalStateException("Conflicting config label: " + key + " / " + previous + " / " + label);
                }
            }
        }
    }

    @Override
    public void add(Item item, String translation) {
        super.add(item, translation);
        if (addModuleKeybindings) {
            var registryName = BuiltInRegistries.ITEM.getKey(item);
            add("keybinding." + registryName.getNamespace() + "." + registryName.getPath(), translation);
        }
    }

    @Override
    public void addItemGroup() {
        add(MPSConstants.CREATIVE_TAB_TRANSLATION_KEY, "MPS Re-modularized");
        add("itemGroup." + MPSConstants.MOD_ID, "MPS Re-modularized");
    }

    @Override
    public void addModularItems() {
        // Helmet --------------------------------------------------------------------------------------
        add(MPSItems.POWER_ARMOR_HELMET_1.get(), "Primitive Power Armor Helmet");
        add(MPSItems.POWER_ARMOR_HELMET_2.get(), "Prototype Power Armor Helmet");
        add(MPSItems.POWER_ARMOR_HELMET_3.get(), "Prototype Power Armor Helmet Mk. II");
        add(MPSItems.POWER_ARMOR_HELMET_4.get(), "Power Armor Helmet");

        // Chestplate ----------------------------------------------------------------------------------
        add(MPSItems.POWER_ARMOR_CHESTPLATE_1.get(), "Primitive Power Armor Chestplate");
        add(MPSItems.POWER_ARMOR_CHESTPLATE_2.get(), "Prototype Power Armor Chestplate");
        add(MPSItems.POWER_ARMOR_CHESTPLATE_3.get(), "Prototype Power Armor Chestplate Mk. II");
        add(MPSItems.POWER_ARMOR_CHESTPLATE_4.get(), "Power Armor Chestplate");

        // Leggings ------------------------------------------------------------------------------------
        add(MPSItems.POWER_ARMOR_LEGGINGS_1.get(), "Primitive Power Armor Leggings");
        add(MPSItems.POWER_ARMOR_LEGGINGS_2.get(), "Prototype Power Armor Leggings");
        add(MPSItems.POWER_ARMOR_LEGGINGS_3.get(), "Prototype Power Armor Leggings Mk. II");
        add(MPSItems.POWER_ARMOR_LEGGINGS_4.get(), "Power Armor Leggings");

        // Boots ---------------------------------------------------------------------------------------
        add(MPSItems.POWER_ARMOR_BOOTS_1.get(), "Primitive Power Armor Boots");
        add(MPSItems.POWER_ARMOR_BOOTS_2.get(), "Prototype Power Armor Boots");
        add(MPSItems.POWER_ARMOR_BOOTS_3.get(), "Prototype Power Armor Boots Mk. II");
        add(MPSItems.POWER_ARMOR_BOOTS_4.get(), "Power Armor Boots");

        // Power Fist ----------------------------------------------------------------------------------
        add(MPSItems.POWER_FIST_1.get(), "Primitive Power Fist");
        add(MPSItems.POWER_FIST_2.get(), "Prototype Power Fist");
        add(MPSItems.POWER_FIST_3.get(), "Prototype Power Fist Mk. II");
        add(MPSItems.POWER_FIST_4.get(), "Power Fist");
    }

    @Override
    public void addModules() {
        add(MPSItems.SHOCK_MODULE.get(), "Shock Module");
        add(MPSItems.BLOCK_REACH_MODULE.get(), "Block Reach");
        addItemDescriptions(MPSItems.BLOCK_REACH_MODULE.get(), "Extends block reach from vanilla +1 up to 59 blocks. Successful block actions cost 1,000 FE for each started block beyond vanilla reach; shorter actions cost less.");
        add(MPSItems.SHOCK_COIL.get(), "Shock Coil");
        add("mpsremodularized.module.category.paxel", "Paxel / AIOT");
        add("mpsremodularized.module.category.construction_stick", "Construction Stick");
        for (var entry : mpsremodularized.common.compat.IntermodModules.ENTRIES) add(entry.item().get(), entry.name());
        addModuleKeybindings = true;

        // Armor =======================================================================================
        // Iron Plating --------------------------------------------------------------------------------
        add(MPSItems.IRON_PLATING_MODULE.get(), "Iron Plating");
        addItemDescriptions(MPSItems.IRON_PLATING_MODULE.get(), "Iron plating is heavy but offers a bit more protection.");

        // Diamond Plating -----------------------------------------------------------------------------
        add(MPSItems.DIAMOND_PLATING_MODULE.get(), "Diamond Plating");
        addItemDescriptions(MPSItems.DIAMOND_PLATING_MODULE.get(),"Diamond Plating is harder and more expensive to make, but offers better protection.");

        // Netherite Plating ---------------------------------------------------------------------------
        add(MPSItems.NETHERITE_PLATING_MODULE.get(), "Netherite Plating");
        addItemDescriptions(MPSItems.NETHERITE_PLATING_MODULE.get(),"Armor plating made from Netherite");

        // Energy Shield -------------------------------------------------------------------------------
        add(MPSItems.ENERGY_SHIELD_MODULE.get(), "Energy Shield");
        addItemDescriptions(MPSItems.ENERGY_SHIELD_MODULE.get(), "Energy shields are much lighter than plating, but consume energy.");

        // Cosmetic ====================================================================================
        // Transparent Armor ---------------------------------------------------------------------------
        add(MPSItems.TRANSPARENT_ARMOR_MODULE.get(), "Transparent Armor");
        addItemDescriptions(MPSItems.TRANSPARENT_ARMOR_MODULE.get(), "Make the item transparent, so you can show off your skin without losing armor.");

        // Energy Generation ===========================================================================
        // Coal Generator ------------------------------------------------------------------------------
        // TODO:
//        add(MPSItems.COAL_GENERATOR_MODULE.get(), "Coal Generator");
//
//        addItemDescriptions(MPSItems.COAL_GENERATOR_MODULE.get(), "Generate power with solid fuels");

        // Solar Generator -----------------------------------------------------------------------------
        add(MPSItems.SOLAR_GENERATOR_MODULE_1.get(), "Primitive Solar Generator");
        addItemDescriptions(MPSItems.SOLAR_GENERATOR_MODULE_1.get(), "Let the sun power your adventures.");

        add(MPSItems.SOLAR_GENERATOR_MODULE_2.get(), "Prototype Solar Generator");
        addItemDescriptions(MPSItems.SOLAR_GENERATOR_MODULE_2.get(), "Let the sun power your adventures.");

        add(MPSItems.SOLAR_GENERATOR_MODULE_3.get(), "Prototype Solar Generator Mk. II");
        addItemDescriptions(MPSItems.SOLAR_GENERATOR_MODULE_3.get(), "Let the sun power your adventures.");

        add(MPSItems.SOLAR_GENERATOR_MODULE_4.get(), "Solar Generator");
        addItemDescriptions(MPSItems.SOLAR_GENERATOR_MODULE_4.get(), "Let the sun power your adventures.");

        // Kinetic Generator ---------------------------------------------------------------------------
//        add(MPSItems.KINETIC_GENERATOR_MODULE.get(), "Kinetic Generator");
//
//        addItemDescriptions(MPSItems.KINETIC_GENERATOR_MODULE.get(), "Generate power with your movement.");

        // Thermal Generator ---------------------------------------------------------------------------
        add(MPSItems.THERMAL_GENERATOR_MODULE_1.get(), "Primitive Thermal Generator");
        addItemDescriptions(MPSItems.THERMAL_GENERATOR_MODULE_1.get(), "Generate power from extreme amounts of heat.");

        add(MPSItems.THERMAL_GENERATOR_MODULE_2.get(), "Prototype Thermal Generator");
        addItemDescriptions(MPSItems.THERMAL_GENERATOR_MODULE_2.get(), "Generate power from extreme amounts of heat.");

        add(MPSItems.THERMAL_GENERATOR_MODULE_3.get(), "Prototype Thermal Generator Mk. II");
        addItemDescriptions(MPSItems.THERMAL_GENERATOR_MODULE_3.get(), "Generate power from extreme amounts of heat.");

        add(MPSItems.THERMAL_GENERATOR_MODULE_4.get(), "Thermal Generator");
        addItemDescriptions(MPSItems.THERMAL_GENERATOR_MODULE_4.get(), "Generate power from extreme amounts of heat.");

        // Environmental ===============================================================================
        // Auto Feeder ---------------------------------------------------------------------------------
        add(MPSItems.AUTO_FEEDER_MODULE.get(), "Auto-Feeder");

        addItemDescriptions(MPSItems.AUTO_FEEDER_MODULE.get(), "Whenever you're hungry, this module will grab the bottom-left-most food item from your inventory and feed it to you, storing the rest for later.");

        // Coolant Tank --------------------------------------------------------------------------------
        add(MPSItems.COOLANT_TANK_MODULE_1.get(), "Primitive Coolant Tank");
        addItemDescriptions(MPSItems.COOLANT_TANK_MODULE_1.get(),  "Boosts output of thermal generators and provides emergency cooling");

        add(MPSItems.COOLANT_TANK_MODULE_2.get(), "Prototype Coolant Tank");
        addItemDescriptions(MPSItems.COOLANT_TANK_MODULE_2.get(),  "Boosts output of thermal generators and provides emergency cooling");

        add(MPSItems.COOLANT_TANK_MODULE_3.get(), "Prototype Coolant Tank Mk. II");
        addItemDescriptions(MPSItems.COOLANT_TANK_MODULE_3.get(),  "Boosts output of thermal generators and provides emergency cooling");

        add(MPSItems.COOLANT_TANK_MODULE_4.get(), "Coolant Tank");
        addItemDescriptions(MPSItems.COOLANT_TANK_MODULE_4.get(),  "Boosts output of thermal generators and provides emergency cooling");

        // Mob Repulsor --------------------------------------------------------------------------------
        add(MPSItems.MOB_REPULSOR_MODULE.get(), "Mob Repulsor");
        addItemDescriptions(MPSItems.MOB_REPULSOR_MODULE.get(), "Pushes mobs away from you when activated, but constantly drains power. It is highly recommended that you set this module to a keybind because of the high energy draw.");

        // Water Electrolyzer --------------------------------------------------------------------------
        add(MPSItems.WATER_ELECTROLYZER_MODULE.get(), "Water Electrolyzer");
        addItemDescriptions(MPSItems.WATER_ELECTROLYZER_MODULE.get(), "When you run out of air, this module will jolt the water around you, electrolyzing a small bubble to breathe from.");

        // Mining Enchantments =========================================================================
        // Aqua Affinity -------------------------------------------------------------------------------
        add(MPSItems.AQUA_AFFINITY_MODULE.get(), "Aqua Affinity");
        addItemDescriptions(MPSItems.AQUA_AFFINITY_MODULE.get(), "Reduces the speed penalty for using your tool underwater.");

        // Silk Touch Enchantment ----------------------------------------------------------------------
        add(MPSItems.SILK_TOUCH_MODULE.get(), "Silk Touch Enchantment");
        addItemDescriptions(MPSItems.SILK_TOUCH_MODULE.get(), "A module that provides the Silk Touch enchantment");

        // Fortune Enchantment -------------------------------------------------------------------------
        add(MPSItems.FORTUNE_MODULE.get(), "Fortune Enchantment");
        addItemDescriptions(MPSItems.FORTUNE_MODULE.get(), "A module that provides the fortune enchantment.");

        // Mining Enhancements =========================================================================
        // Vein Miner ----------------------------------------------------------------------------------
        add(MPSItems.VEIN_MINER_MODULE.get(), "Vein Miner");
        addItemDescriptions(MPSItems.VEIN_MINER_MODULE.get(), "A module for mining ore veins");

        // Tunnel Bore ---------------------------------------------------------------------------------
        add(MPSItems.TUNNEL_BORE_MODULE.get(), "Tunnel Bore");
        addItemDescriptions(MPSItems.TUNNEL_BORE_MODULE.get(), "A module that enables the pickaxe module to mine 5x5 range blocks simultaneously.");

        // Selective Miner -----------------------------------------------------------------------------
        add(MPSItems.SELECTIVE_MINER_MODULE.get(), "Selective Miner");
        addItemDescriptions(MPSItems.SELECTIVE_MINER_MODULE.get(), "Breaks blocks similar to the vein miner, but selectively. Shift and click to select block type.");

        // Movement ====================================================================================
        // Blink Drive ---------------------------------------------------------------------------------
        add(MPSItems.BLINK_DRIVE_MODULE.get(), "Blink Drive");

        addItemDescriptions(MPSItems.BLINK_DRIVE_MODULE.get(), "Select this Power Fist mode and right-click a block to teleport instantly. Increase Range in the Tinker Table to reach farther.");

        // Uphill Step Assist --------------------------------------------------------------------------
        add(MPSItems.UPHILL_STEP_ASSIST_MODULE.get(), "Uphill Step Assist");

        addItemDescriptions(MPSItems.UPHILL_STEP_ASSIST_MODULE.get(), "Artificial muscle and control electronics help you step up 1m-high ledges.");

        // Dimensional Tear Generator ------------------------------------------------------------------
        add(MPSItems.DIMENSIONAL_TEAR_MODULE.get(), "Dimensional Tear Generator");

        addItemDescriptions(MPSItems.DIMENSIONAL_TEAR_MODULE.get(), "Select this Power Fist mode and right-click to travel between the Overworld and Nether at the same X/Z coordinates. Requires a safe landing nearby.");

        // Flight Control ------------------------------------------------------------------------------
        add(MPSItems.FLIGHT_CONTROL_MODULE.get(), "Flight Control");

        addItemDescriptions(MPSItems.FLIGHT_CONTROL_MODULE.get(), "An integrated control circuit to help you fly better. Press Z to go down.");

        // Glider --------------------------------------------------------------------------------------
        add(MPSItems.GLIDER_MODULE.get(), "Glider");

        addItemDescriptions(MPSItems.GLIDER_MODULE.get(),"Tack on some wings to turn downward into forward momentum. Press sneak+forward while falling to activate.");

        // Jet Boots -----------------------------------------------------------------------------------
        add(MPSItems.JET_BOOTS_MODULE.get(), "Jet Boots");

        addItemDescriptions(MPSItems.JET_BOOTS_MODULE.get(), "Jet boots are not as strong as a jetpack, but they should at least be strong enough to counteract gravity.");

        // Jetpack -------------------------------------------------------------------------------------
        add(MPSItems.JETPACK_MODULE.get(), "Jetpack");

        addItemDescriptions(MPSItems.JETPACK_MODULE.get(), "A jetpack should allow you to jump indefinitely, or at least until you run out of power.");

        // Jump Assist ---------------------------------------------------------------------------------
        add(MPSItems.JUMP_ASSIST_MODULE.get(), "Jump Assist");

        addItemDescriptions(MPSItems.JUMP_ASSIST_MODULE.get(), "An artificial muscle actuator helps you jump higher.");



        // Shock Absorber ------------------------------------------------------------------------------
        add(MPSItems.SHOCK_ABSORBER_MODULE.get(), "Shock Absorber");

        addItemDescriptions(MPSItems.SHOCK_ABSORBER_MODULE.get(),"Artificial muscle, springs, and padding reduce a portion of fall damage.");

        // Sprint Assist -------------------------------------------------------------------------------
        add(MPSItems.SPRINT_ASSIST_MODULE.get(), "Sprint Assist");

        addItemDescriptions(MPSItems.SPRINT_ASSIST_MODULE.get(), "Artificial muscles and servo motors help you sprint (double-tap forward) and walk faster.");

        // Swim Boost ----------------------------------------------------------------------------------
        add(MPSItems.SWIM_BOOST_MODULE.get(), "Swim Boost");
        addItemDescriptions(MPSItems.SWIM_BOOST_MODULE.get(), "By refitting an ion thruster for underwater use, you may be able to add extra forward (or backward) thrust when underwater.");

        // Special =====================================================================================
        // Active Camouflage ---------------------------------------------------------------------------
        add(MPSItems.ACTIVE_CAMOUFLAGE_MODULE.get(), "Active Camouflage");
        addItemDescriptions(MPSItems.ACTIVE_CAMOUFLAGE_MODULE.get(), "Emit a hologram of your surroundings to make yourself almost imperceptible.");

        // Magnet --------------------------------------------------------------------------------------
        add(MPSItems.MAGNET_MODULE.get(), "Magnet");
        addItemDescriptions(MPSItems.MAGNET_MODULE.get(), "Generates a magnetic field strong enough to attract items towards the player.         WARNING:                   This module drains power continuously. Turn it off when not needed.");

        // Piglin Pacification Module ------------------------------------------------------------------
        add(MPSItems.PIGLIN_PACIFICATION_MODULE.get(), "Piglin Pacification Module");
        addItemDescriptions(MPSItems.PIGLIN_PACIFICATION_MODULE.get(), "Simple module to make Piglins neutral as if wearing gold armor");

        // Vision ======================================================================================
        // Binoculars ----------------------------------------------------------------------------------
        add(MPSItems.BINOCULARS_MODULE.get(), "Binoculars");
        addItemDescriptions(MPSItems.BINOCULARS_MODULE.get(), "With the problems that have been plaguing Optifine lately, you've decided to take that Zoom ability into your own hands.");

        // Night Vision --------------------------------------------------------------------------------
        add(MPSItems.NIGHTVISION_MODULE.get(), "Night Vision");
        addItemDescriptions(MPSItems.NIGHTVISION_MODULE.get(), "A pair of augmented vision goggles to help you see at night and underwater.");

        // Tools =======================================================================================
        // Axe -----------------------------------------------------------------------------------------
        // Stone
        add(MPSItems.STONE_AXE_MODULE.get(), "Stone Axe Module");
        addItemDescriptions(MPSItems.STONE_AXE_MODULE.get(), "Primitive, but gets the job done");
        // Iron
        add(MPSItems.IRON_AXE_MODULE.get(), "Iron Axe Module");
        addItemDescriptions(MPSItems.IRON_AXE_MODULE.get(), "An improvement over stone.");

        add(MPSItems.DIAMOND_AXE_MODULE.get(), "Diamond Axe Module");
        addItemDescriptions(MPSItems.DIAMOND_AXE_MODULE.get(), "An improvement over iron.");

        add(MPSItems.NETHERITE_AXE_MODULE.get(), "Netherite Axe Module");
        addItemDescriptions(MPSItems.NETHERITE_AXE_MODULE.get(), "As good as it gets.");

        // Flint and Steel -----------------------------------------------------------------------------
        add(MPSItems.FLINT_AND_STEEL_MODULE.get(), "Flint and Steel");
        addItemDescriptions(MPSItems.FLINT_AND_STEEL_MODULE.get(), "A portable igniter that creates fire through the power of energy.");

        add(MPSItems.UNIVERSAL_WRENCH_MODULE.get(), "Universal Wrench Module");
        addItemDescriptions(MPSItems.UNIVERSAL_WRENCH_MODULE.get(), "Select this Power Fist mode to rotate supported blocks and use installed mods' native wrench behavior. Sneak for the native wrench's alternate action. Requires Prototype tier or higher.");
        add(MPSItems.WRENCH_MODULE_FRAME.get(), "Wrench Module Frame");
        addItemDescriptions(MPSItems.WRENCH_MODULE_FRAME.get(), "An unpowered wrench assembly used to craft the Universal Wrench Module. Crafting component only; cannot be used as a wrench.");

        // Rototiller ----------------------------------------------------------------------------------
        add(MPSItems.STONE_ROTOTILLER_MODULE.get(), "Stone Hoe Module");
        addItemDescriptions(MPSItems.STONE_ROTOTILLER_MODULE.get(), "An automated tilling addon to make it easy to till large swaths of land at once.");

        add(MPSItems.IRON_ROTOTILLER_MODULE.get(), "Iron Hoe Module");
        addItemDescriptions(MPSItems.IRON_ROTOTILLER_MODULE.get(), "An automated tilling addon to make it easy to till large swaths of land at once.");

        add(MPSItems.DIAMOND_ROTOTILLER_MODULE.get(), "Diamond Hoe Module");
        addItemDescriptions(MPSItems.DIAMOND_ROTOTILLER_MODULE.get(), "An automated tilling addon to make it easy to till large swaths of land at once.");

        add(MPSItems.NETHERITE_ROTOTILLER_MODULE.get(), "Netherite Hoe Module");
        addItemDescriptions(MPSItems.NETHERITE_ROTOTILLER_MODULE.get(), "An automated tilling addon to make it easy to till large swaths of land at once.");

        // Leaf Blower ---------------------------------------------------------------------------------
        add(MPSItems.LEAF_BLOWER_MODULE.get(), "Leaf Blower");
        addItemDescriptions(MPSItems.LEAF_BLOWER_MODULE.get(), "Create a torrent of air to knock plants out of the ground and leaves off of trees.");

        // Lux Capacitor -------------------------------------------------------------------------------
        add(MPSItems.LUX_CAPACITOR_MODULE.get(), "Lux Capacitor");
        addItemDescriptions(MPSItems.LUX_CAPACITOR_MODULE.get(), "Launch a virtually infinite number of attractive light sources at the wall.");

        // Pickaxe -------------------------------------------------------------------------------------
        add(MPSItems.STONE_PICKAXE_MODULE.get(), "Stone Pickaxe Module");
        addItemDescriptions(MPSItems.STONE_PICKAXE_MODULE.get(), "The most basic mining module you can make.");

        add(MPSItems.IRON_PICKAXE_MODULE.get(), "Iron Pickaxe Module");
        addItemDescriptions(MPSItems.IRON_PICKAXE_MODULE.get(), "Slightly more advanced mining module.");

        add(MPSItems.DIAMOND_PICKAXE_MODULE.get(), "Diamond Pickaxe Module");
        addItemDescriptions(MPSItems.DIAMOND_PICKAXE_MODULE.get(), "Very advanced mining module.");

        add(MPSItems.NETHERITE_PICKAXE_MODULE.get(), "Netherite Pickaxe Module");
        addItemDescriptions(MPSItems.NETHERITE_PICKAXE_MODULE.get(), "Top tier mining module.");

        // Shears --------------------------------------------------------------------------------------
        add(MPSItems.SHEARS_MODULE.get(), "Shears");
        addItemDescriptions(MPSItems.SHEARS_MODULE.get(), "Cuts through leaves, wool, and creepers alike.");

        // Shovel --------------------------------------------------------------------------------------
        // Stone
        add(MPSItems.STONE_SHOVEL_MODULE.get(), "Stone Shovel Module");
        addItemDescriptions(MPSItems.STONE_SHOVEL_MODULE.get(), "Shovels are good for soft materials like dirt and sand.");

        // Iron
        add(MPSItems.IRON_SHOVEL_MODULE.get(), "Iron Shovel Module");
        addItemDescriptions(MPSItems.IRON_SHOVEL_MODULE.get(), "Shovels are good for soft materials like dirt and sand.");

        // Diamond
        add(MPSItems.DIAMOND_SHOVEL_MODULE.get(), "Diamond Shovel Module");
        addItemDescriptions(MPSItems.DIAMOND_SHOVEL_MODULE.get(), "Shovels are good for soft materials like dirt and sand.");

        // Netherite
        add(MPSItems.NETHERITE_SHOVEL_MODULE.get(), "Netherite Shovel Module");
        addItemDescriptions(MPSItems.NETHERITE_SHOVEL_MODULE.get(), "Shovels are good for soft materials like dirt and sand.");

        // Debug =======================================================================================
        // TODO

        // Weapons =====================================================================================
        // Blade Launcher ------------------------------------------------------------------------------
        add(MPSItems.BLADE_LAUNCHER_MODULE.get(), "Blade Launcher");
        addItemDescriptions(MPSItems.BLADE_LAUNCHER_MODULE.get(), "Launches a spinning blade of death (or shearing).");

        // Lightning Summoner ------------------------------------------------------------------------
        add(MPSItems.LIGHTNING_SUMMONER_MODULE.get(), "Lightning Summoner");
        addItemDescriptions(MPSItems.LIGHTNING_SUMMONER_MODULE.get(), "Allows you to summon lightning for a large energy cost.");

        // Melee Assist --------------------------------------------------------------------------------
        add(MPSItems.MELEE_ASSIST_MODULE.get(), "Melee Assist");
        addItemDescriptions(MPSItems.MELEE_ASSIST_MODULE.get(), "A much simpler addon, makes your powertool punches hit harder.");

        // Plasma Cannon -------------------------------------------------------------------------------
        add(MPSItems.PLASMA_CANNON_MODULE.get(), "Plasma Cannon");
        addItemDescriptions(MPSItems.PLASMA_CANNON_MODULE.get(), "Use electrical arcs in a containment field to superheat air to a plasma and launch it at enemies.");

        // Railgun -------------------------------------------------------------------------------------
        add(MPSItems.RAILGUN_MODULE.get(), "Railgun");
        addItemDescriptions(MPSItems.RAILGUN_MODULE.get(), "An assembly which accelerates a projectile to supersonic speeds using magnetic force. Heavy recoil.");

        addModuleKeybindings = false;
    }

    @Override
    public void addBlocks() {
        // Lux Capacitor -------------------------------------------------------------------------------
        add(MPSBlocks.LUX_CAPACITOR_BLOCK.get(), "Lux Capacitor");

        // Power Armor Tinker Table --------------------------------------------------------------------
        add(MPSBlocks.TINKER_TABLE_BLOCK.get(), "Power Armor Tinker Table");
    }

    @Override
    public void addGui() {
        add("message.mpsremodularized.blink.range", "Blink Drive: increase Range in the Tinker Table.");
        add("message.mpsremodularized.blink.target", "Blink Drive: aim at a block within range. Increase Range in the Tinker Table to reach farther.");
        add("message.mpsremodularized.blink.unsafe", "Blink Drive: there is no safe space beside that block face.");
        add("message.mpsremodularized.blink.energy", "Blink Drive: not enough energy.");
        add("message.mpsremodularized.rift.unsafe", "Dimensional Tear: no safe landing found nearby in the destination dimension.");
        add("message.mpsremodularized.rift.energy", "Dimensional Tear: not enough energy.");
        add("message.mpsremodularized.rift.dimension", "Dimensional Tear: travel is only available between the enabled Overworld and Nether.");
        add("message.mpsremodularized.rift.mounted", "Dimensional Tear: dismount and remove passengers before travelling.");
        // Armor ---------------------------------------------------------------------------------------
        add(MPSConstants.GUI_ARMOR, "Armor");

        // Color ---------------------------------------------------------------------------------------
        add(MPSConstants.GUI_COLOR_PREFIX, "Color:");

        // Compatible Modules -------------------------------------------------------------------------
        add(MPSConstants.GUI_COMPATIBLE_MODULES, "Compatible Modules");

        // Energy Storage ------------------------------------------------------------------------------
        add(MPSConstants.GUI_ENERGY_STORAGE, "Energy Storage");

        // Equipped Totals -----------------------------------------------------------------------------
        add(MPSConstants.GUI_EQUIPPED_TOTALS, "Equipped Totals");

        // Install -------------------------------------------------------------------------------------
        add(MPSConstants.GUI_INSTALL, "Install");

        // Install description -------------------------------------------------------------------------
        add(MPSConstants.GUI_INSTALL_DESC, "Installs the module in the selected item");

        // Installed Modules ---------------------------------------------------------------------------
        add("gui." + MPSConstants.MOD_ID + ".installed.modules", "Installed Modules");

        // No modular items ----------------------------------------------------------------------------
        add("gui." + MPSConstants.MOD_ID + ".line1", "No Modular Powersuit items \n found in inventory. Make some!");

        // Show on HUD ---------------------------------------------------------------------------------
        add("gui." + MPSConstants.MOD_ID + ".showOnHud", "Show on HUD:");

        // Install/Salvage -----------------------------------------------------------------------------
        add(MPSConstants.GUI_INSTALL_SALVAGE, "Install/Salvage");

        // Keybinds ------------------------------------------------------------------------------------
        add("gui." + MPSConstants.MOD_ID + ".tab.keybinds", "Keybinds");

        // Module Tweak --------------------------------------------------------------------------------
        add("gui." + MPSConstants.MOD_ID + ".tab.module.tweak", "Module Tweak");

        // Visual --------------------------------------------------------------------------------------
        add("gui." + MPSConstants.MOD_ID + ".tab.visual","Visual");

        // Tinker --------------------------------------------------------------------------------------
        add("gui." + MPSConstants.MOD_ID + ".tinker", "Tinker");

        // Modular Item Inventory ----------------------------------------------------------------------
        add("gui." + MPSConstants.MOD_ID + ".modularitem.inventory", "Modular Item Inventory");
    }

    @Override
    public void addModuleTradeoffs() {
        addTradeoff(mpsremodularized.common.item.module.environmental.ShockModule.DAMAGE_SLIDER, "Shock Damage");
        addTradeoff(mpsremodularized.common.item.module.environmental.ShockModule.RANGE, "Shock Range");
        addTradeoff(mpsremodularized.common.item.module.environmental.ShockModule.ENERGY, "Shock Energy Per Mob");
        // Activation Percent --------------------------------------------------------------------------
        addTradeoff(MPSConstants.ACTIVATION_PERCENT, "Activation Percent");

        // Alpha ---------------------------------------------------------------------------------------
        addTradeoff(MPSConstants.ALPHA, "Alpha");

        // Amperage ------------------------------------------------------------------------------------
        addTradeoff(MPSConstants.AMPERAGE, "Amperage");

        // Armor (Energy) ------------------------------------------------------------------------------
        addTradeoff(MPSConstants.ARMOR_VALUE_ENERGY, "Armor (Energy)");

        // Armor Energy Per Damage ---------------------------------------------------------------------
        addTradeoff(MPSConstants.ARMOR_ENERGY_CONSUMPTION, "Energy Per Damage");

        // Armor (Physical) ----------------------------------------------------------------------------
        addTradeoff(MPSConstants.ARMOR_VALUE_PHYSICAL, "Armor (Physical)");

        // Armor Toughness -----------------------------------------------------------------------------
        addTradeoff(MPSConstants.ARMOR_TOUGHNESS, "Toughness");

        // Auto-Feeder Efficiency ----------------------------------------------------------------------
        addTradeoff(MPSConstants.AUTO_FEEDER_EFFICIENCY, "Auto-Feeder Efficiency");

        // Block Limit ---------------------------------------------------------------------------------
        addTradeoff(MPSConstants.SELECTIVE_MINER_LIMIT, "Block Limit");

        addTradeoff(mpsremodularized.common.item.module.environmental.BlockReachModule.REACH, "Block Reach");

        // Cooling Bonus -------------------------------------------------------------------------------
        addTradeoff(MPSConstants.COOLING_BONUS, "Cooling Bonus");

        // Heat Activation Percent ---------------------------------------------------------------------
        addTradeoff(MPSConstants.HEAT_ACTIVATION_PERCENT, "Heat Activation %");


//        // Mining Diameter -----------------------------------------------------------------------------
        addTradeoff(MPSConstants.MINING_DIAMETER, "Mining Diameter");

        // Harvest Speed -------------------------------------------------------------------------------
        addTradeoff(MPSConstants.AQUA_HARVEST_SPEED, "Harvest Speed");





        // pts -----------------------------------------------------------------------------------------
        addTradeoff(MPSConstants.ARMOR_POINTS, "pts");



        // Range ---------------------------------------------------------------------------------------
//        addTradeoff(MPSConstants.BLINK_DRIVE_RANGE, "Range");

        // Blue ----------------------------------------------------------------------------------------
        addTradeoff(MPSConstants.BLUE, "Blue");

        // Lux Capacitor Blue Hue ----------------------------------------------------------------------
        addTradeoff(MPSConstants.BLUE_HUE, "Lux Capacitor Blue Hue");

        // Carry-through -------------------------------------------------------------------------------
        addTradeoff(MPSConstants.CARRY_THROUGH, "Carry-through");

        // Compensation --------------------------------------------------------------------------------
        addTradeoff(MPSConstants.COMPENSATION, "Compensation");

//        // Cooling Bonus -------------------------------------------------------------------------------
//        addTradeoff(MPSConstants.COOLING_BONUS, "Cooling Bonus");
//
//        // Daytime Energy Generation -------------------------------------------------------------------
//        addTradeoff(MPSConstants.ENERGY_GENERATION_DAY, "Daytime Energy Generation");
//
//        // Daytime Heat Generation ---------------------------------------------------------------------
//        addTradeoff(MPSConstants.HEAT_GENERATION_DAY, "Daytime Heat Generation");

        // Diameter ------------------------------------------------------------------------------------
        addTradeoff(MPSConstants.DIAMETER, "Diameter");

        // Distance Reduction --------------------------------------------------------------------------
        addTradeoff(MPSConstants.DISTANCE_REDUCTION, "Distance Reduction");

        // Efficiency ----------------------------------------------------------------------------------
        addTradeoff(MPSConstants.EFFICIENCY, "Efficiency");

        // Enchantment Level ---------------------------------------------------------------------------
        addTradeoff(MPSConstants.ENCHANTMENT_LEVEL, "Enchantment Level");

        // Energy Consumption --------------------------------------------------------------------------
        addTradeoff(MPSConstants.ENERGY_CONSUMPTION, "Energy Consumption");
//
//        // Energy Generated ----------------------------------------------------------------------------
//        addTradeoff(MPSConstants.ENERGY_GENERATED, "Energy Generated");
//
//        // Energy Per Block Per Second -----------------------------------------------------------------
//        addTradeoff(MPSConstants.ENERGY_GENERATION, "Energy Per Block Per Second");

        // FOV multiplier ------------------------------------------------------------------------------
        addTradeoff(MPSConstants.FIELD_OF_VIEW, "FOV multiplier");

        // Field of View -------------------------------------------------------------------------------
        addTradeoff(MPSConstants.FOV, "Field of View");

        // Field Strength ------------------------------------------------------------------------------
        addTradeoff(MPSConstants.MODULE_FIELD_STRENGTH, "Field Strength");

        // Tank Size -----------------------------------------------------------------------------------
//        addTradeoff(MPSConstants.FLUID_TANK_SIZE, "Tank Size");

        // Fortune Energy Consumption ------------------------------------------------------------------
        addTradeoff(MPSConstants.FORTUNE_ENERGY_CONSUMPTION, "Fortune Energy Consumption");

        // Fortune Level -------------------------------------------------------------------------------
        addTradeoff(MPSConstants.FORTUNE_ENCHANTMENT_LEVEL, "Fortune Level");

        // Green ---------------------------------------------------------------------------------------
        addTradeoff(MPSConstants.GREEN, "Green");

        // Lux Capacitor Green Hue ---------------------------------------------------------------------
        addTradeoff(MPSConstants.GREEN_HUE, "Lux Capacitor Green Hue");

        // Max Heat ------------------------------------------------------------------------------------
        addTradeoff(MPSConstants.MAXIMUM_HEAT, "Max Heat");

        // Heat Activation Percent ---------------------------------------------------------------------
//        addTradeoff(MPSConstants.HEAT_ACTIVATION_PERCENT, "Heat Activation Percent");

        // Heat Emission -------------------------------------------------------------------------------
        addTradeoff(MPSConstants.HEAT_EMISSION, "Heat Emission");

        // Heat Generation -----------------------------------------------------------------------------
        addTradeoff(MPSConstants.HEAT_GENERATION, "Heat Generation");

//        // Heat Generation -----------------------------------------------------------------------------
//        addTradeoff(heatGeneration", "Heat Generation");

        // Impact --------------------------------------------------------------------------------------
        addTradeoff(MPSConstants.IMPACT, "Impact");

        // Jump Boost ----------------------------------------------------------------------------------
        addTradeoff(MPSConstants.JUMP_BOOST, "Jump Boost");

        // Jetboots Thrust -----------------------------------------------------------------------------
        addTradeoff(MPSConstants.JETBOOTS_THRUST, "Jetboots Thrust");

        // Jetpack Thrust ------------------------------------------------------------------------------
        addTradeoff(MPSConstants.JETPACK_THRUST, "Jetpack Thrust");

        // Knockback Resistance ------------------------------------------------------------------------
        addTradeoff(MPSConstants.KNOCKBACK_RESISTANCE, "Knockback Resistance");

//        // Maximum Heat --------------------------------------------------------------------------------
//        addTradeoff(maxHeat", "Maximum Heat");

        // Melee Damage --------------------------------------------------------------------------------
        addTradeoff(MPSConstants.PUNCH_DAMAGE, "Melee Damage");

        // Melee Knockback -----------------------------------------------------------------------------
        addTradeoff(MPSConstants.PUNCH_KNOCKBACK, "Melee Knockback");

        // Movement Resistance -------------------------------------------------------------------------
//        addTradeoff(MPSConstants.MOVEMENT_RESISTANCE, "Movement Resistance");

        // Multiplier ----------------------------------------------------------------------------------
        addTradeoff(MPSConstants.MULTIPLIER, "Multiplier");

//        // Nighttime Energy Generation -----------------------------------------------------------------
//        addTradeoff(MPSConstants.ENERGY_GENERATION_NIGHT, "Nighttime Energy Generation");
//
//        // Nighttime Heat Generation -------------------------------------------------------------------
//        addTradeoff(MPSConstants.HEAT_GENERATION_NIGHT, "Nighttime Heat Generation");

        // Lux Capacitor Opacity -----------------------------------------------------------------------
        addTradeoff(MPSConstants.OPACITY, "Lux Capacitor Opacity");

        // Overclock -----------------------------------------------------------------------------------
        addTradeoff(MPSConstants.OVERCLOCK, "Overclock");

        // Plasma Damage At Full Charge ----------------------------------------------------------------
        addTradeoff(MPSConstants.PLASMA_CANNON_DAMAGE_AT_FULL_CHARGE, "Plasma Damage At Full Charge");

        // Plasma Energy Per Tick ----------------------------------------------------------------------
//        addTradeoff(MPSConstants.PLASMA_CANNON_ENERGY_PER_TICK, "Plasma Energy Per Tick");

        // Plasma Explosiveness ------------------------------------------------------------------------
        addTradeoff(MPSConstants.PLASMA_CANNON_EXPLOSIVENESS, "Plasma Explosiveness");

        // Power ---------------------------------------------------------------------------------------
        addTradeoff(MPSConstants.POWER, "Power");
        addTradeoff(MPSConstants.HARVEST_ENERGY_CONSUMPTION, "Harvest Energy");
        addTradeoff(MPSConstants.ON_USE_ENERGY_CONSUMPTION, "Use Energy");

        // Radius --------------------------------------------------------------------------------------
        addTradeoff(MPSConstants.RADIUS, "Radius");

//        // Railgun Energy Cost -------------------------------------------------------------------------
//        addTradeoff(MPSConstants.RAILGUN_ENERGY_COST, "Railgun Energy Cost");
//
//        // Railgun Heat Emission -----------------------------------------------------------------------
//        addTradeoff(MPSConstants.RAILGUN_HEAT_EMISSION, "Railgun Heat Emission");
//
        // Railgun Total Impulse -----------------------------------------------------------------------
        addTradeoff(MPSConstants.TOTAL_IMPULSE, "Total Impulse");

        // Range ---------------------------------------------------------------------------------------
        addTradeoff(MPSConstants.RANGE, "Range");

        // Red -----------------------------------------------------------------------------------------
        addTradeoff(MPSConstants.RED, "Red");

        // Lux Capacitor Red Hue -----------------------------------------------------------------------
        addTradeoff(MPSConstants.RED_HUE, "Lux Capacitor Red Hue");

        // Silk Touch Energy Consumption ---------------------------------------------------------------
        addTradeoff(MPSConstants.SILK_TOUCH_ENERGY_CONSUMPTION, "Silk Touch Energy Consumption");

        // Spinning Blade Damage -----------------------------------------------------------------------
        addTradeoff(MPSConstants.BLADE_DAMAGE, "Spinning Blade Damage");

        // Sprint Assist -------------------------------------------------------------------------------
        addTradeoff(MPSConstants.SPRINT_ASSIST, "Sprint Assist");

        // Sprint Energy Consumption -------------------------------------------------------------------
        addTradeoff(MPSConstants.SPRINT_ENERGY_CONSUMPTION, "Sprint Energy Usage");

        // Sprint Exhaustion Compensation --------------------------------------------------------------
        addTradeoff(MPSConstants.EXHAUSTION_COMPENSATION, "Exhaustion Compensation");

        // Sprint Speed Multiplier ---------------------------------------------------------------------
        addTradeoff(MPSConstants.SPRINT_SPEED_MULTIPLIER, "Sprint Speed Multiplier");

        // Steam Energy Generation ---------------------------------------------------------------------
        addTradeoff(MPSConstants.STEAM_ELECTRIC_ENERGY_GENERATION, "Steam Generation");

        // Steam Water Consumption ---------------------------------------------------------------------
        addTradeoff(MPSConstants.STEAM_ELECTRIC_WATER_CONSUMPTION, "Water Consumption");

        // Thermoelectric Energy Generation ------------------------------------------------------------
        addTradeoff(MPSConstants.THERMOELECTRIC_ENERGY_GENERATION, "Thermoelectric Generation");

//        // Energy Generation ---------------------------------------------------------------------------
//        addTradeoff(thermalEnergyGen", "Energy Generation");

        // Thrust --------------------------------------------------------------------------------------
        addTradeoff(MPSConstants.THRUST, "Thrust");

        // Underwater Movement Boost -------------------------------------------------------------------
        addTradeoff(MPSConstants.SWIM_BOOST_AMOUNT, "Swim Boost");

        // Verticality ---------------------------------------------------------------------------------
        addTradeoff(MPSConstants.VERTICALITY, "Verticality");

        // Voltage -------------------------------------------------------------------------------------
        addTradeoff(MPSConstants.VOLTAGE, "Voltage");

        // Walking Assist ------------------------------------------------------------------------------
        addTradeoff(MPSConstants.WALKING_ASSISTANCE, "Walking Assist");

        // Walking Energy Consumption ------------------------------------------------------------------
        addTradeoff(MPSConstants.WALKING_ENERGY_CONSUMPTION, "Walking Energy Usage");

        // Walking Speed Multiplier --------------------------------------------------------------------
        addTradeoff(MPSConstants.WALKING_SPEED_MULTIPLIER, "Walking Speed Multiplier");

        // Y-look ratio --------------------------------------------------------------------------------
        addTradeoff(MPSConstants.FLIGHT_VERTICALITY, "Y-look ratio");
    }

    @Override
    public void addKeybinds() {
        // External items that can be toggled through the MPS keybind system ---------------------------
        add("keybinding.minecraft.clock", "Clock");
        add("keybinding.minecraft.compass", "Compass");
        add("keybinding.minecraft.recovery_compass", "Recovery Compass");
        add("keybinding.ae2.meteorite_compass", "Meteorite Compass");

        // Cycle Tool Backward (MPS) -------------------------------------------------------------------
        add("keybinding." + MPSConstants.MOD_ID + ".cycleToolBackward", "Cycle Tool Backward (MPS)");

        // Cycle Tool Forward (MPS) --------------------------------------------------------------------
        add("keybinding." + MPSConstants.MOD_ID + ".cycleToolForward", "Cycle Tool Forward (MPS)");

        // Toggle Module -------------------------------------------------------------------------------
        add("keybinding." + MPSConstants.MOD_ID + ".toggleModule", "Toggle %s");
    }

    @Override
    public void addModels() {
        // Generic armor model parts ===================================================================
        // Head ----------------------------------------------------------------------------------------
        add("javaModel.head.Head.partName", "Head");

        // Body ----------------------------------------------------------------------------------------
        add("javaModel.chest.Body.partName", "Body");

        // Chest ---------------------------------------------------------------------------------------
        add("javaModel.chest.partName.partName","Chest");

        // Left Arm ------------------------------------------------------------------------------------
        add("javaModel.chest.LeftArm.partName", "LeftArm");

        // Right Arm -----------------------------------------------------------------------------------
        add("javaModel.chest.RightArm.partName", "RightArm");

        // Legs ----------------------------------------------------------------------------------------
        add("javaModel.legs.partName", "Legs");

        // Left Leg ------------------------------------------------------------------------------------
        add("javaModel.legs.LeftLeg.partName", "LeftLeg");

        // Right Leg -----------------------------------------------------------------------------------
        add("javaModel.legs.RightLeg.partName",  "RightLeg");

        // Feet ----------------------------------------------------------------------------------------
        add("javaModel.feet.partName", "Feet");

        // Left Foot -----------------------------------------------------------------------------------
        add("javaModel.feet.LeftFoot.partName", "LeftFoot");

        // Right Foot ----------------------------------------------------------------------------------
        add("javaModel.feet.RightFoot.partName", "RightFoot");

        // Armor skins ---------------------------------------------------------------------------------
        add("javaModel.citizenjoe_armorskin.specName", "Citizen Joe Armor Skin");

        // Default ArmorSkin ---------------------------------------------------------------------------
        add("javaModel.default_armorskin.specName", "Default Armor Skin");

        // Power Fist ==================================================================================
        add("javaModel.powerfist.specName", "Power Fist");

        // Main Arm ------------------------------------------------------------------------------------
        addPowerfistPart("mainarm", "Main Arm");

        // Armor Right ---------------------------------------------------------------------------------
        addPowerfistPart("armorright", "Armor Right");

        // Armor Left ----------------------------------------------------------------------------------
        addPowerfistPart("armorleft", "Armor Left");

        // Wrist Top Right -----------------------------------------------------------------------------
        addPowerfistPart("wristtopright", "Wrist Top Right");

        // Wrist Top Left ------------------------------------------------------------------------------
        addPowerfistPart("wristtopleft", "Wrist Top Left");

        // Wrist Bottom Right --------------------------------------------------------------------------
        addPowerfistPart("wristbottomright", "Wrist Bottom Right");

        // Wrist Bottom Left ---------------------------------------------------------------------------
        addPowerfistPart("wristbottomleft", "Wrist Bottom Left");

        // Finger Guard --------------------------------------------------------------------------------
        addPowerfistPart("fingerguard", "Finger Guard");

        // Crystal Holder ------------------------------------------------------------------------------
        addPowerfistPart("crystalholder", "Crystal Holder");

        // Crystal -------------------------------------------------------------------------------------
        addPowerfistPart("crystal", "Crystal");

        // Support Right 1 -----------------------------------------------------------------------------
        addPowerfistPart("supportright1", "Support Right 1");

        // Support Right 2 -----------------------------------------------------------------------------
        addPowerfistPart("supportright2", "Support Right 2");

        // Support Right 3 -----------------------------------------------------------------------------
        addPowerfistPart("supportright3", "Support Right 3");

        // Support Right 4 -----------------------------------------------------------------------------
        addPowerfistPart("supportright4", "Support Right 4");

        // Support Right 5 -----------------------------------------------------------------------------
        addPowerfistPart("supportright5", "Support Right 5");

        // Support Base Right --------------------------------------------------------------------------
        addPowerfistPart("supportbaseright", "Support Base Right");

        // Support Right Front -------------------------------------------------------------------------
        addPowerfistPart("supportrightfront", "Support Right Front");

        // Support Base Left ---------------------------------------------------------------------------
        addPowerfistPart("supportbaseleft", "Support Base Left");

        // Support Left Front --------------------------------------------------------------------------
        addPowerfistPart("supportleftfront", "Support Left Front");

        // Support Left 1 ------------------------------------------------------------------------------
        addPowerfistPart("supportleft1", "Support Left 1");

        // Support Left 2 ------------------------------------------------------------------------------
        addPowerfistPart("supportleft2", "Support Left 2");

        // Support Left 3 ------------------------------------------------------------------------------
        addPowerfistPart("supportleft3", "Support Left 3");

        // Support Left 4 ------------------------------------------------------------------------------
        addPowerfistPart("supportleft4", "Support Left 4");

        // Support Left 5 ------------------------------------------------------------------------------
        addPowerfistPart("supportleft5", "Support Left 5");

        // Palm ----------------------------------------------------------------------------------------
        addPowerfistPart("palm", "Palm");

        // Index 1 -------------------------------------------------------------------------------------
        addPowerfistPart("palm.index1", "Index 1");

        // Index 2 -------------------------------------------------------------------------------------
        addPowerfistPart("palm.index1.index2", "Index 2");

        // Middle Finger 1 -----------------------------------------------------------------------------
        addPowerfistPart("palm.middlefinger1", "Middle Finger 1");

        // Middle Finger 2 -----------------------------------------------------------------------------
        addPowerfistPart("palm.middlefinger1.middlefinger2", "Middle Finger 2");

        // Ring Finger 1 -------------------------------------------------------------------------------
        addPowerfistPart("palm.ringfinger1", "Ring Finger 1");

        // Ring Finger 2 -------------------------------------------------------------------------------
        addPowerfistPart("palm.ringfinger1.ringfinger2", "Ring Finger 2");

        // Pinky 1 -------------------------------------------------------------------------------------
        addPowerfistPart("palm.pinky1", "Pinky 1");

        // Pinky 2 -------------------------------------------------------------------------------------
        addPowerfistPart("palm.pinky1.pinky2", "Pinky 2");

        // Thumb 1 -------------------------------------------------------------------------------------
        addPowerfistPart("palm.thumb1", "Thumb 1");

        // Thumb 2 -------------------------------------------------------------------------------------
        addPowerfistPart("palm.thumb1.thumb2", "Thumb 2");

        // MPS Armor ===================================================================================
        // Helm ----------------------------------------------------------------------------------------
        add("model.mps_helm.modelName", "MPS Helm");

        // Helmet --------------------------------------------------------------------------------------
        add("model.mps_helm.helm_main.partName", "Helmet");

        // Tubes ---------------------------------------------------------------------------------------
        add("model.mps_helm.helm_tubes.partName", "Tubes");

        // Left Tube Entry -----------------------------------------------------------------------------
        add("model.mps_helm.helm_tube_entry1.partName", "Left Tube Entry");

        // Right Tube Entry ----------------------------------------------------------------------------
        add("model.mps_helm.helm_tube_entry2.partName", "Right Tube Entry");

        // Visor ---------------------------------------------------------------------------------------
        add("model.mps_helm.visor.partName", "Visor");

        // MPS Chestplate ------------------------------------------------------------------------------
        add("model.mps_chest.modelName", "MPS Chestplate");

        // Chest Plating -------------------------------------------------------------------------------
        add("model.mps_chest.chest_main.partName", "Chest Plating");

        // Chest Padding -------------------------------------------------------------------------------
        add("model.mps_chest.chest_padding.partName", "Chest Padding");

        // Belt Crystal --------------------------------------------------------------------------------
        add("model.mps_chest.crystal_belt.partName", "Belt Crystal");

        // Backpack ------------------------------------------------------------------------------------
        add("model.mps_chest.backpack.partName", "Backpack");

        // Belt ----------------------------------------------------------------------------------------
        add("model.mps_chest.belt.partName", "Belt");

        // Accessory ----------------------------------------------------------------------------------
        add("model.mps_chest.polySurface36.partName", "Accessory");

        // MPS Arms ------------------------------------------------------------------------------------
        add("model.mps_arms.modelName", "MPS Arms");

        // Left Arm ------------------------------------------------------------------------------------
        add("model.mps_arms.arms2.partName", "Left Arm");

        // Right Arm -----------------------------------------------------------------------------------
        add("model.mps_arms.arms3.partName", "Right Arm");

        // Left Shoulder Crystal -----------------------------------------------------------------------
        add("model.mps_arms.crystal_shoulder_1.partName", "Left Shoulder Crystal");

        // Right Shoulder Crystal ----------------------------------------------------------------------
        add("model.mps_arms.crystal_shoulder_2.partName", "Right Shoulder Crystal");

        // MPS Jetpack ---------------------------------------------------------------------------------
        add("model.jetpack.modelName", "MPS Jetpack");

        // Main ----------------------------------------------------------------------------------------
        add("model.jetpack.default.partName", "Main");

        // Secondary -----------------------------------------------------------------------------------
        add("model.jetpack.jetpack5.partName", "Secondary");

        // Lights --------------------------------------------------------------------------------------
        add("model.jetpack.jetpack_glow.partName", "Lights");

        // MPS Pantaloons ------------------------------------------------------------------------------
        add("model.mps_pantaloons.modelName", "MPS Pantaloons");

        // Right Leg -----------------------------------------------------------------------------------
        add("model.mps_pantaloons.leg1.partName", "Right Leg");

        // Left Leg ------------------------------------------------------------------------------------
        add("model.mps_pantaloons.leg2.partName", "Left Leg");

        // MPS Boots -----------------------------------------------------------------------------------
        add("model.mps_boots.modelName", "MPS Boots");

        // Right Boot ----------------------------------------------------------------------------------
        add("model.mps_boots.boots1.partName", "Right Boot");

        // Left Boot -----------------------------------------------------------------------------------
        add("model.mps_boots.boots2.partName", "Left Boot");

        // Armor 2 =====================================================================================
        add("model.armor2.modelName", "Armor 2");

        // Helmet --------------------------------------------------------------------------------------
        addModel2Part("helmetmain", "Helmet");

        // Helmet Tubes and Visor ----------------------------------------------------------------------
        addModel2Part("helmetglow1", "Helmet Tubes and Visor");

        // Front Chest Protection ----------------------------------------------------------------------
        addModel2Part("chestmain", "Front Chest Protection");

        // Chest Base ----------------------------------------------------------------------------------
        addModel2Part("chestgray", "Chest Base");

        // Back Chest Protection -----------------------------------------------------------------------
        addModel2Part("chestback1", "Back Chest Protection");

        // Spinal Power Crystals -----------------------------------------------------------------------
        addModel2Part("chestglowback", "Spinal Power Crystals");

        // Front Power Crystal -------------------------------------------------------------------------
        addModel2Part("chestglowfront", "Front Power Crystal");

        // Arm and Shoulder Protection -----------------------------------------------------------------
        addModel2Part("armmain", "Arm and Shoulder Protection");

        // Arm and Shoulder Protection 1 ---------------------------------------------------------------
        addModel2Part("armmain1", "Arm and Shoulder Protection");

        // Lef Boot Glow -------------------------------------------------------------------------------
        addModel2Part("bootglow", "Left Boot Glow");

        // Right Boot Glow -----------------------------------------------------------------------------
        addModel2Part("bootglow1", "Right Boot Glow");

        // Left Boot -----------------------------------------------------------------------------------
        addModel2Part("bootmain", "Left Boot");

        // Right Boot ----------------------------------------------------------------------------------
        addModel2Part("bootmain1", "Right Boot");

        // Shoulder Light ------------------------------------------------------------------------------
        addModel2Part("armglow", "Shoulder Light");

        // Shoulder Light 1 -----------------------------------------------------------------------------
        addModel2Part("armglow1", "Shoulder Light");

        // Arm Base ------------------------------------------------------------------------------------
        addModel2Part("armgray",  "Arm Base");

        // Arm Base 1 ----------------------------------------------------------------------------------
        addModel2Part("armgray1", "Arm Base");

        // Left Leg Base -------------------------------------------------------------------------------
        addModel2Part("leggray", "Left Leg Base");

        // Right Leg Base ------------------------------------------------------------------------------
        addModel2Part("leggray1", "Right Leg Base");

        // Left Leg Protection -------------------------------------------------------------------------
        addModel2Part("legbit", "Left Leg Protection");

        // Right Leg Protection ------------------------------------------------------------------------
        addModel2Part("legbit1", "Right Leg Protection");

        // Left Leg Light ------------------------------------------------------------------------------
        addModel2Part("legglow", "Left Leg Light");

        // Right Leg Light -----------------------------------------------------------------------------
        addModel2Part("legglow1", "Right Leg Light");
    }


    private final java.util.Map<String, String> translations = new java.util.HashMap<>();

    @Override
    public void add(String key, String value) {
        String previous = translations.putIfAbsent(key, value);
        if (previous == null) {
            super.add(key, value);
        } else if (!previous.equals(value)) {
            throw new IllegalStateException("Conflicting translation for " + key + ": " + previous + " / " + value);
        }
    }

    @Override
    protected void addTranslations() {
        super.addTranslations();
        addCoreGui();
        addCoreArmorStand();
        addCoreModuleTradeoffs();
        addCoreBlocks();
        addCoreToolTips();
        addCoreModules();
        addCoreComponents();
        addCoreItemUpgrades();
        addCoreModuleCategories();
        configTranslations().forEach(this::add);
    }

    public void addCoreGui() {
        add("gui." + MPSConstants.MOD_ID + ".charging_base", "Charging Base");
        add(MPSConstants.TOOLTIP_CHARGING_BASE, "Charges an entity's equipped items");

        // Energy --------------------------------------------------------------------------------------
        add(MPSConstants.MOD_ID + ".energy", "Energy");

        // FOV Fix Toggle ------------------------------------------------------------------------------
        add("key." + MPSConstants.MOD_ID + ".fovfixtoggle", "Toggle field of view fix");

        // FOV fix enabled -----------------------------------------------------------------------------
        add("message." + MPSConstants.MOD_ID + ".fovfixtoggle.enabled", "Field of view fix enabled");

        // FOV fix disabled ----------------------------------------------------------------------------
        add("message." + MPSConstants.MOD_ID + ".fovfixtoggle.disabled", "Field of view fix disabled");

        // Install (creative) --------------------------------------------------------------------------
        add(MPSConstants.GUI_CREATIVE_INSTALL, "Install");

        // Install (creative, description) -------------------------------------------------------------
        add(MPSConstants.GUI_CREATIVE_INSTALL_DESC, "Installs module into selected modular item while player is in creative mode");

        // Install All (creative) ----------------------------------------------------------------------
        add(MPSConstants.GUI_CREATIVE_INSTALL_ALL, "Install All");
        add(MPSConstants.GUI_CREATIVE_INSTALL_ALL_SHORT, "All");

        // Install All (creative, description) ---------------------------------------------------------
        add(MPSConstants.GUI_CREATIVE_INSTALL_ALL_DESC, "Installs compatible modules, highest tiers first, subject to available slots while in creative mode");
    }


    public void addCoreArmorStand() {
        add(MPSItems.ARMOR_STAND_ITEM.get(), "Armor Stand");
        add(MPSEntities.ARMOR_STAND__ENTITY_TYPE.get(),  "Armor Stand");
    }

    public void addCoreModuleTradeoffs() {
        add(MPSConstants.MAX_ENERGY, "Maximum Energy");

        add(MPSConstants.MAX_TRAMSFER, "Maximum Transfer per Tick");

        // Harvest Speed -------------------------------------------------------------------------------
        addTradeoff(MPSConstants.HARVEST_SPEED, "Harvest Speed");
    }

    public void addCoreBlocks() {
        add(MPSBlocks.CHARGING_BASE_BLOCK.get(), "Charging Base");
    }

    public void addCoreToolTips() {
        add(MPSConstants.TOOLTIP_BATTERY_ENERGY, "%d/%d FE");

        // Press SHIFT ---------------------------------------------------------------------------------
        add(MPSConstants.TOOLTIP_PRESS_SHIFT, "Press SHIFT for more information.");

        // Mode Change ---------------------------------------------------------------------------------
        add(MPSConstants.TOOLTIP_CHANGE_MODES, "Change modes: Press and hold the number of the hotbar slot the Power Fist is in.");

        // Energy --------------------------------------------------------------------------------------
        add(MPSConstants.TOOLTIP_ENERGY, "Energy: ");

        // No Modules Installed ------------------------------------------------------------------------
        add(MPSConstants.TOOLTIP_NO_MODULES, "No installed modules! This item is useless until you add some modules at a Tinker Table.");

        // Installed Modules: --------------------------------------------------------------------------
        add(MPSConstants.TOOLTIP_INSTALLED_MODULES, "Installed Modules:");

        // Mode: ---------------------------------------------------------------------------------------
        add(MPSConstants.TOOLTIP_MODE, "Mode: ");
    }

    public void addCoreModules() {
        // Basic Battery -------------------------------------------------------------------------------
        add(MPSItems.BATTERY_1.get(), "Primitive Battery");

        addItemDescriptions(MPSItems.BATTERY_1.get(), "Integrate a battery to allow the item to store energy.");

        // Advanced Battery ----------------------------------------------------------------------------
        add(MPSItems.BATTERY_2.get(), "Prototype Battery");

        addItemDescriptions(MPSItems.BATTERY_2.get(), "Integrate a battery to allow the item to store energy.");

        // Elite Battery -------------------------------------------------------------------------------
        add(MPSItems.BATTERY_3.get(), "Prototype Battery Mk. II");

        addItemDescriptions(MPSItems.BATTERY_3.get(), "Integrate a the most advanced battery to store an extensive amount of energy.");

        // Ultimate Battery  ---------------------------------------------------------------------------
        add(MPSItems.BATTERY_4.get(), "Battery");

        addItemDescriptions(MPSItems.BATTERY_4.get(), "Experimental high end power storage device, now with almost no chance of exploding (we think). Despite nasty rumors, it is not based on stolen alien technology.");
    }

    public void addCoreComponents() {
        // Artificial Muscle ---------------------------------------------------------------------------
        add(MPSItems.ARTIFICIAL_MUSCLE.get(), "Artificial Muscle");
        addItemDescriptions(MPSItems.ARTIFICIAL_MUSCLE.get(), "An electrical, artificial muscle, with less range of movement than human muscle but orders of magnitude more strength.");

        // Carbon Myofiber -----------------------------------------------------------------------------
        add(MPSItems.CARBON_MYOFIBER.get(), "Carbon Myofiber");
        addItemDescriptions(MPSItems.CARBON_MYOFIBER.get(), "A small bundle of carbon fibers, refined for use in artificial muscles.");

        // Capacitor -----------------------------------------------------------------------------------
        add(MPSItems.CAPACITOR_1.get(), "Primitive Capacitor");
        addItemDescriptions(MPSItems.CAPACITOR_1.get(), "A very basic capacitor.");

        add(MPSItems.CAPACITOR_2.get(), "Prototype Capacitor");
        addItemDescriptions(MPSItems.CAPACITOR_2.get(), "A slightly improved capacitor.");

        add(MPSItems.CAPACITOR_3.get(), "Prototype Capacitor Mk. II");
        addItemDescriptions(MPSItems.CAPACITOR_3.get(), "A drastically improved capacitor.");

        add(MPSItems.CAPACITOR_4.get(), "Capacitor");
        addItemDescriptions(MPSItems.CAPACITOR_4.get(), "A drastically improved, overly powerful capacitor.");

        // Computer Chip -------------------------------------------------------------------------------
        add(MPSItems.COMPUTER_CHIP.get(), "Computer Chip");
        addItemDescriptions(MPSItems.COMPUTER_CHIP.get(), "An upgraded control circuit that contains a CPU which is capable of more advanced calculations.");

        // Control Circuit -----------------------------------------------------------------------------
        add(MPSItems.CONTROL_CIRCUIT_1.get(),  "Primitive Control Circuit");
        addItemDescriptions(MPSItems.CONTROL_CIRCUIT_1.get(), "Are you sure this thing will actually work? Is it supposed to smoke like that?");

        add(MPSItems.CONTROL_CIRCUIT_2.get(),  "Prototype Control Circuit");
        addItemDescriptions(MPSItems.CONTROL_CIRCUIT_2.get(), "Oh, look. It kind of works now.");

        add(MPSItems.CONTROL_CIRCUIT_3.get(),  "Prototype Control Circuit Mk. II");
        addItemDescriptions(MPSItems.CONTROL_CIRCUIT_3.get(), "A simple networkable microcontroller for coordinating an individual component.");

        add(MPSItems.CONTROL_CIRCUIT_4.get(),  "Control Circuit");
        addItemDescriptions(MPSItems.CONTROL_CIRCUIT_4.get(),  "A highly advanced networkable microcontroller.");

        // Force Field Emitter -------------------------------------------------------------------------
        add(MPSItems.FORCE_FIELD_EMITTER.get(), "Force Field Emitter");
        addItemDescriptions(MPSItems.FORCE_FIELD_EMITTER.get(), "An advanced device which directly manipulates electromagnetic and gravitational fields in an area.");

        // Glider Wing ---------------------------------------------------------------------------------
        add(MPSItems.GLIDER_WING.get(), "Glider Wing");
        addItemDescriptions(MPSItems.GLIDER_WING.get(), "A lightweight aerodynamic wing with an artificial muscle and electromagnet for deployment and retraction.");

        // Ion Thruster --------------------------------------------------------------------------------
        add(MPSItems.ION_THRUSTER.get(), "Ion Thruster");
        addItemDescriptions(MPSItems.ION_THRUSTER.get(), "Essentially a miniature particle accelerator. Accelerates ions to near-light speed to produce thrust.");

        // Hologram Emitter ----------------------------------------------------------------------------
        add(MPSItems.HOLOGRAM_EMITTER.get(), "Hologram Emitter");
        addItemDescriptions(MPSItems.HOLOGRAM_EMITTER.get(), "A multicolored laser array which can cheaply alter the appearance of something.");

        // Magnet --------------------------------------------------------------------------------------
        add(MPSItems.MAGNET.get(), "Magnet");
        addItemDescriptions(MPSItems.MAGNET.get(), "A metallic device that generates a magnetic field which pulls items towards the player.");

        // Myofiber Gel --------------------------------------------------------------------------------
        add(MPSItems.MYOFIBER_GEL.get(), "Myofiber Gel");
        addItemDescriptions(MPSItems.MYOFIBER_GEL.get(), "A thick, conductive paste, perfect for fitting between myofibers in an artificial muscle.");

        // Plating -------------------------------------------------------------------------------------
        add(MPSItems.PLATING_IRON.get(), "Iron Plate");
        addItemDescriptions(MPSItems.PLATING_IRON.get(), "Structural crafting component made of iron and used to create some modules.");

        add(MPSItems.PLATING_DIAMOND.get(), "Diamond Plate");
        addItemDescriptions(MPSItems.PLATING_DIAMOND.get(), "Structural crafting component made of diamond and used to create some modules.");

        add(MPSItems.PLATING_NETHERITE.get(), "Netherite Plate");
        addItemDescriptions(MPSItems.PLATING_NETHERITE.get(), "Structural crafting component made of netherite and used to create some modules.");


        // Rubber Hose ---------------------------------------------------------------------------------
        add(MPSItems.RUBBER_HOSE.get(), "Insulated Rubber Hose");
        addItemDescriptions(MPSItems.RUBBER_HOSE.get(), "A heavily insulated rubber hose capable of withstanding extreme heat or cold");

        // Servo Motor ---------------------------------------------------------------------------------
        add(MPSItems.SERVO.get(), "Servo Motor");
        addItemDescriptions(MPSItems.SERVO.get(), "A special type of motor which uses a pulse-modulated signal to enact very precise movements.");

        // Solar Panel ---------------------------------------------------------------------------------
        add(MPSItems.SOLAR_PANEL.get(), "Solar Panel");
        addItemDescriptions(MPSItems.SOLAR_PANEL.get(), "A light sensitive device that will generate electricity from the sun.");

        // Solenoid ------------------------------------------------------------------------------------
        add(MPSItems.SOLENOID.get(), "Solenoid");
        addItemDescriptions(MPSItems.SOLENOID.get(), "Wires wound around a ferromagnetic core produces a basic electromagnet.");

        // Wiring --------------------------------------------------------------------------------------
        add(MPSItems.WIRING_STANDARD.get(), "Wiring");
        addItemDescriptions(MPSItems.WIRING_STANDARD.get(), "A special type of wiring with high voltaic capacity and precision, necessary for the sensitive electronics in power armor.");
        add(MPSItems.WIRING_HIGH_CONDUCTIVITY.get(), "High-conductivity Wiring");
        addItemDescriptions(MPSItems.WIRING_HIGH_CONDUCTIVITY.get(), "Low-resistance wiring for advanced components. Made with electrum when available, otherwise gold.");
        add(MPSItems.COMPONENT_MOLD.get(), "MPS Component Mold");
        addItemDescriptions(MPSItems.COMPONENT_MOLD.get(), "A reusable Immersive Engineering Metal Press mold for MPS plating, carbon myofiber and component templates.");
        add(MPSItems.WIRING_TEMPLATE.get(), "Wiring Template");
        add(MPSItems.HIGH_CONDUCTIVITY_WIRING_TEMPLATE.get(), "High Conductivity Wiring Template");
        add(MPSItems.SOLENOID_TEMPLATE.get(), "Solenoid Template");
        for (var item : java.util.List.of(MPSItems.WIRING_TEMPLATE.get(), MPSItems.HIGH_CONDUCTIVITY_WIRING_TEMPLATE.get(), MPSItems.SOLENOID_TEMPLATE.get()))
            addItemDescriptions(item, "Consumed by the Metal Press with an MPS Component Mold installed. Contains the full crafting materials.");
        add(MPSItems.UNFINISHED_HOLOGRAM_EMITTER.get(), "Unfinished Hologram Emitter");
        add(MPSItems.UNFINISHED_PLASMA_CANNON.get(), "Unfinished Plasma Cannon");
        add(MPSItems.UNFINISHED_RAILGUN.get(), "Unfinished Railgun");
        for (var item : java.util.List.of(MPSItems.UNFINISHED_HOLOGRAM_EMITTER.get(), MPSItems.UNFINISHED_PLASMA_CANNON.get(), MPSItems.UNFINISHED_RAILGUN.get()))
            addItemDescriptions(item, "Finish in PneumaticCraft's Assembly Laser. Cannot be installed until processing is complete.");
        add(MPSItems.PRIMITIVE_HELMET_FRAME.get(), "Primitive Power Armor Helmet Frame");
        add(MPSItems.PRIMITIVE_CHESTPLATE_FRAME.get(), "Primitive Power Armor Chestplate Frame");
        add(MPSItems.PRIMITIVE_LEGGINGS_FRAME.get(), "Primitive Power Armor Leggings Frame");
        add(MPSItems.PRIMITIVE_BOOTS_FRAME.get(), "Primitive Power Armor Boots Frame");
        add(MPSItems.PRIMITIVE_POWERFIST_FRAME.get(), "Primitive Power Fist Frame");
        for (var item : java.util.List.of(MPSItems.PRIMITIVE_HELMET_FRAME.get(), MPSItems.PRIMITIVE_CHESTPLATE_FRAME.get(), MPSItems.PRIMITIVE_LEGGINGS_FRAME.get(), MPSItems.PRIMITIVE_BOOTS_FRAME.get(), MPSItems.PRIMITIVE_POWERFIST_FRAME.get()))
            addItemDescriptions(item, "Assemble with a Primitive Control Circuit in Modern Industrialization's Assembler. An unfinished component, not usable equipment.");
    }

    public void addCoreItemUpgrades() {
        for (int tier = 2; tier <= 4; tier++) {
            String prefix = "item.mpsremodularized.smithing_upgrade.tier_" + tier + "_upgrade.";
            add("upgrade.mpsremodularized.tier_" + tier + "_upgrade", "Tier " + tier + " Upgrade");
            add(prefix + "applies_to", "Tier " + (tier - 1) + " equipment and upgradeable modules");
            add(prefix + "ingredients", tier == 2 ? "Iron or gold ingots (see recipe)" : tier == 3 ? "Diamonds (see recipe)" : "Netherite ingots (see recipe)");
            add(prefix + "base_slot_description", "Add tier " + (tier - 1) + " equipment or an upgradeable module");
            add(prefix + "additions_slot_description", "Add the materials and count shown in the recipe");
        }
        add(MPSItems.TIER_2_SMITHING_UPGRADE.get(), "Tier 2 Smithing Upgrade");
        addItemDescriptions(MPSItems.TIER_2_SMITHING_UPGRADE.get(), "Used for upgrading some modules and modular items from tier 1 to tier 2");

        add(MPSItems.TIER_3_SMITHING_UPGRADE.get(), "Tier 3 Smithing Upgrade");
        addItemDescriptions(MPSItems.TIER_3_SMITHING_UPGRADE.get(), "Used for upgrading some modules and modular items from tier 2 to tier 3");

        add(MPSItems.TIER_4_SMITHING_UPGRADE.get(), "Tier 4 Smithing Upgrade");
        addItemDescriptions(MPSItems.TIER_4_SMITHING_UPGRADE.get(), "Used for upgrading some modules and modular items from tier 3 to tier 4");
    }

    public void addCoreModuleCategories() {
        // None ----------------------------------------------------------------------------------------
        add(ModuleCategory.NONE.getTranslationKey(), "None");

        // Debug Modules -------------------------------------------------------------------------------
        add(ModuleCategory.DEBUG.getTranslationKey(), "Debug Modules");

        // Armor Modules -------------------------------------------------------------------------------
        add(ModuleCategory.ARMOR.getTranslationKey(), "Armor Modules");

        // Energy Storage ------------------------------------------------------------------------------
        add(ModuleCategory.ENERGY_STORAGE.getTranslationKey(), "Energy Storage");

        // Energy Generation ---------------------------------------------------------------------------
        add(ModuleCategory.ENERGY_GENERATION.getTranslationKey(), "Energy Generation");

        // Fluid Storage -------------------------------------------------------------------------------
        add(ModuleCategory.FLUID_STORAGE.getTranslationKey(), "Fluid Storage");

        // Tool ----------------------------------------------------------------------------------------
        add(ModuleCategory.TOOL.getTranslationKey(), "Tool");

        add(ModuleCategory.AXE.getTranslationKey(), "Axe");

        add(ModuleCategory.HOE.getTranslationKey(), "Hoe");

        add(ModuleCategory.PICKAXE.getTranslationKey(), "Pickaxe");

        add(ModuleCategory.SHOVEL.getTranslationKey(), "Shovel");

        // Weapon --------------------------------------------------------------------------------------
        add(ModuleCategory.WEAPON.getTranslationKey(), "Weapon");

        // Movement ------------------------------------------------------------------------------------
        add(ModuleCategory.MOVEMENT.getTranslationKey(), "Movement");

        // Cosmetic ------------------------------------------------------------------------------------
        add(ModuleCategory.COSMETIC.getTranslationKey(), "Cosmetic");

        // Vision --------------------------------------------------------------------------------------
        add(ModuleCategory.VISION.getTranslationKey(), "Vision");

        // Environment ---------------------------------------------------------------------------------
        add(ModuleCategory.ENVIRONMENTAL.getTranslationKey(), "Environment");

        // Special -------------------------------------------------------------------------------------
        add(ModuleCategory.SPECIAL.getTranslationKey(), "Special");

        // Mining Enhancement --------------------------------------------------------------------------
        add(ModuleCategory.MINING_ENHANCEMENT.getTranslationKey(),  "Mining Enhancement");

        // Mining Enchantment --------------------------------------------------------------------------
        add(ModuleCategory.MINING_ENCHANTMENT.getTranslationKey(), "Mining Enchantment");
    }

}
