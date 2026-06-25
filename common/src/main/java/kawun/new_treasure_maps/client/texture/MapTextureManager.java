package kawun.new_treasure_maps.client.texture;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.enums.FoldType;
import kawun.new_treasure_maps.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.material.MapColor;

import java.io.InputStream;
import java.util.Optional;


public class MapTextureManager {


    public static Int2ObjectMap<DynamicTexture> maps = new Int2ObjectOpenHashMap<>();



    public static Identifier getTextureIdentifier(int id) {
        return Utils.identifier("map" + String.valueOf(id));
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
        return identifier;
    }


    public static void insertPixels(int id, byte[] pixels) {
        if (!maps.containsKey(id)) {
            Constants.LOG.error("insertPixels: No contains map " + id);
            return;
        }

        boolean upscale = pixels.length == 16384;

        DynamicTexture texture = maps.get(id);
        NativeImage image = texture.getPixels();

        int x_offset = 22;
        int y_offset = 22;

        for (int y = 0; y < 256; y++) {
            for (int x = 0; x < 256; x++) {
                int i;
                if (upscale) {
                    i = (x / 2) + (y / 2) * 128;
                } else {
                    i = x + y * 256;
                }
                image.setPixel(x + x_offset, y + y_offset, MapColor.getColorFromPackedId(pixels[i]));
            }
        }

        texture.upload();

    }




}
