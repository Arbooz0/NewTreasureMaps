package kawun.new_treasure_maps.commands;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.client.model.MapModelGenerator;
import kawun.new_treasure_maps.client.render.MapRenderer;
import kawun.new_treasure_maps.utils.Utils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.text.DecimalFormat;

public class PoseCommand {


    public static PoseStack.Pose pose = new PoseStack.Pose();
    public static PoseStack.Pose base_pose = new PoseStack.Pose();
    public static PoseStack.Pose result_pose = new PoseStack.Pose();

    public static LiteralArgumentBuilder<CommandSourceStack> get_command() {
        /*pose = new PoseStack.Pose();
        pose.pose().set(3, 2, -1.5f);
        pose.mulPose(new Matrix4f().rotateLocalX(-45 * ((float) Math.PI / 180F)));*/

        return Commands.literal("pose").then(Commands.literal("reset").executes(PoseCommand::reset))
                .then(Commands.literal("pos").then(Commands.argument("pos", Vec3Argument.vec3()).executes(PoseCommand::pos)))
                .then(Commands.literal("rotate")
                        .then(Commands.literal("X").then(Commands.argument("angle", DoubleArgumentType.doubleArg()).executes(PoseCommand::rotate_x)))
                        .then(Commands.literal("Y").then(Commands.argument("angle", DoubleArgumentType.doubleArg()).executes(PoseCommand::rotate_y)))
                        .then(Commands.literal("Z").then(Commands.argument("angle", DoubleArgumentType.doubleArg()).executes(PoseCommand::rotate_z)))
                ).then(Commands.literal("scale").then(Commands.argument("scale", Vec3Argument.vec3()).executes(PoseCommand::scale)))
                .then(Commands.literal("print").executes(PoseCommand::print))
                .then(Commands.literal("print_base").executes(PoseCommand::print_base))
                .then(Commands.literal("print_result").executes(PoseCommand::print_result))
                .then(Commands.literal("set").then(
                        Commands.argument("column", IntegerArgumentType.integer(0, 3)).then(
                                Commands.argument("row", IntegerArgumentType.integer(0, 3)).then(
                                        Commands.argument("value", DoubleArgumentType.doubleArg()).executes(PoseCommand::set)))))
                .then(Commands.literal("anim").then(Commands.argument("add", DoubleArgumentType.doubleArg()).executes(PoseCommand::anim)));
    }



    private static void chat(String message, CommandContext<CommandSourceStack> context) {
        context.getSource().getPlayer().sendSystemMessage(Component.literal(message));
        Constants.LOG.info(message);
    }


    private static void print_pose(String message, PoseStack.Pose p, CommandContext<CommandSourceStack> context) {
        Matrix4f matrix = p.pose();
        chat("+------------------+", context);
        chat(message + ":\n" + Utils.matrixToString(matrix), context);
        chat("Pos: " + matrix.getTranslation(new Vector3f()).toString(new DecimalFormat()), context);
        chat("Rot: " + matrix.getRotation(new AxisAngle4f()).toString(new DecimalFormat()), context);
        chat("Sce: " + matrix.getScale(new Vector3f()).toString(new DecimalFormat()), context);
    }



    private static int print(CommandContext<CommandSourceStack> context) {
        print_pose("Pose", pose, context);
        return 1;
    }


    private static int print_base(CommandContext<CommandSourceStack> context) {
        print_pose("Base Pose", base_pose, context);
        return 1;
    }


    private static int print_result(CommandContext<CommandSourceStack> context) {
        print_pose("Result Pose", result_pose, context);
        return 1;
    }




    private static int reset(CommandContext<CommandSourceStack> context) {
        pose.setIdentity();
        return 1;
    }


    private static int pos(CommandContext<CommandSourceStack> context) {
        Vec3 vec = Vec3Argument.getVec3(context, "pos");
        pose.translate((float) vec.x, (float) vec.y, (float) vec.z);
        return 1;
    }



    private static int rotate_x(CommandContext<CommandSourceStack> context) {
        float angle = (float) DoubleArgumentType.getDouble(context, "angle");
        pose.mulPose(new Matrix4f().rotateLocalX(angle * ((float) Math.PI / 180F)));
        return 1;
    }



    private static int rotate_y(CommandContext<CommandSourceStack> context) {
        float angle = (float) DoubleArgumentType.getDouble(context, "angle");
        pose.mulPose(new Matrix4f().rotateLocalY(angle * ((float) Math.PI / 180F)));
        return 1;
    }



    private static int rotate_z(CommandContext<CommandSourceStack> context) {
        float angle = (float) DoubleArgumentType.getDouble(context, "angle");
        pose.mulPose(new Matrix4f().rotateLocalZ(angle * ((float) Math.PI / 180F)));
        return 1;
    }





    private static int scale(CommandContext<CommandSourceStack> context) {
        Vec3 vec = Vec3Argument.getVec3(context, "scale");
        pose.scale((float) vec.x, (float) vec.y, (float) vec.z);
        return 1;
    }



    private static int set(CommandContext<CommandSourceStack> context) {
        int row = IntegerArgumentType.getInteger(context, "row");
        int column = IntegerArgumentType.getInteger(context, "column");
        float value = (float) DoubleArgumentType.getDouble(context, "value");
        pose.pose().set(column, row, value);
        return 1;
    }



    private static int anim(CommandContext<CommandSourceStack> context) {
        float value = (float) DoubleArgumentType.getDouble(context, "add");
        MapModelGenerator.anim = Math.clamp(MapModelGenerator.anim + value, 0, 1);
        return 1;
    }




}
