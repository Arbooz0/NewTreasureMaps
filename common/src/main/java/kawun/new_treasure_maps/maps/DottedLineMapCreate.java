package kawun.new_treasure_maps.maps;


import com.mojang.blaze3d.platform.NativeImage;
import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.client.texture.MapTextureManager;
import kawun.new_treasure_maps.enums.MapType;
import kawun.new_treasure_maps.items.Items;
import kawun.new_treasure_maps.network.MapPacket;
import kawun.new_treasure_maps.saveddata.FreeID;
import kawun.new_treasure_maps.saveddata.MapSavedData;
import kawun.new_treasure_maps.utils.Pixels;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.MapColor;
import org.joml.Vector2f;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class DottedLineMapCreate {



    public static final Pixels CROSS;



    public static ItemStack create(Vector2i from, Level level) {
        Constants.LOG.info("Player pos: " + from.toString(new DecimalFormat()));
        Vector2i vec = Maps.getRandomPoint(40, 80);
        Constants.LOG.info("VEC: " + vec.toString(new DecimalFormat()));
        from.add(vec);
        Maps.createChest(from.x, from.y, level);

        byte[] bytes = generateLines(vec);

        int id = FreeID.getFreeID();
        MapSavedData data = new MapSavedData(id, MapType.DOTTED_LINE, bytes);
        data.save();

        return Items.newTreasureMap(id);
    }



    public static void clientHandle(MapPacket packet) {
        Pixels pixels = createImage(packet.bytes());
        MapTextureManager.insertPixels(packet.id(), pixels);
    }




    public static Pixels createImage(byte[] bytes) {
        Pixels image = new Pixels(256);
        image.converter = DottedLineMapCreate::convertColor;
        image.addCopyImage("compass", bytes[2] & 0xFF, bytes[3] & 0xFF);

        Vector2i start = new Vector2i(bytes[0] & 0xFF, bytes[1] & 0xFF);
        Vector2i pos = new Vector2i(start);

        int size = (bytes.length - 4) / 2;
        byte color = 1;
        byte color2 = 2;
        byte border = 3;

        boolean is_darker = false;

        for (int i = 0; i < size; i++) {
            Vector2i dir = new Vector2i(bytes[i * 2 + 4], bytes[i * 2 + 5]);
            int len = Math.abs(dir.get(dir.maxComponent())) - 1;
            normalizeVector(dir).mul(2);

            ArrayList<Vector2i> fill = new ArrayList<>();

            for (int j = 0; j < len; j++) {
                image.fillSquare(pos.x - 1, pos.y - 1, 4, border);
                fill.add(new Vector2i(pos));
                pos.add(dir);
            }

            for (Vector2i p : fill) {
                is_darker = !is_darker;
                image.fillSquare(p.x, p.y, 2, is_darker ? color2 : color);
            }
            is_darker = !is_darker;

            pos.add(dir);
            pos.add(dir.div(2));
        }

        image.fillSquare(start.x, start.y, 2, (byte) 4);

        pos.x += 1;
        pos.y += 1;
        image.drawImage(pos, CROSS);

        return image;
    }



    public static int convertColor(byte color) {
        return switch (color) {
            case 1 -> -53722;
            case 2 -> -5046246;
            case 3 -> -16777216;
            case 4 -> -12806867;
            default -> 0;
        };
    }





    public static byte[] generateLines(Vector2i vector) {
        ArrayList<Vector2i> list = new ArrayList<>();
        Vector2i min = new Vector2i();
        Vector2i max = new Vector2i();

        Vector2i now = new Vector2i();
        Vector2i now_client = new Vector2i();
        Vector2i lastDir = null;

        for (int i = 0; i < 30; i ++) {
            Vector2i dir = randomDir(vector.sub(now, new Vector2i()), lastDir);
            lastDir = new Vector2i(dir);
            int count = (int) (Math.random() * 3) + 1;
            for (int j = 0; j < count; j++) {
                Vector2i add = dir.mul((int) (Math.random() * 4) + 3, new Vector2i());
                list.add(add);
                now.add(add);

                now_client.add(add.mul(2, new Vector2i()));
                now_client.add(dir);
            }

            min.min(now_client);
            max.max(now_client);

            if (now.distanceSquared(vector) < 400) {
                break;
            }
        }

        Vector2i left = vector.sub(now, new Vector2i());

        if (left.x != 0) {
            if (Math.abs(left.x) < 7) {
                list.add(new Vector2i(left.x, 0));
            } else {
                int v1 = left.x / 2;
                int v2 = left.x - v1;
                list.add(new Vector2i(v1, 0));
                list.add(new Vector2i(v2, 0));
            }
            now.x += left.x;
        }
        if (left.y != 0) {
            if (Math.abs(left.y) < 7) {
                list.add(new Vector2i(0, left.y));
            } else {
                int v1 = left.y / 2;
                int v2 = left.y - v1;
                list.add(new Vector2i(0, v1));
                list.add(new Vector2i(0, v2));
            }
        }

        Vector2i start = max.sub(min);
        new Vector2i(256, 256).sub(start, start);
        start.div(2);
        start.sub(min);

        byte[] bytes = new byte[list.size() * 2 + 4];
        bytes[0] = (byte) start.x;
        bytes[1] = (byte) start.y;

        HashMap<Vector2i, Double> distToCorner = new HashMap<>();
        distToCorner.put(new Vector2i(32, 32), 256.0);
        distToCorner.put(new Vector2i(224, 32), 256.0);
        distToCorner.put(new Vector2i(32, 224), 256.0);
        distToCorner.put(new Vector2i(224, 224), 256.0);
        now_client.set(start);

        int i = 4;
        for (Vector2i vec : list) {
            bytes[i] = (byte) vec.x;
            i++;
            bytes[i] = (byte) vec.y;
            i++;

            now_client.add(vec.mul(2));
            now_client.add(normalizeVector(vec));

            distToCorner.forEach((pos, d) -> {
                double this_d = now_client.distance(pos);
                if (this_d < d) {
                    distToCorner.put(pos, this_d);
                }
            });
        }

        Vector2i compassPos = new Vector2i();
        distToCorner.forEach((pos, d) -> {
            double far = distToCorner.getOrDefault(compassPos, 0.0);
            if (d > far) {
                compassPos.set(pos);
            }
        });

        compassPos.sub(32, 32);
        bytes[2] = (byte) compassPos.x;
        bytes[3] = (byte) compassPos.y;

        return bytes;

    }

    private static Vector2i randomDir(Vector2i dir, @Nullable Vector2i lastDir) {
        ArrayList<Vector2i> list = new ArrayList<>();
        list.add(normalizeVector(dir));
        Vector2i v1 = new Vector2i(dir.y, dir.x * -1);
        list.add(v1);
        Vector2i v2 = new Vector2i(dir.y * -1, dir.x);
        list.add(v2);
        list.add(normalizeVector(dir.add(v1, new Vector2i())));
        list.add(normalizeVector(dir.add(v2, new Vector2i())));

        if (Math.random() > 0.5) {
            list.removeIf(v -> ((v.x != 0) && (v.y != 0)));
        }

        ArrayList<Vector2i> copy = (ArrayList<Vector2i>) list.clone();

        if (lastDir != null) {
            list.remove(lastDir);
            Vector2i inverse = lastDir.mul(-1, new Vector2i());
            list.remove(inverse);
            if (!dir.equals(inverse) && list.size() > 1) {
                list.remove(normalizeVector((new Vector2i(inverse.y, inverse.x * -1)).add(inverse)));
                list.remove(normalizeVector((new Vector2i(inverse.y * -1, inverse.x)).add(inverse)));
            }
        }

        if (list.isEmpty()) {
            return dir;
        }
        return list.get((int) (Math.random() * list.size()));
    }


    private static Vector2i normalizeVector(Vector2i vector) {
        if (vector.x != 0) {
            vector.x = vector.x > 0 ? 1 : -1;
        }
        if (vector.y != 0) {
            vector.y = vector.y > 0 ? 1 : -1;
        }
        return vector;
    }


    private static int randomNumber() {
        return (int) ((Math.random() * 100) - 50);
    }



    static {
        byte l = 1;
        byte r = 2;
        byte b = 3;
        byte[] bytes = new byte[]{
                0, b, b, 0, 0, 0, 0, b, b, 0,
                b, b, r, b, 0, 0, b, r, b, b,
                b, r, l, r, b, b, r, l, r, b,
                0, b, r, l, r, r, l, r, b, 0,
                0, 0, b, r, l, l, r, b, 0, 0,
                0, 0, b, r, l, l, r, b, 0, 0,
                0, b, r, l, r, r, l, r, b, 0,
                b, r, l, r, b, b, r, l, r, b,
                b, b, r, b, 0, 0, b, r, b, b,
                0, b, b, 0, 0, 0, 0, b, b, 0,
        };
        CROSS = new Pixels(bytes);
    }


}
