package com.mygame.stardew;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

// =================================================================================
// Main Game Class
// =================================================================================
public class Main extends Game {
    public SpriteBatch batch;
    public BitmapFont font;
    public Player player;
    public Skin skin;
    public AssetManager assets;
    public ShapeRenderer shapeRenderer;

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        shapeRenderer = new ShapeRenderer();
        assets = new AssetManager();
        assets.load();

        player = new Player("Farmer", 50000); // Initial money and wood

        try {
            skin = new Skin(Gdx.files.internal("uiskin.json"));
        } catch (Exception e) {
            Gdx.app.error("Skin Error", "Could not load uiskin.json. Creating default skin.", e);
            skin = new Skin();
            skin.add("default-font", font, BitmapFont.class);
            TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle(); textButtonStyle.font = font; skin.add("default", textButtonStyle);
            Window.WindowStyle windowStyle = new Window.WindowStyle(); windowStyle.titleFont = font; skin.add("default", windowStyle);
            Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE); skin.add("default", labelStyle);
            com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle listStyle = new com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle();
            listStyle.font = font;
            listStyle.selection = new Image(assets.whitePixel).getDrawable();
            listStyle.fontColorSelected = Color.BLACK;
            listStyle.fontColorUnselected = Color.WHITE;
            skin.add("default", listStyle);
        }

        this.setScreen(new FarmScreen(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        if (getScreen() != null) {
            getScreen().dispose();
        }
        batch.dispose();
        font.dispose();
        skin.dispose();
        assets.dispose();
        shapeRenderer.dispose();
    }
}

// =================================================================================
// Asset Manager
// =================================================================================
class AssetManager implements Disposable {
    public Texture grassTexture, playerTexture, heartTexture, waterTexture, whitePixel, fedIconTexture;
    public Texture barnTexture, coopTexture, woodFenceTexture;
    public Texture cowTexture, chickenTexture, sheepTexture, pigTexture, goatTexture, duckTexture, rabbitTexture, dinosaurTexture;
    public Texture milkPailTexture, shearsTexture;
    public Texture milkTexture, eggTexture, woolTexture, truffleTexture, goatMilkTexture, duckEggTexture, rabbitFootTexture, dinosaurEggTexture;
    public Texture fishIconTexture, fishingHookTexture;


    // Animal Animations
    public Animation<TextureRegion> cowWalk, chickenWalk, sheepWalk, pigWalk, goatWalk, duckWalk, rabbitWalk, dinosaurWalk;

    public void load() {
        grassTexture = new Texture(Gdx.files.internal("grass.png"));
        waterTexture = new Texture(Gdx.files.internal("water.png"));
        heartTexture = new Texture(Gdx.files.internal("heart_icon.png"));
        fedIconTexture = new Texture(Gdx.files.internal("heart_icon.png")); // Re-using heart icon for fed visual
        whitePixel = new Texture(Gdx.files.internal("white_pixel.png"));
        playerTexture = new Texture(Gdx.files.internal("player.png"));

        barnTexture = new Texture(Gdx.files.internal("barn.png"));
        coopTexture = new Texture(Gdx.files.internal("coop.png"));
        woodFenceTexture = new Texture(Gdx.files.internal("wood_fence.png"));


        cowTexture = new Texture(Gdx.files.internal("cow.png"));
        chickenTexture = new Texture(Gdx.files.internal("chicken.png"));
        sheepTexture = new Texture(Gdx.files.internal("sheep.png"));
        pigTexture = new Texture(Gdx.files.internal("pig.png"));
        goatTexture = new Texture(Gdx.files.internal("goat.png"));
        duckTexture = new Texture(Gdx.files.internal("duck.png"));
        rabbitTexture = new Texture(Gdx.files.internal("rabbit.png"));
        dinosaurTexture = new Texture(Gdx.files.internal("dinosaur.png"));


        milkPailTexture = new Texture(Gdx.files.internal("milk_pail.png"));
        shearsTexture = new Texture(Gdx.files.internal("shears.png"));
        milkTexture = new Texture(Gdx.files.internal("milk.png"));
        eggTexture = new Texture(Gdx.files.internal("egg.png"));
        woolTexture = new Texture(Gdx.files.internal("wool.png"));
        truffleTexture = new Texture(Gdx.files.internal("truffle.png"));
        goatMilkTexture = new Texture(Gdx.files.internal("goat_milk.png"));
        duckEggTexture = new Texture(Gdx.files.internal("duck_egg.png"));
        rabbitFootTexture = new Texture(Gdx.files.internal("rabbit_foot.png"));
        dinosaurEggTexture = new Texture(Gdx.files.internal("dinosaur_egg.png"));


        fishIconTexture = new Texture(Gdx.files.internal("fish_icon.png"));
        fishingHookTexture = new Texture(Gdx.files.internal("fishing_hook.png"));


        // Simple animations for animals
        cowWalk = new Animation<>(0.5f, new TextureRegion(cowTexture));
        chickenWalk = new Animation<>(0.5f, new TextureRegion(chickenTexture));
        sheepWalk = new Animation<>(0.5f, new TextureRegion(sheepTexture));
        pigWalk = new Animation<>(0.5f, new TextureRegion(pigTexture));
        goatWalk = new Animation<>(0.5f, new TextureRegion(goatTexture));
        duckWalk = new Animation<>(0.5f, new TextureRegion(duckTexture));
        rabbitWalk = new Animation<>(0.5f, new TextureRegion(rabbitTexture));
        dinosaurWalk = new Animation<>(0.5f, new TextureRegion(dinosaurTexture));
    }

    @Override
    public void dispose() {
        grassTexture.dispose(); playerTexture.dispose(); heartTexture.dispose(); fedIconTexture.dispose();
        waterTexture.dispose(); barnTexture.dispose(); coopTexture.dispose(); woodFenceTexture.dispose();
        cowTexture.dispose(); chickenTexture.dispose(); sheepTexture.dispose(); pigTexture.dispose();
        goatTexture.dispose(); duckTexture.dispose(); rabbitTexture.dispose(); dinosaurTexture.dispose();
        milkPailTexture.dispose(); shearsTexture.dispose();
        milkTexture.dispose(); eggTexture.dispose(); woolTexture.dispose(); truffleTexture.dispose();
        goatMilkTexture.dispose(); duckEggTexture.dispose(); rabbitFootTexture.dispose(); dinosaurEggTexture.dispose();
        fishIconTexture.dispose(); fishingHookTexture.dispose();
        whitePixel.dispose();
    }
}

