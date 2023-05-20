package io.github.jodlodi.minions.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jodlodi.minions.MinUtil;
import io.github.jodlodi.minions.network.MineDownButtonPacket;
import io.github.jodlodi.minions.registry.PacketRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
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
public class DigButton extends AdjustableMastersButton {
    protected int size;
    protected boolean stairs;

    public DigButton(int x, int y, MastersStaffScreen.BlockStaffScreen screen) {
        super(x, y, screen, true, true);
        this.size = 7;
        this.stairs = true;
    }

    @Override
    public MastersStaffScreen.BlockStaffScreen getScreen() {
        return (MastersStaffScreen.BlockStaffScreen)this.screen;
    }

    @Override
    protected void renderBackground(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, MastersStaffScreen.LOCATION);
        this.blit(stack, this.x, this.y, 73, 0, this.width, this.height);
    }

    @Override
    protected void renderFrame(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, MastersStaffScreen.LOCATION);
        this.blit(stack, this.x, this.y, 73, 19, this.width, this.height);
    }

    @Override
    protected void renderIcon(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, MastersStaffScreen.LOCATION);
        this.blit(stack, this.x, this.y, 73, 38, this.width, this.height);
    }

    @Override
    public void onPress() {
        MastersStaffScreen.BlockStaffScreen screen = this.getScreen();
        LocalPlayer player = screen.getPlayer();
        float partialTick = Minecraft.getInstance().getPartialTick();

        int offset = this.size / 2;
        int trueSet = (this.size + 1) % 2;

        boolean south =  Direction.SOUTH.isFacingAngle(player.getViewYRot(partialTick));
        boolean east =  Direction.EAST.isFacingAngle(player.getViewYRot(partialTick));

        BlockPos context = screen.getContext().getBlockPos();
        BlockPos minPos = context.north(offset - (south ? trueSet : 0)).west(offset - (east ? trueSet : 0));
        BlockPos maxPos = context.south(offset - (!south ? trueSet : 0)).east(offset - (!east ? trueSet : 0));

        PacketRegistry.CHANNEL.sendToServer(new MineDownButtonPacket(minPos, maxPos, player.getDirection().getOpposite(), this.stairs));

        this.screen.onClose();
    }

    @Override
    public void onSelectedTick(TickEvent.ClientTickEvent event) {
        MastersStaffScreen.BlockStaffScreen screen = this.getScreen();
        LocalPlayer player = screen.getPlayer();
        float partialTick = Minecraft.getInstance().getPartialTick();
        ClientLevel level = player.clientLevel;
        int offset = this.size / 2;
        int trueSet = (this.size + 1) % 2;

        boolean south =  Direction.SOUTH.isFacingAngle(player.getViewYRot(partialTick));
        boolean east =  Direction.EAST.isFacingAngle(player.getViewYRot(partialTick));

        BlockPos minPos = screen.getContext().getBlockPos().north(offset - (south ? trueSet : 0)).west(offset - (east ? trueSet : 0));
        BlockPos maxPos = screen.getContext().getBlockPos().south(offset - (!south ? trueSet : 0)).east(offset - (!east ? trueSet : 0));

        for (BlockPos pos : BlockPos.betweenClosed(minPos, maxPos)) {
            if (level.getBlockState(pos).getMaterial().isReplaceable()) pos = pos.below();
            if (level.getBlockState(pos).getMaterial().isReplaceable()) pos = pos.below();

            if (player.getRandom().nextInt(3) == 0) {
                double randX = player.getRandom().nextDouble();
                double randZ = player.getRandom().nextDouble();

                Vec3 vec3 = Vec3.atLowerCornerOf(pos).add(randX, 1.05D, randZ);
                level.addParticle(ParticleTypes.SMOKE, vec3.x, vec3.y, vec3.z, 0, 0, 0);
            }
        }

        MinUtil.particleAtBorders(player.getRandom(), maxPos.getY() + 1.1D, level, minPos.getX(), maxPos.getX() + 1, minPos.getZ(), maxPos.getZ() + 1);
    }

    @Override
    protected List<? extends FormattedCharSequence> getTooltip() {
        return List.of(
                Component.literal("Dig Mineshaft").withStyle(ChatFormatting.BLUE).getVisualOrderText(),
                Component.literal("Size: " + this.size).withStyle(ChatFormatting.GRAY).getVisualOrderText(),
                Component.literal(this.stairs ? "With stairs" : "Without stairs").withStyle(ChatFormatting.GRAY).getVisualOrderText());
    }

    @Override
    protected void onLeftPress() {
        if (this.size > 1) this.size--;
    }

    @Override
    protected void onTogglePress() {
        this.stairs = !this.stairs;
    }

    @Override
    protected void onRightPress() {
        this.size++;
    }
}
