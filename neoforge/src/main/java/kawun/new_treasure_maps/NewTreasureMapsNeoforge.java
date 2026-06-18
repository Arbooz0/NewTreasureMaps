package kawun.new_treasure_maps;


import kawun.new_treasure_maps.commands.CommandRegister;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

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
    public static void registerCommand(RegisterCommandsEvent event) {
        CommandRegister.register(event.getDispatcher());
    }
}