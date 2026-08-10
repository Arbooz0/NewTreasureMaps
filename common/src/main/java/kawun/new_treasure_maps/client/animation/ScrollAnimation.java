package kawun.new_treasure_maps.client.animation;

import com.mojang.math.Axis;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class ScrollAnimation extends Animation {

    public static final Matrix4f rightMapMatrix = new Matrix4f(
            0.00f,  0.00f,  0.50f,  0.00f,
            -0.50f,  0.00f,  0.00f,  0.00f,
            0.00f, -0.50f,  0.00f,  0.00f,
            -0.38f,  0.72f, -0.00f,  1.00f
    );
    public static final Matrix4f leftMapMatrix = new Matrix4f(
            0.00f,  0.00f,  0.50f,  0.00f,
            -0.50f,  0.00f,  0.00f,  0.00f,
            0.00f, -0.50f,  0.00f,  0.00f,
            0.38f,  0.72f, -0.00f,  1.00f
    );

    public static final Matrix4f rightMapThirdPersonMatrix = new Matrix4f(
            0.00f,  0.00f,  0.40f,  0.00f,
            -0.40f,  0.00f,  0.00f,  0.00f,
            0.00f, -0.40f,  0.00f,  0.00f,
            -0.038f,  0.62f, -0.00f,  1.00f
    );
    public static final Matrix4f leftMapThirdPersonMatrix = new Matrix4f(
            0.00f,  0.00f,  0.40f,  0.00f,
            -0.40f,  0.00f,  0.00f,  0.00f,
            0.00f, -0.40f,  0.00f,  0.00f,
            0.038f,  0.62f, -0.00f,  1.00f
    );


    @Override
    public Matrix4f getMapOffsetInHand(HumanoidArm arm) {
        return arm == HumanoidArm.RIGHT ? rightMapMatrix : leftMapMatrix;
    }

    @Override
    public Matrix4f getMapOffsetThirdPerson(HumanoidArm arm) {
        return arm == HumanoidArm.RIGHT ? rightMapThirdPersonMatrix : leftMapThirdPersonMatrix;
    }


    @Override
    protected void animate() {
        Vector3f pos1 = animateArmFirstPerson(arm.getOpposite()).getTranslation(new Vector3f());

        Matrix4f matrixArm = animateArmFirstPerson(arm);
        Vector3f pos2 = matrixArm.getTranslation(new Vector3f());

        float scale = 0.5f + time.part2 * 0.05f;
        poseStack.scale(scale, scale, scale);
        float t = 1 - Math.min(time.part2 * 10, 1) / 2.0f;
        poseStack.last().pose().setTranslation(pos1.lerp(pos2, t));
        poseStack.last().pose().lerp(matrixArm, 1 - time.partTransition);
    }


    private Matrix4f animateArmFirstPerson(HumanoidArm arm) {
        poseStack.pushPose();
        poseStack.pushPose();

        float invert = getInvert(arm);
        poseStack.mulPose(Axis.XP.rotationDegrees(10));
        poseStack.translate(invert * 0.4f, -0.5f, -0.2f - (time.part2 * 0.15f));
        poseStack.mulPose(Axis.YP.rotationDegrees( 90));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-90));
        poseStack.translate((-0.25f - (0.1f * time.part2)) + (0.2f - (0.15f * time.part2)) * invert, 0, 0);
        poseStack.mulPose(Axis.XP.rotationDegrees( -30 * invert));
        poseStack.mulPose(Axis.ZP.rotationDegrees( invert * (-10 + (time.part2 * 30))));
        Matrix4f matrix = poseStack.last().pose();

        poseStack.popPose();

        if (this.arm != arm && !mainHandEmpty) {
            poseStack.translate(0, (1 - time.part1) * -0.5f, 0);
        }
        applyDefaultArmPose(arm);

        if (time.part1 == 1) {
            poseStack.last().pose().set(matrix);
        } else {
            Matrix4f m = poseStack.last().pose();
            lerpMatrix(m, matrix, time.part1);
        }

        applyHand(arm);
        poseStack.mulPose(getMapOffsetInHand(arm));

        Matrix4f matrixResult = poseStack.last().pose();

        poseStack.popPose();

        return matrixResult;
    }




    @Override
    protected void animateThirdPerson() {
        if (time.partTransition == 0) {
            translateToHand(arm);
            poseStack.mulPose(getMapOffsetThirdPerson(arm));
        } else {
            poseStack.pushPose();
            translateToHand(arm.getOpposite());
            poseStack.mulPose(getMapOffsetThirdPerson(arm.getOpposite()));
            Vector3f pos1 = poseStack.last().pose().getTranslation(new Vector3f());
            poseStack.popPose();

            poseStack.pushPose();
            translateToHand(arm);
            poseStack.mulPose(getMapOffsetThirdPerson(arm));
            Matrix4f matrix = poseStack.last().pose();
            Vector3f pos2 = matrix.getTranslation(new Vector3f());
            poseStack.popPose();

            float scale = 0.4f;
            poseStack.scale(scale, scale, scale);
            float t = 1 - Math.min(time.part2 * 10, 1) / 2.0f;
            poseStack.last().pose().setTranslation(pos1.lerp(pos2, t));
            poseStack.mulPose(Axis.XP.rotation(Math.min(getHeadRotationX(), 0.5f) - 1.5f + (float) (Math.PI * 0.5)));
            poseStack.mulPose(Axis.ZP.rotationDegrees(180));
            poseStack.translate(0, 0, -0.2f);

            poseStack.last().pose().lerp(matrix, 1 - time.partTransition);
        }
    }

    @Override
    protected void animateArm() {
        animateArm(HumanoidArm.RIGHT);
        animateArm(HumanoidArm.LEFT);
    }

    private void animateArm(HumanoidArm arm) {
        float invert = getInvert(arm);

        Matrix3f rot = new Matrix3f();
        rot.rotateLocalZ(-0.55f * invert);
        rot.rotateLocalX(Math.min(getHeadRotationX(), 0.5f) - 1.5f - 0.6f * invert * time.part2);
        rot.rotateY((float) (Math.PI / -2.0) * time.part1);

        Vector3f newAngles = rot.getEulerAnglesZYX(new Vector3f());

        lerpArm(arm, newAngles.x, newAngles.y, newAngles.z, time.part1);

        getArm(arm).y -= time.part2 * invert * 0.8f;
        getArm(arm).z -= time.part2;
    }
}
