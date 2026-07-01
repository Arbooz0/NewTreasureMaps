package kawun.new_treasure_maps.utils;

import org.joml.Vector2i;

import java.util.ArrayList;


public class Pixels {


    public int size;
    public byte[] pixels;

    public ConvertColor converter;
    public ArrayList<CopyImage> copyImages;


    public Pixels(int size) {
        this.size = size;
        this.pixels = new byte[size * size];
    }

    public Pixels(byte[] pixels) {
        this.size = (int) Math.sqrt(pixels.length);
        this.pixels = pixels;
    }


    public void setPixel(int x, int y, byte color) {
        pixels[x + y * size] = color;
    }


    public void setPixelSafe(int x, int y, byte color) {
        if (x >= 0 && x < size && y >= 0 && y < size) {
            setPixel(x, y, color);
        }
    }


    public byte getPixel(int x, int y) {
        return pixels[x + y * size];
    }


    public int getPixel(int index) {
        return converter.convert(pixels[index]);
    }


    public void drawImage(Vector2i pos, Pixels image) {
        int start_x = pos.x - (image.size / 2);
        int start_y = pos.y - (image.size / 2);
        for (int y = 0; y < image.size; y++) {
            for (int x = 0; x < image.size; x++) {
                byte color = image.getPixel(x, y);
                if (color != 0) {
                    setPixelSafe(start_x + x, start_y + y, color);
                }
            }
        }
    }


    public void fillSquare(int start_x, int start_y, int size, byte color) {
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                setPixelSafe(start_x + x, start_y + y, color);
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
        int convert(byte color );
    }


    public record CopyImage(String texture, int x, int y){};

}