// =================================================================================
// Data Models
// =================================================================================

enum AnimalType { COW, CHICKEN, SHEEP, PIG, GOAT, DUCK, RABBIT, DINOSAUR }
enum BuildingType { BARN, COOP, FENCE }
enum ProduceQuality { NORMAL, SILVER, GOLD, IRIDIUM }
enum ToolType { MILK_PAIL, SHEARS }
enum ProduceType { MILK, EGG, WOOL, TRUFFLE, GOAT_MILK, DUCK_EGG, RABBIT_FOOT, DINOSAUR_EGG, FISH }

class Item {
    public String name;
    public int baseSellPrice;
    public String uniqueId;
    public Texture texture;

    public Item(String name, int baseSellPrice, Texture texture) {
        this.name = name;
        this.baseSellPrice = baseSellPrice;
        this.uniqueId = UUID.randomUUID().toString();
        this.texture = texture;
    }
}

class Produce extends Item {
    public ProduceType type;
    public ProduceQuality quality;
    public int actualSellPrice;

    public Produce(ProduceType type, ProduceQuality quality, int basePrice, Texture texture) {
        super(type.name() + (quality != ProduceQuality.NORMAL ? " (" + quality.name() + ")" : ""), basePrice, texture);
        this.type = type;
        this.quality = quality;
        this.actualSellPrice = calculateActualSellPrice(basePrice, quality);
    }

    private int calculateActualSellPrice(int basePrice, ProduceQuality quality) {
        switch (quality) {
            case SILVER: return (int) (basePrice * 1.25);
            case GOLD: return (int) (basePrice * 1.5);
            case IRIDIUM: return (int) (basePrice * 2.0);
            default: return basePrice;
        }
    }
}

class Tool extends Item {
    public ToolType type;
    public int energyCost;

    public Tool(ToolType type, String name, int baseSellPrice, int energyCost, Texture texture) {
        super(name, baseSellPrice, texture);
        this.type = type;
        this.energyCost = energyCost;
    }
}

class MilkPail extends Tool {
    public MilkPail(Texture texture) { super(ToolType.MILK_PAIL, "Milk Pail", 1000, 4, texture); }
}

class Shears extends Tool {
    public Shears(Texture texture) { super(ToolType.SHEARS, "Shears", 1000, 4, texture); }
}

class Player {
    public String playerName;
    public int money;
    public int wood;
    public Map<String, Integer> inventory; // Item name -> count
    public java.util.List<Tool> tools;
    public java.util.List<Building> buildings;
    public java.util.List<Animal> animals;
    public java.util.List<GameEntity> fences;
    public float energy = 200;
    public final float maxEnergy = 200;

    public Player(String name, int startingMoney) {
        this.playerName = name;
        this.money = startingMoney;
        this.wood = startingMoney;
        this.inventory = new HashMap<>();
        this.tools = new ArrayList<>();
        this.buildings = new ArrayList<>();
        this.animals = new ArrayList<>();
        this.fences = new ArrayList<>();
    }

    public void addProduce(Produce p) {
        inventory.put(p.name, inventory.getOrDefault(p.name, 0) + 1);
    }
    public void addTool(Tool t) { if (!hasTool(t.type)) { tools.add(t); } }
    public boolean hasTool(ToolType type) {
        for (Tool tool : tools) { if (tool.type == type) return true; }
        return false;
    }
    public void addAnimal(Animal animal) { this.animals.add(animal); }
    public void removeAnimal(Animal animal) { this.animals.remove(animal); }
    public void addBuilding(Building building) { this.buildings.add(building); }
    public void addFence(GameEntity fence) { this.fences.add(fence); }
}

// =================================================================================
// Game Entities
// =================================================================================

class GameEntity {
    public Vector2 position;
    public Rectangle bounds;
    protected Texture texture;
    protected Animation<TextureRegion> animation;
    protected float stateTime = 0f;
    protected boolean facingRight = true;

    public GameEntity(Animation<TextureRegion> anim, float x, float y, float width, float height) {
        this.animation = anim;
        this.position = new Vector2(x, y);
        this.bounds = new Rectangle(x, y, width, height);
    }

    public GameEntity(Texture tex, float x, float y, float width, float height) {
        this.texture = tex;
        this.position = new Vector2(x, y);
        this.bounds = new Rectangle(x, y, width, height);
    }

    public void render(SpriteBatch batch) {
        TextureRegion currentFrame = null;
        if (animation != null) {
            currentFrame = animation.getKeyFrame(stateTime, true);
        } else if (texture != null) {
            currentFrame = new TextureRegion(texture);
        }

        if (currentFrame != null) {
            if (!facingRight && !currentFrame.isFlipX()) {
                currentFrame.flip(true, false);
            } else if (facingRight && currentFrame.isFlipX()) {
                currentFrame.flip(true, false);
            }
            batch.draw(currentFrame, position.x, position.y, bounds.width, bounds.height);
        }
    }

    public void update(float delta) {
        stateTime += delta;
        bounds.setPosition(position.x, position.y);
    }
}

class PlayerCharacter extends GameEntity {
    private float speed = 250f;

    public PlayerCharacter(Texture texture, float x, float y) {
        super(texture, x, y, 48, 72);
    }

    public void update(float deltaTime) {
        super.update(deltaTime);
        if (Gdx.input.isKeyPressed(Input.Keys.W)) position.y += speed * deltaTime;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) position.y -= speed * deltaTime;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            position.x -= speed * deltaTime;
            facingRight = false;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            position.x += speed * deltaTime;
            facingRight = true;
        }
    }
}

abstract class Building extends GameEntity {
    protected String name;
    protected Array<Animal> housedAnimals = new Array<>();
    protected int capacity;
    public boolean isDoorOpen = false;
    public float alpha = 1.0f; // For transparency

    public Building(String name, Texture texture, float x, float y, float w, float h, int cap) {
        super(texture, x, y, w, h);
        this.name = name;
        this.capacity = cap;
    }

    public abstract boolean canHouse(AnimalType type);

    public void houseAnimal(Animal animal) {
        if (housedAnimals.size < capacity) {
            housedAnimals.add(animal);
            animal.setHome(this);
        }
    }

    public void toggleDoor() {
        isDoorOpen = !isDoorOpen;
        if (isDoorOpen) {
            for (Animal a : housedAnimals) {
                a.isOutside = true;
                a.position.set(this.position.x + this.bounds.width / 2, this.position.y - a.bounds.height);
            }
        }
    }
}

