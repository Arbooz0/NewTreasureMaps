package kawun.new_treasure_maps;


import kawun.new_treasure_maps.client.NewTreasureMapsClient;
import kawun.new_treasure_maps.commands.CommandRegister;
import kawun.new_treasure_maps.network.Network;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT)
public class NeoforgeClientEntrypoint {


    public NeoforgeClientEntrypoint(IEventBus eventBus) {
    }


    @SubscribeEvent
    public static void clientLeaved(ClientPlayerNetworkEvent.LoggingOut event) {
        NewTreasureMapsClient.clientLeaved();
    }


    @SubscribeEvent
    public static void registerClientPayload(RegisterClientPayloadHandlersEvent event) {
        Network.registerHandler(
                (type, consumer) -> event.register(type,
                        ((payload, context) -> consumer.accept(payload)))
        );
    }


    @SubscribeEvent
    public static void registerCommand(RegisterClientCommandsEvent event) {
        CommandRegister.registerClient(event.getDispatcher());
    }




}
