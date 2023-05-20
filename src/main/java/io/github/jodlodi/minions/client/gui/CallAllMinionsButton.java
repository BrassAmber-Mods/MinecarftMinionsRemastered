package io.github.jodlodi.minions.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jodlodi.minions.capabilities.IMasterCapability;
import io.github.jodlodi.minions.network.BanishPacket;
import io.github.jodlodi.minions.network.SummonPacket;
import io.github.jodlodi.minions.registry.PacketRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CallAllMinionsButton extends MastersButton {
    protected final IMasterCapability capability;

    public CallAllMinionsButton(int x, int y, MastersStaffScreen.BlockStaffScreen screen, IMasterCapability capability) {
        super(x, y, screen);
        this.capability = capability;
    }

    @Override
    public void onPress() {
        BlockPos pos = this.getScreen().getContext().getBlockPos();
        for (UUID uuid : this.capability.getMinions()) {
            if (uuid != null) {
                int index = this.capability.getMinions().indexOf(uuid);
                PacketRegistry.CHANNEL.sendToServer(new BanishPacket(uuid));
                PacketRegistry.CHANNEL.sendToServer(new SummonPacket(pos, index));
            }
        }
        this.screen.onClose();
    }

    @Override
    public MastersStaffScreen.BlockStaffScreen getScreen() {
        return (MastersStaffScreen.BlockStaffScreen)this.screen;
    }

    @Override
    protected void renderBackground(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, MastersStaffScreen.LOCATION);
        this.blit(stack, this.x, this.y, 149, 0, this.width, this.height);
    }

    @Override
    protected void renderFrame(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, MastersStaffScreen.LOCATION);
        this.blit(stack, this.x, this.y, 149, 19, this.width, this.height);
    }

    @Override
    protected void renderIcon(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, MastersStaffScreen.LOCATION);
        this.blit(stack, this.x, this.y, 149, 38, this.width, this.height);
    }

    @Override
    protected List<? extends FormattedCharSequence> getTooltip() {
        return List.of(Component.literal("Call Minions").withStyle(ChatFormatting.BLUE).getVisualOrderText());
    }
}
