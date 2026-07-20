package kawun.new_treasure_maps.loot;

import kawun.new_treasure_maps.Constants;
import kawun.new_treasure_maps.NewTreasureMaps;
import kawun.new_treasure_maps.items.Items;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
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

import java.lang.reflect.Field;
import java.util.*;

public class RandomItems {

    public static ArrayList<ItemEntry> items = null;

    public static void init() {
        if (items != null) {
            return;
        }

        items = new ArrayList<>();

        int[] rarities = new int[4];
        int[] types = new int[Type.values().length];

        HashSet<Identifier> recipe = new HashSet<>();

        Collection<RecipeHolder<?>> recipes = NewTreasureMaps.server.getRecipeManager().getRecipes();
        for (RecipeHolder recipeHolder : recipes) {
            Identifier id = recipeHolder.id().identifier();
            recipe.add(id);
        }

        HashSet<Item> loots = new HashSet<>();

        try {
            for (Holder.Reference<LootTable> lootTableReference : NewTreasureMaps.server.reloadableRegistries().lookup().lookupOrThrow(Registries.LOOT_TABLE).listElements().toList()) {
                Field poolsField = LootTable.class.getDeclaredField("pools");
                poolsField.setAccessible(true);
                List<LootPool> pools = (List<LootPool>) poolsField.get(lootTableReference.value());

                for (LootPool pool : pools) {
                    Field entriesField = LootPool.class.getDeclaredField("entries");
                    entriesField.setAccessible(true);
                    List<LootPoolEntryContainer> entries = (List<LootPoolEntryContainer>) entriesField.get(pool);

                    for (LootPoolEntryContainer entry : entries) {
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
            boolean vanilla = id.getNamespace().equals("minecraft");

            Type type = Type.ITEMS;

            if (item instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                if (block.getLootTable().isEmpty()) {
                    //if (!vanilla) {Constants.LOG.info("No has drop: " + item);}
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
                ItemStack itemStack = item.getDefaultInstance();
                if (isWeapon(itemStack)) {
                    type = Type.WEAPON;
                    //if (!vanilla) {Constants.LOG.info("WEAPON: " + item);}
                } else if (isArmor(itemStack)) {
                    type = Type.ARMOR;
                    //if (!vanilla) {Constants.LOG.info("ARMOR: " + item);}
                } else if (isFood(itemStack)) {
                    type = Type.FOOD;
                    //if (!vanilla) {Constants.LOG.info("FOOD: " + item);}
                } else {
                    if (!recipe.contains(id) && !loots.contains(item)) {
                        //if (!vanilla) {Constants.LOG.info("No loot and recipe: " + item);}
                        continue;
                    }
                }
            }

            types[type.ordinal()] += 1;

            Rarity rarity = item.getDefaultInstance().getRarity();
            rarities[rarity.ordinal()] += 1;
            int w = switch (rarity) {
                case COMMON -> 100;
                case UNCOMMON -> 50;
                case RARE -> 25;
                case EPIC -> 10;
            };
            if (!vanilla) {
                w /= 2;
            }
            items.add(new ItemEntry(item, w, w, rarity.ordinal(), type, vanilla));
        }

        Constants.LOG.info("TOTAL ITEMS: " + items.size() + "/" + BuiltInRegistries.ITEM.size());
        Constants.LOG.info("Rarities: " + Arrays.toString(rarities));
        Constants.LOG.info("Types: ");
        for (int i = 0; i < types.length; i++) {
            Constants.LOG.info(Type.values()[i] + ": " + types[i]);
        }
    }


    public static @Nullable ItemEntry getRandomItem(Type type, int maxRarity, int luck) {
        init();

        int totalWeight = 0;

        ArrayList<ItemEntry> filtered = new ArrayList<>();
        for (ItemEntry entry : items) {
            if (entry.type != type) {
                continue;
            }
            if (entry.rarity > maxRarity) {
                continue;
            }
            entry.calculateWeight(luck);
            if (entry.weight > 0) {
                filtered.add(entry);
                totalWeight += entry.weight;
            }
        }

        if (totalWeight == 0) {
            return null;
        }

        int value = (int) (Math.random() * totalWeight);

        for (ItemEntry entry : filtered) {
            value -= entry.weight;
            if (value < 0) {
                return entry;
            }
        }

        return items.getLast();
    }




    private static void processEntry(LootPoolEntryContainer entry, HashSet<Item> loots) throws Exception {
        if (entry instanceof LootItem lootItem) {
            Field itemField = LootItem.class.getDeclaredField("item");
            itemField.setAccessible(true);
            Holder<Item> itemHolder = (Holder<Item>) itemField.get(lootItem);
            loots.add(itemHolder.value());
        } else if (entry instanceof CompositeEntryBase compositeEntry) {
            Field childrenField = CompositeEntryBase.class.getDeclaredField("children");
            childrenField.setAccessible(true);
            List<LootPoolEntryContainer> children = (List<LootPoolEntryContainer>) childrenField.get(compositeEntry);

            for (LootPoolEntryContainer entryContainer : children) {
                processEntry(entryContainer, loots);
            }

        } else if (entry instanceof TagEntry tagEntry) {
            Field tagField = TagEntry.class.getDeclaredField("tag");
            tagField.setAccessible(true);
            TagKey<Item> tagKey = (TagKey<Item>) tagField.get(tagEntry);

            BuiltInRegistries.ITEM.get(tagKey).ifPresent(holders -> {
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




    public static class ItemEntry {

        public final Item item;
        public final int defaultWeight;
        public final int rarity;
        public final Type type;
        public final boolean vanilla;

        public int weight;


        public ItemEntry(Item item, int defaultWeight, int weight, int rarity, Type type, boolean vanilla) {
            this.item = item;
            this.defaultWeight = defaultWeight;
            this.rarity = rarity;
            this.type = type;
            this.vanilla = vanilla;

            this.weight = weight;
        }

        public void calculateWeight(int luck) {
            if (luck == 0) {
                weight = defaultWeight;
            } else {
                weight = defaultWeight + (rarity - 2) * luck;
            }
        }
    };



    public enum Type {
        ARMOR(3),
        WEAPON(3),
        FOOD(6),
        ITEMS(9),
        BLOCK(8);

        public final int weight;
        public static int totalWeight = 0; // 29

        Type(int weight) {
            this.weight = weight;
        }


        public static Type[] getRandomTypes(int count) {
            Type[] types = new Type[count];
            int index = 0;
            float m = count / (float) totalWeight;

            for (Type type : values()) {
                int countType;
                if (type == BLOCK) {
                    countType = types.length - index;
                } else {
                    float c = type.weight * m;
                    if (Math.random() < (c % 1.0f)) {
                        countType = (int) Math.ceil(c);
                    } else {
                        countType = (int) Math.floor(c);
                    }
                }

                if (countType == 0) {
                    continue;
                }

                for (int i = 0; i < countType; i++) {
                    types[index] = type;
                    index++;
                }
            }

            return types;
        }


        static {
            for (Type type : values()) {
                totalWeight += type.weight;
            }
        }
    }

}
