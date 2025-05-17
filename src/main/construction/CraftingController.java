package main.construction;

public class CraftingController {


    public static boolean IsPlayerInHouse(){

        if((Crafting.player_x>Crafting.house_x)
                &&(Crafting.player_x<(Crafting.house_x+Crafting.house_length))
                &&(Crafting.player_y>Crafting.house_y)
                &&(Crafting.player_y<(Crafting.house_y+Crafting.house_width))){

            return true;
        }
    else {
            return false;
        }

    }


    public static void showRecipes(){

        for(Item item:Construction.items){

            if(isLearnedItem(item)){
                CraftingViews.printItem(item.name);
            }

        }




    }



    public static boolean isLearnedItem(Item item){


        if(item.source.equals("-")){
            return true;
        }

        else if(item.source.equals("Pierre's General Store")){

            if(Crafting.Pierres_General_Store){

                return true;
            }
            else {
                return false;
            }

        }

        else if(item.source.equals("Fish Shop")){

            if(Crafting.Fish_Shop){

                return true;
            }
            else {
                return false;
            }

        }

        else if(item.source.equals("Mining")){

            if(Crafting.mining_level>=item.source_level){

                return true;
            }
            else {
                return false;
            }

        }

        else if(item.source.equals("Farming")){

            if(Crafting.farming_level>=item.source_level){

                return true;
            }
            else {
                return false;
            }

        }

        else if(item.source.equals("Foraging")){

            if(Crafting.foraging_level>=item.source_level){

                return true;
            }
            else {
                return false;
            }

        }


        return false;


    }




}
