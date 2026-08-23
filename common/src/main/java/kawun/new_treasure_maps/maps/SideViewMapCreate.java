package kawun.new_treasure_maps.maps;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.objects.Object2ByteArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.client.texture.MapTextureManager;
import kawun.new_treasure_maps.enums.MapType;
import kawun.new_treasure_maps.network.MapPacket;
import kawun.new_treasure_maps.utils.Utils;
import kawun.new_treasure_maps.utils.pixels.Pixels;
import kawun.new_treasure_maps.utils.pixels.PixelsInt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import org.joml.Vector2i;
import org.joml.Vector3i;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;


public class SideViewMapCreate extends BaseMapCreate {


    public boolean isX = true;


    @Override
    public void start() {
        BlockPos chestPos = findPlaceChest();
        if (chestPos == null) {
            errorGenerate();
            return;
        }
        createChest(chestPos);

        Vector3i start = new Vector3i();
        Vector2i crossPos = new Vector2i(0, 220);
        start.y = chestPos.getY() + (crossPos.y / 2);
        if (isX) {
            start.x = (fromPosition.x + chestPos.getX()) / 2 - 64;
            start.z = fromPosition.y;
            crossPos.x = (chestPos.getX() - start.x) * 2;
        } else {
            start.z = (fromPosition.y + chestPos.getZ()) / 2 - 64;
            start.x = fromPosition.x;
            crossPos.x = (chestPos.getZ() - start.z) * 2;
        }

        byte[] bytes = generateMap(start);
        bytes[0] = (byte) crossPos.x;
        bytes[1] = (byte) crossPos.y;

        save(bytes);
    }

    @Override
    public MapType getMapType() {
        return MapType.SIDE_VIEW;
    }


    private @Nullable BlockPos findPlaceChest() {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 20; i++) {
            int offset = Utils.getRandomRange(-32, 32);
            boolean isX = Math.random() > 0.5;
            if (isX) {
                pos.setX(fromPosition.x + offset);
                pos.setZ(fromPosition.y);
            } else {
                pos.setZ(fromPosition.y + offset);
                pos.setX(fromPosition.x);
            }

            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos);
            y = Utils.getRandomRange(y - 100, y - 90);
            pos.setY(y);

