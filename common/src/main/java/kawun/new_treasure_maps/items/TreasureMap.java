package kawun.new_treasure_maps.items;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.client.render.MapRenderer;
import kawun.new_treasure_maps.enums.MapType;
import kawun.new_treasure_maps.network.Network;
import kawun.new_treasure_maps.network.OpenMapPacket;
import kawun.new_treasure_maps.saveddata.MapSavedData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ARGB;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class TreasureMap extends Item {

    public static boolean splitToolTip = false;
    public static final int colorDescription = ARGB.color(173, 130, 102);
    public static Int2ObjectOpenHashMap<Tracker> trackers = new Int2ObjectOpenHashMap<>();
    public static Int2ObjectOpenHashMap<MapType> changeType = new Int2ObjectOpenHashMap<>();
    private int tick = 0;


    public TreasureMap(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack itemStack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        if (level.isClientSide()) {
            return;
        }

        if (slot == null || slot.getType() != EquipmentSlot.Type.HAND) {
            return;
        }

        tick++;
        if (tick < 10) {
            return;
        }
        tick = 0;

        if (owner instanceof ServerPlayer ownerPlayer) {
            MapComponent data = itemStack.get(Items.MAP_COMPONENT);
            if (data == null) {
                return;
            }
            if (changeType.containsKey(data.id())) {
                data = data.changeType(changeType.remove(data.id()));
                itemStack.set(Items.MAP_COMPONENT, data);
            }
            getTracker(data.id()).update(level, ownerPlayer, data.mapType() == MapType.NONE);
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        MapComponent data = player.getItemInHand(hand).get(Items.MAP_COMPONENT);
        if (data == null) {
            return InteractionResult.FAIL;
        }

        if (level.isClientSide()) {
            MapRenderer.toggleOpenMap(data.id());
        } else {
            getTracker(data.id()).toggleOpen((ServerPlayer) player);
        }

        return InteractionResult.CONSUME;
    }


    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        MapComponent data = itemStack.get(Items.MAP_COMPONENT);
        if (data == null) {
            return;
        }
        String key = switch (data.mapType()) {
            case DOTTED_LINE -> "new_treasure_maps.tip_dotted_line";
            case PERSPECTIVE -> "new_treasure_maps.tip_perspective";
            case DRAWN -> "new_treasure_maps.tip_drawn";
            case AREA_DRAWN, AREA_COLORED -> "new_treasure_maps.tip_area";
            default -> "";
        };
        if (!key.isEmpty()) {
            if (splitToolTip) {
                String line = "";
                for (String word : Component.translatable(key).getString().split(" ")) {
                    line += word + " ";
                    if (line.length() > 30) {
                        builder.accept(Component.literal(line).withColor(colorDescription));
                        line = "";
                    }
                }
                if (!line.isEmpty()) {
                    builder.accept(Component.literal(line).withColor(colorDescription));
                }
            } else {
                builder.accept(Component.translatable(key).withColor(colorDescription));
            }
        }
    }


    public static Tracker getTracker(int id) {
        return trackers.computeIfAbsent(id, i -> new Tracker(id));
    }


    public static void clear() {
        trackers.clear();
    }

    public static void playerLeaved(ServerPlayer player) {
        for (Tracker tracker : trackers.values()) {
            tracker.playersReceived.removeBoolean(player);
        }
    }



    public static class Tracker {

        public final int id;
        public Object2BooleanOpenHashMap<ServerPlayer> playersReceived = new Object2BooleanOpenHashMap<>();
        public boolean isOpen = false;


        public Tracker(int id) {
            this.id = id;
        }


        public void update(ServerLevel level, ServerPlayer ownerPlayer, boolean isNone) {
            for (ServerPlayer player : getPlayersNearPlayer(level, ownerPlayer, 10)) {

                if (playersReceived.containsKey(player)) {
                    if (playersReceived.getBoolean(player) != isOpen) {
                        playersReceived.put(player, isOpen);
                        sendOpen(player);
                    }
                } else {
                    if (isNone) {
                        playersReceived.put(player, isOpen);
                        if (isOpen) {
                            sendOpen(player);
                        }
                    } else {
                        MapSavedData savedData = MapSavedData.load(id);
                        if (savedData != null) {
                            savedData.sendToPlayer(player);
                            playersReceived.put(player, isOpen);
                            if (isOpen) {
                                sendOpen(player);
                            }
                            Constants.LOG.info("Send MapData " + id + " to " + player);
                        } else {
                            break;
                        }
                    }
                }

            }
        }


        public List<ServerPlayer> getPlayersNearPlayer(ServerLevel level, ServerPlayer player, float radius) {
            float radiusSq = radius * radius;
            return level.getPlayers((p) -> p.distanceToSqr(player) <= radiusSq);
        }


        public void toggleOpen(ServerPlayer playerOpen) {
            isOpen = !isOpen;
            /*if (playersReceived.containsKey(playerOpen)) {
                playersReceived.put(playerOpen, isOpen);
            }*/

            for (Object2BooleanMap.Entry<ServerPlayer> entry : playersReceived.object2BooleanEntrySet()) {
                if (isOpen != entry.getBooleanValue()) {
                    if (entry.getKey().distanceToSqr(playerOpen) <= 100) {
                        playersReceived.put(entry.getKey(), isOpen);
                        sendOpen(entry.getKey());
                    }
                }
            }
        }



        public void sendOpen(ServerPlayer player) {
            Network.sendToPlayer(player, new OpenMapPacket(id, isOpen));
        }



    }


}
