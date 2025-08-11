package com.mygame.stardew;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// ###################################################################################
// ## کلاس اصلی بازی (Main Class)
// ###################################################################################
public class Main extends ApplicationAdapter {
    private Stage gameStage;
    private Stage uiStage;

    private Skin skin;
    private TradeController tradeController;
    private ShopUi shopUi;

    private OrthographicCamera camera;
    private Viewport gameViewport;

    private PlayerActor player;
    private ArrayList<BuildingActor> buildings = new ArrayList<>();
    private Label interactionLabel;
    private Texture placeholderTexture;

    private Stage errorStage;
    private boolean initializationFailed = false;

    @Override
    public void create() {
        try {
            camera = new OrthographicCamera();
            gameViewport = new ScreenViewport(camera);
            gameStage = new Stage(gameViewport);

            uiStage = new Stage(new ScreenViewport());

            InputMultiplexer inputMultiplexer = new InputMultiplexer();
            inputMultiplexer.addProcessor(uiStage);
            inputMultiplexer.addProcessor(gameStage);
            Gdx.input.setInputProcessor(inputMultiplexer);

            skin = new Skin(Gdx.files.internal("uiskin.json"));
            tradeController = new TradeController();
            shopUi = new ShopUi(skin, uiStage, tradeController);

            createPlaceholderTexture();

            Texture grassTexture = loadTexture("grass.png");
            grassTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

            gameStage.addActor(new BackgroundActor(grassTexture));
            createBuildings();
            createPlayer();
            createUI();

            setupGameStageInputListener();

        } catch (Exception e) {
            System.err.println("!!!!!!!!!! An error occurred during game initialization !!!!!!!!!!");
            e.printStackTrace();
            initializationFailed = true;
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            setupErrorStage(sw.toString());
        }
    }

