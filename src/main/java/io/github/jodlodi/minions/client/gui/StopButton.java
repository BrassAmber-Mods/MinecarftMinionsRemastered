package io.github.jodlodi.minions.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jodlodi.minions.network.StopButtonPacket;
import io.github.jodlodi.minions.registry.PacketRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class StopButton extends MastersButton {

    public StopButton(int x, int y, MastersStaffScreen.BlockStaffScreen screen) {
        super(x, y, screen);
    }

    @Override
    public void onPress() {
        PacketRegistry.CHANNEL.sendToServer(new StopButtonPacket());
        this.screen.onClose();
    }

    @Override
    public MastersStaffScreen.BlockStaffScreen getScreen() {
        return (MastersStaffScreen.BlockStaffScreen)this.screen;
    }

    @Override
    protected void renderBackground(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, MastersStaffScreen.LOCATION);
        this.blit(stack, this.x, this.y, 64, 117, this.width, this.height);
    }

    @Override
    protected void renderIcon(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, MastersStaffScreen.LOCATION);
        this.blit(stack, this.x, this.y, 85, 117, this.width, this.height);
        super.renderIcon(stack, mouseX, mouseY, partialTick);
    }

    @Override
    public void onSelectedTick(TickEvent.ClientTickEvent event) {

    }

    @Override
    protected List<? extends FormattedCharSequence> getTooltip() {
        return List.of(Component.literal("Stop order").withStyle(ChatFormatting.DARK_RED).getVisualOrderText());
    }
}
