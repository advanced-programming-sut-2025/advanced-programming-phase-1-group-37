package main.cooking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cooking {

    public static List<Food> foods=new ArrayList<>();

    public static Map<Food,Integer> player_inventory;

    public static int energy = 100;
    public static int maxEnergy = 100;
    public static Food activeBuffFood = null;
    public static int buffHoursRemaining = 0;


    public static boolean starter;
    public static boolean stardrop_saloon;
    public static boolean leah_reward;
    public static int fishing_level;
    public static int foraging_level;
    public static int farming_level;
    public static int mining_level;


    public static int player_energy;

    public static int MAX_INVENTORY_SIZE;





    public static void ListFoods(){
        Map<String, Integer> friedEggIngredients = new HashMap<>();
        friedEggIngredients.put("egg", 1);
        foods.add(new Food("Fried Egg", friedEggIngredients, 50, null, "Starter", 0, 35));


        Map<String, Integer> bakedFishIngredients = new HashMap<>();
        bakedFishIngredients.put("Sardine", 1);
        bakedFishIngredients.put("Salmon", 1);
        bakedFishIngredients.put("Wheat", 1);
        foods.add(new Food("Baked Fish", bakedFishIngredients, 75, null, "Starter", 0, 100));


        Map<String, Integer> saladIngredients = new HashMap<>();
        saladIngredients.put("Leek", 1);
        saladIngredients.put("Dandelion", 1);
        foods.add(new Food("Salad", saladIngredients, 113, null, "Starter", 0, 110));


        Map<String, Integer> omeletIngredients = new HashMap<>();
        omeletIngredients.put("Egg", 1);
        omeletIngredients.put("Milk", 1);
        foods.add(new Food("Omelet", omeletIngredients, 100, null, "Stardrop Saloon", 0, 125));


        Map<String, Integer> pumpkinPieIngredients = new HashMap<>();
        pumpkinPieIngredients.put("Pumpkin", 1);
        pumpkinPieIngredients.put("Wheat Flour", 1);
        pumpkinPieIngredients.put("Milk", 1);
        pumpkinPieIngredients.put("Sugar", 1);
        foods.add(new Food("Pumpkin Pie", pumpkinPieIngredients, 225, null, "Stardrop Saloon", 0, 385));


        Map<String, Integer> spaghettiIngredients = new HashMap<>();
        spaghettiIngredients.put("Wheat Flour", 1);
        spaghettiIngredients.put("Tomato", 1);
        foods.add(new Food("Spaghetti", spaghettiIngredients, 75, null, "Stardrop Saloon", 0, 120));

        // Pizza
        Map<String, Integer> pizzaIngredients = new HashMap<>();
        pizzaIngredients.put("Wheat Flour", 1);
        pizzaIngredients.put("Tomato", 1);
        pizzaIngredients.put("Cheese", 1);
        foods.add(new Food("Pizza", pizzaIngredients, 150, null, "Stardrop Saloon", 0, 300));

        // Tortilla
        Map<String, Integer> tortillaIngredients = new HashMap<>();
        tortillaIngredients.put("Corn", 1);
        foods.add(new Food("Tortilla", tortillaIngredients, 50, null, "Stardrop Saloon", 0, 50));


        Map<String, Integer> makiRollIngredients = new HashMap<>();
        makiRollIngredients.put("Any Fish", 1);
        makiRollIngredients.put("Rice", 1);
        makiRollIngredients.put("Fiber", 1);
        foods.add(new Food("Maki Roll", makiRollIngredients, 100, null, "Stardrop Saloon", 0, 220));


        Map<String, Integer> espressoIngredients = new HashMap<>();
        espressoIngredients.put("Coffee", 3);
        foods.add(new Food("Triple Shot Espresso", espressoIngredients, 200, "Max Energy +100 (5 hours)", "Stardrop Saloon", 0, 450));


        Map<String, Integer> cookieIngredients = new HashMap<>();
        cookieIngredients.put("Wheat Flour", 1);
        cookieIngredients.put("Sugar", 1);
        cookieIngredients.put("Egg", 1);
        foods.add(new Food("Cookie", cookieIngredients, 90, null, "Stardrop Saloon", 0, 140));


        Map<String, Integer> hashBrownsIngredients = new HashMap<>();
        hashBrownsIngredients.put("Potato", 1);
        hashBrownsIngredients.put("Oil", 1);
        foods.add(new Food("Hash Browns", hashBrownsIngredients, 90, "Farming (5 hours)", "Stardrop Saloon", 0, 120));


        Map<String, Integer> pancakesIngredients = new HashMap<>();
        pancakesIngredients.put("Wheat Flour", 1);
        pancakesIngredients.put("Egg", 1);
        foods.add(new Food("Pancakes", pancakesIngredients, 90, "Foraging (11 hours)", "Stardrop Saloon", 0, 80));


        Map<String, Integer> fruitSaladIngredients = new HashMap<>();
        fruitSaladIngredients.put("Blueberry", 1);
        fruitSaladIngredients.put("Melon", 1);
        fruitSaladIngredients.put("Apricot", 1);
        foods.add(new Food("Fruit Salad", fruitSaladIngredients, 263, null, "Stardrop Saloon", 0, 450));


        Map<String, Integer> redPlateIngredients = new HashMap<>();
        redPlateIngredients.put("Red Cabbage", 1);
        redPlateIngredients.put("Radish", 1);
        foods.add(new Food("Red Plate", redPlateIngredients, 240, "Max Energy +50 (3 hours)", "Stardrop Saloon", 0, 400));


        Map<String, Integer> breadIngredients = new HashMap<>();
        breadIngredients.put("Wheat Flour", 1);
        foods.add(new Food("Bread", breadIngredients, 50, null, "Stardrop Saloon", 0, 60));


        Map<String, Integer> salmonDinnerIngredients = new HashMap<>();
        salmonDinnerIngredients.put("Salmon", 1);
        salmonDinnerIngredients.put("Amaranth", 1);
        salmonDinnerIngredients.put("Kale", 1);
        foods.add(new Food("Salmon Dinner", salmonDinnerIngredients, 125, null, "Leah reward", 0, 300));


        Map<String, Integer> vegetableMedleyIngredients = new HashMap<>();
        vegetableMedleyIngredients.put("Tomato", 1);
        vegetableMedleyIngredients.put("Beet", 1);
        foods.add(new Food("Vegetable Medley", vegetableMedleyIngredients, 165, null, "Foraging", 2, 120));


        Map<String, Integer> farmersLunchIngredients = new HashMap<>();
        farmersLunchIngredients.put("Omelet", 1);
        farmersLunchIngredients.put("Parsnip", 1);
        foods.add(new Food("Farmer's Lunch", farmersLunchIngredients, 200, "Farming (5 hours)", "Farming", 1, 150));


        Map<String, Integer> survivalBurgerIngredients = new HashMap<>();
        survivalBurgerIngredients.put("Bread", 1);
        survivalBurgerIngredients.put("Carrot", 1);
        survivalBurgerIngredients.put("Eggplant", 1);
        foods.add(new Food("Survival Burger", survivalBurgerIngredients, 125, "Foraging (5 hours)", "Foraging", 3, 180));


        Map<String, Integer> dishOSeaIngredients = new HashMap<>();
        dishOSeaIngredients.put("Sardine", 2);
        dishOSeaIngredients.put("Hash Browns", 1);
        foods.add(new Food("Dish O' the Sea", dishOSeaIngredients, 150, "Fishing (5 hours)", "Fishing", 2, 220));


        Map<String, Integer> seafoamPuddingIngredients = new HashMap<>();
        seafoamPuddingIngredients.put("Flounder", 1);
        seafoamPuddingIngredients.put("Midnight Carp", 1);
        foods.add(new Food("Seafoam Pudding", seafoamPuddingIngredients, 175, "Fishing (10 hours)", "Fishing", 3, 300));


        Map<String, Integer> minersTreatIngredients = new HashMap<>();
        minersTreatIngredients.put("Carrot", 2);
        minersTreatIngredients.put("Sugar", 1);
        minersTreatIngredients.put("Milk", 1);
        foods.add(new Food("Miner's Treat", minersTreatIngredients, 125, "Mining (5 hours)", "Mining", 1, 200));
    }


}
