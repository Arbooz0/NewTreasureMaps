package kawun.new_treasure_maps;


import kawun.new_treasure_maps.config.ConfigManager;
import kawun.new_treasure_maps.items.TreasureMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;

public class NewTreasureMaps {


    public static MinecraftServer server;
    public static ArrayList<Task> tasks = new ArrayList<>();
    public static int totalTimeTask = 0;


    public static void init() {
        ConfigManager.load();
    }


    public static void serverStarted(MinecraftServer server) {
        NewTreasureMaps.server = server;
        server.addTickable(NewTreasureMaps::tick);
    }


    public static void serverStopped() {
        tasks.clear();
        TreasureMap.clear();
        server = null;
    }


    public static void playerLeaved(ServerPlayer player) {
        TreasureMap.playerLeaved(player);
    }


    private static void tick() {
        if (tasks.isEmpty()) {
            return;
        }

        Task task = tasks.getFirst();

        long start = System.currentTimeMillis();
        boolean isFinished = true;
        try {
            isFinished = task.run();
        } catch (Exception e) {
            Constants.LOG.error("Error run task: " + e.getMessage());
        }
        long end = System.currentTimeMillis();
        end -= start; // passed
        totalTimeTask += (int) end;

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
}