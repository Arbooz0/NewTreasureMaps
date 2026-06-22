package kawun.new_treasure_maps.mixin;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.client.render.MapRenderer;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ItemInHandLayer.class)
public abstract class MixinItemInHandLayer<S extends net.minecraft.client.renderer.entity.state.ArmedEntityRenderState, M extends EntityModel<S> & ArmedModel> extends RenderLayer<S, M> {

    public MixinItemInHandLayer(RenderLayerParent<S, M> renderer) {
        super(renderer);
    }
/*
    @Inject(at = @At("HEAD"), method = "submitArmWithItem", cancellable = true)
    private void submitArmWithItem(S state, ItemStackRenderState item, ItemStack itemStack, HumanoidArm arm, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci) {
        //Constants.LOG.info("submitArmWithItem mixin: " + state.entityType + ", " + itemStack.getItem());
        poseStack.scale(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        submitNodeCollector.submitCustomGeometry(poseStack, MapRenderer.MAP_BACKGROUND, (pose, vertexConsumer) -> {
            vertexConsumer.addVertex(pose, -7.0F, 7.0F, 0.0F).setColor(-1).setUv(0.0F, 1.0F).setLight(lightCoords);
            vertexConsumer.addVertex(pose, 7.0F, 7.0F, 0.0F).setColor(-1).setUv(1.0F, 1.0F).setLight(lightCoords);
            vertexConsumer.addVertex(pose, 7.0F, -7.0F, 0.0F).setColor(-1).setUv(1.0F, 0.0F).setLight(lightCoords);
            vertexConsumer.addVertex(pose, -7.0F, -7.0F, 0.0F).setColor(-1).setUv(0.0F, 0.0F).setLight(lightCoords);
        });
        submitNodeCollector.submitCustomGeometry(poseStack, MapRenderer.MAP_BACKGROUND, (pose, vertexConsumer) -> {
            vertexConsumer.addVertex(pose, -7.0f, -7.0f, 0.0f).setColor(-1).setUv(0, 0).setLight(lightCoords);
            vertexConsumer.addVertex(pose, 7.0F, -7.0f, 0.0f).setColor(-1).setUv(1, 0).setLight(lightCoords);
            vertexConsumer.addVertex(pose, 7.0F, 7.0F, 0.0f).setColor(-1).setUv(1, 1).setLight(lightCoords);
            vertexConsumer.addVertex(pose, -7.0f, 7.0F, 0.0f).setColor(-1).setUv(0, 1).setLight(lightCoords);
        });


        //ci.cancel();
    }*/
}
