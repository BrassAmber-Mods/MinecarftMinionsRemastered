package io.github.jodlodi.minions;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MinUtil {
    public static void msg(String msg) {
        msg(msg, true);
    }

    public static void msg(String msg, boolean fancy) {
        if (Minecraft.getInstance().cameraEntity instanceof LocalPlayer player) {
            player.displayClientMessage(Component.literal(msg), fancy);
        }
    }

    @Nullable
    public static BlockPos randomOpenNearby(BlockPos center, LevelAccessor level) {
        List<BlockPos> possiblePositions = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            if (direction.getAxis() != Direction.Axis.Y) {
                BlockPos testPos = center.relative(direction);
                if (level.getBlockState(testPos).isAir()) {
                    possiblePositions.add(testPos);
                }
            }
        }

        if (possiblePositions.isEmpty()) return null;
        return possiblePositions.get(level.getRandom().nextInt(possiblePositions.size()));
    }

    public static void particleAtBorders(RandomSource random, double y, Level level, int minX, int maxX, int minZ, int maxZ) {
        level.addAlwaysVisibleParticle(ParticleTypes.SMOKE, minX, y, minZ, 0.0D, 0.0D, 0.0D);
        level.addAlwaysVisibleParticle(ParticleTypes.SMOKE, minX, y, maxZ, 0.0D, 0.0D, 0.0D);
        level.addAlwaysVisibleParticle(ParticleTypes.SMOKE, maxX, y, minZ, 0.0D, 0.0D, 0.0D);
        level.addAlwaysVisibleParticle(ParticleTypes.SMOKE, maxX, y, maxZ, 0.0D, 0.0D, 0.0D);

        for (int x = minX; x <= maxX - 1; x++) {
            level.addAlwaysVisibleParticle(ParticleTypes.SMOKE, x + random.nextDouble(), y, minZ, 0.0D, 0.0D, 0.0D);
            level.addAlwaysVisibleParticle(ParticleTypes.SMOKE, x + random.nextDouble(), y, maxZ, 0.0D, 0.0D, 0.0D);
        }

        for (int z = minZ; z <= maxZ - 1; z++) {
            level.addAlwaysVisibleParticle(ParticleTypes.SMOKE, minX, y, z + random.nextDouble(), 0.0D, 0.0D, 0.0D);
            level.addAlwaysVisibleParticle(ParticleTypes.SMOKE, maxX, y, z + random.nextDouble(), 0.0D, 0.0D, 0.0D);
        }
    }

    //TODO
    private static final ItemStack PICKAXE = Items.NETHERITE_PICKAXE.getDefaultInstance();
    private static final ItemStack AXE = Items.NETHERITE_AXE.getDefaultInstance();
    private static final ItemStack SHOVEL = Items.NETHERITE_SHOVEL.getDefaultInstance();
    private static final ItemStack HOE = Items.NETHERITE_HOE.getDefaultInstance();
    private static final ItemStack SWORD = Items.NETHERITE_SWORD.getDefaultInstance();
    //TODO

    public static ItemStack bestToolForBlock(BlockState state) {
        return Stream.of(PICKAXE, AXE, SHOVEL, HOE, SWORD).max(Comparator.comparingDouble(e -> e.getItem().getDestroySpeed(e, state))).orElse(PICKAXE);
    }
}
