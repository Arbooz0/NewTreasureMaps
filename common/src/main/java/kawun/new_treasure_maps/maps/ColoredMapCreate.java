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
        int lastY;
        boolean nextLow = false;
        int[] prevColumnY = new int[256];

        for (int x = 0; x < 256; x++) {
            lastY = 0;
            for (int y = 0; y < 256; y++) {
                int posX = start.x + x;
                int posZ = start.y + y;

                LevelChunk chunk = level.getChunk(SectionPos.blockToSectionCoord(posX), SectionPos.blockToSectionCoord(posZ));

                int posY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, posX, posZ);
                blockPos.set(posX, posY, posZ);
                BlockState state = chunk.getBlockState(blockPos);
                MapColor color = state.getMapColor(level, blockPos);

                MapColor.Brightness b;
                if (color == MapColor.WATER) {
                    int floorY = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR, posX, posZ);
                    floorY = posY - floorY;
                    floorY += ((x + y) & 1) * 3;
                    if (floorY < 6) {
                        b = MapColor.Brightness.HIGH;
                    } else if (floorY < 12) {
                        b = MapColor.Brightness.NORMAL;
                    } else if (floorY < 20) {
                        b = MapColor.Brightness.LOW;
                    } else {
                        b = MapColor.Brightness.LOWEST;
                    }
                    nextLow = false;
                } else {
                    if (posY == lastY) {
                        int prev_y = prevColumnY[y];
                        if (posY == prev_y) {
                            if (nextLow) {
                                b = MapColor.Brightness.LOW;
                            } else {
                                b = MapColor.Brightness.NORMAL;
                            }
                        } else if (posY > prev_y) {
                            b = MapColor.Brightness.HIGH;
                        } else {
                            b = MapColor.Brightness.LOW;
                        }
                        nextLow = false;
                    } else if (posY > lastY) {
                        b = MapColor.Brightness.HIGH;
                        nextLow = false;
                    } else {
                        if ((lastY - posY) > 2) {
                            b = MapColor.Brightness.LOWEST;
                            nextLow = true;
                        } else {
                            b = MapColor.Brightness.LOW;
                            nextLow = false;
                        }
                    }
                }

                pixels.setPixel(x, y, color.getPackedId(b));

                lastY = posY;
                prevColumnY[y] = posY;
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
