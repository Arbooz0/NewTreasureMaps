package kawun.new_treasure_maps.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;


public class MapModelGenerator {


    private final Vector2i[] clockwise_add = {
            new Vector2i(0, 0),
            new Vector2i(1, 0),
            new Vector2i(1, 1),
            new Vector2i(0, 1)
    };
    private final Vector2i[] counterclockwise_add = { clockwise_add[3], clockwise_add[2], clockwise_add[1], clockwise_add[0] };

    private PoseStack.Pose pose;
    private VertexConsumer vertexConsumer;
    private int lightCoords;
    public static float anim = 0;

    private final int size;
    private final Vector3f[] grid;


    public MapModelGenerator() {
        size = 11;
        grid = createGrid(3);
    }


    public void update(PoseStack.Pose pose, VertexConsumer vertexConsumer, int lightCoords, float anim) {
        this.pose = pose;
        this.vertexConsumer = vertexConsumer;
        this.lightCoords = lightCoords;
        //this.anim = anim;
        generate();
    }


    public void generate() {
        drawGrid(true);
        //drawGrid(false);
    }


    private Vector3f[] createGrid(float scale) {
        Vector3f[] grid = new Vector3f[size * size];

        for (int i = 0; i < grid.length; i++) {
            float y = i / size;
            float x = i - (y * size);
            y /= (float) (size - 1);
            x /= (float) (size - 1);
            y -= 0.5F;
            x -= 0.5F;
            y *= scale;
            x *= scale;
            y += (float) (Math.random() * 0.1 - 0.05);
            x += (float) (Math.random() * 0.1 - 0.05);
            grid[i] = new Vector3f(x, y, (float) (Math.random() - 0.5f) / 20.0F);
        }

        return grid;
    }


    private void drawGrid(boolean clockwise) {
        for (int y = 0; y < size - 1; y++) {
            for (int x = 0; x < size - 1; x++) {
                Vector2i pos = new Vector2i(x, y);

                Vertex p1 = calculateVertex(pos, 0, clockwise);
                Vertex p2 = calculateVertex(pos, 1, clockwise);
                Vertex p3 = calculateVertex(pos, 2, clockwise);
                Vertex p4 = calculateVertex(pos, 3, clockwise);

                p2.calculateNormal(p3, p1);
                p4.calculateNormal(p1, p3);

                addVertex(p1);
                addVertex(p2);
                addVertex(p3);
                addVertex(p4);
            }
        }
    }


    private Vertex calculateVertex(Vector2i pos, int i, boolean clockwise) {
        Vector2i p = pos.add(getAdd(i, clockwise), new Vector2i());
        Vector2f uv = new Vector2f(p);
        uv.div(size - 1);
        Vector3f point = new Vector3f(getPosFromGrid(p));

        point.x = lerp(point.x, point.x / 10.0f, anim);
        point.z = lerp(point.z, p.x % 2 == 0 ? -0.2f : 0.2f, anim);

        return new Vertex(point, uv);
    }


    private float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }



    private void addVertex(Vertex vertex) {
        addVertex(vertex.pos, vertex.uv, vertex.normal);
    }

    private void addVertex(Vector3f vec, Vector2f uv) {
        addVertex(vec, uv, new Vector3f(0, 0, 1));
    }

    private void addVertex(Vector3f vec, Vector2f uv, Vector3f normal) {
        vertexConsumer
                .addVertex(pose, vec)
                .setColor(-1)
                .setUv(uv.x, uv.y)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(pose, normal);
    }


    private Vector3f randomVec() {
        return new Vector3f(randomNumber(), randomNumber(), randomNumber());
    }

    private float randomNumber() {
        return (float) (Math.random() * 20.0 - 10.0);
    }



    private Vector3f getPosFromGrid(Vector2i pos) {
        return grid[pos.y * size + pos.x];
    }


    private Vector2i getAdd(int index,  boolean clockwise) {
        return (clockwise ? clockwise_add : counterclockwise_add)[index];
    }



    private class Vertex {

        public Vector3f pos;
        public Vector3f normal;
        public Vector2f uv;

        public Vertex(Vector3f pos, Vector2f uv) {
            this.pos = pos;
            this.uv = uv;
        }

        public void calculateNormal(Vertex p1, Vertex p2) {
            Vector3f v1 = p1.pos.sub(pos, new Vector3f()).normalize();
            Vector3f v2 = p2.pos.sub(pos, new Vector3f()).normalize();
            normal = new Vector3f();
            v1.cross(v2, normal);
            normal.normalize();
            //Constants.LOG.info(pos.toString(new DecimalFormat()) + " " + p1.pos.toString(new DecimalFormat()) + " " + p2.pos.toString(new DecimalFormat()) + " n: " + normal.toString(new DecimalFormat()));
            p1.setNormal(normal);
            p2.setNormal(normal);
        }

        public void setNormal(Vector3f n) {
            if (normal == null) {
                normal = n;
            } else {
                normal.add(n).div(2).normalize();
            }
        }
    };

}
