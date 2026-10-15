package mpsremodularized.common.utils;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import javax.annotation.Nonnull;

public class PlayerUtils {
    public static void resetFloatKickTicks(Player player) {
        if (player instanceof ServerPlayer) {
            ((ServerPlayer) player).connection.aboveGroundTickCount = 0;
        }
    }

    public static float getPlayerCoolingBasedOnMaterial(@Nonnull Player player) {
        Level level = player.level();
        if (player.isInLava()) {
            return 0;
        }

        Biome biome = getBiome(player, level);
        // Colder biome base temperatures provide more passive cooling.
        float cool = (2.0F - biome.getBaseTemperature()) / 2.0F;

        if (player.isInWater())
            cool += 0.5F;

        // Increase cooling in every biome when Minecraft reports nighttime.
        if (level.isNight()) {
            cool += 0.8F;
        }

        // check for rain and if player is in the rain
        // check if rain can happen in the biome the player is in
        if (biome.hasPrecipitation()
                // check if raining in the world
                && level.isRaining()
                // check if the player can see the sky
                && level.canSeeSky(player.blockPosition().offset(0, 1, 0))) {
            cool += 0.2;
        }
        return cool;
    }

    public static Biome getBiome(Player player, Level level) {
        return level.getBiome(player.blockPosition()).value();
    }
}
