package mpsremodularized.mixin.compat;

import mpsremodularized.imixin.common.item.INativeToolMenu;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo @Mixin(targets = "com.lothrazar.cyclic.block.anvil.ScreenAnvil", remap = false)
public abstract class CyclicPortableAnvilScreenMixin extends AbstractContainerScreen<AbstractContainerMenu> {
    protected CyclicPortableAnvilScreenMixin(AbstractContainerMenu menu, Inventory inventory, Component title) { super(menu, inventory, title); }
    @Inject(method = "init", at = @At("RETURN"))
    private void mps$alwaysOn(CallbackInfo ci) {
        if (((INativeToolMenu)menu).mpsremodularized$nativeOwner() == null) return;
        try {
            var field = getClass().getDeclaredField("btnRedstone"); field.setAccessible(true);
            var button = (AbstractWidget)field.get(this); button.visible = false; button.active = false; removeWidget(button);
        } catch (ReflectiveOperationException error) { throw new IllegalStateException("Unsupported Cyclic redstone control", error); }
    }
}
