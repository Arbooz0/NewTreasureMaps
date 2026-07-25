package kawun.new_treasure_maps.client.animation;

import com.mojang.math.Axis;
import kawun.new_treasure_maps.client.utils.HandHelper;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class AccordionAnimation extends Animation {



    public static final Matrix4f rightMapMatrix = new Matrix4f(
            -0.50f,  0.00f,  0.00f,  0.00f,
            0.00f,  0.00f, -0.50f,  0.00f,
            0.00f, -0.50f,  0.00f,  0.00f,
            -0.38f,  0.65f,  0.00f,  1.00f
    );
    public static final Matrix4f leftMapMatrix = new Matrix4f(
            -0.50f,  0.00f,  0.00f,  0.00f,
            0.00f,  0.00f, -0.50f,  0.00f,
            0.00f, -0.50f,  0.00f,  0.00f,
            0.38f,  0.65f,  0.00f,  1.00f
    );



    @Override
    public Matrix4f getMapOffsetInHand(HumanoidArm arm) {
        return (arm == HumanoidArm.RIGHT ? rightMapMatrix : leftMapMatrix).set(3, 2, -0.05f);
    }


    @Override
    public void animate(
    ) {

        poseStack.mulPose(Axis.XP.rotationDegrees(time.part2 * 3));

        Vector3f pos1 = animateArm(arm.getOpposite()).getTranslation(new Vector3f());

        Matrix4f matrixArm = animateArm(arm);
        Vector3f pos2 = matrixArm.getTranslation(new Vector3f());

        float scale = 0.5f + time.part2 * 0.05f;
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Axis.XP.rotationDegrees(-8.0F));
        poseStack.last().pose().setTranslation(pos1.add(pos2).mul(0.5f));
        poseStack.translate(0, time.part2 * 0.12f, 0);
        poseStack.last().pose().lerp(matrixArm, 1 - time.partTransition);

    }


    private Matrix4f animateArm(HumanoidArm arm) {
        poseStack.pushPose();
        poseStack.pushPose();

        float invert = getInvert(arm);
        poseStack.translate(invert * time.part2 * 0.08, -0.5 + (invert * 0.0001) + (time.part2 * 0.08f), -0.2 - (time.part2 * 0.2));
        poseStack.mulPose(Axis.YP.rotationDegrees( 180));
        poseStack.mulPose(Axis.XP.rotationDegrees( 90 - (time.part2 * 30)));
        poseStack.mulPose(Axis.ZP.rotationDegrees( invert * (-28 + (time.part2 * 38))));
        Matrix4f matrix = poseStack.last().pose();

        poseStack.popPose();

        if (this.arm != arm) {
            poseStack.translate(0, -0.5f, 0);
        }
        applyDefaultArmPose(arm);
        poseStack.last().pose().lerp(matrix, time.part1);

        applyHand(arm);
        poseStack.mulPose(getMapOffsetInHand(arm));

        Matrix4f matrixResult = poseStack.last().pose();

        poseStack.popPose();

        return matrixResult;
    }


}
