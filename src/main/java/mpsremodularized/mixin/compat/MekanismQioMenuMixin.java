package mpsremodularized.mixin.compat;
import mpsremodularized.common.compat.*;
import mpsremodularized.imixin.common.item.INativeToolMenu;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Pseudo @Mixin(targets="mekanism.common.inventory.container.item.PortableQIODashboardContainer",remap=false)
public abstract class MekanismQioMenuMixin implements INativeToolMenu {
    @Unique private NativeToolIntegration.Installed mps$owner;
    // The shared constructor also covers the native client menu's resize/recreation path.
    @Inject(method="<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;ZLmekanism/common/content/qio/IQIOCraftingWindowHolder;Lmekanism/common/network/to_client/qio/BulkQIOData;Lmekanism/common/inventory/container/QIOItemViewerContainer$CachedSearchData;Lmekanism/common/inventory/container/QIOItemViewerContainer$CachedSortingData;Lmekanism/common/inventory/container/SelectedWindowData;Lmekanism/common/content/qio/QIOFrequency;)V",at=@At("RETURN"))
    private void mps$bind(int id,Inventory inv,InteractionHand hand,ItemStack stack,boolean remote,@Coerce Object crafting,@Coerce Object data,@Coerce Object search,@Coerce Object sorting,@Coerce Object window,@Coerce Object frequency,CallbackInfo ci) {
        var owner=NativeToolIntegration.find(inv.player,hand,"mekanism:portable_qio_dashboard");
        if (owner!=null && stack.is(owner.source().getItem())) {
            mps$owner=new NativeToolIntegration.Installed(owner.player(),owner.hand(),owner.fist(),owner.host(),owner.index(),owner.item(),owner.identity(),stack);
            PortableStationIntegration.protectHost((AbstractContainerMenu)(Object)this,mps$owner);
        }
    }
    @Override public NativeToolIntegration.Installed mpsremodularized$nativeOwner() { return mps$owner; }
    @Override public boolean mpsremodularized$nativeOwnerValid() { return mps$owner==null || mps$owner.valid() && MekanismToolIntegration.canAccess(mps$owner.player(),mps$owner.source()); }
    @Inject(method="stillValid",at=@At("HEAD"),cancellable=true)
    private void mps$valid(Player player,CallbackInfoReturnable<Boolean> ci) { if(mps$owner!=null)ci.setReturnValue(player==mps$owner.player()&&mpsremodularized$nativeOwnerValid()); }
    @Inject(method="clicked",at=@At("HEAD"),cancellable=true)
    private void mps$click(int slot,int button,net.minecraft.world.inventory.ClickType type,Player player,CallbackInfo ci) { if(mps$owner!=null&&!mpsremodularized$nativeOwnerValid())ci.cancel(); }
    @Inject(method="clicked",at=@At("RETURN"))
    private void mps$save(int slot,int button,net.minecraft.world.inventory.ClickType type,Player player,CallbackInfo ci) { mpsremodularized$saveNativeSource(); }
}
