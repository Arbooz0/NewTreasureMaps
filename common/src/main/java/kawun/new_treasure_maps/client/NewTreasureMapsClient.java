package kawun.new_treasure_maps.client;

import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.client.render.MapRenderer;
import kawun.new_treasure_maps.client.texture.MapTextureManager;

public class NewTreasureMapsClient {


    public static void clientLeaved() {
        Constants.LOG.info("Client leaved");
        MapRenderer.clear();
        MapTextureManager.clear();
    }


}
