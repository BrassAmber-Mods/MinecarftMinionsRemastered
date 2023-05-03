package io.github.jodlodi.minions.orders;

import io.github.jodlodi.minions.MinUtil;
import io.github.jodlodi.minions.MinionsRemastered;
import io.github.jodlodi.minions.capabilities.IMasterCapability;
import io.github.jodlodi.minions.minion.FollowOrderGoal;
import io.github.jodlodi.minions.minion.Minion;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
public class MineDownOrder extends AbstractOrder {
    public static final ResourceLocation ID = MinionsRemastered.locate("mine_down_order");

    //Stored variables
    private final BlockPos minPos;
    private final BlockPos maxPos;
    private int currentY;

    //Temp variables
    private final Map<Integer, BlockPos> mineMap = new HashMap<>();
    private final Map<Integer, Float> breakMap = new HashMap<>();

    public MineDownOrder(BlockPos minPos, BlockPos maxPos) {
        this.minPos = minPos;
        this.maxPos = maxPos;
        this.currentY = maxPos.getY();
    }

    public MineDownOrder(CompoundTag tag) {
        this.minPos = BlockPos.of(tag.getLong("min"));
        this.maxPos = BlockPos.of(tag.getLong("max"));
        this.currentY = tag.getInt("currentY");
    }

    @Override
    public @NotNull CompoundTag serialize() {
        return Util.make(new CompoundTag(), compoundTag -> {
            compoundTag.putLong("min", this.minPos.asLong());
            compoundTag.putLong("max", this.maxPos.asLong());
            compoundTag.putInt("currentY", this.currentY);
        });
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean goalCanContinueToUse(FollowOrderGoal goal) {
        return true;
    }

    @Override
    public void goalStart(FollowOrderGoal goal) {

    }

    @Override
    public void goalStop(FollowOrderGoal goal) {
        goal.getMinion().setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        goal.getMasterCapability().setOrder(null);
    }

    @Override
    public void goalTick(FollowOrderGoal goal) {
        Minion minion = goal.getMinion();
        int id = goal.getMasterCapability().getMinions().indexOf(minion.getUUID());
        Level level = minion.level;
        PathNavigation navigation = goal.getNavigation();
        LivingEntity owner = goal.getOwner();

        if (!this.mineMap.containsKey(id)) {
            BlockPos one = new BlockPos(this.minPos.getX(), this.currentY, this.minPos.getZ());
            BlockPos two = new BlockPos(this.maxPos.getX(), this.currentY, this.maxPos.getZ());

            BlockPos closest = null;
            for (BlockPos pos : BlockPos.betweenClosed(one, two)) {
                if (this.breakable(level.getBlockState(pos), level, pos, owner) && (closest == null || minion.distanceToSqr(Vec3.atCenterOf(pos)) < minion.distanceToSqr(Vec3.atCenterOf(closest)))) {
                    closest = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
                }
            }

            if (closest == null) return;
            minion.setItemInHand(InteractionHand.MAIN_HAND, MinUtil.bestToolForBlock(level.getBlockState(closest)));

            this.mineMap.put(id, closest);
            this.breakMap.put(id, 0.0F);
        }
        BlockPos pos = this.mineMap.get(id);

        if (pos.getY() != this.currentY) {
            this.mineMap.remove(id);
            return;
        }

        Vec3 center = Vec3.atCenterOf(pos);
        minion.getLookControl().setLookAt(center);

        if (minion.distanceToSqr(center) > 4.0D) {
            this.breakMap.put(id, 0.0F);
            Vec3 g = Vec3.atBottomCenterOf(pos);
            navigation.moveTo(g.x, g.y, g.z, goal.getSpeedModifier());
        } else if (!minion.getBlinking()) {
            minion.swing(InteractionHand.MAIN_HAND);
            navigation.stop();

            BlockState state = level.getBlockState(pos);
            if (minion.tickCount % 5 == 0 && !this.breakable(state, level, pos, owner)) {
                this.mineMap.remove(id);
                this.breakMap.put(id, 0.0F);
                return;
            }

            float breakProgress = this.breakMap.get(id) + (minion.getMineSpeed() / state.getDestroySpeed(level, pos) / 30.0F);

            if (breakProgress >= 1.0F) {
                level.destroyBlock(pos, true, owner);
                this.blockLiquids(pos, level);
                this.mineMap.remove(id);
                this.breakMap.put(id, 0.0F);
            } else {
                level.destroyBlockProgress(minion.getId(), pos, (int)(breakProgress * 10.0F));
                this.breakMap.put(id, breakProgress);

                for (int i = 0; i < 4; i++) {
                    if (i != id && this.mineMap.get(i) != null && this.mineMap.get(id) != null &&this.mineMap.get(i).equals(this.mineMap.get(id))) {
                        this.breakMap.put(i, breakProgress);
                    }
                }
            }
        }
    }

    @Override
    public void tick(IMasterCapability masterCapability, Player player, Level level) {
        if (!level.isClientSide) {
            BlockPos one = new BlockPos(this.minPos.getX(), this.currentY, this.minPos.getZ());
            BlockPos two = new BlockPos(this.maxPos.getX(), this.currentY, this.maxPos.getZ());

            boolean flag = true;

            for (BlockPos pos : BlockPos.betweenClosed(one, two)) {
                if (this.breakable(level.getBlockState(pos), level, pos, player)) flag = false;
            }

            if (flag) {
                this.currentY -= 1;
                if (level.isOutsideBuildHeight(this.currentY)) masterCapability.setOrder(null);
            }
        } else {
            MinUtil.particleAtBorders(player.getRandom(), this.maxPos.getY() + 1.1D, level, this.minPos.getX(), this.maxPos.getX() + 1, this.minPos.getZ(), this.maxPos.getZ() + 1);
        }
    }

    protected boolean breakable(BlockState state, BlockGetter level, BlockPos pos, Entity destroyer) {
        return !state.isAir() && state.getDestroySpeed(level, pos) >= 0.0F && !(state.getBlock() instanceof LiquidBlock) && state.getBlock().canEntityDestroy(state, level, pos, destroyer);
    }

    protected void blockLiquids(BlockPos pos, Level level) {
        for (Direction direction : Direction.values()) {
            if (direction != Direction.UP) {
                BlockPos relative = pos.relative(direction);
                FluidState state = level.getFluidState(relative);
                if (state != Fluids.EMPTY.defaultFluidState() && state.isSource() && level.getBlockState(relative).getMaterial().isReplaceable()) {
                    level.setBlock(relative, Blocks.COBBLESTONE.defaultBlockState(), 3);
                }
            }
        }
    }
}