    private void setupGameStageInputListener() {
        gameStage.addListener(new InputListener() {
            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                if (keycode == Input.Keys.E) {
                    if (uiStage.getActors().select(actor -> actor instanceof Dialog).iterator().hasNext()) {
                        return false;
                    }
                    for (BuildingActor building : buildings) {
                        if (player.isInInteractionRange(building)) {
                            // **FIX**: Shipping Bin now opens the sell window.
                            if (building.getBuildingName().equals("Shipping Bin")) {
                                shopUi.showSellWindow();
                            } else {
                                tradeController.enterShop(building.getBuildingName());
                                shopUi.showShopWindow();
                            }
                            return true;
                        }
                    }
                }
                return false;
            }
        });
    }

    private void createPlayer() {
        player = new PlayerActor(loadTexture("player.png"));
        player.setPosition(1500, 1000);
        gameStage.addActor(player);
    }

    private void createBuildings() {
        buildings.add(new BuildingActor("Pierre's General Store", loadTexture("pierre_shop.png"), 800, 2000));
        buildings.add(new BuildingActor("Blacksmith", loadTexture("blacksmith_shop.png"), 1500, 2500));
        buildings.add(new BuildingActor("Carpenter's Shop", loadTexture("carpenter_shop.png"), 2200, 2000));
        buildings.add(new BuildingActor("Marnie's Ranch", loadTexture("marnie_shop.png"), 800, 500));
        buildings.add(new BuildingActor("The Stardrop Saloon", loadTexture("saloon_shop.png"), 1500, 500));
        buildings.add(new BuildingActor("JojaMart", loadTexture("jojamart_shop.png"), 2200, 500));
        buildings.add(new BuildingActor("Fish Shop", loadTexture("fish_shop.png"), 2800, 1200));
        buildings.add(new BuildingActor("Shipping Bin", loadTexture("shipping_bin.png"), 1350, 950));

        for (BuildingActor building : buildings) {
            gameStage.addActor(building);
        }
    }

    private void createUI() {
        Table rootUITable = new Table();
        rootUITable.setFillParent(true);
        uiStage.addActor(rootUITable);

        Label balanceLabel = new Label("", skin);
        balanceLabel.setName("balanceLabel");

        Label dayLabel = new Label("", skin);
        dayLabel.setName("dayLabel");

        // **NEW**: Next Day Button
        TextButton nextDayButton = new TextButton("Next Day", skin);
        nextDayButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                shopUi.showNextDayConfirmation();
            }
        });

        // Top row for Day and Balance
        Table topTable = new Table();
        topTable.add(dayLabel).left().pad(10);
        topTable.add().expandX();
        topTable.add(balanceLabel).right().pad(10);

        rootUITable.add(topTable).growX().row();
        rootUITable.add().expandY(); // Spacer to push the next button down
        rootUITable.add(nextDayButton).bottom().right().pad(10).row();


        interactionLabel = new Label("", skin, "default");
        interactionLabel.setVisible(false);
        uiStage.addActor(interactionLabel);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.2f, 0.5f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (initializationFailed) {
            if (errorStage != null) {
                errorStage.act(Gdx.graphics.getDeltaTime());
                errorStage.draw();
            }
            return;
        }

        handleInput();
        gameStage.act(Gdx.graphics.getDeltaTime());
        uiStage.act(Gdx.graphics.getDeltaTime());
        updateCamera();
        checkInteraction();
        updateUI();

        gameStage.draw();
        uiStage.draw();
    }

    private void handleInput() {
        if (uiStage.getActors().select(actor -> actor instanceof Dialog).iterator().hasNext()) {
            return;
        }

        float moveSpeed = 200f * Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyPressed(Input.Keys.W)) player.moveBy(0, moveSpeed);
        if (Gdx.input.isKeyPressed(Input.Keys.S)) player.moveBy(0, -moveSpeed);
        if (Gdx.input.isKeyPressed(Input.Keys.A)) player.moveBy(-moveSpeed, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.D)) player.moveBy(moveSpeed, 0);
    }

    private void updateCamera() {
        camera.position.x = player.getX() + player.getWidth() / 2;
        camera.position.y = player.getY() + player.getHeight() / 2;
        camera.update();
    }

    private void updateUI() {
        Label balanceLabel = uiStage.getRoot().findActor("balanceLabel");
        if (balanceLabel != null) {
            balanceLabel.setText(String.format("Balance: %.0fg", tradeController.getPlayer().getBalance()));
        }

        Label dayLabel = uiStage.getRoot().findActor("dayLabel");
        if(dayLabel != null) {
            dayLabel.setText("Day: " + tradeController.getDay());
        }
    }

    private void checkInteraction() {
        boolean inRange = false;
        for (BuildingActor building : buildings) {
            if (player.isInInteractionRange(building)) {
                interactionLabel.setVisible(true);
                interactionLabel.setText("Press 'E' to interact with " + building.getBuildingName());
                interactionLabel.pack();

                Vector3 screenPos = camera.project(new Vector3(building.getX(), building.getY() + building.getHeight(), 0));
                interactionLabel.setPosition(screenPos.x - interactionLabel.getWidth() / 2, screenPos.y + 5);

                inRange = true;
                break;
            }
        }
        if (!inRange) {
            interactionLabel.setVisible(false);
        }
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, false);
        uiStage.getViewport().update(width, height, true);
        if (errorStage != null) {
            errorStage.getViewport().update(width, height, true);
        }
    }

    @Override
    public void dispose() {
        if (gameStage != null) gameStage.dispose();
        if (uiStage != null) uiStage.dispose();
        if (skin != null) skin.dispose();
        if (placeholderTexture != null) placeholderTexture.dispose();
        if (errorStage != null) errorStage.dispose();
    }

    private void createPlaceholderTexture() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.MAGENTA);
        pixmap.fill();
        placeholderTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    private Texture loadTexture(String path) {
        try {
            return new Texture(Gdx.files.internal(path));
        } catch (GdxRuntimeException e) {
            System.err.println("Error loading texture: " + path + ". Using placeholder instead.");
            return placeholderTexture;
        }
    }

    private void setupErrorStage(String errorMessage) {
        errorStage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(errorStage);
        Skin errorSkin = new Skin();
        errorSkin.add("default-font", new BitmapFont(Gdx.files.classpath("com/badlogic/gdx/utils/arial-15.fnt"), false), BitmapFont.class);
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        errorSkin.add("white", new Texture(pixmap));
        pixmap.dispose();
        errorSkin.add("default", new Label.LabelStyle(errorSkin.getFont("default-font"), Color.WHITE));
        errorSkin.add("default", new TextButton.TextButtonStyle(errorSkin.newDrawable("white", Color.DARK_GRAY), null, null, errorSkin.getFont("default-font")));
        errorSkin.add("default", new Window.WindowStyle(errorSkin.getFont("default-font"), Color.WHITE, errorSkin.newDrawable("white", Color.BLACK)));
        errorSkin.add("default", new ScrollPane.ScrollPaneStyle());
        Dialog errorDialog = new Dialog("Initialization Error", errorSkin) {
            @Override
            protected void result(Object object) { Gdx.app.exit(); }
        };
        TextArea errorText = new TextArea("A critical error occurred:\n\n" + errorMessage, errorSkin);
        errorText.setDisabled(true);
        ScrollPane scrollPane = new ScrollPane(errorText, errorSkin);
        errorDialog.getContentTable().add(scrollPane).grow();
        errorDialog.button("Exit", true).pack();
        errorDialog.setPosition((Gdx.graphics.getWidth() - errorDialog.getWidth()) / 2f, (Gdx.graphics.getHeight() - errorDialog.getHeight()) / 2f);
        errorStage.addActor(errorDialog);
    }

    class BackgroundActor extends Actor {
        private final Texture texture;
        public BackgroundActor(Texture texture) { this.texture = texture; }
        @Override
        public void draw(Batch batch, float parentAlpha) {
            batch.setColor(getColor());
            batch.draw(texture, -10000, -10000, 0, 0, 20000, 20000);
        }
    }
}

