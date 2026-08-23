package kawun.new_treasure_maps.maps;


import kawun.new_treasure_maps.client.texture.MapTextureManager;
import kawun.new_treasure_maps.network.MapPacket;
import kawun.new_treasure_maps.utils.pixels.Pixels;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import org.joml.Vector2i;


public abstract class BaseColoredMapCreate extends BaseMapCreate {


    @Override
    public void start() {
        BlockPos chestBlockPos = findPlaceChest(6, 10);
        if (chestBlockPos == null) {
            errorGenerate();
            return;
        }
        createChest(chestBlockPos);

        Vector2i chestPos = new Vector2i(chestBlockPos.getX(), chestBlockPos.getZ());
        Vector2i start = fromPosition.add(chestPos, new Vector2i()).div(2).sub(128, 128);

        Pixels pixels = generateMap(start);

        chestPos.sub(start, start);

        save(modify(pixels, start, chestBlockPos));
    }


    protected abstract byte[] modify(Pixels pixels, Vector2i crossPos, BlockPos chestPos);



    public static void clientHandle(MapPacket packet) {
        Pixels pixels = new Pixels(packet.bytes());
        pixels.converter = MapColor::getColorFromPackedId;
        MapTextureManager.insertPixels(packet.id(), pixels);
    }



    public Pixels generateMap(Vector2i start) {
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


}
