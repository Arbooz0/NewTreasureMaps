package kawun.new_treasure_maps.commands.test;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.commands.Command;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class TestCommand extends Command {


    public static LiteralArgumentBuilder<CommandSourceStack> get_command() {
        return Commands.literal("test").executes(TestCommand::execute);
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        Constants.LOG.info("TEST!");
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        ServerLevel level = source.getLevel();

        ItemStack item = player.getMainHandItem();

        MapId id = item.get(DataComponents.MAP_ID);
        MapItemSavedData data = MapItem.getSavedData(item, level);
        Constants.LOG.info("Center X: " + data.centerX + " Z: " + data.centerZ);

        return 1;
    }

}
