package com.mygame.stardew;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// ===================================================================================
// Main Game Class - Entry Point
// ===================================================================================
public class Main extends Game {
    public Skin skin;
    public TextureAtlas atlas;

    @Override
    public void create() {
        try {
            atlas = new TextureAtlas(Gdx.files.internal("uiskin.atlas"));
            skin = new Skin(Gdx.files.internal("uiskin.json"), atlas);

            if (skin.has("default-font", BitmapFont.class) && !skin.has("default-fnt", BitmapFont.class)) {
                skin.add("default-fnt", skin.getFont("default-font"));
            }

            if (skin.getDrawable("white") == null) {
                Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
                pixmap.setColor(Color.WHITE);
                pixmap.fill();
                skin.add("white", new Texture(pixmap));
                pixmap.dispose();
            }

            GameState.initialize();
            setScreen(new GameScreen(this, skin));

        } catch (Throwable t) {
            Gdx.app.error("AssetLoading", "A critical error occurred, showing error screen.", t);
            setScreen(new ErrorScreen(t));
        }
    }

    @Override
    public void dispose() {
        if (getScreen() != null) getScreen().dispose();
        if (skin != null) skin.dispose();
        if (atlas != null) atlas.dispose();
    }
}

// ===================================================================================
// Error Screen
// ===================================================================================
class ErrorScreen implements Screen {
    private final Stage stage;
    private final Skin errorSkin;

    public ErrorScreen(Throwable t) {
        stage = new Stage(new ScreenViewport());
        this.errorSkin = new Skin();
        errorSkin.add("default", new BitmapFont());
        Label.LabelStyle labelStyle = new Label.LabelStyle(errorSkin.getFont("default"), Color.RED);
        errorSkin.add("default", labelStyle);

        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();

        Label errorLabel = new Label("FATAL ERROR:\n" + t.getClass().getSimpleName() + "\n\n" + t.getMessage() + "\n\nCheck console for full stack trace.", errorSkin);
        errorLabel.setWrap(true);
        errorLabel.setAlignment(Align.center);

        Table table = new Table();
        table.setFillParent(true);
        table.add(errorLabel).width(Gdx.graphics.getWidth() * 0.9f);

        stage.addActor(table);
        Gdx.app.error("FATAL", exceptionAsString);
    }

    @Override
    public void show() { Gdx.input.setInputProcessor(stage); }
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }
    @Override
    public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void hide() {}
    @Override
    public void dispose() {
        stage.dispose();
        errorSkin.dispose();
    }
}


// ===================================================================================
// Game Screen
// ===================================================================================
class GameScreen implements Screen {
    private final Main game;
    private final Skin skin;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final SpriteBatch batch;
    private final Stage hudStage;
    private final Player player;
    private final GameMap gameMap;
    private final Label energyLabel;
    private final Label buffLabel;
    private final Label notificationLabel;
    private float notificationTimer = 0;

    public GameScreen(Main game, Skin skin) {
        this.game = game;
        this.skin = skin;
        camera = new OrthographicCamera();
        viewport = new FitViewport(480, 270, camera);
        batch = new SpriteBatch();
        gameMap = new GameMap();
        player = new Player(gameMap.getPlayerStart());

        hudStage = new Stage(new ScreenViewport());
        Table hudTable = new Table();
        hudTable.top().left();
        hudTable.setFillParent(true);
        energyLabel = new Label("", skin);
        buffLabel = new Label("", skin);
        notificationLabel = new Label("", skin);
        notificationLabel.setColor(Color.YELLOW);

        hudTable.add(energyLabel).pad(10).left().row();
        hudTable.add(buffLabel).pad(10).left().row();
        hudTable.add(notificationLabel).pad(10).left().expandX().row();
        hudStage.addActor(hudTable);
    }

    @Override
    public void render(float delta) {
        handleInput(delta);
        GameState.update(delta);
        player.update(delta);

        if (GameState.foodJustEaten) {
            player.showEatEffect();
            GameState.foodJustEaten = false;
        }
        if (GameState.buffJustApplied) {
            player.showBuffEffect();
            GameState.buffJustApplied = false;
        }

        energyLabel.setText("Energy: " + GameState.energy + " / " + GameState.maxEnergy);
        buffLabel.setText("Active Buff: " + GameState.getActiveBuffStatus());
        updateNotification();

        Gdx.gl.glClearColor(0.2f, 0.8f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.position.set(player.getBounds().getX(), player.getBounds().getY(), 0);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        gameMap.render(batch);
        player.render(batch);
        batch.end();

        hudStage.act(delta);
        hudStage.draw();
    }

    private void handleInput(float delta) {
        float dx = 0, dy = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) dy = 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) dy = -1;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) dx = -1;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) dx = 1;
        player.move(dx, dy, gameMap, delta);

        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            if (gameMap.isPlayerInHouse(player)) {
                try {
                    game.setScreen(new CookingScreen(game, this, skin));
                } catch (Throwable t) {
                    Gdx.app.error("ScreenSwitch", "Failed to create or switch to CookingScreen", t);
                    game.setScreen(new ErrorScreen(t));
                }
            } else {
                showNotification("You must be outside your house to cook.");
            }
        }
    }

    private void updateNotification() {
        if (notificationTimer > 0) {
            notificationTimer -= Gdx.graphics.getDeltaTime();
            if (notificationTimer <= 0) {
                notificationLabel.setText("");
            }
        } else {
            notificationLabel.setText(gameMap.isPlayerInHouse(player) ? "Press 'C' to open the cooking menu." : "");
        }
    }

    public void showNotification(String message) {
        notificationLabel.setText(message);
        notificationTimer = 3f;
    }

    @Override
    public void show() { Gdx.input.setInputProcessor(null); }
    @Override
    public void resize(int width, int height) { viewport.update(width, height, true); hudStage.getViewport().update(width, height, true); }
    @Override
    public void hide() {}
    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void dispose() { batch.dispose(); player.dispose(); gameMap.dispose(); hudStage.dispose(); }
}

