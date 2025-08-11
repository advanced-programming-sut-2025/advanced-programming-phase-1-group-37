package com.mygame.stardew;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

// =================================================================================
// بخش ۱: کلاس‌های مدل گرافیکی و منطق نقشه
// =================================================================================

enum TileType { GRASS, HOUSE_FLOOR, HOUSE_WALL }
enum MapElementType { TREE, ROCK, PLACED_ITEM }

class MapElement {
    protected Rectangle bounds;
    protected MapElementType type;
    public MapElement(float x, float y, float width, float height, MapElementType type) { this.bounds = new Rectangle(x, y, width, height); this.type = type; }
    public Rectangle getBounds() { return bounds; }
    public MapElementType getType() { return type; }
}

class Tree extends MapElement { public Tree(float x, float y) { super(x, y, Main.TILE_SIZE, Main.TILE_SIZE, MapElementType.TREE); } }
class Rock extends MapElement { public Rock(float x, float y) { super(x, y, Main.TILE_SIZE, Main.TILE_SIZE, MapElementType.ROCK); } }

class PlacedItemElement extends MapElement {
    private GameLogic.Item item;
    public PlacedItemElement(GameLogic.Item item, float x, float y) { super(x, y, Main.TILE_SIZE, Main.TILE_SIZE, MapElementType.PLACED_ITEM); this.item = item; }
    public GameLogic.Item getItem() { return item; }
}

class Tile {
    private TileType type;
    public Tile(TileType type) { this.type = type; }
    public TileType getType() { return type; }
    public boolean isWalkable() { return (type != TileType.HOUSE_WALL); }
}

class GameMap {
    private Tile[][] tiles;
    private List<MapElement> elements;
    private int width, height;
    private Rectangle houseBounds;

    public GameMap(int width, int height) {
        this.width = width; this.height = height;
        this.tiles = new Tile[width][height];
        this.elements = new ArrayList<>();
        initialize();
    }

    private void initialize() {
        for (int x = 0; x < width; x++) for (int y = 0; y < height; y++) tiles[x][y] = new Tile(TileType.GRASS);
        int hX = 15, hY = 10, hW = 8, hH = 6;
        houseBounds = new Rectangle(hX, hY, hW, hH);
        for (int x = hX; x < hX + hW; x++) {
            for (int y = hY; y < hY + hH; y++) {
                if (x == hX || x == hX + hW - 1 || y == hY || y == hY + hH - 1) tiles[x][y] = new Tile(TileType.HOUSE_WALL);
                else tiles[x][y] = new Tile(TileType.HOUSE_FLOOR);
            }
        }
        tiles[hX + hW / 2][hY] = new Tile(TileType.HOUSE_FLOOR);
        Random random = new Random();
        for (int i = 0; i < 40; i++) {
            int rX = random.nextInt(width), rY = random.nextInt(height);
            if (tiles[rX][rY].isWalkable() && !houseBounds.contains(rX, rY)) {
                elements.add(random.nextBoolean() ? new Tree(rX * Main.TILE_SIZE, rY * Main.TILE_SIZE) : new Rock(rX * Main.TILE_SIZE, rY * Main.TILE_SIZE));
            }
        }
    }

    public Tile getTile(int x, int y) { if (x >= 0 && x < width && y >= 0 && y < height) return tiles[x][y]; return null; }
    public boolean isCollision(Rectangle rect) {
        if (rect.x < 0 || rect.y < 0 || rect.x + rect.width > width * Main.TILE_SIZE || rect.y + rect.height > height * Main.TILE_SIZE) {
            return true;
        }
        int startX = (int) (rect.x / Main.TILE_SIZE), startY = (int) (rect.y / Main.TILE_SIZE);
        int endX = (int) ((rect.x + rect.width) / Main.TILE_SIZE), endY = (int) ((rect.y + rect.height) / Main.TILE_SIZE);
        for (int x = startX; x <= endX; x++) for (int y = startY; y <= endY; y++) {
            Tile tile = getTile(x, y);
            if (tile != null && !tile.isWalkable()) return true;
        }
        for (MapElement element : elements) if (element.getBounds().overlaps(rect)) return true;
        return false;
    }
    public void addElement(MapElement element) { elements.add(element); }
    public int getWidth() { return width; } public int getHeight() { return height; } public List<MapElement> getElements() { return elements; }
}

