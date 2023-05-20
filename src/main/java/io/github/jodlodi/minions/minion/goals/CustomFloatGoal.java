package io.github.jodlodi.minions.minion.goals;

import io.github.jodlodi.minions.minion.Minion;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.goal.Goal;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumSet;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CustomFloatGoal extends Goal {
    protected final Minion minion;

    public CustomFloatGoal(Minion minion) {
        this.minion = minion;
        this.setFlags(EnumSet.of(Goal.Flag.JUMP));
        minion.getNavigation().setCanFloat(true);
    }

    @Override
    public boolean canUse() {
        if (this.minion.sittingOrRiding()) return false;
        return this.minion.isInWater() && this.minion.getFluidHeight(FluidTags.WATER) > this.minion.getFluidJumpThreshold() || this.minion.isInLava() || this.minion.isInFluidType((fluidType, height) -> this.minion.canSwimInFluidType(fluidType) && height > this.minion.getFluidJumpThreshold());
    }

    @Override
    public boolean canContinueToUse() {
        return !this.minion.sittingOrRiding() && super.canContinueToUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.minion.getRandom().nextFloat() < 0.8F) {
            this.minion.getJumpControl().jump();
        }
    }
}
