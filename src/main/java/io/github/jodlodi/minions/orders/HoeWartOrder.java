package io.github.jodlodi.minions.orders;

import io.github.jodlodi.minions.MinUtil;
import io.github.jodlodi.minions.MinionsRemastered;
import io.github.jodlodi.minions.capabilities.IMasterCapability;
import io.github.jodlodi.minions.minion.Minion;
import io.github.jodlodi.minions.minion.goals.EnactOrderGoal;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
public class HoeWartOrder extends AbstractOrder {
    public static final ResourceLocation ID = MinionsRemastered.locate("hoe_wart_order");
    public static final Predicate<BlockState> DEFAULT_CHOP = (blockState) -> blockState.is(BlockTags.WART_BLOCKS);

    //Stored variables
    private final BlockPos minPos;
    private final BlockPos maxPos;
    private final Block predicate;

    //Temp variables
    private final Map<Integer, BlockPos> mineMap = new HashMap<>();
    private final Map<Integer, Float> breakMap = new HashMap<>();

    public HoeWartOrder(BlockPos minPos, BlockPos maxPos, Block predicate) {
        this.minPos = minPos;
        this.maxPos = maxPos;
        this.predicate = predicate;
    }

    public HoeWartOrder(CompoundTag tag) {
        this.minPos = BlockPos.of(tag.getLong("min"));
        this.maxPos = BlockPos.of(tag.getLong("max"));
        this.predicate = tag.contains("predicate") ? ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryParse(tag.getString("predicate"))) : null;
    }

    @Override
    public @NotNull CompoundTag serialize() {
        return Util.make(new CompoundTag(), compoundTag -> {
            compoundTag.putLong("min", this.minPos.asLong());
            compoundTag.putLong("max", this.maxPos.asLong());
            if (this.predicate != null) compoundTag.putString("predicate", String.valueOf(ForgeRegistries.BLOCKS.getKey(this.predicate)));
        });
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean goalCanUse(EnactOrderGoal goal) {
        return true;
    }

    @Override
    public boolean goalCanContinueToUse(EnactOrderGoal goal) {
        return true;
    }

    @Override
    public void goalStart(EnactOrderGoal goal) {

    }

    @Override
    public void goalStop(EnactOrderGoal goal) {
        goal.getMinion().setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        Minion minion = goal.getMinion();
        int id = goal.getMasterCapability().getMinions().indexOf(minion.getUUID());
        this.mineMap.remove(id);
    }

    @Override
    public void goalTick(EnactOrderGoal goal) {
        Minion minion = goal.getMinion();
        int id = goal.getMasterCapability().getMinions().indexOf(minion.getUUID());
        Level level = minion.level;
        PathNavigation navigation = goal.getNavigation();
        LivingEntity owner = goal.getOwner();

        if (!this.mineMap.containsKey(id)) {
            BlockPos closest = null;
            for (BlockPos pos : BlockPos.betweenClosed(this.minPos, this.maxPos)) {
                if (this.isBlockBreakableDeluxe(level.getBlockState(pos), level, pos, owner) && (closest == null || minion.distanceToSqr(Vec3.atCenterOf(pos)) < minion.distanceToSqr(Vec3.atCenterOf(closest)))) {
                    closest = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
                }
            }

            if (closest == null) return;
            minion.setItemInHand(InteractionHand.MAIN_HAND, MinUtil.bestToolForBlock(level.getBlockState(closest)));

            this.mineMap.put(id, closest);
            this.breakMap.put(id, 0.0F);
        }

        BlockPos pos = this.mineMap.get(id);

        BlockState state = level.getBlockState(pos);
        if (minion.tickCount % 5 == 0 && !this.isBlockBreakableDeluxe(state, level, pos, owner)) {
            this.mineMap.remove(id);
            this.breakMap.put(id, 0.0F);
            return;
        }

        Vec3 center = Vec3.atBottomCenterOf(pos);
        Vec3 minionPos = minion.position();
        minion.getLookControl().setLookAt(center);

        if (MinUtil.airDistanceSqr(center, minionPos) > 4.0D) {
            this.breakMap.put(id, 0.0F);
            navigation.moveTo(center.x, center.y, center.z, goal.getSpeedModifier());
        } else if (!minion.getBlinking()) {
            minion.swing(InteractionHand.MAIN_HAND);
            navigation.stop();

            float breakProgress = this.breakMap.get(id) + (minion.getMineSpeed() / state.getDestroySpeed(level, pos) / 30.0F);

            if (breakProgress >= 1.0F) {
                this.mineMap.remove(id);
                this.breakMap.put(id, 0.0F);
                level.destroyBlock(pos, true, owner);
                navigation.stop();
            } else {
                level.destroyBlockProgress(minion.getId(), pos, (int)(breakProgress * 10.0F));
                this.breakMap.put(id, breakProgress);

                for (int i = 0; i < 4; i++) {
                    if (i != id && this.mineMap.get(i) != null && this.mineMap.get(id) != null && this.mineMap.get(i).equals(this.mineMap.get(id))) {
                        this.breakMap.put(i, breakProgress);
                    }
                }
            }
        }
    }

    protected boolean isBlockBreakableDeluxe(BlockState state, Level level, BlockPos pos, Entity destroyer) {
        return this.getPredicate().test(state) && MinUtil.isBlockBreakable(state, level, pos, destroyer);
    }

    @Override
    public void tick(IMasterCapability masterCapability, Player player, Level level) {
        if (!level.isClientSide) {
            boolean allEmpty = true;
            for (int i = 0; i < 4; i++) {
                if (this.mineMap.get(i) != null) allEmpty = false;
            }

            if (allEmpty) {
                for (BlockPos pos : BlockPos.betweenClosed(this.minPos, this.maxPos)) {
                    if (this.isBlockBreakableDeluxe(level.getBlockState(pos), level, pos, player)) {
                        return;
                    }
                }
                masterCapability.setOrder(null);
            }
        } else {
            MinUtil.particlesAroundTag(player.getRandom(), level, this.minPos, this.maxPos, this.getPredicate());
        }
    }

    public Predicate<BlockState> getPredicate() {
        return this.predicate != null ? (blockState) -> blockState.is(this.predicate) : DEFAULT_CHOP;
    }
}
