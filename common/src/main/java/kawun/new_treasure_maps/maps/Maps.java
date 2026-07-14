package kawun.new_treasure_maps.maps;

import com.mojang.brigadier.context.CommandContext;
import kawun.new_treasure_maps.enums.MapType;
import kawun.new_treasure_maps.items.Items;
import kawun.new_treasure_maps.saveddata.FreeID;
import kawun.new_treasure_maps.utils.TimePassed;
import kawun.new_treasure_maps.utils.Utils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2i;

import java.awt.*;


public class Maps {


    public static ItemStack createMapFromCommand(MapType type, CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        ServerLevel level = source.getLevel();
        Vector2i player_pos = new Vector2i(player.getBlockX(), player.getBlockZ());

        int id = FreeID.getFreeID();
        ItemStack itemStack = Items.newTreasureMap(id);

        TimePassed time = new TimePassed();
        BaseMapCreate map = switch (type) {
            case NONE -> null;
            case DOTTED_LINE -> new DottedLineMapCreate();
            case COLORED -> new ColoredMapCreate();
            case LANDMARKS -> new LandmarksMapCreate();
            case PERSPECTIVE -> new PerspectiveMapCreate();
            case SIDE_VIEW -> null;
            case TEST -> new TestMapCreate();
        };
        if (map != null) {
            map.setContext(player_pos, level, id);
        } else {
            Utils.sendErrorCreateMap();
        }
        time.end("Init map");

        player.getInventory().add(itemStack);
        return itemStack;
    }


}
