package kawun.new_treasure_maps.maps;

import com.mojang.brigadier.context.CommandContext;
import kawun.new_treasure_maps.enums.MapType;
import kawun.new_treasure_maps.items.Items;
import kawun.new_treasure_maps.items.MapComponent;
import kawun.new_treasure_maps.saveddata.FreeID;
import kawun.new_treasure_maps.utils.TimePassed;
import kawun.new_treasure_maps.utils.Utils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2i;


public class Maps {


    public static ItemStack createMapFromCommand(MapType type, CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        ServerLevel level = source.getLevel();
        Vector2i player_pos = new Vector2i(player.getBlockX(), player.getBlockZ());

        ItemStack itemStack = createMap(type, player_pos, level, 0);

        player.getInventory().add(itemStack);
        return itemStack;
    }



    public static ItemStack createMap(MapType type, Vector2i pos, ServerLevel level, int lootLevel) {
        int id = FreeID.getFreeID();
        ItemStack itemStack = Items.newTreasureMap(id, type);

        TimePassed time = new TimePassed();
        BaseMapCreate map = switch (type) {
            case NONE -> null;
            case DOTTED_LINE -> new DottedLineMapCreate();
            case COLORED -> new ColoredMapCreate();
            case AREA_COLORED -> new AreaColoredMapCreate();
            case DRAWN -> new DrawnMapCreate();
            case AREA_DRAWN -> new AreaDrawnMapCreate();
            case PERSPECTIVE -> new PerspectiveMapCreate();
            case SIDE_VIEW -> new SideViewMapCreate();
        };
        if (map != null) {
            map.setContext(pos, level, id, lootLevel);
        } else {
            Utils.sendErrorCreateMap();
        }
        time.end("Init map");

        return itemStack;
    }


}
