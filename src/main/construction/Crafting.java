package main.construction;


import java.util.Map;

public class Crafting {


    public static int player_wood;
    public static int player_stone;
    public static int player_copper_ore;
    public static int player_copper_bar;
    public static int player_coal;
    public static int player_iron_ore;
    public static int player_iron_bar;
    public static int player_gold_ore;
    public static int player_gold_bar;
    public static int player_fiber;
    public static int iridium_ore;
    public static int iridium_bar;
    public static int player_acorn;
    public static int player_maple_seed;
    public static int player_pine_cone;
    public static int player_mahogany_seed;


    public static int player_x;
    public static int player_y;
    public static int house_x;
    public static int house_y;
    public static int house_length;
    public static int house_width;

    public static int mining_level;
    public static int farming_level;
    public static int foraging_level;
    public static boolean Pierres_General_Store;
    public static boolean Fish_Shop;


    public static int player_energy = 100;
    public static final int MAX_INVENTORY_SIZE = 20;

    public static void craftItem(String itemName) {

        Item itemToCraft = null;
        for (Item item : Construction.items) {
            if (item.name.equalsIgnoreCase(itemName)) {
                itemToCraft = item;
                break;
            }
        }

        if (itemToCraft == null) {
            CraftingViews.InvalidItemName();
            return;
        }


        if (Construction.player_inventory.size() >= MAX_INVENTORY_SIZE) {
            CraftingViews.IsInventoryFull();
            return;
        }


        if (player_energy < 2) {
            return;
        }


        for (Map.Entry<String, Integer> entry : itemToCraft.required_materials.entrySet()) {
            String material = entry.getKey();
            int required = entry.getValue();

            int available = getMaterialCount(material);
            if (available < required) {
                CraftingViews.NotEnoughResources();
                return;
            }
        }

        for (Map.Entry<String, Integer> entry : itemToCraft.required_materials.entrySet()) {
            removeMaterial(entry.getKey(), entry.getValue());
        }


        Construction.player_inventory.put(itemToCraft, Construction.player_inventory.getOrDefault(itemToCraft, 0) + 1);
        player_energy -= 2;

    }

    public static int getMaterialCount(String material) {
        switch (material) {
            case "wood": return player_wood;
            case "stone": return player_stone;
            case "copper ore": return player_copper_ore;
            case "copper bar": return player_copper_bar;
            case "coal": return player_coal;
            case "iron ore": return player_iron_ore;
            case "iron bar": return player_iron_bar;
            case "gold ore": return player_gold_ore;
            case "gold bar": return player_gold_bar;
            case "fiber": return player_fiber;
            case "iridium ore": return iridium_ore;
            case "iridium bar": return iridium_bar;
            case "acorn": return player_acorn;
            case "maple seed": return player_maple_seed;
            case "pine cone": return player_pine_cone;
            case "mahogany seed": return player_mahogany_seed;
            default: return 0;
        }
    }

    public static void removeMaterial(String material, int amount) {
        switch (material) {
            case "wood": player_wood -= amount; break;
            case "stone": player_stone -= amount; break;
            case "copper ore": player_copper_ore -= amount; break;
            case "copper bar": player_copper_bar -= amount; break;
            case "coal": player_coal -= amount; break;
            case "iron ore": player_iron_ore -= amount; break;
            case "iron bar": player_iron_bar -= amount; break;
            case "gold ore": player_gold_ore -= amount; break;
            case "gold bar": player_gold_bar -= amount; break;
            case "fiber": player_fiber -= amount; break;
            case "iridium ore": iridium_ore -= amount; break;
            case "iridium bar": iridium_bar -= amount; break;
            case "acorn": player_acorn -= amount; break;
            case "maple seed": player_maple_seed -= amount; break;
            case "pine cone": player_pine_cone -= amount; break;
            case "mahogany seed": player_mahogany_seed -= amount; break;
        }
    }


    public static void CheatAddItem(String item_name,int count){


        Item itemToCraft = null;
        for (Item item : Construction.items) {
            if (item.name.equalsIgnoreCase(item_name)) {
                itemToCraft = item;
                break;
            }
        }

        if (itemToCraft == null) {
            CraftingViews.InvalidItemName();
            return;
        }


        if (Construction.player_inventory.size()+count > MAX_INVENTORY_SIZE) {
            CraftingViews.IsInventoryFull();
            return;
        }






        Construction.player_inventory.put(itemToCraft, count);


    }




}
