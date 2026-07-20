package kawun.new_treasure_maps;

import kawun.new_treasure_maps.commands.CommandRegister;
import kawun.new_treasure_maps.items.Items;
import kawun.new_treasure_maps.loot.LootRegister;
import kawun.new_treasure_maps.loot.LootTableModify;
import kawun.new_treasure_maps.network.Network;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;

public class NewTreasureMapsFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        NewTreasureMaps.init();

        ServerLifecycleEvents.SERVER_STARTED.register((MinecraftServer server) -> NewTreasureMaps.serverStarted(server));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CommandRegister.register(dispatcher);
        });

        Items.register((id, item) -> Registry.register(BuiltInRegistries.ITEM, id, item));
        Items.registerComponents((id, data) -> Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id, data));
        LootRegister.registerLootEntry((id, codec) -> Registry.register(BuiltInRegistries.LOOT_POOL_ENTRY_TYPE, id, codec));
        LootRegister.registerLootFunction((id, codec) -> Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, id, codec));

        Network.registerPacket((type, codec) -> PayloadTypeRegistry.clientboundPlay()
                .register(type, codec));

        Network.registerHandler((type, consumer) -> ClientPlayNetworking
                .registerGlobalReceiver(type, (payload, context) -> consumer.accept(payload)));

        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            LootPool.Builder pool = LootTableModify.modify(key.identifier());
            if (pool != null) {
                tableBuilder.withPool(pool);
            }
        });
    }
}
