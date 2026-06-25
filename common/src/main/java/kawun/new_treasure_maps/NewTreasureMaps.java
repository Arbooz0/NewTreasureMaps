package kawun.new_treasure_maps;


import net.minecraft.server.MinecraftServer;

public class NewTreasureMaps {


    public static MinecraftServer server;


    public static void init() {

    }


    public static void serverStarted(MinecraftServer server) {
        NewTreasureMaps.server = server;
    }

    public static byte compress(int rgb) {
        // 1. Извлекаем чистые каналы 0-255
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8)  & 0xFF;
        int b =  rgb        & 0xFF;

        // 2. Урезаем биты (сдвигаем вправо, оставляя только старшие биты)
        int r3 = r >> 5; // Из 8 бит оставляем 3 старших (значения 0..7)
        int g3 = g >> 5; // Из 8 бит оставляем 3 старших (значения 0..7)
        int b2 = b >> 6; // Из 8 бит оставляем 2 старших (значения 0..3)

        // 3. Упаковываем их в один байт по схеме: RRRGGGBB
        int packed = (r3 << 5) | (g3 << 2) | b2;

        return (byte) packed;
    }

    public static int decompress(byte compressed) {
        // Убираем знаковый бит Java, превращая byte (-128..127) в чистый int (0..255)
        int c = compressed & 0xFF;

        // 1. Достаем урезанные каналы по их маскам
        int r3 = (c >> 5) & 0x07; // Маска 00000111
        int g3 = (c >> 2) & 0x07; // Маска 00000111
        int b2 =  c       & 0x03; // Маска 00000011

        // 2. Растягиваем их обратно до диапазона 0..255
        // Формула (val * 255) / max гарантирует, что максимальное сжатое значение станет ровно 255
        int r = (r3 * 255) / 7;
        int g = (g3 * 255) / 7;
        int b = (b2 * 255) / 3;

        // 3. Собираем обратно в один int (формат RGB)
        return (r << 16) | (g << 8) | b;
    }
}