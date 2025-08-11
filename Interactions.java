package com.mygame.stardew;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Main class for the game, implementing interactions using LibGDX.
 * This version fixes a compilation error related to checking for existing windows.
 */
public class Main extends ApplicationAdapter {

    //================================================================================
    // World and Camera Constants
    //================================================================================
    private static final float WORLD_WIDTH = 3200;
    private static final float WORLD_HEIGHT = 1800;
    private static final float TILE_SIZE = 128;
    private static final float INTERACTION_DISTANCE = 150f;
    private OrthographicCamera gameCamera;
    private Viewport gameViewport;
    private Viewport uiViewport;

    //================================================================================
    // Game Logic (Model)
    //================================================================================

    static class Player {
        String username;
        String gender;
        java.util.List<Item> inventory = new ArrayList<>();
        Map<Player, Integer> friendshipLevels = new HashMap<>();
        Map<Player, Integer> friendshipXp = new HashMap<>();
        boolean isMarried = false;
        java.util.List<String> pendingNotifications = new ArrayList<>();

        public Player(String username, String gender) {
            this.username = username;
            this.gender = gender;
        }

        public boolean hasItem(String itemName, int amount) {
            return inventory.stream().filter(item -> item.name.equals(itemName)).count() >= amount;
        }

        public Item getItem(String itemName) {
            return inventory.stream().filter(item -> item.name.equals(itemName)).findFirst().orElse(null);
        }

        public void addItem(Item item) {
            inventory.add(item);
        }

        public void removeItem(String itemName, int amount) {
            for (int i = 0; i < amount; i++) {
                Item itemToRemove = getItem(itemName);
                if (itemToRemove != null) {
                    inventory.remove(itemToRemove);
                }
            }
        }

        public int getFriendshipLevel(Player other) {
            return friendshipLevels.getOrDefault(other, 0);
        }
    }

    static class Item {
        String name;
        public Item(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }

    static class Ring extends Item {
        public Ring(String name) { super(name); }
    }

    static class Gift {
        static int nextId = 1;
        int id;
        Player from;
        Player to;
        Item item;
        int rating = 0;

        public Gift(Player from, Player to, Item item) {
            this.id = nextId++;
            this.from = from;
            this.to = to;
            this.item = item;
        }
    }

    static class MarriageProposal {
        Player proposer;
        Player proposedTo;
        Ring ring;

        public MarriageProposal(Player proposer, Player proposedTo, Ring ring) {
            this.proposer = proposer;
            this.proposedTo = proposedTo;
            this.ring = ring;
        }
    }

    static class InteractionManager {
        public final Map<String, Player> players = new HashMap<>();
        private final java.util.List<Gift> giftLog = new ArrayList<>();
        private final Map<Player, MarriageProposal> pendingProposals = new HashMap<>();
        private final Map<String, java.util.List<String>> talkHistories = new HashMap<>();

        public InteractionManager() {
            Player p1 = new Player("player", "Male");
            Player p2 = new Player("Sebastian", "Male");
            Player p3 = new Player("Leah", "Female");
            Player p4 = new Player("Abigail", "Female");

            p1.addItem(new Item("Wood"));
            p1.addItem(new Item("Stone"));
            p1.addItem(new Item("Flower"));
            p1.addItem(new Ring("Diamond Ring"));
            p2.addItem(new Item("Obsidian"));
            p3.addItem(new Item("Salad"));
            p4.addItem(new Item("Amethyst"));

            players.put(p1.username, p1);
            players.put(p2.username, p2);
            players.put(p3.username, p3);
            players.put(p4.username, p4);
        }

        public Player getPlayer(String username) {
            return players.get(username);
        }

        private String getHistoryKey(String user1, String user2) {
            java.util.List<String> names = new ArrayList<>();
            names.add(user1);
            names.add(user2);
            Collections.sort(names);
            return names.get(0) + "-" + names.get(1);
        }

        private void addFriendshipXp(Player p1, Player p2, int amount) {
            if (p1 == null || p2 == null || p1.equals(p2)) return;
            updateFriendshipForPlayer(p1, p2, amount);
            updateFriendshipForPlayer(p2, p1, amount);
        }

        private void updateFriendshipForPlayer(Player subject, Player object, int amount) {
            int currentXp = subject.friendshipXp.getOrDefault(object, 0);
            int currentLevel = subject.getFriendshipLevel(object);
            int newXp = currentXp + amount;

            if (currentLevel == 4) {
                subject.friendshipXp.put(object, newXp);
                return;
            }

            int requiredXpForNextLevel = 100 * (currentLevel + 1);
            if (newXp >= requiredXpForNextLevel) {
                subject.friendshipLevels.put(object, Math.min(4, currentLevel + 1));
                subject.friendshipXp.put(object, newXp - requiredXpForNextLevel);
            } else {
                subject.friendshipXp.put(object, Math.max(0, newXp));
            }
        }

