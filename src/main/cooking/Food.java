package main.cooking;

import java.util.Map;

public class Food {
    public String name;
    public Map<String, Integer> ingredients;
    public int energy;
    public String buff;
    public String source;
    public int source_level;
    public int sellPrice;

    public Food(String name, Map<String, Integer> ingredients, int energy, String buff, String source, int source_level, int sellPrice) {
        this.name = name;
        this.ingredients = ingredients;
        this.energy = energy;
        this.buff = buff;
        this.source = source;
        this.source_level = source_level;
        this.sellPrice = sellPrice;
    }
}