class Barn extends Building {
    public Barn(Texture texture, float x, float y) {
        super("Barn", texture, x, y, 160, 160, 8);
    }
    @Override public boolean canHouse(AnimalType type) {
        return housedAnimals.size < capacity &&
            (type == AnimalType.COW || type == AnimalType.SHEEP || type == AnimalType.PIG || type == AnimalType.GOAT);
    }
}

class Coop extends Building {
    public Coop(Texture texture, float x, float y) {
        super("Coop", texture, x, y, 160, 160, 4);
    }
    @Override public boolean canHouse(AnimalType type) {
        return housedAnimals.size < capacity &&
            (type == AnimalType.CHICKEN || type == AnimalType.DUCK || type == AnimalType.RABBIT || type == AnimalType.DINOSAUR);
    }
}

class Fence extends GameEntity {
    public Fence(Texture texture, float x, float y) {
        super(texture, x, y, 32, 32);
    }
}


abstract class Animal extends GameEntity {
    protected String animalName;
    protected AnimalType animalType;
    protected int friendship;
    protected boolean pettedToday;
    protected boolean fedToday = false;
    protected boolean hasUncollectedProduce;
    protected Random randomGenerator;
    private Vector2 targetPosition;
    private float speed = 40f;
    public boolean showHeart = false;
    private float heartTimer = 0;
    public boolean showFedIcon = false;
    private float fedIconTimer = 0;
    private Building homeBuilding = null;
    public boolean isOutside = false;
    private Texture produceIcon;
    private FarmScreen farmScreen; // Reference to the screen for player position

    public Animal(String name, AnimalType type, Animation<TextureRegion> anim, float x, float y, Texture produceIcon, FarmScreen screen) {
        super(anim, x, y, 64, 64);
        this.animalName = name;
        this.animalType = type;
        this.friendship = 0;
        this.pettedToday = false;
        this.hasUncollectedProduce = true;
        this.randomGenerator = new Random();
        this.targetPosition = new Vector2(x, y);
        this.produceIcon = produceIcon;
        this.farmScreen = screen;
    }

    public void setHome(Building building) {
        this.homeBuilding = building;
    }

    public String getName() { return animalName; }
    public AnimalType getType() { return animalType; }

    public void pet() {
        if (!pettedToday) {
            this.friendship = Math.min(1000, this.friendship + 15);
            this.pettedToday = true;
            this.showHeart = true;
            this.heartTimer = 1.5f;
        }
    }

    public void feed() {
        if (!fedToday) {
            this.fedToday = true;
            this.friendship = Math.min(1000, this.friendship + 10);
            this.showFedIcon = true;
            this.fedIconTimer = 1.5f;
        }
    }


    @Override
    public void update(float delta) {
        super.update(delta);
        if (showHeart) {
            heartTimer -= delta;
            if (heartTimer <= 0) showHeart = false;
        }
        if (showFedIcon) {
            fedIconTimer -= delta;
            if (fedIconTimer <= 0) showFedIcon = false;
        }

        // Return home if door is closed
        if (isOutside && homeBuilding != null && !homeBuilding.isDoorOpen) {
            targetPosition.set(homeBuilding.position.x + homeBuilding.bounds.width / 2, homeBuilding.position.y + homeBuilding.bounds.height / 2);
            if (bounds.overlaps(homeBuilding.bounds)) {
                isOutside = false;
            }
        }
        // Shepherding logic (follow player)
        else if (isOutside && farmScreen.getPlayerCharacter().position.dst(position) < 150f) {
            targetPosition.set(farmScreen.getPlayerCharacter().position);
        }
        // Normal random movement
        else if (position.dst(targetPosition) < 5f) {
            float newX, newY;
            if (homeBuilding != null && !isOutside) {
                newX = MathUtils.random(homeBuilding.position.x, homeBuilding.position.x + homeBuilding.bounds.width - bounds.width);
                newY = MathUtils.random(homeBuilding.position.y, homeBuilding.position.y + homeBuilding.bounds.height - bounds.height);
            } else if (homeBuilding != null && isOutside) {
                float radius = 250f;
                newX = MathUtils.clamp(homeBuilding.position.x + homeBuilding.bounds.width/2 + MathUtils.random(-radius, radius), 0, FarmScreen.WORLD_WIDTH - bounds.width);
                newY = MathUtils.clamp(homeBuilding.position.y + homeBuilding.bounds.height/2 + MathUtils.random(-radius, radius), 0, FarmScreen.WORLD_HEIGHT - bounds.height);
            } else {
                newX = MathUtils.clamp(position.x + MathUtils.random(-150, 150), 0, FarmScreen.WORLD_WIDTH - bounds.width);
                newY = MathUtils.clamp(position.y + MathUtils.random(-150, 150), 0, FarmScreen.WORLD_HEIGHT - bounds.height);
            }
            targetPosition.set(newX, newY);
        }

        // Move towards target
        Vector2 direction = targetPosition.cpy().sub(position).nor();
        position.mulAdd(direction, speed * delta);
        facingRight = direction.x > 0;
    }

    @Override
    public void render(SpriteBatch batch) {
        super.render(batch);
        // Show produce icon when ready
        if (hasUncollectedProduce && produceIcon != null) {
            batch.draw(produceIcon, position.x + bounds.width / 2 - 16, position.y + bounds.height, 32, 32);
        }
    }

    protected ProduceQuality determineProduceQuality() {
        double score = (this.friendship / 1000.0) * (0.5 + 0.5 * randomGenerator.nextDouble());
        if (score > 0.9) return ProduceQuality.IRIDIUM;
        if (score > 0.7) return ProduceQuality.GOLD;
        if (score > 0.5) return ProduceQuality.SILVER;
        return ProduceQuality.NORMAL;
    }

    public int getSellPrice() {
        int basePrice = 1000;
        return (int) (basePrice * ((getFriendship() / 1000.0) + 0.3));
    }

    public int getFriendship() { return friendship; }

    public abstract Produce collectProduce(Player player, AssetManager assets);
    public abstract ToolType getRequiredTool();
}

