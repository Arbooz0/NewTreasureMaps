package kawun.new_treasure_maps.maps;


import kawun.new_treasure_maps.client.texture.MapTextureManager;
import kawun.new_treasure_maps.enums.MapType;
import kawun.new_treasure_maps.items.Items;
import kawun.new_treasure_maps.network.MapPacket;
import kawun.new_treasure_maps.saveddata.FreeID;
import kawun.new_treasure_maps.saveddata.MapSavedData;
import kawun.new_treasure_maps.utils.Pixels;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import org.joml.Vector2i;


public class ColoredMapCreate {



    public static final Pixels CROSS;



    public static ItemStack create(Vector2i from, Level level) {
        Vector2i chestPos = Maps.getRandomPoint(50, 100).add(from);

        Maps.createChest(chestPos.x, chestPos.y, level);

        Vector2i start = from.add(chestPos, new Vector2i()).div(2).sub(128, 128);

        Pixels pixels = generateMap(start, level);

        chestPos.sub(start, start);

        pixels.drawImage(start, CROSS);

        int id = FreeID.getFreeID();
        MapSavedData data = new MapSavedData(id, MapType.COLORED, pixels.pixels);
        data.save();

        return Items.newTreasureMap(id);
    }


    public static void clientHandle(MapPacket packet) {
        Pixels pixels = new Pixels(packet.bytes());
        pixels.converter = MapColor::getColorFromPackedId;
        MapTextureManager.insertPixels(packet.id(), pixels);
    }






    public static Pixels generateMap(Vector2i start, Level level) {
        Pixels pixels = new Pixels(256);

        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        int last_y;
        boolean next_low = false;
        int[] prev_column_y = new int[256];

        for (int x = 0; x < 256; x++) {
            last_y = 0;
            for (int y = 0; y < 256; y++) {
                int pos_x = start.x + x;
                int pos_z = start.y + y;

                LevelChunk chunk = level.getChunk(SectionPos.blockToSectionCoord(pos_x), SectionPos.blockToSectionCoord(pos_z));

                int pos_y = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, pos_x, pos_z);
                blockPos.set(pos_x, pos_y, pos_z);
                BlockState state = chunk.getBlockState(blockPos);
                MapColor color = state.getMapColor(level, blockPos);

                MapColor.Brightness b;
                if (color == MapColor.WATER) {
                    int floor_y = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR, pos_x, pos_z);
                    floor_y = pos_y - floor_y;
                    floor_y += ((x + y) & 1) * 3;
                    if (floor_y < 6) {
                        b = MapColor.Brightness.HIGH;
                    } else if (floor_y < 12) {
                        b = MapColor.Brightness.NORMAL;
                    } else if (floor_y < 20) {
                        b = MapColor.Brightness.LOW;
                    } else {
                        b = MapColor.Brightness.LOWEST;
                    }
                    next_low = false;
                } else {
                    if (pos_y == last_y) {
                        int prev_y = prev_column_y[y];
                        if (pos_y == prev_y) {
                            if (next_low) {
                                b = MapColor.Brightness.LOW;
                            } else {
                                b = MapColor.Brightness.NORMAL;
                            }
                        } else if (pos_y > prev_y) {
                            b = MapColor.Brightness.HIGH;
                        } else {
                            b = MapColor.Brightness.LOW;
                        }
                        next_low = false;
                    } else if (pos_y > last_y) {
                        b = MapColor.Brightness.HIGH;
                        next_low = false;
                    } else {
                        if ((last_y - pos_y) > 2) {
                            b = MapColor.Brightness.LOWEST;
                            next_low = true;
                        } else {
                            b = MapColor.Brightness.LOW;
                            next_low = false;
                        }
                    }
                }

                pixels.setPixel(x, y, color.getPackedId(b));

                last_y = pos_y;
                prev_column_y[y] = pos_y;
            }
        }

        return pixels;
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