// ===================================================================================
// Cooking Screen
// ===================================================================================
class CookingScreen implements Screen {
    private final Main game;
    private final Screen previousScreen;
    private final Stage stage;
    private final Skin skin;
    private final Table recipeGrid;
    private final Table inventoryGrid;
    private final Label detailsLabel;
    private final Label feedbackLabel;
    private final Table contentContainer;
    private final Table cookingContentTable;
    private final Table skillsContentTable;
    private final Table cheatsContentTable; // تب جدید
    private final Label farmingSkillLabel, miningSkillLabel, foragingSkillLabel, fishingSkillLabel;
    private final Label starterSkillLabel, saloonSkillLabel, leahSkillLabel;
    private final TextButton farmButton, mineButton, forageButton, fishButton;
    private final TextButton starterButton, saloonButton, leahButton;
    private final CheckBox showLearnedOnlyCheckbox;

    public CookingScreen(Main game, Screen previousScreen, Skin skin) {
        this.game = game;
        this.previousScreen = previousScreen;
        this.stage = new Stage(new ScreenViewport());
        this.skin = skin;

        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.setBackground(skin.getDrawable("default-pane"));

        Table window = new Table(skin);
        window.setBackground(skin.getDrawable("default-window"));
        window.pad(10);

        window.add(new Label("Kitchen Menu", skin, "default")).colspan(4).pad(15).row();

        contentContainer = new Table();
        cookingContentTable = new Table();
        skillsContentTable = new Table();
        cheatsContentTable = new Table();

        recipeGrid = new Table();
        inventoryGrid = new Table();
        detailsLabel = new Label("Select a recipe or item.", skin);
        detailsLabel.setWrap(true);
        detailsLabel.setAlignment(Align.topLeft);

        showLearnedOnlyCheckbox = new CheckBox(" Show Learned Only", skin);
        setupCookingTable();

        farmingSkillLabel = new Label("", skin);
        miningSkillLabel = new Label("", skin);
        foragingSkillLabel = new Label("", skin);
        fishingSkillLabel = new Label("", skin);
        starterSkillLabel = new Label("", skin);
        saloonSkillLabel = new Label("", skin);
        leahSkillLabel = new Label("", skin);
        farmButton = new TextButton("Level Up", skin);
        mineButton = new TextButton("Level Up", skin);
        forageButton = new TextButton("Level Up", skin);
        fishButton = new TextButton("Level Up", skin);
        starterButton = new TextButton("Learn", skin);
        saloonButton = new TextButton("Learn", skin);
        leahButton = new TextButton("Learn", skin);
        setupSkillsTable();
        setupCheatsTable();

        window.add(contentContainer).colspan(4).expand().fill().row();

        feedbackLabel = new Label("Welcome to your kitchen!", skin);
        feedbackLabel.setColor(Color.YELLOW);

        TextButton recipesButton = new TextButton("Recipes", skin, "toggle");
        TextButton skillsButton = new TextButton("Skills", skin, "toggle");
        TextButton cheatsButton = new TextButton("Cheats", skin, "toggle");
        TextButton fridgeButton = new TextButton("Refrigerator", skin);
        TextButton closeButton = new TextButton("Back to Game", skin);

        ButtonGroup<TextButton> buttonGroup = new ButtonGroup<>(recipesButton, skillsButton, cheatsButton);
        buttonGroup.setMaxCheckCount(1);
        buttonGroup.setMinCheckCount(1);
        recipesButton.setChecked(true);

        recipesButton.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { showCookingView(); } });
        skillsButton.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { showSkillsView(); } });
        cheatsButton.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { showCheatsView(); } });
        fridgeButton.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { showRefrigeratorDialog(); } });
        closeButton.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { Gdx.app.postRunnable(() -> game.setScreen(previousScreen)); } });

        Table bottomBar = new Table();
        bottomBar.add(feedbackLabel).expandX().left().padLeft(10);
        bottomBar.add(recipesButton).pad(5);
        bottomBar.add(skillsButton).pad(5);
        bottomBar.add(cheatsButton).pad(5);
        bottomBar.add(fridgeButton).pad(5);
        bottomBar.add(closeButton).pad(5);
        window.add(bottomBar).colspan(4).fillX();

        rootTable.add(window).width(1050).height(550);
        stage.addActor(rootTable);

        showCookingView();
        refreshAll();
    }

    private void setupCookingTable() {
        Table leftPanel = new Table();
        leftPanel.add(new Label("Recipes", skin, "default")).row();
        leftPanel.add(showLearnedOnlyCheckbox).left().pad(5).row();
        showLearnedOnlyCheckbox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                refreshAll();
            }
        });

        ScrollPane recipeScroll = new ScrollPane(recipeGrid, skin);
        recipeScroll.setFadeScrollBars(false);
        leftPanel.add(recipeScroll).expand().fill();

        Table centerPanel = new Table();
        centerPanel.add(new Label("Details", skin, "default")).row();
        centerPanel.add(detailsLabel).expand().fill().pad(10);

        Table rightPanel = new Table();
        rightPanel.add(new Label("Inventory (Click to Eat)", skin, "default")).row();
        ScrollPane inventoryScroll = new ScrollPane(inventoryGrid, skin);
        inventoryScroll.setFadeScrollBars(false);
        rightPanel.add(inventoryScroll).expand().fill();

        cookingContentTable.add(leftPanel).width(250).expandY().fillY();
        cookingContentTable.add(centerPanel).width(350).expandY().fillY();
        cookingContentTable.add(rightPanel).width(300).expandY().fillY();
    }

    private void setupSkillsTable() {
        skillsContentTable.pad(20);
        skillsContentTable.add(new Label("Your Skills", skin, "default")).colspan(2).padBottom(20).row();

        skillsContentTable.add(farmingSkillLabel).left().pad(10);
        farmButton.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { if (GameState.farming_level < GameState.MAX_SKILL_LEVEL) GameState.farming_level++; refreshAll(); } });
        skillsContentTable.add(farmButton).row();

        skillsContentTable.add(miningSkillLabel).left().pad(10);
        mineButton.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { if (GameState.mining_level < GameState.MAX_SKILL_LEVEL) GameState.mining_level++; refreshAll(); } });
        skillsContentTable.add(mineButton).row();

        skillsContentTable.add(foragingSkillLabel).left().pad(10);
        forageButton.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { if (GameState.foraging_level < GameState.MAX_SKILL_LEVEL) GameState.foraging_level++; refreshAll(); } });
        skillsContentTable.add(forageButton).row();

        skillsContentTable.add(fishingSkillLabel).left().pad(10);
        fishButton.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { if (GameState.fishing_level < GameState.MAX_SKILL_LEVEL) GameState.fishing_level++; refreshAll(); } });
        skillsContentTable.add(fishButton).row();

        skillsContentTable.add(starterSkillLabel).left().pad(10);
        starterButton.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { GameState.starter = true; refreshAll(); } });
        skillsContentTable.add(starterButton).row();

        skillsContentTable.add(saloonSkillLabel).left().pad(10);
        saloonButton.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { GameState.stardrop_saloon = true; refreshAll(); } });
        skillsContentTable.add(saloonButton).row();

        skillsContentTable.add(leahSkillLabel).left().pad(10);
        leahButton.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { GameState.leah_reward = true; refreshAll(); } });
        skillsContentTable.add(leahButton).row();
    }

    private void setupCheatsTable() {
        cheatsContentTable.pad(10);
        cheatsContentTable.add(new Label("Add Ingredients to Inventory", skin, "default")).colspan(2).padBottom(15).row();

        Table ingredientsTable = new Table();
        for (GameItem item : GameState.allItems) {
            if (item.type == ItemType.INGREDIENT) {
                ingredientsTable.add(new Label(item.name, skin)).left().pad(5);
                TextButton addButton = new TextButton("+", skin);
                addButton.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        CookingController.cheatAddIngredient(item.name);
                        feedbackLabel.setText("Added 1 " + item.name);
                        refreshAll();
                    }
                });
                ingredientsTable.add(addButton).pad(5).row();
            }
        }

        ScrollPane scrollPane = new ScrollPane(ingredientsTable, skin);
        scrollPane.setFadeScrollBars(false);
        cheatsContentTable.add(scrollPane).expand().fill();
    }

    private void showCookingView() { contentContainer.clear(); contentContainer.add(cookingContentTable).expand().fill(); }
    private void showSkillsView() { contentContainer.clear(); contentContainer.add(skillsContentTable).expand().fill(); }
    private void showCheatsView() { contentContainer.clear(); contentContainer.add(cheatsContentTable).expand().fill(); }

    private void refreshAll() {
        populateRecipeGrid();
        populateInventoryGrid();
        updateSkillLabels();
    }

    private void updateSkillLabels() {
        farmingSkillLabel.setText("Farming Level: " + GameState.farming_level);
        miningSkillLabel.setText("Mining Level: " + GameState.mining_level);
        foragingSkillLabel.setText("Foraging Level: " + GameState.foraging_level);
        fishingSkillLabel.setText("Fishing Level: " + GameState.fishing_level);

        farmButton.setDisabled(GameState.farming_level >= GameState.MAX_SKILL_LEVEL);
        mineButton.setDisabled(GameState.mining_level >= GameState.MAX_SKILL_LEVEL);
        forageButton.setDisabled(GameState.foraging_level >= GameState.MAX_SKILL_LEVEL);
        fishButton.setDisabled(GameState.fishing_level >= GameState.MAX_SKILL_LEVEL);

        starterSkillLabel.setText("Starter Recipes: " + (GameState.starter ? "Learned" : "Locked"));
        saloonSkillLabel.setText("Stardrop Saloon Recipes: " + (GameState.stardrop_saloon ? "Learned" : "Locked"));
        leahSkillLabel.setText("Leah's Recipes: " + (GameState.leah_reward ? "Learned" : "Locked"));

        starterButton.setDisabled(GameState.starter);
        saloonButton.setDisabled(GameState.stardrop_saloon);
        leahButton.setDisabled(GameState.leah_reward);
    }

    private void populateRecipeGrid() {
        recipeGrid.clear();
        recipeGrid.align(Align.top);
        boolean learnedOnly = showLearnedOnlyCheckbox.isChecked();

        for (GameItem item : GameState.allItems) {
            if (item.type != ItemType.COOKED_DISH) continue;
            if (learnedOnly && !CookingController.hasLearnedRecipe(item)) continue;

            TextButton foodButton = new TextButton(item.name, skin);
            boolean canCraft = CookingController.hasAllIngredients(item) && CookingController.hasLearnedRecipe(item);
            foodButton.setDisabled(!canCraft);
            foodButton.setColor(canCraft ? Color.WHITE : Color.GRAY);

            foodButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (!foodButton.isDisabled()) {
                        String result = CookingController.prepareFood(item.name);
                        feedbackLabel.setText(result);
                        refreshAll();
                    }
                }
                @Override
                public void enter(InputEvent e, float x, float y, int p, Actor a) { updateDetailsPanel(item); }
            });
            recipeGrid.add(foodButton).growX().pad(5).row();
        }
    }

    private void populateInventoryGrid() {
        inventoryGrid.clear();
        inventoryGrid.align(Align.top);
        for (Map.Entry<GameItem, Integer> entry : GameState.player_inventory.entrySet()) {
            GameItem item = entry.getKey();
            int count = entry.getValue();
            TextButton itemButton = new TextButton(item.name + " x" + count, skin);

            if (item.isEdible()) {
                itemButton.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        String result = CookingController.eat(item.name);
                        feedbackLabel.setText(result);
                        refreshAll();
                    }
                });
            } else {
                itemButton.setColor(Color.GRAY); // غیرقابل کلیک کردن آیتم‌های غیرخوراکی
            }

            itemButton.addListener(new ClickListener() {
                @Override
                public void enter(InputEvent e, float x, float y, int p, Actor a) { updateDetailsPanel(item); }
            });
            inventoryGrid.add(itemButton).growX().pad(5).row();
        }
    }

    private void showRefrigeratorDialog() {
        Dialog dialog = new Dialog("Refrigerator", skin);
        dialog.pad(20);
        Table content = dialog.getContentTable();
        Table playerInvTable = new Table();
        Table fridgeInvTable = new Table();

        final Label dialogFeedback = new Label("", skin);
        dialogFeedback.setColor(Color.RED);

        ScrollPane playerScroll = new ScrollPane(playerInvTable, skin);
        ScrollPane fridgeScroll = new ScrollPane(fridgeInvTable, skin);
        playerScroll.setFadeScrollBars(false);
        fridgeScroll.setFadeScrollBars(false);
        populateFridgeDialogTables(playerInvTable, fridgeInvTable, dialogFeedback);

        content.add(new Label("Your Inventory", skin)).pad(10);
        content.add(new Label("Refrigerator", skin)).pad(10).row();
        content.add(playerScroll).width(250).height(300);
        content.add(fridgeScroll).width(250).height(300).row();
        content.add(dialogFeedback).colspan(2).padTop(10).row();

        dialog.button("Close", true);
        dialog.show(stage);
    }

    private void populateFridgeDialogTables(Table playerInvTable, Table fridgeInvTable, Label dialogFeedback) {
        playerInvTable.clear();
        fridgeInvTable.clear();
        playerInvTable.align(Align.top);
        fridgeInvTable.align(Align.top);

        for (Map.Entry<GameItem, Integer> entry : new HashMap<>(GameState.player_inventory).entrySet()) {
            GameItem item = entry.getKey();
            TextButton moveButton = new TextButton(item.name + " x" + entry.getValue() + " ->", skin);
            moveButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    String result = CookingController.manageRefrigerator("put", item.name);
                    if (result != null) {
                        dialogFeedback.setText(result);
                    } else {
                        dialogFeedback.setText("");
                        populateFridgeDialogTables(playerInvTable, fridgeInvTable, dialogFeedback);
                        refreshAll();
                    }
                }
            });
            playerInvTable.add(moveButton).growX().pad(5).row();
        }

        for (Map.Entry<GameItem, Integer> entry : new HashMap<>(GameState.fridge_inventory).entrySet()) {
            GameItem item = entry.getKey();
            TextButton moveButton = new TextButton("<- " + item.name + " x" + entry.getValue(), skin);
            moveButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    CookingController.manageRefrigerator("pick", item.name);
                    dialogFeedback.setText("");
                    populateFridgeDialogTables(playerInvTable, fridgeInvTable, dialogFeedback);
                    refreshAll();
                }
            });
            fridgeInvTable.add(moveButton).growX().pad(5).row();
        }
    }

    private void updateDetailsPanel(GameItem item) {
        StringBuilder sb = new StringBuilder();
        sb.append("[YELLOW]").append(item.name).append("[]\n\n");
        if (!item.ingredients.isEmpty()) {
            sb.append("[GRAY]Ingredients:[]").append("\n");
            for (Map.Entry<String, Integer> entry : item.ingredients.entrySet()) {
                String ingredientName = entry.getKey();
                int required = entry.getValue();
                int available = CookingController.getAvailableAmount(ingredientName);
                String color = available >= required ? "[GREEN]" : "[RED]";
                sb.append("- ").append(ingredientName).append(" (").append(color).append(available).append("[]/").append(required).append(")\n");
            }
        }
        if (item.isEdible()) {
            sb.append("\n[GREEN]Energy: +").append(item.energy).append("[]");
        }
        if (item.buff != null) sb.append("\n[CYAN]Buff: ").append(item.buff.description).append("[]");
        if (item.sellPrice > 0) sb.append("\n[GOLD]Sell Price: ").append(item.sellPrice).append(" G[]");

        detailsLabel.setText(sb.toString());
        detailsLabel.getStyle().font.getData().markupEnabled = true;
    }

    @Override
    public void render(float delta) { Gdx.gl.glClearColor(0, 0, 0, 1); Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); stage.act(delta); stage.draw(); }
    @Override
    public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override
    public void show() { Gdx.input.setInputProcessor(stage); }
    @Override
    public void hide() {}
    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void dispose() { stage.dispose(); }
}

