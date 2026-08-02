package kawun.new_treasure_maps.client.model;

import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.enums.FoldType;
import kawun.new_treasure_maps.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.Resource;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Optional;

public class Model {


    private static final HashMap<FoldType, Model> loadedModels = new HashMap<>();


    public final int countVertex;
    public final int countFrame;
    public final int maxFrameIndex;
    private final Vector3f[] vertex;
    private final Vector2f[] uv;
    private final Vector3f[] normal;
    private final Vector3f temp = new Vector3f();



    public Model(int countVertex, int countFrame) {
        this.countVertex = countVertex;
        this.countFrame = countFrame;
        this.maxFrameIndex = countFrame - 1;
        int size = countFrame * countVertex;
        vertex = new Vector3f[size];
        uv = new Vector2f[countVertex];
        normal = new Vector3f[size];
    }



    public Vector3f getVertex(int index, float time) {
        return getElement(index, time, vertex);
    }


    public Vector2f getUV(int index) {
        return uv[index];
    }


    public Vector3f getNormal(int index, float time) {
        return getElement(index, time, normal);
    }


    private Vector3f getElement(int index, float time, Vector3f[] array) {
        if (time <= 0) {
            return temp.set(array[index]);
        } else if (time >= 1) {
            return temp.set(array[countVertex * maxFrameIndex + index]);
        }
        int frame = (int) (time * maxFrameIndex);
        time = (time * maxFrameIndex) - frame;
        Vector3f p1 = array[countVertex * frame + index];
        Vector3f p2 = array[countVertex * (frame + 1) + index];
        return p1.lerp(p2, time, temp);
    }



    public static @Nullable Model getModel(FoldType type) {
        if (loadedModels.containsKey(type)) {
            return loadedModels.get(type);
        }
        Model model = loadModel(type);
        loadedModels.put(type, model);
        return model;
    }


    private static @Nullable Model loadModel(FoldType type) {
        Optional<Resource> res = Minecraft.getInstance().getResourceManager().getResource(Utils.identifier("models/" + type.model + ".model"));
        if (res.isPresent()) {
            try (InputStream stream = res.get().open()) {
                byte[] bytes = stream.readAllBytes();
                return loadModel(bytes);
            } catch (Exception e) {
                Constants.LOG.error("Error load model " + type + ": " + e.getMessage());
            }
        } else {
            Constants.LOG.error("No has model " + type);
        }
        return null;
    }


    private static Model loadModel(byte[] bytes) {
        int countVertex = ((bytes[1] & 0xFF) << 8) | (bytes[0] & 0xFF);
        int countFrame = (bytes.length - 2 - (countVertex * 2)) / (countVertex * 6);
        Model model = new Model(countVertex, countFrame);

        int i = 2;

        for (int j = 0; j < countVertex; j++) {
            float x = (bytes[i] & 0xFF) / 255.0f;
            float y = (bytes[i + 1] & 0xFF) / 255.0f;
            model.uv[j] = new Vector2f(x, y);
            i += 2;
        }

        for (int frame = 0; frame < countFrame; frame++) {
            int offset = frame * countVertex;

            for (int j = 0; j < countVertex; j++) {
                float x = bytes[i] / 127.0f;
                float y = bytes[i + 1] / 127.0f;
                float z = bytes[i + 2] / 127.0f;
                model.vertex[j + offset] = new Vector3f(x, y, z);
                i += 3;
            }

            for (int j = 0; j < countVertex; j++) {
                float x = bytes[i] / 127.0f;
                float y = bytes[i + 1] / 127.0f;
                float z = bytes[i + 2] / 127.0f;
                model.normal[j + offset] = new Vector3f(x, y, z);
                i += 3;
            }

        }

        return model;
    }


}
