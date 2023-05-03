package io.github.jodlodi.minions;

import io.github.jodlodi.minions.event.UseStaffEvent;
import io.github.jodlodi.minions.minion.Minion;
import io.github.jodlodi.minions.registry.CommonRegistry;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MastersStaff extends Item {
    public MastersStaff(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext onContext) {
        Player player = onContext.getPlayer();
        if (player != null) {
            player.getCapability(CommonRegistry.MASTER_CAPABILITY).ifPresent(capability -> {
                player.getCooldowns().addCooldown(this, 10);
                MinecraftForge.EVENT_BUS.post(new UseStaffEvent(player, this.contextToHitResult(onContext)));

                if (true) return;//TODO

                if ((capability.minionCount() < 4 || player.isSecondaryUseActive()) && !player.getOffhandItem().is(Items.STICK)) {
                    if (player.level instanceof ServerLevel serverLevel) {
                        if (player.isSecondaryUseActive()) {
                            if (capability.getOrder() != null) capability.setOrder(null);
                            else capability.releaseMinions(4);
                        } else {
                            BlockPos bPos = onContext.getClickedPos();
                            if (!player.level.getBlockState(bPos).getMaterial().isReplaceable()) bPos = bPos.above();
                            if (player.level.getBlockState(bPos).getMaterial().isReplaceable()) {
                                Minion minion = new Minion(serverLevel);
                                minion.setOwnerUUID(player.getUUID());
                                Vec3 pos = Vec3.atBottomCenterOf(bPos);
                                minion.moveTo(pos.x, pos.y, pos.z, player.getYRot(), 0.0F);
                                capability.addMinion(minion.getUUID());
                                serverLevel.addFreshEntity(minion);
                                capability.finalizeMinion(minion);
                            }
                        }
                    }
                } else if (player.level.isClientSide) {
                    MinecraftForge.EVENT_BUS.post(new UseStaffEvent(player, this.contextToHitResult(onContext)));
                }
            });
        }

        return InteractionResult.SUCCESS;
    }

    /*@Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (level instanceof ServerLevel serverLevel && false) {
            player.getCapability(CommonRegistry.MASTER_CAPABILITY).ifPresent(capability -> {
                if (capability.minionCount() < 4) {
                    Minion minion = new Minion(level);
                    minion.setOwnerUUID(player.getUUID());
                    minion.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0.0F);
                    capability.addMinion(minion.getUUID());
                    level.addFreshEntity(minion);
                } else {
                    for (Entity entity : serverLevel.getAllEntities()) {
                        if (entity instanceof Minion minion && minion.getOwner() == player) minion.release();
                    }
                }
            });
        }
        return InteractionResultHolder.success(itemstack);
    }*/

    protected BlockHitResult contextToHitResult(UseOnContext context) {
        return new BlockHitResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), false);
    }
}
