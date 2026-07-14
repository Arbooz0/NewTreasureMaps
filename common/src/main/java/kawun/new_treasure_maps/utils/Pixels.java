package kawun.new_treasure_maps.utils;

import org.joml.Vector2i;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;


public class Pixels {


    public int width;
    public int height;
    public byte[] pixels;

    public ConvertColor converter;
    public ArrayList<CopyImage> copyImages;


    public Pixels(int size) {
        this.width = size;
        this.height = size;
        this.pixels = new byte[size * size];
    }

    public Pixels(int width, int height, byte[] pixels) {
        this.width = width;
        this.height = height;
        this.pixels = pixels;
    }

    public Pixels(byte[] pixels) {
        this.width = (int) Math.sqrt(pixels.length);
        this.height = this.width;
        this.pixels = pixels;
    }


    public boolean isRect(int x, int y) {
        return (x >= 0 && x < width && y >= 0 && y < height);
    }


    public void setPixel(int x, int y, byte color) {
        pixels[x + y * width] = color;
    }


    public void setPixelSafe(int x, int y, byte color) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            setPixel(x, y, color);
        }
    }


    public byte getPixel(int x, int y) {
        return pixels[x + y * width];
    }


    public int getPixel(int index) {
        return converter.convert(pixels[index]);
    }



    public void drawImage(Vector2i pos, Pixels image) {
        drawImage(pos, image, 0.5f, 0.5f, false, null);
    }


    public void drawImage(Vector2i pos, Pixels image, float pivotX, float pivotY, boolean swapX, @Nullable BlendColor blend) {
        int startX = (int) (pos.x - (image.width * pivotX));
        int startY = (int) (pos.y - (image.height * pivotY));
        for (int y = 0; y < image.height; y++) {
            for (int x = 0; x < image.width; x++) {
                int px = startX + x;
                int py = startY + y;
                if (!isRect(px, py)) {
                    continue;
                }

                byte color = image.getPixel(swapX ? image.width - x - 1 : x, y);
                if (color != 0) {
                    if (blend != null) {
                        color = blend.blend(getPixel(px, py), color);
                    }
                    setPixel(px, py, color);
                }
            }
        }
    }


    public void fillSquare(int startX, int startY, int size, byte color) {
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                setPixelSafe(startX + x, startY + y, color);
            }
        }
    }

    public void addCopyImage(String texture, int x, int y) {
        if (copyImages == null) {
            copyImages = new ArrayList<>();
        }
        copyImages.add(new CopyImage(texture, x, y));
    }


    public interface ConvertColor {
        int convert(byte color);
    }

    public interface BlendColor {
        byte blend(byte bg, byte fg);
    }


    public record CopyImage(String texture, int x, int y){};

}
