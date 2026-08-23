package kawun.new_treasure_maps.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import kawun.new_treasure_maps.config.ConfigManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ConfigCommand {




    public static LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return Commands.literal("config")
                .then(Commands.literal("imbalanced_loot").then(Commands.argument("imbalanced_loot", BoolArgumentType.bool()).executes(ConfigCommand::imbalanced_loot)))
                .then(Commands.literal("include_other_mods_items").then(Commands.argument("include_other_mods_items", BoolArgumentType.bool()).executes(ConfigCommand::include_other_mods_items)))
                ;
    }


    private static int imbalanced_loot(CommandContext<CommandSourceStack> context) {
        ConfigManager.config.imbalanced_loot = BoolArgumentType.getBool(context, "imbalanced_loot");
        ConfigManager.save();
        return 1;
    }


    private static int include_other_mods_items(CommandContext<CommandSourceStack> context) {
        ConfigManager.config.include_other_mods_items = BoolArgumentType.getBool(context, "include_other_mods_items");
        ConfigManager.save();
        return 1;
    }




}
