package io.github.jodlodi.minions.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MastersButton extends Button {
    protected final MastersStaffScreen screen;

    public MastersButton(int x, int y, MastersStaffScreen screen) {
        super((screen.width - 19) / 2 + x, (screen.height - 19) / 2 + y, 19, 19, CommonComponents.EMPTY, (button) -> {});
        this.screen = screen;
    }

    @Override
    public abstract void onPress();

    public MastersStaffScreen getScreen() {
        return this.screen;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
    }

    @Override
    public void renderButton(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.renderBackground(stack, mouseX, mouseY, partialTick);
        this.renderFrame(stack, mouseX, mouseY, partialTick);
        this.renderIcon(stack, mouseX, mouseY, partialTick);
        if (this.isHoveredOrFocused()) this.renderToolTip(stack, mouseX, mouseY);
    }

    protected void renderBackground(PoseStack stack, int mouseX, int mouseY, float partialTick) {

    }

    protected void renderFrame(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, MastersStaffScreen.LOCATION);
        this.blit(stack, this.x, this.y, 1, 117, this.width, this.height);
    }

    protected void renderIcon(PoseStack stack, int mouseX, int mouseY, float partialTick) {

    }

    @Override
    public void renderToolTip(PoseStack stack, int mouseX, int mouseY) {
        this.screen.renderTooltip(stack, this.getTooltip(), mouseX, mouseY);
    }

    abstract protected List<? extends FormattedCharSequence> getTooltip();

     public void onSelectedTick(TickEvent.ClientTickEvent event) {

    }
}
