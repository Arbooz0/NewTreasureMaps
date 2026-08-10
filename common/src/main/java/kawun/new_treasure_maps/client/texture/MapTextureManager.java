package kawun.new_treasure_maps.client.texture;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.enums.FoldType;
import kawun.new_treasure_maps.utils.TimePassed;
import kawun.new_treasure_maps.utils.pixels.Pixels;
import kawun.new_treasure_maps.utils.Utils;
import kawun.new_treasure_maps.utils.pixels.PixelsBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Optional;


public class MapTextureManager {


    public static Int2ObjectMap<DynamicTexture> maps = new Int2ObjectOpenHashMap<>();
    public static HashSet<String> backTextureInit = new HashSet<>();
    public static Int2ObjectMap<PixelsBase> unsettedPixels = new Int2ObjectOpenHashMap<>();
    public static NativeImage noiseTransparency = null;
    public static NativeImage noiseBlackout = null;
    public static NativeImage mask = null;



    public static Identifier getTextureIdentifier(int id) {
        return Utils.identifier("map" + id);
    }

    public static Identifier getBackTextureIdentifier(String texture) {
        return Utils.identifier("map_" + texture);
    }


    private static NativeImage loadTexture(String name) {
        Optional<Resource> res = Minecraft.getInstance().getResourceManager().getResource(Utils.identifier("textures/" + name + ".png"));
        if (res.isPresent()) {
            try (InputStream stream = res.get().open()) {
                return NativeImage.read(stream);
            } catch (Exception e) {
                Constants.LOG.error("Error load texture " + name + ": " + e.getMessage());
            }
        }
        return null;
    }


    public static @Nullable NativeImage loadBlockTexture(Identifier id) {
        Block block = BuiltInRegistries.BLOCK.getValue(id);
        BlockState state = block.defaultBlockState();
        TextureAtlasSprite texture = Minecraft.getInstance().getModelManager().getBlockStateModelSet().getParticleMaterial(state).sprite();

        String path = texture.contents().name().getPath();
        if (block == Blocks.GRASS_BLOCK) {
            path = "block/grass_block_top";
        }

        Identifier idTexture = Identifier.parse("textures/" + path + ".png");

        Optional<Resource> res = Minecraft.getInstance().getResourceManager().getResource(idTexture);
        if (res.isPresent()) {
            try (InputStream stream = res.get().open()) {
                NativeImage image = NativeImage.read(stream);
                NativeImage newImage = new NativeImage(8, 8, true);
                image.resizeSubRectTo(0, 0, 8, 8, newImage);
                image.close();
                BlockTintSource tint = Minecraft.getInstance().getBlockColors().getTintSource(state, 0);
                if (tint != null) {
                    int color = block == Blocks.WATER ? 0xFF2068EF : tint.color(state);
                    for (int x = 0; x < 8; x++) {
                        for (int y = 0; y < 8; y++) {
                            newImage.setPixel(x, y, ARGB.multiply(newImage.getPixel(x, y), color));
                        }
                    }
                }
                return newImage;
            } catch (Exception e) {
                Constants.LOG.error("Error load texture " + idTexture + ": " + e.getMessage());
            }
        } else {
            Constants.LOG.info("No find " + idTexture);
        }
        return null;
    }


    public static void loadNoiseTransparency() {
        if (noiseTransparency == null) {
            noiseTransparency = loadTexture("noise_transparency");
        }
    }

    public static void loadNoiseBlackout() {
        if (noiseBlackout == null) {
            noiseBlackout = loadTexture("noise_blackout");
        }
    }

    public static void loadMask() {
        if (mask == null) {
            mask = loadTexture("mask");
        }
    }


    public static Identifier createNewTexture(int id, FoldType type) {
        NativeImage image = loadTexture(type.getTexture());
        if (image == null) {
            return Identifier.withDefaultNamespace("textures/map/map_background.png");
        }
        DynamicTexture texture = new DynamicTexture(() -> "treasuremap" + id, image);

        Identifier identifier = getTextureIdentifier(id);
        Minecraft.getInstance().getTextureManager().register(identifier, texture);
        maps.put(id, texture);
        if (unsettedPixels.containsKey(id)) {
            TimePassed time = new TimePassed();
            insertPixels(id, unsettedPixels.get(id));
            time.end("Insert pixels " + id);
            unsettedPixels.remove(id);
        }
        return identifier;
    }