// ###################################################################################
// ## کلاس‌های بازیگر (Actors)
// ###################################################################################

class PlayerActor extends Actor {
    private final Texture texture;
    private final float INTERACTION_RADIUS = 100f;

    public PlayerActor(Texture texture) {
        this.texture = texture;
        setBounds(getX(), getY(), 48, 64);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(texture, getX(), getY(), getWidth(), getHeight());
    }

    public boolean isInInteractionRange(BuildingActor building) {
        Vector2 playerCenter = new Vector2(getX() + getWidth() / 2, getY() + getHeight() / 2);
        Vector2 buildingCenter = new Vector2(building.getX() + building.getWidth() / 2, building.getY() + building.getHeight() / 2);
        return playerCenter.dst(buildingCenter) < INTERACTION_RADIUS;
    }
}

class BuildingActor extends Actor {
    private final String name;
    private final Texture texture;

    public BuildingActor(String name, Texture texture, float x, float y) {
        this.name = name;
        this.texture = texture;
        setBounds(x, y, texture.getWidth(), texture.getHeight());
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(texture, getX(), getY(), getWidth(), getHeight());
    }

    public String getBuildingName() { return name; }
}

// ###################################################################################
// ## کلاس‌های منطق بازی (Logic)
// ###################################################################################

class TradeController {
    private final Player player;
    private final List<Shop> allShops;
    private Shop currentShop;
    private int day = 1;

    public TradeController() {
        this.player = new Player(5000);
        this.allShops = new ArrayList<>();
        initializeShops();
    }

    public void nextDay() {
        day++;
        for (Shop shop : allShops) {
            shop.resetDailyLimits();
        }
    }

    public int getDay() {
        return day;
    }

