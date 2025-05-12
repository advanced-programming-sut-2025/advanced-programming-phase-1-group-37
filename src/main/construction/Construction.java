package main.construction;
import java.util.*;


public class Construction {


    public static List<Item> items=new ArrayList<>();

    public static Map<Item,Integer> player_inventory;


    public static void ListItems(){

        items.add(new Item("Cherry Bomb", Map.of("copper ore", 4, "coal", 1), "Mining", 1, 50));
        items.add(new Item("Bomb", Map.of("iron ore", 4, "coal", 1), "Mining", 2, 50));
        items.add(new Item("Mega Bomb", Map.of("gold ore", 4, "coal", 1), "Mining", 3, 50));
        items.add(new Item("Sprinkler", Map.of("copper bar", 1, "iron bar", 1), "Farming", 1, 0));
        items.add(new Item("Quality Sprinkler", Map.of("Iron bar", 1, "Gold bar", 1), "Farming", 2, 0));
        items.add(new Item("Iridium Sprinkler", Map.of("gold bar", 1, "iridium bar", 1), "Farming", 3, 0));
        items.add(new Item("Charcoal Klin", Map.of("wood", 20, "Copper bar", 2), "Foraging", 1, 0));
        items.add(new Item("Furnace", Map.of("Copper ore", 20, "Stone", 25), "-", 0, 0));
        items.add(new Item("Scarecrow", Map.of("wood", 50, "coal", 1, "Fiber", 20), "-", 0, 0));
        items.add(new Item("Deluxe Scarecrow", Map.of("wood", 50, "coal", 1, "Fiber", 20, "iridium ore", 1), "Farming", 2, 0));
        items.add(new Item("Bee House", Map.of("wood", 40, "coal", 8, "iron bar", 1), "Farming", 1, 0));
        items.add(new Item("Cheese Press", Map.of("wood", 45, "stone", 45, "copper bar", 1), "Farming", 2, 0));
        items.add(new Item("Keg", Map.of("wood", 30, "copper bar", 1, "iron bar", 1), "Farming", 3, 0));
        items.add(new Item("Loom", Map.of("wood", 60, "fiber", 30), "Farming", 3, 0));
        items.add(new Item("Mayonnaise Machine", Map.of("wood", 15, "stone", 15, "copper bar", 1), "-", 0, 0));
        items.add(new Item("Oil Maker", Map.of("wood", 100, "gold bar", 1, "iron bar", 1), "Farming", 3, 0));
        items.add(new Item("Preserves Jar", Map.of("wood", 50, "stone", 40, "coal", 8), "Farming", 2, 0));
        items.add(new Item("Dehydrator", Map.of("wood", 30, "stone", 20, "fiber", 30), "Pierre's General Store", 0, 0));
        items.add(new Item("Grass Starter", Map.of("wood", 1, "fiber", 1), "Pierre's General Store", 0, 0));
        items.add(new Item("Fish Smoker", Map.of("wood", 50, "iron bar", 3, "coal", 10), "Fish Shop", 0, 0));
        items.add(new Item("Mystic Tree Seed", Map.of("acorn", 5, "maple seed", 5, "pine cone", 5, "mahogany seed", 5), "Foraging", 4, 100));


    }


    public static void placeItem(String itemName, String direction) {

        Set<String> validDirections = Set.of("N", "S", "E", "W", "NE", "NW", "SE", "SW");
        if (!validDirections.contains(direction.toUpperCase())) {
            CraftingViews.InvalidDirection();
            return;
        }


        Item targetItem = null;
        for (Item item : player_inventory.keySet()) {
            if (item.name.equalsIgnoreCase(itemName)) {
                targetItem = item;
                break;
            }
        }


        if (targetItem == null) {
            CraftingViews.NotInInventory();
            return;
        }


    }







}
