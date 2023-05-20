package io.github.jodlodi.minions.orders;

import io.github.jodlodi.minions.MinionsRemastered;
import io.github.jodlodi.minions.capabilities.IMasterCapability;
import io.github.jodlodi.minions.minion.Minion;
import io.github.jodlodi.minions.minion.goals.EnactOrderGoal;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@ParametersAreNonnullByDefault
public class CarryLivingOrder extends AbstractOrder {
    public static final ResourceLocation ID = MinionsRemastered.locate("carry_living_order");

    //Stored variables
    private final UUID target;

    public CarryLivingOrder(UUID target) {
        this.target = target;
    }

    public CarryLivingOrder(CompoundTag tag) {
        this.target = tag.getUUID("target");
    }

    @Override
    public @NotNull CompoundTag serialize() {
        return Util.make(new CompoundTag(), compoundTag -> {
            compoundTag.putUUID("target", this.target);
        });
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean goalCanUse(EnactOrderGoal goal) {
        Minion minion = goal.getMinion();
        return !minion.isVehicle();
    }

    @Override
    public boolean goalCanContinueToUse(EnactOrderGoal goal) {
        Minion minion = goal.getMinion();
        if (minion.isVehicle()) return false;
        if (minion.getLevel() instanceof ServerLevel serverLevel) return this.canCarry(serverLevel);
        return true;
    }

    @Override
    public void goalStart(EnactOrderGoal goal) {

    }

    @Override
    public void goalStop(EnactOrderGoal goal) {

    }

    @Override
    public void goalTick(EnactOrderGoal goal) {
        Minion minion = goal.getMinion();
        if (minion.getLevel() instanceof ServerLevel serverLevel) {
            PathNavigation navigation = goal.getNavigation();

            Entity victim = serverLevel.getEntity(this.target);
            if (victim == null) return;

            minion.getLookControl().setLookAt(victim);

            if (minion.distanceToSqr(victim) > 3.0D) {
                Vec3 g = victim.position();
                navigation.moveTo(g.x, g.y, g.z, goal.getSpeedModifier());
            } else if (!minion.getBlinking()) {
                victim.startRiding(minion, true);
                navigation.stop();
            }
        }
    }

    @Override
    public void tick(IMasterCapability masterCapability, Player player, Level level) {
        if (level instanceof ServerLevel serverLevel && !this.canCarry(serverLevel)) masterCapability.setOrder(null);
    }

    protected boolean canCarry(ServerLevel serverLevel) {
        Entity entity = serverLevel.getEntity(this.target);
        return entity != null && !entity.isPassenger();
    }
}
