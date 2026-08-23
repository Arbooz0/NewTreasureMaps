package kawun.new_treasure_maps.maps;


import kawun.new_treasure_maps.enums.MapType;
import kawun.new_treasure_maps.utils.pixels.Pixels;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.MapColor;
import org.joml.Vector2i;


public class ColoredMapCreate extends BaseColoredMapCreate {



    public static final Pixels CROSS;


    @Override
    public byte[] modify(Pixels pixels, Vector2i crossPos, BlockPos chestPos) {
        pixels.drawImage(crossPos, CROSS);
        return pixels.pixels;
    }

    @Override
    public MapType getMapType() {
        return MapType.COLORED;
    }



    static {
        byte b = MapColor.COLOR_BLACK.getPackedId(MapColor.Brightness.NORMAL);
        byte r = MapColor.COLOR_RED.getPackedId(MapColor.Brightness.NORMAL);
        byte l = MapColor.COLOR_RED.getPackedId(MapColor.Brightness.HIGH);
        byte[] bytes = new byte[]{
            b, b, b, 0, b, b, b,
            b, r, r, b, r, r, b,
            b, r, l, r, l, r, b,
            0, b, r, l, r, b, 0,
            b, r, l, r, l, r, b,
            b, r, r, b, r, r, b,
            b, b, b, 0, b, b, b,
        };
        CROSS = new Pixels(bytes);
    }
}