class Player {
    private Rectangle bounds;
    private float speed = 0.8f;

    public Player(float x, float y) { this.bounds = new Rectangle(x, y, Main.TILE_SIZE * 0.8f, Main.TILE_SIZE * 0.5f); }

    public void move(float dx, float dy, GameMap map) {
        // حرکت در محور X و بررسی برخورد
        bounds.x += dx;
        if (map.isCollision(bounds)) {
            bounds.x -= dx; // بازگشت در صورت برخورد
        }
        // حرکت در محور Y و بررسی برخورد
        bounds.y += dy;
        if (map.isCollision(bounds)) {
            bounds.y -= dy; // بازگشت در صورت برخورد
        }
    }
    public Vector2 getPosition() { return new Vector2(bounds.x, bounds.y); }
    public Rectangle getBounds() { return bounds; }
    public float getSpeed() { return speed; }
}

// =================================================================================
// بخش ۲: کلاس اصلی بازی با LibGDX
// =================================================================================

public class Main extends ApplicationAdapter {
    public static final int TILE_SIZE = 16;
    public static final int MAP_WIDTH_TILES = 40;
    public static final int MAP_HEIGHT_TILES = 30;

    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Stage stage;
    private Skin skin;
    private Map<String, Texture> textures = new HashMap<>();
    private Texture playerTexture;

    private GameMap gameMap;
    private Player player;
    private GameLogic gameLogic;

    private Window craftingWindow;
    private Window inventoryWindow;
    private Table consoleTable;
    private ScrollPane consoleScrollPane;
    private TextField commandInput;

    @Override
    public void create() {
        batch = new SpriteBatch();
        float worldWidth = MAP_WIDTH_TILES * TILE_SIZE;
        float worldHeight = MAP_HEIGHT_TILES * TILE_SIZE;
        camera = new OrthographicCamera();
        viewport = new FitViewport(worldWidth / 2, worldHeight / 2, camera);
        stage = new Stage(new ScreenViewport());

        loadAssets();
        setupSkin();

        gameMap = new GameMap(MAP_WIDTH_TILES, MAP_HEIGHT_TILES);
        player = new Player(worldWidth / 2f, worldHeight / 2f);
        gameLogic = new GameLogic(gameMap);

        setupUI();
        Gdx.input.setInputProcessor(stage);
    }

    private void loadAssets() {
        try {
            playerTexture = new Texture(Gdx.files.internal("farmer.png"));
            textures.put("grass", new Texture(Gdx.files.internal("grass.png")));
            textures.put("rock", new Texture(Gdx.files.internal("rock.png")));
            textures.put("tree", new Texture(Gdx.files.internal("tree.png")));
            textures.put("house_floor", new Texture(Gdx.files.internal("house_floor.png")));
            textures.put("house_wall", new Texture(Gdx.files.internal("house_wall.png")));

            textures.put("Furnace", new Texture(Gdx.files.internal("furnace.png")));
            textures.put("Scarecrow", new Texture(Gdx.files.internal("scarecrow.png")));
            textures.put("Bee House", new Texture(Gdx.files.internal("bee_house.png")));
            textures.put("Keg", new Texture(Gdx.files.internal("keg.png")));

        } catch (Exception e) {
            Gdx.app.error("AssetLoader", "Error loading assets! Make sure all PNG files are in the assets folder.", e);
            Gdx.app.exit();
        }
    }

