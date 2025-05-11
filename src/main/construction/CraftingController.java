package main.construction;

public class CraftingController {


    public static void showRecipes(){

        for(Item item:Construction.items){

            if(isLearnedItem(item)){
                CraftingViews.printItem(item.name);
            }

        }




    }



    public static boolean isLearnedItem(Item item){



    }




}
