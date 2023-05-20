package io.github.jodlodi.minions.minion;

import io.github.jodlodi.minions.MinUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MinionNodeEvaluator extends WalkNodeEvaluator {
    @Override
    public void prepare(PathNavigationRegion region, Mob mob) {
        this.level = region;
        this.mob = mob;
        this.nodes.clear();

        float width = mob.getBbWidth();
        float height = mob.getBbHeight();
        if (!mob.getPassengers().isEmpty()) {
            Entity passenger = mob.getPassengers().get(0);
            width = Math.max(width, passenger.getBbWidth());
            height = (float)(mob.getPassengersRidingOffset() + passenger.getMyRidingOffset()) + passenger.getBbHeight();
        }

        this.entityWidth = Mth.floor(width + 1.0F);
        this.entityHeight = Mth.floor(height + 1.0F);
        this.entityDepth = Mth.floor(width + 1.0F);
        this.oldWaterCost = mob.getPathfindingMalus(BlockPathTypes.WATER);
    }

    @Nullable
    @Override
    public Node getStart() {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        int i = this.mob.getBlockY();
        BlockState blockstate = this.level.getBlockState(blockpos$mutableblockpos.set(this.mob.getX(), i, this.mob.getZ()));
        if (!this.mob.canStandOnFluid(blockstate.getFluidState())) {
            if (this.canFloat() && this.mob.isInWater()) {
                while(true) {
                    if (!blockstate.is(Blocks.WATER) && blockstate.getFluidState() != Fluids.WATER.getSource(false)) {
                        --i;
                        break;
                    }

                    ++i;
                    blockstate = this.level.getBlockState(blockpos$mutableblockpos.set(this.mob.getX(), i, this.mob.getZ()));
                }
            } else if (this.mob.isOnGround()) {
                i = Mth.floor(this.mob.getY() + 0.5D);
            } else {
                BlockPos blockpos;
                for(blockpos = this.mob.blockPosition(); (this.level.getBlockState(blockpos).isAir() || this.level.getBlockState(blockpos).isPathfindable(this.level, blockpos, PathComputationType.LAND)) && blockpos.getY() > this.mob.level.getMinBuildHeight(); blockpos = blockpos.below()) {
                }

                i = blockpos.above().getY();
            }
        } else {
            while(this.mob.canStandOnFluid(blockstate.getFluidState())) {
                ++i;
                blockstate = this.level.getBlockState(blockpos$mutableblockpos.set(this.mob.getX(), i, this.mob.getZ()));
            }

            --i;
        }

        BlockPos blockpos1 = this.mob.blockPosition();
        BlockPathTypes blockpathtypes = this.getCachedBlockType(this.mob, blockpos1.getX(), i, blockpos1.getZ());
        if (this.mob.getPathfindingMalus(blockpathtypes) < 0.0F) {
            AABB aabb = MinUtil.getRidingAABB(this.mob);
            if (this.hasPositiveMalus(blockpos$mutableblockpos.set(aabb.minX, i, aabb.minZ)) || this.hasPositiveMalus(blockpos$mutableblockpos.set(aabb.minX, i, aabb.maxZ)) || this.hasPositiveMalus(blockpos$mutableblockpos.set(aabb.maxX, i, aabb.minZ)) || this.hasPositiveMalus(blockpos$mutableblockpos.set(aabb.maxX, i, aabb.maxZ))) {
                return this.getStartNode(blockpos$mutableblockpos);
            }
        }

        return this.getStartNode(new BlockPos(blockpos1.getX(), i, blockpos1.getZ()));
    }

    @Nullable
    @Override
    protected Node findAcceptedNode(int x, int y, int z, int recursion, double floorLevel, Direction direction, BlockPathTypes pathTypes) {
        Node node = null;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        double floorLevel1 = this.getFloorLevel(blockpos$mutableblockpos.set(x, y, z));
        if (floorLevel1 - floorLevel > 1.125D) {
            return null;
        } else {
            BlockPathTypes blockpathtypes = this.getCachedBlockType(this.mob, x, y, z);
            float malus = this.mob.getPathfindingMalus(blockpathtypes);
            double width = (double)this.getMaxBBWidth() / 2.0D;
            if (malus >= 0.0F) node = this.getNodeAndUpdateCostToMax(x, y, z, blockpathtypes, malus);

            if (doesBlockHavePartialCollision(pathTypes) && node != null && node.costMalus >= 0.0F && !this.canReachWithoutCollision(node)) {
                node = null;
            }

            if (blockpathtypes != BlockPathTypes.WALKABLE && (!this.isAmphibious() || blockpathtypes != BlockPathTypes.WATER)) {
                if ((node == null || node.costMalus < 0.0F) && recursion > 0 && blockpathtypes != BlockPathTypes.FENCE && blockpathtypes != BlockPathTypes.UNPASSABLE_RAIL && blockpathtypes != BlockPathTypes.TRAPDOOR && blockpathtypes != BlockPathTypes.POWDER_SNOW) {
                    node = this.findAcceptedNode(x, y + 1, z, recursion - 1, floorLevel, direction, pathTypes);
                    if (node != null && (node.type == BlockPathTypes.OPEN || node.type == BlockPathTypes.WALKABLE) && this.getMaxBBWidth() < 1.0F) {
                        double xStep = (double)(x - direction.getStepX()) + 0.5D;
                        double zStep = (double)(z - direction.getStepZ()) + 0.5D;
                        AABB aabb = new AABB(xStep - width, getFloorLevel(this.level, blockpos$mutableblockpos.set(xStep, y + 1, zStep)) + 0.001D, zStep - width, xStep + width, (double)this.getMaxBBHeight() + getFloorLevel(this.level, blockpos$mutableblockpos.set(node.x, node.y, (double)node.z)) - 0.002D, zStep + width);
                        if (this.hasCollisions(aabb)) {
                            node = null;
                        }
                    }
                }

                if (!this.isAmphibious() && blockpathtypes == BlockPathTypes.WATER && !this.canFloat()) {
                    if (this.getCachedBlockType(this.mob, x, y - 1, z) != BlockPathTypes.WATER) {
                        return node;
                    }

                    while(y > this.mob.level.getMinBuildHeight()) {
                        --y;
                        blockpathtypes = this.getCachedBlockType(this.mob, x, y, z);
                        if (blockpathtypes != BlockPathTypes.WATER) {
                            return node;
                        }

                        node = this.getNodeAndUpdateCostToMax(x, y, z, blockpathtypes, this.mob.getPathfindingMalus(blockpathtypes));
                    }
                }

                if (blockpathtypes == BlockPathTypes.OPEN) {
                    int j = 0;
                    int i = y;

                    while(blockpathtypes == BlockPathTypes.OPEN) {
                        --y;
                        if (y < this.mob.level.getMinBuildHeight()) {
                            return this.getBlockedNode(x, i, z);
                        }

                        if (j++ >= this.mob.getMaxFallDistance()) {
                            return this.getBlockedNode(x, y, z);
                        }

                        blockpathtypes = this.getCachedBlockType(this.mob, x, y, z);
                        malus = this.mob.getPathfindingMalus(blockpathtypes);
                        if (blockpathtypes != BlockPathTypes.OPEN && malus >= 0.0F) {
                            node = this.getNodeAndUpdateCostToMax(x, y, z, blockpathtypes, malus);
                            break;
                        }

                        if (malus < 0.0F) {
                            return this.getBlockedNode(x, y, z);
                        }
                    }
                }

                if (doesBlockHavePartialCollision(blockpathtypes)) {
                    node = this.getNode(x, y, z);
                    if (node != null) {
                        node.closed = true;
                        node.type = blockpathtypes;
                        node.costMalus = blockpathtypes.getMalus();
                    }
                }

            }
            return node;
        }
    }

    @Override
    public boolean canReachWithoutCollision(Node node) {
        AABB aabb = MinUtil.getRidingAABB(this.mob);
        Vec3 vec3 = new Vec3((double)node.x - this.mob.getX() + aabb.getXsize() / 2.0D, (double)node.y - this.mob.getY() + aabb.getYsize() / 2.0D, (double)node.z - this.mob.getZ() + aabb.getZsize() / 2.0D);
        int i = Mth.ceil(vec3.length() / aabb.getSize());
        vec3 = vec3.scale(1.0F / (float)i);

        for(int j = 1; j <= i; ++j) {
            aabb = aabb.move(vec3);
            if (this.hasCollisions(aabb)) {
                return false;
            }
        }

        return true;
    }

    protected float getMaxBBWidth() {
        if (this.mob.getPassengers().isEmpty()) return this.mob.getBbWidth();
        else return Math.max(this.mob.getBbWidth(), this.mob.getPassengers().get(0).getBbWidth());
   }

    protected float getMaxBBHeight() {
        Entity passenger = this.mob.getFirstPassenger();
        if (passenger == null) return this.mob.getBbHeight();
        else return (float)(mob.getPassengersRidingOffset() + passenger.getMyRidingOffset()) + passenger.getBbHeight();
    }
}
