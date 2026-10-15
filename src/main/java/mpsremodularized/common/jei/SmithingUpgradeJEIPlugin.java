package mpsremodularized.common.jei;

import mpsremodularized.common.recipe.SmithingUpgradeRecipe;
import mpsremodularized.common.registration.RecipeSerializersRegistry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class SmithingUpgradeJEIPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return RecipeSerializersRegistry.SMITHING_UPGRADE_SERIALIZER.getId();
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        registration.getSmithingCategory().addExtension(SmithingUpgradeRecipe.class, new SmithingUpgradeCategoryExtension());
    }
}