class Cow extends Animal {
    public Cow(String name, Main game, float x, float y, FarmScreen screen) { super(name, AnimalType.COW, game.assets.cowWalk, x, y, game.assets.milkTexture, screen); }
    @Override public ToolType getRequiredTool() { return ToolType.MILK_PAIL; }
    @Override public Produce collectProduce(Player p, AssetManager assets) {
        if (!hasUncollectedProduce || !p.hasTool(ToolType.MILK_PAIL)) return null;
        hasUncollectedProduce = false;
        this.friendship = Math.min(1000, this.friendship + 5);
        return new Produce(ProduceType.MILK, determineProduceQuality(), 125, assets.milkTexture);
    }
}

class Chicken extends Animal {
    public Chicken(String name, Main game, float x, float y, FarmScreen screen) { super(name, AnimalType.CHICKEN, game.assets.chickenWalk, x, y, game.assets.eggTexture, screen); }
    @Override public ToolType getRequiredTool() { return null; }
    @Override public Produce collectProduce(Player p, AssetManager assets) {
        if (!hasUncollectedProduce) return null;
        hasUncollectedProduce = false;
        return new Produce(ProduceType.EGG, determineProduceQuality(), 50, assets.eggTexture);
    }
}

class Sheep extends Animal {
    public Sheep(String name, Main game, float x, float y, FarmScreen screen) { super(name, AnimalType.SHEEP, game.assets.sheepWalk, x, y, game.assets.woolTexture, screen); }
    @Override public ToolType getRequiredTool() { return ToolType.SHEARS; }
    @Override public Produce collectProduce(Player p, AssetManager assets) {
        if (!hasUncollectedProduce || !p.hasTool(ToolType.SHEARS)) return null;
        hasUncollectedProduce = false;
        this.friendship = Math.min(1000, this.friendship + 5);
        return new Produce(ProduceType.WOOL, determineProduceQuality(), 340, assets.woolTexture);
    }
}

class Pig extends Animal {
    public Pig(String name, Main game, float x, float y, FarmScreen screen) { super(name, AnimalType.PIG, game.assets.pigWalk, x, y, game.assets.truffleTexture, screen); }
    @Override public ToolType getRequiredTool() { return null; }
    @Override public Produce collectProduce(Player p, AssetManager assets) {
        if (!hasUncollectedProduce) return null;
        hasUncollectedProduce = false;
        return new Produce(ProduceType.TRUFFLE, determineProduceQuality(), 625, assets.truffleTexture);
    }
}

class Goat extends Animal {
    public Goat(String name, Main game, float x, float y, FarmScreen screen) { super(name, AnimalType.GOAT, game.assets.goatWalk, x, y, game.assets.goatMilkTexture, screen); }
    @Override public ToolType getRequiredTool() { return ToolType.MILK_PAIL; }
    @Override public Produce collectProduce(Player p, AssetManager assets) {
        if (!hasUncollectedProduce || !p.hasTool(ToolType.MILK_PAIL)) return null;
        hasUncollectedProduce = false;
        this.friendship = Math.min(1000, this.friendship + 5);
        return new Produce(ProduceType.GOAT_MILK, determineProduceQuality(), 225, assets.goatMilkTexture);
    }
}

class Duck extends Animal {
    public Duck(String name, Main game, float x, float y, FarmScreen screen) { super(name, AnimalType.DUCK, game.assets.duckWalk, x, y, game.assets.duckEggTexture, screen); }
    @Override public ToolType getRequiredTool() { return null; }
    @Override public Produce collectProduce(Player p, AssetManager assets) {
        if (!hasUncollectedProduce) return null;
        hasUncollectedProduce = false;
        return new Produce(ProduceType.DUCK_EGG, determineProduceQuality(), 95, assets.duckEggTexture);
    }
}

class Rabbit extends Animal {
    public Rabbit(String name, Main game, float x, float y, FarmScreen screen) { super(name, AnimalType.RABBIT, game.assets.rabbitWalk, x, y, game.assets.rabbitFootTexture, screen); }
    @Override public ToolType getRequiredTool() { return null; }
    @Override public Produce collectProduce(Player p, AssetManager assets) {
        if (!hasUncollectedProduce) return null;
        hasUncollectedProduce = false;
        return new Produce(ProduceType.RABBIT_FOOT, determineProduceQuality(), 565, assets.rabbitFootTexture);
    }
}

class Dinosaur extends Animal {
    public Dinosaur(String name, Main game, float x, float y, FarmScreen screen) { super(name, AnimalType.DINOSAUR, game.assets.dinosaurWalk, x, y, game.assets.dinosaurEggTexture, screen); }
    @Override public ToolType getRequiredTool() { return null; }
    @Override public Produce collectProduce(Player p, AssetManager assets) {
        if (!hasUncollectedProduce) return null;
        hasUncollectedProduce = false;
        return new Produce(ProduceType.DINOSAUR_EGG, determineProduceQuality(), 350, assets.dinosaurEggTexture);
    }
}


// =================================================================================
// Screens and UI
// =================================================================================

class FarmScreen implements Screen {
    private final Main game;
    private final OrthographicCamera gameCamera;
    private final Viewport gameViewport;
    private PlayerCharacter playerCharacter;
    private final Stage stage;
    public final HUD hud;
    private AnimalContextMenu animalContextMenu;
    private LivestockManagementWindow livestockWindow;
    private AnimalShopWindow animalShopWindow;
    private BuildShopWindow buildShopWindow;
    private InventoryWindow inventoryWindow;
    private InputMultiplexer inputMultiplexer;
    private Array<GameEntity> waterFish;


    // Map dimensions
    public static final float WORLD_WIDTH = 4096;
    public static final float WORLD_HEIGHT = 2304;
    private Rectangle waterArea;

    // Game state variables
    private boolean isBuildMode = false;
    private String buildItemType = "";
    private boolean isPlacingAnimal = false;
    private AnimalType animalToPlace;
    private boolean isFishing = false;
    private long fishingStartTime;


