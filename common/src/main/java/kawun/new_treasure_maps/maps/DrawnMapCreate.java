package kawun.new_treasure_maps.maps;


import kawun.new_treasure_maps.enums.MapType;
import kawun.new_treasure_maps.utils.pixels.Pixels;
import kawun.new_treasure_maps.utils.pixels.PixelsLoader;
import org.joml.Vector2f;
import org.joml.Vector2i;


public class DrawnMapCreate extends BaseDrawnMapCreate {


    @Override
    public void start() {
        isBigCross = true;
        checkAirOverChest = true;
        placeTree = true;
        super.start();
    }

    @Override
    protected byte[] modify(Pixels pixels) {
        Pixels cross = PixelsLoader.getTexture("cross");
        if (cross != null) {
            pixels.drawImage(crossPos, cross);
        }

        fromPosition.sub(start, fromPosition).div(4);
        Vector2f dir = new Vector2f(crossPos.sub(fromPosition, new Vector2i()));
        float len = dir.length();
        dir.normalize();
        Vector2f pDir = new Vector2f(dir.y, dir.x * -1);
        Vector2f temp = new Vector2f();

        Vector2f[] points = new Vector2f[7];
        points[0] = new Vector2f(fromPosition);
        points[points.length -1] = new Vector2f(crossPos);

        int m = (Math.random() > 0.5) ? 1 : -1;

        for (int i = 1; i < (points.length - 1); i++) {
            for (int j = 0; j < 10; j++) {
                float l = ((float) i / points.length) * 0.5f;
                l += (float) (Math.random() * 0.5);
                l *= len;

                Vector2f pos = new Vector2f(fromPosition);
                pos.add(dir.mul(l, temp));

                m *= -1;
                float pL = (float) ((Math.random() * 20) + 40) * m;
                pos.add(pDir.mul(pL, temp));

                boolean success = true;
                if (j != 9) {
                    for (Vector2f p : points) {
                        if (p != null) {
                            if (pos.distanceSquared(p) < 900) {
                                success = false;
                                break;
                            }
                        }
                    }
                }

                if (success) {
                    points[i] = pos;
                    break;
                }
            }
        }

        byte redColor = (byte) 0b1011_1111;
        byte redColor2 = (byte) 0b1010_1111;
        byte border = (byte) 0b1001_1111;

        Vector2f p1 = new Vector2f();
        Vector2f p2 = new Vector2f();
        Vector2f p3 = new Vector2f();
        Vector2f q = new Vector2f();

        Vector2i lastPos = new Vector2i(fromPosition);
        Vector2i lastPos2 = new Vector2i(fromPosition);

        for (int i = 0; i < (points.length - 2); i++) {

            if (i == 0) {
                p1.set(points[0]);
            } else {
                points[i].add(points[i + 1], p1).div(2);
            }

            p2.set(points[i + 1]);

            if (i == (points.length - 3)) {
                p3.set(points[points.length - 1]);
            } else {
                points[i + 1].add(points[i + 2], p3).div(2);
            }

            float skipT = 0.2f;
            boolean isLastLine = i == (points.length - 3);

            for (float t = 0; t < 1; t+=0.002f) {
                if (t > skipT) {
                    t += 0.1f;
                    skipT += 0.3f;
                }

                if (isLastLine) {
                    if (t > 0.94) {
                        break;
                    }
                }

                p1.lerp(p2, t, q);
                p2.lerp(p3, t, temp);
                q.lerp(temp, t);

                int x = (int) q.x;
                int y = (int) q.y;
                if (lastPos.equals(x, y)) {
                    continue;
                }

                byte color = (Math.sin((skipT - t) / 0.2 * Math.PI) < 0.4) ? redColor2 : redColor;

                pixels.fillSquare(x - 1, y - 1, 3, border);
                pixels.setPixelSafe(lastPos2.x, lastPos2.y, color);
                lastPos2.set(lastPos);
                lastPos.set(x, y);
            }
        }

        return pixels.pixels;
    }

    @Override
    public MapType getMapType() {
        return MapType.DRAWN;
    }


}
