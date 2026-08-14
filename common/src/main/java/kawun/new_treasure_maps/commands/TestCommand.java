package kawun.new_treasure_maps.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.client.texture.MapTextureManager;
import kawun.new_treasure_maps.utils.TimePassed;
import kawun.new_treasure_maps.utils.Utils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.BitStorage;
import net.minecraft.util.Mth;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Optional;

public class TestCommand {




    public static LiteralArgumentBuilder<CommandSourceStack> get_command() {
        return Commands.literal("test")
                                .then(Commands.argument("text", StringArgumentType.string())
                                    .executes(TestCommand::execute));
    }


    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        ServerLevel level = source.getLevel();
        ItemStack itemStack = player.getMainHandItem();
        BlockPos.MutableBlockPos pos = player.blockPosition().mutable();
        int x = player.getBlockX();
        int z = player.getBlockZ();

        String text = StringArgumentType.getString(context, "text");

        Optional<StructureTemplate> optional = level.getStructureManager().get(Utils.identifier(text));
        if (optional.isEmpty()) {
            return 0;
        }

        StructureTemplate structureTemplate = optional.get();
        Vec3i size = structureTemplate.getSize();
        pos.move(size.getX() / -2, 0, size.getZ() / -2);

        structureTemplate.placeInWorld(level, pos, pos, new StructurePlaceSettings(), level.getRandom(), 2);

        return 1;
    }




}