            if (canPlaceChest(pos)) {
                this.isX = isX;
                return pos;
            }
        }
        return null;
    }




    public byte[] generateMap(Vector3i start) {
        Pixels pixels = new Pixels(128);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        if (isX) {
            pos.setZ(start.z);
        } else {
            pos.setX(start.x);
        }

        Object2ByteArrayMap<Block> blocks = new Object2ByteArrayMap<>();

        for (int x = 0; x < 128; x++) {
            if (isX) {
                pos.setX(start.x + x);
            } else {
                pos.setZ(start.z + x);
            }

            for (int y = 0; y < 128; y++) {
                pos.setY(start.y - y);

                BlockState state = level.getBlockState(pos);

                if (isAir(state)) {

                    for (int l = 0; l < 20; l++) {
                        if (isX) {
                            pos.setZ(pos.getZ() - 1);
                        } else {
                            pos.setX(pos.getX() + 1);
                        }
                        state = level.getBlockState(pos);
                        if (isAir(state)) {
                            continue;
                        }

                        byte id = blocks.computeIfAbsent(state.getBlock(), b -> (byte) (blocks.size() + 1));
                        if (id < 64) {
                            int i = Math.min(l / 3, 2) + 1;
                            pixels.setPixel(x, y, (byte) (id + (i << 6)));
                        }
                        break;
                    }

                    if (isX) {
                        pos.setZ(start.z);
                    } else {
                        pos.setX(start.x);
                    }

                } else {
                    byte id = blocks.computeIfAbsent(state.getBlock(), b -> (byte) (blocks.size() + 1));
                    if (id < 64) {
                        pixels.setPixel(x, y, id);
                    }
                }
            }
        }

        if (blocks.size() >= 63) {
            Constants.LOG.error("Many blocks: " + blocks.size() + " ( >= 63)");
            for (Object2ByteMap.Entry<Block> entry : blocks.object2ByteEntrySet()) {
                if (entry.getByteValue() >= 64) {
                    blocks.removeByte(entry.getKey());
                }
            }
        }

        ByteArrayList bytes = new ByteArrayList();
        bytes.add((byte) 0);
        bytes.add((byte) 0);
        bytes.add((byte) blocks.size());

        for (Block block : blocks.keySet()) {
            Identifier id = BuiltInRegistries.BLOCK.getKey(block);
            String idText = id.getNamespace().equals("minecraft") ? id.getPath() : id.toString();
            byte[] idBytes = idText.getBytes(StandardCharsets.US_ASCII);
            bytes.add((byte) idBytes.length);
            bytes.addElements(bytes.size(), idBytes);
        }

        bytes.addElements(bytes.size(), pixels.pixels);

        return bytes.toByteArray();
    }


    private boolean isAir(BlockState state) {
        if (state.isAir()) {
            return true;
        }
        if (state.is(Blocks.WATER) || state.is(Blocks.LAVA)) {
            return false;
        }
        return state.is(BlockTags.REPLACEABLE) || state.is(BlockTags.FLOWERS);
    }



    public static void clientHandle(MapPacket packet) {
        byte[] bytes = packet.bytes();

        int countBlocks = bytes[2];
        int i = 3;

        Byte2ObjectOpenHashMap<NativeImage> images = new Byte2ObjectOpenHashMap<>();
        byte stoneId = -1;

        for (int n = 0; n < countBlocks; n++) {
            int l = bytes[i];
            i++;
            String idText = new String(bytes, i, l, StandardCharsets.US_ASCII);
            if (idText.equals("stone")) {
                stoneId = (byte) (n + 1);
            }
            i += l;
            Identifier id = Identifier.parse(idText);
            NativeImage image = MapTextureManager.loadBlockTexture(id);
            if (image != null) {
                images.put((byte) (n + 1), image);
            }
        }

        int[] blackoutY = new int[256];
        for (int x = 0; x < 128; x++) {
            for (int y = 0; y < 128; y++) {
                int b = bytes[i + x + y * 128] & 0xFF;
                byte id = (byte) (b & 63);
                if (id != stoneId) {
                    continue;
                }
                int layer = b >> 6;
                if (layer == 0) {
                    blackoutY[x * 2] = y * 2;
                    blackoutY[x * 2 + 1] = y * 2;
                    break;
                }
            }
        }

        for (int x = 1; x < 256; x++) {
            if ((blackoutY[x]) > blackoutY[x - 1]) {
                blackoutY[x] = blackoutY[x - 1] + 2;
            }
        }
        for (int x = 254; x >= 0; x--) {
            if ((blackoutY[x]) > blackoutY[x + 1]) {
                blackoutY[x] = blackoutY[x + 1] + 2;
            }
        }

        PixelsInt pixels = new PixelsInt(256);
        boolean[] lastColumnIsStone = new boolean[256];
        int blackColor = ARGB.color(50, 50, 50);

        for (int x = 0; x < 256; x++) {
            for (int y = 0; y < 256; y++) {
                int b = bytes[i + x / 2 + y / 2 * 128] & 0xFF;
                byte id = (byte) (b & 63);
                int layer = b >> 6;

                boolean isStone = id == stoneId;
                if (layer == 0) {
                    if (y > 0) {
                        if (isStone != lastColumnIsStone[y - 1]) {
                            if (isStone) {
                                pixels.setPixel(x, y, blackColor);
                                lastColumnIsStone[y] = isStone;
                                continue;
                            } else {
                                if (id != 0) {
                                    pixels.setPixel(x, y - 1, blackColor);
                                }
                            }
                        }
                    }
                    if (x > 0) {
                        if (isStone != lastColumnIsStone[y]) {
                            if (isStone) {
                                pixels.setPixel(x, y, blackColor);
                                lastColumnIsStone[y] = isStone;
                                continue;
                            } else {
                                if (id != 0) {
                                    pixels.setPixel(x - 1, y, blackColor);
                                }
                            }
                        }
                    }
                }

                lastColumnIsStone[y] = isStone;

                if (!images.containsKey(id)) {
                    if (y > blackoutY[x]) {
                        pixels.setPixel(x, y, 220 << 24);
                    }
                    continue;
                }

                NativeImage image = images.get(id);
                int color = image.getPixel(x % 6, y % 6);
                if (layer > 0) {
                    if (y > blackoutY[x]) {
                        color = ARGB.scaleRGB(color, 1.0f - (layer / 4.0f));
                    } else {
                        color = ARGB.multiplyAlpha(color, 1.0f - (layer / 4.0f));
                    }
                }
                pixels.setPixel(x, y, color);
            }
        }

        for (NativeImage image : images.values()) {
            image.close();
        }

        pixels.addCopyImage("cross", bytes[0] & 0xFF - 5, bytes[1] & 0xFF - 5, false);

        MapTextureManager.insertPixels(packet.id(), pixels);
    }





}
