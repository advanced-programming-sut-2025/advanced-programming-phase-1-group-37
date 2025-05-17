package main.cooking;

import main.construction.CraftingController;
import main.construction.CraftingViews;

import java.util.ArrayList;

import static main.cooking.Cooking.foods;
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
                CookingViews.NotInRefrigerator();
                return;
            }


            int quantity = Refrigerator.fridge_inventory.get(targetFood);
            Refrigerator.fridge_inventory.remove(targetFood);
            player_inventory.put(targetFood, player_inventory.getOrDefault(targetFood, 0) + quantity);

        } else {

        }
    }

    public static Food findFoodByName(String name) {
        for (Food food : foods){
            if (food.name.equalsIgnoreCase(name)) {
                return food;
            }
        }
        return null;
    }


    public static void showLearnedRecipes() {
        for (Food food : foods) {
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


    public static void prepareFood(String recipeName) {

        Food food = null;
        for (Food f : foods) {
            if (f.name.equalsIgnoreCase(recipeName)) {
                food = f;
                break;
            }
        }

        if (food == null) {
            CraftingViews.InvalidItemName();
            return;
        }


        if (!hasLearnedRecipe(food)) {
            CookingViews.NotLearnedRecipe();
            return;
        }


        if (!hasAllIngredients(food)) {
            CraftingViews.NotEnoughResources();
            return;
        }


        if (isInventoryFull()) {
            CraftingViews.IsInventoryFull();
            return;
        }


        consumeIngredients(food);


        player_inventory.put(food, player_inventory.getOrDefault(food, 0) + 1);


        Cooking.player_energy -= 3;


    }


    public static boolean hasAllIngredients(Food food) {
        for (String ingredientName : food.ingredients.keySet()) {
            int requiredAmount = food.ingredients.get(ingredientName);
            int available = getAvailableAmount(ingredientName);
            if (available < requiredAmount)
                return false;
        }
        return true;
    }


    public static int getAvailableAmount(String name) {
        int total = 0;

        for (Food f : player_inventory.keySet()) {
            if (f.name.equalsIgnoreCase(name))
                total += player_inventory.get(f);
        }
        for (Food f : Refrigerator.fridge_inventory.keySet()) {
            if (f.name.equalsIgnoreCase(name))
                total += Refrigerator.fridge_inventory.get(f);
        }

        return total;
    }


    public static void consumeIngredients(Food food) {
        for (String name : food.ingredients.keySet()) {
            int required = food.ingredients.get(name);


            for (Food f : new ArrayList<>(player_inventory.keySet())) {
                if (f.name.equalsIgnoreCase(name) && required > 0) {
                    int available = player_inventory.get(f);
                    int toRemove = Math.min(available, required);
                    required -= toRemove;
                    if (toRemove == available)
                        player_inventory.remove(f);
                    else
                        player_inventory.put(f, available - toRemove);
                }
            }


            for (Food f : new ArrayList<>(Refrigerator.fridge_inventory.keySet())) {
                if (f.name.equalsIgnoreCase(name) && required > 0) {
                    int available = Refrigerator.fridge_inventory.get(f);
                    int toRemove = Math.min(available, required);
                    required -= toRemove;
                    if (toRemove == available)
                        Refrigerator.fridge_inventory.remove(f);
                    else
                        Refrigerator.fridge_inventory.put(f, available - toRemove);
                }
            }
        }
    }


    public static boolean isInventoryFull() {

    if(player_inventory.size() >= Cooking.MAX_INVENTORY_SIZE){
        return true;
    }
    else {
        return false;
    }

    }


    public static void eat(String foodName) {
        Food foodToEat = null;


        for (Food food : Cooking.foods) {
            if (food.name.equalsIgnoreCase(foodName)) {
                foodToEat = food;
                break;
            }
        }

        if (foodToEat == null) {
            CraftingViews.InvalidItemName();
            return;
        }


        int count = Cooking.player_inventory.getOrDefault(foodToEat, 0);
        if (count == 0) {
            CraftingViews.NotInInventory();
            return;
        }


        if (count == 1)
            Cooking.player_inventory.remove(foodToEat);
        else
            Cooking.player_inventory.put(foodToEat, count - 1);


        Cooking.energy += foodToEat.energy;
        if (Cooking.energy > Cooking.maxEnergy)
            Cooking.energy = Cooking.maxEnergy;


        if (!foodToEat.buff.equalsIgnoreCase("none")) {
            Cooking.activeBuffFood = foodToEat;
            Cooking.buffHoursRemaining = 5;
        }


    }


    public static int getEnergyCost(String skill) {
        if (Cooking.activeBuffFood != null && Cooking.buffHoursRemaining > 0) {
            if (Cooking.activeBuffFood.buff.equalsIgnoreCase(skill)) {
                return 1; // مثلاً در حالت عادی انرژی 2 است ولی با buff فقط 1 انرژی مصرف می‌شود
            }
        }
        return 2; // مصرف انرژی معمول
    }

    public static void tickBuffHour() {
        if (Cooking.buffHoursRemaining > 0) {
            Cooking.buffHoursRemaining--;

            if (Cooking.buffHoursRemaining == 0) {
                Cooking.activeBuffFood = null;
            }
        }
    }








}
