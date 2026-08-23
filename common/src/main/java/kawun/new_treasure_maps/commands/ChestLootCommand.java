package kawun.new_treasure_maps.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import kawun.new_treasure_maps.utils.Utils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SeededContainerLoot;

public class ChestLootCommand {


    public static LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return Commands.literal("chest_loot")
                .then(Commands.argument("rarity", IntegerArgumentType.integer(0, 2)).executes(ChestLootCommand::execute)
                        .then(Commands.argument("is_map", BoolArgumentType.bool()).executes(ChestLootCommand::execute)));
    }


    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();

        int rarity = IntegerArgumentType.getInteger(context, "rarity");
        boolean is_map = false;
        try {
            is_map = BoolArgumentType.getBool(context, "is_map");
        } catch (Exception e) {

        }
        String name = (is_map ? "Map" : "Loot") + " rariry " + rarity;
        String path = (is_map ? "treasure_map/" : "treasure/") + rarity;

        ItemStack itemStack = new ItemStack(Items.CHEST);
        SeededContainerLoot loot = new SeededContainerLoot(ResourceKey.create(Registries.LOOT_TABLE, Utils.identifier(path)), 0);
        itemStack.set(DataComponents.CONTAINER_LOOT, loot);
        itemStack.set(DataComponents.ITEM_NAME, Component.literal(name));

        player.getInventory().add(itemStack);

        return 1;
    }
}
