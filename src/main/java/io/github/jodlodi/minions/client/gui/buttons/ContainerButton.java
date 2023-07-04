package io.github.jodlodi.minions.client.gui.buttons;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jodlodi.minions.client.gui.MastersStaffScreen;
import io.github.jodlodi.minions.network.ContainerBlockPacket;
import io.github.jodlodi.minions.network.ContainerEntityPacket;
import io.github.jodlodi.minions.registry.PacketRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ContainerButton extends AbstractMastersButton {
    public ContainerButton(int x, int y, MastersStaffScreen screen) {
        super(x, y, screen);
    }

    @Override
    public void onPress() {
        if (this.getScreen().getContext() instanceof BlockHitResult blockHitResult) {
            PacketRegistry.CHANNEL.sendToServer(new ContainerBlockPacket(blockHitResult.getBlockPos()));
        } else if (this.getScreen().getContext() instanceof EntityHitResult entityHitResult) {
            PacketRegistry.CHANNEL.sendToServer(new ContainerEntityPacket(entityHitResult.getEntity().getUUID()));
        }
        this.screen.onClose();
    }

    @Override
    protected void renderBackground(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, MastersStaffScreen.LOCATION);
        this.blit(stack, this.x, this.y, 168, 0, this.width, this.height);
    }

    @Override
    protected void renderFrame(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, MastersStaffScreen.LOCATION);
        this.blit(stack, this.x, this.y, 168, 19, this.width, this.height);
    }

    @Override
    protected void renderIcon(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, MastersStaffScreen.LOCATION);
        this.blit(stack, this.x, this.y, 168, 38, this.width, this.height);
    }

    @Override
    protected MutableComponent getName() {
        return Component.literal("Set Container").withStyle(ChatFormatting.BLUE);
    }
}
