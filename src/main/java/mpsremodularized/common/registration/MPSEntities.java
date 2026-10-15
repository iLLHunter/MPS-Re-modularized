package mpsremodularized.common.registration;

import java.util.function.Supplier;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.entity.LuxCapacitorEntity;
import mpsremodularized.common.entity.MPSArmorStand;
import mpsremodularized.common.entity.PlasmaBallEntity;
import mpsremodularized.common.entity.SpinningBladeEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MPSEntities {
    /**
     * Entity Types ------------------------------------------------------------------------------
     */
    public static final DeferredRegister<EntityType<?>> MPS_ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, MPSConstants.MOD_ID);

    public static final Supplier<EntityType<LuxCapacitorEntity>> LUX_CAPACITOR_ENTITY_TYPE = MPS_ENTITIES.register(MPSConstants.LUX_CAPACITOR.getPath(),
        ()-> EntityType.Builder.<LuxCapacitorEntity>of(LuxCapacitorEntity::new, MobCategory.MISC)
        .sized(0.25F, 0.25F)
        .build(MPSConstants.LUX_CAPACITOR.toString()));

    public static final Supplier<EntityType<SpinningBladeEntity>> SPINNING_BLADE_ENTITY_TYPE = MPS_ENTITIES.register(MPSConstants.SPINNING_BLADE.getPath(),
        ()-> EntityType.Builder.<SpinningBladeEntity>of(SpinningBladeEntity::new, MobCategory.MISC)
        .sized(0.25F, 0.25F) // FIXME! check size
        .build(MPSConstants.SPINNING_BLADE.toString()));

    public static final Supplier<EntityType<PlasmaBallEntity>> PLASMA_BALL_ENTITY_TYPE = MPS_ENTITIES.register(MPSConstants.PLASMA_BALL.getPath(),
        ()-> EntityType.Builder.<PlasmaBallEntity>of(PlasmaBallEntity::new, MobCategory.MISC)
        //                    .size(0.25F, 0.25F)
        .build(MPSConstants.PLASMA_BALL.toString()));

    //    public static final Supplier<EntityType<RailgunBoltEntity>> RAILGUN_BOLT_ENTITY_TYPE = MPS_ENTITIES.register(MPSConstants.RAILGUN_BOLT.getPath(),
    //            ()-> EntityType.Builder.<RailgunBoltEntity>of(RailgunBoltEntity::new, MobCategory.MISC)
    //                    .sized(0.25F, 0.25F)
    //                    .build(MPSConstants.RAILGUN_BOLT.toString()));

    public static final Supplier<EntityType<MPSArmorStand>> ARMOR_STAND__ENTITY_TYPE = MPS_ENTITIES.register(MPSConstants.ARMOR_STAND__ENTITY_TYPE_REGNAME,
        () -> EntityType.Builder.<MPSArmorStand>of(MPSArmorStand::new, MobCategory.CREATURE)
        .sized(0.5F, 1.975F) // Hitbox Size
        .build(ResourceLocation.fromNamespaceAndPath(MPSConstants.MOD_ID, MPSConstants.ARMOR_STAND__ENTITY_TYPE_REGNAME).toString()));
}