        public String giveGift(Player from, Player to, Item item) {
            if (!from.hasItem(item.name, 1)) return "You don't have this item in your inventory.";
            if (from.getFriendshipLevel(to) < 1) return "Your friendship level is not high enough (Min Level 1).";

            from.removeItem(item.name, 1);
            to.addItem(item);
            giftLog.add(new Gift(from, to, item));
            addFriendshipXp(from, to, 40);

            to.pendingNotifications.add("You received a " + item.name + " from " + from.username + "!");
            return "You gave " + to.username + " a " + item.name + ".";
        }

        public java.util.List<Gift> getGiftLog() {
            return giftLog;
        }

        public java.util.List<Gift> getGiftHistory(Player p1, Player p2) {
            return giftLog.stream()
                .filter(g -> (g.from.equals(p1) && g.to.equals(p2)) || (g.from.equals(p2) && g.to.equals(p1)))
                .collect(Collectors.toList());
        }

        public String rateGift(Player rater, Gift gift, int rating) {
            if (gift.rating != 0) return "You have already rated this gift.";
            if (rating >= 1 && rating <= 5) {
                gift.rating = rating;
                int xpGained = 20 * (rating - 3);
                addFriendshipXp(rater, gift.from, xpGained);
                return "Your rating was submitted. Friendship XP changed by " + xpGained + ".";
            }
            return "Rating must be between 1 and 5.";
        }

        public String proposeMarriage(Player proposer, Player proposedTo, Ring ring) {
            if (proposer.isMarried || proposedTo.isMarried) return "One of the players is already married.";
            if (!proposer.gender.equals("Male") || !proposedTo.gender.equals("Female"))
                return "Genders are not suitable for marriage in this context.";
            if (proposer.getFriendshipLevel(proposedTo) < 3)
                return "Friendship level must be at least 3 to propose.";
            if (!proposer.hasItem(ring.name, 1)) return "You don't have a ring to propose with.";
            if (pendingProposals.containsKey(proposedTo)) return proposedTo.username + " already has a pending proposal.";

            pendingProposals.put(proposedTo, new MarriageProposal(proposer, proposedTo, ring));
            return "A marriage proposal has been sent to " + proposedTo.username + ".";
        }

        public String respondToProposal(Player responder, boolean accept) {
            MarriageProposal proposal = pendingProposals.get(responder);
            if (proposal == null) return "There are no pending marriage proposals for you.";

            pendingProposals.remove(responder);
            if (accept) {
                proposal.proposer.removeItem(proposal.ring.name, 1);
                responder.addItem(proposal.ring);

                responder.friendshipLevels.put(proposal.proposer, 4);
                proposal.proposer.friendshipLevels.put(responder, 4);
                responder.isMarried = true;
                proposal.proposer.isMarried = true;

                return "You are now married to " + proposal.proposer.username + "!";
            } else {
                responder.friendshipLevels.put(proposal.proposer, 0);
                responder.friendshipXp.put(proposal.proposer, 0);
                proposal.proposer.friendshipLevels.put(responder, 0);
                proposal.proposer.friendshipXp.put(responder, 0);

                return "You rejected the marriage proposal. Your friendship has been reset.";
            }
        }

        public MarriageProposal getPendingProposal(Player p) {
            return pendingProposals.get(p);
        }

        public void addTalkMessage(Player from, Player to, String message) {
            String key = getHistoryKey(from.username, to.username);
            talkHistories.computeIfAbsent(key, k -> new ArrayList<>()).add(from.username + ": " + message);
            addFriendshipXp(from, to, 20);
            to.pendingNotifications.add(from.username + " says: " + message);
        }

        public java.util.List<String> getTalkHistory(Player p1, Player p2) {
            String key = getHistoryKey(p1.username, p2.username);
            return talkHistories.getOrDefault(key, new ArrayList<>());
        }
    }

    //================================================================================
    // Game Characters (View & Controller)
    //================================================================================

    static class GameCharacter extends Actor {
        Player data;
        Texture texture;
        float speed;
        boolean isPlayerControlled = false;
        boolean isPaused = false;

        private float timeToChangeDirection = 0;
        private final Vector2 direction = new Vector2();

