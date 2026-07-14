import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;


class Converter {


    public static void main(String[] args) {
        convert("landmarks", Converter::landmarksConvert);
        System.out.println("END");
    }


    public static void convert(String folder, ConvertColor converter) {
        try {
            Path path = Paths.get("server_images/" + folder);

            File[] files = path.toFile().listFiles();

            if (files == null || files.length == 0) {
                System.out.println("empty folder " + folder);
                return;
            }

            for (File file : files) {
                String name = file.getName().split("\\.")[0];

                BufferedImage image = ImageIO.read(file);
                int width = image.getWidth();
                int height = image.getHeight();

                try (FileOutputStream f = new FileOutputStream("common/src/main/resources/data/new_treasure_maps/images/" + name + ".pixels")) {
                    f.write((byte) width);
                    f.write((byte) height);

                    /*System.out.printf("%10s%10s%10s%10s", "--------", "--------", "--------", "--------");
                    System.out.print("\n");*/

                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            int color_rgb = image.getRGB(x, y);
                            byte color = converter.convert(color_rgb);
                            f.write(color);
                            //System.out.printf("%10s", Integer.toBinaryString(color & 0xFF));
                        }
                        //System.out.print("\n");
                    }
                }

                System.out.println("Converted: " + name);
            }

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
        }
    }


    public static byte landmarksConvert(int color) {
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        a >>= 4;

        int type = 0;
        int blackout = 0;

        if (r > 5) {
            blackout = (r >> 6) & 3;
            if (g > 5) {
                if (b < 5) {
                    type = 3;
                }
            } else {
                type = 2;
            }
        } else if (b > 5) {
            type = 1;
            blackout = (b >> 6) & 3;
        }

        return (byte) ((type << 6) | (blackout << 4) | a);
    }



    public static void changeImage(BufferedImage image, File file) {
        int width = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color_rgb = image.getRGB(x, y);
                Color color = new Color(color_rgb, true);
                int a = color.getAlpha();
                if (a >= 250) {
                    continue;
                }
                a /= 2;
                color = new Color(color.getRed(), color.getGreen(), color.getBlue(), a);
                image.setRGB(x, y, color.getRGB());
            }
        }

        try {
            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            System.err.println("Error save " + file.getName() + ": " + e.getMessage());
        }
    }




    public interface ConvertColor {
        byte convert(int color);
    }


}