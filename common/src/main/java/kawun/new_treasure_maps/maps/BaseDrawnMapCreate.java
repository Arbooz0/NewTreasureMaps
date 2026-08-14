package kawun.new_treasure_maps.maps;

import it.unimi.dsi.fastutil.bytes.*;
import it.unimi.dsi.fastutil.ints.Int2ByteMap;
import it.unimi.dsi.fastutil.ints.Int2ByteOpenHashMap;
import kawun.new_treasure_maps.NewTreasureMaps;
import kawun.new_treasure_maps.client.texture.MapTextureManager;
import kawun.new_treasure_maps.enums.MapType;
import kawun.new_treasure_maps.network.MapPacket;
import kawun.new_treasure_maps.utils.pixels.Pixels;
import kawun.new_treasure_maps.utils.pixels.PixelsLoader;
import kawun.new_treasure_maps.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.biome.Biome;
import org.joml.Vector2i;
import org.jspecify.annotations.Nullable;

import java.util.*;


public abstract class BaseDrawnMapCreate extends BaseMapCreate {


    public static final HashMap<TagKey<Biome>, TextureType> TAGS;
    public static final TagKey<Biome> ICEBERG_TAG = TagKey.create(Registries.BIOME, Utils.identifier("iceberg"));
    public static final String[] ALL_STRUCTURES = new String[]{
            "pillager_outpost",
            "village_plains",
            "village_taiga"
    };

    byte[] biomes = new byte[65536];
    Int2ByteMap mapping = new Int2ByteOpenHashMap();
    Byte2IntMap mappingReverse = new Byte2IntOpenHashMap();
    HashMap<Vector2i, String> structures = new HashMap<>();
    Vector2i start;
    Vector2i crossPos;
    int step = 0;

    boolean placeTree = false;



    @Override
    public void start() {
        if (false) { // TEST
            spriteSheet();
            return;
        }

        canChestUnderWater = false;

        BlockPos chestBlockPos = getChestPos();
        if (chestBlockPos == null) {
            Utils.sendErrorCreateMap();
            return;
        }
        placeCross(chestBlockPos, false);
        if (placeTree) {
            addStructure(chestBlockPos.offset(-1, 4, 0), "tree");
        }
        createChest(chestBlockPos);

        Vector2i chestPos = new Vector2i(chestBlockPos.getX(), chestBlockPos.getZ());
        start = fromPosition.add(chestPos, new Vector2i()).div(2).sub(512, 512);
        crossPos = chestPos.sub(start, chestPos).div(4);

        NewTreasureMaps.addTask(this::update);
    }



    public @Nullable BlockPos getChestPos() {
        return findPlaceChest(30, 50);
    }


    protected abstract byte[] modify(Pixels pixels);


    public abstract MapType getMapType();





    public boolean update() {
        if (step < 4) {
            gettingBiomes();
            step++;
            return false;
        }

        end();
        return true;
    }