// ===================================================================================
// Game Entities and Logic
// ===================================================================================

class Player {
    private final Rectangle bounds;
    private final Texture texture;
    private final float speed = 100f;
    private final Texture eatEffectTexture;
    private final Texture buffEffectTexture;
    private float eatEffectTimer = 0;
    private float buffEffectTimer = 0;

    public Player(Rectangle startRect) {
        this.bounds = startRect;

            this.texture = new Texture(Gdx.files.internal("farmer.png"));


        Pixmap eatPixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        eatPixmap.setColor(Color.GREEN);
        eatPixmap.fillCircle(8, 8, 7);
        eatPixmap.setColor(Color.WHITE);
        eatPixmap.drawLine(8, 4, 8, 12);
        eatPixmap.drawLine(4, 8, 12, 8);
        eatEffectTexture = new Texture(eatPixmap);
        eatPixmap.dispose();

        Pixmap buffPixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        buffPixmap.setColor(Color.YELLOW);
        int[] starVertices = {8,0, 10,6, 16,6, 11,10, 13,16, 8,12, 3,16, 5,10, 0,6, 6,6};
        for (int i = 0; i < starVertices.length; i += 2) {
            buffPixmap.drawLine(starVertices[i], starVertices[i+1], starVertices[(i+2)%starVertices.length], starVertices[(i+3)%starVertices.length]);
        }
        buffEffectTexture = new Texture(buffPixmap);
        buffPixmap.dispose();
    }

