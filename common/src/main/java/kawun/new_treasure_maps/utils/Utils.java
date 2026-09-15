package kawun.new_treasure_maps.utils;

import kawun.new_treasure_maps.Constants;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2i;


public class Utils {


    public static ResourceLocation identifier(String id) {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, id);
    }



    public static Vector2i getRandomPoint(int min, int max) {
        int v1 = getRandomRange(min, max);
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



    public static void sendErrorCreateMap() {
        Constants.LOG.error("Map not generated");
    }

}
