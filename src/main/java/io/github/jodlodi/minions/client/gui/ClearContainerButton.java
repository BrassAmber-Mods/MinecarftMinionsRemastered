package io.github.jodlodi.minions.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jodlodi.minions.network.ContainerClearPacket;
import io.github.jodlodi.minions.registry.PacketRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ClearContainerButton extends MastersButton {
    public ClearContainerButton(int x, int y, MastersStaffScreen screen) {
        super(x, y, screen);
    }

    @Override
    public void onPress() {
        PacketRegistry.CHANNEL.sendToServer(new ContainerClearPacket());
        this.screen.onClose();
    }

    protected void renderBackground(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, MastersStaffScreen.LOCATION);
        this.blit(stack, this.x, this.y, 64, 159, this.width, this.height);
    }

    protected void renderIcon(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, MastersStaffScreen.LOCATION);
        this.blit(stack, this.x, this.y, 85, 159, this.width, this.height);
    }

    @Override
    protected List<? extends FormattedCharSequence> getTooltip() {
        return List.of(Component.literal("Remove Container").withStyle(ChatFormatting.BLUE).getVisualOrderText());
    }
}
