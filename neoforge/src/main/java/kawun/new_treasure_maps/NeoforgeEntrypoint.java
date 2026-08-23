package kawun.new_treasure_maps;


import kawun.new_treasure_maps.commands.CommandRegister;
import kawun.new_treasure_maps.items.Items;
import kawun.new_treasure_maps.loot.LootRegister;
import kawun.new_treasure_maps.loot.LootTableModify;
import kawun.new_treasure_maps.network.Network;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootPool;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(Constants.MOD_ID)
@EventBusSubscriber
public class NeoforgeEntrypoint {

    public NeoforgeEntrypoint(IEventBus eventBus, Dist dist) {
        NewTreasureMaps.init();
    }


    @SubscribeEvent
    public static void serverStarted(ServerStartedEvent event) {
        NewTreasureMaps.serverStarted(event.getServer());
    }

    @SubscribeEvent
    public static void serverStopping(ServerStoppingEvent event) {
        NewTreasureMaps.serverStopped();
    }

    @SubscribeEvent
    public static void playerLeaved(PlayerEvent.PlayerLoggedOutEvent event) {
        NewTreasureMaps.playerLeaved((ServerPlayer) event.getEntity());
    }


    @SubscribeEvent
    public static void registerEvent(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.ITEM)) {
            Items.register((id, item) -> event.register(Registries.ITEM, id, () -> item));
        }
        if (event.getRegistryKey().equals(Registries.DATA_COMPONENT_TYPE)) {
            Items.registerComponents((id, data) -> event.register(Registries.DATA_COMPONENT_TYPE, id, () -> data));
        }
        if (event.getRegistryKey().equals(Registries.LOOT_POOL_ENTRY_TYPE)) {
            LootRegister.registerLootEntry((id, codec) -> event.register(Registries.LOOT_POOL_ENTRY_TYPE, id, () -> codec));
        }
        if (event.getRegistryKey().equals(Registries.LOOT_FUNCTION_TYPE)) {
            LootRegister.registerLootFunction((id, codec) -> event.register(Registries.LOOT_FUNCTION_TYPE, id, () -> codec));
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
    public static void lootTableLoaded(LootTableLoadEvent event) {
        LootPool.Builder pool = LootTableModify.modify(event.getName());
        if (pool != null) {
            event.getTable().addPool(pool.build());
        }
    }


}