package kawun.new_treasure_maps.maps;


import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.client.texture.MapTextureManager;
import kawun.new_treasure_maps.enums.MapType;
import kawun.new_treasure_maps.network.MapPacket;
import kawun.new_treasure_maps.utils.Utils;
import kawun.new_treasure_maps.utils.pixels.Pixels;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.MapColor;
import org.joml.Vector2i;

import java.text.DecimalFormat;
import java.util.Arrays;


public class AreaColoredMapCreate extends BaseColoredMapCreate {


    @Override
    public void start() {
        canChestUnderWater = false;
        checkPlaceCross = true;
        super.start();
    }


    @Override
    public byte[] modify(Pixels pixels, Vector2i crossPos, BlockPos chestPos) {
        placeCross(chestPos, false);

        final int radius = 50;
        crossPos.add(Utils.getRandomVector(radius));

        pixels.drawCircle(crossPos, radius, MapColor.COLOR_RED.getPackedId(MapColor.Brightness.LOWEST));

        byte[] bytes = Arrays.copyOf(pixels.pixels, pixels.pixels.length + 2);
        bytes[bytes.length - 2] = (byte) crossPos.x;
        bytes[bytes.length - 1] = (byte) crossPos.y;
        return bytes;
    }

    @Override
    public MapType getMapType() {
        return MapType.AREA_COLORED;
    }


    public static void clientHandle(MapPacket packet) {
        byte[] bytes = packet.bytes();
        int x = bytes[bytes.length - 2] & 0xFF;
        int y = bytes[bytes.length - 1] & 0xFF;
        bytes = Arrays.copyOf(bytes, bytes.length - 2);

        Pixels pixels = new Pixels(bytes);
        pixels.addCopyImage("red_circle", x - 50, y - 50);
        pixels.converter = MapColor::getColorFromPackedId;
        MapTextureManager.insertPixels(packet.id(), pixels);
    }


}
