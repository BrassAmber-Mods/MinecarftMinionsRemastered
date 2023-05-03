package io.github.jodlodi.minions.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class UseStaffEvent extends PlayerEvent {
    private final  BlockHitResult blockHitResult;

    public UseStaffEvent(Player player, BlockHitResult blockHitResult) {
        super(player);
        this.blockHitResult = blockHitResult;
    }

    public BlockHitResult getBlockHitResult() {
        return this.blockHitResult;
    }
}