    public void update(float delta) {
        if (eatEffectTimer > 0) eatEffectTimer -= delta;
        if (buffEffectTimer > 0) buffEffectTimer -= delta;
    }

    public void move(float dx, float dy, GameMap map, float deltaTime) {
        float moveAmountX = dx * speed * deltaTime;
        float moveAmountY = dy * speed * deltaTime;
        bounds.x += moveAmountX;
        if (map.isCollision(bounds)) bounds.x -= moveAmountX;
        bounds.y += moveAmountY;
        if (map.isCollision(bounds)) bounds.y -= moveAmountY;
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);

        if (eatEffectTimer > 0) {
            batch.draw(eatEffectTexture, bounds.x, bounds.y + bounds.height, 16, 16);
        }
        if (buffEffectTimer > 0) {
            batch.draw(buffEffectTexture, bounds.x, bounds.y + bounds.height, 16, 16);
        }
    }

    public void showEatEffect() { this.eatEffectTimer = 2.0f; } // افزایش زمان
    public void showBuffEffect() { this.buffEffectTimer = 3.0f; } // افزایش زمان
    public Rectangle getBounds() { return bounds; }
    public void dispose() {
        texture.dispose();
        eatEffectTexture.dispose();
        buffEffectTexture.dispose();
    }
}

