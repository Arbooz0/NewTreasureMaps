package kawun.new_treasure_maps;


import kawun.new_treasure_maps.utils.TimePassed;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;

public class NewTreasureMaps {


    public static MinecraftServer server;
    public static ArrayList<Task> tasks = new ArrayList<>();
    public static int totalTimeTask = 0;


    public static void init() {

    }


    public static void serverStarted(MinecraftServer server) {
        NewTreasureMaps.server = server;
        server.addTickable(NewTreasureMaps::tick);
    }


    private static void tick() {
        if (tasks.isEmpty()) {
            return;
        }

        Task task = tasks.getFirst();

        long start = System.currentTimeMillis();
        boolean isFinished = task.run();
        long end = System.currentTimeMillis();
        end -= start; // passed
        totalTimeTask += (int) end;
        Constants.LOG.info("Task running: " + end + " ms");

        if (isFinished) {
            tasks.removeFirst();
            Constants.LOG.info("Total time Task running: " + totalTimeTask + " ms");
            totalTimeTask = 0;
        }
    }


    public static void addTask(Task task) {
        tasks.add(task);
    }



    public interface Task {
        boolean run();
    }







    public static byte compress(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8)  & 0xFF;
        int b =  rgb        & 0xFF;
        int r3 = r >> 5;
        int g3 = g >> 5;
        int b2 = b >> 6;
        int packed = (r3 << 5) | (g3 << 2) | b2;
        return (byte) packed;
    }

    public static int decompress(byte compressed) {
        int c = compressed & 0xFF;
        int r3 = (c >> 5) & 0x07;
        int g3 = (c >> 2) & 0x07;
        int b2 =  c       & 0x03;
        int r = (r3 * 255) / 7;
        int g = (g3 * 255) / 7;
        int b = (b2 * 255) / 3;
        return (r << 16) | (g << 8) | b;
    }
}