    public static Identifier getBackTexture(FoldType type) {
        if (backTextureInit.contains(type.lastTexture)) {
            return getBackTextureIdentifier(type.lastTexture);
        }
        NativeImage image = loadTexture(type.lastTexture);
        if (image == null) {
            return Identifier.withDefaultNamespace("textures/map/map_background.png");
        }
        DynamicTexture texture = new DynamicTexture(() -> "treasuremap_" + type.lastTexture, image);

        Identifier identifier = getBackTextureIdentifier(type.lastTexture);
        Minecraft.getInstance().getTextureManager().register(identifier, texture);
        backTextureInit.add(type.lastTexture);
        return identifier;
    }


    public static void insertPixels(int id, PixelsBase pixels) {
        if (!maps.containsKey(id)) {
            unsettedPixels.put(id, pixels);
            return;
        }

        DynamicTexture texture = maps.get(id);
        NativeImage image = texture.getPixels();
        loadNoiseTransparency();
        loadNoiseBlackout();
        loadMask();

        int x_offset = 4;
        int y_offset = image.getHeight() - 260;

        for (int y = 0; y < 256; y++) {
            for (int x = 0; x < 256; x++) {
                if (!getMask(x, y)) {
                    continue;
                }
                int color = pixels.getPixel(x + y * 256);
                int alpha = (int) (((color >> 24) & 0xFF) * getNoise(x, y));
                if (alpha < 10) {
                    continue;
                }
                color = ARGB.scaleRGB(color, getNoise2(x, y));
                int bg = image.getPixel(x + x_offset, y + y_offset);
                color = blendColor(bg, color, alpha);
                image.setPixel(x + x_offset, y + y_offset, color);
            }
        }


        if (pixels.copyImages != null) {
            for (Pixels.CopyImage copyImage : pixels.copyImages) {
                NativeImage i = loadTexture(copyImage.texture());
                if (i == null) {
                    continue;
                }
                int w = i.getWidth();
                int h = i.getHeight();

                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        int tX = copyImage.x() + x_offset + x;
                        int tY = copyImage.y() + y_offset + y;

                        int color = i.getPixel(x, y);
                        int alpha = (color >> 24) & 0xFF;
                        if (alpha != 255) {
                            if (alpha < 10) {
                                continue;
                            }
                            int bg = image.getPixel(tX, tY);
                            color = blendColor(bg, color, alpha);
                        }
                        image.setPixel(tX, tY, color);
                    }
                }

                i.close();
            }
        }

        try {
            image.writeToFile(Paths.get("C:/Users/Admin/Downloads/test/map.png")); // TEST
        } catch (IOException e) {
            System.err.println("ERROR SAVE: " + e.getMessage());
        }

        texture.upload();

    }


    public static float getNoise(int x, int y) {
        return noiseTransparency == null ? 1 : (noiseTransparency.getPixel(x, y) & 0xFF) / 255.0f;
    }

    public static float getNoise2(int x, int y) {
        return noiseBlackout == null ? 1 : (noiseBlackout.getPixel(x, y) & 0xFF) / 255.0f;
    }

    public static boolean getMask(int x, int y) {
        return mask == null || (mask.getPixel(x / 4, y / 4) & 0xFF) != 0;
    }


    private static int blendColor(int bg, int fg, int a) {
        int r1 = (bg >> 16) & 0xFF;
        int g1 = (bg >> 8) & 0xFF;
        int b1 = bg & 0xFF;

        int r2 = (fg >> 16) & 0xFF;
        int g2 = (fg >> 8) & 0xFF;
        int b2 = fg & 0xFF;

        int r = (r2 * a + r1 * (255 - a)) / 255;
        int g = (g2 * a + g1 * (255 - a)) / 255;
        int b = (b2 * a + b1 * (255 - a)) / 255;

        return (255 << 24) | (r << 16) | (g << 8) | b;
    }



    public static void clear() {
        unsettedPixels.clear();

        TextureManager manager = Minecraft.getInstance().getTextureManager();
        for (int id : maps.keySet()) {
            manager.release(getTextureIdentifier(id));
        }
        maps.clear();

        for (String texture : backTextureInit) {
            manager.release(getBackTextureIdentifier(texture));
        }
        backTextureInit.clear();

        if (noiseTransparency != null) {
            noiseTransparency.close();
            noiseTransparency = null;
        }
        if (noiseBlackout != null) {
            noiseBlackout.close();
            noiseBlackout = null;
        }
        if (mask != null) {
            mask.close();
            mask = null;
        }
    }




}
