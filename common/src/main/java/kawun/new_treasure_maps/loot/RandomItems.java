package kawun.new_treasure_maps.loot;

import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.NewTreasureMaps;
import kawun.new_treasure_maps.items.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.*;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class RandomItems {

    public static HashMap<Type, ArrayList<Item>> items = null;

    public static void init() {
        if (items != null) {
            return;
        }

        items = new HashMap<>();
        for (Type type : Type.values()) {
            items.put(type, new ArrayList<>());
        }

        int[] types = new int[Type.values().length];

        HashSet<Identifier> recipe = new HashSet<>();

        Collection<RecipeHolder<?>> recipes = NewTreasureMaps.server.getRecipeManager().getRecipes();
        for (RecipeHolder recipeHolder : recipes) {
            Identifier id = recipeHolder.id().identifier();
            if (!id.getNamespace().equals("minecraft")) {
                recipe.add(id);
            }
        }

        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.TREASURE_MAP) {
                continue;
            }

            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            if (id.getNamespace().equals("minecraft")) {
                continue;
            }
            if (id.getPath().contains("debug") || id.getPath().contains("test") || id.getPath().contains("creative")) {
                continue;
            }

            ItemStack itemStack = item.getDefaultInstance();
            if (itemStack.getRarity() != Rarity.COMMON) {
                continue;
            }

            Type type = Type.ITEM;

            if (isWeapon(itemStack)) {
                type = Type.WEAPON;
            } else if (isArmor(itemStack)) {
                type = Type.ARMOR;
            } else if (isFood(itemStack)) {
                type = Type.FOOD;
            } else {
                if (item instanceof BlockItem blockItem) {
                    Block block = blockItem.getBlock();
                    if (block.getLootTable().isEmpty()) {
                        continue;
                    }
                    if (block instanceof StairBlock) {
                        continue;
                    } else if (block instanceof SlabBlock) {
                        continue;
                    } else if (block instanceof FenceBlock) {
                        continue;
                    } else if (block instanceof FenceGateBlock) {
                        continue;
                    } else if (block instanceof WallBlock) {
                        continue;
                    } else if (block instanceof TrapDoorBlock) {
                        continue;
                    } else if (block instanceof DoorBlock) {
                        continue;
                    }
                    type = Type.BLOCK;

                } else {
                    if (!recipe.contains(id)) {
                        continue;
                    }
                }
            }

            types[type.ordinal()] += 1;

            items.get(type).add(item);
        }

        Constants.LOG.info("Types: ");
        for (int i = 0; i < types.length; i++) {
            Constants.LOG.info(Type.values()[i] + ": " + types[i]);
        }
    }


    public static @Nullable Item getRandomItem(String name) {
        init();

        Type type = Type.map.get(name);
        if (type == null) {
            Constants.LOG.error("No has type " + name);
            return null;
        }

        if (Math.random() > type.percent) {
            return null;
        }

        ArrayList<Item> list = items.get(type);
        if (list.isEmpty()) {
            return null;
        }

        return list.get((int) (Math.random() * list.size()));
    }


    public static boolean isWeapon(ItemStack item) {
        return item.has(DataComponents.TOOL) || item.has(DataComponents.WEAPON);
    }

    public static boolean isArmor(ItemStack item) {
        return item.has(DataComponents.EQUIPPABLE);
    }

    public static boolean isFood(ItemStack item) {
        return item.has(DataComponents.FOOD);
    }



    public enum Type {
        ARMOR(0.02f),
        WEAPON(0.02f),
        FOOD(0.3f),
        ITEM(0.1f),
        BLOCK(0.1f);

        public final float percent;
        public static HashMap<String, Type> map = new HashMap<>();

        Type(float percent) {
            this.percent = percent;
        }

        static {
            for (Type type : values()) {
                map.put(type.name().toLowerCase(), type);
            }
        }
    }

}
