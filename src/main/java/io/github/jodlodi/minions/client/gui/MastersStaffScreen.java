package io.github.jodlodi.minions.client.gui;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import io.github.jodlodi.minions.MinionsRemastered;
import io.github.jodlodi.minions.minion.Minion;
import io.github.jodlodi.minions.registry.CommonRegistry;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.RenderTypeHelper;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MastersStaffScreen extends Screen {
    public static final ResourceLocation LOCATION = MinionsRemastered.locate("textures/gui/minion.png");
    public static final int BUTTON_DISTANCE = 32;
    public static final int POINTER_DISTANCE = 8;

    private final MastersButton[] buttons = { null, null, null, null }; // [0]TOP, [1]RIGHT, [2]BOTTOM, [3]LEFT

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
            MastersButton[] buttonSetup = { null, null, null, null }; // [0]TOP, [1]RIGHT, [2]BOTTOM, [3]LEFT

            boolean secondary = this.player.isSecondaryUseActive();
            if (this instanceof BlockStaffScreen blockStaffScreen) { // BLOCK RIGHT CLICK
                // VALUES
                BlockHitResult hitResult = blockStaffScreen.getContext();
                BlockPos pos = hitResult.getBlockPos();
                BlockEntity blockEntity = this.player.level.getBlockEntity(pos);

                // FLAGS
                boolean minionSpace = capability.minionCount() < 4;

                // TOP BUTTON // SUMMONING
                if (minionSpace && !secondary) {
                    buttonSetup[0] = new SummonButton(0, -BUTTON_DISTANCE, blockStaffScreen, capability);
                } else {
                    buttonSetup[0] = new CallAllMinionsButton(0, -BUTTON_DISTANCE, blockStaffScreen, capability);
                }

                // RIGHT BUTTON // STORAGE
                if (blockEntity != null && blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) {
                    buttonSetup[1] = new ContainerButton(BUTTON_DISTANCE, 0, this);
                }

                // BOTTOM BUTTON // ORDERS
                if (capability.getOrder() == null) {
                    buttonSetup[2] = new DigButton(0, BUTTON_DISTANCE, blockStaffScreen);
                } else {
                    buttonSetup[2] = new StopButton(0, BUTTON_DISTANCE, blockStaffScreen);
                }
            } else if (this instanceof EntityStaffScreen entityStaffScreen) { // ENTITY RIGHT CLICK
                if (entityStaffScreen.target == this.player) {
                    // RIGHT BUTTON // STORAGE
                    buttonSetup[1] = new ClearContainerButton(BUTTON_DISTANCE, 0, this);

                    // BOTTOM BUTTON // ORDERS
                    buttonSetup[2] = new CarryLivingButton(0, BUTTON_DISTANCE, entityStaffScreen);
                } else if (entityStaffScreen.target instanceof Minion minion) { // MINION
                    if (capability.isMinion(minion.getUUID())) {
                        // TOP BUTTON // SUMMONING
                        buttonSetup[0] = new BanishButton(0, -BUTTON_DISTANCE, entityStaffScreen, capability);

                        // BOTTOM BUTTON // ORDERS
                        buttonSetup[2] = new SitButton(0, BUTTON_DISTANCE, entityStaffScreen, capability);
                    }
                } else { // FRIENDLY MOB
                    if (!entityStaffScreen.hostile) {
                        if (entityStaffScreen.target.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) {
                            // RIGHT BUTTON // STORAGE
                            buttonSetup[1] = new ContainerButton(BUTTON_DISTANCE, 0, this);

                            // BOTTOM BUTTON // ORDERS
                            buttonSetup[2] = new CarryLivingButton(0, BUTTON_DISTANCE, entityStaffScreen);
                        }
                    } else { // HOSTILE MOB

                    }
                }
            }

            boolean emptyFlag = true;
            for (int i = 0; i < buttonSetup.length; i++) {
                if (buttonSetup[i] != null) {
                    this.buttons[i] = this.addRenderableWidget(buttonSetup[i]);
                    emptyFlag = false;
                }
            }
            if (emptyFlag) this.onClose();
        });
    }

    public static MastersStaffScreen make(LocalPlayer player, HitResult context, boolean flag) {
        if (context instanceof EntityHitResult entityContext) {
            return new EntityStaffScreen(player, entityContext, flag);
        } else if (context instanceof BlockHitResult blockContext) {
            return new BlockStaffScreen(player, blockContext);
        }
        return new MastersStaffScreen(player, context);
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
            this.selectedButton = this.buttons[2];
        } else this.selectedButton = this.buttons[0];//FIXME math

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

        if (this.buttons[0] != null) this.blit(stack, (this.width - 27) / 2, (this.height - 27) / 2 - POINTER_DISTANCE, Mth.degreesDifferenceAbs(angle, -90) < 45 ? 27 : 0, 0, 27, 27);
        if (this.buttons[1] != null) this.blit(stack, (this.width - 27) / 2 + POINTER_DISTANCE, (this.height - 27) / 2, Mth.degreesDifferenceAbs(angle, 0) < 45 ? 27 : 0, 27, 27, 27);
        if (this.buttons[2] != null) this.blit(stack, (this.width - 27) / 2, (this.height - 27) / 2 + POINTER_DISTANCE, Mth.degreesDifferenceAbs(angle, 90) < 45 ? 27 : 0, 54, 27, 27);
        if (this.buttons[3] != null) this.blit(stack, (this.width - 27) / 2 - POINTER_DISTANCE, (this.height - 27) / 2, Mth.degreesDifferenceAbs(angle, 180) < 45 ? 27 : 0, 81, 27, 27);

        super.render(stack, mouseX, mouseY, partialTick);

        if (this instanceof EntityStaffScreen entityStaffScreen) {
            renderEntity(entityStaffScreen.target, this.width * 0.5D - 0.5D, this.height * 0.5D, partialTick);
        } else if (this instanceof BlockStaffScreen blockStaffScreen) {
            BlockState state = this.player.level.getBlockState(blockStaffScreen.getContext().getBlockPos());
            renderSingleBlock(state, this.width * 0.5D - 0.5D, this.height * 0.5D, partialTick, this.player.tickCount);
        }
    }

    public static void renderSingleBlock(BlockState state, double x, double y, float partialTick, int time) {
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();

        float scale = 0.85F * 26.0F;
        y += 12.0F;
        float spin = (time + partialTick) * 0.15F;
        float tilt = (float)Math.sin(spin * 0.45F) * 4.0F;
        scale *= (Math.cos(spin * 0.5F) + 1.0F) * 0.01F + 0.98F;

        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        poseStack.translate(x, y, 1050.0D);
        poseStack.scale(1.0F, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();
        PoseStack poseStack1 = new PoseStack();
        poseStack1.translate(0.0D, 0.0D, 1000.0D);
        poseStack1.mulPose(Vector3f.YP.rotationDegrees(spin * 8.0F));
        poseStack1.pushPose();
        double offset = 12.0D;
        poseStack1.translate(offset, 0.0D, -offset);
        poseStack1.scale(scale, scale, scale);
        Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
        Quaternion quaternion1 = Vector3f.XP.rotationDegrees(tilt);
        quaternion.mul(quaternion1);
        poseStack1.mulPose(quaternion);

        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        quaternion1.conj();
        entityrenderdispatcher.overrideCameraOrientation(quaternion1);
        entityrenderdispatcher.setRenderShadow(false);
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        RenderShape rendershape = state.getRenderShape();
        if (rendershape != RenderShape.INVISIBLE) {
            switch (rendershape) {
                case MODEL -> {
                    BakedModel bakedmodel = dispatcher.getBlockModel(state);
                    int i = dispatcher.blockColors.getColor(state, null, null, 0);
                    float f = (float) (i >> 16 & 255) / 255.0F;
                    float f1 = (float) (i >> 8 & 255) / 255.0F;
                    float f2 = (float) (i & 255) / 255.0F;
                    for (RenderType rt : bakedmodel.getRenderTypes(state, RandomSource.create(42), ModelData.EMPTY))
                        dispatcher.modelRenderer.renderModel(poseStack1.last(), bufferSource.getBuffer(RenderTypeHelper.getEntityRenderType(rt, false)), state, bakedmodel, f, f1, f2, 15728880, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, rt);
                }
                case ENTITYBLOCK_ANIMATED -> {
                    ItemStack stack = new ItemStack(state.getBlock());
                    IClientItemExtensions.of(stack).getCustomRenderer().renderByItem(stack, ItemTransforms.TransformType.NONE, poseStack1, bufferSource, 15728880, OverlayTexture.NO_OVERLAY);
                }
            }
        }

        bufferSource.endBatch();
        entityrenderdispatcher.setRenderShadow(true);
        poseStack.popPose();
        poseStack1.popPose();
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
    }

    @SuppressWarnings("deprecation")
    public static void renderEntity(Entity entity, double x, double y, float partialTick) {
        // VARIABLES
        boolean livingFlag = entity instanceof LivingEntity;
        float verticalScale = entity.getBbHeight();
        float flatScale = entity.getBbWidth();
        float scale = (livingFlag ? (float)Math.sqrt((Math.min(flatScale / verticalScale, verticalScale / flatScale) + 0.5D) * 0.5D) : 0.85F) * 26.0F;
        y += livingFlag ? (1.5D * verticalScale) + 12.0F : 12.0F;
        float spin = (entity.tickCount + partialTick) * 0.15F;
        float tilt = (float)Math.sin(spin * 0.45F) * 4.0F;
        scale *= (Math.cos(spin * 0.5F) + 1.0F) * 0.01F + 0.98F;

        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        poseStack.translate(x, y, 1050.0D);
        poseStack.scale(1.0F, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();
        PoseStack poseStack1 = new PoseStack();
        poseStack1.translate(0.0D, 0.0D, 1000.0D);
        if (!livingFlag) poseStack1.mulPose(Vector3f.YP.rotationDegrees(spin * 8.0F));
        poseStack1.scale(scale, scale, scale);
        Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
        Quaternion quaternion1 = Vector3f.XP.rotationDegrees(tilt);
        quaternion.mul(quaternion1);
        poseStack1.mulPose(quaternion);
        float yRot = entity.getYRot();
        float xRot = 0.0F;
        float yBodyRot = 0.0F;
        float yHeadRot0 = 0.0F;
        float yHeadRot = 0.0F;

        if (entity instanceof LivingEntity living) {
            xRot = entity.getXRot();
            yBodyRot = living.yBodyRot;
            yHeadRot0 = living.yHeadRotO;
            yHeadRot = living.yHeadRot;
            entity.setYRot(spin * 8.0F);
            living.yBodyRot = spin * 8.0F;
            living.setXRot(0.0F);
            living.yHeadRot = entity.getYRot();
            living.yHeadRotO = entity.getYRot();
        } else entity.setYRot(180.0F);
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        quaternion1.conj();
        entityrenderdispatcher.overrideCameraOrientation(quaternion1);
        entityrenderdispatcher.setRenderShadow(false);
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> entityrenderdispatcher.render(entity, 0.0D, 0.0D, 0.0D, 180.0F, 1.0F, poseStack1, bufferSource, 15728880));
        bufferSource.endBatch();
        entityrenderdispatcher.setRenderShadow(true);
        if (entity instanceof LivingEntity living) {
            living.yBodyRot = yBodyRot;
            living.yHeadRotO = yHeadRot0;
            living.yHeadRot = yHeadRot;
            living.setXRot(xRot);
        }
        entity.setYRot(yRot);
        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
    }

    public static class EntityStaffScreen extends MastersStaffScreen {
        public final Entity target;
        public final boolean hostile;

        public EntityStaffScreen(LocalPlayer player, EntityHitResult context, boolean hostile) {
            super(player, context);
            this.target = context.getEntity();
            this.hostile = hostile;
        }

        @Override
        public EntityHitResult getContext() {
            return (EntityHitResult)this.context;
        }
    }

    public static class BlockStaffScreen extends MastersStaffScreen {
        public final BlockState target;

        public BlockStaffScreen(LocalPlayer player, BlockHitResult context) {
            super(player, context);
            this.target = player.level.getBlockState(context.getBlockPos());
        }

        @Override
        public BlockHitResult getContext() {
            return (BlockHitResult)this.context;
        }
    }
}
