package main;
import main.construction.Construction;
import main.construction.CraftingController;
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
                CraftingController craftingController=new CraftingController();
                craftingController.showRecipes();
            }


        }


    }




}
