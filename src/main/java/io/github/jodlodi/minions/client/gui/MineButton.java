package io.github.jodlodi.minions.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jodlodi.minions.MinUtil;
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
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MineButton extends AdjustableMastersButton {
    protected int depth;
    protected boolean small;

    public MineButton(int x, int y, MastersStaffScreen.BlockStaffScreen screen) {
        super(x, y, screen, true, true);
        this.depth = 16;
        this.small = false;
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
        BlockPos minPos = this.small ? context.below() : context.below().relative(clockAxis, -1);
        BlockPos maxPos = this.small ? new BlockPos(context) : context.above().relative(clockAxis, 1);

        PacketRegistry.CHANNEL.sendToServer(new MineAheadButtonPacket(minPos, maxPos, dir, this.depth));

        this.screen.onClose();
    }

    @Override
    public void onSelectedTick(TickEvent.ClientTickEvent event) {
        MastersStaffScreen.BlockStaffScreen screen = this.getScreen();
        LocalPlayer player = screen.getPlayer();
        ClientLevel level = player.clientLevel;

        Direction dir = screen.getContext().getDirection();
        Direction.Axis clockAxis = dir.getClockWise().getAxis();

        BlockPos context = screen.getContext().getBlockPos();
        BlockPos minPos = this.small ? context.below() : context.below().relative(clockAxis, -1);
        BlockPos maxPos = this.small ? new BlockPos(context) : context.above().relative(clockAxis, 1);

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
    protected List<? extends FormattedCharSequence> getTooltip() {
        return List.of(
                Component.literal("Mine Mineshaft").withStyle(ChatFormatting.BLUE).getVisualOrderText(),
                Component.literal("Depth: " + this.depth).withStyle(ChatFormatting.GRAY).getVisualOrderText(),
                Component.literal(this.small ? "Small mine" : "Large mine").withStyle(ChatFormatting.GRAY).getVisualOrderText());
    }

    @Override
    protected void onLeftPress() {
        if (this.depth > 1) this.depth--;
    }

    @Override
    protected void onTogglePress() {
        this.small = !this.small;
    }

    @Override
    protected void onRightPress() {
        this.depth++;
    }
}
