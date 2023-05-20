package io.github.jodlodi.minions.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jodlodi.minions.capabilities.IMasterCapability;
import io.github.jodlodi.minions.network.SummonPacket;
import io.github.jodlodi.minions.registry.PacketRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SummonButton extends MastersButton {
    protected final IMasterCapability capability;

    public SummonButton(int x, int y, MastersStaffScreen.BlockStaffScreen screen, IMasterCapability capability) {
        super(x, y, screen);
        this.capability = capability;
    }

    @Override
    public void onPress() {
        PacketRegistry.CHANNEL.sendToServer(new SummonPacket(this.getScreen().getContext().getBlockPos()));
        this.screen.onClose();
    }

    @Override
    public MastersStaffScreen.BlockStaffScreen getScreen() {
        return (MastersStaffScreen.BlockStaffScreen)this.screen;
    }

    protected void renderBackground(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, MastersStaffScreen.LOCATION);
        this.blit(stack, this.x, this.y, 111, 0, this.width, this.height);
    }

    @Override
    protected void renderFrame(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, MastersStaffScreen.LOCATION);
        this.blit(stack, this.x, this.y, 111, 19, this.width, this.height);
    }

    protected void renderIcon(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, MastersStaffScreen.LOCATION);
        this.blit(stack, this.x, this.y, 111, 38, this.width, this.height);
    }

    @Override
    protected List<? extends FormattedCharSequence> getTooltip() {
        return List.of(Component.literal("Summon Minion").withStyle(ChatFormatting.BLUE).getVisualOrderText(), Component.literal("Count: " + this.capability.minionCount()).withStyle(ChatFormatting.GRAY).getVisualOrderText());
    }
}
