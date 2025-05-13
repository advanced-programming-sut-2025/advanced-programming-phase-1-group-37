package main.weather;
import java.util.Random;

public class Weather {

    public static String today_weather;
    public static String tomorrow_weather;
    public static String season;
    public static int hour;
    public static int consuming_energy_amount;
    public static int today_consuming_energy_amount;

    public static int days_passed=0;


    static Random rand=new Random(4);

    public static void changeWeather(){

        today_weather=tomorrow_weather;

        int number;

        do {

            number=rand.nextInt();

        }

        while(!checkWeather(number));


        tomorrow_weather=selectWeather(number);

        days_passed++;

        if(days_passed>28){
            changeSeason();
        }

        switch(today_weather){
            case "sunny":{

                break;
            }
            case "rain":{
            RainWeather.Irrigation();
            today_consuming_energy_amount=(consuming_energy_amount*3/2);
                break;
            }
            case "storm":{
            StormWeather.Thor();
                break;
            }
            case "snow":{
            today_consuming_energy_amount=(consuming_energy_amount*2);
                break;
            }

        }

    }


    public static void changeSeason(){

        switch(season){
            case "spring":{
                season="summer";
                break;
            }
            case "summer":{
                season="fall";
                break;
            }
            case "fall":{
                season="winter";
                break;
            }
            case "winter":{
                season="spring";
                break;
            }

        }



    }


    public static String selectWeather(int number){
        switch(number){
            case 0:{
                return "sunny";
            }
            case 1:{
                return "rain";
            }
            case 2:{
                return "storm";
            }
            case 3:{
                return "snow";
            }

        }

        return "sunny";

    }


    public static boolean checkWeather(int number){

        if(season.equals("spring")){
            if(number==3){
                return false;
            }
            else {
                return true;
            }
        }

        else if(season.equals("summer")){
            if(number==3){
                return false;
            }
            else {
                return true;
            }
        }

        else if(season.equals("fall")){
            if(number==3){
                return false;
            }
            else {
                return true;
            }
        }

        else if(season.equals("winter")){
            if((number==1)||(number==2)){
                return false;
            }
            else {
                return true;
            }
        }

        return false;

    }







}
