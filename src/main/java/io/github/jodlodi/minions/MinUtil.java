package io.github.jodlodi.minions;

import io.github.jodlodi.minions.minion.Minion;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MinUtil {
    public static final BlockState BLACKSTONE = Blocks.BLACKSTONE.defaultBlockState();
    public static final BlockState GILDED_BLACKSTONE = Blocks.GILDED_BLACKSTONE.defaultBlockState();

    public static void msg(String msg) {
        msg(msg, true);
    }

    public static void msg(String msg, boolean fancy) {
        if (Minecraft.getInstance().cameraEntity instanceof LocalPlayer player) {
            player.displayClientMessage(Component.literal(msg), fancy);
        }
    }

    @Nullable
    public static BlockPos randomOpenNearbyOrAboveOrBelow(BlockPos center, LevelAccessor level) {
        BlockPos pos = randomOpenNearby(center, level);
        if (pos != null) return pos;
        pos = randomOpenNearby(center.above(), level);
        if (pos != null) return pos;
        pos = randomOpenNearby(center.below(), level);
        return pos;
    }

    @Nullable
    public static BlockPos randomOpenNearby(BlockPos center, LevelAccessor level) {
        List<BlockPos> possiblePositions = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            if (direction.getAxis() != Direction.Axis.Y) {
                BlockPos testPos = center.relative(direction);
                if (!level.getBlockState(testPos).getMaterial().isSolid()) {
                    BlockPos below = testPos.below();
                    if (level.getBlockState(below).isSolidRender(level, below)) {
                        possiblePositions.add(testPos);
                    }
                }
            }
        }

        if (possiblePositions.isEmpty()) return null;
        return possiblePositions.get(level.getRandom().nextInt(possiblePositions.size()));
    }

    public static boolean isBlockBreakable(BlockState state, BlockGetter level, BlockPos pos, Entity destroyer) {
        return !state.isAir() && state.getDestroySpeed(level, pos) >= 0.0F && !(state.getBlock() instanceof LiquidBlock) && state.getBlock().canEntityDestroy(state, level, pos, destroyer);
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

    public static int getColor(CompoundTag tag) {
        if (tag.contains("Color")) return tag.getInt("Color");
        return Minion.DEFAULT_RED;
    }

    public static int dye(DyeItem dyeitem, int currentColor) {
        int[] rgb = new int[3];
        int i = 0;
        int dev = 2;

        float f = (float) (currentColor >> 16 & 255) / 255.0F;
        float f1 = (float) (currentColor >> 8 & 255) / 255.0F;
        float f2 = (float) (currentColor & 255) / 255.0F;
        i += (int) (Math.max(f, Math.max(f1, f2)) * 255.0F);
        rgb[0] += (int) (f * 255.0F);
        rgb[1] += (int) (f1 * 255.0F);
        rgb[2] += (int) (f2 * 255.0F);

        float[] frgb = dyeitem.getDyeColor().getTextureDiffuseColors();
        int i2 = (int) (frgb[0] * 255.0F);
        int l = (int) (frgb[1] * 255.0F);
        int i1 = (int) (frgb[2] * 255.0F);
        i += Math.max(i2, Math.max(l, i1));
        rgb[0] += i2;
        rgb[1] += l;
        rgb[2] += i1;

        int r = rgb[0] / dev;
        int g = rgb[1] / dev;
        int b = rgb[2] / dev;
        float f3 = (float) i / (float) dev;
        float f4 = (float) Math.max(r, Math.max(g, b));
        r = (int) ((float) r * f3 / f4);
        g = (int) ((float) g * f3 / f4);
        b = (int) ((float) b * f3 / f4);
        int irgb = (r << 8) + g;
        irgb = (irgb << 8) + b;

        return irgb;
    }

    public static AABB getRidingAABB(Mob minion) {
        AABB aabb = minion.getBoundingBox();
        if (!minion.getPassengers().isEmpty()) {
            Entity passenger = minion.getPassengers().get(0);
            aabb = aabb.minmax(passenger.getBoundingBox());
        }
        return aabb;
    }

    public static void blockLiquids(BlockPos pos, Level level) {
        for (Direction direction : Direction.values()) {
            if (direction != Direction.UP) {
                BlockPos relative = pos.relative(direction);
                FluidState state = level.getFluidState(relative);
                if (state != Fluids.EMPTY.defaultFluidState() && state.isSource() && level.getBlockState(relative).getMaterial().isReplaceable()) {
                    MinUtil.stoneUp(relative, level);
                }
            }
        }
    }

    public static void stoneUp(BlockPos pos, Level level) {
        level.setBlock(pos, level.random.nextInt(100) == 1 ? GILDED_BLACKSTONE : BLACKSTONE, 3);
    }
}
