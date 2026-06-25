package kawun.new_treasure_maps.client.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;

public class HandHelper {


    private static Minecraft minecraft = Minecraft.getInstance();


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
