package main.weather;

public class WeatherViews {


    public static void notEnoughsources(){


        System.out.println("You dont have enough sources");

    }


    public static void Weather(){

        System.out.println(Weather.today_weather);

    }

    public static void WeatherForecast(){

        System.out.println(Weather.tomorrow_weather);

    }


}
