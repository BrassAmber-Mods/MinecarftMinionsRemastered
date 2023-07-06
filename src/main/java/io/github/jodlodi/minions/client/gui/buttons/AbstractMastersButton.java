package io.github.jodlodi.minions.client.gui.buttons;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jodlodi.minions.client.gui.MastersStaffScreen;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class AbstractMastersButton extends Button {
    // Static
    public static final int BUTTON_SIZE = 19;

    // Finals
    protected final int textureX;
    protected final int textureY;
    protected final MastersStaffScreen screen;

    // Variables
    protected int time = 0;

    public AbstractMastersButton(int x, int y, int textureX, int textureY, MastersStaffScreen screen) {
        super((screen.width - BUTTON_SIZE) / 2 + x, (screen.height - BUTTON_SIZE) / 2 + y, BUTTON_SIZE, BUTTON_SIZE, CommonComponents.EMPTY, (button) -> {});
        this.textureX = textureX;
        this.textureY = textureY;
        this.screen = screen;
    }

    @Override
    public abstract void onPress();

    public MastersStaffScreen getScreen() {
        return this.screen;
    }

    @Override
    public void renderButton(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, this.getShaderTexture(mouseX, mouseY));
        this.renderBackground(stack, mouseX, mouseY, partialTick);
        this.renderFrame(stack, mouseX, mouseY, partialTick);
        this.renderIcon(stack, mouseX, mouseY, partialTick);
        this.renderExtra(stack, mouseX, mouseY, partialTick);
        if (this.isHoveredOrFocused()) this.renderToolTip(stack, mouseX, mouseY);
    }

    protected ResourceLocation getShaderTexture(int mouseX, int mouseY) {
        return this.isMouseOver(mouseX, mouseY) && (this.time / 20) % 2 == 1 ? MastersStaffScreen.ALT_TEXTURE_LOCATION : MastersStaffScreen.TEXTURE_LOCATION;
    }

    protected void renderBackground(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        this.blit(stack, this.textureX, this.textureY);
    }

    protected void renderFrame(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        this.blit(stack, this.textureX, this.textureY + BUTTON_SIZE);
    }

    protected void renderIcon(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        this.blit(stack, this.textureX, this.textureY + BUTTON_SIZE + BUTTON_SIZE);
    }

    protected void renderExtra(PoseStack stack, int mouseX, int mouseY, float partialTick) {

    }

    public void blit(PoseStack stack, int textureX, int textureY) {
        super.blit(stack, this.x, this.y, textureX, textureY, this.width, this.height);
    }

    @Override
    public void renderToolTip(PoseStack stack, int mouseX, int mouseY) {
        List<FormattedCharSequence> list = new ArrayList<>();
        list.add(this.getName().getVisualOrderText());
        for (MutableComponent toolTip : this.getTooltip()) list.add(toolTip.getVisualOrderText());
        this.screen.renderTooltip(stack, list, mouseX, mouseY);
    }

    abstract protected MutableComponent getName();

    protected List<MutableComponent> getTooltip() {
        return List.of();
    }

    public void onSelectedTick() {
        this.time++;
    }

    public void onUnSelectedTick() {
        this.time = 1;
    }
}
