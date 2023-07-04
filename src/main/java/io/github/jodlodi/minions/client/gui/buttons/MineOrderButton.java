package io.github.jodlodi.minions.client.gui.buttons;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jodlodi.minions.MinUtil;
import io.github.jodlodi.minions.client.gui.MastersStaffScreen;
import io.github.jodlodi.minions.network.MineAheadButtonPacket;
import io.github.jodlodi.minions.registry.PacketRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MineOrderButton extends AbstractAdjustableOrderButton {
    protected int depth;
    protected int scale;

    public MineOrderButton(int x, int y, MastersStaffScreen.BlockStaffScreen screen) {
        super(x, y, screen, true, true, true);
        this.depth = 16;
        this.scale = 1;
    }

    @Override
    public MastersStaffScreen.BlockStaffScreen getScreen() {
        return (MastersStaffScreen.BlockStaffScreen)this.screen;
    }

    @Override
    protected void renderBackground(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, MastersStaffScreen.LOCATION);
        this.blit(stack, this.x, this.y, 92, 0, this.width, this.height);
    }

    @Override
    protected void renderFrame(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, MastersStaffScreen.LOCATION);
        this.blit(stack, this.x, this.y, 92, 19, this.width, this.height);
    }

    @Override
    protected void renderIcon(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, MastersStaffScreen.LOCATION);
        this.blit(stack, this.x, this.y, 92, 38, this.width, this.height);
    }

    @Override
    public void onPress() {
        MastersStaffScreen.BlockStaffScreen screen = this.getScreen();

        Direction dir = screen.getContext().getDirection().getOpposite();
        Direction.Axis clockAxis = dir.getClockWise().getAxis();

        BlockPos context = screen.getContext().getBlockPos();
        BlockPos minPos = this.scale == 0 ? context.below() : context.below(this.scale).relative(clockAxis, -this.scale);
        BlockPos maxPos = this.scale == 0 ? new BlockPos(context) : context.above(this.scale).relative(clockAxis, this.scale);

        PacketRegistry.CHANNEL.sendToServer(new MineAheadButtonPacket(this.screen.isShiftKeyDown(), minPos, maxPos, dir, this.depth));

        this.screen.onClose();
    }

    @Override
    public void onSelectedTick() {
        MastersStaffScreen.BlockStaffScreen screen = this.getScreen();
        LocalPlayer player = screen.getPlayer();
        ClientLevel level = player.clientLevel;

        Direction dir = screen.getContext().getDirection();
        Direction.Axis clockAxis = dir.getClockWise().getAxis();

        BlockPos context = screen.getContext().getBlockPos();
        BlockPos minPos = this.scale == 0 ? context.below() : context.below(this.scale).relative(clockAxis, -this.scale);
        BlockPos maxPos = this.scale == 0 ? new BlockPos(context) : context.above(this.scale).relative(clockAxis, this.scale);

        for (BlockPos pos : BlockPos.betweenClosed(minPos, maxPos)) {
            if (player.getRandom().nextInt(3) == 0) {
                double randXZ = player.getRandom().nextDouble() - 0.5D;
                double randY = player.getRandom().nextDouble();

                Vec3 vec3 = Vec3.atBottomCenterOf(pos).add(clockAxis == Direction.Axis.Z ? 0.55D * dir.getStepX() : randXZ, randY, clockAxis == Direction.Axis.X ? 0.55D * dir.getStepZ() : randXZ);
                level.addParticle(ParticleTypes.SMOKE, vec3.x, vec3.y, vec3.z, 0, 0, 0);
            }
        }

        boolean axis = dir.getAxis() == Direction.Axis.Z;
        int mul = (axis ? dir.getStepZ() : dir.getStepX()) == 1 ? 1 : 0;
        MinUtil.particleAtWall(player.getRandom(), (axis ? minPos.getZ() : minPos.getX()) + mul, level, axis, axis ? minPos.getX() : minPos.getZ(), (axis ? maxPos.getX() : maxPos.getZ()) + 1, minPos.getY(), maxPos.getY() + 1);
    }

    @Override
    protected void onLeftPress() {
        this.depth = Math.max(1, this.depth - (this.screen.isShiftKeyDown() ? 5 : 1));
    }

    @Override
    protected void onTogglePress() {
        this.scale = (this.scale + (this.screen.isShiftKeyDown() ? 2 : 1)) % 3;
    }

    @Override
    protected void onRightPress() {
        this.depth = Math.min(Integer.MAX_VALUE, this.depth + (this.screen.isShiftKeyDown() ? 5 : 1));
    }

    @Override
    protected MutableComponent getName() {
        return Component.literal("Mine Mineshaft").withStyle(ChatFormatting.BLUE);
    }

    @Override
    protected List<MutableComponent> getAdjustableTooltip() {
        return List.of(
                Component.literal("Depth: " + this.depth).withStyle(ChatFormatting.GRAY),
                Component.literal(this.scale == 0 ? "Small mine" : this.scale == 1 ? "Medium mine" : "Large mine").withStyle(ChatFormatting.GRAY));
    }
}