class GameMap {
    private final Tile[][] tiles;
    private final int width, height;
    private final Rectangle houseBounds;
    private final Texture grassTexture, wallTexture, floorTexture, refrigeratorTexture;
    public static final int TILE_SIZE = 16;

    public GameMap() {
        this.width = 40; this.height = 30;
        this.tiles = new Tile[width][height];
        int hX = 15, hY = 10, hW = 8, hH = 6;
        houseBounds = new Rectangle(hX * TILE_SIZE, hY * TILE_SIZE, hW * TILE_SIZE, hH * TILE_SIZE);

        grassTexture = createFallbackTexture(Color.GREEN, "grass.png");
        wallTexture = createFallbackTexture(Color.BROWN, "house_wall.png");
        floorTexture = createFallbackTexture(Color.TAN, "house_floor.png");
        refrigeratorTexture = createFallbackTexture(Color.LIGHT_GRAY, "refrigerator.png");

        initialize();
    }

    private Texture createFallbackTexture(Color color, String fileName) {
        try {
            return new Texture(Gdx.files.internal(fileName));
        } catch (Exception e) {
            Pixmap pixmap = new Pixmap(TILE_SIZE, TILE_SIZE, Pixmap.Format.RGBA8888);
            pixmap.setColor(color);
            pixmap.fill();
            Texture texture = new Texture(pixmap);
            pixmap.dispose();
            return texture;
        }
    }

    private void initialize() {
        for (int x = 0; x < width; x++) for (int y = 0; y < height; y++) tiles[x][y] = new Tile(TileType.GRASS);
        int hX = 15, hY = 10, hW = 8, hH = 6;
        for (int x = hX; x < hX + hW; x++) {
            for (int y = hY; y < hY + hH; y++) {
                if (x == hX || x == hX + hW - 1 || y == hY || y == hY + hH - 1) tiles[x][y] = new Tile(TileType.HOUSE_WALL);
                else tiles[x][y] = new Tile(TileType.HOUSE_FLOOR);
            }
        }
        tiles[hX + hW / 2][hY] = new Tile(TileType.HOUSE_FLOOR); // Door
        tiles[hX + 1][hY + hH - 2] = new Tile(TileType.REFRIGERATOR);
    }

    public void render(SpriteBatch batch) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Texture currentTexture = grassTexture;
                boolean drawOnTop = false;
                switch(tiles[x][y].getType()) {
                    case HOUSE_WALL: currentTexture = wallTexture; break;
                    case HOUSE_FLOOR: currentTexture = floorTexture; break;
                    case REFRIGERATOR:
                        currentTexture = floorTexture;
                        drawOnTop = true;
                        break;
                }
                batch.draw(currentTexture, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                if (drawOnTop) {
                    batch.draw(refrigeratorTexture, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }

    public boolean isCollision(Rectangle rect) {
        if (rect.x < 0 || rect.y < 0 || rect.x + rect.width > width * TILE_SIZE || rect.y + rect.height > height * TILE_SIZE) return true;
        int startX = (int)(rect.x / TILE_SIZE), endX = (int)((rect.x + rect.width) / TILE_SIZE);
        int startY = (int)(rect.y / TILE_SIZE), endY = (int)((rect.y + rect.height) / TILE_SIZE);
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    if (!tiles[x][y].isWalkable()) return true;
                }
            }
        }
        return false;
    }

    public boolean isPlayerInHouse(Player player) { return houseBounds.overlaps(player.getBounds()); }
    public Rectangle getPlayerStart() { return new Rectangle(16 * TILE_SIZE, 11 * TILE_SIZE, TILE_SIZE * 0.8f, TILE_SIZE); }
    public void dispose() { grassTexture.dispose(); wallTexture.dispose(); floorTexture.dispose(); refrigeratorTexture.dispose(); }
}

enum TileType { GRASS, HOUSE_FLOOR, HOUSE_WALL, REFRIGERATOR }

