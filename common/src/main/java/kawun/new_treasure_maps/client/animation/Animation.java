package kawun.new_treasure_maps.client.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import kawun.new_treasure_maps.client.utils.AnimationTime;
import kawun.new_treasure_maps.client.utils.HandHelper;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;


public abstract class Animation {

    protected PoseStack poseStack;
    protected MultiBufferSource submitNodeCollector;
    protected HumanoidArm arm;
    protected AnimationTime time;
    protected int lightCoords;

    protected boolean mainHandEmpty;

    protected PlayerModel model;



    public abstract Matrix4f getMapOffsetInHand(HumanoidArm arm);

    public abstract Matrix4f getMapOffsetThirdPerson(HumanoidArm arm);

    protected abstract void animate();

    protected abstract void animateThirdPerson();

    protected abstract void animateArm();



    public void animate(
            PoseStack poseStack,
            MultiBufferSource submitNodeCollector,
            HumanoidArm arm,
            AnimationTime time,
            int lightCoords,
            boolean mainHandEmpty
    ) {
        this.poseStack = poseStack;
        this.submitNodeCollector = submitNodeCollector;
        this.arm = arm;
        this.time = time;
        this.lightCoords = lightCoords;
        this.mainHandEmpty = mainHandEmpty;
        animate();
        this.poseStack = null;
        this.submitNodeCollector = null;
        this.arm = null;
        this.time = null;
    }


    public void animateThirdPerson(
            PoseStack poseStack,
            MultiBufferSource submitNodeCollector,
            HumanoidArm arm,
            AnimationTime time,
            PlayerModel model,
            int lightCoords
    ) {
        this.poseStack = poseStack;
        this.submitNodeCollector = submitNodeCollector;
        this.arm = arm;
        this.time = time;
        this.model = model;
        this.lightCoords = lightCoords;
        animateThirdPerson();
        this.poseStack = null;
        this.submitNodeCollector = null;
        this.arm = null;
        this.time = null;
        this.model = null;
    }


    public void animateArm(PlayerModel model, AnimationTime time, HumanoidArm arm) {
        if (this.time != null) {
            return;
        }
        this.model = model;
        this.time = time;
        this.arm = arm;
        animateArm();
        model.rightSleeve.copyFrom(model.rightArm);
        model.leftSleeve.copyFrom(model.leftArm);
        this.model = null;
        this.time = null;
        this.arm = null;
    }




    public float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }


    public void lerpMatrix(Matrix4f m1, Matrix4f m2, float t) {
        Vector3f pos = m1.getTranslation(new Vector3f()).lerp(m2.getTranslation(new Vector3f()), t);
        Quaternionf rot = m1.getNormalizedRotation(new Quaternionf()).slerp(m2.getNormalizedRotation(new Quaternionf()), t);
        m1.translationRotateScale(pos, rot, m2.getScale(new Vector3f()));
    }


    public void applyDefaultArmPose(HumanoidArm arm) {
        HandHelper.defaultArmPose(poseStack, 0, 0, arm);
    }


    public void applyHand(HumanoidArm arm) {
        HandHelper.setHand(poseStack, arm, submitNodeCollector, lightCoords);
    }


    public float getInvert(HumanoidArm arm) {
        return (arm == HumanoidArm.RIGHT) ? 1.0F : -1.0F;
    }


    public void translateToHand(HumanoidArm arm) {
        model.translateToHand(arm, poseStack);
    }


    public ModelPart getArm(HumanoidArm arm) {
        return arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;
    }

    public void lerpArm(HumanoidArm arm, float x, float y, float z, float t) {
        ModelPart part = getArm(arm);
        part.xRot = lerp(part.xRot, x, t);
        part.yRot = lerp(part.yRot, y, t);
        part.zRot = lerp(part.zRot, z, t);
    }

    public float getHeadRotationX() {
        return model.head.xRot;
    }

}
