package kawun.new_treasure_maps.maps;

import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.NewTreasureMaps;
import kawun.new_treasure_maps.client.texture.MapTextureManager;
import kawun.new_treasure_maps.enums.MapType;
import kawun.new_treasure_maps.items.Items;
import kawun.new_treasure_maps.network.MapPacket;
import kawun.new_treasure_maps.saveddata.FreeID;
import kawun.new_treasure_maps.saveddata.MapSavedData;
import kawun.new_treasure_maps.utils.Pixels;
import kawun.new_treasure_maps.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.joml.Vector2i;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LandmarksMapCreate {


    public static HashMap<String, Pixels> textures = new HashMap<>();
    public static final HashMap<TagKey<Biome>, TextureType> tags;



    public static ItemStack create(Vector2i from, Level level) {
        //Vector2i chestPos = Maps.getRandomPoint(50, 100).add(from);

        //Maps.createChest(chestPos.x, chestPos.y, level);

        //Vector2i start = from.add(chestPos, new Vector2i()).div(2).sub(128, 128);

        Pixels pixels = generateMap(from, level);

        //chestPos.sub(start, start);

        //pixels.drawImage(start, CROSS);

        int id = FreeID.getFreeID();
        MapSavedData data = new MapSavedData(id, MapType.LANDMARKS, pixels.pixels);
        data.save();

        return Items.newTreasureMap(id);
    }



    public static void clientHandle(MapPacket packet) {
        Pixels pixels = new Pixels(packet.bytes());
        pixels.converter = LandmarksMapCreate::convertColor;
        MapTextureManager.insertPixels(packet.id(), pixels);
    }


    public static int convertColor(byte color) {
        int type = ((color & 0xFF) >> 6);
        int blackout = (color >> 4) & 3;
        blackout++;
        blackout = (blackout * 255) / 4;
        int a = color & 15;
        a = ((a * 255) / 15) << 24;

        return switch (type) {
            case 1 -> ARGB.scaleRGB(a | 4352433, blackout); // Синий
            case 2 -> ARGB.scaleRGB(a | 13107250, blackout); // Красный
            case 3 -> ARGB.scaleRGB(a | 8866583, blackout); // Коричневый
            default -> a;
        };
    }


    public static byte blendColor(byte bg, byte fg) {
        int a = fg & 15;
        a -= (int) (Math.random() * 5) + 1;
        if (a <= 0) {
            return bg;
        }
        fg = (byte) ((fg & 240) | a);

        if (a == 15) {
            return fg;
        }
        if (bg == 0) {
            return fg;
        }

        int type = (bg >> 6);
        if (type == 0) {
            int bg_a = bg & 15;
            if (bg_a > a) {
                return bg;
            }
            return fg;
        } else {
            int blackout = (bg >> 4) & 3;
            blackout -= (a >> 2) + 1;
            if (blackout < 0) {
                blackout = 0;
            }
            return (byte) ((bg & 207) | (blackout << 4));
        }
    }




    public static Pixels generateMap(Vector2i from, Level level) {
        Pixels pixels = new Pixels(256);

        int startX = from.x - (128 * 4);
        int startY = from.y - (128 * 4);

        byte[] water = new byte[]{79,95,111,127};
        byte border_color = 3;

        HashMap<Vector2i, TextureType> textures = new HashMap<>();
        Holder<Biome>[] prevRow = new Holder[256];
        boolean[] waters = new boolean[65536];
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int y = 0; y < 256; y++) {
            for (int x = 0; x < 256; x++) {
                pos.setX(x * 4 + startX);
                pos.setZ(y * 4 + startY);
                Holder<Biome> biome = level.getBiome(pos);
                boolean isWater = biome.is(BiomeTags.WATER_ON_MAP_OUTLINES);
                waters[x + y * 256] = isWater;

                if (((x + y) & 1) == 0) {
                    if (x > 0) {
                        if (prevRow[x - 1] != biome) {
                            pixels.setPixel(x, y, border_color);
                        }
                    }
                    if (y > 0) {
                        if (prevRow[x] != biome) {
                            pixels.setPixel(x, y, border_color);
                        }
                    }
                }
                prevRow[x] = biome;

                if (!isWater && y > 5) {
                    Vector2i pos2d = new Vector2i(x, y);
                    boolean skip = false;
                    for (Vector2i v : textures.keySet()) {
                        if (pos2d.distanceSquared(v) < 40) {
                            skip = true;
                            break;
                        }
                    }

                    if (skip) {
                        continue;
                    }

                    for (TagKey<Biome> tag : tags.keySet()) {
                        if (biome.is(tag)) {
                            TextureType type = tags.get(tag);
                            if (Math.random() < (type == TextureType.MOUNTAINS ? 0.01 : 0.03)) {
                                textures.put(pos2d, type);
                                break;
                            }
                        }
                    }
                }
            }
        }

        int size = textures.size();

        for (int y = 0; y < 256; y++) {
            for (int x = 0; x < 256; x++) {
                boolean isWater = waters[x + y * 256];

                int nearest = 10;
                for (int addY = -2; addY <= 2; addY++) {
                    for (int addX = -2; addX <= 2; addX++) {
                        if (addX == 0 && addY == 0) {
                            continue;
                        }
                        int px = x + addX;
                        int py = y + addY;
                        if (px >= 0 && px < 256 && py >= 0 && py < 256) {
                            if (waters[px + py * 256] != isWater) {
                                int d = addX * addX + addY * addY;
                                if (d < nearest) {
                                    nearest = d;
                                }
                            }
                        }
                    }
                }

                if (isWater) {
                    if (nearest > 7) {
                        pixels.setPixel(x, y, water[3]);
                    } else {
                        nearest = (int) Math.sqrt(nearest);
                        pixels.setPixelSafe(x, y, water[nearest]);
                    }
                } else if (nearest <= 8) {
                    if (nearest <= 5) {
                        nearest = switch (nearest) {
                            case 1 -> 0;
                            case 2 -> 1;
                            case 4 -> 2;
                            case 5 -> 3;
                            default -> {
                                Constants.LOG.error("Invalid nearest: " + nearest);
                                yield 4;
                            }
                        };
                        pixels.setPixel(x, y, (byte) (4 - nearest));
                    }
                    textures.remove(new Vector2i(x, y));
                }
            }
        }

        Constants.LOG.info("DELETE: " + (size - textures.size()));

        for (Map.Entry<Vector2i, TextureType> entry : textures.entrySet()) {
            addTexture(entry.getValue(), entry.getKey(), pixels);
        }

        return pixels;
    }



    public static void addTexture(TextureType type, Vector2i pos, Pixels to) {
        Pixels texture = getTexture(type.getRandomTexture());
        if (texture == null) {
            return;
        }
        to.drawImage(pos, texture, 0.5f, 0.8f, Math.random() > 0.5, LandmarksMapCreate::blendColor);
    }




    public static @Nullable Pixels getTexture(String name) {
        if (textures.containsKey(name)) {
            return textures.get(name);
        }

        Pixels pixels = loadTexture(name);
        textures.put(name, pixels);
        return pixels;
    }


    public static @Nullable Pixels loadTexture(String name) {
        Optional<Resource> res = NewTreasureMaps.server.getResourceManager().getResource(Utils.identifier("images/" + name + ".pixels"));
        if (res.isPresent()) {
            try (InputStream stream = res.get().open()) {
                int w = stream.read();
                int h = stream.read();
                byte[] bytes = stream.readAllBytes();
                stream.close();
                return new Pixels(w, h, bytes);
            } catch (IOException e) {
                Constants.LOG.error("ERROR LOAD PIXELS " + name + ": " + e.getMessage());
            }
        }
        return null;
    }


    public enum TextureType {

        TREE("tree", 5),
        ACACIA("acacia", 3),
        SPRUCE("spruce", 4),
        GRASS("grass", 4),
        MOUNTAINS("mountains", 4);

        public final String texture;
        public final int count;

        TextureType(String texture, int count) {
            this.texture = texture;
            this.count = count;
        }

        public String getRandomTexture() {
            int n = (int) (Math.random() * count) + 1;
            return texture + n;
        }
    }


    static {
        tags = new HashMap<>();
        tags.put(TagKey.create(Registries.BIOME, Utils.identifier("tree")), TextureType.TREE);
        tags.put(TagKey.create(Registries.BIOME, Utils.identifier("acacia")), TextureType.ACACIA);
        tags.put(TagKey.create(Registries.BIOME, Utils.identifier("spruce")), TextureType.SPRUCE);
        tags.put(TagKey.create(Registries.BIOME, Utils.identifier("grass")), TextureType.GRASS);
        tags.put(TagKey.create(Registries.BIOME, Utils.identifier("mountains")), TextureType.MOUNTAINS);
    }

}
