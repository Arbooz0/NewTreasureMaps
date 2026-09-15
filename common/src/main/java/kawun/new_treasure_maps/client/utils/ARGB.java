package kawun.new_treasure_maps.client.utils;

import net.minecraft.util.Mth;

public class ARGB {


    public static int alpha(final int color) {
        return color >>> 24;
    }

    public static int red(final int color) {
        return color >> 16 & 0xFF;
    }

    public static int green(final int color) {
        return color >> 8 & 0xFF;
    }

    public static int blue(final int color) {
        return color & 0xFF;
    }


    public static int color(final int alpha, final int red, final int green, final int blue) {
        return (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
    }


    public static int color(final float alpha, final int rgb) {
        return Mth.floor(alpha * 255.0F) << 24 | rgb & 16777215;
    }

    public static int scaleRGB(final int color, final float scale) {
        return color(
                alpha(color),
                Math.clamp((int)(red(color) * scale), 0, 255),
                Math.clamp((int)(green(color) * scale), 0, 255),
                Math.clamp((int)(blue(color) * scale), 0, 255)
        );
    }

    public static int scaleRGB(final int color, final int scale) {
        return color(
                alpha(color),
                Math.clamp((long)red(color) * scale / 255L, 0, 255),
                Math.clamp((long)green(color) * scale / 255L, 0, 255),
                Math.clamp((long)blue(color) * scale / 255L, 0, 255)
        );
    }


    public static int multiplyAlpha(final int color, final float alphaMultiplier) {
        if (color == 0 || alphaMultiplier <= 0.0F) {
            return 0;
        } else {
            return alphaMultiplier >= 1.0F ? color : color((alpha(color) / 255.0F) * alphaMultiplier, color);
        }
    }

}
