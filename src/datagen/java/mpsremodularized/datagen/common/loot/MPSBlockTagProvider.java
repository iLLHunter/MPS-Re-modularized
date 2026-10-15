package mpsremodularized.datagen.common.loot;

import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.registration.MPSBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class MPSBlockTagProvider extends BlockTagsProvider {
    public MPSBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, MPSConstants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(MPSBlocks.CHARGING_BASE_BLOCK.get(), MPSBlocks.TINKER_TABLE_BLOCK.get());
    }

    @Override
    public String getName() {
        return "MPSBlockTagProvider";
    }
}
