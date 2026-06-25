package kawun.new_treasure_maps.client.model;

import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.Resource;

import java.io.InputStream;
import java.util.Optional;

public class ModelLoader {


    public static void loadModel(String name) {
        Optional<Resource> res = Minecraft.getInstance().getResourceManager().getResource(Utils.identifier("models/" + name + ".data"));
        if (res.isPresent()) {
            try (InputStream stream = res.get().open()) {
                byte[] bytes = stream.readAllBytes();
                Constants.LOG.info("Bytes: " + bytes.length);
            } catch (Exception e) {
                Constants.LOG.error("Error load model " + name + ": " + e.getMessage());
            }
        }
    }
}
