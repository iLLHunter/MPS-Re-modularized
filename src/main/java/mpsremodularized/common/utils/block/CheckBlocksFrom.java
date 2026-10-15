package mpsremodularized.common.utils.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.Level;

public class CheckBlocksFrom {
    public final BlockPos startPos;
    public final int blockLimit;
    final BlockCheckedMap blockCheckedMap = new BlockCheckedMap();

    public CheckBlocksFrom(BlockPos startPos, int blockLimit) {
        this.startPos = startPos.immutable();
        this.blockLimit = blockLimit;
    }

    boolean isMatch(BlockPos pos, Level level) {
        return pos == startPos || level.getBlockState(startPos) == level.getBlockState(pos);
    }

    public boolean checkPositionMatch(Level level, BlockPos pos) {
        // check if within radius and limit not reached
        double distanceSq = getDistance(pos);
        if(distanceSq > 400 || blockCheckedMap.getMatches().size() > blockLimit) {
            blockCheckedMap.setMatch(pos,  false);
            return false;
        }

        boolean match = isMatch(pos, level );
        blockCheckedMap.setMatch(pos, match);
        return match;
    }

    public NonNullList<BlockPos> startCheck(Level level) {
        var matches = NonNullList.<BlockPos>create();
        var queue = new java.util.ArrayDeque<BlockPos>();
        var visited = new java.util.HashSet<BlockPos>();
        queue.add(startPos);
        while (!queue.isEmpty() && matches.size() < Math.max(1, blockLimit)) {
            var pos = queue.removeFirst();
            if (!visited.add(pos) || getDistance(pos) > 400 || !isMatch(pos, level)) continue;
            matches.add(pos);
            for (Direction direction : Direction.values()) queue.addLast(pos.relative(direction));
        }
        return matches;
    }

    public boolean checkPosition(Level level, BlockPos pos) {
        BlockChecked checked = blockCheckedMap.getChecked(pos);

        // no need to check something already checked
        if(checked.isChecked()) {
            return checked.isMatch() == Checked.CHECKED_MATCH;
        }
        boolean match = checkPositionMatch(level, pos);

        if(match) {
            for (Direction direction : Direction.values()) {
                BlockPos pos2 = pos.relative(direction);
                boolean match2 = checkPositionMatch(level, pos2);
                blockCheckedMap.setDirectionMatch(pos, direction, match2);
            }
        }
        return match;
    }

//    void debugPrintChecked(BlockPos pos, BlockChecked checked) {
//        switch (checked.isMatch()) {
//        case NOT_CHECKED -> MPSLogger.logDebug(pos + " not checked");
//        case CHECKED_MATCH -> MPSLogger.logDebug(pos + " checked match");
//        default -> MPSLogger.logDebug(pos + " checked NO Match");
//        }
//        for (Direction direction : Direction.values()) {
//            switch (checked.getChecked(direction)) {
//            case NOT_CHECKED -> MPSLogger.logDebug(pos + " " + direction.getName() + " not checked");
//            case CHECKED_MATCH -> MPSLogger.logDebug(pos + " " + direction.getName() + " checked match");
//            default -> MPSLogger.logDebug(pos + " " + direction.getName() + " checked NO Match");
//            }
//        }
//    }


    double getDistance(BlockPos pos) {
        if(pos == startPos) {
            return 0;
        }

        return startPos.distSqr(pos);
    }
}
