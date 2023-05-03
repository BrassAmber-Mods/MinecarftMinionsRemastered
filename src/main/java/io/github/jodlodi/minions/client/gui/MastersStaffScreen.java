package io.github.jodlodi.minions.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jodlodi.minions.MinionsRemastered;
import io.github.jodlodi.minions.minion.Minion;
import io.github.jodlodi.minions.registry.CommonRegistry;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MastersStaffScreen extends Screen {
    public static final ResourceLocation LOCATION = MinionsRemastered.locate("textures/gui/minion.png");

    private MastersButton bottomButton = null;
    private MastersButton topButton = null;
    private MastersButton leftButton = null;
    private MastersButton rightButton = null;

    protected final HitResult context;
    protected final LocalPlayer player;
    protected MastersButton selectedButton;

    public MastersStaffScreen(LocalPlayer player, HitResult context) {
        super(GameNarrator.NO_TITLE);
        this.player = player;
        this.context = context;
    }

    public HitResult getContext() {
        return this.context;
    }

    public LocalPlayer getPlayer() {
        return this.player;
    }

    public @Nullable MastersButton getSelectedButton() {
        return this.selectedButton;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        this.player.getCapability(CommonRegistry.MASTER_CAPABILITY).ifPresent(capability -> {
            boolean secondary = this.player.isSecondaryUseActive();
            if (this instanceof BlockStaffScreen blockStaffScreen) {
                // VALUES
                BlockHitResult hitResult = blockStaffScreen.getContext();
                BlockPos pos = hitResult.getBlockPos();
                BlockEntity entity = this.player.level.getBlockEntity(pos);
                Vec3 location = hitResult.getLocation();
                Vec3 playerLocation = this.player.position();

                // FLAGS
                boolean minionSpace = capability.minionCount() < 4;
                boolean under = location.distanceTo(playerLocation) <= 1.0D && location.y <= playerLocation.y;

                // TOP BUTTON // SUMMONING
                if (minionSpace && !secondary) {
                    this.topButton = this.addRenderableWidget(new SummonButton(0, -24, blockStaffScreen, capability));
                } else {
                    this.topButton = this.addRenderableWidget(new CallAllMinionsButton(0, -24, blockStaffScreen, capability));
                }

                // RIGHT BUTTON // STORAGE
                if (under) {
                    this.rightButton = this.addRenderableWidget(new ClearContainerButton(24, 0, this));
                } else if (entity != null && entity.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) {
                    this.rightButton = this.addRenderableWidget(new ContainerButton(24, 0, this));
                }

                // BOTTOM BUTTON // ORDERS
                if (capability.getOrder() == null) {
                    this.bottomButton = this.addRenderableWidget(new DigButton(0, 24, blockStaffScreen));
                } else {
                    this.bottomButton = this.addRenderableWidget(new StopButton(0, 24, blockStaffScreen));
                }
            } else if (this instanceof EntityStaffScreen entityStaffScreen) {
                if (entityStaffScreen.target instanceof Minion minion) {
                    if (capability.isMinion(minion.getUUID())) {
                        this.topButton = this.addRenderableWidget(new BanishButton(0, -24, entityStaffScreen, capability));
                    }
                } else {
                    entityStaffScreen.target.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(itemHandler -> {
                        this.rightButton = this.addRenderableWidget(new ContainerButton(24, 0, this));
                    });
                }
            }
        });
    }

    public static MastersStaffScreen make(LocalPlayer player, HitResult context) {
        if (context instanceof EntityHitResult entityContext && entityContext.getEntity() instanceof LivingEntity living) {
            return new EntityStaffScreen(player, entityContext, living);
        } else if (context instanceof BlockHitResult blockContext) {
            return new BlockStaffScreen(player, blockContext, player.level.getBlockState(blockContext.getBlockPos()));
        }
        return new MastersStaffScreen(player, context);
    }

    @Override
    public <T extends GuiEventListener & Widget & NarratableEntry> T addRenderableWidget(T widget) {
        return super.addRenderableWidget(widget);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == 1) {
            this.onClose();
            return true;
        } else return super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
        int i = this.width / 2;
        int j = this.height / 2;

        float deltaX = (float)mouseX - i;
        float deltaY =  (float)mouseY - j;
        float angle = (float)Math.atan2(deltaY, deltaX) * Mth.RAD_TO_DEG;
        if (Mth.degreesDifferenceAbs(angle, 90) < 45) {
            this.selectedButton = this.bottomButton;
        } else this.selectedButton = this.topButton;//FIXME math

        return super.mouseDragged(mouseX, mouseY, mouseButton, dragX, dragY);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        //this.renderBackground(stack)  ;
        this.setFocused(null);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, LOCATION);
        int i = this.width / 2;
        int j = this.height / 2;


        float deltaX = mouseX - i;
        float deltaY = mouseY - j;
        float angle = (float)Math.atan2(deltaY, deltaX) * Mth.RAD_TO_DEG;

        if (this.topButton != null) this.blit(stack, (this.width - 27) / 2, (this.height - 27) / 2, Mth.degreesDifferenceAbs(angle, -90) < 45 ? 30 : 1, 1, 27, 27);
        if (this.rightButton != null) this.blit(stack, (this.width - 27) / 2, (this.height - 27) / 2, Mth.degreesDifferenceAbs(angle, 0) < 45 ? 30 : 1, 30, 27, 27);
        if (this.bottomButton != null) this.blit(stack, (this.width - 27) / 2, (this.height - 27) / 2, Mth.degreesDifferenceAbs(angle, 90) < 45 ? 30 : 1, 59, 27, 27);
        if (this.leftButton != null) this.blit(stack, (this.width - 27) / 2, (this.height - 27) / 2, Mth.degreesDifferenceAbs(angle, 180) < 45 ? 30 : 1, 88, 27, 27);

        super.render(stack, mouseX, mouseY, partialTick);
    }

    public static class EntityStaffScreen extends MastersStaffScreen {
        public final LivingEntity target;

        public EntityStaffScreen(LocalPlayer player, EntityHitResult context, LivingEntity target) {
            super(player, context);
            this.target = target;
        }

        @Override
        public EntityHitResult getContext() {
            return (EntityHitResult)this.context;
        }
    }

    public static class BlockStaffScreen extends MastersStaffScreen {
        public final BlockState target;

        public BlockStaffScreen(LocalPlayer player, BlockHitResult context, BlockState target) {
            super(player, context);
            this.target = target;
        }

        @Override
        public BlockHitResult getContext() {
            return (BlockHitResult)this.context;
        }
    }
}
