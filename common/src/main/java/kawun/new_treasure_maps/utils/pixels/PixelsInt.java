package kawun.new_treasure_maps.utils.pixels;

public class PixelsInt extends PixelsBase {


    public int[] pixels;


    public PixelsInt(int size) {
        super(size, size);
        this.pixels = new int[size * size];
    }



    @Override
    public int getPixel(int index) {
        return pixels[index];
    }


    public void setPixel(int x, int y, int color) {
        pixels[x + y * width] = color;
    }

    public void setPixelSafe(int x, int y, int color) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            setPixel(x, y, color);
        }
    }

}