    private void initializeShops() {
        List<Product> pierreProducts = List.of(
            new Product("Large Pack", "Unlocks the 2nd row of inventory.", 2000, 0, 1),
            new Product("Deluxe Pack", "Unlocks the 3rd row of inventory.", 10000, 0, 1),
            new Product("Parsnip Seeds", "Plant these in the spring. Takes 4 days to mature.", 20, 10, -1),
            new Product("Bean Starter", "Plant these in the spring. Takes 10 days to mature...", 60, 30, -1),
            new Product("Cauliflower Seeds", "Plant these in the spring. Takes 12 days to mature.", 80, 40, -1),
            new Product("Potato Seeds", "Plant in the spring. Takes 6 days to mature.", 50, 25, -1),
            new Product("Tulip Bulb", "Plant in spring. Takes 6 days to produce a colorful flower.", 20, 10, -1),
            new Product("Kale Seeds", "Plant in spring. Takes 6 days to mature.", 70, 35, -1),
            new Product("Jazz Seeds", "Plant in spring. Takes 7 days to produce a blue puffball flower.", 30, 15, -1),
            new Product("Garlic Seeds", "Plant in spring. Takes 4 days to mature.", 40, 20, -1),
            new Product("Rice Shoot", "Plant in the spring. Takes 8 days to mature.", 40, 20, -1),
            new Product("Melon Seeds", "Plant in summer. Takes 12 days to mature.", 80, 40, -1),
            new Product("Tomato Seeds", "Plant in summer. Takes 11 days to mature.", 50, 25, -1),
            new Product("Blueberry Seeds", "Plant in summer. Takes 13 days to mature.", 80, 40, -1),
            new Product("Pepper Seeds", "Plant in summer. Takes 5 days to mature.", 40, 20, -1),
            new Product("Wheat Seeds", "Plant in summer. Takes 4 days to mature.", 10, 5, -1),
            new Product("Radish Seeds", "Plant in summer. Takes 6 days to mature.", 40, 20, -1),
            new Product("Poppy Seeds", "Plant in summer. Produces a bright red flower in 7 days.", 100, 50, -1),
            new Product("Spangle Seeds", "Plant in summer. Takes 8 days to produce a vibrant tropical flower.", 50, 25, -1),
            new Product("Hops Starter", "Plant in summer. Takes 11 days to grow.", 60, 30, -1),
            new Product("Corn Seeds", "Plant in summer or fall. Takes 14 days to mature.", 150, 75, -1),
            new Product("Sunflower Seeds", "Plant in summer or fall. Takes 8 days to produce a large sunflower.", 200, 100, -1),
            new Product("Red Cabbage Seeds", "Plant in summer. Takes 9 days to mature.", 100, 50, -1),
            new Product("Bouquet", "A gift that shows your romantic interest.", 100, 100, -1),
            new Product("Grass Starter", "Place this on your farm to start a new patch of grass.", 100, 50, -1),
            new Product("Sugar", "Adds sweetness to pastries and candies.", 125, 62, -1),
            new Product("Wheat Flour", "A common cooking ingredient.", 100, 50, -1),
            new Product("Rice", "A basic grain often served under vegetables.", 200, 100, -1)
        );
        allShops.add(new Shop("Pierre's General Store", pierreProducts));
        List<Product> blacksmithProducts = List.of(
            new Product("Copper Ore", "A common ore that can be smelted into bars.", 75, 20, -1),
            new Product("Iron Ore", "A fairly common ore that can be smelted into bars.", 150, 50, -1),
            new Product("Coal", "A combustible rock that is useful for crafting and smelting.", 150, 15, -1),
            new Product("Gold Ore", "A precious ore that can be smelted into bars.", 400, 100, -1)
        );
        allShops.add(new Shop("Blacksmith", blacksmithProducts));
        List<Product> carpenterProducts = List.of(
            new Product("Wood", "A sturdy, yet flexible plant material with a wide variety of uses.", 10, 2, -1),
            new Product("Stone", "A common material with many uses in crafting and building.", 20, 2, -1),
            new Product("Barn", "Houses 4 barn-dwelling animals.", 6000, 0, 1),
            new Product("Coop", "Houses 4 coop-dwelling animals.", 4000, 0, 1)
        );
        allShops.add(new Shop("Carpenter's Shop", carpenterProducts));
        List<Product> marnieProducts = List.of(
            new Product("Hay", "Dried grass used as animal food.", 50, 25, -1),
            new Product("Milk Pail", "Gather milk from your animals.", 1000, 200, 1),
            new Product("Shears", "Use this to collect wool from sheep.", 1000, 200, 1),
            new Product("Chicken", "Well-cared-for chickens lay eggs every day.", 800, 150, 1),
            new Product("Cow", "Can be milked daily.", 1500, 300, 1),
            new Product("Goat", "Happy provide goat milk every other day.", 4000, 500, 1),
            new Product("Duck", "Happy lay duck eggs every other day.", 1200, 250, 1)
        );
        allShops.add(new Shop("Marnie's Ranch", marnieProducts));
        List<Product> saloonProducts = List.of(
            new Product("Beer", "Drink in moderation.", 400, 100, -1),
            new Product("Salad", "A healthy garden salad.", 220, 110, -1),
            new Product("Bread", "A crusty baguette.", 120, 60, -1),
            new Product("Spaghetti", "It's popular for all the right reasons.", 240, 120, -1),
            new Product("Pizza", "It's popular for all the right reasons.", 600, 300, -1),
            new Product("Coffee", "It smells delicious. This is sure to give you a boost.", 300, 75, -1),
            new Product("Hashbrowns Recipe", "A recipe to make Hashbrowns.", 50, 0, 1),
            new Product("Omelet Recipe", "A recipe to make Omelet.", 100, 0, 1)
        );
        allShops.add(new Shop("The Stardrop Saloon", saloonProducts));
        List<Product> jojaProducts = List.of(
            new Product("Joja Cola", "The flagship product of Joja corporation.", 75, 25, -1),
            new Product("Grass Starter", "Place this on your farm to start a new patch of grass.", 125, 50, -1),
            new Product("Sugar", "Adds sweetness to pastries and candies.", 125, 62, -1),
            new Product("Wheat Flour", "A common cooking ingredient.", 125, 50, -1),
            new Product("Tomato Seeds", "Plant these in the summer. Takes 11 days to mature.", 62, 25, -1),
            new Product("Corn Seeds", "Plant these in the summer or fall. Takes 14 days to mature.", 187, 50, -1)
        );
        allShops.add(new Shop("JojaMart", jojaProducts));
        List<Product> fishShopProducts = List.of(
            new Product("Fish Smoker Recipe", "A recipe to make Fish Smoker.", 10000, 0, 1),
            new Product("Trout Soup", "Pretty salty.", 250, 125, -1),
            new Product("Bamboo Pole", "Use in the water to catch fish.", 500, 50, 1),
            new Product("Training Rod", "It's a lot easier to use than other rods.", 25, 10, 1),
            new Product("Fiberglass Rod", "Use in the water to catch fish.", 1800, 180, 1),
            new Product("Iridium Rod", "Use in the water to catch fish.", 7500, 750, 1)
        );
        allShops.add(new Shop("Fish Shop", fishShopProducts));
    }