    public FarmScreen(Main game) {
        this.game = game;
        gameCamera = new OrthographicCamera();
        gameViewport = new FitViewport(1280, 720, gameCamera);
        stage = new Stage(new FitViewport(1280, 720), game.batch);

        playerCharacter = new PlayerCharacter(game.assets.playerTexture, 400, 400);
        waterArea = new Rectangle(WORLD_WIDTH - 500, 100, 450, WORLD_HEIGHT - 200);

        game.player.addTool(new MilkPail(game.assets.milkPailTexture));
        game.player.addTool(new Shears(game.assets.shearsTexture));

        hud = new HUD(game, this);
        stage.addActor(hud.getTable());

        animalContextMenu = new AnimalContextMenu(game);
        stage.addActor(animalContextMenu);

        livestockWindow = new LivestockManagementWindow("Livestock Management", game.skin, game);
        stage.addActor(livestockWindow);
        livestockWindow.setVisible(false);

        animalShopWindow = new AnimalShopWindow("Animal Shop", game.skin, game, this);
        stage.addActor(animalShopWindow);
        animalShopWindow.setVisible(false);

        buildShopWindow = new BuildShopWindow("Build Menu", game.skin, game, this);
        stage.addActor(buildShopWindow);
        buildShopWindow.setVisible(false);

        inventoryWindow = new InventoryWindow("Inventory", game.skin, game);
        stage.addActor(inventoryWindow);
        inventoryWindow.setVisible(false);

        waterFish = new Array<>();
        for (int i = 0; i < 10; i++) {
            float x = MathUtils.random(waterArea.x, waterArea.x + waterArea.width - 64);
            float y = MathUtils.random(waterArea.y, waterArea.y + waterArea.height - 64);
            waterFish.add(new GameEntity(game.assets.fishIconTexture, x, y, 64, 64));
        }


        setupInputProcessor();
    }

    public PlayerCharacter getPlayerCharacter() { return playerCharacter; }

    public void startPlacingAnimal(AnimalType type) {
        isPlacingAnimal = true;
        animalToPlace = type;
        animalShopWindow.setVisible(false);
    }

    public void enterBuildMode(String itemType) {
        isBuildMode = true;
        buildItemType = itemType;
        buildShopWindow.setVisible(false);
    }

