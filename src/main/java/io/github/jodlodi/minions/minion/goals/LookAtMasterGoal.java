package io.github.jodlodi.minions.minion.goals;

import io.github.jodlodi.minions.minion.Minion;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class LookAtMasterGoal extends Goal {
    protected final Minion mob;
    @Nullable
    protected LivingEntity lookAt;
    protected final float lookDistance = 7.0F;

    public LookAtMasterGoal(Minion minion) {
        this.mob = minion;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.mob.isOwnerHoldingStaff()) this.lookAt = this.mob.getOwner();

        return this.lookAt != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.lookAt == null || !this.lookAt.isAlive() || !this.mob.isOwnerHoldingStaff()) {
            return false;
        } else {
            return !(this.mob.distanceToSqr(this.lookAt) > (double)(this.lookDistance * this.lookDistance));
        }
    }

    @Override
    public void stop() {
        this.lookAt = null;
    }

    @Override
    public void tick() {
        if (this.lookAt != null && this.lookAt.isAlive()) {
            this.mob.getLookControl().setLookAt(this.lookAt.getX(), this.lookAt.getEyeY(), this.lookAt.getZ());
        }
    }
}