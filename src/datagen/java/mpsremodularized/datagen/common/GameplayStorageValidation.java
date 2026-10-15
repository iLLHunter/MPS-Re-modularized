package mpsremodularized.datagen.common;

import mpsremodularized.common.blockentity.ChargingBaseBlockEntity;
import mpsremodularized.common.config.MPSCommonConfig;
import mpsremodularized.common.recipe.ShapedEnergyRecipe;
import mpsremodularized.common.registration.MPSBlocks;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.common.registration.MPSDataComponents;
import mpsremodularized.common.registration.MPSItems;
import mpsremodularized.common.utils.ElectricItemUtils;
import mpsremodularized.common.utils.TagUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.EnergyStorage;

import java.util.List;
import java.util.Map;

/** Uses actual registered items, recipe assembly, capabilities and persistent codecs after bootstrap. */
final class GameplayStorageValidation {
    private static int checks;

    static void validate(HolderLookup.Provider registries) {
        checks = 0;
        registerCapabilitiesForValidation();
        verifyBatteryCrafting(registries);
        verifyEquipmentPersistence(registries);
        verifyChargingBasePersistence(registries);
        System.out.println("Gameplay storage and crafting checks passed: " + checks);
    }

    private static void registerCapabilitiesForValidation() {
        // Datagen skips normal capability registration. Invoke the real MPS registrar only here.
        try {
            var constructor = RegisterCapabilitiesEvent.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            var event = constructor.newInstance();
            if (!event.isItemRegistered(Capabilities.EnergyStorage.ITEM, MPSItems.BATTERY_1.get())) {
                MPSCapabilities.registerCapabilities(event);
            }
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot initialize datagen capability fixtures", exception);
        }
    }

    private static void verifyBatteryCrafting(HolderLookup.Provider registries) {
        Item[] batteries = {MPSItems.BATTERY_1.get(), MPSItems.BATTERY_2.get(), MPSItems.BATTERY_3.get(), MPSItems.BATTERY_4.get()};
        for (int tier = 1; tier <= batteries.length; tier++) {
            try {
                var field = MPSCommonConfig.class.getField("batteryMaxTransfer" + tier);
                int original = field.getInt(null);
                try {
                    for (int limit : new int[]{0, 1, original}) {
                        field.setInt(null, limit);
                        ItemStack emptyBattery = new ItemStack(batteries[tier - 1]);
                        EnergyStorage source = new EnergyStorage(10000, 10000, 10000, 10000);
                        int charged = ElectricItemUtils.transferEnergy(source,
                                emptyBattery.getCapability(Capabilities.EnergyStorage.ITEM), 10000);
                        check(charged == Math.min(limit, 10000), "Real tier " + tier + " battery respects transfer limit " + limit);
                        check(charged == emptyBattery.getOrDefault(MPSDataComponents.ENERGY, 0), "Real battery credits match withdrawal");
                        check(source.getEnergyStored() + charged == 10000, "Real battery charging conserves energy");
                        for (boolean chargedInput : new boolean[]{false, true}) {
                            ItemStack ingredient = new ItemStack(chargedInput ? MPSItems.BATTERY_1.get() : Items.STONE);
                            if (chargedInput) ingredient.set(MPSDataComponents.ENERGY, 12345);
                            ItemStack template = new ItemStack(batteries[tier - 1]);
                            template.set(MPSDataComponents.ENERGY, 9999);
                            ShapedEnergyRecipe recipe = new ShapedEnergyRecipe("", CraftingBookCategory.MISC,
                                    ShapedRecipePattern.of(Map.of('#', Ingredient.of(ingredient)), "#"), template, true);
                            ItemStack result = recipe.assemble(CraftingInput.of(1, 1, List.of(ingredient)), registries);
                            check(result.getCapability(Capabilities.EnergyStorage.ITEM).getEnergyStored() == 0,
                                    "Crafted tier " + tier + " is empty at transfer " + limit);
                            check(template.getOrDefault(MPSDataComponents.ENERGY, 0) == 9999, "Recipe template is not mutated");
                            check(ingredient.getOrDefault(MPSDataComponents.ENERGY, 0) == (chargedInput ? 12345 : 0),
                                    "Recipe assembly does not mutate ingredients");
                        }
                    }
                } finally {
                    field.setInt(null, original);
                }
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Cannot set up battery transfer fixture", exception);
            }
        }
    }

