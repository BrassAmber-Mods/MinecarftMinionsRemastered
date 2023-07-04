package io.github.jodlodi.minions.client.gui.buttons;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jodlodi.minions.MinUtil;
import io.github.jodlodi.minions.client.gui.MastersStaffScreen;
import io.github.jodlodi.minions.network.HoeWartButtonPacket;
import io.github.jodlodi.minions.orders.HoeWartOrder;
import io.github.jodlodi.minions.registry.PacketRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class HoeWartButton extends AbstractAdjustableOrderButton {
    protected int range;
    protected boolean allWart;

    public HoeWartButton(int x, int y, MastersStaffScreen.BlockStaffScreen screen) {
        super(x, y, screen, true, true, true);
        this.range = 12;
        this.allWart = true;
    }

    @Override
    public MastersStaffScreen.BlockStaffScreen getScreen() {
        return (MastersStaffScreen.BlockStaffScreen)this.screen;
    }

    @Override
    protected void renderBackground(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, MastersStaffScreen.LOCATION);
        this.blit(stack, this.x, this.y, 111, 57, this.width, this.height);
    }

    @Override
    protected void renderFrame(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, MastersStaffScreen.LOCATION);
        this.blit(stack, this.x, this.y, 111, 76, this.width, this.height);
    }

    @Override
    protected void renderIcon(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, MastersStaffScreen.LOCATION);
        this.blit(stack, this.x, this.y, 111, 95, this.width, this.height);
    }

    @Override
    public void onPress() {
        MastersStaffScreen.BlockStaffScreen screen = this.getScreen();
        LocalPlayer player = screen.getPlayer();
        float partialTick = Minecraft.getInstance().getPartialTick();

        int offset = this.range / 2;
        int trueSet = (this.range + 1) % 2;

        boolean south =  Direction.SOUTH.isFacingAngle(player.getViewYRot(partialTick));
        boolean east =  Direction.EAST.isFacingAngle(player.getViewYRot(partialTick));

        BlockPos context = screen.getContext().getBlockPos();
        BlockPos minPos = context.north(offset - (south ? trueSet : 0)).west(offset - (east ? trueSet : 0)).below(16);
        BlockPos maxPos = context.south(offset - (!south ? trueSet : 0)).east(offset - (!east ? trueSet : 0)).above(16);

        PacketRegistry.CHANNEL.sendToServer(new HoeWartButtonPacket(this.screen.isShiftKeyDown(), minPos, maxPos, this.allWart ? null : this.getScreen().target.getBlock()));

        this.screen.onClose();
    }

    @Override
    public void onSelectedTick() {
        MastersStaffScreen.BlockStaffScreen screen = this.getScreen();
        LocalPlayer player = screen.getPlayer();
        float partialTick = Minecraft.getInstance().getPartialTick();
        ClientLevel level = player.clientLevel;
        int offset = this.range / 2;
        int trueSet = (this.range + 1) % 2;

        boolean south =  Direction.SOUTH.isFacingAngle(player.getViewYRot(partialTick));
        boolean east =  Direction.EAST.isFacingAngle(player.getViewYRot(partialTick));

        BlockPos context = screen.getContext().getBlockPos();
        BlockPos minPos = context.north(offset - (south ? trueSet : 0)).west(offset - (east ? trueSet : 0)).below(16);
        BlockPos maxPos = context.south(offset - (!south ? trueSet : 0)).east(offset - (!east ? trueSet : 0)).above(16);

        MinUtil.particlesAroundTag(player.getRandom(), level, minPos, maxPos, this.allWart ? HoeWartOrder.DEFAULT_CHOP : (blockState) -> blockState.is(this.getScreen().target.getBlock()));
    }

    @Override
    protected void onLeftPress() {
        this.range = Math.max(1, this.range - (this.screen.isShiftKeyDown() ? 5 : 1));
    }

    @Override
    protected void onTogglePress() {
        this.allWart = !this.allWart;
    }

    @Override
    protected void onRightPress() {
        this.range = Math.min(32, this.range + (this.screen.isShiftKeyDown() ? 5 : 1));
    }

    @Override
    protected MutableComponent getName() {
        return Component.literal("Harvest wart").withStyle(ChatFormatting.BLUE);
    }

    @Override
    protected List<MutableComponent> getAdjustableTooltip() {
        return List.of(
                Component.literal("Range: " + this.range).withStyle(ChatFormatting.GRAY),
                Component.literal(this.allWart ? "Harvest all wart" : "Only harvest " + this.getScreen().target.getBlock().getName().getString()).withStyle(ChatFormatting.GRAY));
    }
}
