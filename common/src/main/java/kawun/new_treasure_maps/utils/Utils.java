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


    public static Vector2i getRandomVector(double maxRadius) {
        double offset = Math.random() * (maxRadius - (maxRadius / 5.0)) + (maxRadius / 10.0);
        double angle = Math.random() * Math.TAU;
        return new Vector2i((int) (Math.cos(angle) * offset), (int) (Math.sin(angle) * offset));
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

}
