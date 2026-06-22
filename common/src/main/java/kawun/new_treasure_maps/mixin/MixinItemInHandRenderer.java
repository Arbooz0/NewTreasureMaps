package kawun.new_treasure_maps.mixin;

import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import kawun.new_treasure_maps.commands.pose.PoseCommand;
import kawun.new_treasure_maps.items.Items;
import kawun.new_treasure_maps.model.MapModelGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class MixinItemInHandRenderer {


    private final MapModelGenerator map_generator = new MapModelGenerator();
    private final Identifier map_id = Identifier.withDefaultNamespace("textures/map/map_background.png");
    private final Identifier alban_id = Identifier.withDefaultNamespace("textures/painting/alban.png");

    private final RenderPipeline.Snippet snippet = RenderPipeline
            .builder(RenderPipelines.MATRICES_FOG_LIGHT_DIR_SNIPPET)
            .withVertexShader("core/entity")
            .withFragmentShader("core/entity")
            .withSampler("Sampler0")
            .withSampler("Sampler2")
            .withVertexFormat(DefaultVertexFormat.ENTITY, VertexFormat.Mode.QUADS)
            .withDepthStencilState(DepthStencilState.DEFAULT)
            .buildSnippet();
    private final RenderPipeline pipeline = RenderPipeline.builder(snippet)
            .withLocation("pipeline/treasure_map")
            .withShaderDefine("ALPHA_CUTOUT", 0.1F)
            .withShaderDefine("PER_FACE_LIGHTING")
            .withSampler("Sampler1")
            .withCull(false)
            .build();
    private final RenderSetup render_state = RenderSetup.builder(pipeline)
            .withTexture("Sampler0", alban_id)
            .useLightmap().useOverlay().affectsCrumbling()
            .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE).createRenderSetup();
    private final RenderType render_type = RenderType.create("treasure_map", render_state);


    @Shadow
    private Minecraft minecraft;
    @Shadow
    private EntityRenderDispatcher entityRenderDispatcher;


    @Inject(at = @At("HEAD"), method = "renderArmWithItem", cancellable = true)
    private void renderArmWithItem(
          AbstractClientPlayer player,
          float frameInterp,
          float xRot,
          InteractionHand hand,
          float attack,
          ItemStack itemStack,
          float inverseArmHeight,
          PoseStack poseStack,
          SubmitNodeCollector submitNodeCollector,
          int lightCoords,
          CallbackInfo ci
    ) {
        if (hand != InteractionHand.MAIN_HAND) {
            return;
        }

        //PoseCommand.base_pose = poseStack.last().copy();

        if (!itemStack.is(Items.TREASURE_MAP)) {
          return;
        }

        poseStack.mulPose(PoseCommand.pose.pose());

        poseStack.mulPose(new Matrix4f().rotateLocalX(xRot * ((float) Math.PI / 180F)));

        submitNodeCollector.submitCustomGeometry(poseStack, render_type, (pose, vertexConsumer) -> {
          map_generator.update(pose, vertexConsumer, lightCoords, inverseArmHeight);
        });

        //boolean isMainHand = hand == InteractionHand.MAIN_HAND;
        //HumanoidArm arm = isMainHand ? player.getMainArm() : player.getMainArm().getOpposite();

        //this.renderPlayerArm(poseStack, submitNodeCollector, lightCoords, inverseArmHeight, attack, arm);

        //renderTwoHandedMap(poseStack, submitNodeCollector, lightCoords, 90, inverseArmHeight, attack);

        //setHand(poseStack, arm, submitNodeCollector, lightCoords);

        //PoseCommand.result_pose = poseStack.last().copy();

        ci.cancel();
    }


    private void renderTwoHandedMap(
            PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, float xRot, float inverseArmHeight, float attackValue
    ) {
        /*float sqrtAttackValue = Mth.sqrt(attackValue);
        float ySwingPosition = -0.2F * Mth.sin(attackValue * (float) Math.PI);
        float zSwingPosition = -0.4F * Mth.sin(sqrtAttackValue * (float) Math.PI);
        poseStack.translate(0.0F, -ySwingPosition / 2.0F, zSwingPosition);*/
        float mapTilt = this.calculateMapTilt(xRot);
        poseStack.translate(0.0F, 0.04F + inverseArmHeight * -1.2F + mapTilt * -0.5F, -0.72F);
        poseStack.mulPose(Axis.XP.rotationDegrees(mapTilt * -85.0F));

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        this.renderMapHand(poseStack, submitNodeCollector, lightCoords, HumanoidArm.RIGHT);
        this.renderMapHand(poseStack, submitNodeCollector, lightCoords, HumanoidArm.LEFT);
        poseStack.popPose();
    }


    private void renderMapHand(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, HumanoidArm arm) {
        AvatarRenderer<AbstractClientPlayer> avatarRenderer = this.entityRenderDispatcher.getPlayerRenderer(this.minecraft.player);
        poseStack.pushPose();
        float invert = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        poseStack.mulPose(Axis.YP.rotationDegrees(92.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(invert * -41.0F));
        poseStack.translate(invert * 0.3F, -1.1F, 0.45F);
        Identifier skinTexture = this.minecraft.player.getSkin().body().texturePath();
        if (arm == HumanoidArm.RIGHT) {
            avatarRenderer.renderRightHand(
                    poseStack, submitNodeCollector, lightCoords, skinTexture, this.minecraft.player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE)
            );
        } else {
            avatarRenderer.renderLeftHand(
                    poseStack, submitNodeCollector, lightCoords, skinTexture, this.minecraft.player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE)
            );
        }

        poseStack.popPose();
    }


    private void setHand(PoseStack poseStack, HumanoidArm arm, SubmitNodeCollector submitNodeCollector, int lightCoords) {
        AvatarRenderer<AbstractClientPlayer> avatarRenderer = this.entityRenderDispatcher.getPlayerRenderer(this.minecraft.player);
        Identifier skinTexture = this.minecraft.player.getSkin().body().texturePath();
        if (arm == HumanoidArm.RIGHT) {
            avatarRenderer.renderRightHand(
                    poseStack, submitNodeCollector, lightCoords, skinTexture, this.minecraft.player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE)
            );
        } else {
            avatarRenderer.renderLeftHand(
                    poseStack, submitNodeCollector, lightCoords, skinTexture, this.minecraft.player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE)
            );
        }
    }


    private float calculateMapTilt(float xRot) {
        float tilt = 1.0F - xRot / 45.0F + 0.1F;
        tilt = Mth.clamp(tilt, 0.0F, 1.0F);
        return -Mth.cos(tilt * (float) Math.PI) * 0.5F + 0.5F;
    }



    @Shadow
    protected abstract void renderPlayerArm(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, float inverseArmHeight, float attackValue, HumanoidArm arm);

}