        public GameCharacter(Player data, Texture texture, float x, float y, float speed, float scale, boolean isPlayer) {
            this.data = data;
            this.texture = texture;
            this.speed = speed;
            this.isPlayerControlled = isPlayer;

            setPosition(x, y);
            setSize(texture.getWidth() * scale, texture.getHeight() * scale);
            setOrigin(getWidth() / 2, getHeight() / 2);
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            if (isPaused) return;

            if (isPlayerControlled) {
                if (Gdx.input.isKeyPressed(Input.Keys.W)) moveBy(0, speed * delta);
                if (Gdx.input.isKeyPressed(Input.Keys.S)) moveBy(0, -speed * delta);
                if (Gdx.input.isKeyPressed(Input.Keys.A)) moveBy(-speed * delta, 0);
                if (Gdx.input.isKeyPressed(Input.Keys.D)) moveBy(speed * delta, 0);
            } else {
                timeToChangeDirection -= delta;
                if (timeToChangeDirection <= 0) {
                    timeToChangeDirection = MathUtils.random(2f, 5f);
                    direction.set(MathUtils.random(-1f, 1f), MathUtils.random(-1f, 1f)).nor();
                }
                moveBy(direction.x * 80f * delta, direction.y * 80f * delta);
            }
            keepInBounds();
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            Color color = getColor();
            batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
            batch.draw(texture, getX(), getY(), getOriginX(), getOriginY(),
                getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation(),
                0, 0, texture.getWidth(), texture.getHeight(), false, false);
        }

        public void keepInBounds() {
            setX(MathUtils.clamp(getX(), 0, WORLD_WIDTH - getWidth()));
            setY(MathUtils.clamp(getY(), 0, WORLD_HEIGHT - getHeight()));
        }
    }

    //================================================================================
    // Graphics and UI (View & Controller)
    //================================================================================

    private SpriteBatch batch;
    private Stage gameStage;
    private Stage uiStage;
    private Skin skin;
    private Actor proximityMenu;
    private Map<String, Texture> textureAssets;
    private ImageButton notificationButton;

    private InteractionManager interactionManager;
    private GameCharacter currentPlayerCharacter;

    @Override
    public void create() {
        try {
            batch = new SpriteBatch();

            loadAssets();

            gameCamera = new OrthographicCamera();
            gameViewport = new FitViewport(1280, 720, gameCamera);
            uiViewport = new ScreenViewport();

            gameStage = new Stage(gameViewport, batch);
            uiStage = new Stage(uiViewport, batch);

            InputMultiplexer multiplexer = new InputMultiplexer();
            multiplexer.addProcessor(uiStage);
            multiplexer.addProcessor(new GameInputAdapter());
            Gdx.input.setInputProcessor(multiplexer);

            interactionManager = new InteractionManager();

            GameCharacter player = new GameCharacter(interactionManager.getPlayer("player"), textureAssets.get("player"), WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 250f, 0.4f, true);
            GameCharacter sebastian = new GameCharacter(interactionManager.getPlayer("Sebastian"), textureAssets.get("Sebastian"), 800, 800, 250f, 1.2f, false);
            GameCharacter leah = new GameCharacter(interactionManager.getPlayer("Leah"), textureAssets.get("Leah"), 1200, 500, 250f, 1.2f, false);
            GameCharacter abigail = new GameCharacter(interactionManager.getPlayer("Abigail"), textureAssets.get("Abigail"), 400, 1000, 250f, 1.2f, false);

            gameStage.addActor(player);
            gameStage.addActor(sebastian);
            gameStage.addActor(leah);
            gameStage.addActor(abigail);

            currentPlayerCharacter = player;

            setupUI();
            checkForPendingProposals(currentPlayerCharacter.data);

        } catch (GdxRuntimeException e) {
            Gdx.app.error("FATAL ERROR", "Could not load assets. Make sure the 'assets' directory is configured correctly.", e);
            Gdx.app.exit();
        }
    }

    private void loadAssets() {
        textureAssets = new HashMap<>();
        textureAssets.put("player", new Texture(Gdx.files.internal("player.png")));
        textureAssets.put("Sebastian", new Texture(Gdx.files.internal("sebastian.png")));
        textureAssets.put("Leah", new Texture(Gdx.files.internal("leah.png")));
        textureAssets.put("Abigail", new Texture(Gdx.files.internal("abigail.png")));
        textureAssets.put("tile", new Texture(Gdx.files.internal("grass.png")));
        textureAssets.put("flower_icon", new Texture(Gdx.files.internal("flower_icon.png")));
        textureAssets.put("heart_icon", new Texture(Gdx.files.internal("heart_icon.png")));
        textureAssets.put("notification_icon", new Texture(Gdx.files.internal("notification_icon.png")));

        skin = new Skin(Gdx.files.internal("uiskin.json"));
    }

