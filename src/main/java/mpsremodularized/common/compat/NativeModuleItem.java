package mpsremodularized.common.compat;

import mpsremodularized.common.config.MPSCommonConfig;
import mpsremodularized.common.item.module.AbstractPowerModule;
import mpsremodularized.common.registration.MPSDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Unbreakable;
import net.neoforged.fml.ModList;

/** A crafted envelope, never a substitute item or a newly constructed donor. */
public final class NativeModuleItem extends AbstractPowerModule {
    public enum Kind { GAS_MASK, GRENADE_LAUNCHER, PAXEL, CONSTRUCTION_STICK, TOOL, STATION, HELMET_UNIT, ARMOR_HEAD, ARMOR_CHEST, ARMOR_LEGS, ARMOR_FEET }

    public static net.minecraft.world.entity.EquipmentSlot armorSlot(Kind kind) {
        return switch (kind) {
            case ARMOR_HEAD -> net.minecraft.world.entity.EquipmentSlot.HEAD;
            case ARMOR_CHEST -> net.minecraft.world.entity.EquipmentSlot.CHEST;
            case ARMOR_LEGS -> net.minecraft.world.entity.EquipmentSlot.LEGS;
            case ARMOR_FEET -> net.minecraft.world.entity.EquipmentSlot.FEET;
            default -> null;
        };
    }
    public final ResourceLocation donor;
    public final CompatibleMod provider;
    public final Kind kind;

    public NativeModuleItem(String donor, CompatibleMod provider, Kind kind) {
        this.donor = ResourceLocation.parse(donor);
        this.provider = provider;
        this.kind = kind;
    }

    public boolean available() {
        return (provider == null || ModList.get().isLoaded(provider.modId))
                && BuiltInRegistries.ITEM.containsKey(donor)
                && (kind == Kind.GAS_MASK || !fitsCurios(new ItemStack(BuiltInRegistries.ITEM.get(donor))))
                && (provider == null || !MPSCommonConfig.SERVER_SPEC.isLoaded()
                || MPSCommonConfig.COMPATIBILITY_SETTINGS.enabledMods().contains(provider));
    }

    public boolean acceptsSource(ItemStack source) {
        return kind != Kind.CONSTRUCTION_STICK || ConstructionStickIntegration.hasBattery(source);
    }

    /** Creative installation uses a real default donor, with the required stick augment. */
    public ItemStack creativeModule(net.minecraft.core.HolderLookup.Provider registries) {
        if (!available()) return ItemStack.EMPTY;
        ItemStack source = new ItemStack(BuiltInRegistries.ITEM.get(donor));
        if (kind == Kind.CONSTRUCTION_STICK) {
            var battery = ConstructionStickIntegration.batteryComponent();
            if (battery == null) return ItemStack.EMPTY;
            source.set(battery, true);
        }
        ItemStack module = new ItemStack(this);
        store(module, source, registries);
        return module;
    }

    /** Ask Curios' effective slot predicates, including indirect tags and other providers. */
    private static boolean fitsCurios(ItemStack donor) {
        if (!ModList.get().isLoaded("curios")) return false;
        try {
            return !((java.util.Map<?, ?>) Class.forName("top.theillusivec4.curios.api.CuriosApi")
                    .getMethod("getItemStackSlots", ItemStack.class, boolean.class).invoke(null, donor, false)).isEmpty();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unsupported Curios slot predicate API", exception);
        }
    }

    public ItemStack source(ItemStack module) {
        var registries = registries();
        return registries == null ? ItemStack.EMPTY : source(module, registries);
    }

    public ItemStack source(ItemStack module, net.minecraft.core.HolderLookup.Provider registries) {
        var payload = module.get(MPSDataComponents.NATIVE_ITEM);
        if (payload == null || (provider != null && !ModList.get().isLoaded(provider.modId)) || !BuiltInRegistries.ITEM.containsKey(donor)) return ItemStack.EMPTY;
        // Decode only when usable. Failed or unavailable native codecs never rewrite the raw payload.
        ItemStack source = ItemStack.parse(registries, payload).orElse(ItemStack.EMPTY);
        boolean wornAnvil = donor.toString().equals("minecraft:anvil")
                && (source.is(net.minecraft.world.item.Items.CHIPPED_ANVIL) || source.is(net.minecraft.world.item.Items.DAMAGED_ANVIL));
        boolean wrenchSelection = donor.toString().equals("rftoolsbase:smartwrench")
                && BuiltInRegistries.ITEM.getKey(source.getItem()).toString().equals("rftoolsbase:smartwrench_select");
        return !source.isEmpty() && (BuiltInRegistries.ITEM.getKey(source.getItem()).equals(donor) || wornAnvil || wrenchSelection) ? source : ItemStack.EMPTY;
    }

    private static net.minecraft.core.HolderLookup.Provider registries() {
        var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server != null) return server.registryAccess();
        return net.neoforged.fml.loading.FMLEnvironment.dist == net.neoforged.api.distmarker.Dist.CLIENT
                ? mpsremodularized.client.compat.NativeModuleRegistries.current() : null;
    }

    @Override public boolean isEnchantable(ItemStack stack) { return false; }
    @Override public boolean isBookEnchantable(ItemStack stack, ItemStack book) { return false; }

    public void store(ItemStack module, ItemStack source) {
        var registries = registries();
        if (registries == null) throw new IllegalStateException("Native module state requires registry access");
        store(module, source, registries);
    }

    public void store(ItemStack module, ItemStack source, net.minecraft.core.HolderLookup.Provider registries) {
        // Approved rule: converted tools and armor have no durability or disassembly.
        // Keep pre-existing damage, enchantments, upgrades and every other donor component.
        if (!source.isEmpty() && (kind == Kind.PAXEL || kind == Kind.TOOL || armorSlot(kind) != null) && !source.has(DataComponents.UNBREAKABLE)) {
            source.set(DataComponents.UNBREAKABLE, new Unbreakable(false));
        }
        module.set(MPSDataComponents.NATIVE_ITEM, (net.minecraft.nbt.CompoundTag) source.saveOptional(registries));
        if (!module.has(MPSDataComponents.NATIVE_ID)) module.set(MPSDataComponents.NATIVE_ID, java.util.UUID.randomUUID().toString());
    }
}