class Tile {
    private final TileType type;
    public Tile(TileType type) { this.type = type; }
    public TileType getType() { return type; }
    public boolean isWalkable() {
        return type != TileType.HOUSE_WALL && type != TileType.REFRIGERATOR;
    }
}

class Buff {
    enum BuffType { MAX_ENERGY, FARMING, FORAGING, FISHING, MINING }
    public final String description; public final BuffType type; public final int value; public final float durationHours;
    public Buff(String d, BuffType t, int v, float dur) { description = d; type = t; value = v; durationHours = dur; }
}

// --- کلاس Food به GameItem تغییر نام یافت ---
enum ItemType { COOKED_DISH, INGREDIENT, NON_FOOD }
class GameItem {
    public final String name;
    public final Map<String, Integer> ingredients;
    public final int energy;
    public final Buff buff;
    public final String source;
    public final int source_level;
    public final int sellPrice;
    public final ItemType type;

    public GameItem(String n, Map<String, Integer> i, int e, Buff b, String s, int sl, int sp, ItemType type) {
        this.name = n; this.ingredients = i; this.energy = e; this.buff = b; this.source = s; this.source_level = sl; this.sellPrice = sp; this.type = type;
    }

    public boolean isEdible() { return this.type == ItemType.COOKED_DISH || (this.type == ItemType.INGREDIENT && this.energy > 0); }

    @Override public boolean equals(Object o) { return o instanceof GameItem && name.equals(((GameItem) o).name); }
    @Override public int hashCode() { return Objects.hash(name); }
}

class GameState {
    public static List<GameItem> allItems = new ArrayList<>();
    public static Map<GameItem, Integer> player_inventory = new HashMap<>();
    public static Map<GameItem, Integer> fridge_inventory = new HashMap<>();
    public static int energy = 200, maxEnergy = 200;
    private static final int BASE_MAX_ENERGY = 200;
    public static Buff activeBuff = null;
    public static float buffTimeRemaining = 0;

    public static boolean starter;
    public static boolean stardrop_saloon;
    public static boolean leah_reward;
    public static int fishing_level;
    public static int foraging_level;
    public static int farming_level;
    public static int mining_level;
    public static final int MAX_SKILL_LEVEL = 5;

    public static final int MAX_INVENTORY_SIZE = 24;
    public static boolean foodJustEaten = false;
    public static boolean buffJustApplied = false;

    public static void initialize() {
        starter = false;
        stardrop_saloon = false;
        leah_reward = false;
        fishing_level = 1;
        foraging_level = 1;
        farming_level = 1;
        mining_level = 1;

        initializeItems();
        player_inventory.put(findItemByName("Egg"), 5);
        player_inventory.put(findItemByName("Milk"), 3);
        fridge_inventory.put(findItemByName("Sardine"), 2);
        player_inventory.put(findItemByName("Wheat Flour"), 10);
        player_inventory.put(findItemByName("Cherry Bomb"), 3);
    }

    public static void update(float delta) {
        if (activeBuff != null) {
            buffTimeRemaining -= delta;
            if (buffTimeRemaining <= 0) clearBuff();
        }
    }

    public static void applyBuff(Buff buff) {
        clearBuff();
        activeBuff = buff;
        buffTimeRemaining = buff.durationHours * 10f;
        if (buff.type == Buff.BuffType.MAX_ENERGY) maxEnergy = BASE_MAX_ENERGY + buff.value;
        buffJustApplied = true;
    }

    public static void clearBuff() {
        if (activeBuff != null && activeBuff.type == Buff.BuffType.MAX_ENERGY) {
            maxEnergy = BASE_MAX_ENERGY;
            if (energy > maxEnergy) energy = maxEnergy;
        }
        activeBuff = null;
        buffTimeRemaining = 0;
    }

    public static String getActiveBuffStatus() {
        if (activeBuff == null) return "None";
        return activeBuff.description + " (" + (int)buffTimeRemaining + "s)";
    }