    private void setupUI() {
        Table uiTable = new Table();
        uiTable.setFillParent(true);
        uiStage.addActor(uiTable);

        TextButton friendsButton = new TextButton("Friends", skin);
        friendsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // *** COMPILATION FIX ***
                // Correctly check if the window already exists to prevent duplicates.
                boolean windowExists = false;
                for (Actor a : uiStage.getActors()) {
                    if (a instanceof Window && ((Window) a).getTitleLabel().getText().toString().equals("Friends List")) {
                        windowExists = true;
                        break;
                    }
                }
                if (!windowExists) {
                    createFriendsListWindow();
                }
            }
        });

        notificationButton = new ImageButton(new Image(textureAssets.get("notification_icon")).getDrawable());
        notificationButton.setVisible(false);
        notificationButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                createNotificationWindow();
            }
        });

        uiTable.top().left();
        uiTable.add(friendsButton).pad(10);
        uiTable.add(notificationButton).pad(10).size(32, 32);

        uiStage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (proximityMenu != null && proximityMenu.hasParent()) {
                    if (event.getTarget() != proximityMenu && !proximityMenu.isAscendantOf(event.getTarget())) {
                        ((GameCharacter)proximityMenu.getUserObject()).isPaused = false;
                        proximityMenu.remove();
                        proximityMenu = null;
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private class GameInputAdapter extends InputAdapter {
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            if (button == Input.Buttons.RIGHT) {
                Vector2 worldCoords = gameStage.screenToStageCoordinates(new Vector2(screenX, screenY));
                Actor hitActor = gameStage.hit(worldCoords.x, worldCoords.y, true);

                if (hitActor instanceof GameCharacter && hitActor != currentPlayerCharacter) {
                    GameCharacter character = (GameCharacter) hitActor;

                    if (proximityMenu != null && proximityMenu.hasParent()) {
                        ((GameCharacter)proximityMenu.getUserObject()).isPaused = false;
                        proximityMenu.remove();
                    }

                    float distance = new Vector2(currentPlayerCharacter.getX(), currentPlayerCharacter.getY())
                        .dst(character.getX(), character.getY());

                    if (distance <= INTERACTION_DISTANCE) {
                        createProximityMenu(character, screenX, Gdx.graphics.getHeight() - screenY);
                    } else {
                        showNotification("You are too far away to interact with " + character.data.username, 3);
                    }
                    return true;
                }
            }
            return false;
        }
    }


    private void checkForPendingProposals(Player p) {
        MarriageProposal proposal = interactionManager.getPendingProposal(p);
        if (proposal != null) {
            createProposalDialog(p);
        }
    }

    @Override
    public void resize(int width, int height) {
        if (gameViewport != null) gameViewport.update(width, height, true);
        if (uiViewport != null) uiViewport.update(width, height, true);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.1f, 0.3f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        updateGameLogic(Gdx.graphics.getDeltaTime());

        gameCamera.position.set(currentPlayerCharacter.getX() + currentPlayerCharacter.getOriginX(),
            currentPlayerCharacter.getY() + currentPlayerCharacter.getOriginY(), 0);
        gameCamera.position.x = MathUtils.clamp(gameCamera.position.x, gameViewport.getWorldWidth() / 2f, WORLD_WIDTH - gameViewport.getWorldWidth() / 2f);
        gameCamera.position.y = MathUtils.clamp(gameCamera.position.y, gameViewport.getWorldHeight() / 2f, WORLD_HEIGHT - gameViewport.getWorldHeight() / 2f);
        gameCamera.update();

        gameViewport.apply();
        batch.setProjectionMatrix(gameCamera.combined);
        batch.begin();
        Texture tile = textureAssets.get("tile");
        for (float x = 0; x < WORLD_WIDTH; x += TILE_SIZE) {
            for (float y = 0; y < WORLD_HEIGHT; y += TILE_SIZE) {
                batch.draw(tile, x, y, TILE_SIZE, TILE_SIZE);
            }
        }
        batch.end();
        gameStage.draw();

        uiViewport.apply();
        uiStage.draw();
    }

    private void updateGameLogic(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            switchPlayer();
        }
        gameStage.act(delta);
        uiStage.act(delta);
        notificationButton.setVisible(!currentPlayerCharacter.data.pendingNotifications.isEmpty());
    }

    private void switchPlayer() {
        int currentIndex = gameStage.getActors().indexOf(currentPlayerCharacter, true);
        currentPlayerCharacter.isPlayerControlled = false;

        int nextIndex = (currentIndex + 1) % gameStage.getActors().size;
        currentPlayerCharacter = (GameCharacter)gameStage.getActors().get(nextIndex);
        currentPlayerCharacter.isPlayerControlled = true;

        showNotification("Controlling: " + currentPlayerCharacter.data.username, 2);

        checkForPendingProposals(currentPlayerCharacter.data);
    }

    private void createFriendsListWindow() {
        Window friendsWindow = new Window("Friends List", skin);
        friendsWindow.setSize(650, 500);
        friendsWindow.setPosition(Gdx.graphics.getWidth() / 2f - 325, Gdx.graphics.getHeight() / 2f - 250);
        friendsWindow.setMovable(true);
        friendsWindow.setModal(true);

        Table content = new Table();

        for (Actor actor : gameStage.getActors()) {
            if (actor instanceof GameCharacter) {
                GameCharacter friendChar = (GameCharacter) actor;
                if (friendChar.equals(currentPlayerCharacter)) continue;
                Player friend = friendChar.data;

                int level = currentPlayerCharacter.data.getFriendshipLevel(friend);
                int xp = currentPlayerCharacter.data.friendshipXp.getOrDefault(friend, 0);
                String marriageStatus = friend.isMarried ? " (Married)" : "";
                Label friendLabel = new Label(friend.username + " | Level: " + level + " | XP: " + xp + marriageStatus, skin);

                TextButton talkButton = new TextButton("Talk", skin);
                talkButton.addListener(new ChangeListener() { @Override public void changed(ChangeEvent e, Actor a) {
                    float distance = new Vector2(currentPlayerCharacter.getX(), currentPlayerCharacter.getY()).dst(friendChar.getX(), friendChar.getY());
                    if (distance > INTERACTION_DISTANCE) {
                        showNotification("You are too far away to talk to " + friend.username, 3);
                        return;
                    }
                    createTalkWindow(friendChar);
                }});

                TextButton giftButton = new TextButton("Gift", skin);
                giftButton.addListener(new ChangeListener() { @Override public void changed(ChangeEvent e, Actor a) {
                    float distance = new Vector2(currentPlayerCharacter.getX(), currentPlayerCharacter.getY()).dst(friendChar.getX(), friendChar.getY());
                    if (distance > INTERACTION_DISTANCE) {
                        showNotification("You are too far away to give a gift to " + friend.username, 3);
                        return;
                    }
                    createGiftSelectionWindow(friendChar);
                }});

                TextButton historyButton = new TextButton("History", skin);
                historyButton.addListener(new ChangeListener() { @Override public void changed(ChangeEvent e, Actor a) { createGiftHistoryWindow(friend); } });

                content.add(friendLabel).left().pad(5).expandX();
                content.add(talkButton).pad(5);
                content.add(giftButton).pad(5);
                content.add(historyButton).pad(5);
                content.row();
            }
        }

        ScrollPane scrollPane = new ScrollPane(content, skin);
        friendsWindow.add(scrollPane).expand().fill().row();

        TextButton receivedGiftsButton = new TextButton("Received Gifts", skin);
        receivedGiftsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                createReceivedGiftsWindow();
            }
        });

        TextButton closeButton = new TextButton("Close", skin);
        closeButton.addListener(new ChangeListener() { @Override public void changed(ChangeEvent e, Actor a) { friendsWindow.remove(); } });

        Table bottomTable = new Table();
        bottomTable.add(receivedGiftsButton).pad(10);
        bottomTable.add(closeButton).pad(10);
        friendsWindow.add(bottomTable);

        uiStage.addActor(friendsWindow);
    }

    private void createProximityMenu(GameCharacter character, float screenX, float screenY) {
        character.isPaused = true;
        Player friend = character.data;
        Table menuTable = new Table(skin);
        menuTable.setBackground("default-pane");
        menuTable.setUserObject(character);

        menuTable.add(createHugButton(character)).fillX().row();
        menuTable.add(createGiveFlowerButton(character)).fillX().row();
        if (currentPlayerCharacter.data.gender.equals("Male") && friend.gender.equals("Female") && !currentPlayerCharacter.data.isMarried && !friend.isMarried) {
            menuTable.add(createProposeButton(friend)).fillX().row();
        }

        menuTable.pack();
        menuTable.setPosition(screenX, screenY - menuTable.getHeight());

        this.proximityMenu = menuTable;
        uiStage.addActor(proximityMenu);
    }

    private void createVisualEffect(Texture texture, float worldX, float worldY, float duration) {
        Image icon = new Image(texture);
        icon.setSize(48, 48);
        icon.setOrigin(Align.center);
        icon.setPosition(worldX - icon.getWidth()/2, worldY + 50);

        icon.addAction(Actions.sequence(
            Actions.parallel(
                Actions.moveBy(0, 60, duration, Interpolation.pow2Out),
                Actions.fadeOut(duration, Interpolation.pow2In)
            ),
            Actions.removeActor()
        ));
        gameStage.addActor(icon);
    }

    private TextButton createHugButton(GameCharacter friendChar) {
        TextButton hugButton = new TextButton("Hug", skin);
        hugButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (proximityMenu != null) {
                    ((GameCharacter)proximityMenu.getUserObject()).isPaused = false;
                    proximityMenu.remove();
                }

                if (currentPlayerCharacter.data.getFriendshipLevel(friendChar.data) >= 2) {
                    interactionManager.addFriendshipXp(currentPlayerCharacter.data, friendChar.data, 60);
                    showNotification("You hugged " + friendChar.data.username + "!", 3);

                    currentPlayerCharacter.isPaused = true;
                    friendChar.isPaused = true;
                    Vector2 playerOriginalPos = new Vector2(currentPlayerCharacter.getX(), currentPlayerCharacter.getY());
                    Vector2 friendOriginalPos = new Vector2(friendChar.getX(), friendChar.getY());
                    Vector2 meetingPoint = playerOriginalPos.cpy().lerp(friendOriginalPos, 0.5f);

                    Action hugAction = Actions.sequence(
                        Actions.moveTo(meetingPoint.x - 20, meetingPoint.y, 0.5f, Interpolation.sine),
                        Actions.run(() -> createVisualEffect(textureAssets.get("heart_icon"), meetingPoint.x, meetingPoint.y, 1.0f)),
                        Actions.delay(1.0f),
                        Actions.moveTo(playerOriginalPos.x, playerOriginalPos.y, 0.5f, Interpolation.sine),
                        Actions.run(() -> currentPlayerCharacter.isPaused = false)
                    );
                    Action friendHugAction = Actions.sequence(
                        Actions.moveTo(meetingPoint.x + 20, meetingPoint.y, 0.5f, Interpolation.sine),
                        Actions.delay(1.0f),
                        Actions.moveTo(friendOriginalPos.x, friendOriginalPos.y, 0.5f, Interpolation.sine),
                        Actions.run(() -> friendChar.isPaused = false)
                    );

                    currentPlayerCharacter.addAction(hugAction);
                    friendChar.addAction(friendHugAction);

                } else {
                    showNotification("Friendship level must be at least 2 to hug.", 3);
                }
            }
        });
        return hugButton;
    }

    private TextButton createGiveFlowerButton(GameCharacter friendChar) {
        TextButton flowerButton = new TextButton("Give Flower", skin);
        flowerButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (proximityMenu != null) {
                    ((GameCharacter)proximityMenu.getUserObject()).isPaused = false;
                    proximityMenu.remove();
                }
                Item flower = currentPlayerCharacter.data.getItem("Flower");
                if (flower != null) {
                    String result = interactionManager.giveGift(currentPlayerCharacter.data, friendChar.data, flower);
                    showNotification(result, 3);
                    createVisualEffect(textureAssets.get("flower_icon"), friendChar.getX(), friendChar.getY(), 2.0f);
                } else {
                    showNotification("You don't have any flowers to give.", 3);
                }
            }
        });
        return flowerButton;
    }

    private TextButton createProposeButton(Player friend) {
        TextButton proposeButton = new TextButton("Propose", skin);
        proposeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (proximityMenu != null) {
                    ((GameCharacter)proximityMenu.getUserObject()).isPaused = false;
                    proximityMenu.remove();
                }
                Item ringItem = currentPlayerCharacter.data.getItem("Diamond Ring");
                if (ringItem instanceof Ring) {
                    String result = interactionManager.proposeMarriage(currentPlayerCharacter.data, friend, (Ring) ringItem);
                    showNotification(result, 4);
                } else {
                    showNotification("You don't have a ring to propose!", 3);
                }
            }
        });
        return proposeButton;
    }

    private void createTalkWindow(GameCharacter friendChar) {
        friendChar.isPaused = true;
        Window talkWindow = new Window("Chat with " + friendChar.data.username, skin);
        talkWindow.setSize(500, 450);
        talkWindow.setPosition(Gdx.graphics.getWidth() / 2f - 250, Gdx.graphics.getHeight() / 2f - 225);
        talkWindow.setMovable(true);

        TextArea historyArea = new TextArea("", skin);
        historyArea.setDisabled(true);

        java.util.List<String> history = interactionManager.getTalkHistory(currentPlayerCharacter.data, friendChar.data);
        history.forEach(msg -> historyArea.appendText(msg + "\n"));

        TextField messageField = new TextField("", skin);
        TextButton sendButton = new TextButton("Send", skin);

        ScrollPane scrollPane = new ScrollPane(historyArea, skin);
        scrollPane.setFadeScrollBars(false);

        sendButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String message = messageField.getText();
                if (!message.trim().isEmpty()) {
                    interactionManager.addTalkMessage(currentPlayerCharacter.data, friendChar.data, message);
                    historyArea.appendText(currentPlayerCharacter.data.username + ": " + message + "\n");
                    messageField.setText("");
                }
            }
        });

        talkWindow.add(scrollPane).expand().fill().colspan(2).height(300).row();
        talkWindow.add(messageField).expandX().fillX().pad(5);
        talkWindow.add(sendButton).pad(5).row();
        TextButton closeButton = new TextButton("Close", skin);
        closeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent e, Actor a) {
                friendChar.isPaused = false; // Unpause on close
                talkWindow.remove();
            }
        });
        talkWindow.add(closeButton).colspan(2).pad(10);
        uiStage.addActor(talkWindow);
        scrollPane.layout();
        scrollPane.setScrollPercentY(100);
    }

    private void createGiftSelectionWindow(GameCharacter friendChar) {
        friendChar.isPaused = true;
        Window giftWindow = new Window("Select a gift for " + friendChar.data.username, skin);
        giftWindow.setSize(350, 400);
        giftWindow.setPosition(Gdx.graphics.getWidth() / 2f - 175, Gdx.graphics.getHeight() / 2f - 200);

        Table itemsTable = new Table();
        for (Item item : currentPlayerCharacter.data.inventory) {
            if (item instanceof Ring) continue;

            Label itemLabel = new Label(item.name, skin);
            TextButton giveButton = new TextButton("Give", skin);
            giveButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    String result = interactionManager.giveGift(currentPlayerCharacter.data, friendChar.data, item);
                    showNotification(result, 3);
                    friendChar.isPaused = false;
                    giftWindow.remove();
                }
            });
            itemsTable.add(itemLabel).pad(5).left();
            itemsTable.add(giveButton).pad(5).row();
        }

        ScrollPane scrollPane = new ScrollPane(itemsTable, skin);
        giftWindow.add(scrollPane).expand().fill().row();

        TextButton cancelButton = new TextButton("Cancel", skin);
        cancelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent e, Actor a) {
                friendChar.isPaused = false; // Unpause on cancel
                giftWindow.remove();
            }
        });
        giftWindow.add(cancelButton).pad(10);
        uiStage.addActor(giftWindow);
    }

    private void createGiftHistoryWindow(Player friend) {
        Window historyWindow = new Window("Gift History with " + friend.username, skin);
        historyWindow.setSize(550, 400);
        historyWindow.setPosition(Gdx.graphics.getWidth() / 2f - 275, Gdx.graphics.getHeight() / 2f - 200);

        Table giftsTable = new Table();
        java.util.List<Gift> history = interactionManager.getGiftHistory(currentPlayerCharacter.data, friend);

        if (history.isEmpty()) {
            giftsTable.add(new Label("No gifts exchanged yet.", skin));
        } else {
            for (Gift gift : history) {
                String text = "From: " + gift.from.username + ", To: " + gift.to.username + ", Item: " + gift.item.name;
                giftsTable.add(new Label(text, skin)).left().pad(5);

                if (gift.to.equals(currentPlayerCharacter.data) && gift.rating == 0) {
                    TextButton rateButton = new TextButton("Rate", skin);
                    rateButton.addListener(new ChangeListener() {
                        @Override
                        public void changed(ChangeEvent event, Actor actor) {
                            createRatingDialog(gift, friend, historyWindow);
                        }
                    });
                    giftsTable.add(rateButton).pad(5);
                } else if (gift.rating != 0) {
                    giftsTable.add(new Label("Rated: " + gift.rating + "/5", skin)).pad(5);
                } else {
                    giftsTable.add();
                }
                giftsTable.row();
            }
        }

        ScrollPane scrollPane = new ScrollPane(giftsTable, skin);
        historyWindow.add(scrollPane).expand().fill().row();

        TextButton closeButton = new TextButton("Close", skin);
        closeButton.addListener(new ChangeListener() { @Override public void changed(ChangeEvent e, Actor a) { historyWindow.remove(); } });
        historyWindow.add(closeButton).pad(10);
        uiStage.addActor(historyWindow);
    }

    private void createReceivedGiftsWindow() {
        Window receivedWindow = new Window("Received Gifts", skin);
        receivedWindow.setSize(550, 400);
        receivedWindow.setPosition(Gdx.graphics.getWidth() / 2f - 275, Gdx.graphics.getHeight() / 2f - 200);
        receivedWindow.setModal(true);

        Table giftsTable = new Table();
        java.util.List<Gift> receivedGifts = interactionManager.getGiftLog().stream()
            .filter(g -> g.to.equals(currentPlayerCharacter.data))
            .collect(Collectors.toList());

        if (receivedGifts.isEmpty()) {
            giftsTable.add(new Label("You have not received any gifts yet.", skin));
        } else {
            for (Gift gift : receivedGifts) {
                String text = "From: " + gift.from.username + ", Item: " + gift.item.name;
                giftsTable.add(new Label(text, skin)).left().pad(5);

                if (gift.rating == 0) {
                    TextButton rateButton = new TextButton("Rate", skin);
                    rateButton.addListener(new ChangeListener() {
                        @Override
                        public void changed(ChangeEvent event, Actor actor) {
                            createRatingDialog(gift, gift.from, receivedWindow);
                        }
                    });
                    giftsTable.add(rateButton).pad(5);
                } else {
                    giftsTable.add(new Label("Rated: " + gift.rating + "/5", skin)).pad(5);
                }
                giftsTable.row();
            }
        }

        ScrollPane scrollPane = new ScrollPane(giftsTable, skin);
        receivedWindow.add(scrollPane).expand().fill().row();

        TextButton closeButton = new TextButton("Close", skin);
        closeButton.addListener(new ChangeListener() { @Override public void changed(ChangeEvent e, Actor a) { receivedWindow.remove(); } });
        receivedWindow.add(closeButton).pad(10);
        uiStage.addActor(receivedWindow);
    }

    private void createNotificationWindow() {
        if (currentPlayerCharacter.data.pendingNotifications.isEmpty()) return;

        Window notificationWindow = new Window("Inbox", skin);
        notificationWindow.setSize(400, 300);
        notificationWindow.setPosition(Gdx.graphics.getWidth() / 2f - 200, Gdx.graphics.getHeight() / 2f - 150);
        notificationWindow.setModal(true);

        List<String> list = new List<>(skin);
        list.setItems(currentPlayerCharacter.data.pendingNotifications.toArray(new String[0]));

        ScrollPane scrollPane = new ScrollPane(list, skin);
        notificationWindow.add(scrollPane).expand().fill().row();

        TextButton closeButton = new TextButton("Clear & Close", skin);
        closeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                currentPlayerCharacter.data.pendingNotifications.clear();
                notificationWindow.remove();
            }
        });
        notificationWindow.add(closeButton).pad(10);
        uiStage.addActor(notificationWindow);
    }

    private void createRatingDialog(Gift gift, Player friend, Window parentWindow) {
        Dialog dialog = new Dialog("Rate Gift: " + gift.item.name, skin);
        dialog.text("How much did you like this gift?");
        Table buttonTable = new Table();
        for (int i = 1; i <= 5; i++) {
            final int rating = i;
            TextButton rateNumButton = new TextButton(String.valueOf(i), skin);
            rateNumButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    String result = interactionManager.rateGift(currentPlayerCharacter.data, gift, rating);
                    showNotification(result, 4);
                    dialog.hide();
                    parentWindow.remove();
                    // Re-open the appropriate window after rating
                    if (parentWindow.getTitleLabel().getText().toString().contains("History")) {
                        createGiftHistoryWindow(friend);
                    } else {
                        createReceivedGiftsWindow();
                    }
                }
            });
            buttonTable.add(rateNumButton).pad(5);
        }
        dialog.getContentTable().row();
        dialog.getContentTable().add(buttonTable).row();
        dialog.button("Cancel").padTop(20);
        dialog.show(uiStage);
    }

    private void createProposalDialog(Player responder) {
        MarriageProposal proposal = interactionManager.getPendingProposal(responder);
        if (proposal == null) return;

        Dialog dialog = new Dialog("Marriage Proposal", skin) {
            @Override
            protected void result(Object object) {
                String result = interactionManager.respondToProposal(responder, (Boolean) object);
                showNotification(result, 5);
            }
        };
        dialog.text(proposal.proposer.username + " wants to marry you. Do you accept?");
        dialog.button("I Accept", true);
        dialog.button("I Reject", false);
        dialog.show(uiStage);
    }

    private void showNotification(String message, float duration) {
        Label notificationLabel = new Label(message, skin, "default");
        notificationLabel.setColor(Color.WHITE);

        Table table = new Table(skin);
        table.setBackground("default-pane");
        table.add(notificationLabel).pad(10);

        table.setPosition(Gdx.graphics.getWidth() / 2f, 40, Align.center);
        table.getColor().a = 0f;
        Action action = Actions.sequence(
            Actions.fadeIn(0.5f),
            Actions.delay(duration),
            Actions.fadeOut(0.5f),
            Actions.removeActor()
        );
        table.addAction(action);

        uiStage.addActor(table);
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (gameStage != null) gameStage.dispose();
        if (uiStage != null) uiStage.dispose();
        if (skin != null) skin.dispose();
        if (textureAssets != null) {
            for (Texture texture : textureAssets.values()) {
                texture.dispose();
            }
        }
    }
}
