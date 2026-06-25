package kawun.new_treasure_maps;


import kawun.new_treasure_maps.commands.CommandRegister;
import kawun.new_treasure_maps.items.Items;
import kawun.new_treasure_maps.network.Network;
import kawun.new_treasure_maps.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.packs.resources.Resource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientResourceLoadFinishedEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;

@Mod(Constants.MOD_ID)
@EventBusSubscriber
public class NewTreasureMapsNeoforge {

    public NewTreasureMapsNeoforge(IEventBus eventBus, Dist dist) {
        NewTreasureMaps.init();

        Constants.LOG.info("+----------------+");
        Constants.LOG.info("Load: " + dist);
        Constants.LOG.info("+----------------+");
    }

    @SubscribeEvent
    public static void clientLoaded(ClientResourceLoadFinishedEvent event) {
        Constants.LOG.info("RESOURCE LOAD FINISHED");
    }


    @SubscribeEvent
    public static void serverStarted(ServerStartedEvent event) {
        NewTreasureMaps.serverStarted(event.getServer());
    }


    @SubscribeEvent
    public static void registerEvent(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.ITEM)) {
            Items.register((id, item) -> event.register(Registries.ITEM, id, () -> item));
        }
        if (event.getRegistryKey().equals(Registries.DATA_COMPONENT_TYPE)) {
            Items.registerComponents((id, data) -> event.register(Registries.DATA_COMPONENT_TYPE, id, () -> data));
        }

    }


    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {
        CommandRegister.register(event.getDispatcher());
    }


    @SubscribeEvent
    public static void registerPayload(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        Network.registerPacket(
                (type, codec) -> registrar.playToClient(type, codec)
        );
    }


    @SubscribeEvent
    public static void registerClientPayload(RegisterClientPayloadHandlersEvent event) {
        Network.registerHandler(
                (type, consumer) -> event.register(type,
                        ((payload, context) -> consumer.accept(payload)))
        );
    }


}