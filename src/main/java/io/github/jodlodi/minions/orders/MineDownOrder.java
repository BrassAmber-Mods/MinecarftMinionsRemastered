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
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ParametersAreNonnullByDefault
public class MineDownOrder extends AbstractOrder {
    public static final ResourceLocation ID = MinionsRemastered.locate("mine_down_order");
    public static final List<Direction> DIRECTIONS = List.of(Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.EAST);
    public static final BlockState STAIRS = Blocks.BLACKSTONE_STAIRS.defaultBlockState();

    //Stored variables
    private final BlockPos minPos;
    private final BlockPos maxPos;
    private int currentY;
    private final int startDir;
    private final boolean stairs;

    //Temp variables
    private final Map<Integer, BlockPos> mineMap = new HashMap<>();
    private final Map<Integer, Float> breakMap = new HashMap<>();

    public MineDownOrder(BlockPos minPos, BlockPos maxPos, int startDir, boolean stairs) {
        this.minPos = minPos;
        this.maxPos = maxPos;
        this.currentY = maxPos.getY();
        this.startDir = startDir;
        this.stairs = stairs;
    }

    public MineDownOrder(CompoundTag tag) {
        this.minPos = BlockPos.of(tag.getLong("min"));
        this.maxPos = BlockPos.of(tag.getLong("max"));
        this.currentY = tag.getInt("currentY");
        this.startDir = tag.getInt("startDir");
        this.stairs = tag.getBoolean("stairs");
    }

