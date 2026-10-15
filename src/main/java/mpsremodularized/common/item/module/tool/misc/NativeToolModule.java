package mpsremodularized.common.item.module.tool.misc;

import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.capabilities.module.powermodule.ModuleTarget;
import mpsremodularized.common.capabilities.module.rightclick.RightClickModule;
import mpsremodularized.common.capabilities.module.toggleable.IToggleableModule;
import mpsremodularized.common.compat.NativeModuleItem;
import mpsremodularized.common.compat.NativeToolIntegration;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class NativeToolModule extends RightClickModule {
    public static final class Toggleable extends NativeToolModule implements IToggleableModule {
        public Toggleable(ItemStack module) { super(module); }
    }

    public NativeToolModule(ItemStack module) { super(module,
            ((NativeModuleItem) module.getItem()).kind == NativeModuleItem.Kind.CONSTRUCTION_STICK
                    ? ModuleCategory.CONSTRUCTION_STICK : ModuleCategory.TOOL, ModuleTarget.TOOLONLY); }
    @Override public boolean isAllowed() { return ((NativeModuleItem) getModule().getItem()).available(); }
    private NativeToolIntegration.Installed find(Player player, InteractionHand hand) {
        return NativeToolIntegration.find(player, hand, ((NativeModuleItem) getModule().getItem()).donor.toString());
    }
    @Override public InteractionResultHolder<ItemStack> use(ItemStack fist, Level level, Player player, InteractionHand hand) {
        var installed = find(player, hand);
        if (installed == null || installed.fist() != fist) return InteractionResultHolder.pass(fist);
        if (mpsremodularized.common.compat.NativeCompassIntegration.isCompass(installed.item().donor)
                && !player.isShiftKeyDown()) return InteractionResultHolder.pass(fist);
        var result = NativeToolIntegration.use(installed, () -> installed.source().getItem().use(level, player, hand));
        if (mpsremodularized.common.compat.NativeCompassIntegration.isCompass(installed.item().donor)) return InteractionResultHolder.sidedSuccess(fist, level.isClientSide());
        return new InteractionResultHolder<>(result.getResult(), fist);
    }
    @Override public InteractionResult useOn(UseOnContext context) {
        var installed = find(context.getPlayer(), context.getHand());
        if (installed == null) return InteractionResult.PASS;
        return NativeToolIntegration.use(installed, () -> installed.source().getItem().useOn(NativeToolIntegration.context(installed, context)));
    }
    @Override public InteractionResult onItemUseFirst(ItemStack fist, UseOnContext context) {
        var installed = find(context.getPlayer(), context.getHand());
        if (installed == null || installed.fist() != fist) return InteractionResult.PASS;
        return NativeToolIntegration.use(installed, () -> installed.source().getItem().onItemUseFirst(installed.source(), NativeToolIntegration.context(installed, context)));
    }
    @Override public InteractionResultHolder<ItemStack> interactLivingEntity(ItemStack fist, Player player, LivingEntity entity, InteractionHand hand) {
        var installed = find(player, hand);
        if (installed == null || installed.fist() != fist) return InteractionResultHolder.pass(fist);
        var result = NativeToolIntegration.use(installed, () -> {
            if (installed.item().donor.toString().equals("industrialforegoing:mob_imprisonment_tool")) {
                // Native wrapper assigns its donor to the hand. Call the explicit-stack API instead.
                try {
                    boolean captured = (boolean) installed.source().getItem().getClass()
                            .getMethod("capture", ItemStack.class, LivingEntity.class, Player.class)
                            .invoke(installed.source().getItem(), installed.source(), entity, player);
                    if (captured) player.swing(hand);
                    return captured ? InteractionResult.SUCCESS : InteractionResult.FAIL;
                } catch (ReflectiveOperationException exception) { throw new IllegalStateException("Unsupported native capture API", exception); }
            }
            return installed.source().getItem().interactLivingEntity(installed.source(), player, entity, hand);
        });
        return new InteractionResultHolder<>(result, fist);
    }
}