    private static void verifyEquipmentPersistence(HolderLookup.Provider registries) {
        for (Item item : new Item[]{MPSItems.POWER_ARMOR_HELMET_4.get(), MPSItems.POWER_ARMOR_CHESTPLATE_4.get(),
                MPSItems.POWER_ARMOR_LEGGINGS_4.get(), MPSItems.POWER_ARMOR_BOOTS_4.get(), MPSItems.POWER_FIST_4.get()}) {
            ItemStack gear = new ItemStack(item);
            var host = MPSCapabilities.getModularItemOrModeChangingCapability(gear);
            ItemStack battery = new ItemStack(MPSItems.BATTERY_4.get());
            battery.set(MPSDataComponents.ENERGY, 6789);
            battery.set(MPSDataComponents.ONLINE, false);
            TagUtils.setModuleDouble(battery, "persistence_fixture", 0.375);
            int installedSlot = -1;
            for (int slot = 0; slot < host.getSlots(); slot++) {
                if (host.isModuleValidForPlacement(slot, battery) && host.insertItem(slot, battery, false).isEmpty()) {
                    installedSlot = slot;
                    break;
                }
            }
            check(installedSlot >= 0, "Battery fixture installs in every equipment type");
            gear.set(MPSDataComponents.HEAT, 12.5);
            CompoundTag cosmetic = new CompoundTag();
            cosmetic.putIntArray("colors", new int[]{0xFF123456});
            gear.set(MPSDataComponents.MODEL_SPEC_ITEM_CODEC, cosmetic);

            ItemStack restored = ItemStack.parse(registries, gear.saveOptional(registries)).orElseThrow();
            check(ItemStack.isSameItemSameComponents(gear, restored), "Vanilla item persistence preserves complete gear components");
            ItemStack restoredBattery = MPSCapabilities.getModularItemOrModeChangingCapability(restored).getStackInSlot(installedSlot);
            check(restoredBattery.getOrDefault(MPSDataComponents.ENERGY, 0) == 6789, "Installed battery charge survives saved inventory storage");
            check(!restoredBattery.getOrDefault(MPSDataComponents.ONLINE, true), "Module online state survives saved inventory storage");
            check(TagUtils.getModuleTag(restoredBattery).getDouble("persistence_fixture") == 0.375,
                    "Module settings survive saved inventory storage");
            EnergyStorage source = new EnergyStorage(10000, 10000, 10000, 10000);
            var target = restored.getCapability(Capabilities.EnergyStorage.ITEM);
            int before = target.getEnergyStored();
            int charged = ElectricItemUtils.transferEnergy(source, target, 10000);
            check(charged == 10000, "Real equipment receives source-funded charge");
            check(target.getEnergyStored() == before + charged, "Installed battery receives the actual credit");
            check(source.getEnergyStored() + target.getEnergyStored() == before + 10000, "Equipment charging conserves energy");
        }
    }

    private static void verifyChargingBasePersistence(HolderLookup.Provider registries) {
        ChargingBaseBlockEntity base = new ChargingBaseBlockEntity(BlockPos.ZERO, MPSBlocks.CHARGING_BASE_BLOCK.get().defaultBlockState());
        int charged = base.getEnergyHandler().receiveEnergy(10000, false);
        check(charged == 10000 && base.getStoredPower() == 10000, "Base stores real internal energy without a battery");
        ChargingBaseBlockEntity restored = new ChargingBaseBlockEntity(BlockPos.ZERO, MPSBlocks.CHARGING_BASE_BLOCK.get().defaultBlockState());
        restored.loadWithComponents(base.saveWithFullMetadata(registries), registries);
        check(restored.getStoredPower() == 10000, "Internal energy survives block entity NBT round-trip");
        ItemStack battery = new ItemStack(MPSItems.BATTERY_1.get());
        battery.set(MPSDataComponents.ENERGY, 40000);
        base.getItems().setStackInSlot(0, battery);
        check(base.getStoredPower() == 50000, "Real inserted battery is counted once");
        restored.loadWithComponents(base.saveWithFullMetadata(registries), registries);
        check(restored.getStoredPower() == 50000, "Block energy and inserted battery both survive storage");
        check(restored.getItems().getStackInSlot(0).getOrDefault(MPSDataComponents.ENERGY, 0) == 40000,
                "Inserted battery preserves its own charge");
        CompoundTag before = restored.saveWithFullMetadata(registries);
        restored.getEnergyHandler().receiveEnergy(10000, true);
        restored.getEnergyHandler().extractEnergy(10000, true);
        check(before.equals(restored.saveWithFullMetadata(registries)), "Energy simulations leave serialized block state unchanged");
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
}
