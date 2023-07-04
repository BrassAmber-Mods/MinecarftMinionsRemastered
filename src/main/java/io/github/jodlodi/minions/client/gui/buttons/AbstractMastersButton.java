package io.github.jodlodi.minions.client.gui.buttons;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jodlodi.minions.client.gui.MastersStaffScreen;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.MutableComponent;
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
    protected final MastersStaffScreen screen;

    public AbstractMastersButton(int x, int y, MastersStaffScreen screen) {
        super((screen.width - 19) / 2 + x, (screen.height - 19) / 2 + y, 19, 19, CommonComponents.EMPTY, (button) -> {});
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
        this.renderBackground(stack, mouseX, mouseY, partialTick);
        this.renderFrame(stack, mouseX, mouseY, partialTick);

        boolean flag = this.isMouseOver(mouseX, mouseY) && (this.screen.getPlayer().tickCount / 20) % 2 == 0;
        if (flag) {
            stack.pushPose();
            stack.translate(.0D, -1.0D, .0D);
        }
        this.renderIcon(stack, mouseX, mouseY, partialTick);
        if (flag) stack.popPose();

        this.renderExtra(stack, mouseX, mouseY, partialTick);
        if (this.isHoveredOrFocused()) this.renderToolTip(stack, mouseX, mouseY);
    }

    protected abstract void renderBackground(PoseStack stack, int mouseX, int mouseY, float partialTick);

    protected abstract void renderFrame(PoseStack stack, int mouseX, int mouseY, float partialTick);

    protected abstract void renderIcon(PoseStack stack, int mouseX, int mouseY, float partialTick);

    protected void renderExtra(PoseStack stack, int mouseX, int mouseY, float partialTick) {

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

    }
}
