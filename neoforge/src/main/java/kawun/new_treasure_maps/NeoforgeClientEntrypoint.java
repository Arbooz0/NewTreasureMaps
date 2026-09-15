package kawun.new_treasure_maps;


import kawun.new_treasure_maps.client.NewTreasureMapsClient;
import kawun.new_treasure_maps.client.model_property.MapTypeModelProperty;
import kawun.new_treasure_maps.items.Items;
import kawun.new_treasure_maps.utils.Utils;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class NeoforgeClientEntrypoint {


    public NeoforgeClientEntrypoint(IEventBus eventBus) {
        eventBus.addListener(this::creativeTabs);
        eventBus.addListener(this::clientSetup);
        NeoForge.EVENT_BUS.addListener(this::clientLeaved);
    }


    public void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(this::registerItemProperty);
    }


    public void registerItemProperty() {
        ItemProperties.register(Items.TREASURE_MAP, Utils.identifier("map_type"), new MapTypeModelProperty());
    }


    public void creativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(Items.MUSIC_DISC_PIRATE_COVE);
        }
    }


    public void clientLeaved(ClientPlayerNetworkEvent.LoggingOut event) {
        NewTreasureMapsClient.clientLeaved();
    }




}
