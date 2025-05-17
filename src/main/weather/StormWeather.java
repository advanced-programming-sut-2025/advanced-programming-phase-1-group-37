package main.weather;
import java.util.Random;


public class StormWeather {

    public static int Map_width;
    public static int Map_height;
    public static int farms_count;
    public static char[][][] map=new char[farms_count][Map_width][Map_height];


    static Random rand=new Random(farms_count);
    static Random rand2=new Random(Map_width);
    static Random rand3=new Random(Map_height);


    public static void Thor(){

        int[] num=new int[3];
        int[] num2=new int[3];

        do {
            num[0]=rand.nextInt();
            num[1]=rand.nextInt();
            num[2]=rand.nextInt();
        }
        while((num[0]==num[1])||(num[0]==num[2])||(num[1]==num[2]));

        do {
            num2[0]=rand.nextInt();
            num2[1]=rand.nextInt();
            num2[2]=rand.nextInt();
        }
        while((num2[0]==num2[1])||(num2[0]==num2[2])||(num2[1]==num2[2]));

        for(int i=0;i<farms_count;i++){
            for(int j=0;j<3;j++){
                map[i][num[j]][num2[j]]='F';
            }
        }



    }


    public static void cheatThor(int x,int y){

        map[0][x][y]='F';

    }





}
