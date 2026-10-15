package mpsremodularized.common.item.module.tool.misc;

import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.rightclick.RightClickModule;
import mpsremodularized.common.capabilities.module.toggleable.IToggleableModule;
import mpsremodularized.common.compat.NativeModuleItem;
import mpsremodularized.common.compat.NativeToolIntegration;
import mpsremodularized.common.compat.PortableStationIntegration;
import mpsremodularized.common.compat.PortableBrewingIntegration;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PortableStationModule extends RightClickModule {
    public static final class Toggleable extends PortableStationModule implements IToggleableModule {
        public Toggleable(ItemStack module) { super(module); }
    }

    public PortableStationModule(ItemStack module) { super(module, ModuleCategory.TOOL, ModuleTarget.TOOLONLY); }
    @Override public boolean isAllowed() { return ((NativeModuleItem) getModule().getItem()).available(); }
    @Override public InteractionResultHolder<ItemStack> use(ItemStack fist, Level level, Player player, InteractionHand hand) {
        var owner = NativeToolIntegration.find(player, hand, ((NativeModuleItem) getModule().getItem()).donor.toString());
        if (owner == null || owner.fist() != fist || !owner.valid()) return InteractionResultHolder.fail(fist);
        if (level.isClientSide()) return InteractionResultHolder.success(fist);
        if (owner.item().donor.toString().equals("cyclic:anvil")) {
            mpsremodularized.common.compat.PoweredDiamondAnvilIntegration.open(owner);
            return InteractionResultHolder.consume(fist);
        }
        if (mpsremodularized.common.compat.NativeStationIntegration.supports(owner.item().donor.toString())) {
            mpsremodularized.common.compat.NativeStationIntegration.open(owner);
            return InteractionResultHolder.consume(fist);
        }
        String station = owner.item().donor.getPath();
        player.openMenu(new SimpleMenuProvider((id, inventory, menuPlayer) -> menu(id, inventory, owner),
                Component.translatable("container." + (station.equals("smithing_table") ? "upgrade" : station.equals("anvil") ? "repair" : station.equals("brewing_stand") ? "brewing" : "stonecutter"))));
        player.awardStat(station.equals("smithing_table") ? Stats.INTERACT_WITH_SMITHING_TABLE
                : station.equals("anvil") ? Stats.INTERACT_WITH_ANVIL : station.equals("brewing_stand") ? Stats.INTERACT_WITH_BREWINGSTAND : Stats.INTERACT_WITH_STONECUTTER);
        return InteractionResultHolder.consume(fist);
    }
    private static AbstractContainerMenu menu(int id, Inventory inventory, NativeToolIntegration.Installed owner) {
        var access = ContainerLevelAccess.create(owner.player().level(), owner.player().blockPosition());
        AbstractContainerMenu menu = switch (owner.item().donor.getPath()) {
            case "smithing_table" -> new SmithingMenu(id, inventory, access) {
                @Override public boolean stillValid(Player player) { return player == owner.player() && owner.valid(); }
                @Override protected boolean mayPickup(Player player, boolean hasStack) { return owner.valid() && super.mayPickup(player, hasStack); }
            };
            case "stonecutter" -> new StonecutterMenu(id, inventory, access) {
                @Override public boolean stillValid(Player player) { return player == owner.player() && owner.valid(); }
                @Override public boolean clickMenuButton(Player player, int button) { return stillValid(player) && super.clickMenuButton(player, button); }
            };
            case "anvil" -> new AnvilMenu(id, inventory, access) {
                @Override public boolean stillValid(Player player) { return player == owner.player() && owner.valid(); }
                @Override protected boolean mayPickup(Player player, boolean hasStack) { return owner.valid() && super.mayPickup(player, hasStack); }
                @Override protected void onTake(Player player, ItemStack result) {
                    PortableStationIntegration.takeAnvil(owner, () -> super.onTake(player, result));
                }
            };
            case "brewing_stand" -> {
                var brew = PortableBrewingIntegration.get(owner.fist(), owner.player(), owner.index());
                if (brew == null) throw new IllegalStateException("Missing owned brewing state");
                yield new BrewingStandMenu(id, inventory, brew, brew.data()) {
                    @Override public boolean stillValid(Player player) { return player == owner.player() && owner.valid() && brew.stillValid(player); }
                    @Override public void removed(Player player) { super.removed(player); brew.save(); }
                };
            }
            default -> throw new IllegalStateException("Unsupported portable station " + owner.item().donor);
        };
        // These stations persist through their native containers/anvil wear adapter.
        // Saving the opening donor here would overwrite the live worn-anvil/brewing state.
        ((mpsremodularized.imixin.common.item.INativeToolMenu) menu).mpsremodularized$bindNativeOwner(owner, () -> {});
        PortableStationIntegration.protectHost(menu, owner);
        return menu;
    }
}
