package io.github.jodlodi.minions.minion;

import io.github.jodlodi.minions.MinUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MinionNavigation extends GroundPathNavigation {
    public Vec3 lastPos = Vec3.ZERO;
    public BlockPos lastBlockPos = BlockPos.ZERO;

    public MinionNavigation(Minion minion, Level level) {
        super(minion, level);
    }

    protected Minion getMinion() {
        return (Minion)this.mob;
    }

    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        this.nodeEvaluator = new MinionNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true);
        return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
    }

    @Override
    public void tick() {
        Minion minion = this.getMinion();
        if (minion.sittingOrRiding()) return;
        if (minion.canBlink() && minion.isEnactingOrder()) {
            Optional.ofNullable(this.getPath()).ifPresent(path -> {
                if (this.shouldBlink(path)) {
                    Optional.ofNullable(MinUtil.randomOpenNearbyOrAboveOrBelow(path.getTarget(), level)).ifPresent(pos -> {
                        minion.blink(pos);
                        this.stop();
                    });
                }
            });
        }
        if (!this.getMinion().getBlinking()) super.tick();
    }

    protected boolean shouldBlink(Path path) { //TODO: see if theres a better way
        int roll = this.mob.getRandom().nextInt(100);
        if (!path.canReach() && roll % 25 == 1) return true;
        Vec3 pos = this.mob.position();
        BlockPos blockPos = this.mob.blockPosition();
        boolean flag = pos.equals(this.lastPos) && path.getDistToTarget() > 4.0F;
        this.lastPos = pos;
        if (!flag) flag = blockPos.equals(this.lastBlockPos) && roll == 1;
        this.lastBlockPos = blockPos;
        return flag;
    }
}
