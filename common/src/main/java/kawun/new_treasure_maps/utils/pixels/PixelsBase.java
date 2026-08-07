package kawun.new_treasure_maps.utils.pixels;

import java.util.ArrayList;

public abstract class PixelsBase {


    public final int width;
    public final int height;


    public ArrayList<CopyImage> copyImages;



    public PixelsBase(int width, int height) {
        this.width = width;
        this.height = height;
    }


    public abstract int getPixel(int index);


    public void addCopyImage(String texture, int x, int y) {
        if (copyImages == null) {
            copyImages = new ArrayList<>();
        }
        copyImages.add(new CopyImage(texture, x, y));
    }



    public record CopyImage(String texture, int x, int y){};
}