    private void setupInputProcessor() {
        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(stage);
        inputMultiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Vector3 mousePos = new Vector3(screenX, screenY, 0);
                gameCamera.unproject(mousePos);

                if (button == Input.Buttons.LEFT) {
                    // Priority 1: Placing an animal
                    if (isPlacingAnimal) {
                        placeAnimal(mousePos.x, mousePos.y);
                        return true;
                    }

                    // Priority 2: Placing a building/fence
                    if (isBuildMode) {
                        placeBlueprint(mousePos.x, mousePos.y);
                        return true;
                    }

                    // Priority 3: Interacting with buildings (toggling door)
                    for (Building b : game.player.buildings) {
                        if (b.bounds.contains(mousePos.x, mousePos.y)) {
                            b.toggleDoor();
                            hud.showMessage(b.name + " door " + (b.isDoorOpen ? "opened." : "closed."), 2);
                            return true;
                        }
                    }
                }

                if (button == Input.Buttons.RIGHT) {
                    if (isBuildMode || isPlacingAnimal) {
                        isBuildMode = false;
                        isPlacingAnimal = false;
                        return true;
                    }
                    if (!animalContextMenu.isVisible()) {
                        for (Animal animal : game.player.animals) {
                            if (animal.bounds.contains(mousePos.x, mousePos.y)) {
                                animalContextMenu.show(animal, Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
                                return true;
                            }
                        }
                    }
                }
                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                boolean uiVisible = livestockWindow.isVisible() || animalShopWindow.isVisible() || buildShopWindow.isVisible() || animalContextMenu.isVisible() || inventoryWindow.isVisible();

                if (keycode == Input.Keys.ESCAPE) {
                    if (animalContextMenu.isVisible()) animalContextMenu.setVisible(false);
                    else if (livestockWindow.isVisible()) livestockWindow.setVisible(false);
                    else if (animalShopWindow.isVisible()) animalShopWindow.setVisible(false);
                    else if (buildShopWindow.isVisible()) buildShopWindow.setVisible(false);
                    else if (inventoryWindow.isVisible()) inventoryWindow.setVisible(false);
                    isBuildMode = false;
                    isPlacingAnimal = false;
                    isFishing = false;
                    return true;
                }

                if (uiVisible) {
                    return false;
                }

                switch(keycode) {
                    case Input.Keys.L:
                        livestockWindow.updateList();
                        livestockWindow.setVisible(true);
                        return true;
                    case Input.Keys.P:
                        animalShopWindow.setVisible(true);
                        return true;
                    case Input.Keys.B:
                        buildShopWindow.setVisible(true);
                        return true;
                    case Input.Keys.F:
                        startFishing();
                        return true;
                    case Input.Keys.I:
                        inventoryWindow.updateList();
                        inventoryWindow.setVisible(true);
                        return true;
                }
                return false;
            }
        });
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    private void placeBlueprint(float mouseX, float mouseY) {
        float snappedX = snapToGrid(mouseX);
        float snappedY = snapToGrid(mouseY);

        int woodCost = 0;
        int moneyCost = 0;
        Rectangle bounds = new Rectangle();

        switch(buildItemType) {
            case "fence":
                woodCost = 5;
                bounds.set(snappedX - 2.5f * 32, snappedY - 2.5f * 32, 5 * 32, 5 * 32);
                break;
            case "barn":
                woodCost = 350;
                moneyCost = 6000;
                bounds.set(snappedX, snappedY, 160, 160);
                break;
            case "coop":
                woodCost = 300;
                moneyCost = 4000;
                bounds.set(snappedX, snappedY, 160, 160);
                break;
        }

        if (game.player.wood < woodCost || game.player.money < moneyCost) {
            hud.showMessage("Not enough resources!", 3);
            return;
        }

        if (isPlacementValid(bounds)) {
            game.player.wood -= woodCost;
            game.player.money -= moneyCost;

            if (buildItemType.equals("fence")) {
                for (int i = -2; i <= 2; i++) {
                    for (int j = -2; j <= 2; j++) {
                        if (Math.abs(i) == 2 || Math.abs(j) == 2) {
                            game.player.addFence(new Fence(game.assets.woodFenceTexture, snappedX + i * 32, snappedY + j * 32));
                        }
                    }
                }
            } else if (buildItemType.equals("barn")) {
                game.player.addBuilding(new Barn(game.assets.barnTexture, snappedX, snappedY));
            } else if (buildItemType.equals("coop")) {
                game.player.addBuilding(new Coop(game.assets.coopTexture, snappedX, snappedY));
            }

            hud.showMessage(buildItemType + " built!", 2);
            isBuildMode = false;
        } else {
            hud.showMessage("Invalid placement location!", 2);
        }
    }

    private void placeAnimal(float mouseX, float mouseY) {
        for (Building b : game.player.buildings) {
            if (b.bounds.contains(mouseX, mouseY)) {
                if (b.canHouse(animalToPlace)) {
                    Animal newAnimal = null;
                    String animalName = animalToPlace.toString();
                    float spawnX = b.position.x + b.bounds.width / 2;
                    float spawnY = b.position.y + b.bounds.height / 2;

                    switch(animalToPlace) {
                        case COW: newAnimal = new Cow(animalName, game, spawnX, spawnY, this); break;
                        case CHICKEN: newAnimal = new Chicken(animalName, game, spawnX, spawnY, this); break;
                        case SHEEP: newAnimal = new Sheep(animalName, game, spawnX, spawnY, this); break;
                        case PIG: newAnimal = new Pig(animalName, game, spawnX, spawnY, this); break;
                        case GOAT: newAnimal = new Goat(animalName, game, spawnX, spawnY, this); break;
                        case DUCK: newAnimal = new Duck(animalName, game, spawnX, spawnY, this); break;
                        case RABBIT: newAnimal = new Rabbit(animalName, game, spawnX, spawnY, this); break;
                        case DINOSAUR: newAnimal = new Dinosaur(animalName, game, spawnX, spawnY, this); break;
                    }
                    if (newAnimal != null) {
                        game.player.addAnimal(newAnimal);
                        b.houseAnimal(newAnimal);
                        hud.showMessage(newAnimal.getName() + " was placed in its new home!", 3);
                        isPlacingAnimal = false;
                        return;
                    }
                } else {
                    hud.showMessage("This building is full or not suitable for this animal!", 3);
                    return;
                }
            }
        }
        hud.showMessage("You must place the animal inside a suitable building.", 3);
    }

    private void startFishing() {
        if (isFishing) return;
        if (playerCharacter.bounds.overlaps(waterArea)) {
            isFishing = true;
            fishingStartTime = TimeUtils.millis();
            hud.showMessage("Fishing...", 5);
        } else {
            hud.showMessage("You need to be near water to fish!", 2);
        }
    }

    private void checkFishing() {
        if (isFishing) {
            long elapsedTime = TimeUtils.timeSinceMillis(fishingStartTime);
            if (elapsedTime > MathUtils.random(3000, 5000)) {
                isFishing = false;
                Produce fish = new Produce(ProduceType.FISH, ProduceQuality.NORMAL, 50, game.assets.fishIconTexture);
                game.player.addProduce(fish);
                hud.showMessage("You caught a fish! (Added to inventory)", 3);
            }
        }
    }

    private float snapToGrid(float val) {
        return Math.round(val / 32f) * 32f;
    }

    private boolean isPlacementValid(Rectangle newBounds) {
        if (waterArea.overlaps(newBounds)) return false;
        for (Building b : game.player.buildings) if (b.bounds.overlaps(newBounds)) return false;
        for (GameEntity f : game.player.fences) if (f.bounds.overlaps(newBounds)) return false;
        return true;
    }


    public void update(float deltaTime) {
        if (! (livestockWindow.isVisible() || animalShopWindow.isVisible() || buildShopWindow.isVisible() || inventoryWindow.isVisible())) {
            playerCharacter.update(deltaTime);
        }
        checkFishing();

        for (Animal animal : game.player.animals) {
            animal.update(deltaTime);
        }

        // Update building transparency
        for (Building b : game.player.buildings) {
            if (playerCharacter.position.dst(b.position.x + b.bounds.width/2, b.position.y + b.bounds.height/2) < 200f) {
                b.alpha = Math.max(0.4f, b.alpha - deltaTime * 2f);
            } else {
                b.alpha = Math.min(1.0f, b.alpha + deltaTime * 2f);
            }
        }

        gameCamera.position.x = MathUtils.clamp(playerCharacter.position.x, gameViewport.getWorldWidth() / 2, WORLD_WIDTH - gameViewport.getWorldWidth() / 2);
        gameCamera.position.y = MathUtils.clamp(playerCharacter.position.y, gameViewport.getWorldHeight() / 2, WORLD_HEIGHT - gameViewport.getWorldHeight() / 2);
        gameCamera.update();

        stage.act(deltaTime);
        hud.update();
    }

    @Override
    public void render(float delta) {
        update(delta);
        Gdx.gl.glClearColor(0.2f, 0.6f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.setProjectionMatrix(gameCamera.combined);
        game.batch.begin();

        // Render ground layer
        for (float x = 0; x < WORLD_WIDTH; x += game.assets.grassTexture.getWidth()) {
            for (float y = 0; y < WORLD_HEIGHT; y += game.assets.grassTexture.getHeight()) {
                game.batch.draw(game.assets.grassTexture, x, y);
            }
        }
        game.batch.draw(game.assets.waterTexture, waterArea.x, waterArea.y, waterArea.width, waterArea.height);
        for(GameEntity fish : waterFish) {
            fish.render(game.batch);
        }
        for (GameEntity f : game.player.fences) f.render(game.batch);

        // Render animals and player
        for (Animal animal : game.player.animals) animal.render(game.batch);
        playerCharacter.render(game.batch);

        // Render buildings with transparency
        for (Building b : game.player.buildings) {
            Color c = game.batch.getColor();
            game.batch.setColor(c.r, c.g, c.b, b.alpha);
            b.render(game.batch);
            game.batch.setColor(c.r, c.g, c.b, 1f); // Reset alpha
        }

        // Render icons on top of everything
        for (Animal animal : game.player.animals) {
            if (animal.showHeart) {
                game.batch.draw(game.assets.heartTexture, animal.position.x + 16, animal.position.y + 64, 32, 32);
            }
            if (animal.showFedIcon) {
                game.batch.setColor(Color.GREEN); // Tint fed icon to green
                game.batch.draw(game.assets.fedIconTexture, animal.position.x + 16, animal.position.y + 96, 32, 32);
                game.batch.setColor(Color.WHITE); // Reset color
            }
        }

        // Render open door indicator
        for (Building b : game.player.buildings) {
            if (b.isDoorOpen) {
                game.batch.setColor(Color.BLACK);
                game.batch.draw(game.assets.whitePixel, b.position.x + b.bounds.width/2 - 20, b.position.y, 40, 10);
                game.batch.setColor(Color.WHITE);
            }
        }

        if (isFishing) {
            game.batch.draw(game.assets.fishingHookTexture, playerCharacter.position.x + 24, playerCharacter.position.y - 32);
        }

        game.batch.end();

        // Render build blueprint
        if (isBuildMode || isPlacingAnimal) {
            Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            gameCamera.unproject(mousePos);

            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            if (isBuildMode) {
                float snappedX = snapToGrid(mousePos.x);
                float snappedY = snapToGrid(mousePos.y);
                Texture blueprintTexture = null;
                Rectangle bounds = new Rectangle();
                switch(buildItemType) {
                    case "fence":
                        blueprintTexture = game.assets.woodFenceTexture;
                        bounds.set(snappedX - 2.5f * 32, snappedY - 2.5f * 32, 5 * 32, 5 * 32);
                        break;
                    case "barn":
                        blueprintTexture = game.assets.barnTexture;
                        bounds.set(snappedX, snappedY, 160, 160);
                        break;
                    case "coop":
                        blueprintTexture = game.assets.coopTexture;
                        bounds.set(snappedX, snappedY, 160, 160);
                        break;
                }

                boolean isValid = isPlacementValid(bounds);
                game.shapeRenderer.setProjectionMatrix(gameCamera.combined);
                game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                game.shapeRenderer.setColor(isValid ? 0f : 1f, isValid ? 1f : 0f, 0f, 0.5f);
                game.shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
                game.shapeRenderer.end();

                if (blueprintTexture != null && !buildItemType.equals("fence")) {
                    game.batch.setProjectionMatrix(gameCamera.combined);
                    game.batch.begin();
                    game.batch.setColor(1,1,1,0.7f);
                    game.batch.draw(blueprintTexture, bounds.x, bounds.y, bounds.width, bounds.height);
                    game.batch.setColor(1,1,1,1);
                    game.batch.end();
                }
            }

            if (isPlacingAnimal) {
                game.batch.setProjectionMatrix(gameCamera.combined);
                game.batch.begin();
                Texture animalTex = null;
                switch(animalToPlace) {
                    case COW: animalTex = game.assets.cowTexture; break;
                    case CHICKEN: animalTex = game.assets.chickenTexture; break;
                    case SHEEP: animalTex = game.assets.sheepTexture; break;
                    case PIG: animalTex = game.assets.pigTexture; break;
                    case GOAT: animalTex = game.assets.goatTexture; break;
                    case DUCK: animalTex = game.assets.duckTexture; break;
                    case RABBIT: animalTex = game.assets.rabbitTexture; break;
                    case DINOSAUR: animalTex = game.assets.dinosaurTexture; break;
                }
                if (animalTex != null) {
                    game.batch.setColor(1,1,1,0.7f);
                    game.batch.draw(animalTex, mousePos.x - 32, mousePos.y - 32, 64, 64);
                    game.batch.setColor(1,1,1,1);
                }
                game.batch.end();
            }
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }


        stage.getViewport().apply();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, true);
        stage.getViewport().update(width, height, true);
    }

    @Override public void show() { Gdx.input.setInputProcessor(inputMultiplexer); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { Gdx.input.setInputProcessor(null); }
    @Override public void dispose() { stage.dispose(); }
}

