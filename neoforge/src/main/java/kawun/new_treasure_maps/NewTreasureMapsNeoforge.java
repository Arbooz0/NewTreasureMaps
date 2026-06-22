package kawun.new_treasure_maps;


import kawun.new_treasure_maps.commands.CommandRegister;
import kawun.new_treasure_maps.items.Items;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(Constants.MOD_ID)
@EventBusSubscriber
public class NewTreasureMapsNeoforge {

    public NewTreasureMapsNeoforge(IEventBus eventBus) {

        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        // Use NeoForge to bootstrap the Common mod.
        Constants.LOG.info("Hello NeoForge world!");
        NewTreasureMaps.init();

    }

    @SubscribeEvent
    public static void registerItem(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.ITEM)) {
            Items.register((id, item) -> event.register(Registries.ITEM, id, () -> item));
        }
    }


    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {
        CommandRegister.register(event.getDispatcher());
    }
}