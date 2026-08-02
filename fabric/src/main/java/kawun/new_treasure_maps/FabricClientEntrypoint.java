package kawun.new_treasure_maps;

import kawun.new_treasure_maps.client.NewTreasureMapsClient;
import kawun.new_treasure_maps.network.Network;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class FabricClientEntrypoint implements ClientModInitializer {


    @Override
    public void onInitializeClient() {
        Network.registerHandler((type, consumer) -> ClientPlayNetworking
                .registerGlobalReceiver(type, (payload, context) -> consumer.accept(payload)));

        ClientPlayConnectionEvents.DISCONNECT.register((c, m) -> NewTreasureMapsClient.clientLeaved());
    }
}
