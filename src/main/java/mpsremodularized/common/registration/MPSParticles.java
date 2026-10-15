package mpsremodularized.common.registration;

import java.util.function.Supplier;
import mpsremodularized.common.constants.MPSConstants;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MPSParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, MPSConstants.MOD_ID);

    public static final Supplier<SimpleParticleType> RAILGUN_FLASH =
            PARTICLE_TYPES.register("railgun_flash", () -> new SimpleParticleType(true));
    public static final Supplier<SimpleParticleType> RAILGUN_SPARK =
            PARTICLE_TYPES.register("railgun_spark", () -> new SimpleParticleType(false));

    private MPSParticles() {}
}
