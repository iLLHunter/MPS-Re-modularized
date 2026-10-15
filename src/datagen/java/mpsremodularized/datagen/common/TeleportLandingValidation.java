package mpsremodularized.datagen.common;

import mpsremodularized.common.item.module.movement.DimensionalRiftModule;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** Runs after the data launch has initialized NeoForge and Minecraft registries. */
final class TeleportLandingValidation {
    private static int checks;

    static void validate() {
        checks = 0;
        for (Block block : new Block[]{Blocks.STONE, Blocks.DIRT, Blocks.OAK_PLANKS, Blocks.OBSIDIAN, Blocks.BEDROCK}) {
            check(block.defaultBlockState(), true, "Full opaque floor supports landing: " + block);
        }
        for (Block block : new Block[]{Blocks.AIR, Blocks.WATER, Blocks.LAVA, Blocks.COBWEB,
                Blocks.BAMBOO_SAPLING, Blocks.GLASS, Blocks.OAK_LEAVES, Blocks.STONE_SLAB, Blocks.OAK_STAIRS}) {
            check(block.defaultBlockState(), false, "Existing full opaque floor restriction rejects: " + block);
        }
        check(Blocks.STONE_SLAB.defaultBlockState().setValue(BlockStateProperties.SLAB_TYPE, SlabType.DOUBLE),
                true, "Double slab provides a full floor");
        for (Block block : new Block[]{Blocks.MAGMA_BLOCK, Blocks.CACTUS, Blocks.CAMPFIRE,
                Blocks.SOUL_CAMPFIRE, Blocks.POWDER_SNOW}) {
            check(block.defaultBlockState(), false, "Dangerous floor rejected: " + block);
        }
        verifyDestination();
        verifySearch();
        System.out.println("Teleport destination safety checks passed: " + checks);
    }

    private static void verifyDestination() {
        Fixture world = new Fixture();
        BlockPos target = new BlockPos(0, -32, 0);
        world.blocks.put(target.below(), Blocks.STONE.defaultBlockState());
        Vec3 origin = new Vec3(0.2, 70.1, 0.8);
        AABB bounds = DimensionalRiftModule.destinationBounds(
                new AABB(-0.1, 70.1, 0.5, 0.5, 71.9, 1.1), origin, target);
        require(Math.abs(bounds.getCenter().x - 0.5) < 1.0E-6
                && Math.abs(bounds.getCenter().z - 0.5) < 1.0E-6
                && Math.abs(bounds.minY + 32) < 1.0E-6, "Checks use the actual centered teleport destination");
        require(safe(world, target, bounds), "Negative-height floor with standing room is safe");
        world.blocks.put(target.above(), Blocks.STONE.defaultBlockState());
        require(!safe(world, target, bounds), "Head obstruction rejected by Minecraft collision shapes");
        world.blocks.remove(target.above());
        world.blocks.put(target, Blocks.STONE.defaultBlockState());
        require(!safe(world, target, bounds), "Feet obstruction rejected");
        for (Block hazard : new Block[]{Blocks.WATER, Blocks.LAVA, Blocks.FIRE, Blocks.SOUL_FIRE,
                Blocks.SWEET_BERRY_BUSH, Blocks.WITHER_ROSE, Blocks.POWDER_SNOW,
                Blocks.NETHER_PORTAL, Blocks.END_PORTAL, Blocks.END_GATEWAY}) {
            world.blocks.put(target, hazard.defaultBlockState());
            require(!safe(world, target, bounds), "Unsafe body space rejected: " + hazard);
        }
        world.blocks.remove(target);
        world.entityCollision = true;
        require(!safe(world, target, bounds), "Colliding entity rejected");
        world.entityCollision = false;
        world.border.setSize(1);
        require(!safe(world, target, bounds), "Full body must fit inside the world border");
        require(!DimensionalRiftModule.isWithinBuildBounds(bounds, -32, 320), "Floor must fit above minimum height");
        require(!DimensionalRiftModule.isWithinBuildBounds(new AABB(0, 127, 0, 1, 128.8, 1), 0, 128),
                "Nether logical ceiling includes headroom");
        require(DimensionalRiftModule.isWithinBuildBounds(new AABB(0, 126, 0, 1, 128, 1), 0, 128),
                "Body ending at ceiling remains valid");
    }

    private static boolean safe(Fixture world, BlockPos target, AABB bounds) {
        return DimensionalRiftModule.isSafeDestination(world, null, target, bounds, -64, 320);
    }

    private static void verifySearch() {
        BlockPos center = BlockPos.ZERO;
        var underground = DimensionalRiftModule.searchDestination(center, -64, 320,
                (x, z) -> 70, pos -> pos.getY() == -32);
        require(underground.orElseThrow().equals(new BlockPos(0, -32, 0)), "Search includes negative build heights");
        var raised = DimensionalRiftModule.searchDestination(center, 256, 512,
                (x, z) -> 300, pos -> pos.getY() == 270);
        require(raised.orElseThrow().getY() == 270, "Search respects nonzero positive minimum height");
        AtomicInteger attempts = new AtomicInteger();
        var exhausted = DimensionalRiftModule.searchDestination(center, -64, -61, (x, z) -> -62, pos -> {
            if (Math.abs(pos.getX()) > 16 || Math.abs(pos.getZ()) > 16 || pos.getY() <= -64 || pos.getY() >= -61) {
                throw new AssertionError("Search exceeded its spatial bounds");
            }
            attempts.incrementAndGet();
            return false;
        });
        require(exhausted.isEmpty(), "No unchecked fallback after an exhausted search");
        require(attempts.get() > 0 && attempts.get() <= 33 * 33 * 2, "Search work is bounded");
        var edge = DimensionalRiftModule.searchDestination(center, 0, 128, (x, z) -> 64,
                pos -> pos.getX() == 1 && pos.getZ() == 0 && pos.getY() == 64);
        require(edge.orElseThrow().equals(new BlockPos(1, 64, 0)), "Unsafe columns are skipped before selecting a safe one");
    }

    private static final class Fixture implements CollisionGetter {
        final Map<BlockPos, BlockState> blocks = new HashMap<>();
        final WorldBorder border = new WorldBorder();
        boolean entityCollision;
        public WorldBorder getWorldBorder() { return border; }
        public BlockGetter getChunkForCollisions(int x, int z) { return this; }
        public List<VoxelShape> getEntityCollisions(Entity entity, AABB bounds) {
            return entityCollision ? List.of(Shapes.create(bounds)) : List.of();
        }
        public BlockEntity getBlockEntity(BlockPos pos) { return null; }
        public BlockState getBlockState(BlockPos pos) { return blocks.getOrDefault(pos, Blocks.AIR.defaultBlockState()); }
        public FluidState getFluidState(BlockPos pos) { return getBlockState(pos).getFluidState(); }
        public int getHeight() { return 384; }
        public int getMinBuildHeight() { return -64; }
    }

    private static void require(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    private static void check(BlockState state, boolean expected, String message) {
        checks++;
        boolean actual = DimensionalRiftModule.isLandingSurface(state, EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        if (actual != expected) {
            throw new AssertionError(message + ": expected " + expected + ", got " + actual);
        }
    }
}
