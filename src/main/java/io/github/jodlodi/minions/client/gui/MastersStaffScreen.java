package io.github.jodlodi.minions.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import io.github.jodlodi.minions.MinReflections;
import io.github.jodlodi.minions.MinionsRemastered;
import io.github.jodlodi.minions.client.gui.buttons.*;
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
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.RenderTypeHelper;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MastersStaffScreen extends Screen {
    public static final ResourceLocation LOCATION = MinionsRemastered.locate("textures/gui/minion.png");
    public static final int BUTTON_DISTANCE = 32;
    public static final int POINTER_DISTANCE = 8;

    private final AbstractMastersButton[] buttons = { null, null, null, null }; // [0]TOP, [1]RIGHT, [2]BOTTOM, [3]LEFT

    protected final HitResult context;
    protected final LocalPlayer player;

    protected float realTime = 0.0F;
    protected float time = 0.0F;
    protected float boost = 1.0F;
    protected float shade = 0.0F;

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

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        this.player.getCapability(CommonRegistry.MASTER_CAPABILITY).ifPresent(capability -> {
            AbstractMastersButton[] buttonSetup = { null, null, null, null }; // [0]TOP, [1]RIGHT, [2]BOTTOM, [3]LEFT

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
                    if (this.player.level.getBlockState(hitResult.getBlockPos()).is(BlockTags.LOGS)) {
                        buttonSetup[2] = new ChopOrderButton(0, BUTTON_DISTANCE, blockStaffScreen);
                    } else if (this.player.level.getBlockState(hitResult.getBlockPos()).is(BlockTags.WART_BLOCKS)) {
                        buttonSetup[2] = new HoeWartButton(0, BUTTON_DISTANCE, blockStaffScreen);
                    }else if (hitResult.getDirection().getAxis() == Direction.Axis.Y) {
                        buttonSetup[2] = new DigOrderButton(0, BUTTON_DISTANCE, blockStaffScreen);
                    } else {
                        buttonSetup[2] = new MineOrderButton(0, BUTTON_DISTANCE, blockStaffScreen);
                    }
                } else {
                    buttonSetup[2] = new StopOrderButton(0, BUTTON_DISTANCE, blockStaffScreen);
                }
            } else if (this instanceof EntityStaffScreen entityStaffScreen) { // ENTITY RIGHT CLICK
                if (entityStaffScreen.target == this.player) {
                    // RIGHT BUTTON // STORAGE
                    buttonSetup[1] = new ClearContainerButton(BUTTON_DISTANCE, 0, this);

                    // BOTTOM BUTTON // ORDERS
                    if (capability.getOrder() == null) {
                        buttonSetup[2] = new CarryLivingOrderButton(0, BUTTON_DISTANCE, entityStaffScreen);
                    } else {
                        buttonSetup[2] = new StopOrderButton(0, BUTTON_DISTANCE, entityStaffScreen);
                    }
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
                            if (capability.getOrder() == null) {
                                buttonSetup[2] = new CarryLivingOrderButton(0, BUTTON_DISTANCE, entityStaffScreen);
                            } else {
                                buttonSetup[2] = new StopOrderButton(0, BUTTON_DISTANCE, entityStaffScreen);
                            }
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
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        boolean clicked = super.mouseClicked(mouseX, mouseY, mouseButton);
        if (!clicked) {
            double centerX = this.width * 0.5D;
            double centerY = this.height * 0.5D;
            if (mouseX > centerX - 10.0D && mouseX < centerX + 10.0D && mouseY > centerY - 10.0D && mouseY < centerY + 10.0D) {
                if (!this.isShiftKeyDown()) {
                    this.boost *= 2.0F;
                    this.boost += 3.0F;
                }
                if (this instanceof MastersStaffScreen.BlockStaffScreen screen) {
                    SoundType soundType = this.player.level.getBlockState(screen.getContext().getBlockPos()).getSoundType();
                    this.player.level.playLocalSound(this.player.getX(), this.player.getY(), this.player.getZ(), soundType.getHitSound(), this.player.getSoundSource(), (soundType.getVolume() + 1.0F) / 8.0F, soundType.getPitch() * 0.5F, false);
                } else if (this instanceof MastersStaffScreen.EntityStaffScreen screen && screen.target instanceof Mob mob && screen.ambientSound != null) {
                    this.player.level.playLocalSound(this.player.getX(), this.player.getY(), this.player.getZ(), screen.ambientSound, this.player.getSoundSource(), screen.soundVolume, mob.getVoicePitch(), false);
                }
            }
        }
        return clicked;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == 1) {
            this.onClose();
            return true;
        } else return super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public void tick() {
        this.realTime += 1.0F;
        this.time += this.boost;
        this.boost *= 0.8F;

        if (this.isShiftKeyDown()) {
            if (this.shade < 1.0F) this.shade = Math.min(this.shade + 0.1F, 1.0F);
        } else if (this.shade > 0.0F) this.shade = Math.max(this.shade - 0.1F, 0.0F);

        this.boost = Math.max(this.boost, 1.0F - this.shade);

        if (this.minecraft == null) return;
        double x = this.minecraft.mouseHandler.xpos() * (double) this.minecraft.getWindow().getGuiScaledWidth() / (double) this.minecraft.getWindow().getScreenWidth();
        double y = this.minecraft.mouseHandler.ypos() * (double) this.minecraft.getWindow().getGuiScaledHeight() / (double) this.minecraft.getWindow().getScreenHeight();
        for (int i = 0; i < 4; i++) {
            if (this.buttons[i] != null && this.buttons[i].isMouseOver(x, y)) {
                this.buttons[i].onSelectedTick();
            }
        }
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(stack);
        this.setFocused(null);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, LOCATION);

        if (this.buttons[0] != null) this.blit(stack, (this.width - 27) / 2, (this.height - 27) / 2 - POINTER_DISTANCE, this.buttons[0].isMouseOver(mouseX, mouseY) ? 27 : 0, 0, 27, 27);
        if (this.buttons[1] != null) this.blit(stack, (this.width - 27) / 2 + POINTER_DISTANCE, (this.height - 27) / 2, this.buttons[1].isMouseOver(mouseX, mouseY) ? 27 : 0, 27, 27, 27);
        if (this.buttons[2] != null) this.blit(stack, (this.width - 27) / 2, (this.height - 27) / 2 + POINTER_DISTANCE, this.buttons[2].isMouseOver(mouseX, mouseY) ? 27 : 0, 54, 27, 27);
        if (this.buttons[3] != null) this.blit(stack, (this.width - 27) / 2 - POINTER_DISTANCE, (this.height - 27) / 2, this.buttons[3].isMouseOver(mouseX, mouseY) ? 27 : 0, 81, 27, 27);

        super.render(stack, mouseX, mouseY, partialTick);

        if (this instanceof EntityStaffScreen entityStaffScreen) {
            this.renderEntity(entityStaffScreen.target, this.width * 0.5D - 0.5D, this.height * 0.5D, partialTick);
        } else if (this instanceof BlockStaffScreen blockStaffScreen) {
            this.renderSingleBlock(this.player.level.getBlockState(blockStaffScreen.getContext().getBlockPos()), this.width * 0.5D - 0.5D, this.height * 0.5D, partialTick);
        }

        //this.font.drawShadow(stack, Component.literal("Command Minions"), 10.0F, 10.0F, ChatFormatting.DARK_RED.getColor());
    }

    @SuppressWarnings("UnstableApiUsage")
    public void renderBackground(PoseStack stack) {
        int rgb = 16;

        float mul = this.shade * 0.5F;
        int o1 = (int)(192.0F * mul);
        int o2 = (int)(208.0F * mul);

        int i2 = (o1 << 24) | (rgb << 16) | (rgb << 8) | rgb;
        int j2 = (o2 << 24) | (rgb << 16) | (rgb << 8) | rgb;

        this.fillGradient(stack, 0, 0, this.width, this.height, i2, j2);
        MinecraftForge.EVENT_BUS.post(new ScreenEvent.BackgroundRendered(this, stack));
    }

    public void renderSingleBlock(BlockState state, double x, double y, float partialTick) {
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();

        float scale = 0.85F * 26.0F;
        float offset = 12.0F;
        y += offset;
        float spin = (this.time + partialTick) * 0.15F;
        float realTilt = (this.realTime + partialTick) * 0.15F;
        float tilt = (float)Math.sin(realTilt * 0.45F) * 4.0F;
        scale *= (Math.cos(realTilt * 0.5F) + 1.0F) * 0.01F + 0.98F;

        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        poseStack.translate(x, y, 1050.0D);
        poseStack.scale(1.0F, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();
        PoseStack poseStack1 = new PoseStack();
        poseStack1.translate(0.0D, 0.0D, 1000.0D);
        poseStack1.mulPose(Vector3f.YP.rotationDegrees(spin * 8.0F));
        poseStack1.pushPose();
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
    public void renderEntity(Entity entity, double x, double y, float partialTick) {
        // VARIABLES
        boolean livingFlag = entity instanceof LivingEntity;
        float verticalScale = entity.getBbHeight();
        float flatScale = entity.getBbWidth();
        float scale = (livingFlag ? (float)Math.sqrt((Math.min(flatScale / verticalScale, verticalScale / flatScale) + 0.5D) * 0.5D) : 0.85F) * 26.0F;
        y += livingFlag ? (1.5D * verticalScale) + 12.0F : 12.0F;
        float spin = (this.time + partialTick) * 0.15F;
        float realTilt = (this.realTime + partialTick) * 0.15F;
        float tilt = (float)Math.sin(realTilt * 0.45F) * 4.0F;
        scale *= (Math.cos(realTilt * 0.5F) + 1.0F) * 0.01F + 0.98F;

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

    public boolean isShiftKeyDown() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344);
    }

    public static class EntityStaffScreen extends MastersStaffScreen {
        public final Entity target;
        public final boolean hostile;
        public final SoundEvent ambientSound;
        public final float soundVolume;

        public EntityStaffScreen(LocalPlayer player, EntityHitResult context, boolean hostile) {
            super(player, context);
            this.target = context.getEntity();
            this.hostile = hostile;

            if (this.target instanceof Mob mob) {
                this.ambientSound = MinReflections.getAmbientSound(mob);
                this.soundVolume = MinReflections.getSoundVolume(mob);
            } else {
                this.ambientSound = null;
                this.soundVolume = 1.0F;
            }
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
