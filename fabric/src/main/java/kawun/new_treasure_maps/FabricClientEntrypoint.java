package kawun.new_treasure_maps;

import kawun.new_treasure_maps.client.NewTreasureMapsClient;
import kawun.new_treasure_maps.client.model_property.MapTypeModelProperty;
import kawun.new_treasure_maps.items.Items;
import kawun.new_treasure_maps.items.TreasureMap;
import kawun.new_treasure_maps.network.Network;
import kawun.new_treasure_maps.utils.Utils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.item.CreativeModeTabs;

public class FabricClientEntrypoint implements ClientModInitializer {


    @Override
    public void onInitializeClient() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(tab -> tab.accept(Items.MUSIC_DISC_PIRATE_COVE));

        Network.registerHandler((type, consumer) -> ClientPlayNetworking
                .registerGlobalReceiver(type, (payload, context) -> consumer.accept(payload)));

        ClientPlayConnectionEvents.DISCONNECT.register((c, m) -> NewTreasureMapsClient.clientLeaved());

        TreasureMap.splitToolTip = true;

        Constants.LOG.info("ItemProperty: " + Items.TREASURE_MAP);
        ItemProperties.register(Items.TREASURE_MAP, Utils.identifier("map_type"), new MapTypeModelProperty());
    }
}
