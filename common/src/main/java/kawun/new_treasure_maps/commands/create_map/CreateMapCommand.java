package kawun.new_treasure_maps.commands.create_map;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.commands.Command;
import kawun.new_treasure_maps.enums.MapType;
import kawun.new_treasure_maps.maps.Maps;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CreateMapCommand extends Command {


    public static LiteralArgumentBuilder<CommandSourceStack> get_command() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("create_map");

        for (MapType type : MapType.values()) {
            command.then(Commands.literal(type.name().toLowerCase()).executes(
                    context -> execute(type, context)
            ));
        }
        return command;
    }

    private static int execute(MapType type, CommandContext<CommandSourceStack> context) {
        Constants.LOG.info("CREATE MAP: " + type);
        Maps.createMapFromCommand(type, context);
        return 1;
    }

}