    public void enterShop(String shopName) {
        currentShop = allShops.stream()
            .filter(s -> s.getName().equalsIgnoreCase(shopName))
            .findFirst()
            .orElse(null);
    }

    public String purchase(String productName, int count) {
        if (currentShop == null) return "You are not in a shop.";
        if (count <= 0) return "Count must be positive.";
        Optional<Product> optProduct = currentShop.getProductByName(productName);
        if (optProduct.isEmpty()) return "Product not found.";
        Product product = optProduct.get();
        int remainingLimit = currentShop.getRemainingLimit(product.getName());
        if (remainingLimit != -1 && count > remainingLimit) {
            return "You can only buy " + remainingLimit + " more today.";
        }
        double totalCost = product.getPrice() * count;
        if (player.getBalance() < totalCost) return "Not enough money.";
        player.spendMoney(totalCost);
        player.addItem(product.getName(), count);
        if (remainingLimit != -1) {
            currentShop.decreaseLimit(product.getName(), count);
        }
        return "Purchase successful!";
    }

    public String sell(String productName, int count) {
        if (count <= 0) return "Count must be positive.";
        int playerQuantity = player.getItemQuantity(productName);
        if (playerQuantity < count) {
            return "You only have " + playerQuantity + " of " + productName + ".";
        }
        Optional<Product> productToSell = findProductGlobally(productName);
        int baseSellPrice = productToSell.map(p -> p.getSellPrice() == 0 ? p.getPrice() / 2 : p.getSellPrice()).orElse(10);
        double totalEarning = baseSellPrice * count;
        player.removeItem(productName, count);
        player.addMoney(totalEarning);
        return "Sold " + count + " " + productName + " for " + totalEarning + "g.";
    }

