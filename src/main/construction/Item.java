package main.construction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Item {
    public String name;
    public Map<String, Integer> required_materials;
    public String source;
    public int source_level;
    public int sell_price;

    public Item(String name, Map<String, Integer> required_materials, String source, int source_level, int sell_price) {
        this.name = name;
        this.required_materials = required_materials;
        this.source = source;
        this.source_level = source_level;
        this.sell_price = sell_price;
    }
}
