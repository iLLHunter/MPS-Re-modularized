package mpsremodularized.common.compat;

/** Optional integrations never reference another mod's Java classes. */
public enum CompatibleMod {
    GASCRAFT("gascraft", "useGascraft", "Gascraft"),
    MEKANISM_TOOLS("mekanismtools", "useMekanismTools", "Mekanism Tools"),
    MYSTICAL_AGRADDITIONS("mysticalagradditions", "useMysticalAgradditions", "Mystical Agradditions"),
    ACTUALLY_ADDITIONS("actuallyadditions", "useActuallyAdditions", "Actually Additions"),
    FLUX_NETWORKS("fluxnetworks", "useFluxNetworks", "Flux Networks"),
    NATURES_COMPASS("naturescompass", "useNaturesCompass", "Nature's Compass"),
    EXPLORERS_COMPASS("explorerscompass", "useExplorersCompass", "Explorer's Compass"),
    CYCLIC("cyclic", "useCyclic", "Cyclic"),
    CONSTRUCTION_STICKS("constructionstick", "useConstructionSticks", "Construction Sticks"),
    SECRET_ROOMS("secretroomsmod", "useSecretRooms", "Secret Rooms: Reborn"),
    AURORAL("auroral", "useAuroral", "Auroral"),
    IRONS_SPELLBOOKS("irons_spellbooks", "useIronsSpellbooks", "Iron's Spells 'n Spellbooks"),
    FORBIDDEN_ARCANUS("forbidden_arcanus", "useForbiddenArcanus", "Forbidden and Arcanus"),
    MALUM("malum", "useMalum", "Malum"),
    MEKANISM_LASERS("mekanism_lasers", "useMekanismLasers", "Mekanism Lasers"),
    CREATE("create", "useCreate", "Create"),
    MEKANISM("mekanism", "useMekanism", "Mekanism"),
    AE2("ae2", "useAE2", "Applied Energistics 2"),
    MODERN_INDUSTRIALIZATION("modern_industrialization", "useModernIndustrialization", "Modern Industrialization"),
    PNEUMATICCRAFT("pneumaticcraft", "usePneumaticCraft", "PneumaticCraft"),
    ORITECH("oritech", "useOritech", "Oritech"),
    ENDER_IO("enderio", "useEnderIO", "Ender IO"),
    ENDER_IO_EVOLUTION("enderio_evolution", "useEnderIOEvolution", "Ender IO: Evolution"),
    POWAH("powah", "usePowah", "Powah"),
    SOLAR_FLUX("solarflux", "useSolarFlux", "Solar Flux Reborn"),
    INDUSTRIAL_FOREGOING("industrialforegoing", "useIndustrialForegoing", "Industrial Foregoing"),
    IMMERSIVE_ENGINEERING("immersiveengineering", "useImmersiveEngineering", "Immersive Engineering"),
    ARS_ADDITIONS("ars_additions", "useArsAdditions", "Ars Additions"),
    ADVANCED_AE("advanced_ae", "useAdvancedAE", "AdvancedAE"),
    MFFS("mffs", "useMFFS", "Modular Force Field System"),
    RFTOOLS_BASE("rftoolsbase", "useRFToolsBase", "RFTools Base"),
    MYSTICAL_AGRICULTURE("mysticalagriculture", "useMysticalAgriculture", "Mystical Agriculture");

    public final String modId;
    public final String configKey;
    public final String displayName;

    CompatibleMod(String modId, String configKey, String displayName) {
        this.modId = modId;
        this.configKey = configKey;
        this.displayName = displayName;
    }
}
