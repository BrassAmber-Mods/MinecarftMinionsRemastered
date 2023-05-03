package io.github.jodlodi.minions.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jodlodi.minions.network.ContainerBlockPacket;
import io.github.jodlodi.minions.network.ContainerEntityPacket;
import io.github.jodlodi.minions.registry.PacketRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ContainerButton extends MastersButton {
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

    protected void renderBackground(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, MastersStaffScreen.LOCATION);
        this.blit(stack, this.x, this.y, 22, 138, this.width, this.height);
    }

    protected void renderIcon(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, MastersStaffScreen.LOCATION);
        this.blit(stack, this.x, this.y, 43, 138, this.width, this.height);
    }

    @Override
    protected List<? extends FormattedCharSequence> getTooltip() {
        return List.of(Component.literal("Set Container").withStyle(ChatFormatting.BLUE).getVisualOrderText());
    }
}