    public static GameItem findItemByName(String name) {
        return allItems.stream().filter(f -> f.name.equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    private static Buff parseBuff(String buffString) {
        if (buffString == null || buffString.isEmpty()) return null;
        Pattern pattern = Pattern.compile("([a-zA-Z ]+)(?: \\+(\\d+))? \\((\\d+) hours\\)");
        Matcher matcher = pattern.matcher(buffString);
        if (matcher.find()) {
            String name = matcher.group(1).trim().toUpperCase();
            int value = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 0;
            float duration = Float.parseFloat(matcher.group(3));
            Buff.BuffType type;
            switch(name) {
                case "MAX ENERGY": type = Buff.BuffType.MAX_ENERGY; break;
                case "FARMING": type = Buff.BuffType.FARMING; break;
                case "FORAGING": type = Buff.BuffType.FORAGING; break;
                case "FISHING": type = Buff.BuffType.FISHING; break;
                case "MINING": type = Buff.BuffType.MINING; break;
                default: return null;
            }
            return new Buff(buffString, type, value, duration);
        }
        return null;
    }

    private static void initializeItems() {
        if (!allItems.isEmpty()) return;

        // --- غذاهای پخته شده ---
        allItems.add(new GameItem("Fried Egg", new HashMap<String, Integer>() {{ put("Egg", 1); }}, 50, null, "Starter", 0, 35, ItemType.COOKED_DISH));
        allItems.add(new GameItem("Baked Fish", new HashMap<String, Integer>() {{ put("Sardine", 1); put("Salmon", 1); put("Wheat", 1); }}, 75, null, "Starter", 0, 100, ItemType.COOKED_DISH));
        allItems.add(new GameItem("Salad", new HashMap<String, Integer>() {{ put("Leek", 1); put("Dandelion", 1); }}, 113, null, "Starter", 0, 110, ItemType.COOKED_DISH));
        allItems.add(new GameItem("Omelet", new HashMap<String, Integer>() {{ put("Egg", 1); put("Milk", 1); }}, 100, null, "Stardrop Saloon", 0, 125, ItemType.COOKED_DISH));
        allItems.add(new GameItem("Pumpkin Pie", new HashMap<String, Integer>() {{ put("Pumpkin", 1); put("Wheat Flour", 1); put("Milk", 1); put("Sugar", 1); }}, 225, null, "Stardrop Saloon", 0, 385, ItemType.COOKED_DISH));
        allItems.add(new GameItem("Spaghetti", new HashMap<String, Integer>() {{ put("Wheat Flour", 1); put("Tomato", 1); }}, 75, null, "Stardrop Saloon", 0, 120, ItemType.COOKED_DISH));
        allItems.add(new GameItem("Pizza", new HashMap<String, Integer>() {{ put("Wheat Flour", 1); put("Tomato", 1); put("Cheese", 1); }}, 150, null, "Stardrop Saloon", 0, 300, ItemType.COOKED_DISH));
        allItems.add(new GameItem("Tortilla", new HashMap<String, Integer>() {{ put("Corn", 1); }}, 50, null, "Stardrop Saloon", 0, 50, ItemType.COOKED_DISH));
        allItems.add(new GameItem("Maki Roll", new HashMap<String, Integer>() {{ put("Any Fish", 1); put("Rice", 1); put("Fiber", 1); }}, 100, null, "Stardrop Saloon", 0, 220, ItemType.COOKED_DISH));
        allItems.add(new GameItem("Triple Shot Espresso", new HashMap<String, Integer>() {{ put("Coffee", 3); }}, 8, parseBuff("Max Energy +100 (5 hours)"), "Stardrop Saloon", 0, 450, ItemType.COOKED_DISH));
        allItems.add(new GameItem("Cookie", new HashMap<String, Integer>() {{ put("Wheat Flour", 1); put("Sugar", 1); put("Egg", 1); }}, 90, null, "Stardrop Saloon", 0, 140, ItemType.COOKED_DISH));
        allItems.add(new GameItem("Hashbrowns", new HashMap<String, Integer>() {{ put("Potato", 1); put("Oil", 1); }}, 90, parseBuff("Farming +1 (5 hours)"), "Stardrop Saloon", 0, 120, ItemType.COOKED_DISH));
        allItems.add(new GameItem("Pancakes", new HashMap<String, Integer>() {{ put("Wheat Flour", 1); put("Egg", 1); }}, 90, parseBuff("Foraging +2 (11 hours)"), "Stardrop Saloon", 0, 80, ItemType.COOKED_DISH));
        allItems.add(new GameItem("Fruit Salad", new HashMap<String, Integer>() {{ put("Blueberry", 1); put("Melon", 1); put("Apricot", 1); }}, 263, null, "Stardrop Saloon", 0, 450, ItemType.COOKED_DISH));
        allItems.add(new GameItem("Red Plate", new HashMap<String, Integer>() {{ put("Red Cabbage", 1); put("Radish", 1); }}, 240, parseBuff("Max Energy +50 (3 hours)"), "Stardrop Saloon", 0, 400, ItemType.COOKED_DISH));
        allItems.add(new GameItem("Bread", new HashMap<String, Integer>() {{ put("Wheat Flour", 1); }}, 50, null, "Stardrop Saloon", 0, 60, ItemType.COOKED_DISH));
        allItems.add(new GameItem("Salmon Dinner", new HashMap<String, Integer>() {{ put("Salmon", 1); put("Amaranth", 1); put("Kale", 1); }}, 125, null, "Leah reward", 3, 300, ItemType.COOKED_DISH));
        allItems.add(new GameItem("Vegetable Medley", new HashMap<String, Integer>() {{ put("Tomato", 1); put("Beet", 1); }}, 165, parseBuff("Foraging +2 (2 hours)"), "Foraging", 7, 120, ItemType.COOKED_DISH));
        allItems.add(new GameItem("Farmer's Lunch", new HashMap<String, Integer>() {{ put("Omelet", 1); put("Parsnip", 1); }}, 200, parseBuff("Farming +3 (5 hours)"), "Farming", 3, 150, ItemType.COOKED_DISH));
        allItems.add(new GameItem("Survival Burger", new HashMap<String, Integer>() {{ put("Bread", 1); put("Cave Carrot", 1); put("Eggplant", 1); }}, 125, parseBuff("Foraging +3 (5 hours)"), "Foraging", 2, 180, ItemType.COOKED_DISH));
        allItems.add(new GameItem("Dish O' The Sea", new HashMap<String, Integer>() {{ put("Sardine", 2); put("Hashbrowns", 1); }}, 150, parseBuff("Fishing +3 (5 hours)"), "Fishing", 3, 220, ItemType.COOKED_DISH));
        allItems.add(new GameItem("Seafoam Pudding", new HashMap<String, Integer>() {{ put("Flounder", 1); put("Midnight Carp", 1); }}, 175, parseBuff("Fishing +4 (10 hours)"), "Fishing", 9, 300, ItemType.COOKED_DISH));
        allItems.add(new GameItem("Miner's Treat", new HashMap<String, Integer>() {{ put("Cave Carrot", 2); put("Sugar", 1); put("Milk", 1); }}, 125, parseBuff("Mining +3 (5 hours)"), "Mining", 3, 200, ItemType.COOKED_DISH));

        // --- مواد اولیه ---
        String[] ingredientNames = {"Egg", "Sardine", "Salmon", "Wheat", "Leek", "Dandelion", "Milk", "Pumpkin", "Wheat Flour", "Sugar", "Tomato", "Cheese", "Corn", "Any Fish", "Rice", "Fiber", "Coffee", "Potato", "Oil", "Blueberry", "Melon", "Apricot", "Red Cabbage", "Radish", "Amaranth", "Kale", "Beet", "Parsnip", "Cave Carrot", "Eggplant", "Flounder", "Midnight Carp"};
        for (String name : ingredientNames) {
            if (allItems.stream().noneMatch(f -> f.name.equalsIgnoreCase(name))) {
                // اگر ماده اولیه خودش یک غذای قابل پخت است (مثل Hashbrowns)، آن را اضافه نکن
                boolean isCookable = allItems.stream().anyMatch(item -> item.name.equalsIgnoreCase(name) && item.type == ItemType.COOKED_DISH);
                if (!isCookable) {
                    allItems.add(new GameItem(name, new HashMap<>(), 5, null, "Ingredient", 0, 10, ItemType.INGREDIENT));
                }
            }
        }

        // --- آیتم‌های غیرخوراکی ---
        allItems.add(new GameItem("Cherry Bomb", new HashMap<>(), 0, null, "Crafting", 0, 50, ItemType.NON_FOOD));
    }
}

class CookingController {
    public static boolean hasLearnedRecipe(GameItem item) {
        switch (item.source.toLowerCase()) {
            case "starter": return GameState.starter;
            case "stardrop saloon": return GameState.stardrop_saloon;
            case "leah reward": return GameState.leah_reward;
            case "fishing": return GameState.fishing_level >= item.source_level;
            case "foraging": return GameState.foraging_level >= item.source_level;
            case "farming": return GameState.farming_level >= item.source_level;
            case "mining": return GameState.mining_level >= item.source_level;
            default: return false;
        }
    }

    public static String prepareFood(String itemName) {
        GameItem item = GameState.findItemByName(itemName);
        if (item == null || item.ingredients.isEmpty()) return "Invalid recipe.";
        if (!hasLearnedRecipe(item)) return "You haven't learned this recipe.";
        if (!hasAllIngredients(item)) return "Not enough ingredients.";
        if (isInventoryFull(item)) return "Your inventory is full.";
        if (GameState.energy < 3) return "Not enough energy to cook.";
        consumeIngredients(item);
        addToInventory(item, 1);
        GameState.energy -= 3;
        return item.name + " cooked successfully!";
    }

    public static String eat(String itemName) {
        GameItem itemToEat = GameState.findItemByName(itemName);
        if (!GameState.player_inventory.containsKey(itemToEat)) return "You don't have that item.";
        if (!itemToEat.isEdible()) return "You can't eat that!";

        GameState.energy += itemToEat.energy;
        if (GameState.energy > GameState.maxEnergy) GameState.energy = GameState.maxEnergy;
        if (itemToEat.buff != null) GameState.applyBuff(itemToEat.buff);
        removeFromInventory(itemToEat, 1);
        GameState.foodJustEaten = true;
        return "You ate " + itemToEat.name + ".";
    }

    public static String manageRefrigerator(String action, String itemName) {
        GameItem targetItem = GameState.findItemByName(itemName);
        if ("put".equalsIgnoreCase(action)) {
            if (targetItem.type == ItemType.NON_FOOD) {
                return "Cannot store non-food items in the refrigerator.";
            }
            int quantity = GameState.player_inventory.getOrDefault(targetItem, 0);
            if (quantity > 0) {
                removeFromInventory(targetItem, quantity);
                GameState.fridge_inventory.put(targetItem, GameState.fridge_inventory.getOrDefault(targetItem, 0) + quantity);
            }
        } else if ("pick".equalsIgnoreCase(action)) {
            int quantity = GameState.fridge_inventory.getOrDefault(targetItem, 0);
            if (quantity > 0) {
                GameState.fridge_inventory.remove(targetItem);
                addToInventory(targetItem, quantity);
            }
        }
        return null;
    }

    public static void cheatAddIngredient(String itemName) {
        GameItem item = GameState.findItemByName(itemName);
        if (item != null) {
            addToInventory(item, 1);
        }
    }

    public static boolean hasAllIngredients(GameItem item) {
        for (Map.Entry<String, Integer> entry : item.ingredients.entrySet()) {
            if (getAvailableAmount(entry.getKey()) < entry.getValue()) return false;
        }
        return true;
    }

    public static int getAvailableAmount(String name) {
        GameItem ingredient = GameState.findItemByName(name);
        return GameState.player_inventory.getOrDefault(ingredient, 0) + GameState.fridge_inventory.getOrDefault(ingredient, 0);
    }

    private static void consumeIngredients(GameItem item) {
        for (Map.Entry<String, Integer> entry : item.ingredients.entrySet()) {
            GameItem ingredient = GameState.findItemByName(entry.getKey());
            int required = entry.getValue();
            int fromInv = Math.min(required, GameState.player_inventory.getOrDefault(ingredient, 0));
            if (fromInv > 0) {
                removeFromInventory(ingredient, fromInv);
                required -= fromInv;
            }
            if (required > 0) {
                int fromFridge = Math.min(required, GameState.fridge_inventory.getOrDefault(ingredient, 0));
                if(fromFridge > 0) {
                    GameState.fridge_inventory.put(ingredient, GameState.fridge_inventory.get(ingredient) - fromFridge);
                    if (GameState.fridge_inventory.get(ingredient) == 0) GameState.fridge_inventory.remove(ingredient);
                }
            }
        }
    }

    private static boolean isInventoryFull(GameItem itemToCraft) {
        return GameState.player_inventory.size() >= GameState.MAX_INVENTORY_SIZE &&
            !GameState.player_inventory.containsKey(itemToCraft);
    }

    private static void addToInventory(GameItem item, int count) {
        GameState.player_inventory.put(item, GameState.player_inventory.getOrDefault(item, 0) + count);
    }

    private static void removeFromInventory(GameItem item, int count) {
        int current = GameState.player_inventory.getOrDefault(item, 0);
        if (current <= count) GameState.player_inventory.remove(item);
        else GameState.player_inventory.put(item, current - count);
    }
}
