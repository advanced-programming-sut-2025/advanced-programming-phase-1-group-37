package main.cooking;

import main.construction.CraftingController;
import main.construction.CraftingViews;

import static main.cooking.Cooking.player_inventory;

public class CookingController {



    public static void Refrigerator(String action, String foodName) {
        if (!CraftingController.IsPlayerInHouse()) {
            CraftingViews.NotPlayerInHouse();
            return;
        }

        Food targetFood = findFoodByName(foodName);
        if (targetFood == null) {
            CraftingViews.InvalidItemName();
            return;
        }

        if (action.equals("put")) {
            if (!player_inventory.containsKey(targetFood)) {
                CraftingViews.NotInInventory();
                return;
            }

            int quantity = player_inventory.get(targetFood);
            player_inventory.remove(targetFood);
            Refrigerator.fridge_inventory.put(targetFood, Refrigerator.fridge_inventory.getOrDefault(targetFood, 0) + quantity);

        } else if (action.equals("pick")) {
            if (!Refrigerator.fridge_inventory.containsKey(targetFood)) {
                CookingViews.NoyInRefrigerator();
                return;
            }


            int quantity = Refrigerator.fridge_inventory.get(targetFood);
            Refrigerator.fridge_inventory.remove(targetFood);
            player_inventory.put(targetFood, player_inventory.getOrDefault(targetFood, 0) + quantity);

        } else {

        }
    }

    public static Food findFoodByName(String name) {
        for (Food food : Cooking.foods){
            if (food.name.equalsIgnoreCase(name)) {
                return food;
            }
        }
        return null;
    }


    public static void showLearnedRecipes() {
        for (Food food : Cooking.foods) {
            if (hasLearnedRecipe(food)) {
                System.out.println(food.name);
            }
        }
    }

    public static boolean hasLearnedRecipe(Food food) {
        switch (food.source.toLowerCase()) {
            case "starter":
                return Cooking.starter;
            case "stardrop saloon":
                return Cooking.stardrop_saloon;
            case "leah reward":
                return Cooking.leah_reward;
            case "fishing level":
                return Cooking.fishing_level >= food.source_level;
            case "foraging level":
                return Cooking.foraging_level >= food.source_level;
            case "farming level":
                return Cooking.farming_level >= food.source_level;
            case "mining level":
                return Cooking.mining_level >= food.source_level;
            default:
                return false;
        }
    }





}
