package io.github.jodlodi.minions.client;
// Made with Blockbench 4.6.1
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

// The model was later re-adjusted to be a humanoid model

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jodlodi.minions.minion.Minion;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MinionEntityModel<T extends Minion> extends HumanoidModel<T> {
	public final ModelPart minion;

	public MinionEntityModel(ModelPart root) { //Avert your gaze, this init is cursed
		super(root.getChild("minion"));
		this.minion = root.getChild("minion");
		this.minion.children.remove("head");
		this.body.children.put("head", this.head);
		this.minion.children.remove("hat");
		this.body.children.put("hat", this.hat);
		this.minion.children.remove("left_arm");
		this.body.children.put("left_arm", this.leftArm);
		this.minion.children.remove("right_arm");
		this.body.children.put("right_arm", this.rightArm);
	}

	@Override
	protected Iterable<ModelPart> headParts() {
		return ImmutableList.of();
	}

	protected Iterable<ModelPart> bodyParts() {
		return ImmutableList.of(this.minion);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition minion = partdefinition.addOrReplaceChild("minion", CubeListBuilder.create(), PartPose.offset(0.0F, 19.0F, 0.0F));
		PartDefinition body = minion.addOrReplaceChild("body", CubeListBuilder.create().texOffs(10, 9).addBox(-4.0F, -8.0F, -2.0F, 8.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, 0.1745F, 0.0F, 0.0F));
		body.addOrReplaceChild("backpack", CubeListBuilder.create().texOffs(0, 25).addBox(-6.0F, -11.0F, 2.0F, 12.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 33).addBox(-4.0F, -7.0F, 2.0F, 8.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 0.0F));
		minion.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -4.5F, -2.5F, 6.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.0F, -0.5F, -0.1309F, 0.0F, 0.0F));
		minion.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(20, 0).addBox(-3.0F, -4.5F, -2.5F, 6.0F, 5.0F, 4.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(0.0F, -8.0F, -0.5F, -0.1309F, 0.0F, 0.0F));
		minion.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(34, 19).addBox(-1.0F, -1.0F, -1.5F, 2.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -6.0F, -0.5F, -0.1309F, 0.0F, 0.0F));
		minion.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(34, 9).addBox(-1.0F, -1.0F, -1.5F, 2.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -6.0F, -0.5F, -0.1309F, 0.0F, 0.0F));
		minion.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 17).addBox(-3.0F, 0.0F, -1.5F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
		minion.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 9).addBox(1.0F, 0.0F, -1.5F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(T minion, float limbSwing, float limbSwingAmount, float ticks, float netHeadYaw, float headPitch) {
		boolean flag = minion.getFallFlyingTicks() > 4;
		boolean flag1 = minion.isVisuallySwimming();

		this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
		if (flag) {
			this.head.xRot = (-(float)Math.PI / 4F);
		} else if (this.swimAmount > 0.0F) {
			if (flag1) {
				this.head.xRot = this.rotlerpRad(this.swimAmount, this.head.xRot, (-(float)Math.PI / 4F));
			} else {
				this.head.xRot = this.rotlerpRad(this.swimAmount, this.head.xRot, headPitch * ((float)Math.PI / 180F));
			}
		} else {
			this.head.xRot = headPitch * ((float)Math.PI / 180F);
		}
		this.head.xRot -= 0.1309F;

		/*this.body.yRot = 0.0F;
		this.rightArm.z = 0.0F;
		this.rightArm.x = -5.0F;
		this.leftArm.z = 0.0F;
		this.leftArm.x = 5.0F;*/
		float f = 1.0F;
		if (flag) {
			f = (float)minion.getDeltaMovement().lengthSqr();
			f /= 0.2F;
			f *= f * f;
		}

		if (f < 1.0F) {
			f = 1.0F;
		}

		this.rightArm.xRot = (Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 2.0F * limbSwingAmount * 0.5F / f) - 0.1309F;
		this.leftArm.xRot = (Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F / f) - 0.1309F;
		this.rightArm.zRot = 0.0F;
		this.leftArm.zRot = 0.0F;
		this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount / f;
		this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount / f;
		this.rightLeg.yRot = 0.0F;
		this.leftLeg.yRot = 0.0F;
		this.rightLeg.zRot = 0.0F;
		this.leftLeg.zRot = 0.0F;
		if (this.riding) {
			this.rightArm.xRot += (-(float)Math.PI / 5F);
			this.leftArm.xRot += (-(float)Math.PI / 5F);
			this.rightLeg.xRot = -1.4137167F;
			this.rightLeg.yRot = ((float)Math.PI / 10F);
			this.rightLeg.zRot = 0.07853982F;
			this.leftLeg.xRot = -1.4137167F;
			this.leftLeg.yRot = (-(float)Math.PI / 10F);
			this.leftLeg.zRot = -0.07853982F;
		}

		this.rightArm.yRot = 0.0F;
		this.leftArm.yRot = 0.0F;
		boolean flag2 = minion.getMainArm() == HumanoidArm.RIGHT;
		if (minion.isUsingItem()) {
			boolean flag3 = minion.getUsedItemHand() == InteractionHand.MAIN_HAND;
			if (flag3 == flag2) {
				this.poseRightArm(minion);
			} else {
				this.poseLeftArm(minion);
			}
		} else {
			boolean flag4 = flag2 ? this.leftArmPose.isTwoHanded() : this.rightArmPose.isTwoHanded();
			if (flag2 != flag4) {
				this.poseLeftArm(minion);
				this.poseRightArm(minion);
			} else {
				this.poseRightArm(minion);
				this.poseLeftArm(minion);
			}
		}

		this.setupAttackAnimation(minion, ticks);
		if (this.crouching) {
			this.body.xRot += 0.5F;
			this.rightArm.xRot += 0.4F;
			this.leftArm.xRot += 0.4F;
			this.rightLeg.z += 3.9F;
			this.leftLeg.z += 3.9F;
			this.rightLeg.y += 0.2F;
			this.leftLeg.y += 0.2F;
			this.head.y += 4.2F;
			this.body.y += 3.2F;
			this.leftArm.y += 3.2F;
			this.rightArm.y += 3.2F;
		}

		if (this.rightArmPose != HumanoidModel.ArmPose.SPYGLASS) {
			AnimationUtils.bobModelPart(this.rightArm, ticks, 1.0F);
		}

		if (this.leftArmPose != HumanoidModel.ArmPose.SPYGLASS) {
			AnimationUtils.bobModelPart(this.leftArm, ticks, -1.0F);
		}

		if (this.swimAmount > 0.0F) {
			float f5 = limbSwing % 26.0F;
			HumanoidArm humanoidarm = this.getAttackArm(minion);
			float f1 = humanoidarm == HumanoidArm.RIGHT && this.attackTime > 0.0F ? 0.0F : this.swimAmount;
			float f2 = humanoidarm == HumanoidArm.LEFT && this.attackTime > 0.0F ? 0.0F : this.swimAmount;
			if (!minion.isUsingItem()) {
				if (f5 < 14.0F) {
					this.leftArm.xRot = this.rotlerpRad(f2, this.leftArm.xRot, 0.0F);
					this.rightArm.xRot = Mth.lerp(f1, this.rightArm.xRot, 0.0F);
					this.leftArm.yRot = this.rotlerpRad(f2, this.leftArm.yRot, (float)Math.PI);
					this.rightArm.yRot = Mth.lerp(f1, this.rightArm.yRot, (float)Math.PI);
					this.leftArm.zRot = this.rotlerpRad(f2, this.leftArm.zRot, (float)Math.PI + 1.8707964F * this.quadraticArmUpdate(f5) / this.quadraticArmUpdate(14.0F));
					this.rightArm.zRot = Mth.lerp(f1, this.rightArm.zRot, (float)Math.PI - 1.8707964F * this.quadraticArmUpdate(f5) / this.quadraticArmUpdate(14.0F));
				} else if (f5 >= 14.0F && f5 < 22.0F) {
					float f6 = (f5 - 14.0F) / 8.0F;
					this.leftArm.xRot = this.rotlerpRad(f2, this.leftArm.xRot, ((float)Math.PI / 2F) * f6);
					this.rightArm.xRot = Mth.lerp(f1, this.rightArm.xRot, ((float)Math.PI / 2F) * f6);
					this.leftArm.yRot = this.rotlerpRad(f2, this.leftArm.yRot, (float)Math.PI);
					this.rightArm.yRot = Mth.lerp(f1, this.rightArm.yRot, (float)Math.PI);
					this.leftArm.zRot = this.rotlerpRad(f2, this.leftArm.zRot, 5.012389F - 1.8707964F * f6);
					this.rightArm.zRot = Mth.lerp(f1, this.rightArm.zRot, 1.2707963F + 1.8707964F * f6);
				} else if (f5 >= 22.0F && f5 < 26.0F) {
					float f3 = (f5 - 22.0F) / 4.0F;
					this.leftArm.xRot = this.rotlerpRad(f2, this.leftArm.xRot, ((float)Math.PI / 2F) - ((float)Math.PI / 2F) * f3);
					this.rightArm.xRot = Mth.lerp(f1, this.rightArm.xRot, ((float)Math.PI / 2F) - ((float)Math.PI / 2F) * f3);
					this.leftArm.yRot = this.rotlerpRad(f2, this.leftArm.yRot, (float)Math.PI);
					this.rightArm.yRot = Mth.lerp(f1, this.rightArm.yRot, (float)Math.PI);
					this.leftArm.zRot = this.rotlerpRad(f2, this.leftArm.zRot, (float)Math.PI);
					this.rightArm.zRot = Mth.lerp(f1, this.rightArm.zRot, (float)Math.PI);
				}
			}

			this.leftLeg.xRot = Mth.lerp(this.swimAmount, this.leftLeg.xRot, 0.3F * Mth.cos(limbSwing * 0.33333334F + (float)Math.PI));
			this.rightLeg.xRot = Mth.lerp(this.swimAmount, this.rightLeg.xRot, 0.3F * Mth.cos(limbSwing * 0.33333334F));
		}

		this.hat.copyFrom(this.head);
	}

	@Override
	public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
		this.minion.translateAndRotate(poseStack);
		this.body.translateAndRotate(poseStack);

		float f = arm == HumanoidArm.RIGHT ? 0.75F : -0.75F;
		ModelPart modelpart = this.getArm(arm);
		modelpart.x += f;
		modelpart.y -= 1.5F;
		modelpart.translateAndRotate(poseStack);
		modelpart.x -= f;
		modelpart.y += 1.5F;
		poseStack.scale(0.75F, 0.75F, 0.75F);
	}

	public void translateToHead(PoseStack poseStack) {
		this.minion.translateAndRotate(poseStack);
		this.body.translateAndRotate(poseStack);

		/*modelpart.x += f;
		modelpart.y -= 1.5F;*/
		this.head.translateAndRotate(poseStack);
		/*modelpart.x -= f;
		modelpart.y += 1.5F;*/
		poseStack.scale(0.65F, 0.65F, 0.65F);
	}
}