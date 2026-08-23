package kawun.new_treasure_maps.client;

import kawun.new_treasure_maps.client.render.MapRenderer;
import kawun.new_treasure_maps.client.texture.MapTextureManager;

public class NewTreasureMapsClient {


    public static void clientLeaved() {
        MapRenderer.clear();
        MapTextureManager.clear();
    }


}
