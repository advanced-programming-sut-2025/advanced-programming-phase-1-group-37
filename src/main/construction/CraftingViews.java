package main.construction;

public class CraftingViews {

    public static void printItem(String item_name){

        System.out.println(item_name);

    }

    public static void NotPlayerInHouse(){

        System.out.println("You are not in house");

    }

    public static void InvalidItemName(){

        System.out.println("This Item doesn't exist");

    }

    public static void IsInventoryFull(){

        System.out.println("The inventory is full");

    }

    public static void NotEnoughResources(){

        System.out.println("You don't have enough resources");

    }

    public static void NotInInventory(){

        System.out.println("This item doesn't exist in inventory");

    }

    public static void InvalidDirection(){

        System.out.println("Invalid direction");

    }



}
