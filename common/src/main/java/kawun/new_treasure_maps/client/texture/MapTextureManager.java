package kawun.new_treasure_maps.client.texture;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.client.utils.ARGB;
import kawun.new_treasure_maps.enums.FoldType;
import kawun.new_treasure_maps.utils.TimePassed;
import kawun.new_treasure_maps.utils.Utils;
import kawun.new_treasure_maps.utils.pixels.Pixels;
import kawun.new_treasure_maps.utils.pixels.PixelsBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.awt.*;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Optional;


public class MapTextureManager {


    public static Int2ObjectMap<DynamicTexture> maps = new Int2ObjectOpenHashMap<>();
    public static HashSet<String> backTextureInit = new HashSet<>();
    public static Int2ObjectMap<PixelsBase> unsettedPixels = new Int2ObjectOpenHashMap<>();
    public static NativeImage noiseTransparency = null;
    public static NativeImage noiseBlackout = null;
    public static NativeImage mask = null;



    public static ResourceLocation getTextureIdentifier(int id) {
        return Utils.identifier("map" + id);
    }

    public static ResourceLocation getBackTextureIdentifier(String texture) {
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


    public static NativeImage loadBlockTexture(ResourceLocation id) {
        Block block = BuiltInRegistries.BLOCK.get(id);
        BlockState state = block.defaultBlockState();
        TextureAtlasSprite texture = Minecraft.getInstance().getModelManager().getBlockModelShaper().getParticleIcon(state);

        String path = texture.contents().name().getPath();
        if (block == Blocks.GRASS_BLOCK) {
            path = "block/grass_block_top";
        }

        ResourceLocation idTexture = ResourceLocation.parse("textures/" + path + ".png");

        Optional<Resource> res = Minecraft.getInstance().getResourceManager().getResource(idTexture);
        if (res.isPresent()) {
            try (InputStream stream = res.get().open()) {
                NativeImage image = NativeImage.read(stream);
                NativeImage newImage = new NativeImage(8, 8, true);
                image.resizeSubRectTo(0, 0, 8, 8, newImage);
                image.close();
                int color = block == Blocks.WATER ? -1087456 : Minecraft.getInstance().getBlockColors().getColor(state, null, null, 0);
                for (int x = 0; x < 8; x++) {
                    for (int y = 0; y < 8; y++) {
                        newImage.setPixelRGBA(x, y, ARGB32.multiply(newImage.getPixelRGBA(x, y), color));
                    }
                }
                return newImage;
            } catch (Exception e) {
                Constants.LOG.error("Error load texture " + idTexture + ": " + e.getMessage());
            }
        } else {
            Constants.LOG.error("No find " + idTexture);
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


    public static ResourceLocation createNewTexture(int id, FoldType type) {
        NativeImage image = loadTexture(type.getTexture());
        if (image == null) {
            return ResourceLocation.withDefaultNamespace("textures/map/map_background.png");
        }
        DynamicTexture texture = new DynamicTexture(image);

        ResourceLocation identifier = getTextureIdentifier(id);
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


    public static ResourceLocation getBackTexture(FoldType type) {
        if (backTextureInit.contains(type.lastTexture)) {
            return getBackTextureIdentifier(type.lastTexture);
        }
        NativeImage image = loadTexture(type.lastTexture);
        if (image == null) {
            return ResourceLocation.withDefaultNamespace("textures/map/map_background.png");
        }
        DynamicTexture texture = new DynamicTexture(image);

        ResourceLocation identifier = getBackTextureIdentifier(type.lastTexture);
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

        int offsetX = 4;
        int offsetY = image.getHeight() - 260;

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
                int bg = image.getPixelRGBA(x + offsetX, y + offsetY);
                color = blendColor(bg, color, alpha);
                image.setPixelRGBA(x + offsetX, y + offsetY, color);
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
                        try {
                            int tX = copyImage.x() + x;
                            int tY = copyImage.y() + y;
                            if (!getMask(tX, tY)) {
                                continue;
                            }

                            int color = i.getPixelRGBA(x, y);
                            int alpha = (color >> 24) & 0xFF;
                            if (copyImage.applyNoise()) {
                                alpha = (int) (alpha * getNoise(tX, tY));
                            }
                            if (alpha != 255) {
                                if (alpha < 10) {
                                    continue;
                                }
                                int bg = image.getPixelRGBA(offsetX + tX, offsetY + tY);
                                color = blendColor(bg, color, alpha);
                            }
                            image.setPixelRGBA(offsetX + tX, offsetY + tY, color);
                        } catch (Exception e) {

                        }
                    }
                }

                i.close();
            }
        }

        /*try {
            image.writeToFile(Paths.get("C:/Users/Admin/Downloads/test/map.png")); // TEST
        } catch (IOException e) {
            System.err.println("ERROR SAVE: " + e.getMessage());
        }*/

        texture.upload();

    }


    public static float getNoise(int x, int y) {
        return noiseTransparency == null ? 1 : (noiseTransparency.getPixelRGBA(x, y) & 0xFF) / 255.0f;
    }

    public static float getNoise2(int x, int y) {
        return noiseBlackout == null ? 1 : (noiseBlackout.getPixelRGBA(x, y) & 0xFF) / 255.0f;
    }

    public static boolean getMask(int x, int y) {
        return mask == null || (mask.getPixelRGBA(x / 4, y / 4) & 0xFF) != 0;
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
