package io.github.jodlodi.minions.orders;

import io.github.jodlodi.minions.MinionsRemastered;
import io.github.jodlodi.minions.capabilities.IMasterCapability;
import io.github.jodlodi.minions.minion.FollowOrderGoal;
import io.github.jodlodi.minions.minion.Minion;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FollowMasterOrder extends AbstractOrder { //TODO
    public static final ResourceLocation ID = MinionsRemastered.locate("follow_master_order");
    private final float START_DISTANCE = 9.0F;
    private final float STOP_DISTANCE = 2.0F;

    public FollowMasterOrder() {

    }

    public FollowMasterOrder(CompoundTag tag) {

    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean goalCanContinueToUse(FollowOrderGoal goal) {
        if (goal.getNavigation().isDone()) {
            return false;
        } else {
            return !(goal.getMinion().distanceToSqr(goal.getOwner()) <= (double)(STOP_DISTANCE * STOP_DISTANCE));
        }
    }

    @Override
    public void goalStart(FollowOrderGoal goal) {

    }

    @Override
    public void goalStop(FollowOrderGoal goal) {

    }

    @Override
    public void goalTick(FollowOrderGoal goal) {
        Minion minion = goal.getMinion();
        LivingEntity owner = goal.getOwner();
        minion.getLookControl().setLookAt(owner, 10.0F, (float)minion.getMaxHeadXRot());
        if (--goal.timeToRecalcPath <= 0) {
            goal.timeToRecalcPath = goal.adjustedTickDelay(10);
            if (!minion.isLeashed() && !minion.isPassenger()) {
                if (minion.distanceToSqr(owner) >= 144.0D) this.teleportToOwner(owner, minion, goal.getNavigation());
                else goal.getNavigation().moveTo(owner, goal.getSpeedModifier());
            }
        }
    }

    @Override
    public void tick(IMasterCapability masterCapability, Player player, Level level) {

    }

    private void teleportToOwner(LivingEntity owner, Minion minion, PathNavigation navigation) {
        BlockPos blockpos = owner.blockPosition();
        RandomSource random = minion.getRandom();

        for(int i = 0; i < 10; ++i) {
            int x = this.randomIntInclusive(random, -3, 3);
            int y = this.randomIntInclusive(random, -1, 1);
            int z = this.randomIntInclusive(random, -3, 3);
            boolean flag = this.maybeTeleportTo(owner, minion, navigation, blockpos.getX() + x, blockpos.getY() + y, blockpos.getZ() + z);
            if (flag) return;
        }
    }

    private boolean maybeTeleportTo(LivingEntity owner, Minion minion, PathNavigation navigation, int x, int y, int z) {
        if (Math.abs((double)x - owner.getX()) < 2.0D && Math.abs((double)z - owner.getZ()) < 2.0D) {
            return false;
        } else if (!this.canTeleportTo(minion, new BlockPos(x, y, z))) {
            return false;
        } else {
            minion.moveTo((double)x + 0.5D, y, (double)z + 0.5D, minion.getYRot(), minion.getXRot());
            navigation.stop();
            return true;
        }
    }

    private boolean canTeleportTo(Minion minion, BlockPos pos) {
        BlockPathTypes blockpathtypes = WalkNodeEvaluator.getBlockPathTypeStatic(minion.level, pos.mutable());
        if (blockpathtypes != BlockPathTypes.WALKABLE) {
            return false;
        } else {
            BlockState blockstate = minion.level.getBlockState(pos.below());
            if (blockstate.getBlock() instanceof LeavesBlock) {
                return false;
            } else {
                BlockPos blockpos = pos.subtract(minion.blockPosition());
                return minion.level.noCollision(minion, minion.getBoundingBox().move(blockpos));
            }
        }
    }

    private int randomIntInclusive(RandomSource random, int i, int j) {
        return random.nextInt(j - i + 1) + i;
    }

    @Override
    public @NotNull CompoundTag serialize() {
        return new CompoundTag();
    }
}
