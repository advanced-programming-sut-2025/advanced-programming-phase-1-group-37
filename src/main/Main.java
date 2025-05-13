package main;
import main.construction.Construction;
import main.construction.Crafting;
import main.construction.CraftingController;
import main.construction.CraftingViews;
import main.cooking.Cooking;
import main.cooking.CookingController;
import main.weather.Greenhouse;
import main.weather.StormWeather;
import main.weather.Weather;
import main.weather.WeatherViews;

import java.util.Scanner;
import java.util.regex.*;


public class Main {



    public static void main(String[] args){

        Scanner input=new Scanner(System.in);

        Construction.ListItems();
        Cooking.ListFoods();

        while(true){

            String command=input.nextLine().trim();

            if(Weather.hour==22){
                Weather.changeWeather();
            }

            if(command.equals("weather")){
                WeatherViews.Weather();
            }

            else if(command.equals("weather forecast")){
                WeatherViews.WeatherForecast();
            }

            else if(command.matches("cheat Thor -l\\s+\\d+\\s+\\d+")){
                String[] parts=command.split("\\s+");
                int x=Integer.parseInt(parts[3]);
                int y=Integer.parseInt(parts[4]);
                StormWeather.cheatThor(x,y);
            }

            else if(command.matches("cheat weather set (\\S+)")){
                String[] parts=command.split("\\s+");
                Weather.tomorrow_weather=parts[3];
            }

            else if(command.equals("greenhouse build")){
                Greenhouse greenhouse=new Greenhouse();
                Greenhouse.Build();
            }

            else if(command.equals("crafting show recipes")){
                if(CraftingController.IsPlayerInHouse()){
                    CraftingController.showRecipes();
                }
                else {
                    CraftingViews.NotPlayerInHouse();
                }
            }

            else if(command.matches("crafting craft (\\S+)")){

                String[] parts=command.split("\\s+");

                String item_name=parts[2];
                if(CraftingController.IsPlayerInHouse()){
                    Crafting.craftItem(item_name);
                }
                else {
                    CraftingViews.NotPlayerInHouse();
                }

            }

            else if(command.matches("place item -n (\\S+) -d (\\S+)")){

                String[] parts=command.split("\\s+");

                String item_name=parts[2];
                String direction=parts[3];
                if(CraftingController.IsPlayerInHouse()){
                    Construction.placeItem(item_name,direction);
                }
                else {
                    CraftingViews.NotPlayerInHouse();
                }

            }


            else if(command.matches("cheat add item -n (\\S+) -c \\d+")){

                String[] parts=command.split("\\s+");

                String item_name=parts[4];

                int count=Integer.parseInt(parts[6]);

                if(CraftingController.IsPlayerInHouse()){
                    Crafting.CheatAddItem(item_name,count);
                }
                else {
                    CraftingViews.NotPlayerInHouse();
                }


            }


            else if(command.matches("cooking refrigerator (\\S+) (\\S+)")){

                String[] parts=command.split("\\s+");

                String action=parts[2];

                String item_name=parts[3];

                if(CraftingController.IsPlayerInHouse()){
                    CookingController.Refrigerator(action,item_name);
                }
                else {
                    CraftingViews.NotPlayerInHouse();
                }


            }


            else if(command.equals("cooking show recipes")){

                if(CraftingController.IsPlayerInHouse()){
                    CookingController.showLearnedRecipes();
                }
                else {
                    CraftingViews.NotPlayerInHouse();
                }

            }


            else if(command.matches("eat (\\S+)")){

                String[] parts=command.split("\\s+");

                String food_name=parts[1];


                if(CraftingController.IsPlayerInHouse()){
                    CookingController.eat(food_name);
                }
                else {
                    CraftingViews.NotPlayerInHouse();
                }


            }







        }


    }




}
