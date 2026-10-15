package mpsremodularized.common.recipe.ingredients;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.stream.Stream;
import mpsremodularized.common.compat.CompatibleComponent;
import mpsremodularized.common.compat.CompatibilityPolicy;
import mpsremodularized.common.compat.CompatibilityRecipes;
import mpsremodularized.common.registration.MPSIngredientTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

/** Resolves to an immutable item list; simple network sync sends that exact server list. */
public record ComponentIngredient(CompatibleComponent component, boolean foreignOnly, List<Item> items)
        implements ICustomIngredient {
    public static final MapCodec<ComponentIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.xmap(CompatibleComponent::byKey, c -> c.key).fieldOf("component").forGetter(ComponentIngredient::component),
            Codec.BOOL.optionalFieldOf("foreign_only", false).forGetter(ComponentIngredient::foreignOnly)
    ).apply(instance, (component, foreignOnly) -> new ComponentIngredient(component, foreignOnly, CompatibilityRecipes.policy())));

    public ComponentIngredient {
        items = List.copyOf(items);
    }

    public ComponentIngredient(CompatibleComponent component, boolean foreignOnly, CompatibilityPolicy policy) {
        this(component, foreignOnly, policy.acceptedItems(component, foreignOnly));
    }

    public static Ingredient of(ItemLike item) {
        return CompatibleComponent.forItem(item).map(component ->
                new ComponentIngredient(component, false, CompatibilityRecipes.policy()).toVanilla()).orElseGet(() -> Ingredient.of(item));
    }

    @Override
    public boolean test(ItemStack stack) {
        return !stack.isEmpty() && items.contains(stack.getItem());
    }

    @Override
    public Stream<ItemStack> getItems() {
        return items.stream().map(ItemStack::new);
    }

    @Override
    public boolean isSimple() {
        return true;
    }

    @Override
    public IngredientType<?> getType() {
        return MPSIngredientTypes.COMPONENT.get();
    }
}
