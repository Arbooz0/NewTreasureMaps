package kawun.new_treasure_maps.client.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import kawun.new_treasure_maps.client.utils.AnimationTime;
import kawun.new_treasure_maps.client.utils.HandHelper;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Matrix4f;


public abstract class Animation {

    protected PoseStack poseStack;
    protected SubmitNodeCollector submitNodeCollector;
    protected HumanoidArm arm;
    protected AnimationTime time;
    protected int lightCoords;
    protected float inverseArmHeight;
    protected float attack;




    public abstract Matrix4f getMapOffsetInHand(HumanoidArm arm);

    protected abstract void animate();


    public void animate(
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            HumanoidArm arm,
            AnimationTime time,
            int lightCoords,
            float inverseArmHeight,
            float attack
    ) {
        this.poseStack = poseStack;
        this.submitNodeCollector = submitNodeCollector;
        this.arm = arm;
        this.time = time;
        this.lightCoords = lightCoords;
        this.inverseArmHeight = inverseArmHeight;
        this.attack = attack;
        animate();
        this.poseStack = null;
        this.submitNodeCollector = null;
        this.arm = null;
        this.time = null;
    }



    public void applyDefaultArmPose(HumanoidArm arm) {
        HandHelper.defaultArmPose(poseStack, inverseArmHeight, attack, arm);
    }


    public void applyHand(HumanoidArm arm) {
        HandHelper.setHand(poseStack, arm, submitNodeCollector, lightCoords);
    }


    public float getInvert(HumanoidArm arm) {
        return (arm == HumanoidArm.RIGHT) ? 1.0F : -1.0F;
    }

}