    public Player getPlayer() { return player; }
    public Shop getCurrentShop() { return currentShop; }
    public Optional<Product> findProductGlobally(String productName) {
        return allShops.stream()
            .flatMap(shop -> shop.getProducts().stream())
            .filter(p -> p.getName().equalsIgnoreCase(productName))
            .findFirst();
    }
}

// ###################################################################################
// ## کلاس‌های مدل (Models)
// ###################################################################################

class Product {
    private final String name, description;
    private final int price, sellPrice, dailyLimit;
    public Product(String name, String description, int price, int sellPrice, int dailyLimit) {
        this.name = name; this.description = description; this.price = price;
        this.sellPrice = sellPrice; this.dailyLimit = dailyLimit;
    }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getPrice() { return price; }
    public int getSellPrice() { return sellPrice; }
    public int getDailyLimit() { return dailyLimit; }
}

class Shop {
    private final String name;
    private final List<Product> products;
    private final Map<String, Integer> dailyLimitsTracker;
    public Shop(String name, List<Product> products) {
        this.name = name; this.products = products; this.dailyLimitsTracker = new HashMap<>();
        resetDailyLimits();
    }
    public void resetDailyLimits() {
        dailyLimitsTracker.clear();
        products.stream().filter(p -> p.getDailyLimit() != -1)
            .forEach(p -> dailyLimitsTracker.put(p.getName().toLowerCase(), p.getDailyLimit()));
    }
    public Optional<Product> getProductByName(String name) {
        return products.stream().filter(p -> p.getName().equalsIgnoreCase(name)).findFirst();
    }
    public int getRemainingLimit(String productName) {
        return dailyLimitsTracker.getOrDefault(productName.toLowerCase(), -1);
    }
    public void decreaseLimit(String productName, int count) {
        String lowerCaseName = productName.toLowerCase();
        if (dailyLimitsTracker.containsKey(lowerCaseName)) {
            dailyLimitsTracker.put(lowerCaseName, dailyLimitsTracker.get(lowerCaseName) - count);
        }
    }
    public String getName() { return name; }
    public List<Product> getProducts() { return products; }
}

class Player {
    private double balance;
    private final Map<String, Integer> inventory = new HashMap<>();
    public Player(double initialBalance) { this.balance = initialBalance; }
    public void addMoney(double amount) { this.balance += amount; }
    public void spendMoney(double amount) { this.balance -= amount; }
    public void addItem(String productName, int quantity) {
        inventory.put(productName, inventory.getOrDefault(productName, 0) + quantity);
    }
    public void removeItem(String productName, int quantity) {
        int currentQuantity = inventory.getOrDefault(productName, 0);
        if (currentQuantity > quantity) {
            inventory.put(productName, currentQuantity - quantity);
        } else {
            inventory.remove(productName);
        }
    }
    public int getItemQuantity(String productName) { return inventory.getOrDefault(productName, 0); }
    public double getBalance() { return balance; }
    public Map<String, Integer> getInventory() { return inventory; }
}

// ###################################################################################
// ## کلاس رابط کاربری (UI)
// ###################################################################################

class ShopUi {
    private final Skin skin;
    private final Stage uiStage;
    private final TradeController controller;
    private boolean showAvailableOnly = false;

    public ShopUi(Skin skin, Stage uiStage, TradeController controller) {
        this.skin = skin;
        this.uiStage = uiStage;
        this.controller = controller;
    }

