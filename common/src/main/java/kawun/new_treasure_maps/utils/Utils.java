package kawun.new_treasure_maps.utils;

import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.NewTreasureMaps;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.joml.Matrix4f;
import org.joml.Vector2i;

import java.awt.*;

public class Utils {


    public static Identifier identifier(String id) {
        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, id);
    }



    public static Vector2i getRandomPoint(int min, int max) {
        int v1 = (int) (Math.random() * (max - min)) + min;
        if (Math.random() > 0.5) {
            v1 *= -1;
        }
        int v2 = (int) (Math.random() * (max * 2 + 1)) - max;
        Vector2i pos;
        if (Math.random() > 0.5) {
            pos = new Vector2i(v1, v2);
        } else {
            pos = new Vector2i(v2, v1);
        }
        return pos;
    }


    public static int getRandomRange(int min, int max) {
        return (int) (Math.random() * (max - min + 1)) + min;
    }



    public static ServerPlayer getLocalPlayer() {
        return NewTreasureMaps.server.getPlayerList().getPlayers().getFirst();
    }


    public static void sendMessage(String text) {
        getLocalPlayer().sendSystemMessage(Component.literal(text));
    }



    public static void sendPos(BlockPos pos) {
        sendPos(pos, "Pos: ");
    }


    public static void sendPos(BlockPos pos, String text) {
        String coords = pos.getX() + " " + pos.getY() + " " + pos.getZ();
        getLocalPlayer().sendSystemMessage(Component.literal( text + ": " + coords)
                .withStyle(s -> s.withClickEvent(new ClickEvent.RunCommand("/tp @s " + coords))));
    }


    public static void sendErrorCreateMap() {
        getLocalPlayer().sendSystemMessage(Component
                .literal("Карта не сгенерировалась")
                .withColor(new Color(188, 51, 69).getRGB()));
    }


    public static String matrixToString(Matrix4f m) {
        return "\n" + String.format(
                "%5.2ff; %5.2ff; %5.2ff; %5.2ff;\n" +
                "%5.2ff; %5.2ff; %5.2ff; %5.2ff;\n" +
                "%5.2ff; %5.2ff; %5.2ff; %5.2ff;\n" +
                "%5.2ff; %5.2ff; %5.2ff; %5.2ff\n",
                m.m00(), m.m01(), m.m02(), m.m03(),
                m.m10(), m.m11(), m.m12(), m.m13(),
                m.m20(), m.m21(), m.m22(), m.m23(),
                m.m30(), m.m31(), m.m32(), m.m33()
        ).replace(',', '.').replace(';', ',');
    }

}
