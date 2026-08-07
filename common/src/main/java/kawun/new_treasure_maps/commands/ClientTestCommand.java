package kawun.new_treasure_maps.commands;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.client.texture.MapTextureManager;
import kawun.new_treasure_maps.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.BitStorage;
import net.minecraft.util.Mth;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Optional;

public class ClientTestCommand {




    public static LiteralArgumentBuilder<CommandSourceStack> get_command() {
        return Commands.literal("client")
                                .then(Commands.argument("text", StringArgumentType.string())
                                    .executes(ClientTestCommand::execute));
    }


    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        String text = StringArgumentType.getString(context, "text");
        Identifier id = Identifier.parse(text);
        Block block = BuiltInRegistries.BLOCK.getValue(id);
        BlockState state = block.defaultBlockState();

        //Utils.sendMessage(text + ": " + state.isSolidRender());

        NativeImage image = MapTextureManager.loadBlockTexture(id);
        if (image != null) {
            try {
                image.writeToFile(Paths.get("C:/Users/Admin/Downloads/test/map.png"));
                Constants.LOG.info("WRITE");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return 1;
    }







}
