package mpsremodularized.common.compat;

import mpsremodularized.common.capabilities.inventory.modechanging.IModeChangingItem;
import mpsremodularized.common.capabilities.module.powermodule.ModuleCategory;
import mpsremodularized.common.registration.MPSCapabilities;
import mpsremodularized.imixin.common.item.IUseOnContextMixn;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;

@EventBusSubscriber(modid = "mpsremodularized")
public final class PaxelIntegration {
    public record Installed(IModeChangingItem host, int index, ItemStack module, NativeModuleItem item, ItemStack source) {
        public void save() { item.store(module, source); host.updateModuleInSlot(index, module); }
    }
    private record Use(Player player, InteractionHand hand, ItemStack source) {}
    private static final ThreadLocal<Use> USE = new ThreadLocal<>();
    private PaxelIntegration() {}

    public static Installed find(ItemStack fist) {
        var host = MPSCapabilities.getModeChangingModularItem(fist);
        if (host == null) return null;
        var range = host.getRangeForCategory(ModuleCategory.PAXEL);
        if (range == null) return null;
        int index = range.getFirst();
        var module = host.getStackInSlot(index);
        if (!(module.getItem() instanceof NativeModuleItem item) || item.kind != NativeModuleItem.Kind.PAXEL
                || !host.isModuleOperational(host.getModuleCapability(module))) return null;
        var source = item.source(module);
        return source.isEmpty() ? null : new Installed(host, index, module, item, source);
    }

    public static ItemStack nativeHeld(Player player, InteractionHand hand) {
        var use = USE.get();
        return use != null && use.player == player && use.hand == hand ? use.source : player.getItemInHand(hand);
    }

    /** Source locator used only by the originating mod's inspected AOE input/highlight handlers. */
    public static ItemStack mysticalAoeTool(Player player) {
        var fist = player.getMainHandItem();
        var installed = find(fist);
        return installed != null && installed.item.provider == CompatibleMod.MYSTICAL_AGRADDITIONS ? installed.source : fist;
    }

    public static void saveMysticalAoeTool(Player player, ItemStack source) {
        var installed = find(player.getMainHandItem());
        if (installed == null || installed.item.provider != CompatibleMod.MYSTICAL_AGRADDITIONS
                || source.getItem() != installed.source.getItem()) return;
        installed.item.store(installed.module, source);
        installed.host.updateModuleInSlot(installed.index, installed.module);
    }

    /** Identifies only the converted tool passed to Mystical's native area harvest helper. */
    public static ItemStack mysticalFistForSource(Player player, ItemStack source) {
        var fist = player.getMainHandItem();
        var installed = find(fist);
        return installed != null && installed.item.provider == CompatibleMod.MYSTICAL_AGRADDITIONS
                && ItemStack.isSameItemSameComponents(source, installed.source) ? fist : ItemStack.EMPTY;
    }

