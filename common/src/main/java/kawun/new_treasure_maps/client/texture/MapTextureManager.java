package kawun.new_treasure_maps.client.texture;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.enums.FoldType;
import kawun.new_treasure_maps.utils.Pixels;
import kawun.new_treasure_maps.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Optional;


public class MapTextureManager {


    public static Int2ObjectMap<DynamicTexture> maps = new Int2ObjectOpenHashMap<>();
    public static HashSet<FoldType> backTextureInit = new HashSet<>();
    public static Int2ObjectMap<Pixels> unsetted_pixels = new Int2ObjectOpenHashMap<>();



    public static Identifier getTextureIdentifier(int id) {
        return Utils.identifier("map" + id);
    }

    public static Identifier getBackTextureIdentifier(FoldType type) {
        return Utils.identifier("map_" + type.texture);
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


    public static Identifier createNewTexture(int id, FoldType type) {
        NativeImage image = loadTexture(type.texture);
        if (image == null) {
            return Identifier.withDefaultNamespace("textures/map/map_background.png");
        }
        DynamicTexture texture = new DynamicTexture(() -> "treasuremap" + id, image);

        Identifier identifier = getTextureIdentifier(id);
        Minecraft.getInstance().getTextureManager().register(identifier, texture);
        maps.put(id, texture);
        if (unsetted_pixels.containsKey(id)) {
            insertPixels(id, unsetted_pixels.get(id));
            unsetted_pixels.remove(id);
        }
        return identifier;
    }


    public static Identifier getBackTexture(FoldType type) {
        if (backTextureInit.contains(type)) {
            return getBackTextureIdentifier(type);
        }
        NativeImage image = loadTexture(type.texture);
        if (image == null) {
            return Identifier.withDefaultNamespace("textures/map/map_background.png");
        }
        DynamicTexture texture = new DynamicTexture(() -> "treasuremap_" + type.texture, image);

        Identifier identifier = getBackTextureIdentifier(type);
        Minecraft.getInstance().getTextureManager().register(identifier, texture);
        backTextureInit.add(type);
        return identifier;
    }


    public static void insertPixels(int id, Pixels pixels) {
        if (!maps.containsKey(id)) {
            unsetted_pixels.put(id, pixels);
            return;
        }

        DynamicTexture texture = maps.get(id);
        NativeImage image = texture.getPixels();

        int x_offset = 22;
        int y_offset = 22;

        for (int y = 0; y < 256; y++) {
            for (int x = 0; x < 256; x++) {
                int color = pixels.getPixel(x + y * 256);
                int alpha = (color >> 24) & 0xFF;
                if (alpha != 255) {
                    if (alpha < 10) {
                        continue;
                    }
                    int bg = image.getPixel(x + x_offset, y + y_offset);
                    color = blendColor(bg, color, alpha);
                }
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
            image.writeToFile(Paths.get("C:/Users/Admin/Downloads/test/map.png"));
        } catch (IOException e) {
            System.err.println("ERROR SAVE: " + e.getMessage());
        }

        texture.upload();

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
        unsetted_pixels.clear();

        TextureManager manager = Minecraft.getInstance().getTextureManager();
        for (int id : maps.keySet()) {
            manager.release(getTextureIdentifier(id));
        }
        maps.clear();

        for (FoldType type : backTextureInit) {
            manager.release(getBackTextureIdentifier(type));
        }
        backTextureInit.clear();
    }




}
