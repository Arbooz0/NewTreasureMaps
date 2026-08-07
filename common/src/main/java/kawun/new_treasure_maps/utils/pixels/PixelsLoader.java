package kawun.new_treasure_maps.utils.pixels;

import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.NewTreasureMaps;
import kawun.new_treasure_maps.utils.Utils;
import net.minecraft.server.packs.resources.Resource;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Optional;

public class PixelsLoader {


    public static HashMap<String, Pixels> textures = new HashMap<>();



    public static void clearCache() {
        textures.clear();
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
        } else {
            Constants.LOG.error("NO PIXELS: " + name);
        }
        return null;
    }
}