class HUD {
    private Table table;
    private Label moneyLabel, woodLabel, energyLabel, messageLabel;
    private Main game;
    private float messageTimer = 0;

    public HUD(Main game, FarmScreen screen) {
        this.game = game;
        table = new Table();
        table.top().left();
        table.setFillParent(true);

        moneyLabel = new Label("", game.skin);
        woodLabel = new Label("", game.skin);
        energyLabel = new Label("", game.skin);
        messageLabel = new Label("", game.skin);

        table.add(moneyLabel).pad(10).left().row();
        table.add(woodLabel).pad(10).left().row();
        table.add(energyLabel).pad(10).left().row();
        table.add(messageLabel).pad(10).left().expandX().row();

        Table keybinds = new Table();
        keybinds.add(new Label("I: Inventory", game.skin)).left().row();
        keybinds.add(new Label("L: Livestock Menu", game.skin)).left().row();
        keybinds.add(new Label("P: Animal Shop", game.skin)).left().row();
        keybinds.add(new Label("B: Build Menu", game.skin)).left().row();
        keybinds.add(new Label("F: Fishing", game.skin)).left().row();
        keybinds.add(new Label("Click on Building: Toggle Door", game.skin)).left().row();
        table.add(keybinds).left().pad(10).bottom().expandY();
    }

    public void update() {
        moneyLabel.setText("Money: " + game.player.money + "g");
        woodLabel.setText("Wood: " + game.player.wood);
        energyLabel.setText("Energy: " + (int)game.player.energy);

        if (messageTimer > 0) {
            messageTimer -= Gdx.graphics.getDeltaTime();
            if (messageTimer <= 0) {
                messageLabel.setText("");
            }
        }
    }

    public void showMessage(String message, float duration) {
        messageLabel.setText(message);
        messageTimer = duration;
    }

    public Table getTable() {
        return table;
    }
}

class AnimalContextMenu extends Window {
    private Animal currentAnimal;
    private Main game;

    public AnimalContextMenu(Main game) {
        super("", game.skin);
        this.game = game;
        setVisible(false);
        setMovable(false);
    }

    public void show(Animal animal, float screenX, float screenY) {
        this.currentAnimal = animal;
        clearChildren();

        getTitleLabel().setText(animal.getName() + " (Friendship: " + animal.getFriendship() + ")");

        TextButton petButton = new TextButton("Pet", game.skin);
        petButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                currentAnimal.pet();
                ((FarmScreen)game.getScreen()).hud.showMessage(currentAnimal.getName() + " petted!", 2);
                setVisible(false);
            }
        });
        add(petButton).row();

        TextButton feedButton = new TextButton("Feed", game.skin);
        if (currentAnimal.fedToday) feedButton.setDisabled(true);
        feedButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                currentAnimal.feed();
                ((FarmScreen)game.getScreen()).hud.showMessage("Fed " + currentAnimal.getName(), 2);
                setVisible(false);
            }
        });
        add(feedButton).row();

        TextButton collectButton = new TextButton("Collect Produce", game.skin);
        if (!currentAnimal.hasUncollectedProduce) collectButton.setDisabled(true);
        collectButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (currentAnimal.getRequiredTool() != null && !game.player.hasTool(currentAnimal.getRequiredTool())) {
                    ((FarmScreen)game.getScreen()).hud.showMessage("Required tool missing: " + currentAnimal.getRequiredTool().name(), 3);
                } else {
                    Produce p = currentAnimal.collectProduce(game.player, game.assets);
                    if (p != null) {
                        game.player.addProduce(p);
                        ((FarmScreen)game.getScreen()).hud.showMessage("Collected " + p.name, 3);
                    } else {
                        ((FarmScreen)game.getScreen()).hud.showMessage("Nothing to collect or tool missing.", 3);
                    }
                }
                setVisible(false);
            }
        });
        add(collectButton).row();

        TextButton sellButton = new TextButton("Sell", game.skin);
        sellButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int price = currentAnimal.getSellPrice();
                game.player.money += price;
                game.player.removeAnimal(currentAnimal);
                ((FarmScreen)game.getScreen()).hud.showMessage("Sold " + currentAnimal.getName() + " for " + price + "g", 3);
                setVisible(false);
            }
        });
        add(sellButton).row();

        pack();
        setPosition(screenX, screenY - getHeight());
        setVisible(true);
    }
}

