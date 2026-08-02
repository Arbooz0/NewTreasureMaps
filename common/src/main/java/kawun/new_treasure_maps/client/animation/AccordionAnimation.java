package kawun.new_treasure_maps.client.animation;

import com.mojang.math.Axis;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class AccordionAnimation extends Animation {



    public static final Matrix4f rightMapMatrix = new Matrix4f(
            -0.50f,  0.00f,  0.00f,  0.00f,
            0.00f,  0.00f, -0.50f,  0.00f,
            0.00f, -0.50f,  0.00f,  0.00f,
            -0.38f,  0.65f,  -0.05f,  1.00f
    );
    public static final Matrix4f leftMapMatrix = new Matrix4f(
            -0.50f,  0.00f,  0.00f,  0.00f,
            0.00f,  0.00f, -0.50f,  0.00f,
            0.00f, -0.50f,  0.00f,  0.00f,
            0.38f,  0.65f,  -0.05f,  1.00f
    );

    public static final Matrix4f rightMapThirdPersonMatrix = new Matrix4f(
            -0.40f,  0.00f,  0.00f,  0.00f,
            0.00f,  0.00f, -0.40f,  0.00f,
            0.00f, -0.40f,  0.00f,  0.00f,
            -0.038f,  0.55f,  -0.05f,  1.00f
    );
    public static final Matrix4f leftMapThirdPersonMatrix = new Matrix4f(
            -0.40f,  0.00f,  0.00f,  0.00f,
            0.00f,  0.00f, -0.40f,  0.00f,
            0.00f, -0.40f,  0.00f,  0.00f,
            0.038f,  0.55f,  -0.05f,  1.00f
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
    protected void animate(
    ) {
        poseStack.mulPose(Axis.XP.rotationDegrees(time.part2 * 3));

        Vector3f pos1 = animateArmFirstPerson(arm.getOpposite()).getTranslation(new Vector3f());

        Matrix4f matrixArm = animateArmFirstPerson(arm);
        Vector3f pos2 = matrixArm.getTranslation(new Vector3f());

        float scale = 0.5f + time.part2 * 0.05f;
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Axis.XP.rotationDegrees(-8.0F));
        poseStack.last().pose().setTranslation(pos1.add(pos2).mul(0.5f));
        poseStack.translate(0, time.part2 * 0.12f, 0);
        poseStack.last().pose().lerp(matrixArm, 1 - time.partTransition);
    }

    private Matrix4f animateArmFirstPerson(HumanoidArm arm) {
        poseStack.pushPose();
        poseStack.pushPose();

        float invert = getInvert(arm);
        poseStack.translate(invert * time.part2 * 0.08, -0.5 + (invert * 0.0001) + (time.part2 * 0.08f), -0.2 - (time.part2 * 0.2));
        poseStack.mulPose(Axis.YP.rotationDegrees( 180));
        poseStack.mulPose(Axis.XP.rotationDegrees( 90 - (time.part2 * 30)));
        poseStack.mulPose(Axis.ZP.rotationDegrees( invert * (-28 + (time.part2 * 38))));
        Matrix4f matrix = poseStack.last().pose();

        poseStack.popPose();

        if (this.arm != arm && !mainHandEmpty) {
            poseStack.translate(0, (1 - time.part1) * -0.5f, 0);
        }
        applyDefaultArmPose(arm);
        poseStack.last().pose().lerp(matrix, time.part1);

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
            Matrix4f matrix = poseStack.last().pose();
            poseStack.popPose();

            translateToHand(arm);
            poseStack.mulPose(getMapOffsetThirdPerson(arm));
            poseStack.last().pose().lerp(matrix, time.partTransition / 2);
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
        rot.rotateLocalZ(lerp(-0.55f, 0.15f, time.part2) * invert);
        rot.rotateLocalX(Math.min(getHeadRotationX(), 0.7f) - 1.5f - (invert * 0.001f));
        Vector3f newAngles = rot.getEulerAnglesZYX(new Vector3f());

        lerpArm(arm, newAngles.x, newAngles.y, newAngles.z, time.part1);
    }


}