    public void end() {
        Pixels pixels = new Pixels(256);

        byte water = 127;
        byte waterDarker = 111;
        byte[] borders = new byte[]{79, 95, waterDarker};

        byte[] biomes2 = new byte[65536];

        for (int i = 0; i < 6; i++) {

            for (int y = 0; y < 256; y++) {
                int offsetY = y << 8;

                for (int x = 0; x < 256; x++) {
                    int index = x + offsetY;

                    if (x == 0 || y == 0 || x == 255 || y == 255) {
                        biomes2[index] = -1;
                    } else {
                        byte biome = biomes[index];
                        if (i == 0 && biome < -1) {
                            pixels.setPixel(x, y, (biome == -2) ? water : waterDarker);
                        }

                        if (
                                biomeDiff(biome, biomes[index - 1]) ||
                                biomeDiff(biome, biomes[index + 1]) ||
                                biomeDiff(biome, biomes[index - 256]) ||
                                biomeDiff(biome, biomes[index + 256])
                        ) {
                            biomes2[index] = -1;
                            if (i < 3 && biome < -1) {
                                pixels.setPixel(x, y, borders[i]);
                            }
                        } else {
                            biomes2[index] = biome;
                        }
                    }
                }
            }

            byte[] copy = biomes;
            biomes = biomes2;
            biomes2 = copy;
        }

        water = (byte) (water & 240);
        waterDarker = (byte) (waterDarker & 240);

        HashMap<String, HashSet<Vector2i>> texturePos = new HashMap<>();

        for (int y = 0; y < 256; y++) {
            int offsetY = y << 8;
            for (int x = 0; x < 256; x++) {
                byte biome = biomes[x + offsetY];
                if (biome == -1) {
                    continue;
                }

                if (biome < 0 ) {
                    if (Math.random() < 0.01) {
                        byte w = (biome == -2) ? water : waterDarker;
                        int l = (int) (Math.random() * 3) + 1;
                        double m = (Math.PI / 2.0) / l;
                        for (int addX = -l; addX <= l; addX++) {
                            int a = 14 - (l - Math.abs(addX));
                            pixels.setPixelSafe(x + addX, y, (byte) (w | a));
                        }
                        continue;
                    }
                    if (biome == -2) {
                        continue;
                    }
                }

                TextureType type;
                if (biome == -3) {
                    if (Math.random() < 0.01) {
                        type = TextureType.ICEBERG;
                    } else {
                        continue;
                    }
                } else {

                    type = TextureType.random();
                    int tag = mappingReverse.get(biome);
                    if (((tag >> type.ordinal()) & 1) == 0) {
                        continue;
                    }

                    if (type == TextureType.CACTUS) {
                        if (Math.random() < 0.1) {
                            pixels.setPixel(x, y, (byte) (Math.random() * 7 + 6));
                            continue;
                        }
                    }
                }

                Vector2i pos2d = new Vector2i(x, y);
                if (texturePos.containsKey(type.group)) {
                    HashSet<Vector2i> positions = texturePos.get(type.group);
                    boolean skip = false;
                    for (Vector2i p : positions) {
                        if (pos2d.distanceSquared(p) < type.sqrtSize) {
                            skip = true;
                            break;
                        }
                    }
                    if (skip) {
                        continue;
                    }
                    positions.add(pos2d);
                } else {
                    HashSet<Vector2i> positions = new HashSet<>();
                    positions.add(pos2d);
                    texturePos.put(type.group, positions);
                }

                addTexture(type, pos2d, pixels);
            }
        }

        for (Map.Entry<Vector2i, String> entry : structures.entrySet()) {
            Pixels texture = PixelsLoader.getTexture(entry.getValue());
            if (texture != null) {
                pixels.drawImage(entry.getKey(), texture);
            }
        }

        save(getMapType(), modify(pixels));

        PixelsLoader.clearCache();
    }


    public boolean biomeDiff(byte b1, byte b2) {
        if (b1 == b2) {
            return false;
        }
        return b1 >= -1 || b2 >= -1;
    }



    public void gettingBiomes() {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(0, 100, 0);

        for (int y = (step * 64); y < ((step + 1) * 64); y++) {
            int offsetY = y << 8;
            boolean checkStructure = y % 4 == 0;

            for (int x = 0; x < 256; x++) {
                pos.setX(x * 4 + start.x);
                pos.setZ(y * 4 + start.y);

                Holder<Biome> biome = level.getBiome(pos);

                boolean isWater = biome.is(BiomeTags.WATER_ON_MAP_OUTLINES);

                int index = x + offsetY;
                if (isWater) {
                    biomes[index] = (byte) (biome.is(ICEBERG_TAG) ? -3 : -2);
                } else  {
                    biomes[index] = -1;

                    int tagsValue = 0;
                    for (TagKey<Biome> tag : TAGS.keySet()) {
                        if (biome.is(tag)) {
                            tagsValue |= (1 << (TAGS.get(tag).ordinal()));
                        }
                    }
                    if (tagsValue != 0) {
                        if (mapping.containsKey(tagsValue)) {
                            biomes[index] = mapping.get(tagsValue);
                        } else {
                            byte b = (byte) mapping.size();
                            biomes[index] = b;
                            mapping.put(tagsValue, b);
                            mappingReverse.put(b, tagsValue);
                        }
                    }
                }

                if (checkStructure && (x % 4 == 0)) {
                    String structureName = getStructure(pos.getX() >> 4, pos.getZ() >> 4, true);
                    if (!structureName.isEmpty()) {
                        if (hasStructure(structureName)) {
                            structures.put(new Vector2i(x + 2, y + 2), structureName);
                        }
                    }
                }

            }
        }
    }


