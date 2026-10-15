package mpsremodularized.common.entity;

import mpsremodularized.client.sound.MPSSoundDictionary;
import mpsremodularized.common.constants.MPSConstants;
import mpsremodularized.common.container.ArmorStandMenu;
import mpsremodularized.common.registration.MPSItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

public class MPSArmorStand extends ArmorStand {
    public MPSArmorStand(EntityType<? extends ArmorStand> entityType, Level level) {
        super(entityType, level);
        setShowArms(true);
        createAttributes();
    }

    public MPSArmorStand(Level level, double x, double y, double z) {
        super(level, x, y, z);
        setShowArms(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes().add(Attributes.MAX_HEALTH, 10.0D);
    }

    @Override
    public void brokenByPlayer(ServerLevel level, DamageSource source) {
        ItemStack itemstack = new ItemStack(MPSItems.ARMOR_STAND_ITEM);
        itemstack.set(DataComponents.CUSTOM_NAME, this.getCustomName());
        Block.popResource(this.level(), this.blockPosition(), itemstack);
        this.brokenByAnything(level, source);
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 vec, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (!this.isMarker() && itemstack.getItem() != Items.NAME_TAG) {
            if (player.isSpectator()) {
                return InteractionResult.SUCCESS;
            } else if (player.level().isClientSide) {
                return InteractionResult.CONSUME;
            } else {
                player.playSound(MPSSoundDictionary.SOUND_EVENT_GUI_SELECT.get(), 16.0F, 1.0F);
                player.openMenu(
                        new SimpleMenuProvider((windowID, playerInventory, playerEntity) ->
                                new ArmorStandMenu(windowID, playerInventory,this),
                                Component.translatable("screen." + MPSConstants.MOD_ID + ".armor_stand")),
                        buf -> buf.writeInt(getId()));
                return InteractionResult.SUCCESS;
            }
        } else {
            return InteractionResult.PASS;
        }
    }
}
