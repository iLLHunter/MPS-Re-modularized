package mpsremodularized.common.constants;

import mpsremodularized.common.utils.StringUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class MPSConstants {
    // Mod ===========================================================================================================
    public static final String MOD_ID = "mpsremodularized";
    public static final String CREATIVE_TAB_TRANSLATION_KEY = MOD_ID + ".creative.tab";
    public static final String MPS_ITEM_GROUP =  "itemGroup." + MOD_ID;
    public static final String RESOURCE_PREFIX = MPSConstants.MOD_ID + ":";
    public static final String TEXTURE_PREFIX = RESOURCE_PREFIX + "textures/";

    // Generic
    public static final String BASE = "Base";
    public static final String MULTIPLIER = "Multiplier";
    public static final String OVERCLOCK_MULTIPLIER = "Overclock" + MULTIPLIER;

    /**
     * Tags -----------------------------------------------------------------------------------------------------------
     */
    // Generic
    public static final String COMPENSATION = "compensation";
    public static final String ENERGY_CONSUMPTION = "energyCon";
    public static final String HARVEST_ENERGY_CONSUMPTION = "harvestEnergyCon";
    public static final String ON_USE_ENERGY_CONSUMPTION = "onUseEnergyCon";
    public static final String ENERGY_CONSUMPTION_BASE = ENERGY_CONSUMPTION + BASE;
    public static final String ENERGY_CONSUMPTION__OVERCLOCK_MULTIPLIER = ENERGY_CONSUMPTION + OVERCLOCK_MULTIPLIER;
    public static final String JUMP_BOOST = "jumpBoost";
    public static final String JUMP_BOOST_BASE = JUMP_BOOST + BASE;
    public static final String ENERGY_CONSUMPTION_MULTIPLIER = ENERGY_CONSUMPTION + MULTIPLIER;
    public static final String POWER = "power";
    public static final String DIAMETER ="diameter";
    public static final String HARVEST_SPEED_BASE = MPSConstants.HARVEST_SPEED + BASE;
    public static final String HARVEST_SPEED_OVERCLOCK_MULTIPLIER = MPSConstants.HARVEST_SPEED + OVERCLOCK_MULTIPLIER;
    public static final String ENERGY_CONSUMPTION_POWER_BASE = ENERGY_CONSUMPTION + StringUtils.capitalize(POWER) + BASE;
    public static final String ENERGY_CONSUMPTION_POWER_MULTIPLIER = ENERGY_CONSUMPTION + StringUtils.capitalize(POWER) + MULTIPLIER;
    public static final String ENERGY_CONSUMPTION_COMPENSATION_BASE = ENERGY_CONSUMPTION + StringUtils.capitalize(COMPENSATION) + BASE;
    public static final String ENERGY_CONSUMPTION_COMPENSATION_MULTIPLIER = ENERGY_CONSUMPTION + StringUtils.capitalize(COMPENSATION) + MULTIPLIER;
    public static final String ENERGY_CONSUMPTION__DIAMETER_MULTIPLIER = ENERGY_CONSUMPTION + StringUtils.capitalize(DIAMETER) + MULTIPLIER;

    // Environmental --------------------------------------------------------------------------------------------------
    // Cooling System
    public static final String COOLING_BONUS = "coolingBonus";
    public static final String ENERGY_CONSUMPTION_COOLING_BONUS_MULTIPLIER = ENERGY_CONSUMPTION + COOLING_BONUS + MULTIPLIER;

    // Fluid Tank
    public static final String FLUID_TANK_SIZE = "fluidTankSize";
    public static final String HEAT_ACTIVATION_PERCENT = "heatActivationPercent";

    //Heat Activation Percent";
    public static final String HEAT_ACTIVATION_PERCENT_BASE = HEAT_ACTIVATION_PERCENT + BASE;
    public static final String HEAT_ACTIVATION_PERCENT_MULTIPLIER = HEAT_ACTIVATION_PERCENT + MULTIPLIER;
    public static final String ACTIVATION_PERCENT = "activationPercent";

    //"Activation Percent"

    // AutoFeeder
    public static final String TAG_FOOD = "food";
    public static final String TAG_SATURATION = "saturation";
    public static final String AUTO_FEEDER_EFFICIENCY = "autoFeederEfficiency";
    public static final String EFFICIENCY = "efficiency";

    // Energy Gemeration ----------------------------------------------------------------------------------------------
    public static final String ENERGY_GENERATION = "energyPerBlock";
    public static final String ENERGY_GENERATED = "energyGenerated";
    public static final String THERMOELECTRIC_ENERGY_GENERATION = "thermoelectricEnergyGeneration";
    public static final String STEAM_ELECTRIC_ENERGY_GENERATION = "steamEnergyGeneration";
    public static final String STEAM_ELECTRIC_WATER_CONSUMPTION = "steamWaterConsumption";
    public static final String ENERGY_GENERATION_DAY = "daytimeEnergyGen";
    public static final String ENERGY_GENERATION_DAY_BASE = ENERGY_GENERATION_DAY + BASE;
    public static final String ENERGY_GENERATION_NIGHT = "nightTimeEnergyGen";
    public static final String ENERGY_GENERATION_NIGHT_BASE = ENERGY_GENERATION_NIGHT + BASE;
    public static final String HEAT_GENERATION_DAY = "daytimeHeatGen";
    public static final String HEAT_GENERATION_DAY_BASE = HEAT_GENERATION_DAY + BASE;
    public static final String HEAT_GENERATION_NIGHT = "nightTimeHeatGen";
    public static final String HEAT_GENERATION_NIGHT_BASE = HEAT_GENERATION_NIGHT + BASE;

    // Mining Enchantments --------------------------------------------------------------------------------------------
    public static final String ENCHANTMENT_LEVEL ="enchLevel";

    // Aqua Affinity
    public static final String AQUA_HARVEST_SPEED = "aquaHarvSpeed";

    // Fortune
    public static final String FORTUNE_ENCHANTMENT_LEVEL ="fortuneLevel";
    public static final String FORTUNE_ENERGY_CONSUMPTION = "fortuneEnCon";

    // Silk Touch
    public static final String SILK_TOUCH_ENERGY_CONSUMPTION = "silkTouchEnCon";

    // Mining Enhancements --------------------------------------------------------------------------------------------
    // Selective Miner
    public static final String MINING_DIAMETER = "miningDiameter";
    public static final String SELECTIVE_MINER_LIMIT = "selMinerLimit";
    public static final String MINING_DIAMETER_MAX = MINING_DIAMETER + "Max";

    // Movement -------------------------------------------------------------------------------------------------------
    // Generic
    public static final String THRUST = "thrust";
    public static final String THRUST_MULTIPLIER = THRUST + MULTIPLIER;
    public static final String ENERGY_CONSUMPTION_THRUST_MULTIPLIER = ENERGY_CONSUMPTION + StringUtils.capitalize(THRUST) + MULTIPLIER;
    public static final String MOVEMENT_SPEED = "movement_speed";
    public static final String SWIM_SPEED = "swim_speed";

    // Blink Drive
    public static final String RANGE = "range";
    public static final String RANGE_BASE = RANGE + BASE;
    public static final String RANGE_MULTIPLIER = RANGE + MULTIPLIER;
    public static final String ENERGY_CONSUMPTION_RANGE_MULTIPLIER = ENERGY_CONSUMPTION + StringUtils.capitalize(RANGE) + MULTIPLIER;

    // Dimensional Rift
    public static final String HEAT_GENERATION = "heatGen";

    // Flight Control
    public static final String VERTICALITY = "vertically";
    public static final String FLIGHT_VERTICALITY = "yLookRatio";

    // JetBoots
    public static final String JETBOOTS_THRUST = "jetbootsThrust";

    // Jetpack
    public static final String JETPACK_THRUST = "jetpackThrust";

    // Sprint Assist
    public static final String SPRINT_ENERGY_CONSUMPTION = "sprintEnergyCon";
    public static final String SPRINT_SPEED_MULTIPLIER = "sprintSpeedMult";
    public static final String SPRINT_ASSIST = "sprintAssist";
    public static final String EXHAUSTION_COMPENSATION = "exhaustionComp";
    public static final String WALKING_ENERGY_CONSUMPTION = "walkingEnergyCon";
    public static final String WALKING_SPEED_MULTIPLIER = "walkingSpeedMult";
    public static final String WALKING_ASSISTANCE = "walkingAssist";

    // Swim Assist
    public static final String SWIM_BOOST_AMOUNT = "underwaterMovBoost";

    // Special --------------------------------------------------------------------------------------------------------
    public static final String ACTIVE_CAMOUFLAGE_ENERGY = "invisibilityEnergy";

    // Tool -----------------------------------------------------------------------------------------------------------
    // Generic
    public static final String OVERCLOCK = "overclock";
    public static final String RADIUS = "radius";
    public static final String RADIUS_MAX = "radiusMax";
    public static final String ENERGY_CONSUMPTION_RADIUS_MULTIPLIER = ENERGY_CONSUMPTION + "_" + RADIUS + "_" + MULTIPLIER;
    public static final String RADIUS_MULTIPLIER = RADIUS + "_" + MULTIPLIER;
    public static final String DISTANCE_REDUCTION = "distanceReduction";

    // Lux Capacitor
    public static final String RED = "red";
    public static final String GREEN = "green";
    public static final String BLUE = "blue";
    public static final String ALPHA = "alpha";
    public static final String RED_HUE = "redHue";
    public static final String RED_HUE_MULTIPLIER = RED_HUE + MULTIPLIER;
    public static final String BLUE_HUE = "blueHue";
    public static final String BLUE_HUE_MULTIPLIER = BLUE_HUE + MULTIPLIER;
    public static final String GREEN_HUE = "greenHue";
    public static final String GREEN_HUE_MULTIPLIER = GREEN_HUE + MULTIPLIER;
    public static final String OPACITY = "opacity";
    public static final String OPACITY_MULTIPLIER = OPACITY + MULTIPLIER;
    public static final String LUX_CAPACITOR_COOLDOWN_TIMER = "luxCapacitorCooldown";

    // Vision ---------------------------------------------------------------------------------------------------------
    public static final String FOV = "fieldOfView";
    public static final String FOV_BASE = FOV + BASE;
    public static final String FIELD_OF_VIEW = "fOVMult";
    public static final String FOV_FIELD_OF_VIEW_MULITIPLIER = FOV + FIELD_OF_VIEW + MULTIPLIER;

    // Weapons --------------------------------------------------------------------------------------------------------
    // Blade Launcher
    public static final String BLADE_DAMAGE = "spinBladeDam";

    // Lightning Summoner
    public static final String HEAT_EMISSION = "heatEmission";

    // Melee Assist
    public static final String PUNCH_DAMAGE = "meleeDamage";
    public static final String PUNCH_DAMAGE_BASE = PUNCH_DAMAGE + BASE;
    public static final String PUNCH_KNOCKBACK = "meleeKnockback";
    public static final String IMPACT = "Impact";
    public static final String CARRY_THROUGH = "carryThrough";
    public static final String ENERGY_CONSUMPTION_IMPACT_MULTIPLIER = ENERGY_CONSUMPTION + IMPACT + MULTIPLIER;
    public static final String ENERGY_CONSUMPTION_CARRY_THROUGH_MULTIPLIER = ENERGY_CONSUMPTION + CARRY_THROUGH + MULTIPLIER;
    public static final String PUNCH_KNOCKBACK_CARRY_THROUGH_MULTIPLIER = PUNCH_KNOCKBACK + CARRY_THROUGH + MULTIPLIER;
    public static final String PUNCH_DAMAGE_IMPACT_MULTIPLIER = PUNCH_DAMAGE + IMPACT + MULTIPLIER;

    // Plasma Cannon
    public static final String AMPERAGE = "amperage";
    public static final String CREEPER = "creeper";
    public static final String VOLTAGE = "voltage";
    public static final String ENERGY_PER_TICK = "energyPerTick";
    public static final String ENERGY_PER_TICK_BASE = ENERGY_PER_TICK + BASE;
    public static final String ENERGY_PER_TICK_VOLTAGE_MULTIPLIER = ENERGY_PER_TICK + VOLTAGE + MULTIPLIER;
    public static final String ENERGY_PER_TICK_AMPERAGE_MULTIPLIER = ENERGY_PER_TICK + AMPERAGE + MULTIPLIER;
    public static final String PLASMA_CANNON_DAMAGE_AT_FULL_CHARGE = "plasmaDamage";
    public static final String PLASMA_CANNON_DAMAGE_AT_FULL_CHARGE_BASE = PLASMA_CANNON_DAMAGE_AT_FULL_CHARGE + BASE;
    public static final String PLASMA_CANNON_DAMAGE_AT_FULL_CHARGE_AMPERAGE_MULTIPLIER = PLASMA_CANNON_DAMAGE_AT_FULL_CHARGE + AMPERAGE + MULTIPLIER;
    public static final String PLASMA_CANNON_EXPLOSIVENESS = "plasmaExplosiveness";
    public static final String PLASMA_CANNON_EXPLOSIVENESS_VOLTAGE_MULTIPLIER = PLASMA_CANNON_EXPLOSIVENESS + VOLTAGE + MULTIPLIER;

    // Railgun Module
    public static final String TOTAL_IMPULSE = "totalImpulse";
    public static final String TOTAL_IMPULSE_BASE = TOTAL_IMPULSE + BASE;
    public static final String ENERGY_CONSUMPTION_VOLTAGE_MULTIPLIER = ENERGY_CONSUMPTION + VOLTAGE + MULTIPLIER;
    public static final String TOTAL_IMPULSE_VOLTAGE_MULTIPLIER = TOTAL_IMPULSE + VOLTAGE + MULTIPLIER;
    public static final String HEAT_EMMISSION_BASE = HEAT_EMISSION + BASE;
    public static final String HEAT_EMMISSION_VOLTAGE_MULTIPLIER = HEAT_EMISSION + VOLTAGE + MULTIPLIER;
    public static final String RAILGUN_COOLDOWN_TIMER = "railgunCooldown";

    /**
     * AbstractContainerMenu ------------------------------------------------------------------------------------------
     */
    public static final String MPS_CRAFTING_CONTAINER_TYPE = "mps_crafting_container";
    public static final String TINKERTABLE_CONTAINER_TYPE = "tinkertable_container_type";
    public static final String INSTALL_SALVAGE_CRAFT_CONTAINER_TYPE = "install_salvage_craft_container_type";
    public static final String INSTALL_SALVAGE_CONTAINER_TYPE = "install_salvage_container_type";
    public static final String MODULE_TWEAK_CONTAINER_TYPE = "module_tweak_container_type";

    /** GUI --------------------------------------------------------------------------------------- */
    public static final String GUI_INSTALL_SALVAGE = getGUI("tab.install.salvage");
    public static final String GUI_ARMOR = getGUI("armor");
    public static final String GUI_COLOR_PREFIX = getGUI("colorPrefix");
    public static final String GUI_COMPATIBLE_MODULES = getGUI("compatible.modules");
    public static final String GUI_ENERGY_STORAGE = getGUI("energyStorage");
    public static final String GUI_EQUIPPED_TOTALS = getGUI("equippedTotals");
    public static final String GUI_INSTALL = getGUI("install");
    public static final String GUI_INSTALL_DESC = getGUI("install.desc");

    static String getGUI(String guiString) {
        return "gui." + MOD_ID + "." + guiString;
    }

    /**
     * Common Config --------------------------------------------------------------------------------------------------
     */
    public static final String CONFIG_PREFIX = "config." + MOD_ID + ".";

    // General --------------------------------------------------------------------------------------------------------
    public static final String CONFIG_PREFIX_GENERAL = CONFIG_PREFIX + "general.";
    public static final String CONFIG_GENERAL_MAX_FLYING_SPEED = CONFIG_PREFIX_GENERAL + "maxFlyingSpeed";

    // Armor ----------------------------------------------------------------------------------------------------------
    public static final String CONFIG_INVENTORY_SLOTS = "inventorySlots";

    // Modules --------------------------------------------------------------------------------------------------------
    // Armor
    public static final String ARMOR_VALUE_PHYSICAL = "armorPhysical";
    public static final String ARMOR_POINTS = "armorPoints";
    public static final String ARMOR_VALUE_ENERGY = "armorEnergy";
    public static final String ARMOR_ENERGY_CONSUMPTION = "armorEnergyPerDamage";
    public static final String KNOCKBACK_RESISTANCE = "knockbackResistance";
    public static final String ARMOR_TOUGHNESS = "armorToughness";
    public static final String MODULE_FIELD_STRENGTH = "fieldStrength";

    // Environmental
    // Auto Feeder
    public static final String USE_OLD_AUTO_FEEDER = "useOldAutoFeeder";

    /**
     * Client Config --------------------------------------------------------------------------------------------------
     */

    // HUD ----------------------------------------------------------------------------------------
    public static final String CONFIG_PREFIX_HUD = CONFIG_PREFIX + "hud.";
    public static final String CONFIG_HUD_DISPLAY_KB_HUD = CONFIG_PREFIX_HUD + "DisplayKBHUD";
    public static final String CONFIG_HUD_KEYBIND_HUD_X = CONFIG_PREFIX_HUD + "Xposition";
    public static final String CONFIG_HUD_KEYBIND_HUD_Y = CONFIG_PREFIX_HUD + "Yposition";
    public static final String CONFIG_HUD_USE_GRAPHICAL_METERS = CONFIG_PREFIX_HUD + "useGraphicalMeters";
    public static final String CONFIG_HUD_USE_24_HOUR_CLOCK = CONFIG_PREFIX_HUD + "use24HrClock";

    /**
     * Registry Names -------------------------------------------------------------------------------------------------
     */

    // Entities -------------------------------------------------------------------------------------------------------
    public static final ResourceLocation SPINNING_BLADE = getRegName("spinning_blade");
    public static final ResourceLocation PLASMA_BALL = getRegName("plasma_ball");
    public static final ResourceLocation RAILGUN_BOLT = getRegName("railgun_bolt");

    // Blocks ---------------------------------------------------------------------------------------------------------
    public static final ResourceLocation TINKER_TABLE = getRegName("tinkertable");
    public static final ResourceLocation LUX_CAPACITOR = getRegName("lux_capacitor");

    // Armor ----------------------------------------------------------------------------------------------------------
    // Helmet
    public static final ResourceLocation POWER_ARMOR_HELMET_1 = getRegName("primitive_power_armor_helmet");
    public static final ResourceLocation POWER_ARMOR_HELMET_2 = getRegName("prototype_power_armor_helmet");
    public static final ResourceLocation POWER_ARMOR_HELMET_3 = getRegName("prototype_power_armor_helmet_mkii");
    public static final ResourceLocation POWER_ARMOR_HELMET_4 = getRegName("power_armor_helmet");

    // ChestPlate
    public static final ResourceLocation POWER_ARMOR_CHESTPLATE_1 = getRegName("primitive_power_armor_chestplate");
    public static final ResourceLocation POWER_ARMOR_CHESTPLATE_2 = getRegName("prototype_power_armor_chestplate");
    public static final ResourceLocation POWER_ARMOR_CHESTPLATE_3 = getRegName("prototype_power_armor_chestplate_mkii");
    public static final ResourceLocation POWER_ARMOR_CHESTPLATE_4 = getRegName("power_armor_chestplate");

    // Leggings
    public static final ResourceLocation POWER_ARMOR_LEGGINGS_1 = getRegName("primitive_power_armor_leggings");
    public static final ResourceLocation POWER_ARMOR_LEGGINGS_2 = getRegName("prototype_power_armor_leggings");
    public static final ResourceLocation POWER_ARMOR_LEGGINGS_3 = getRegName("prototype_power_armor_leggings_mkii");
    public static final ResourceLocation POWER_ARMOR_LEGGINGS_4 = getRegName("power_armor_leggings");

    // Boots
    public static final ResourceLocation POWER_ARMOR_BOOTS_1 = getRegName("primitive_power_armor_boots");
    public static final ResourceLocation POWER_ARMOR_BOOTS_2 = getRegName("prototype_power_armor_boots");
    public static final ResourceLocation POWER_ARMOR_BOOTS_3 = getRegName("prototype_power_armor_boots_mkii");
    public static final ResourceLocation POWER_ARMOR_BOOTS_4 = getRegName("power_armor_boots");

    // HandHeld -------------------------------------------------------------------------------------------------------
    public static final ResourceLocation POWER_FIST_1 = getRegName("primitive_powerfist");
    public static final ResourceLocation POWER_FIST_2 = getRegName("prototype_powerfist");
    public static final ResourceLocation POWER_FIST_3 = getRegName("prototype_powerfist_mkii");
    public static final ResourceLocation POWER_FIST_4 = getRegName("powerfist");

    // Modules --------------------------------------------------------------------------------------------------------
    // Armor
    public static final ResourceLocation IRON_PLATING_MODULE = getRegName("plating_iron");
    public static final ResourceLocation DIAMOND_PLATING_MODULE = getRegName("plating_diamond");
    public static final ResourceLocation NETHERITE_PLATING_MODULE = getRegName("plating_netherite");
    public static final ResourceLocation ENERGY_SHIELD_MODULE = getRegName("energy_shield");

    // Cosmetic
    public static final ResourceLocation TRANSPARENT_ARMOR_MODULE = getRegName("transparent_armor");

    // Energy Generation ----------------------------------------------------------------------------------------------
    // Combustion
    public static final ResourceLocation COMBUSTION_GENERATOR_MODULE_1 = getRegName("primitive_combustion_generator");
    public static final ResourceLocation COMBUSTION_GENERATOR_MODULE_2 = getRegName("prototype_combustion_generator");
    public static final ResourceLocation COMBUSTION_GENERATOR_MODULE_3 = getRegName("prototype_combustion_generator_mkii");
    public static final ResourceLocation COMBUSTION_GENERATOR_MODULE_4 = getRegName("combustion_generator");

    // Thermal
    public static final ResourceLocation THERMAL_GENERATOR_MODULE_1 = getRegName("primitive_thermal_generator");
    public static final ResourceLocation THERMAL_GENERATOR_MODULE_2 = getRegName("prototype_thermal_generator");
    public static final ResourceLocation THERMAL_GENERATOR_MODULE_3 = getRegName("prototype_thermal_generator_mkii");
    public static final ResourceLocation THERMAL_GENERATOR_MODULE_4 = getRegName("thermal_generator");

    // Solar
    public static final ResourceLocation SOLAR_GENERATOR_MODULE_1 = getRegName("primitive_solar_generator");
    public static final ResourceLocation SOLAR_GENERATOR_MODULE_2 = getRegName("prototype_solar_generator");
    public static final ResourceLocation SOLAR_GENERATOR_MODULE_3 = getRegName("prototype_solar_generator_mkii");
    public static final ResourceLocation SOLAR_GENERATOR_MODULE_4 = getRegName("solar_generator");

    // Kinetic
    public static final ResourceLocation KINETIC_GENERATOR_MODULE_1 = getRegName("primitive_kinetic_generator");
    public static final ResourceLocation KINETIC_GENERATOR_MODULE_2 = getRegName("prototype_kinetic_generator");
    public static final ResourceLocation KINETIC_GENERATOR_MODULE_3 = getRegName("prototype_kinetic_generator_mkii");
    public static final ResourceLocation KINETIC_GENERATOR_MODULE_4 = getRegName("kinetic_generator");
    public static final ResourceLocation FISSION_REACTOR_MODULE_1 = getRegName("primitive_fission_reactor");
    public static final ResourceLocation FISSION_REACTOR_MODULE_2 = getRegName("prototype_fission_reactor");
    public static final ResourceLocation FUSION_REACTOR_MODULE_3 = getRegName("prototype_fusion_reactor_mkii");
    public static final ResourceLocation FUSION_REACTOR_MODULE_4 = getRegName("fusion_reactor");

    // Debug --------------------------------------------------------------------------------------
    public static final ResourceLocation DEBUG_MODULE = getRegName("debug_module");

    // Environmental ------------------------------------------------------------------------------
    public static final ResourceLocation ACTIVE_CAMOUFLAGE_MODULE = getRegName("active_camouflage");
    public static final ResourceLocation AUTO_FEEDER_MODULE = getRegName("auto_feeder");
    public static final ResourceLocation COOLANT_TANK_MODULE_1 = getRegName("primitive_coolant_tank");
    public static final ResourceLocation COOLANT_TANK_MODULE_2 = getRegName("prototype_coolant_tank");
    public static final ResourceLocation COOLANT_TANK_MODULE_3 = getRegName("prototype_coolant_tank_mkii");
    public static final ResourceLocation COOLANT_TANK_MODULE_4 = getRegName("coolant_tank");
    public static final ResourceLocation MAGNET_MODULE = getRegName("magnet");
    public static final ResourceLocation PIGLIN_PACIFICATION_MODULE = getRegName("piglin_pacification_module");
    public static final ResourceLocation MOB_REPULSOR_MODULE = getRegName("mob_repulsor");
    public static final ResourceLocation WATER_ELECTROLYZER_MODULE = getRegName("water_electrolyzer");

    // Mining Enhancements ------------------------------------------------------------------------
    public static final ResourceLocation VEIN_MINER_MODULE = getRegName("vein_miner");
    public static final ResourceLocation TUNNEL_BORE_MODULE = getRegName("tunnel_bore");

    // no icon
    public static final ResourceLocation SELECTIVE_MINER_MODULE = getRegName("selective_miner");

    // Mining Enchantments ------------------------------------------------------------------------
    public static final ResourceLocation AQUA_AFFINITY_MODULE = getRegName("aqua_affinity");
    public static final ResourceLocation SILK_TOUCH_MODULE = getRegName("silk_touch");
    public static final ResourceLocation FORTUNE_MODULE = getRegName("fortune");

    // Movement -----------------------------------------------------------------------------------
    public static final ResourceLocation BLINK_DRIVE_MODULE = getRegName("blink_drive");
    public static final ResourceLocation UPHILL_STEP_ASSIST_MODULE = getRegName("uphill_step_assist");
    public static final ResourceLocation DIMENSIONAL_TEAR_MODULE = getRegName("dim_tear_gen");
    public static final ResourceLocation FLIGHT_CONTROL_MODULE = getRegName("flight_control");
    public static final ResourceLocation GLIDER_MODULE = getRegName("glider");
    public static final ResourceLocation JETBOOTS_MODULE = getRegName("jet_boots");
    public static final ResourceLocation JETPACK_MODULE = getRegName("jetpack");
    public static final ResourceLocation JUMP_ASSIST_MODULE = getRegName("jump_assist");
    public static final ResourceLocation SHOCK_ABSORBER_MODULE = getRegName("shock_absorber");
    public static final ResourceLocation SPRINT_ASSIST_MODULE = getRegName("sprint_assist");
    public static final ResourceLocation SWIM_BOOST_MODULE = getRegName("swim_boost");

    // Special ------------------------------------------------------------------------------------

    // TODO?
    //    public static final ResourceLocation AE2_METEOR_COMPASS = getRegName("meteorite_compass");
    //    public static final ResourceLocation RECOVERY_COMPASS = getRegName("recovery_compass");

    // Tools --------------------------------------------------------------------------------------
    // Axe
    public static final ResourceLocation STONE_AXE_MODULE = getRegName("stone_axe");
    public static final ResourceLocation IRON_AXE_MODULE = getRegName("iron_axe");
    public static final ResourceLocation DIAMOND_AXE_MODULE = getRegName("diamond_axe");
    public static final ResourceLocation NETHERITE_AXE_MODULE = getRegName("netherite_axe");

    // Pickaxe
    public static final ResourceLocation STONE_PICKAXE_MODULE = getRegName("stone_pickaxe");
    public static final ResourceLocation IRON_PICKAXE_MODULE = getRegName("iron_pickaxe");
    public static final ResourceLocation DIAMOND_PICKAXE_MODULE = getRegName("diamond_pickaxe");
    public static final ResourceLocation NETHERITE_PICKAXE_MODULE = getRegName("netherite_pickaxe");

    // Shovel
    public static final ResourceLocation STONE_SHOVEL_MODULE = getRegName("stone_shovel");
    public static final ResourceLocation IRON_SHOVEL_MODULE = getRegName("iron_shovel");
    public static final ResourceLocation DIAMOND_SHOVEL_MODULE = getRegName("diamond_shovel");
    public static final ResourceLocation NETHERITE_SHOVEL_MODULE = getRegName("netherite_shovel");

    // Rototiller
    public static final ResourceLocation ROTOTILLER_MODULE_1 = getRegName("stone_hoe");
    public static final ResourceLocation ROTOTILLER_MODULE_2 = getRegName("iron_hoe");
    public static final ResourceLocation ROTOTILLER_MODULE_3 = getRegName("diamond_hoe");
    public static final ResourceLocation ROTOTILLER_MODULE_4 = getRegName("netherite_hoe");
    public static final ResourceLocation SHEARS_MODULE = getRegName("shears");
    public static final ResourceLocation PORTABLE_WORKBENCH_MODULE = getRegName("portable_tinkertable");
    public static final ResourceLocation FLINT_AND_STEEL_MODULE = getRegName("flint_and_steel");
    public static final ResourceLocation UNIVERSAL_WRENCH_MODULE = getRegName("universal_wrench");
    public static final ResourceLocation LEAF_BLOWER_MODULE = getRegName("leaf_blower");

    //FIXME: fix or forget
    public static final ResourceLocation LUX_CAPACITOR_MODULE = getRegName("lux_capacitor_module");
    public static final ResourceLocation PORTABLE_CRAFTING_MODULE = getRegName("portable_crafting_table");

    // Vision -------------------------------------------------------------------------------------
    public static final ResourceLocation BINOCULARS_MODULE = getRegName("binoculars");
    public static final ResourceLocation NIGHT_VISION_MODULE = getRegName("night_vision");

    // Weapons ------------------------------------------------------------------------------------
    public static final ResourceLocation BLADE_LAUNCHER_MODULE = getRegName("blade_launcher");
    public static final ResourceLocation LIGHTNING_MODULE = getRegName("lightning_summoner");
    public static final ResourceLocation MELEE_ASSIST_MODULE = getRegName("melee_assist");
    public static final ResourceLocation PLASMA_CANNON_MODULE = getRegName("plasma_cannon");
    public static final ResourceLocation RAILGUN_MODULE = getRegName("railgun");
    public static final ResourceLocation SONIC_WEAPON_MODULE = getRegName("sonic_weapon");

    // TODO?

    public static ResourceLocation getRegName(String regNameString) {
        return ResourceLocation.fromNamespaceAndPath(MPSConstants.MOD_ID, regNameString);
    }

    /**
     * Common Config ------------------------------------------------------------------------------
     */
    public static final String CONFIG_IS_ALLOWED = "isAllowed";
    public static final String CONFIG_MAX_TRANSFER = "maxEnergyTransfer";
    public static final String CONFIG_MAX_ENERGY = "maxEnergy";
    public static final String CONFIG_USE_FOV_FIX = CONFIG_PREFIX + "useFOVFix";
    public static final String CONFIG_USE_FOV_NORMALIZE = CONFIG_PREFIX + "normalizeFOV";
    public static final String CONFIG_FOV_FIX_DEAULT_STATE = CONFIG_PREFIX + "FOVFixDefaultState";
    public static final String CONFIG_USE_SOUNDS = CONFIG_PREFIX + "useSounds";
    public static final String CONFIG_DEBUGGING_INFO = CONFIG_PREFIX + "useDebuggingInfo";

    /**
     * Textures -----------------------------------------------------------------------------------
     */
    public static final String TEXTURE_PATH_PREFIX = "textures/";
    public static final ResourceLocation LOCATION_MPS_GUI_TEXTURE_ATLAS = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/atlas/gui.png");
    public static final ResourceLocation GLASS_TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, TEXTURE_PATH_PREFIX + "gui/glass.png");
    public static final ResourceLocation BLANK_ARMOR_MODEL_PATH = ResourceLocation.fromNamespaceAndPath(MOD_ID,TEXTURE_PATH_PREFIX + "item/armor/blankarmor.png");
    public static final ResourceLocation WEAPON_SLOT_BACKGROUND = ResourceLocation.fromNamespaceAndPath(MOD_ID,TEXTURE_PATH_PREFIX + "gui/weapon.png");
    public static final ResourceLocation TEXTURE_WHITE_SHORT = ResourceLocation.fromNamespaceAndPath(MPSConstants.MOD_ID, "models/white");
    public static final ResourceLocation TEXTURE_WHITE = ResourceLocation.fromNamespaceAndPath(MPSConstants.MOD_ID, "textures/models/white.png");
    public static final ResourceLocation TEXTURE_ARMOR_STAND = ResourceLocation.fromNamespaceAndPath(MPSConstants.MOD_ID, "textures/models/armorstand2.png");

    /**
     * Tag Constants ------------------------------------------------------------------------------
     */
    public static final String MODULE_TRADEOFF_PREFIX = "module.tradeoff.";
    public static final String MODULAR_ITEM_TAG = "MMModItem";

    // Machine Muse Mod
    public static final String MODULE_TAG = "MMModModule";

    // Machine Muse Mod Module
    public static final String TAG_MODULE_SETTINGS = "Module Settings";
    public static final String MODE = "Mode";

    // Energy Storage -----------------------------------------------------------------------------
    public static final String ENERGY_TAG = "energy";
    public static final String MAX_ENERGY = "maxEnergy";
    public static final String MAX_TRAMSFER = "maxTransfer";

    // Heat Storage -------------------------------------------------------------------------------
    public static final String HEAT = "heat";
    public static final String MAXIMUM_HEAT = "maxHeat";

    // Inventory ----------------------------------------------------------------------------------
    public static final String ITEMS_TAG = "Inventory";
    public static final String MODULES_TAG ="Modules";

    // Misc ---------------------------------------------------------------------------------------
    public static final String BLOCK = "block";
    public static final String HARVEST_SPEED = "harvestSpeed";
    public static final String MOVEMENT_RESISTANCE = "movementResistance";

    // Toggleable ---------------------------------------------------------------------------------
    public static final String TAG_ONLINE = "online";

    // Model Stuff --------------------------------------------------------------------------------
    public static final int FULL_BRIGHTNESS = 0XF000F0;
    public static final String RENDER_TAG = "render";
    public static final String SPECLIST = "specList";
    public static final String COSMETIC_PRESET = "cosmeticPreset";
    public static final String COLOR = "color";

    // single color, like LuxCapacitor
    public static final String COLORS = "colors";
    public static final String MODEL = "model";
    public static final String PART = "part";
    public static final String GLOW = "glow";
    public static final String COLOUR_INDEX = "colorindex";

    /**
     * Smithing Upgrades --------------------------------------------------------------------------
     */
    static final ChatFormatting DESCRIPTION_FORMAT = ChatFormatting.BLUE;

    static final ChatFormatting TITLE_FORMAT = ChatFormatting.GRAY;
    public static final ResourceLocation TIER_2_SMITHING_UPGRADE = getRegName("tier_2_smithing_upgrade");

    public static final Component TIER_2_UPGRADE = Component.translatable(Util.makeDescriptionId("upgrade", getRegName("tier_2_upgrade"))).withStyle(TITLE_FORMAT);

    public static final Component TIER_2_UPGRADE_APPLIES_TO = Component.translatable(Util.makeDescriptionId("item", getRegName("smithing_upgrade.tier_2_upgrade.applies_to"))).withStyle(DESCRIPTION_FORMAT);

    public static final Component TIER_2_UPGRADE_INGREDIENTS = Component.translatable(Util.makeDescriptionId("item", getRegName("smithing_upgrade.tier_2_upgrade.ingredients"))).withStyle(DESCRIPTION_FORMAT);

    public static final Component TIER_2_UPGRADE_BASE_SLOT_DESCRIPTION = Component.translatable(Util.makeDescriptionId("item", getRegName("smithing_upgrade.tier_2_upgrade.base_slot_description")));

    public static final Component TIER_2_UPGRADE_ADDITIONS_SLOT_DESCRIPTION = Component.translatable(Util.makeDescriptionId("item", getRegName("smithing_upgrade.tier_2_upgrade.additions_slot_description")));
    public static final ResourceLocation TIER_3_SMITHING_UPGRADE = getRegName("tier_3_smithing_upgrade");

    public static final Component TIER_3_UPGRADE = Component.translatable(Util.makeDescriptionId("upgrade", getRegName("tier_3_upgrade"))).withStyle(TITLE_FORMAT);

    public static final Component TIER_3_UPGRADE_APPLIES_TO = Component.translatable(Util.makeDescriptionId("item", getRegName("smithing_upgrade.tier_3_upgrade.applies_to"))).withStyle(DESCRIPTION_FORMAT);

    public static final Component TIER_3_UPGRADE_INGREDIENTS = Component.translatable(Util.makeDescriptionId("item", getRegName("smithing_upgrade.tier_3_upgrade.ingredients"))).withStyle(DESCRIPTION_FORMAT);

    public static final Component TIER_3_UPGRADE_BASE_SLOT_DESCRIPTION = Component.translatable(Util.makeDescriptionId("item", getRegName("smithing_upgrade.tier_3_upgrade.base_slot_description")));

    public static final Component TIER_3_UPGRADE_ADDITIONS_SLOT_DESCRIPTION = Component.translatable(Util.makeDescriptionId("item", getRegName("smithing_upgrade.tier_3_upgrade.additions_slot_description")));
    public static final ResourceLocation TIER_4_SMITHING_UPGRADE =  getRegName("tier_4_smithing_upgrade");

    public static final Component TIER_4_UPGRADE = Component.translatable(Util.makeDescriptionId("upgrade", getRegName("tier_4_upgrade"))).withStyle(TITLE_FORMAT);

    public static final Component TIER_4_UPGRADE_APPLIES_TO = Component.translatable(Util.makeDescriptionId("item", getRegName("smithing_upgrade.tier_4_upgrade.applies_to"))).withStyle(DESCRIPTION_FORMAT);

    public static final Component TIER_4_UPGRADE_INGREDIENTS = Component.translatable(Util.makeDescriptionId("item", getRegName("smithing_upgrade.tier_4_upgrade.ingredients"))).withStyle(DESCRIPTION_FORMAT);

    public static final Component TIER_4_UPGRADE_BASE_SLOT_DESCRIPTION = Component.translatable(Util.makeDescriptionId("item", getRegName("smithing_upgrade.tier_4_upgrade.base_slot_description")));

    public static final Component TIER_4_UPGRADE_ADDITIONS_SLOT_DESCRIPTION = Component.translatable(Util.makeDescriptionId("item", getRegName("smithing_upgrade.tier_4_upgrade.additions_slot_description")));

    /**
     * Registry Names -----------------------------------------------------------------------------
     */
    // Mode Changing ... (it's in the name)
    public static final ResourceLocation MODE_CHANGING_ICON_RENDERER = ResourceLocation.fromNamespaceAndPath(MOD_ID, "mode_changing_icon");

    // Overheat damage type
    public static final ResourceLocation OVERHEAT_DAMAGE_REGANAME = ResourceLocation.fromNamespaceAndPath(MOD_ID, "overheat");

    // Armor Stand
    public static final String ARMORSTAND_REGNAME = "armor_stand";
    public static final String ARMOR_STAND__ENTITY_TYPE_REGNAME = ARMORSTAND_REGNAME + "_entity";

    // Blocks
    public static final String CHARGING_BASE_REGNAME = "charging_base";

    // Batteries ----------------------------------------------------------------------------------
    public static final String MODULE_BATTERY_1__REGNAME = "primitive_battery";
    public static final String MODULE_BATTERY_2__REGNAME = "prototype_battery";
    public static final String MODULE_BATTERY_3__REGNAME = "prototype_battery_mkii";
    public static final String MODULE_BATTERY_4__REGNAME = "battery";

    // Components ---------------------------------------------------------------------------------
    public static final String COMPONENT__ARTIFICIAL_MUSCLE__REGNAME = getComponentName("artificial_muscle");
    public static final String COMPONENT__WIRING_STANDARD__REGNAME = getComponentName("wiring_standard");
    public static final String COMPONENT__WIRING_HIGH_CONDUCTIVITY__REGNAME = getComponentName("wiring_high_conductivity");
    public static final String COMPONENT__CAPACAITOR_1__REGNAME = getComponentName("primitive_capacitor");
    public static final String COMPONENT__CAPACAITOR_2__REGNAME = getComponentName("prototype_capacitor");
    public static final String COMPONENT__CAPACAITOR_3__REGNAME = getComponentName("prototype_capacitor_mkii");
    public static final String COMPONENT__CAPACAITOR_4__REGNAME = getComponentName("capacitor");
    public static final String COMPONENT__CARBON_MYOFIBER__REGNAME = getComponentName("carbon_myofiber");
    public static final String COMPONENT__COMPUTER_CHIP__REGNAME = getComponentName("computer_chip");
    public static final String COMPONENT__CONTROL_CIRCUIT_1__REGNAME = getComponentName("primitive_control_circuit");
    public static final String COMPONENT__CONTROL_CIRCUIT_2__REGNAME = getComponentName("prototype_control_circuit");
    public static final String COMPONENT__CONTROL_CIRCUIT_3__REGNAME = getComponentName("prototype_control_circuit_mkii");
    public static final String COMPONENT__CONTROL_CIRCUIT_4__REGNAME = getComponentName("control_circuit");
    public static final String COMPONENT__FORCE_FIELD_EMITTER__REGNAME = getComponentName("force_field_emitter");
    public static final String COMPONENT__GLIDER_WING__REGNAME = getComponentName("glider_wing");
    public static final String COMPONENT__ION_THRUSTER__REGNAME = getComponentName("ion_thruster");
    public static final String COMPONENT__HOLOGRAM_EMITTER__REGNAME = getComponentName("hologram_emitter");
    public static final String COMPONENT__MAGNET__REGNAME = getComponentName("magnet");
    public static final String COMPONENT__MYOFIBER_GEL__REGNAME = getComponentName("myofiber_gel");
    public static final String COMPONENT__PLATING_IRON__REGNAME = getComponentName("plating_iron");
    public static final String COMPONENT__PLATING_DIAMOND__REGNAME = getComponentName("plating_diamond");
    public static final String COMPONENT__PLATING_NETHERITE__REGNAME = getComponentName("plating_netherite");
    public static final String COMPONENT__RUBBER_HOSE__REGNAME = getComponentName("rubber_hose");
    public static final String COMPONENT__SERVO__REGNAME = getComponentName("servo");
    public static final String COMPONENT__SOLAR_PANEL__REGNAME = getComponentName("solar_panel");
    public static final String COMPONENT__SOLENOID__REGNAME = getComponentName("solenoid");

    static String getComponentName(String component) {
        return new StringBuilder("component_").append(component).toString();
    }

    /**
     *  Tooltips -----------------------------------------------------------------------------------------
     */
    public static final String TOOLTIP_MODE = getTooltip("mode");
    public static final String TOOLTIP_CHANGE_MODES = getTooltip(  "changeModes");
    public static final String TOOLTIP_INSTALLED_MODULES = getTooltip("installedModules");
    public static final String TOOLTIP_NO_MODULES = getTooltip("noModules");
    public static final String TOOLTIP_CHARGING_BASE = getTooltip(CHARGING_BASE_REGNAME);
    public static final String TOOLTIP_BATTERY_ENERGY = getTooltip("battery.energy");
    public static final String TOOLTIP_PRESS_SHIFT = getTooltip("pressShift");
    public static final String TOOLTIP_ENERGY = getTooltip("energy");

    static String getTooltip(String tooltipName) {
        return new StringBuilder("tooltip.").append(MPSConstants.MOD_ID).append(".").append(tooltipName).toString();
    }

    /**
     * GUI ------------------------------------------------------------------------------------------------------------
     */
    public static final String GUI_CREATIVE_INSTALL = getGUI("creative.install");
    public static final String GUI_CREATIVE_INSTALL_DESC = getGUI("creative.install.desc");
    public static final String GUI_CREATIVE_INSTALL_ALL = getGUI("creative.install.all");
    public static final String GUI_CREATIVE_INSTALL_ALL_SHORT = getGUI("creative.install.all.short");
    public static final String GUI_CREATIVE_INSTALL_ALL_DESC = getGUI("creative.install.all.desc");
    public static final ResourceLocation ENERGY_STORAGE_ICON = ResourceLocation.fromNamespaceAndPath(MOD_ID, "energystorage");
    public static final ResourceLocation ENERGY_GENERATION_ICON = ResourceLocation.fromNamespaceAndPath(MOD_ID, "energygeneration");
    public static final ResourceLocation FLUID_STORAGE_ICON = ResourceLocation.fromNamespaceAndPath(MOD_ID, "droplet");
}