    public void spriteSheet() {
        Pixels pixels = new Pixels(256);
        boolean isStructure = false;
        int iTexture = 0;
        int iVariation = 0;

        for (int y = 25; y < 230; y+=25) {
            for (int x = 25; x < 230; x+=25) {
                String name = "";
                if (isStructure) {
                    if (iVariation < ALL_STRUCTURES.length) {
                        name = ALL_STRUCTURES[iVariation];
                        iVariation++;
                    }
                } else {
                    TextureType type = TextureType.BY_ID[iTexture];
                    iVariation++;
                    name = type.texture + iVariation;

                    if (iVariation >= type.count) {
                        iTexture++;
                        iVariation = 0;
                        if (iTexture >= TextureType.BY_ID.length) {
                            isStructure = true;
                        }
                    }
                }

                if (!name.isEmpty()) {
                    Pixels texture = PixelsLoader.getTexture(name);
                    if (texture != null) {
                        pixels.drawImage(new Vector2i(x, y), texture);
                    }
                }
            }
        }

        PixelsLoader.clearCache();

        save(MapType.DRAWN, pixels.pixels);
    }



    public static void addTexture(TextureType type, Vector2i pos, Pixels to) {
        Pixels texture = PixelsLoader.getTexture(type.getRandomTexture());
        if (texture == null) {
            return;
        }
        to.drawImage(pos, texture, 0.5f, 0.5f, Math.random() > 0.5,
                (type == TextureType.ICEBERG) ? null : BaseDrawnMapCreate::blendColor);
    }




    public static void clientHandle(MapPacket packet) {
        Pixels pixels = new Pixels(packet.bytes());
        pixels.converter = BaseDrawnMapCreate::convertColor;
        MapTextureManager.insertPixels(packet.id(), pixels);
    }


    public static int convertColor(byte color) {
        int type = ((color & 0xFF) >> 6);
        int blackout = (color >> 4) & 3;
        int a = ((color & 15) * 255) / 15;
        if (type != 0) {
            blackout += 3;
            a <<= 24;
        }
        blackout = (blackout * 255) / 6;

        return switch (type) {
            case 1 -> ARGB.scaleRGB(a | 4352433, blackout); // Синий
            case 2 -> ARGB.scaleRGB(a | 13107250, blackout); // Красный
            case 3 -> ARGB.scaleRGB(a | 8866583, blackout); // Коричневый
            default -> ARGB.color(a, blackout, blackout, blackout); // Черный-Серый
        };
    }


    public static byte blendColor(byte bg, byte fg) {
        int a = fg & 15;
        a -= (int) (Math.random() * 5) + 1;
        if (a <= 0) {
            return bg;
        }
        fg = (byte) ((fg & 240) | a);

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




    public static boolean hasStructure(String structureName) {
        for (String name : ALL_STRUCTURES) {
            if (name.equals(structureName)) {
                return true;
            }
        }
        return false;
    }





    public enum TextureType {

        ACACIA(3, 18),
        BIRCH(4, 7, "tree"),
        CACTUS(3, 15),
        DARK_TREE(3, 7, "tree"),
        GRASS(4, 8),
        HILL(2, 10),
        ICEBERG(3, 15),
        JUNGLE(4, 8, "tree"),
        MOUNTAINS(4, 15),
        SNOW(2, 8),
        SPRUCE(4, 7, "tree"),
        TALL_SPRUCE(3, 9, "tree"),
        TREE(5, 7, "tree");

        public final String texture;
        public final String group;
        public final int count;
        public final int sqrtSize;
        private static final TextureType[] BY_ID = values();

        TextureType(int count, int size) {
            this(count, size, "");
        }

        TextureType(int count, int size, String group) {
            this.texture = name().toLowerCase();
            if (group.isEmpty()) {
                group = this.texture;
            }
            this.group = group;
            this.count = count;
            this.sqrtSize = size * size;
        }

        public String getRandomTexture() {
            int n = (int) (Math.random() * count) + 1;
            return texture + n;
        }


        public static TextureType random() {
            return BY_ID[(int) (Math.random() * BY_ID.length)];
        }

    }


    static {
        TAGS = new HashMap<>();
        for (TextureType type : TextureType.BY_ID) {
            if (type != TextureType.ICEBERG) {
                TAGS.put(TagKey.create(Registries.BIOME, Utils.identifier(type.texture)), type);
            }
        }
    }

}