    public static InteractionResult useOn(Installed installed, UseOnContext original) {
        if (original.getPlayer() == null || original.getHand() != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        var donorContext = new UseOnContext(original.getLevel(), original.getPlayer(), original.getHand(), installed.source,
                ((IUseOnContextMixn) original).mpsremodularized$getBlockHitResult());
        if (USE.get() != null) throw new IllegalStateException("Nested native Paxel use");
        USE.set(new Use(original.getPlayer(), original.getHand(), installed.source));
        try { return installed.source.getItem().useOn(donorContext); }
        finally {
            USE.remove();
            if (!original.getLevel().isClientSide()) installed.save();
        }
    }

    public static InteractionResult use(Installed installed, Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (USE.get() != null) throw new IllegalStateException("Nested native Paxel use");
        USE.set(new Use(player, hand, installed.source));
        try {
            var result = installed.source.getItem().use(level, player, hand);
            // Item.use's default PASS returns the real hand stack. The Fist is still
            // held during delegation, so that result must not replace the donor.
            var returned = result.getObject() == installed.host.getModularItemStack() ? installed.source : result.getObject();
            installed.item.store(installed.module, returned);
            if (!level.isClientSide()) installed.host.updateModuleInSlot(installed.index, installed.module);
            return result.getResult();
        } finally { USE.remove(); }
    }

    public static void tick(ItemStack fist, Level level, Entity owner, int slot, boolean selected) {
        var installed = find(fist);
        if (installed == null) return;
        try { installed.source.getItem().inventoryTick(installed.source, level, owner, slot, selected); }
        finally { if (!level.isClientSide()) installed.save(); }
    }

    /** Supplies the native base tool to existing area-mining plans without selecting another tool. */
    public static net.minecraft.core.NonNullList<mpsremodularized.common.capabilities.module.blockbreaking.IBlockBreakingModule> miningTools(ItemStack fist) {
        var tools = net.minecraft.core.NonNullList.<mpsremodularized.common.capabilities.module.blockbreaking.IBlockBreakingModule>create();
        var installed = find(fist);
        if (installed != null) {
            tools.add(new NativeMiningTool(installed));
            return tools;
        }
        var host = MPSCapabilities.getModeChangingModularItem(fist);
        if (host != null) for (int i = 0; i < host.getSlots(); i++) {
            var module = host.getModuleCapability(host.getStackInSlot(i));
            if (host.isModuleOperational(module) && module instanceof mpsremodularized.common.capabilities.module.blockbreaking.IBlockBreakingModule tool) tools.add(tool);
        }
        return tools;
    }

    private static final class NativeMiningTool extends mpsremodularized.common.capabilities.module.powermodule.PowerModule
            implements mpsremodularized.common.capabilities.module.blockbreaking.IBlockBreakingModule {
        private final Installed installed;
        private NativeMiningTool(Installed installed) {
            super(installed.module, ModuleCategory.PAXEL, mpsremodularized.common.capabilities.module.powermodule.ModuleTarget.TOOLONLY);
            this.installed = installed;
            addBaseProperty(mpsremodularized.common.constants.MPSConstants.HARVEST_SPEED, 1, "");
        }
        @Override public int getEnergyUsage() { return 0; }
        @Override public ItemStack getEmulatedTool() { return installed.source; }
        @Override public void handleBreakSpeed(net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed event) {}
        @Override public boolean mineBlock(ItemStack fist, Level level, net.minecraft.world.level.block.state.BlockState state,
                net.minecraft.core.BlockPos pos, net.minecraft.world.entity.LivingEntity owner, double energy) {
            try { return installed.source.getItem().mineBlock(installed.source, level, state, pos, owner); }
            finally { if (!level.isClientSide()) installed.save(); }
        }
    }

    /** Loot receives effective stacked enchantments on a disposable copy, never on the sealed donor. */
    public static ItemStack lootTool(ItemStack fist, Level level) {
        var installed = find(fist);
        if (installed == null) return fist.copy();
        var copy = installed.source.copy();
        copy.set(net.minecraft.core.component.DataComponents.ENCHANTMENTS,
                fist.getAllEnchantments(level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)));
        return copy;
    }

    @SubscribeEvent public static void attributes(ItemAttributeModifierEvent event) {
        var installed = find(event.getItemStack());
        if (installed == null) return;
        for (var modifier : installed.source.getAttributeModifiers().modifiers()) {
            event.replaceModifier(modifier.attribute(), modifier.modifier(), modifier.slot());
        }
    }

    @SubscribeEvent public static void enchantments(GetEnchantmentLevelEvent event) {
        var installed = find(event.getStack());
        if (installed == null) return;
        // Effective native enchantments only: never write new enchantments into the sealed donor.
        var enchantments = installed.source.getAllEnchantments(event.getLookup());
        for (var enchantment : enchantments.keySet()) {
            // The user approved addition when the carried tool and MPS enhancement share an enchantment.
            event.getEnchantments().set(enchantment, event.getEnchantments().getLevel(enchantment) + enchantments.getLevel(enchantment));
        }
    }
}