    @Override
    public @NotNull CompoundTag serialize() {
        return Util.make(new CompoundTag(), compoundTag -> {
            compoundTag.putLong("min", this.minPos.asLong());
            compoundTag.putLong("max", this.maxPos.asLong());
            compoundTag.putInt("currentY", this.currentY);
            compoundTag.putInt("startDir", this.startDir);
            compoundTag.putBoolean("stairs", this.stairs);
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
            BlockPos one = new BlockPos(this.minPos.getX(), this.currentY, this.minPos.getZ());
            BlockPos two = new BlockPos(this.maxPos.getX(), this.currentY, this.maxPos.getZ());

            BlockPos closest = null;
            for (BlockPos pos : BlockPos.betweenClosed(one, two)) {
                if (this.isBlockBreakableDeluxe(level.getBlockState(pos), level, pos, owner) && (closest == null || minion.distanceToSqr(Vec3.atCenterOf(pos)) < minion.distanceToSqr(Vec3.atCenterOf(closest)))) {
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

        BlockState state = level.getBlockState(pos);
        if (minion.tickCount % 5 == 0 && !this.isBlockBreakableDeluxe(state, level, pos, owner)) {
            this.mineMap.remove(id);
            this.breakMap.put(id, 0.0F);
            return;
        }

        Vec3 center = Vec3.atCenterOf(pos);
        minion.getLookControl().setLookAt(center);

        if (minion.distanceToSqr(center) > 4.0D) {
            this.breakMap.put(id, 0.0F);
            Vec3 g = Vec3.atCenterOf(pos);
            navigation.moveTo(g.x, g.y, g.z, goal.getSpeedModifier());
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

    protected boolean isBlockBreakableDeluxe(BlockState state, Level level, BlockPos pos, Entity destroyer) {
        if (this.stairs) {
            BlockState stairCheck = this.stairCheck(pos, destroyer.level.random);
            if (stairCheck != null && state.getMaterial().isReplaceable()) {
                level.setBlock(pos, stairCheck, 3);
                return false;
            }
            if (stairCheck == state) return false;
        }
        return MinUtil.isBlockBreakable(state, level, pos, destroyer);
    }

    @Nullable
    protected BlockState stairCheck(BlockPos pos, RandomSource randomSource) {
        int maxX = this.maxPos.getX();
        int minX = this.minPos.getX();
        int sizeMinusOne = maxX - minX;
        if (sizeMinusOne <= 0) return null;
        int maxZ = this.maxPos.getZ();
        int minZ = this.minPos.getZ();
        int x = pos.getX();
        int z = pos.getZ();

        boolean xMax = maxX == x;
        boolean xMin = minX == x;
        boolean zMax = maxZ == z;
        boolean zMin = minZ == z;

        int depth = this.maxPos.getY() - pos.getY();

        int adjustedSize = sizeMinusOne - 1;
        if (adjustedSize <= 0) adjustedSize = 1;

        Direction dir = DIRECTIONS.get((this.startDir + (depth / adjustedSize)) % 4);

        int adjustedDepth = depth % adjustedSize;

        if (dir.getAxis() == Direction.Axis.X) {
            Direction faceDir = DIRECTIONS.get((DIRECTIONS.indexOf(dir) + 1) % 4);
            if (sizeMinusOne == 1) faceDir = dir;
            int dirX = dir.getNormal().getX();
            if (dirX == 1) {
                if (xMax) {
                    if (sizeMinusOne > 1 && adjustedDepth == 0 && zMin) return randomSource.nextInt(100) == 1 ? MinUtil.GILDED_BLACKSTONE : MinUtil.BLACKSTONE;
                    if (z - 1 == minZ + adjustedDepth) return STAIRS.setValue(StairBlock.FACING, faceDir);
                }
            } else if (xMin) {
                if (sizeMinusOne > 1 && adjustedDepth == 0 && zMax) return randomSource.nextInt(100) == 1 ? MinUtil.GILDED_BLACKSTONE : MinUtil.BLACKSTONE;
                if (z + 1 == maxZ - adjustedDepth) return STAIRS.setValue(StairBlock.FACING, faceDir);
            }
        } else {
            Direction faceDir = DIRECTIONS.get((DIRECTIONS.indexOf(dir) + 3) % 4);
            if (sizeMinusOne == 1) faceDir = dir.getOpposite();
            int dirZ = dir.getNormal().getZ();
            if (dirZ == 1) {
                if (zMin) {
                    if (sizeMinusOne > 1 && adjustedDepth == 0 && xMin) return randomSource.nextInt(100) == 1 ? MinUtil.GILDED_BLACKSTONE : MinUtil.BLACKSTONE;
                    if (x - 1 == minX + adjustedDepth) return STAIRS.setValue(StairBlock.FACING, faceDir);
                }
            } else if (zMax) {
                if (sizeMinusOne > 1 && adjustedDepth == 0 && xMax) return randomSource.nextInt(100) == 1 ? MinUtil.GILDED_BLACKSTONE : MinUtil.BLACKSTONE;
                if (x + 1 == maxX - adjustedDepth) return STAIRS.setValue(StairBlock.FACING, faceDir);
            }
        }

        return null;
    }

    @Override
    public void tick(IMasterCapability masterCapability, Player player, Level level) {
        if (!level.isClientSide) {
            boolean allEmpty = true;
            for (int i = 0; i < 4; i++) {
                if (this.mineMap.get(i) != null) allEmpty = false;
            }

            if (allEmpty) {
                BlockPos one = new BlockPos(this.minPos.getX(), this.currentY, this.minPos.getZ());
                BlockPos two = new BlockPos(this.maxPos.getX(), this.currentY, this.maxPos.getZ());

                boolean flag = true;

                for (BlockPos pos : BlockPos.betweenClosed(one, two)) {
                    if (this.isBlockBreakableDeluxe(level.getBlockState(pos), level, pos, player)) flag = false;
                }

                if (flag) {
                    this.currentY -= 1;
                    if (level.isOutsideBuildHeight(this.currentY)) masterCapability.setOrder(null);
                }
            }
        } else {
            MinUtil.particleAtBorders(player.getRandom(), this.maxPos.getY() + 1.1D, level, this.minPos.getX(), this.maxPos.getX() + 1, this.minPos.getZ(), this.maxPos.getZ() + 1);
        }
    }

}
