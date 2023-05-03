package io.github.jodlodi.minions.orders;

import io.github.jodlodi.minions.capabilities.IMasterCapability;
import io.github.jodlodi.minions.minion.FollowOrderGoal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public abstract class AbstractOrder {

    abstract public ResourceLocation getID();

    abstract public boolean goalCanContinueToUse(FollowOrderGoal goal);

    abstract public void goalStart(FollowOrderGoal goal);

    abstract public void goalStop(FollowOrderGoal goal);

    abstract public void goalTick(FollowOrderGoal goal);

    abstract public void tick(IMasterCapability masterCapability, Player player, Level level);

    abstract public @NotNull CompoundTag serialize();
}
