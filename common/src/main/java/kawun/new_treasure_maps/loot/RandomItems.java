package kawun.new_treasure_maps.loot;

import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.NewTreasureMaps;
import kawun.new_treasure_maps.items.Items;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.CompositeEntryBase;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.TagEntry;
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

        HashSet<Item> loots = new HashSet<>();

        try {
            for (Holder.Reference<LootTable> lootTableReference : NewTreasureMaps.server.reloadableRegistries().lookup().lookupOrThrow(Registries.LOOT_TABLE).listElements().toList()) {
                for (LootPool pool : lootTableReference.value().pools) {
                    for (LootPoolEntryContainer entry : pool.entries) {
                        processEntry(entry, loots);
                    }
                }
            }
        } catch (Exception e) {
            Constants.LOG.error("ERROR LOAD LOOT TABLE: " + e.getMessage());
        }

        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.TREASURE_MAP) {
                continue;
            }

            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            if (id.getNamespace().equals("minecraft")) {
                continue;
            }
            ItemStack itemStack = item.getDefaultInstance();
            if (itemStack.getRarity() != Rarity.COMMON) {
                continue;
            }

            Type type = Type.ITEM;

            if (isWeapon(itemStack)) {
                type = Type.WEAPON;
                Constants.LOG.info("WEAPON: " + item);
            } else if (isArmor(itemStack)) {
                type = Type.ARMOR;
                Constants.LOG.info("ARMOR: " + item);
            } else if (isFood(itemStack)) {
                type = Type.FOOD;
                Constants.LOG.info("FOOD: " + item);
            } else {
                if (item instanceof BlockItem blockItem) {
                    Block block = blockItem.getBlock();
                    if (block.getLootTable().isEmpty()) {
                        Constants.LOG.info("No has drop: " + item);
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
                    if (!recipe.contains(id) && !loots.contains(item)) {
                        Constants.LOG.info("No loot and recipe: " + item);
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




    private static void processEntry(LootPoolEntryContainer entry, HashSet<Item> loots) {
        if (entry instanceof LootItem lootItem) {
            loots.add(lootItem.item.value());
        } else if (entry instanceof CompositeEntryBase compositeEntry) {
            for (LootPoolEntryContainer entryContainer : compositeEntry.children) {
                processEntry(entryContainer, loots);
            }

        } else if (entry instanceof TagEntry tagEntry) {
            BuiltInRegistries.ITEM.get(tagEntry.tag).ifPresent(holders -> {
                for (Holder<Item> holder : holders) {
                    loots.add(holder.value());
                }
            });
        }
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
        FOOD(0.2f),
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
