package kawun.new_treasure_maps.client.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;

public class HandHelper {


    private static final Minecraft minecraft = Minecraft.getInstance();


    // from ItemInHandRenderer.renderPlayerArm()
    public static void defaultArmPose(PoseStack poseStack, float inverseArmHeight, float attackValue, HumanoidArm arm) {
        float invert = (arm == HumanoidArm.RIGHT) ? 1.0F : -1.0F;

        float sqrtAttackValue = Mth.sqrt(attackValue);
        float xSwingPosition = -0.3F * Mth.sin(sqrtAttackValue * (float) Math.PI);
        float ySwingPosition = 0.4F * Mth.sin(sqrtAttackValue * (float) (Math.PI * 2));
        float zSwingPosition = -0.4F * Mth.sin(attackValue * (float) Math.PI);
        float zSwingRotation = Mth.sin(attackValue * attackValue * (float) Math.PI);
        float ySwingRotation = Mth.sin(sqrtAttackValue * (float) Math.PI);

        poseStack.translate(invert * (xSwingPosition + 0.64000005F), ySwingPosition + -0.6F + inverseArmHeight * -0.6F, zSwingPosition + -0.71999997F);
        poseStack.mulPose(Axis.YP.rotationDegrees(invert * 45.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(invert * ySwingRotation * 70.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(invert * zSwingRotation * -20.0F));
        poseStack.translate(invert * -1.0F, 3.6F, 3.5F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(invert * 120.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(200.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(invert * -135.0F));
        poseStack.translate(invert * 5.6F, 0.0F, 0.0F);
    }



    public static void setHand(PoseStack poseStack, HumanoidArm arm, SubmitNodeCollector submitNodeCollector, int lightCoords) {
        LocalPlayer player = minecraft.player;
        AvatarRenderer<AbstractClientPlayer> avatarRenderer = minecraft.getEntityRenderDispatcher().getPlayerRenderer(player);
        Identifier skinTexture = player.getSkin().body().texturePath();
        if (arm == HumanoidArm.RIGHT) {
            avatarRenderer.renderRightHand(
                    poseStack, submitNodeCollector, lightCoords, skinTexture, player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE)
            );
        } else {
            avatarRenderer.renderLeftHand(
                    poseStack, submitNodeCollector, lightCoords, skinTexture, player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE)
            );
        }
    }
}