    public void showShopWindow() {
        Shop shop = controller.getCurrentShop();
        if (shop == null) return;
        final Dialog shopDialog = new Dialog(shop.getName(), skin);
        shopDialog.setModal(true);
        shopDialog.setMovable(true);

        Table filterTable = new Table();
        TextButton allButton = new TextButton("All Products", skin);
        TextButton availableButton = new TextButton("Available Only", skin);

        allButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showAvailableOnly = false;
                shopDialog.hide();
                showShopWindow();
            }
        });

        availableButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showAvailableOnly = true;
                shopDialog.hide();
                showShopWindow();
            }
        });

        filterTable.add(allButton).pad(5);
        filterTable.add(availableButton).pad(5);

        Table productsTable = new Table(skin);
        ScrollPane scrollPane = new ScrollPane(productsTable, skin);
        scrollPane.setFadeScrollBars(false);

        for (Product product : shop.getProducts()) {
            int remainingLimit = shop.getRemainingLimit(product.getName());
            boolean isAvailable = remainingLimit == -1 || remainingLimit > 0;

            if (showAvailableOnly && !isAvailable) {
                continue;
            }

            String productText;
            if (remainingLimit != -1) {
                productText = String.format("%s (%d left) - %dg", product.getName(), remainingLimit, product.getPrice());
            } else {
                productText = String.format("%s - %dg", product.getName(), product.getPrice());
            }
            TextButton productButton = new TextButton(productText, skin);

            if (!isAvailable) {
                productButton.setDisabled(true);
                productButton.setColor(Color.DARK_GRAY);
            }

            productButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (!productButton.isDisabled()) {
                        showPurchaseDialog(product, shopDialog);
                    }
                }
            });
            productsTable.add(productButton).growX().pad(2).row();
        }

        Table contentTable = shopDialog.getContentTable();
        contentTable.pad(10);
        contentTable.add(filterTable).growX().row();
        contentTable.add(scrollPane).width(400).height(300);
        shopDialog.button("Close");
        shopDialog.show(uiStage);
    }

    private void showPurchaseDialog(Product product, Dialog parentDialog) {
        final Dialog purchaseDialog = new Dialog("Buy " + product.getName(), skin, "dialog");
        purchaseDialog.setModal(true);
        final Label quantityLabel = new Label("1", skin);
        final Label totalCostLabel = new Label("Cost: " + product.getPrice() + "g", skin);
        TextButton plusButton = new TextButton("+", skin);
        plusButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int current = Integer.parseInt(quantityLabel.getText().toString()) + 1;
                quantityLabel.setText(String.valueOf(current));
                totalCostLabel.setText("Cost: " + (product.getPrice() * current) + "g");
            }
        });
        TextButton minusButton = new TextButton("-", skin);
        minusButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int current = Integer.parseInt(quantityLabel.getText().toString());
                if (current > 1) {
                    current--;
                    quantityLabel.setText(String.valueOf(current));
                    totalCostLabel.setText("Cost: " + (product.getPrice() * current) + "g");
                }
            }
        });
        TextButton buyButton = new TextButton("Buy", skin);
        buyButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int quantity = Integer.parseInt(quantityLabel.getText().toString());
                String result = controller.purchase(product.getName(), quantity);
                showInfoDialog(result);
                purchaseDialog.hide();
                parentDialog.hide();
            }
        });
        TextButton cancelButton = new TextButton("Cancel", skin);
        cancelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                purchaseDialog.hide();
            }
        });
        Table table = purchaseDialog.getContentTable();
        table.pad(20);
        table.add(new Label(product.getDescription(), skin)).colspan(3).row();
        table.add(minusButton).pad(10);
        table.add(quantityLabel).pad(10);
        table.add(plusButton).pad(10).row();
        table.add(totalCostLabel).colspan(3).pad(10).row();
        purchaseDialog.getButtonTable().add(buyButton).pad(10);
        purchaseDialog.getButtonTable().add(cancelButton).pad(10);
        purchaseDialog.show(uiStage);
    }

    public void showSellWindow() {
        final Dialog sellDialog = new Dialog("Sell Items", skin);
        sellDialog.setModal(true);
        sellDialog.setMovable(true);
        Table contentTable = new Table(skin);
        ScrollPane scrollPane = new ScrollPane(contentTable, skin);
        scrollPane.setFadeScrollBars(false);
        Map<String, Integer> inventory = controller.getPlayer().getInventory();
        if (inventory.isEmpty()) {
            contentTable.add(new Label("Your inventory is empty.", skin));
        } else {
            for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
                String itemName = entry.getKey();
                int quantity = entry.getValue();
                int sellPrice = controller.findProductGlobally(itemName)
                    .map(p -> p.getSellPrice() == 0 ? p.getPrice() / 2 : p.getSellPrice())
                    .orElse(10);

                if (sellPrice == 0) continue;

                String itemText = String.format("%s (x%d) - %dg each", itemName, quantity, sellPrice);
                Label itemLabel = new Label(itemText, skin);
                TextButton sellButton = new TextButton("Sell...", skin);

                sellButton.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        showSellQuantityDialog(itemName, quantity, sellPrice, sellDialog);
                    }
                });
                contentTable.add(itemLabel).left().pad(5);
                contentTable.add(sellButton).right().pad(5).row();
            }
        }
        sellDialog.getContentTable().add(scrollPane).width(450).height(300);
        sellDialog.button("Close");
        sellDialog.show(uiStage);
    }

    private void showSellQuantityDialog(String itemName, int maxQuantity, int baseSellPrice, Dialog parentDialog) {
        final Dialog sellQuantityDialog = new Dialog("Sell " + itemName, skin, "dialog");
        sellQuantityDialog.setModal(true);

        final Label quantityLabel = new Label("1", skin);
        final Label totalEarningsLabel = new Label("Earn: " + baseSellPrice + "g", skin);

        TextButton plusButton = new TextButton("+", skin);
        plusButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int current = Integer.parseInt(quantityLabel.getText().toString()) + 1;
                quantityLabel.setText(String.valueOf(current));
                totalEarningsLabel.setText("Earn: " + (baseSellPrice * current) + "g");
            }
        });

        TextButton minusButton = new TextButton("-", skin);
        minusButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int current = Integer.parseInt(quantityLabel.getText().toString());
                if (current > 1) {
                    current--;
                    quantityLabel.setText(String.valueOf(current));
                    totalEarningsLabel.setText("Earn: " + (baseSellPrice * current) + "g");
                }
            }
        });

        TextButton sellButton = new TextButton("Sell", skin);
        sellButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int quantity = Integer.parseInt(quantityLabel.getText().toString());
                String result = controller.sell(itemName, quantity);
                showInfoDialog(result);
                if (!result.toLowerCase().contains("only have")) {
                    sellQuantityDialog.hide();
                    parentDialog.hide();
                    showSellWindow();
                }
            }
        });

        TextButton cancelButton = new TextButton("Cancel", skin);
        cancelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                sellQuantityDialog.hide();
            }
        });

        Table table = sellQuantityDialog.getContentTable();
        table.pad(20);
        table.add(new Label("How many to sell?", skin)).colspan(3).row();
        table.add(minusButton).pad(10);
        table.add(quantityLabel).pad(10);
        table.add(plusButton).pad(10).row();
        table.add(totalEarningsLabel).colspan(3).pad(10).row();
        sellQuantityDialog.getButtonTable().add(sellButton).pad(10);
        sellQuantityDialog.getButtonTable().add(cancelButton).pad(10);
        sellQuantityDialog.show(uiStage);
    }

    public void showNextDayConfirmation() {
        Dialog dialog = new Dialog("End Day", skin, "dialog") {
            @Override
            protected void result(Object object) {
                if ((Boolean) object) {
                    controller.nextDay();
                    showInfoDialog("Day " + controller.getDay() + " has begun.\nShop limits have been reset.");
                }
            }
        };
        dialog.text("Go to sleep for the night?");
        dialog.button("Yes", true);
        dialog.button("No", false);
        dialog.show(uiStage);
    }

    private void showInfoDialog(String message) {
        new Dialog("Info", skin, "dialog") {{
            text(message);
            button("OK");
        }}.show(uiStage);
    }
}