    private void setupSkin() {
        skin = new Skin();
        BitmapFont font = new BitmapFont();
        skin.add("default-font", font, BitmapFont.class);
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE); pixmap.fill();
        skin.add("white", new Texture(pixmap));
        pixmap.dispose();

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = font;
        textButtonStyle.up = skin.newDrawable("white", new Color(0.4f, 0.4f, 0.4f, 1));
        textButtonStyle.down = skin.newDrawable("white", new Color(0.2f, 0.2f, 0.2f, 1));
        textButtonStyle.over = skin.newDrawable("white", new Color(0.6f, 0.6f, 0.6f, 1));
        skin.add("default", textButtonStyle);

        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle(font, Color.WHITE,
            skin.newDrawable("white", Color.GRAY), skin.newDrawable("white", Color.DARK_GRAY), skin.newDrawable("white", Color.LIGHT_GRAY));
        skin.add("default", textFieldStyle);

        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        skin.add("default", labelStyle);

        Window.WindowStyle windowStyle = new Window.WindowStyle(font, Color.WHITE, skin.newDrawable("white", new Color(0.1f, 0.1f, 0.1f, 0.85f)));
        skin.add("default", windowStyle);

        ScrollPane.ScrollPaneStyle scrollPaneStyle = new ScrollPane.ScrollPaneStyle();
        scrollPaneStyle.background = skin.newDrawable("white", new Color(0.05f, 0.05f, 0.05f, 0.8f));
        skin.add("default", scrollPaneStyle);
    }

    private void setupUI() {
        craftingWindow = new Window("Crafting Command Console (C)", skin);
        craftingWindow.setSize(600, 400);
        craftingWindow.setPosition(50, 50);
        craftingWindow.setVisible(false);

        consoleTable = new Table(skin);
        consoleTable.align(Align.topLeft);
        consoleScrollPane = new ScrollPane(consoleTable, skin);
        consoleScrollPane.setFadeScrollBars(false);

        commandInput = new TextField("", skin);
        commandInput.setName("commandInput");
        TextButton submitButton = new TextButton("Execute", skin);
        submitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) { processCommand(); }
        });
        commandInput.setTextFieldListener((textField, c) -> {
            if (c == '\n' || c == '\r') { processCommand(); }
        });

        Table inputTable = new Table();
        inputTable.add(commandInput).expandX().fillX().pad(5);
        inputTable.add(submitButton).pad(5);

        craftingWindow.add(consoleScrollPane).expand().fill().pad(5).row();
        craftingWindow.add(inputTable).expandX().fillX().pad(5);
        stage.addActor(craftingWindow);

        inventoryWindow = new Window("Inventory (I)", skin);
        inventoryWindow.setSize(400, 400);
        inventoryWindow.setPosition(Gdx.graphics.getWidth() - 450, 50);
        inventoryWindow.setVisible(false);
        stage.addActor(inventoryWindow);
    }

    private void processCommand() {
        String command = commandInput.getText();
        if (!command.trim().isEmpty()) {
            gameLogic.parseAndExecuteCommand(command, player);
            updateConsole();
            commandInput.setText("");
        }
    }

    @Override
    public void render() {
        // منطق بازی و ورودی
        handleInput(Gdx.graphics.getDeltaTime());

        // به‌روزرسانی دوربین
        camera.position.set(player.getPosition().x, player.getPosition().y, 0);
        camera.update();

        // پاک کردن صفحه
        Gdx.gl.glClearColor(0.1f, 0.4f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // رندر کردن دنیای بازی
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawMap();
        batch.draw(playerTexture, player.getPosition().x, player.getPosition().y, player.getBounds().width, player.getBounds().height * 2);
        batch.end();

        // رندر کردن UI
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    private void handleInput(float deltaTime) {
        boolean uiVisible = craftingWindow.isVisible() || inventoryWindow.isVisible();
        if (uiVisible) {
            Gdx.input.setInputProcessor(stage);
        } else {
            Gdx.input.setInputProcessor(null);
            float moveAmount = player.getSpeed();
            float dx = 0, dy = 0;
            if (Gdx.input.isKeyPressed(Input.Keys.W)) dy += moveAmount;
            if (Gdx.input.isKeyPressed(Input.Keys.S)) dy -= moveAmount;
            if (Gdx.input.isKeyPressed(Input.Keys.A)) dx -= moveAmount;
            if (Gdx.input.isKeyPressed(Input.Keys.D)) dx += moveAmount;
            if (dx != 0 || dy != 0) player.move(dx, dy, gameMap);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            gameLogic.player_x = (int) (player.getPosition().x / TILE_SIZE);
            gameLogic.player_y = (int) (player.getPosition().y / TILE_SIZE);
            if (gameLogic.IsPlayerInHouse()) {
                craftingWindow.setVisible(!craftingWindow.isVisible());
                if(craftingWindow.isVisible()) stage.setKeyboardFocus(commandInput);
            } else {
                gameLogic.NotPlayerInHouse();
                updateConsole();
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            inventoryWindow.setVisible(!inventoryWindow.isVisible());
            if (inventoryWindow.isVisible()) updateInventoryWindow();
        }
    }

    private void drawMap() {
        for (int x = 0; x < gameMap.getWidth(); x++) {
            for (int y = 0; y < gameMap.getHeight(); y++) {
                Tile tile = gameMap.getTile(x, y);
                Texture texture;
                switch (tile.getType()) {
                    case HOUSE_FLOOR: texture = textures.get("house_floor"); break;
                    case HOUSE_WALL: texture = textures.get("house_wall"); break;
                    default: texture = textures.get("grass"); break;
                }
                batch.draw(texture, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
        for (MapElement element : gameMap.getElements()) {
            Texture texture = null;
            if (element.getType() == MapElementType.TREE) texture = textures.get("tree");
            else if (element.getType() == MapElementType.ROCK) texture = textures.get("rock");
            else if (element.getType() == MapElementType.PLACED_ITEM) {
                PlacedItemElement p_item = (PlacedItemElement) element;
                texture = textures.get(p_item.getItem().name);
            }
            if(texture != null) batch.draw(texture, element.getBounds().x, element.getBounds().y, element.getBounds().width, element.getBounds().height);
        }
    }

    private void updateConsole() {
        consoleTable.clear();
        for (String msg : gameLogic.getConsoleHistory()) {
            Label msgLabel = new Label(msg, skin);
            msgLabel.setWrap(true); // **مهم: فعال کردن شکستن خط**
            consoleTable.add(msgLabel).left().width(craftingWindow.getWidth() - 40).pad(2).row(); // **مهم: تعیین عرض برای شکستن خط**
        }
        consoleScrollPane.layout();
        consoleScrollPane.setScrollPercentY(100);
    }

    private void updateInventoryWindow() {
        inventoryWindow.clear();
        inventoryWindow.add(new Label("Player Inventory", skin)).padBottom(10).row();
        Table contentTable = new Table(skin);
        if (gameLogic.player_inventory.isEmpty()) contentTable.add(new Label("Inventory is empty.", skin));
        else gameLogic.player_inventory.forEach((item, count) -> contentTable.add(new Label(item.name + " x " + count, skin)).left().row());
        contentTable.row();
        contentTable.add(new Label("--- Resources ---", skin)).padTop(20).colspan(2).row();
        contentTable.add(new Label("Wood: " + gameLogic.player_wood, skin)).left().pad(2);
        contentTable.add(new Label("Stone: " + gameLogic.player_stone, skin)).left().pad(2).row();
        contentTable.add(new Label("Coal: " + gameLogic.player_coal, skin)).left().pad(2);
        contentTable.add(new Label("Fiber: " + gameLogic.player_fiber, skin)).left().pad(2).row();
        contentTable.add(new Label("Copper Ore: " + gameLogic.player_copper_ore, skin)).left().pad(2);
        contentTable.add(new Label("Iron Bar: " + gameLogic.player_iron_bar, skin)).left().pad(2).row();
        contentTable.add(new Label("Energy: " + gameLogic.player_energy, skin)).left().pad(2).row();
        inventoryWindow.add(new ScrollPane(contentTable, skin)).expand().fill().row();
        TextButton backButton = new TextButton("Close", skin);
        backButton.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { inventoryWindow.setVisible(false); }});
        inventoryWindow.add(backButton).padTop(10);
    }

    @Override
    public void resize(int width, int height) { viewport.update(width, height, true); stage.getViewport().update(width, height, true); }
    @Override
    public void dispose() { batch.dispose(); stage.dispose(); skin.dispose(); textures.values().forEach(Texture::dispose); playerTexture.dispose(); }
}

// =================================================================================
// بخش ۳: کلاس منطق بازی یکپارچه
// =================================================================================

class GameLogic {
    public static class Item {
        public String name;
        public Map<String, Integer> required_materials;
        public String source;
        public int source_level;
        public boolean isPlaceable;
        public int sell_price;
        public Item(String n, Map<String, Integer> req, String src, int lvl, int price, boolean placeable) { name = n; required_materials = req; source = src; source_level = lvl; sell_price = price; isPlaceable = placeable; }
        @Override public boolean equals(Object o) { return o instanceof Item && name.equals(((Item) o).name); }
        @Override public int hashCode() { return name.hashCode(); }
    }

    public List<Item> items = new ArrayList<>();
    public Map<Item, Integer> player_inventory;
    public int player_wood, player_stone, player_copper_ore, player_copper_bar, player_coal, player_iron_ore, player_iron_bar, player_gold_ore, player_gold_bar, player_fiber, iridium_ore, iridium_bar, player_acorn, player_maple_seed, player_pine_cone, player_mahogany_seed;
    public int player_x, player_y, house_x, house_y, house_length, house_width;
    public int mining_level, farming_level, foraging_level;
    public boolean Pierres_General_Store, Fish_Shop;
    public int player_energy = 200;
    public final int MAX_INVENTORY_SIZE = 20;
    private List<String> consoleHistory = new ArrayList<>();
    private GameMap gameMap;

    public GameLogic(GameMap map) {
        this.gameMap = map;
        player_inventory = new HashMap<>();
        ListItems();
        player_wood = 100; player_stone = 100; player_copper_ore = 50; player_copper_bar = 0; player_coal = 50; player_iron_ore = 0; player_iron_bar = 20; player_gold_ore = 0; player_gold_bar = 10; player_fiber = 30; iridium_ore = 0; iridium_bar = 0; player_acorn = 0; player_maple_seed = 0; player_pine_cone = 0; player_mahogany_seed = 0;
        mining_level = 1; farming_level = 1; foraging_level = 1;
        Pierres_General_Store = false; Fish_Shop = false;
        house_x = 15; house_y = 10; house_length = 8; house_width = 6;
    }

    public void ListItems() {
        items.add(new Item("Cherry Bomb", Map.of("copper ore", 4, "coal", 1), "Mining", 1, 50, false));
        items.add(new Item("Bomb", Map.of("iron ore", 4, "coal", 1), "Mining", 2, 50, false));
        items.add(new Item("Mega Bomb", Map.of("gold ore", 4, "coal", 1), "Mining", 3, 50, false));
        items.add(new Item("Furnace", Map.of("copper ore", 20, "stone", 25), "-", 0, 0, true));
        items.add(new Item("Scarecrow", Map.of("wood", 50, "coal", 1, "fiber", 20), "-", 0, 0, true));
        items.add(new Item("Bee House", Map.of("wood", 40, "coal", 8, "iron bar", 1), "Farming", 1, 0, true));
        items.add(new Item("Keg", Map.of("wood", 30, "copper bar", 1, "iron bar", 1), "Farming", 3, 0, true));
        items.add(new Item("Mystic Tree Seed", Map.of("acorn", 5, "maple seed", 5, "pine cone", 5, "mahogany seed", 5), "Foraging", 4, 100, false));
    }

    public void parseAndExecuteCommand(String command, Player player) {
        consoleHistory.add("> " + command);
        String[] parts = command.trim().toLowerCase().split("\\s+");
        if (parts.length == 0) return;

        switch (parts[0]) {
            case "crafting":
                if (parts.length > 2 && parts[1].equals("craft")) {
                    String itemName = joinArray(parts, 2);
                    craftItem(itemName);
                } else {
                    addToConsole("Invalid command. Example: crafting craft Bee House");
                }
                break;
            case "place":
                handlePlaceCommand(command, player);
                break;
            case "cheat":
                handleCheatCommand(command);
                break;
            case "show":
                if (parts.length > 1 && parts[1].equals("recipes")) {
                    showRecipes();
                } else {
                    addToConsole("Invalid command. Example: show recipes");
                }
                break;
            case "learn":
                if(parts[1].equals("recipe")&&parts.length > 2) {
                    String itemName = joinArray(parts, 2);
                    learnRecipe(itemName);
                }
                break;
            default:
                addToConsole("Unknown command: '" + parts[0] + "'");
        }
    }

    private void showRecipes() {
        addToConsole("--- Learned Recipes ---");
        getLearnedRecipes().forEach(item -> addToConsole("- " + item.name));
    }

    private void craftItem(String itemName) {
        Item itemToCraft = items.stream().filter(i -> i.name.equalsIgnoreCase(itemName)).findFirst().orElse(null);
        if (itemToCraft == null) { InvalidItemName(); return; }
        if (player_inventory.size() >= MAX_INVENTORY_SIZE && !player_inventory.containsKey(itemToCraft)) { IsInventoryFull(); return; }
        if (player_energy < 2) { addToConsole("Not enough energy to craft."); return; }
        for (Map.Entry<String, Integer> entry : itemToCraft.required_materials.entrySet()) {
            if (getMaterialCount(entry.getKey()) < entry.getValue()) { NotEnoughResources(); return; }
        }
        if(!isRecipeLearned(itemToCraft)) {IsNotLearnedItem(); return; }
        itemToCraft.required_materials.forEach(this::removeMaterial);
        player_inventory.put(itemToCraft, player_inventory.getOrDefault(itemToCraft, 0) + 1);
        player_energy -= 2;
        addToConsole(itemToCraft.name + " crafted successfully!");
    }

    public void learnRecipe(String recipeName) {
     switch (recipeName) {
         case "mining":
             if (mining_level<5){mining_level++;}
             addToConsole(recipeName + " learned successfully!.");
             break;
             case "farming":
                 if (farming_level<5){farming_level++;}
                 addToConsole(recipeName + " learned successfully!.");
                 break;
                 case "foraging":
                     if (foraging_level<5){foraging_level++;}
                     addToConsole(recipeName + " learned successfully!.");
                     break;
         case "fish shop":
             Fish_Shop=true;
             addToConsole(recipeName + " learned successfully!.");
             break;
         case "pierres general store":
             Pierres_General_Store=true;
             addToConsole(recipeName + " learned successfully!.");
             break;
             default:
                 addToConsole("Unknown recipe: " + recipeName);
     }
    }

    private void handlePlaceCommand(String command, Player player) {
        String[] parts = command.split("\\s-");
        if (parts.length == 3 && parts[0].trim().equalsIgnoreCase("place item")) {
            String itemName = parts[1].replace("n ", "").trim();
            String direction = parts[2].replace("d ", "").trim();

            Item itemToPlace = player_inventory.keySet().stream().filter(i -> i.name.equalsIgnoreCase(itemName)).findFirst().orElse(null);
            if (itemToPlace == null || !itemToPlace.isPlaceable) { addToConsole("Item '" + itemName + "' not in inventory or cannot be placed."); return; }

            int targetX = (int) (player.getPosition().x / Main.TILE_SIZE);
            int targetY = (int) (player.getPosition().y / Main.TILE_SIZE);

            switch(direction.toUpperCase()) {
                case "N": targetY++; break; case "S": targetY--; break;
                case "E": targetX++; break; case "W": targetX--; break;
                default: addToConsole("Invalid direction. Use N, S, E, W."); return;
            }

            if (gameMap.getTile(targetX, targetY) != null && gameMap.getTile(targetX, targetY).isWalkable() && !gameMap.isCollision(new Rectangle(targetX * Main.TILE_SIZE, targetY * Main.TILE_SIZE, 1, 1))) {
                player_inventory.put(itemToPlace, player_inventory.get(itemToPlace) - 1);
                if (player_inventory.get(itemToPlace) == 0) player_inventory.remove(itemToPlace);

                gameMap.addElement(new PlacedItemElement(itemToPlace, targetX * Main.TILE_SIZE, targetY * Main.TILE_SIZE));
                addToConsole(itemName + " placed at (" + targetX + "," + targetY + ").");
            } else {
                addToConsole("Location (" + targetX + "," + targetY + ") is not suitable for placement.");
            }
        } else {
            addToConsole("Invalid command format. Example: place item -n Furnace -d N");
        }
    }

    private void handleCheatCommand(String command) {
        String[] parts = command.split("\\s-");
        if (parts.length == 3 && parts[0].trim().equalsIgnoreCase("cheat add item")) {
            String itemName = parts[1].replace("n ", "").trim();
            int count;
            try { count = Integer.parseInt(parts[2].replace("c ", "").trim()); } catch (Exception e) { addToConsole("Invalid count."); return; }

            Item itemToAdd = items.stream().filter(i -> i.name.equalsIgnoreCase(itemName)).findFirst().orElse(null);
            if (itemToAdd == null) { InvalidItemName(); return; }
            if (player_inventory.size() >= MAX_INVENTORY_SIZE && !player_inventory.containsKey(itemToAdd)) { IsInventoryFull(); return; }

            player_inventory.put(itemToAdd, player_inventory.getOrDefault(itemToAdd, 0) + count);
            addToConsole(count + " of " + itemName + " added via cheat.");
        } else {
            addToConsole("Invalid cheat format. Example: cheat add item -n Scarecrow -c 5");
        }
    }

    private int getMaterialCount(String material) {
        switch (material.toLowerCase()) {
            case "wood": return player_wood; case "stone": return player_stone;
            case "copper ore": return player_copper_ore; case "copper bar": return player_copper_bar;
            case "coal": return player_coal; case "iron ore": return player_iron_ore;
            case "iron bar": return player_iron_bar; case "gold ore": return player_gold_ore;
            case "gold bar": return player_gold_bar; case "fiber": return player_fiber;
            default: return 0;
        }
    }

    private void removeMaterial(String material, int amount) {
        switch (material.toLowerCase()) {
            case "wood": player_wood -= amount; break; case "stone": player_stone -= amount; break;
            case "copper ore": player_copper_ore -= amount; break; case "copper bar": player_copper_bar -= amount; break;
            case "coal": player_coal -= amount; break; case "iron ore": player_iron_ore -= amount; break;
            case "iron bar": player_iron_bar -= amount; break; case "gold ore": player_gold_ore -= amount; break;
            case "gold bar": player_gold_bar -= amount; break; case "fiber": player_fiber -= amount;
        }
    }

    public boolean IsPlayerInHouse() { return (player_x >= house_x && player_x < (house_x + house_length) && player_y >= house_y && player_y < (house_y + house_width)); }

    public List<Item> getLearnedRecipes() {
        return items.stream().filter(this::isRecipeLearned).collect(Collectors.toList());
    }

    private boolean isRecipeLearned(Item item) {
        switch (item.source) {
            case "-": return true;
            case "Pierre's General Store": return Pierres_General_Store;
            case "Fish Shop": return Fish_Shop;
            case "Mining": return mining_level >= item.source_level;
            case "Farming": return farming_level >= item.source_level;
            case "Foraging": return foraging_level >= item.source_level;
            default: return false;
        }
    }

    private String joinArray(String[] arr, int startIndex) { return String.join(" ", Arrays.copyOfRange(arr, startIndex, arr.length)); }

    public List<String> getConsoleHistory() { return consoleHistory; }
    private void addToConsole(String message) { consoleHistory.add(message); }
    public void NotPlayerInHouse() { addToConsole("You must be inside your house to craft!"); }
    public void InvalidItemName() { addToConsole("This item does not exist."); }
    public void IsInventoryFull() { addToConsole("Your inventory is full."); }
    public void NotEnoughResources() { addToConsole("You don't have enough resources."); }
    public void IsNotLearnedItem() { addToConsole("You are not learned this item."); }
}
