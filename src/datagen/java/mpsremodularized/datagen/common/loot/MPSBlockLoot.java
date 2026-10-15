package mpsremodularized.datagen.common.loot;

import mpsremodularized.common.registration.MPSBlocks;
import mpsremodularized.common.registration.MPSDataComponents;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;


public class MPSBlockLoot extends VanillaBlockLoot {
    public MPSBlockLoot(HolderLookup.Provider registries) {
        super(registries);
    }

    @Override
    protected void generate() {
        this.dropSelf(MPSBlocks.TINKER_TABLE_BLOCK.get());
        this.add(MPSBlocks.CHARGING_BASE_BLOCK.get(), createChargedInventoryLoot(MPSBlocks.CHARGING_BASE_BLOCK.get()));
    }

    protected LootTable.Builder createChargedInventoryLoot(Block block) {
        return LootTable.lootTable().withPool(this.
            applyExplosionCondition(block, LootPool.lootPool()
            .setRolls(ConstantValue.exactly(1.0F))
            .add(LootItem.lootTableItem(block)
            .apply(CopyComponentsFunction.copyComponents(
            CopyComponentsFunction.Source.BLOCK_ENTITY)
                .include(DataComponents.CONTAINER)
                .include(MPSDataComponents.ENERGY)
            ))));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return java.util.List.of(MPSBlocks.CHARGING_BASE_BLOCK.get(), MPSBlocks.TINKER_TABLE_BLOCK.get());
    }
}

