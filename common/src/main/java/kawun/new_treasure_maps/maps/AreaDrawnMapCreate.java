package kawun.new_treasure_maps.maps;


import kawun.new_treasure_maps.client.texture.MapTextureManager;
import kawun.new_treasure_maps.enums.MapType;
import kawun.new_treasure_maps.network.MapPacket;
import kawun.new_treasure_maps.utils.Utils;
import kawun.new_treasure_maps.utils.pixels.Pixels;
import kawun.new_treasure_maps.utils.pixels.PixelsLoader;
import net.minecraft.world.level.material.MapColor;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.util.Arrays;


public class AreaDrawnMapCreate extends BaseDrawnMapCreate {


    @Override
    public void start() {
        checkPlaceCross = true;
        super.start();
    }

    @Override
    protected byte[] modify(Pixels pixels) {
        final int radius = 10;
        crossPos.add(Utils.getRandomVector(radius));

        pixels.drawCircle(crossPos, radius, (byte) 0b1011_1111);

        byte[] bytes = Arrays.copyOf(pixels.pixels, pixels.pixels.length + 2);
        bytes[bytes.length - 2] = (byte) crossPos.x;
        bytes[bytes.length - 1] = (byte) crossPos.y;
        return bytes;
    }

    @Override
    public MapType getMapType() {
        return MapType.AREA_DRAWN;
    }



    public static void clientHandle(MapPacket packet) {
        byte[] bytes = packet.bytes();
        int x = bytes[bytes.length - 2] & 0xFF;
        int y = bytes[bytes.length - 1] & 0xFF;
        bytes = Arrays.copyOf(bytes, bytes.length - 2);

        Pixels pixels = new Pixels(bytes);
        pixels.addCopyImage("red_circle_small", x - 10, y - 10);
        pixels.converter = BaseDrawnMapCreate::convertColor;
        MapTextureManager.insertPixels(packet.id(), pixels);
    }


}
