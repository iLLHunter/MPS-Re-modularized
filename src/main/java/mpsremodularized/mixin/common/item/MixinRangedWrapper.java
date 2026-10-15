package mpsremodularized.mixin.common.item;

import com.mojang.datafixers.util.Pair;
import mpsremodularized.imixin.common.item.IMixinRangedWrapper;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RangedWrapper.class)
public class MixinRangedWrapper implements IMixinRangedWrapper {
    @Final
    @Shadow
    private int minSlot;

    @Final
    @Shadow
    private int maxSlot;

    @Override
    public boolean mpsremodularized$contains(int slot) {
        return slot >= minSlot && slot < maxSlot;
    }

    @Override
    public Pair<Integer, Integer> mpsremodularized$getRange() {
        return Pair.of(minSlot, maxSlot);
    }
}
