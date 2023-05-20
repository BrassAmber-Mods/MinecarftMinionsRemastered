package io.github.jodlodi.minions.minion.goals;

import io.github.jodlodi.minions.minion.Minion;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class MinionRandomStrollGoal extends RandomStrollGoal {
    public static final float PROBABILITY = 0.001F;
    protected final float probability;

    public MinionRandomStrollGoal(Minion minion, double speed) {
        this(minion, speed, PROBABILITY);
    }

    public MinionRandomStrollGoal(Minion minion, double speed, float probability) {
        super(minion, speed);
        this.probability = probability;
    }

    @Override
    public boolean canUse() {
        return !((Minion)this.mob).isOwnerHoldingStaff() && !((Minion)this.mob).sittingOrRiding() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return !((Minion)this.mob).isOwnerHoldingStaff() && !((Minion)this.mob).sittingOrRiding() && super.canContinueToUse();
    }

    @Nullable
    protected Vec3 getPosition() {
        if (this.mob.isInWaterOrBubble()) {
            Vec3 vec3 = LandRandomPos.getPos(this.mob, 15, 7);
            return vec3 == null ? super.getPosition() : vec3;
        } else {
            return this.mob.getRandom().nextFloat() >= this.probability ? LandRandomPos.getPos(this.mob, 10, 7) : super.getPosition();
        }
    }
}