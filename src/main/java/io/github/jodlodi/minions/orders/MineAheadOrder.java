package io.github.jodlodi.minions.orders;

import io.github.jodlodi.minions.MinUtil;
import io.github.jodlodi.minions.MinionsRemastered;
import io.github.jodlodi.minions.capabilities.IMasterCapability;
import io.github.jodlodi.minions.minion.Minion;
import io.github.jodlodi.minions.minion.goals.EnactOrderGoal;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
public class MineAheadOrder extends AbstractOrder {
    // Static
    public static final ResourceLocation ID = MinionsRemastered.locate("mine_ahead_order");

    // Stored variables
    private final BlockPos minPos;
    private final BlockPos maxPos;
    private final Direction direction;
    private final int depth;

    // Temp variables
    private final Map<Integer, BlockPos> mineMap = new HashMap<>();
    private final Map<Integer, Float> breakMap = new HashMap<>();
    private int current;

    public MineAheadOrder(BlockPos minPos, BlockPos maxPos, Direction direction, int depth) {
        this.minPos = minPos;
        this.maxPos = maxPos;
        this.current = 0;
        this.direction = direction;
        this.depth = depth;
    }

    public MineAheadOrder(CompoundTag tag) {
        this.minPos = BlockPos.of(tag.getLong("min"));
        this.maxPos = BlockPos.of(tag.getLong("max"));
        this.current = tag.getInt("current");
        this.direction = Direction.values()[tag.getInt("direction")];
        this.depth = tag.getInt("depth");
    }

    @Override
    public @NotNull CompoundTag serialize() {
        return Util.make(new CompoundTag(), compoundTag -> {
            compoundTag.putLong("min", this.minPos.asLong());
            compoundTag.putLong("max", this.maxPos.asLong());
            compoundTag.putInt("current", this.current);
            compoundTag.putInt("direction", this.direction.ordinal());
            compoundTag.putInt("depth", this.depth);
        });
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean goalCanUse(EnactOrderGoal goal) {
        return true;
    }

    @Override
    public boolean goalCanContinueToUse(EnactOrderGoal goal) {
        return true;
    }

    @Override
    public void goalStart(EnactOrderGoal goal) {

    }

    @Override
    public void goalStop(EnactOrderGoal goal) {
        goal.getMinion().setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        Minion minion = goal.getMinion();
        int id = goal.getMasterCapability().getMinions().indexOf(minion.getUUID());
        this.mineMap.remove(id);
    }

    @Override
    public void goalTick(EnactOrderGoal goal) {
        Minion minion = goal.getMinion();
        int id = goal.getMasterCapability().getMinions().indexOf(minion.getUUID());
        Level level = minion.level;
        PathNavigation navigation = goal.getNavigation();
        LivingEntity owner = goal.getOwner();

        if (!this.mineMap.containsKey(id)) {
            BlockPos one = this.minPos.relative(this.direction, this.current);
            BlockPos two = this.maxPos.relative(this.direction, this.current);

            BlockPos closest = null;
            for (BlockPos pos : BlockPos.betweenClosed(one, two)) {
                if (MinUtil.isBlockBreakable(level.getBlockState(pos), level, pos, owner) && (closest == null || minion.distanceToSqr(Vec3.atCenterOf(pos)) < minion.distanceToSqr(Vec3.atCenterOf(closest)))) {
                    closest = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
                }
            }

            if (closest == null) return;
            minion.setItemInHand(InteractionHand.MAIN_HAND, MinUtil.bestToolForBlock(level.getBlockState(closest)));

            this.mineMap.put(id, closest);
            this.breakMap.put(id, 0.0F);
        }
        BlockPos pos = this.mineMap.get(id);

        if (pos.get(this.direction.getAxis()) != this.minPos.relative(this.direction, this.current).get(this.direction.getAxis())) {
            this.mineMap.remove(id);
            return;
        }

        BlockState state = level.getBlockState(pos);
        if (minion.tickCount % 5 == 0 && !MinUtil.isBlockBreakable(state, level, pos, owner)) {
            this.mineMap.remove(id);
            this.breakMap.put(id, 0.0F);
            return;
        }

        minion.getLookControl().setLookAt(Vec3.atCenterOf(pos));

        BlockPos destiny = pos.relative(this.direction, -1);
        Vec3 centerDestiny = Vec3.atBottomCenterOf(destiny);

        if (MinUtil.airDistanceSqr(centerDestiny, minion.position()) > 4.0D) {
            this.breakMap.put(id, 0.0F);
            navigation.moveTo(centerDestiny.x, centerDestiny.y, centerDestiny.z, goal.getSpeedModifier());
        } else if (!minion.getBlinking()) {
            minion.swing(InteractionHand.MAIN_HAND);
            navigation.stop();

            float breakProgress = this.breakMap.get(id) + (minion.getMineSpeed() / state.getDestroySpeed(level, pos) / 30.0F);

            if (breakProgress >= 1.0F) {
                this.mineMap.remove(id);
                this.breakMap.put(id, 0.0F);
                level.destroyBlock(pos, true, owner);
                MinUtil.blockLiquids(pos, level);
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
            for (int i = 0; i < 4; i++) {
                if (this.mineMap.get(i) != null && masterCapability.getMinions().get(i) != null) return; // Return in a non-null minion still has a block to break
            }

            BlockPos one = this.minPos.relative(this.direction, this.current);
            BlockPos two = this.maxPos.relative(this.direction, this.current);

            for (BlockPos pos : BlockPos.betweenClosed(one, two)) {
                if (MinUtil.isBlockBreakable(level.getBlockState(pos), level, pos, player)) return; // Return if a block that could still be broken is found
            }

            this.current += 1;
            if (this.current >= this.depth || !level.isInWorldBounds(one.relative(this.direction))) masterCapability.setOrder(null);
        } else {
            boolean axis = this.direction.getAxis() == Direction.Axis.Z;
            int mul = (axis ? this.direction.getStepZ() : this.direction.getStepX()) == -1 ? 1 : 0;
            MinUtil.particleAtWall(player.getRandom(), (axis ? this.minPos.getZ() : this.minPos.getX()) + mul, level, axis, axis ? this.minPos.getX() : this.minPos.getZ(), (axis ? this.maxPos.getX() : this.maxPos.getZ()) + 1, this.minPos.getY(), this.maxPos.getY() + 1);
        }
    }

}