class LivestockManagementWindow extends Window {
    private Main game;
    private com.badlogic.gdx.scenes.scene2d.ui.List<String> animalList;
    private ScrollPane scrollPane;

    public LivestockManagementWindow(String title, Skin skin, Main game) {
        super(title, skin);
        this.game = game;
        setMovable(true);

        animalList = new com.badlogic.gdx.scenes.scene2d.ui.List<>(skin);
        scrollPane = new ScrollPane(animalList, skin);
        scrollPane.setFadeScrollBars(false);

        add(scrollPane).width(300).height(200).pad(10);
        row();
        TextButton closeButton = new TextButton("Close", skin);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setVisible(false);
            }
        });
        add(closeButton).pad(10);

        pack();
        setPosition(Gdx.graphics.getWidth() / 2f - getWidth() / 2f, Gdx.graphics.getHeight() / 2f - getHeight() / 2f);
    }

    public void updateList() {
        Array<String> animalInfoGdxArray = new Array<>();
        for (Animal a : game.player.animals) {
            String status = (a.isOutside ? "Outside" : "Inside") + (a.fedToday ? ", Fed" : ", Hungry");
            String info = a.getName() + " (" + a.getType() + ") - F:" + a.getFriendship() + " ["+ status +"]";
            animalInfoGdxArray.add(info);
        }
        animalList.setItems(animalInfoGdxArray);
    }
}

class AnimalShopWindow extends Window {
    private Main game;
    private FarmScreen farmScreen;

    public AnimalShopWindow(String title, Skin skin, Main game, FarmScreen screen) {
        super(title, skin);
        this.game = game;
        this.farmScreen = screen;
        setMovable(true);

        Table table = new Table();
        // Barn Animals
        table.add(new Label("--- Barn Animals ---", skin)).colspan(2).pad(10).row();
        addAnimalButton(table, "Cow (1500g)", AnimalType.COW, 1500);
        addAnimalButton(table, "Goat (4000g)", AnimalType.GOAT, 4000);
        addAnimalButton(table, "Sheep (8000g)", AnimalType.SHEEP, 8000);
        addAnimalButton(table, "Pig (16000g)", AnimalType.PIG, 16000);

        // Coop Animals
        table.add(new Label("--- Coop Animals ---", skin)).colspan(2).pad(10).row();
        addAnimalButton(table, "Chicken (800g)", AnimalType.CHICKEN, 800);
        addAnimalButton(table, "Duck (1200g)", AnimalType.DUCK, 1200);
        addAnimalButton(table, "Rabbit (8000g)", AnimalType.RABBIT, 8000);
        addAnimalButton(table, "Dinosaur (14000g)", AnimalType.DINOSAUR, 14000);

        add(table);
        row();
        TextButton closeButton = new TextButton("Close", skin);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setVisible(false);
            }
        });
        add(closeButton).pad(10);

        pack();
        setPosition(Gdx.graphics.getWidth() / 2f - getWidth() / 2f, Gdx.graphics.getHeight() / 2f - getHeight() / 2f);
    }

    private void addAnimalButton(Table table, String text, final AnimalType type, final int price) {
        TextButton button = new TextButton(text, game.skin);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (game.player.money >= price) {
                    game.player.money -= price;
                    farmScreen.startPlacingAnimal(type);
                } else {
                    farmScreen.hud.showMessage("Not enough money!", 2);
                }
            }
        });
        table.add(button).pad(5);
        if (table.getCells().size % 2 == 0) table.row();
    }
}

class BuildShopWindow extends Window {
    private Main game;
    private FarmScreen farmScreen;

    public BuildShopWindow(String title, Skin skin, Main game, FarmScreen screen) {
        super(title, skin);
        this.game = game;
        this.farmScreen = screen;
        setMovable(true);

        addBuildButton("Build Barn (6000g, 350w)", "barn", 6000, 350);
        addBuildButton("Build Coop (4000g, 300w)", "coop", 4000, 300);
        addBuildButton("Build Fence (5w)", "fence", 0, 5);
        row();
        TextButton closeButton = new TextButton("Close", skin);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setVisible(false);
            }
        });
        add(closeButton).colspan(3).pad(10);

        pack();
        setPosition(Gdx.graphics.getWidth() / 2f - getWidth() / 2f, Gdx.graphics.getHeight() / 2f - getHeight() / 2f);
    }

    private void addBuildButton(String text, final String itemType, final int moneyCost, final int woodCost) {
        TextButton button = new TextButton(text, game.skin);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (game.player.money >= moneyCost && game.player.wood >= woodCost) {
                    farmScreen.enterBuildMode(itemType);
                } else {
                    farmScreen.hud.showMessage("Not enough resources!", 2);
                }
            }
        });
        add(button).pad(5);
    }
}

class InventoryWindow extends Window {
    private Main game;
    private com.badlogic.gdx.scenes.scene2d.ui.List<String> itemList;

    public InventoryWindow(String title, Skin skin, Main game) {
        super(title, skin);
        this.game = game;
        setMovable(true);

        itemList = new com.badlogic.gdx.scenes.scene2d.ui.List<>(skin);
        ScrollPane scrollPane = new ScrollPane(itemList, skin);
        add(scrollPane).width(300).height(200).pad(10);
        row();
        TextButton closeButton = new TextButton("Close", skin);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setVisible(false);
            }
        });
        add(closeButton).pad(10);

        pack();
        setPosition(Gdx.graphics.getWidth() / 2f - getWidth() / 2f, Gdx.graphics.getHeight() / 2f - getHeight() / 2f);
    }

    public void updateList() {
        Array<String> items = new Array<>();
        for (Map.Entry<String, Integer> entry : game.player.inventory.entrySet()) {
            items.add(entry.getKey() + " x" + entry.getValue());
        }
        itemList.setItems(items);
    }
}
