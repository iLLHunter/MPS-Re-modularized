package mpsremodularized.datagen.common;

import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.datagen.client.lang.MPSLanguageProvider_EN_US;
import mpsremodularized.datagen.client.model.block.MPSBlockModelProvider;
import mpsremodularized.datagen.client.model.block.MPSBlockStateProvider;
import mpsremodularized.datagen.client.model.item.MPSItemModelProvider;
import mpsremodularized.datagen.common.damage.HeatDamageDataGen;
import mpsremodularized.datagen.common.loot.MPSBlockLoot;
import mpsremodularized.datagen.common.loot.MPSBlockTagProvider;
import mpsremodularized.datagen.common.recipes.MPSRecipeGenerator;
import mpsremodularized.datagen.common.tags.MPSItemTagsProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;

/** The only datagen subscriber. These classes are excluded from the runtime JAR. */
@EventBusSubscriber(modid = MPSConstants.MOD_ID)
public final class MPSDataGenerator {
    private MPSDataGenerator() {}

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        ConsolidationSnapshot.loadDefaultsForValidation();
        ServerConfigSyncValidation.validate();
        ModuleRegistryValidation.validate();
        TeleportLandingValidation.validate();
        NetworkProtocolValidation.validate();
        GameplayStorageValidation.validate(event.getLookupProvider().join());
        mpsremodularized.datagen.common.recipes.CompatibilityValidation.validate(event.getLookupProvider().join());
        if (event.includeClient()) {
            ModelTransformValidation.validate();
        }
        ConsolidationSnapshot.writeIfRequested();
        ConsolidationSnapshot.writeItemNamesIfRequested();
        if (Boolean.getBoolean("mps.consolidationJavaOnly")) {
            return;
        }
        var generator = event.getGenerator();
        var output = generator.getPackOutput();
        var registries = event.getLookupProvider();
        var helper = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new MPSLanguageProvider_EN_US(output));
        generator.addProvider(event.includeClient(), new MPSItemModelProvider(output, helper));
        generator.addProvider(event.includeClient(), new MPSBlockModelProvider(output, helper));
        generator.addProvider(event.includeClient(), new MPSBlockStateProvider(output, helper));
        var blockTags = new MPSBlockTagProvider(output, registries, helper);
        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(), new MPSItemTagsProvider(output, registries, blockTags.contentsGetter(), MPSConstants.MOD_ID, helper));
        generator.addProvider(event.includeServer(), new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(MPSBlockLoot::new, LootContextParamSets.BLOCK)), registries));
        generator.addProvider(event.includeServer(), new MPSRecipeGenerator(output, registries));
        HeatDamageDataGen.onGatherData(event);
    }
}
