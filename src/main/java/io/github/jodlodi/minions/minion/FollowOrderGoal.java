package io.github.jodlodi.minions.minion;

import io.github.jodlodi.minions.capabilities.IMasterCapability;
import io.github.jodlodi.minions.orders.AbstractOrder;
import io.github.jodlodi.minions.registry.CommonRegistry;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumSet;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FollowOrderGoal extends Goal {
    private final Minion minion;
    private LivingEntity owner;
    private AbstractOrder order;
    private IMasterCapability masterCapability;
    private final LevelReader level;
    private final double speedModifier;
    private final PathNavigation navigation;

    public int timeToRecalcPath;
    public float oldWaterCost;

    public FollowOrderGoal(Minion minion, double speedModifier) {
        this.minion = minion;
        this.level = minion.level;
        this.speedModifier = speedModifier;
        this.navigation = minion.getNavigation();
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity living = this.minion.getOwner();
        if (living != null) {
            this.owner = living;
            LazyOptional<IMasterCapability> ma = living.getCapability(CommonRegistry.MASTER_CAPABILITY);
            if (ma.isPresent()) {
                Optional<IMasterCapability> oma = ma.resolve();
                if (oma.isPresent()) {
                    AbstractOrder order = oma.get().getOrder();
                    if (order == null) return false;
                    this.masterCapability = oma.get();
                    this.order = order;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.order == this.masterCapability.getOrder() && this.order.goalCanContinueToUse(this);
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.minion.getPathfindingMalus(BlockPathTypes.WATER);
        this.minion.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
        this.order.goalStart(this);
    }

    @Override
    public void stop() {
        this.navigation.stop();
        this.minion.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
        this.order.goalStop(this);
        this.owner = null;
        this.order = null;
        this.masterCapability = null;
    }

    @Override
    public void tick() {
        this.order.goalTick(this);
    }

    @Override
    public int adjustedTickDelay(int i) {
        return super.adjustedTickDelay(i);
    }

    public Minion getMinion() {
        return this.minion;
    }

    public LivingEntity getOwner() {
        return this.owner;
    }

    public AbstractOrder getOrder() {
        return this.order;
    }

    public IMasterCapability getMasterCapability() {
        return this.masterCapability;
    }

    public LevelReader getLevel() {
        return this.level;
    }

    public double getSpeedModifier() {
        return this.speedModifier;
    }

    public PathNavigation getNavigation() {
        return this.navigation;
    }
}