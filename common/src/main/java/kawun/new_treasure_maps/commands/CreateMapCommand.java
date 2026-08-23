package kawun.new_treasure_maps.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import kawun.new_treasure_maps.enums.MapType;
import kawun.new_treasure_maps.maps.Maps;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CreateMapCommand {


    public static LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("create_map");

        for (MapType type : MapType.values()) {
            if (type == MapType.NONE) {
                continue;
            }
            command.then(Commands.literal(type.name().toLowerCase()).executes(
                    context -> execute(type, context)
            ));
        }
        return command;
    }

    private static int execute(MapType type, CommandContext<CommandSourceStack> context) {
        Maps.createMapFromCommand(type, context);
        return 1;
    }

}
