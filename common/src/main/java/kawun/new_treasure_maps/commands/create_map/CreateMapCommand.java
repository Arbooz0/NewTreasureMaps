package kawun.new_treasure_maps.commands.create_map;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.commands.Command;
import kawun.new_treasure_maps.maps.TestMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import org.joml.Vector2i;

public class CreateMapCommand extends Command {


    public static LiteralArgumentBuilder<CommandSourceStack> get_command() {
        return Commands.literal("create_map").executes(CreateMapCommand::execute);
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        Constants.LOG.info("CREATE MAP!");

        CommandSourceStack source = context.getSource();

        ServerPlayer player = source.getPlayer();
        Vector2i pos = new Vector2i((int) source.getPosition().x, (int) source.getPosition().z);

        player.getInventory().add(TestMap.create_map(pos, source.getLevel()));
        return 1;
    }

}
