package main.weather;

public class Greenhouse{

    public static boolean isBuilt=false;
    public static int player_coin=0;
    public static int player_wood=0;



    public static void build() {

        if ((player_coin > 1000) && (player_wood > 500)) {
            player_coin -= 1000;
            player_wood -= 500;
            isBuilt = true;
            return;
        } else {
            WeatherViews.notEnoughsources();
            return;
        }

    }


    }
