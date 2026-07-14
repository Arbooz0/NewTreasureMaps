package kawun.new_treasure_maps.maps;


import kawun.new_treasure_maps.client.texture.MapTextureManager;
import kawun.new_treasure_maps.enums.MapType;
import kawun.new_treasure_maps.network.MapPacket;
import kawun.new_treasure_maps.utils.Pixels;
import kawun.new_treasure_maps.utils.Utils;
import net.minecraft.core.BlockPos;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;


public class DottedLineMapCreate extends BaseMapCreate {



    public static final Pixels CROSS;



    @Override
    public void start() {
        BlockPos chestPos = findPlaceChest(3, 5);
        if (chestPos == null) {
            Utils.sendErrorCreateMap();
            return;
        }

        createChest(chestPos);

        byte[] bytes = generateLines(new Vector2i(chestPos.getX() - fromPosition.x, chestPos.getZ() - fromPosition.y));

        save(MapType.DOTTED_LINE, bytes);
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





    public byte[] generateLines(Vector2i vector) {
        ArrayList<Vector2i> list = new ArrayList<>();
        Vector2i min = new Vector2i();
        Vector2i max = new Vector2i();

        Vector2i now = new Vector2i();
        Vector2i nowClient = new Vector2i();
        Vector2i lastDir = null;

        for (int i = 0; i < 30; i ++) {
            Vector2i dir = randomDir(vector.sub(now, new Vector2i()), lastDir);
            lastDir = new Vector2i(dir);
            int count = (int) (Math.random() * 3) + 1;
            for (int j = 0; j < count; j++) {
                Vector2i add = dir.mul((int) (Math.random() * 4) + 3, new Vector2i());
                list.add(add);
                now.add(add);

                nowClient.add(add.mul(2, new Vector2i()));
                nowClient.add(dir);
            }

            min.min(nowClient);
            max.max(nowClient);

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
        nowClient.set(start);

        int i = 4;
        for (Vector2i vec : list) {
            bytes[i] = (byte) vec.x;
            i++;
            bytes[i] = (byte) vec.y;
            i++;

            nowClient.add(vec.mul(2));
            nowClient.add(normalizeVector(vec));

            distToCorner.forEach((pos, d) -> {
                double this_d = nowClient.distance(pos);
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

    private Vector2i randomDir(Vector2i dir, @Nullable Vector2i lastDir) {
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
