package com.Client;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.common.Network;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Main extends ApplicationAdapter {
    // Core
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont font;
    private OrthographicCamera gameCamera;
    private Stage uiStage;
    private Skin skin;
    public static Client client;
    private String host = System.getProperty("host", "127.0.0.1");

    // Screens
    private enum ScreenState { LOGIN, LOBBY, PLAY }
    private ScreenState currentState = ScreenState.LOGIN;

    // Session
    public static Network.PlayerInfo myPlayerInfo;
    private String sessionId;
    private boolean reconnecting = false;

    // World
    private final Map<String, Network.PlayerPosition> playerPositions = new ConcurrentHashMap<>();
    private final Map<String, Network.NPCPosition> npcPositions = new ConcurrentHashMap<>();
    private final Map<String, Network.WorldItem> worldItems = new ConcurrentHashMap<>();
    private final List<Network.LobbyInfo> lobbyList = new CopyOnWriteArrayList<>();
    private final List<String> chatMessages = new CopyOnWriteArrayList<>();
    private final List<Reaction> liveReactions = new ArrayList<>();
    private final List<Particle> weatherParticles = new ArrayList<>();
    private int hourOfDay = 9;
    private Network.Weather weather = Network.Weather.SUNNY;
    private long lastMoveSentTime = 0;
    private float camZoom = 1.0f;

    // World constants (mirror server)
    private static final float WORLD_MIN_X = 0f, WORLD_MAX_X = 1600f;
    private static final float WORLD_MIN_Y = 0f, WORLD_MAX_Y = 900f;
    private static final float HOUSE_X = 500f, HOUSE_Y = 120f, HOUSE_W = 240f, HOUSE_H = 180f;

    // Inventory (client mirror)
    private final Map<String,Integer> myInventory = new ConcurrentHashMap<>();
    private int myMoney = 0;

    // UI
    private Table loginTable, lobbyTable, gameHudTable;
    private Table lobbyListTableRef;
    private TextField usernameField, passwordField, chatField;
    private Label statusLabel;
    private Label timeLabel, weatherLabel;
    private com.badlogic.gdx.scenes.scene2d.ui.List<String> onlineList;
    private Label scoreboardLabel;
    private boolean chatTyping = false;

    // Windows
    private Window shopWindow, tradeWindow, missionWindow, voteWindow, interactionWindow, toastWindow, bagWindow;
    private Window emoteWindow; private Table emoteTable;
    private Table tradeTable, missionTable, voteVotesTable, shopItemsTable, bagTable, tradeHistoryTable;
    private Label voteTitle, voteTimer;
    private boolean votePanelVisible = false;

    // Textures
    private Texture tileTex, houseTex, defaultPlayerTex, player1Tex, player2Tex, appleTex;
    private final Map<Integer, Texture> npcTex = new HashMap<>();

    // Music
    private Music music;

    // Emotes
    private static final String[] DEFAULT_EMOTES = new String[]{
        "👍","❤️","😂","😮","😢","😡","👋","🙌","💀","😴","GG","LOL","BRB"
    };

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        font = new BitmapFont();
        gameCamera = new OrthographicCamera();
        gameCamera.setToOrtho(false, 1280, 720);
        uiStage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(uiStage);
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        loadAssets();

        buildLoginUI();
        buildLobbyUI();
        buildGameUI();
        buildTradesUI();
        buildMissionsUI();
        buildVoteUI();
        buildInteractionWindow();
        buildBagUI();
        buildEmoteUI(); // NEW

        client = new Client(16384, 8192);
        Network.register(client.getKryo());
        client.start();
        client.addListener(new ClListener());
        connectToServer();

        FileHandle m = Gdx.files.internal("music.mp3");
        if (m.exists()) {
            music = Gdx.audio.newMusic(m);
            music.setLooping(true); music.setVolume(0.4f); music.play();
        }
    }

    private void loadAssets() {
        tileTex = loadOrNull("tile.png");      // background tile
        houseTex = loadOrNull("house.png");
        defaultPlayerTex = loadOrNull("player_default.png");
        player1Tex = loadOrNull("player1.png");
        player2Tex = loadOrNull("player2.png");
        appleTex  = loadOrNull("apple.png");
        for (int i=1;i<=4;i++) npcTex.put(i, loadOrNull("npc"+i+".png"));
    }
    private Texture loadOrNull(String path) {
        FileHandle fh = Gdx.files.internal(path);
        if (fh.exists()) { Texture t = new Texture(fh); t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear); return t; }
        return null;
    }

    private Texture pickPlayerTexture(String username) {
        if ("player1".equalsIgnoreCase(username) && player1Tex != null) return player1Tex;
        if ("player2".equalsIgnoreCase(username) && player2Tex != null) return player2Tex;
        if (defaultPlayerTex != null) return defaultPlayerTex;
        return null;
    }
    private Texture pickNpcTexture(String npcId) {
        int idx = Math.abs(npcId.hashCode()) % 4 + 1;
        return npcTex.get(idx);
    }

    private void connectToServer() {
        statusLabel.setText("Connecting to " + host + "...");
        new Thread(() -> {
            try {
                client.connect(5000, host, Network.TCP_PORT, Network.UDP_PORT);
            } catch (IOException e) {
                Gdx.app.postRunnable(() -> statusLabel.setText("Connection failed: " + e.getMessage()));
            }
        }).start();
    }

    // ---- Login UI ----
    private void buildLoginUI() {
        loginTable = new Table();
        loginTable.setFillParent(true);
        usernameField = new TextField("", skin);
        passwordField = new TextField("", skin);
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');
        statusLabel = new Label("Enter your credentials.", skin);

        loginTable.add(new Label("Username:", skin)).padRight(10);
        loginTable.add(usernameField).width(220).row();
        loginTable.add(new Label("Password:", skin)).padRight(10).padTop(10);
        loginTable.add(passwordField).width(220).row();
        loginTable.add(statusLabel).colspan(2).padTop(10).row();

        TextButton loginBtn = new TextButton("Login", skin);
        loginBtn.addListener(new ChangeListener() { @Override public void changed(ChangeEvent event, Actor actor) { doLogin(); }});
        TextButton signupBtn = new TextButton("Sign up", skin);
        signupBtn.addListener(new ChangeListener() { @Override public void changed(ChangeEvent event, Actor actor) { showSignupDialog(); }});
        Table buttons = new Table();
        buttons.add(loginBtn).padRight(8);
        buttons.add(signupBtn);
        loginTable.add(buttons).colspan(2).padTop(8);

        uiStage.addActor(loginTable);
    }

    private void doLogin() {
        String user = trim(usernameField.getText());
        String pass = passwordField.getText() == null ? "" : passwordField.getText();
        if (user.isEmpty() || pass.isEmpty()) { statusLabel.setText("Username & password are required."); return; }
        Network.LoginRequest req = new Network.LoginRequest();
        req.username = user;
        req.passwordHash = sha256(pass);
        client.sendTCP(req);
        statusLabel.setText("Logging in...");
    }

    private void showSignupDialog() {
        final TextField suUser = new TextField("", skin);
        final TextField suPass = new TextField("", skin);
        suPass.setPasswordMode(true); suPass.setPasswordCharacter('*');
        final TextField suNick = new TextField("", skin);

        Dialog d = new Dialog("Sign up", skin) {
            @Override protected void result(Object obj) {
                if (Boolean.TRUE.equals(obj)) {
                    String u = trim(suUser.getText());
                    String p = suPass.getText();
                    String n = trim(suNick.getText());
                    if (u.isEmpty() || p.isEmpty()) { statusLabel.setText("Username/Password required."); return; }
                    Network.SignupRequest s = new Network.SignupRequest();
                    s.username = u; s.passwordHash = sha256(p); s.nickname = n.isEmpty() ? u : n;
                    client.sendTCP(s); statusLabel.setText("Signing up...");
                }
            }
        };
        Table t = d.getContentTable();
        t.add(new Label("Username:", skin)).pad(4); t.add(suUser).width(220).row();
        t.add(new Label("Password:", skin)).pad(4); t.add(suPass).width(220).row();
        t.add(new Label("Nickname:", skin)).pad(4); t.add(suNick).width(220).row();
        d.button("Create", true); d.button("Cancel", false);
        d.key(Input.Keys.ENTER, true); d.key(Input.Keys.ESCAPE, false);
        d.show(uiStage);
    }
    private void showJoinByIdDialog() {
        final TextField idField = new TextField("", skin);
        idField.setMessageText("Lobby ID (number)");
        final TextField passField = new TextField("", skin);
        passField.setMessageText("Password (optional)");
        passField.setPasswordMode(true);
        passField.setPasswordCharacter('*');

        Dialog d = new Dialog("Join by ID", skin) {
            @Override protected void result(Object obj) {
                if (Boolean.TRUE.equals(obj)) {
                    String idTxt = idField.getText() == null ? "" : idField.getText().trim();
                    int id;
                    try { id = Integer.parseInt(idTxt); }
                    catch (Exception e) { statusLabel.setText("Invalid ID."); return; }

                    Network.LobbyJoinRequest req = new Network.LobbyJoinRequest();
                    req.lobbyId = id;
                    String pw = passField.getText();
                    if (pw != null && !pw.isEmpty()) req.password = pw;
                    client.sendTCP(req);
                }
            }
        };
        Table t = d.getContentTable();
        t.add(new Label("Lobby ID:", skin)).pad(4); t.add(idField).width(200).row();
        t.add(new Label("Password:", skin)).pad(4); t.add(passField).width(200).row();
        d.button("Join", true); d.button("Cancel", false);
        d.key(Input.Keys.ENTER, true); d.key(Input.Keys.ESCAPE, false);
        d.show(uiStage);
    }

    // ---- Lobby UI ----
    private void buildLobbyUI() {
        lobbyTable = new Table();
        lobbyTable.setFillParent(true);
        lobbyTable.setVisible(false);
        lobbyTable.pad(20);

        Table mainLobbyContent = new Table();

        lobbyListTableRef = new Table();
        lobbyListTableRef.top();
        updateLobbyUI(lobbyListTableRef);
        ScrollPane lobbyScrollPane = new ScrollPane(lobbyListTableRef, skin);
        lobbyScrollPane.setFadeScrollBars(false);

        Table onlinePlayersPanel = new Table();
        onlinePlayersPanel.top();
        onlinePlayersPanel.add(new Label("Online Players", skin)).padBottom(10).row();
        onlineList = new com.badlogic.gdx.scenes.scene2d.ui.List<>(skin);
        ScrollPane onlineScrollPane = new ScrollPane(onlineList, skin);
        onlineScrollPane.setFadeScrollBars(false);
        onlinePlayersPanel.add(onlineScrollPane).grow().pad(5);

        mainLobbyContent.add(lobbyScrollPane).width(Gdx.graphics.getWidth() * 0.6f).growY();
        mainLobbyContent.add(onlinePlayersPanel).width(Gdx.graphics.getWidth() * 0.35f).growY().padLeft(10);

        lobbyTable.add(mainLobbyContent).grow().row();

        Table controls = new Table();
        TextButton newLobbyBtn = new TextButton("New Lobby", skin);
        newLobbyBtn.addListener(new ChangeListener() { @Override public void changed(ChangeEvent event, Actor actor) { showCreateLobbyDialog(); }});
        TextButton refreshBtn = new TextButton("Refresh", skin);
        refreshBtn.addListener(new ChangeListener() { @Override public void changed(ChangeEvent event, Actor actor) {
            client.sendTCP(new Network.LobbyListRequest());
            client.sendTCP(new Network.OnlineListRequest());
        }});
        TextButton joinByIdBtn = new TextButton("Join by ID", skin);
        joinByIdBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { showJoinByIdDialog(); }
        });
        controls.add(joinByIdBtn).pad(5);

        controls.add(newLobbyBtn).pad(5);
        controls.add(refreshBtn).pad(5);
        lobbyTable.add(controls).padTop(10);

        uiStage.addActor(lobbyTable);
    }

    private void updateLobbyUI(Table tableToUpdate) {
        tableToUpdate.clearChildren();
        tableToUpdate.add(new Label("— Game Lobbies —", skin)).colspan(7).padBottom(20).row();
        tableToUpdate.add(new Label("ID", skin)).pad(5);
        tableToUpdate.add(new Label("Name", skin)).pad(5);
        tableToUpdate.add(new Label("Players", skin)).pad(5);
        tableToUpdate.add(new Label("Status", skin)).pad(5);
        tableToUpdate.add(new Label("Join", skin)).pad(5);
        tableToUpdate.add(new Label("\uD83D\uDD12", skin)).pad(5);
        tableToUpdate.add(new Label("Start", skin)).pad(5).row();

        for (Network.LobbyInfo info : lobbyList) {
            tableToUpdate.add(new Label(String.valueOf(info.id), skin)).pad(5);
            tableToUpdate.add(new Label(info.name, skin)).pad(5);
            tableToUpdate.add(new Label(info.playerCount + "/" + info.capacity, skin)).pad(5);
            Label status = new Label(info.gameStarted ? "In Progress" : (info.isPrivate ? "Private" : "Public"), skin);
            status.setColor(info.gameStarted ? Color.SCARLET : (info.isPrivate ? Color.GOLD : Color.CHARTREUSE));
            tableToUpdate.add(status).pad(5);

            final int id = info.id;
            final boolean isPriv = info.isPrivate;
            TextButton joinBtn = new TextButton("Join", skin);
            joinBtn.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, Actor actor) {
                    Network.LobbyJoinRequest req = new Network.LobbyJoinRequest();
                    req.lobbyId = id;
                    if (isPriv) showPasswordDialog(pw -> { req.password = pw; client.sendTCP(req); });
                    else client.sendTCP(req);
                }
            });
            tableToUpdate.add(joinBtn).pad(5);
            tableToUpdate.add(new Label(isPriv ? "YES" : "NO", skin)).pad(5);

            if (myPlayerInfo != null && myPlayerInfo.lobbyId == info.id) {
                TextButton startBtn = new TextButton("Start", skin);
                startBtn.setDisabled(info.playerCount < 2 || info.gameStarted);
                startBtn.addListener(new ChangeListener() { @Override public void changed(ChangeEvent event, Actor actor) {
                    client.sendTCP(new Network.LobbyStartGameRequest());
                }});
                tableToUpdate.add(startBtn).pad(5);
            } else {
                tableToUpdate.add(new Label("", skin)).pad(5);
            }
            tableToUpdate.row();
        }
    }

    private void showPasswordDialog(java.util.function.Consumer<String> cb) {
        final TextField tf = new TextField("", skin);
        tf.setPasswordMode(true); tf.setPasswordCharacter('*');
        Dialog d = new Dialog("Enter Password", skin) {
            @Override protected void result(Object obj) { if (Boolean.TRUE.equals(obj)) cb.accept(tf.getText()); }
        };
        d.getContentTable().add(tf).width(220);
        d.button("OK", true); d.button("Cancel", false);
        d.key(Input.Keys.ENTER, true); d.key(Input.Keys.ESCAPE, false);
        d.show(uiStage);
    }

    private void showCreateLobbyDialog() {
        final TextField name = new TextField((myPlayerInfo!=null?myPlayerInfo.nickname:"") + "'s Room", skin);
        final CheckBox priv = new CheckBox("Private", skin);
        final CheckBox invis = new CheckBox("Invisible (Join by ID only)", skin);
        final TextField pass = new TextField("", skin);
        pass.setPasswordMode(true); pass.setPasswordCharacter('*');

        Dialog d = new Dialog("Create Lobby", skin) {
            @Override protected void result(Object obj) {
                if (Boolean.TRUE.equals(obj)) {
                    Network.LobbyCreateRequest req = new Network.LobbyCreateRequest();
                    req.name = name.getText();
                    req.isPrivate = priv.isChecked();
                    req.password = pass.getText();
                    req.isVisible = !invis.isChecked();
                    client.sendTCP(req);
                }
            }
        };
        Table t = d.getContentTable();
        t.add(new Label("Name:", skin)).pad(5); t.add(name).width(220).row();
        t.add(priv).colspan(2).left().pad(5).row();
        t.add(invis).colspan(2).left().pad(5).row();
        t.add(new Label("Password:", skin)).pad(5); t.add(pass).width(220).row();
        d.button("Create", true); d.button("Cancel", false);
        d.key(Input.Keys.ENTER, true); d.key(Input.Keys.ESCAPE, false);
        d.show(uiStage);
    }

    // ---- Game HUD ----
    private void buildGameUI() {
        gameHudTable = new Table();
        gameHudTable.setFillParent(true);
        gameHudTable.setVisible(false);

        Table topRight = new Table(skin);
        timeLabel = new Label("Time: " + hourOfDay + ":00", skin);
        weatherLabel = new Label("Weather: " + weather, skin);
        scoreboardLabel = new Label("", skin);
        scoreboardLabel.setAlignment(Align.right);
        topRight.add(timeLabel).right().row();
        topRight.add(weatherLabel).right().row();
        topRight.add(new Label("Scoreboard", skin)).right().row();
        topRight.add(scoreboardLabel).width(260).right();
        gameHudTable.add(topRight).top().right().pad(10);
        gameHudTable.row();

        // Bottom-Left Chat
        Table chatBox = new Table(skin);
        chatBox.setBackground("default-pane");
        final Label chatLog = new Label("", skin);
        chatLog.setWrap(true);
        chatLog.setAlignment(Align.topLeft);
        chatLog.getStyle().font.getData().markupEnabled = true;
        chatField = new TextField("", skin);
        chatField.setMessageText("ENTER to chat | !global | /w user msg | @mention");
        chatField.setDisabled(true);
        ScrollPane chatScrollPane = new ScrollPane(chatLog, skin);
        chatScrollPane.setFadeScrollBars(false);
        chatBox.add(chatScrollPane).width(380).height(160).row();
        chatBox.add(chatField).width(380);
        gameHudTable.add(chatBox).expand().bottom().left().pad(10);
        uiStage.addActor(gameHudTable);

        // Toolbar
        Table toolbar = new Table(skin);
        toolbar.setBackground("default-pane");
        toolbar.setPosition(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() - 40, Align.center);
        toolbar.add(makeBtn("Bag", this::toggleBag)).pad(2);
        toolbar.add(makeBtn("Shop", this::toggleShop)).pad(2);
        toolbar.add(makeBtn("Trades", this::toggleTrades)).pad(2);
        toolbar.add(makeBtn("Missions", this::toggleMissions)).pad(2);
        toolbar.add(makeBtn("Vote", this::toggleVotePanel)).pad(2);
        toolbar.add(makeBtn("Emotes", this::toggleEmotes)).pad(2); // NEW
        toolbar.add(makeBtn("Save", () -> client.sendTCP(new Network.SaveGameRequest()))).pad(2); // NEW
        toolbar.add(makeBtn("Load", () -> client.sendTCP(new Network.LoadGameRequest()))).pad(2); // NEW
        toolbar.add(makeBtn("Leave", () -> client.sendTCP(new Network.LobbyLeaveRequest()))).pad(2);
        uiStage.addActor(toolbar);

        // Shop Window
        shopWindow = new Window("Shop", skin);
        shopWindow.setVisible(false);
        shopWindow.setSize(520, 360);
        shopWindow.setPosition(20, 260);
        shopItemsTable = new Table(skin);
        ScrollPane shopScrollPane = new ScrollPane(shopItemsTable, skin);
        shopScrollPane.setFadeScrollBars(false);
        shopWindow.add(shopScrollPane).expand().fill();
        uiStage.addActor(shopWindow);

        // Toast
        toastWindow = new Window("", skin);
        toastWindow.setVisible(false);
        toastWindow.setMovable(false);
        toastWindow.setPosition(Gdx.graphics.getWidth()/2f-150, Gdx.graphics.getHeight()-120);
        toastWindow.add(new Label("", skin)).pad(8);
        uiStage.addActor(toastWindow);

        // update chat view
        uiStage.addAction(new Action() {
            @Override public boolean act(float delta) {
                StringBuilder sb = new StringBuilder();
                for (String s : chatMessages) sb.append(s).append('\n');
                chatLog.setText(sb.toString());
                return false;
            }
        });
    }

    private TextButton makeBtn(String t, Runnable r) {
        TextButton b = new TextButton(t, skin);
        b.addListener(new ChangeListener() { @Override public void changed(ChangeEvent e, Actor a) { r.run(); }});
        return b;
    }

    // ---- Bag UI ----
    private void buildBagUI() {
        bagWindow = new Window("Bag", skin);
        bagWindow.setVisible(false);
        bagWindow.setSize(340, 300);
        bagWindow.setPosition(20, 20);
        bagTable = new Table(skin);
        ScrollPane sc = new ScrollPane(bagTable, skin);
        sc.setFadeScrollBars(false);
        bagWindow.add(sc).width(320).height(260);
        uiStage.addActor(bagWindow);
    }
    private void toggleBag() {
        bagWindow.setVisible(!bagWindow.isVisible());
        if (bagWindow.isVisible()) refreshBagTable();
    }
    private void refreshBagTable() {
        bagTable.clear();
        bagTable.add(new Label("Item", skin)).pad(4);
        bagTable.add(new Label("Count", skin)).pad(4).row();
        if (myInventory.isEmpty()) {
            bagTable.add(new Label("-", skin)).colspan(2).center().pad(8);
        } else {
            for (Map.Entry<String,Integer> e : myInventory.entrySet()) {
                bagTable.add(new Label(e.getKey(), skin)).left().pad(4);
                bagTable.add(new Label(String.valueOf(e.getValue()), skin)).right().pad(4).row();
            }
        }
        bagTable.row();
        bagTable.add(new Label("Money: $" + myMoney, skin)).colspan(2).right().padTop(8);
    }

    // ---- Emote UI ----
    private void buildEmoteUI() {
        emoteWindow = new Window("Emotes", skin);
        emoteWindow.setVisible(false); emoteWindow.setSize(300, 220); emoteWindow.setPosition(370, 20);
        emoteTable = new Table(skin);
        int perRow = 5, c=0; for (String e : DEFAULT_EMOTES) {
            TextButton b = new TextButton(e, skin);
            b.addListener(new ChangeListener(){ @Override public void changed(ChangeEvent e1, Actor a){ sendEmote(((TextButton)a).getText().toString()); emoteWindow.setVisible(false); }});
            emoteTable.add(b).pad(3).width(48);
            if (++c % perRow == 0) emoteTable.row();
        }
        // custom short text
        final TextField custom = new TextField("", skin);
        custom.setMessageText("custom (<=16 chars)");
        TextButton send = new TextButton("Send", skin);
        send.addListener(new ChangeListener(){ @Override public void changed(ChangeEvent e, Actor a){ String t=trim(custom.getText()); if(!t.isEmpty()){ sendEmote(t); emoteWindow.setVisible(false);} }});
        emoteTable.row(); emoteTable.add(custom).colspan(3).pad(4); emoteTable.add(send).pad(4);

        ScrollPane sc = new ScrollPane(emoteTable, skin); sc.setFadeScrollBars(false);
        emoteWindow.add(sc).width(280).height(180);
        uiStage.addActor(emoteWindow);
    }
    private void toggleEmotes(){ emoteWindow.setVisible(!emoteWindow.isVisible()); }
    private void sendEmote(String text){ Network.EmoteRequest r = new Network.EmoteRequest(); r.text = text; client.sendTCP(r); }

    // ---- Trades UI ----
    private void buildTradesUI() {
        tradeWindow = new Window("Trades", skin);
        tradeWindow.setVisible(false);
        tradeWindow.setSize(560, 420);
        tradeWindow.setPosition(560, 260);
        tradeTable = new Table(skin);
        ScrollPane sc = new ScrollPane(tradeTable, skin);
        sc.setFadeScrollBars(false);

        tradeHistoryTable = new Table(skin);
        ScrollPane scHist = new ScrollPane(tradeHistoryTable, skin);
        scHist.setFadeScrollBars(false);

        TextButton newTradeBtn = new TextButton("New Offer", skin);
        newTradeBtn.addListener(new ChangeListener() { @Override public void changed(ChangeEvent e, Actor a) { showNewTradeDialog(); }});

        TextButton refresh = new TextButton("Refresh", skin);
        refresh.addListener(new ChangeListener() { @Override public void changed(ChangeEvent event, Actor actor) { client.sendTCP(new Network.TradeListRequest()); }});

        TextButton histBtn = new TextButton("History", skin);
        histBtn.addListener(new ChangeListener(){ @Override public void changed(ChangeEvent e, Actor a){ client.sendTCP(new Network.TradeHistoryRequest()); }});

        tradeWindow.add(new Label("-- Pending Offers --", skin)).left().padTop(4).row();
        tradeWindow.add(sc).width(540).height(200).row();
        Table buttons = new Table();
        buttons.add(newTradeBtn).pad(4);
        buttons.add(refresh).pad(4);
        buttons.add(histBtn).pad(4);
        tradeWindow.add(buttons).pad(6).row();
        tradeWindow.add(new Label("-- Completed Trades --", skin)).left().padTop(6).row();
        tradeWindow.add(scHist).width(540).height(120);
        uiStage.addActor(tradeWindow);
    }

    private void showNewTradeDialog() {
        Dialog d = new Dialog("New Trade", skin) {
            TextField toUser, apple, bread, flower, price, note;
            {
                toUser = new TextField("", skin); toUser.setMessageText("Target username (required)");
                apple  = new TextField("0", skin);
                bread  = new TextField("0", skin);
                flower = new TextField("0", skin);
                price  = new TextField("10", skin);
                note   = new TextField("", skin);
                getContentTable().add(new Label("To:", skin)).left(); getContentTable().add(toUser).width(220).row();
                getContentTable().add(new Label("Apple:", skin)).left(); getContentTable().add(apple).width(80).row();
                getContentTable().add(new Label("Bread:", skin)).left(); getContentTable().add(bread).width(80).row();
                getContentTable().add(new Label("Flower:", skin)).left(); getContentTable().add(flower).width(80).row();
                getContentTable().add(new Label("Price:", skin)).left(); getContentTable().add(price).width(100).row();
                getContentTable().add(new Label("Note:", skin)).left(); getContentTable().add(note).width(220).row();
                button("Offer", true); button("Cancel", false);
                key(Input.Keys.ENTER, true); key(Input.Keys.ESCAPE, false);
            }
            @Override protected void result(Object obj) {
                if (Boolean.TRUE.equals(obj)) {
                    Network.TradeOffer o = new Network.TradeOffer();
                    o.to = trim(toUser.getText());
                    o.price = safeParseInt(price.getText(), 0);
                    Map<String,Integer> b = new HashMap<>();
                    putIfPos(b, "Apple", safeParseInt(apple.getText(), 0));
                    putIfPos(b, "Bread", safeParseInt(bread.getText(), 0));
                    putIfPos(b, "Flower", safeParseInt(flower.getText(), 0));
                    o.bundle = b; o.note = trim(note.getText());
                    client.sendTCP(o);
                }
            }
        };
        d.show(uiStage);
    }
    private static void putIfPos(Map<String,Integer> m, String k, int v){ if (v>0) m.put(k,v); }
    private static int safeParseInt(String s, int def){ try { return Integer.parseInt(trim(s)); } catch(Exception e){ return def; } }

    private void toggleTrades() {
        tradeWindow.setVisible(!tradeWindow.isVisible());
        if (tradeWindow.isVisible()) client.sendTCP(new Network.TradeListRequest());
    }

    private void updateTradeWindow(Network.TradeListResponse resp) {
        tradeTable.clear();
        tradeTable.add(new Label("ID", skin)).pad(4);
        tradeTable.add(new Label("From", skin)).pad(4);
        tradeTable.add(new Label("To", skin)).pad(4);
        tradeTable.add(new Label("Bundle", skin)).pad(4);
        tradeTable.add(new Label("Price", skin)).pad(4);
        tradeTable.add(new Label("Action", skin)).pad(4).row();

        for (Network.TradeOffer offer : resp.pending) {
            tradeTable.add(new Label(String.valueOf(offer.id), skin)).pad(4);
            tradeTable.add(new Label(offer.from, skin)).pad(4);
            tradeTable.add(new Label(offer.to, skin)).pad(4);
            tradeTable.add(new Label(prettyBundle(offer.bundle), skin)).pad(4);
            tradeTable.add(new Label(String.valueOf(offer.price), skin)).pad(4);

            if (myPlayerInfo != null && offer.to != null && offer.to.equals(myPlayerInfo.username)) {
                TextButton accept = new TextButton("Accept", skin);
                accept.addListener(new ChangeListener() {
                    @Override public void changed(ChangeEvent e, Actor a) {
                        Network.TradeDecision d = new Network.TradeDecision();
                        d.id = offer.id; d.accept = true; client.sendTCP(d);
                    }
                });
                TextButton reject = new TextButton("Reject", skin);
                reject.addListener(new ChangeListener() {
                    @Override public void changed(ChangeEvent e, Actor a) {
                        Network.TradeDecision d = new Network.TradeDecision();
                        d.id = offer.id; d.accept = false; client.sendTCP(d);
                    }
                });
                Table t = new Table();
                t.add(accept).padRight(4); t.add(reject);
                tradeTable.add(t);
            } else {
                tradeTable.add(new Label("-", skin));
            }
            tradeTable.row();
        }
    }
    private String prettyBundle(Map<String,Integer> b){
        if (b==null || b.isEmpty()) return "-";
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String,Integer> e : b.entrySet()) sb.append(e.getKey()).append(" x").append(e.getValue()).append("  ");
        return sb.toString();
    }

    private void updateTradeHistory(java.util.List<Network.TradeRecord> records){
        tradeHistoryTable.clear();
        tradeHistoryTable.add(new Label("ID", skin)).pad(4);
        tradeHistoryTable.add(new Label("From", skin)).pad(4);
        tradeHistoryTable.add(new Label("To", skin)).pad(4);
        tradeHistoryTable.add(new Label("Bundle", skin)).pad(4);
        tradeHistoryTable.add(new Label("Price", skin)).pad(4);
        tradeHistoryTable.add(new Label("Time", skin)).pad(4).row();
        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("HH:mm:ss");
        for (Network.TradeRecord r : records) {
            tradeHistoryTable.add(new Label(String.valueOf(r.id), skin)).pad(4);
            tradeHistoryTable.add(new Label(r.from, skin)).pad(4);
            tradeHistoryTable.add(new Label(r.to, skin)).pad(4);
            tradeHistoryTable.add(new Label(prettyBundle(r.bundle), skin)).pad(4);
            tradeHistoryTable.add(new Label("$"+r.price, skin)).pad(4);
            tradeHistoryTable.add(new Label(fmt.format(new java.util.Date(r.at)), skin)).pad(4).row();
        }
    }

    // ---- Missions UI ----
    private void buildMissionsUI() {
        missionWindow = new Window("Missions", skin);
        missionWindow.setVisible(false);
        missionWindow.setSize(520, 360);
        missionWindow.setPosition(520, 20);
        missionTable = new Table(skin);
        ScrollPane sc = new ScrollPane(missionTable, skin);
        sc.setFadeScrollBars(false);
        missionWindow.add(sc).width(500).height(320);
        uiStage.addActor(missionWindow);
    }
    private void toggleMissions() {
        missionWindow.setVisible(!missionWindow.isVisible());
        if (missionWindow.isVisible()) client.sendTCP(new Network.MissionListRequest());
    }
    private void updateMissionWindow(Network.MissionUpdate update) {
        missionTable.clear();
        missionTable.top().left();
        missionTable.add(new Label("-- Available Missions --", skin)).colspan(4).pad(10).row();
        for (Network.MissionDef def : update.available) {
            missionTable.add(new Label(def.title, skin)).left();
            missionTable.add(new Label(def.description, skin)).width(260).top().left();
            missionTable.add(new Label(def.members.size() + "/" + def.capacity, skin));
            TextButton joinBtn = new TextButton("Join", skin);
            joinBtn.addListener(new ChangeListener() { @Override public void changed(ChangeEvent e, Actor a) {
                Network.MissionJoinLeave req = new Network.MissionJoinLeave(); req.missionId = def.id; req.join = true; client.sendTCP(req);
            }});
            missionTable.add(joinBtn).row();
        }
        missionTable.add(new Label("-- Active Missions --", skin)).colspan(4).pad(10).row();
        for (Network.MissionState state : update.active) {
            missionTable.add(new Label(state.title, skin)).left();
            missionTable.add(new Label(state.progress == null ? "" : state.progress, skin)).width(260).top().left();
            missionTable.add(new Label(String.join(", ", state.members), skin)).width(180).top();
            TextButton leaveBtn = new TextButton("Leave", skin);
            leaveBtn.addListener(new ChangeListener() { @Override public void changed(ChangeEvent e, Actor a) {
                Network.MissionJoinLeave req = new Network.MissionJoinLeave(); req.missionId = state.id; req.join = false; client.sendTCP(req);
            }});
            missionTable.add(leaveBtn).row();
        }
    }

    // ---- Vote UI ----
    private void buildVoteUI() {
        voteWindow = new Window("Vote", skin);
        voteWindow.setVisible(false);
        voteWindow.setSize(460, 360);
        voteWindow.setPosition(820, 420);
        voteTitle = new Label("", skin);
        voteTimer = new Label("", skin);
        voteVotesTable = new Table(skin);

        TextButton yes = new TextButton("YES", skin), no = new TextButton("NO", skin);
        yes.addListener(new ChangeListener() { @Override public void changed(ChangeEvent e, Actor a){ Network.VoteCast c=new Network.VoteCast(); c.vote=true; client.sendTCP(c);} });
        no.addListener(new ChangeListener()  { @Override public void changed(ChangeEvent e, Actor a){ Network.VoteCast c=new Network.VoteCast(); c.vote=false; client.sendTCP(c);} });

        TextButton startKickVote = new TextButton("Kick Player", skin);
        startKickVote.addListener(new ChangeListener() { @Override public void changed(ChangeEvent e, Actor a){ showKickDialog(); }});
        TextButton startContest = new TextButton("Start Contest", skin);
        startContest.addListener(new ChangeListener() { @Override public void changed(ChangeEvent e, Actor a){
            Dialog d = new Dialog("Contest topic", skin) {
                TextField subject = new TextField("Who should be Mayor?", skin);
                { getContentTable().add(subject).width(260); button("Start", true); button("Cancel", false); }
                @Override protected void result(Object obj) {
                    if (Boolean.TRUE.equals(obj)) { Network.VoteStart s = new Network.VoteStart(); s.type="contest"; s.subject = subject.getText(); client.sendTCP(s); }
                }
            }; d.show(uiStage);
        }});
        TextButton startTerminateVote = new TextButton("End Game", skin);
        startTerminateVote.addListener(new ChangeListener() { @Override public void changed(ChangeEvent e, Actor a){
            Network.VoteStart s = new Network.VoteStart(); s.type = "terminate"; client.sendTCP(s);
        }});

        Table btns = new Table();
        btns.add(yes).pad(4); btns.add(no).pad(4);
        ScrollPane sc = new ScrollPane(voteVotesTable, skin);
        sc.setFadeScrollBars(false);
        voteWindow.add(voteTitle).pad(6).row();
        voteWindow.add(voteTimer).pad(2).row();
        voteWindow.add(sc).width(420).height(160).row();
        voteWindow.add(btns).pad(6).row();
        voteWindow.add(startKickVote).pad(4);
        voteWindow.add(startContest).pad(4);
        voteWindow.add(startTerminateVote).pad(4);

        uiStage.addActor(voteWindow);
    }

    private void toggleVotePanel() { voteWindow.setVisible(!voteWindow.isVisible()); }

    private void updateVotePanel(Network.VoteUpdate vu) {
        voteTitle.setText("Vote: " + vu.type + (vu.targetUsername != null ? (" on " + vu.targetUsername) : "")
            + (vu.subject != null ? (" | " + vu.subject) : ""));
        voteTimer.setText("Time left: " + (vu.timeLeftMillis / 1000) + "s");
        voteVotesTable.clear();
        if (vu.votes != null)
            for (Map.Entry<String, Boolean> e : vu.votes.entrySet())
                voteVotesTable.add(new Label(e.getKey() + ": " + (e.getValue() == null ? "..." : (Boolean.TRUE.equals(e.getValue()) ? "YES" : "NO")), skin)).left().row();

        if (!votePanelVisible) { voteWindow.setVisible(true); votePanelVisible = true; }
    }

    // --- Interaction Window ---
    private void buildInteractionWindow() {
        interactionWindow = new Window("Interact", skin);
        interactionWindow.setVisible(false);
        uiStage.addActor(interactionWindow);
    }

    private void showInteractionMenu(String targetUsername) {
        interactionWindow.clear();
        interactionWindow.setVisible(true);

        TextButton hugBtn = new TextButton("Hug", skin);
        hugBtn.addListener(new ChangeListener() { @Override public void changed(ChangeEvent e, Actor a) {
            Network.PlayerInteraction req = new Network.PlayerInteraction();
            req.targetUsername = targetUsername; req.type = Network.InteractionType.HUG; client.sendTCP(req);
            interactionWindow.setVisible(false);
        }});
        interactionWindow.add(hugBtn).row();

        TextButton giftBtn = new TextButton("Give Gift", skin);
        giftBtn.addListener(new ChangeListener() { @Override public void changed(ChangeEvent e, Actor a) {
            Network.PlayerInteraction req = new Network.PlayerInteraction();
            req.targetUsername = targetUsername; req.type = Network.InteractionType.GIFT; req.item = "Flower"; client.sendTCP(req);
            interactionWindow.setVisible(false);
        }});
        interactionWindow.add(giftBtn).row();

        TextButton marryBtn = new TextButton("Propose", skin);
        marryBtn.addListener(new ChangeListener() { @Override public void changed(ChangeEvent e, Actor a) {
            Network.PlayerInteraction req = new Network.PlayerInteraction();
            req.targetUsername = targetUsername; req.type = Network.InteractionType.MARRY; client.sendTCP(req);
            interactionWindow.setVisible(false);
        }});
        interactionWindow.add(marryBtn).row();

        interactionWindow.setPosition(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
    }

    // ---- Render ----
    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        handleInput(dt);

        if (currentState == ScreenState.PLAY) renderGameWorld();

        uiStage.act(dt);
        uiStage.draw();

        if (currentState == ScreenState.PLAY) {
            batch.begin();
            Iterator<Reaction> it = liveReactions.iterator();
            while (it.hasNext()) {
                Reaction r = it.next();
                r.draw(batch, font);
                if (r.done()) it.remove();
            }
            batch.end();
        }
    }

    private void renderGameWorld() {
        if (myPlayerInfo != null) {
            Network.PlayerPosition me = playerPositions.get(myPlayerInfo.username);
            if (me != null) gameCamera.position.set(me.x, me.y, 0);
        }
        gameCamera.zoom = camZoom; gameCamera.update();
        batch.setProjectionMatrix(gameCamera.combined);
        shapes.setProjectionMatrix(gameCamera.combined);

        // --- tiled background ---
        if (tileTex != null) {
            batch.begin();
            float vw = gameCamera.viewportWidth * gameCamera.zoom;
            float vh = gameCamera.viewportHeight * gameCamera.zoom;
            float startX = gameCamera.position.x - vw/2f;
            float startY = gameCamera.position.y - vh/2f;
            int tw = tileTex.getWidth(), th = tileTex.getHeight();
            int sx = (int)Math.floor(startX / tw) - 1;
            int sy = (int)Math.floor(startY / th) - 1;
            int ex = (int)Math.ceil((startX + vw) / tw) + 1;
            int ey = (int)Math.ceil((startY + vh) / th) + 1;
            for (int ix = sx; ix <= ex; ix++) {
                for (int iy = sy; iy <= ey; iy++) {
                    float dx = ix * tw;
                    float dy = iy * th;
                    batch.draw(tileTex, dx, dy);
                }
            }
            batch.end();
        }

        // house
        batch.begin();
        if (houseTex != null) batch.draw(houseTex, HOUSE_X, HOUSE_Y, HOUSE_W, HOUSE_H);
        batch.end();

        if (!weatherParticles.isEmpty()) {
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            for (Particle p : weatherParticles) { p.update(gameCamera); p.draw(shapes); }
            shapes.end();
        }

        // Draw items (apples)
        batch.begin();
        for (Network.WorldItem it : worldItems.values()) {
            if ("Apple".equals(it.type) && appleTex != null) {
                float sz = 22f;
                batch.draw(appleTex, it.x - sz/2f, it.y - sz/2f, sz, sz);
            }
        }
        batch.end();

        // Draw entities
        batch.begin();
        for (Network.PlayerPosition p : playerPositions.values()) {
            Texture tx = pickPlayerTexture(p.username);
            float sz = 34f;
            if (tx != null) batch.draw(tx, p.x - sz/2f, p.y - sz/2f, sz, sz);
            else {
                batch.end();
                shapes.begin(ShapeRenderer.ShapeType.Filled);
                boolean me = (myPlayerInfo != null && p.username.equals(myPlayerInfo.username));
                shapes.setColor(Color.BLACK); shapes.circle(p.x, p.y, 12);
                shapes.setColor(me ? Color.GOLD : Color.SKY); shapes.circle(p.x, p.y, 10);
                shapes.end();
                batch.begin();
            }
            font.draw(batch, p.username, p.x - 24, p.y + 28);
        }
        for (Network.NPCPosition p : npcPositions.values()) {
            Texture tx = pickNpcTexture(p.npcId);
            float sz = 28f;
            if (tx != null) batch.draw(tx, p.x - sz/2f, p.y - sz/2f, sz, sz);
            else {
                batch.end();
                shapes.begin(ShapeRenderer.ShapeType.Filled);
                shapes.setColor(Color.BLACK); shapes.circle(p.x, p.y, 12);
                shapes.setColor(Color.LIME); shapes.circle(p.x, p.y, 10);
                shapes.end();
                batch.begin();
            }
            font.draw(batch, p.npcId, p.x - 24, p.y + 28);
        }
        batch.end();
    }

    // ---- Input ----
    private void handleInput(float delta) {
        if (currentState == ScreenState.LOGIN) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) doLogin();
        } else if (currentState == ScreenState.LOBBY) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                client.sendTCP(new Network.LobbyListRequest());
                client.sendTCP(new Network.OnlineListRequest());
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.P)) client.sendTCP(new Network.LobbyStartGameRequest());
        } else if (currentState == ScreenState.PLAY) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.I)) toggleBag();

            // Toggle chat typing
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                chatTyping = !chatTyping;
                chatField.setDisabled(!chatTyping);
                if (chatTyping) uiStage.setKeyboardFocus(chatField);
                else {
                    String txt = trim(chatField.getText());
                    if (!txt.isEmpty()) { sendChat(txt); chatField.setText(""); }
                    uiStage.setKeyboardFocus(null);
                }
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && chatTyping) {
                chatTyping = false; chatField.setDisabled(true); uiStage.setKeyboardFocus(null);
            }

            // movement with collision (client-side)
            if (!chatTyping) {
                Network.PlayerPosition myPos = myPlayerInfo == null ? null : playerPositions.get(myPlayerInfo.username);
                if (myPos == null) return;
                float speed = 220f * delta;
                float targetX = myPos.x, targetY = myPos.y;

                if (Gdx.input.isKeyPressed(Input.Keys.A)) targetX -= speed;
                if (Gdx.input.isKeyPressed(Input.Keys.D)) targetX += speed;

                // try X move
                targetX = clamp(targetX, WORLD_MIN_X, WORLD_MAX_X);
                if (!insideHouse(targetX, myPos.y)) myPos.x = targetX;

                if (Gdx.input.isKeyPressed(Input.Keys.W)) targetY += speed;
                if (Gdx.input.isKeyPressed(Input.Keys.S)) targetY -= speed;

                // try Y move
                targetY = clamp(targetY, WORLD_MIN_Y, WORLD_MAX_Y);
                if (!insideHouse(myPos.x, targetY)) myPos.y = targetY;
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.MINUS) || Gdx.input.isKeyJustPressed(Input.Keys.Q)) camZoom = Math.min(2f, camZoom + 0.1f);
            if (Gdx.input.isKeyJustPressed(Input.Keys.EQUALS) || Gdx.input.isKeyJustPressed(Input.Keys.E)) camZoom = Math.max(0.5f, camZoom - 0.1f);

            if (!chatTyping) {
//                if (Gdx.input.isKeyJustPressed(Input.Keys.B)) toggleShop();
//                if (Gdx.input.isKeyJustPressed(Input.Keys.T)) toggleTrades();
//                if (Gdx.input.isKeyJustPressed(Input.Keys.Y)) toggleMissions();
//                if (Gdx.input.isKeyJustPressed(Input.Keys.V)) toggleVotePanel();
                if (Gdx.input.isKeyJustPressed(Input.Keys.N)) client.sendTCP(new Network.NextDayRequest());
//                if (Gdx.input.isKeyJustPressed(Input.Keys.H)) toggleEmotes();
            }

            Network.PlayerPosition myPos = myPlayerInfo == null ? null : playerPositions.get(myPlayerInfo.username);
            if (myPos == null) return;
            if (System.currentTimeMillis() - lastMoveSentTime > 30) {
                Network.PlayerMove mv = new Network.PlayerMove(); mv.x = myPos.x; mv.y = myPos.y;
                client.sendUDP(mv); lastMoveSentTime = System.currentTimeMillis();
            }

            if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
                float mouseX = Gdx.input.getX(), mouseY = Gdx.input.getY();
                Vector3 world = gameCamera.unproject(new Vector3(mouseX, mouseY, 0));
                String clickedTarget = null;
                for (Network.PlayerPosition p : playerPositions.values()) {
                    if (!p.username.equals(myPlayerInfo.username) && com.badlogic.gdx.math.Vector2.dst(world.x, world.y, p.x, p.y) < 15) { clickedTarget = p.username; break; }
                }
                if (clickedTarget != null) showInteractionMenu(clickedTarget);
                else interactionWindow.setVisible(false);
            }
        }
    }

    private boolean insideHouse(float x, float y) {
        return x >= HOUSE_X && x <= HOUSE_X + HOUSE_W && y >= HOUSE_Y && y <= HOUSE_Y + HOUSE_H;
    }
    private static float clamp(float v, float a, float b){ return Math.max(a, Math.min(b, v)); }

    private void showKickDialog() {
        final TextField tf = new TextField("", skin);
        Dialog d = new Dialog("Start Kick Vote", skin) {
            @Override protected void result(Object obj) {
                if (Boolean.TRUE.equals(obj)) {
                    Network.VoteStart s = new Network.VoteStart(); s.type = "kick"; s.targetUsername = tf.getText(); client.sendTCP(s);
                }
            }
        };
        d.getContentTable().add(new Label("Username:", skin)).pad(5); d.getContentTable().add(tf).width(220).row();
        d.button("Start", true); d.button("Cancel", false);
        d.key(Input.Keys.ENTER, true); d.key(Input.Keys.ESCAPE, false); d.show(uiStage);
    }

    // ---- Chat ----
    private void sendChat(String raw) {
        if (raw == null || raw.trim().isEmpty()) return;
        Network.ChatMessage m = new Network.ChatMessage();
        if (raw.startsWith("/w ")) {
            String[] sp = raw.split(" ", 3);
            if (sp.length >= 3) { m.isPrivate = true; m.to = sp[1]; m.text = sp[2]; }
        } else {
            m.global = raw.startsWith("!"); m.text = m.global ? raw.substring(1) : raw;
            ArrayList<String> mentions = new ArrayList<>();
            for (String tok : raw.split(" ")) if (tok.startsWith("@") && tok.length() > 1) mentions.add(tok.substring(1));
            m.mentions = mentions;
        }
        client.sendTCP(m);
    }

    private void toggleShop() {
        shopWindow.setVisible(!shopWindow.isVisible());
        if (shopWindow.isVisible()) client.sendTCP(new Network.ShopStockRequest());
    }

    private void updateShopWindow(Network.ShopStockUpdate u) {
        shopItemsTable.clear();
        shopItemsTable.add(new Label("Item", skin)).pad(5);
        shopItemsTable.add(new Label("Stock", skin)).pad(5);
        shopItemsTable.add(new Label("Price", skin)).pad(5);
        shopItemsTable.add(new Label("Buy", skin)).pad(5).row();

        for (Map.Entry<String, Integer> entry : u.prices.entrySet()) {
            String item = entry.getKey();
            int price = entry.getValue();
            int stock = u.stock.getOrDefault(item, -1);

            shopItemsTable.add(new Label(item, skin));
            shopItemsTable.add(new Label(stock == -1 ? "Inf." : String.valueOf(stock), skin));
            shopItemsTable.add(new Label("$" + price, skin));
            TextButton buyBtn = new TextButton("Buy", skin);
            buyBtn.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent e, Actor a) {
                    Network.ShopBuyRequest req = new Network.ShopBuyRequest(); req.item = item; req.amount = 1; client.sendTCP(req);
                }
            });
            shopItemsTable.add(buyBtn).row();
        }
    }

    // ---- Listener ----
    private class ClListener extends Listener {
        @Override public void connected(Connection connection) {
            Gdx.app.postRunnable(() -> {
                statusLabel.setText("Connected! Please log in.");
                if (reconnecting && sessionId != null) {
                    Network.DCReconnectRequest r = new Network.DCReconnectRequest(); r.sessionId = sessionId; client.sendTCP(r); reconnecting = false;
                }
            });
        }

        @Override public void disconnected(Connection connection) {
            Gdx.app.postRunnable(() -> {
                chatMessages.add("[Net] Disconnected. Reconnecting...");
                reconnecting = true;
                setScreenState(ScreenState.LOGIN);
                statusLabel.setText("Disconnected. Trying to reconnect...");
                new Thread(() -> {
                    try { client.reconnect(); }
                    catch (IOException e) {
                        try { client.connect(5000, host, Network.TCP_PORT, Network.UDP_PORT); } catch (IOException ignored) {}
                    }
                }).start();
            });
        }

        @Override public void received(Connection c, Object o) {
            Gdx.app.postRunnable(() -> {
                if (o instanceof Network.SignupResponse) {
                    statusLabel.setText(((Network.SignupResponse) o).message);

                } else if (o instanceof Network.LoginResponse) {
                    Network.LoginResponse resp = (Network.LoginResponse) o;
                    if (resp.success) {
                        myPlayerInfo = resp.player; sessionId = resp.sessionId;
                        setScreenState(ScreenState.LOBBY);
                        client.sendTCP(new Network.LobbyListRequest());
                        client.sendTCP(new Network.OnlineListRequest());
                    } else statusLabel.setText("Login failed: " + resp.message);

                } else if (o instanceof Network.LobbyListResponse) {
                    Network.LobbyListResponse resp = (Network.LobbyListResponse) o;
                    lobbyList.clear(); lobbyList.addAll(resp.lobbies);
                    if (currentState == ScreenState.LOBBY) updateLobbyUI(lobbyListTableRef);

                } else if (o instanceof Network.LobbyJoinResponse) {
                    Network.LobbyJoinResponse r = (Network.LobbyJoinResponse) o;
                    if (!r.success) { Dialog d = new Dialog("Join Failed", skin); d.text(r.message); d.button("OK"); d.show(uiStage); }
                    else { client.sendTCP(new Network.OnlineListRequest()); client.sendTCP(new Network.LobbyListRequest()); }

                } else if (o instanceof Network.OnlineListUpdate) {
                    Network.OnlineListUpdate upd = (Network.OnlineListUpdate) o;
                    ArrayList<String> entries = new ArrayList<>();
                    for(Network.OnlineListUpdate.OnlineEntry e : upd.online) {
                        entries.add((e.online ? "●" : "○") + " " + e.nickname + " (" + e.username + ") " + (e.lobbyId == -1 ? "—" : ("Lobby " + e.lobbyName)));
                    }
                    onlineList.setItems(entries.toArray(new String[0]));

                } else if (o instanceof Network.LobbyUpdate) {
                    Network.LobbyUpdate update = (Network.LobbyUpdate) o;
                    if (update.lobbyId == -1) {
                        if (myPlayerInfo != null) myPlayerInfo.lobbyId = -1;
                        setScreenState(ScreenState.LOBBY);
                        client.sendTCP(new Network.LobbyListRequest());
                    } else {
                        if (myPlayerInfo != null) myPlayerInfo.lobbyId = update.lobbyId;
                    }

                } else if (o instanceof Network.GameStartedNotification) {
                    Network.GameStartedNotification notif = (Network.GameStartedNotification) o;
                    playerPositions.clear();
                    for (Network.PlayerPosition p : notif.initialMapState.positions) playerPositions.put(p.username, p);
                    npcPositions.clear();
                    for (Network.NPCPosition p : notif.initialMapState.npcs) npcPositions.put(p.npcId, p);
                    worldItems.clear();
                    if (notif.initialMapState.items != null)
                        for (Network.WorldItem it : notif.initialMapState.items) worldItems.put(it.id, it);
                    setScreenState(ScreenState.PLAY); ensureWeatherParticles();

                } else if (o instanceof Network.MapUpdate) {
                    Network.MapUpdate upd = (Network.MapUpdate) o;
                    hourOfDay = upd.hourOfDay; weather = upd.weather;
                    timeLabel.setText("Time: " + hourOfDay + ":00");
                    weatherLabel.setText("Weather: " + weather);
                    if (upd.positions != null)
                        for (Network.PlayerPosition p : upd.positions) {
                            if (myPlayerInfo != null && p.username.equals(myPlayerInfo.username)) continue;
                            playerPositions.put(p.username, p);
                        }
                    if (upd.npcs != null) for (Network.NPCPosition p : upd.npcs) npcPositions.put(p.npcId, p);
                    if (upd.items != null) {
                        worldItems.clear();
                        for (Network.WorldItem it : upd.items) worldItems.put(it.id, it);
                    }
                    ensureWeatherParticles();

                } else if (o instanceof Network.InventoryUpdate) {
                    Network.InventoryUpdate iu = (Network.InventoryUpdate) o;
                    if (myPlayerInfo != null && myPlayerInfo.username.equals(iu.username)) {
                        myInventory.clear(); if (iu.inv != null) myInventory.putAll(iu.inv);
                        myMoney = iu.money;
                        if (bagWindow.isVisible()) refreshBagTable();
                    }

                } else if (o instanceof Network.ChatMessage) {
                    Network.ChatMessage msg = (Network.ChatMessage) o;
                    boolean isMention = msg.mentions != null && myPlayerInfo != null && msg.mentions.contains(myPlayerInfo.username);
                    String prefix = msg.isPrivate ? "[Private] " : (msg.global ? "[Global] " : "");
                    String colored = (isMention ? "[#FFD54F]" : "") + prefix + msg.from + ": " + msg.text + (isMention? "[]" : "");
                    chatMessages.add(colored);
                    if (chatMessages.size() > 12) chatMessages.remove(0);
                    if (isMention) {
                        Network.PlayerPosition me = playerPositions.get(myPlayerInfo.username);
                        if (me != null) liveReactions.add(new Reaction(me.x, me.y + 35, "@"));
                    }

                } else if (o instanceof Network.TagToast) {
                    Network.TagToast t = (Network.TagToast) o;
                    showToast("Mention by " + t.from + ": " + t.text);

                } else if (o instanceof Network.PlayerReaction) {
                    Network.PlayerReaction r = (Network.PlayerReaction) o;
                    Network.PlayerPosition p = playerPositions.get(r.username);
                    if (p != null) liveReactions.add(new Reaction(p.x, p.y + 35, r.reactionText));

                } else if (o instanceof Network.NextDayBroadcast) {
                    Network.NextDayBroadcast b = (Network.NextDayBroadcast) o;
                    hourOfDay = b.newHourOfDay; weather = b.newWeather;
                    timeLabel.setText("Time: " + hourOfDay + ":00"); weatherLabel.setText("Weather: " + weather);
                    chatMessages.add("[System] A new day has begun."); ensureWeatherParticles();

                } else if (o instanceof Network.ScoreboardUpdate) {
                    Network.ScoreboardUpdate s = (Network.ScoreboardUpdate) o;
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < s.entries.size(); i++) {
                        Network.ScoreEntry e = s.entries.get(i);
                        sb.append(i + 1).append(". ").append(e.nickname).append(" $").append(e.money).append(" | M:").append(e.missionsCompleted).append("\n");
                    }
                    scoreboardLabel.setText(sb.toString());

                } else if (o instanceof Network.VoteUpdate) {
                    updateVotePanel((Network.VoteUpdate) o);

                } else if (o instanceof Network.VoteResult) {
                    Network.VoteResult r = (Network.VoteResult) o;
                    voteWindow.setVisible(false); votePanelVisible = false; chatMessages.add("[Vote] " + r.message);

                } else if (o instanceof Network.ShopStockUpdate) {
                    updateShopWindow((Network.ShopStockUpdate) o);

                } else if (o instanceof Network.ShopBuyResponse) {
                    chatMessages.add("[Shop] " + ((Network.ShopBuyResponse) o).message);

                } else if (o instanceof Network.TradeListResponse) {
                    updateTradeWindow((Network.TradeListResponse) o);

                } else if (o instanceof Network.TradeHistoryResponse) {
                    updateTradeHistory(((Network.TradeHistoryResponse)o).records);

                } else if (o instanceof Network.MissionUpdate) {
                    updateMissionWindow((Network.MissionUpdate) o);

                } else if (o instanceof Network.PopupNotification) {
                    Dialog d = new Dialog("Notification", skin);
                    d.text(((Network.PopupNotification)o).message);
                    d.button("OK").show(uiStage);
                }
            });
        }
    }

    private void showToast(String text) {
        if (!(toastWindow.getChildren().first() instanceof Label)) return;
        ((Label) toastWindow.getChildren().first()).setText(text);
        toastWindow.pack();
        toastWindow.setVisible(true);
        toastWindow.getColor().a = 1f;
        toastWindow.addAction(Actions.sequence(Actions.delay(2f), Actions.fadeOut(0.6f), Actions.visible(false)));
    }

    private void ensureWeatherParticles() {
        weatherParticles.clear();
        if (weather == Network.Weather.RAIN || weather == Network.Weather.STORM) for (int i=0;i<60;i++) weatherParticles.add(Particle.rain());
        else if (weather == Network.Weather.SNOW) for (int i=0;i<40;i++) weatherParticles.add(Particle.snow());
    }

    private void setScreenState(ScreenState s) {
        currentState = s;
        loginTable.setVisible(s == ScreenState.LOGIN);
        lobbyTable.setVisible(s == ScreenState.LOBBY);
        gameHudTable.setVisible(s == ScreenState.PLAY);
        if (s == ScreenState.LOBBY) updateLobbyUI(lobbyListTableRef);
    }

    @Override
    public void dispose() {
        try { client.stop(); } catch (Exception ignored) {}
        batch.dispose(); shapes.dispose(); font.dispose(); uiStage.dispose(); skin.dispose();
        if (music != null) music.dispose();
        if (tileTex!=null) tileTex.dispose();
        if (houseTex!=null) houseTex.dispose();
        if (defaultPlayerTex!=null) defaultPlayerTex.dispose();
        if (player1Tex!=null) player1Tex.dispose(); if (player2Tex!=null) player2Tex.dispose();
        if (appleTex!=null) appleTex.dispose();
        for (Texture t:npcTex.values()) if (t!=null) t.dispose();
    }

    // --- Utils ---
    private static String sha256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuilder hs = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hs.append('0');
                hs.append(hex);
            }
            return hs.toString();
        } catch (Exception ex) { throw new RuntimeException(ex); }
    }
    private static String trim(String s) { return s == null ? "" : s.trim(); }

    static class Reaction {
        float x, y; long born=System.currentTimeMillis(); String text;
        Reaction(float x, float y, String text) { this.x=x; this.y=y; this.text=text; }
        void draw(SpriteBatch batch, BitmapFont font) { float dt=(System.currentTimeMillis()-born)/1000f; font.draw(batch, text, x-4, y+dt*28f); }
        boolean done(){ return System.currentTimeMillis()-born > 5000; } // 5 seconds lifetime
    }

    static class Particle {
        float x,y,vx,vy; boolean snow; static Random R=new Random();
        static Particle rain(){ Particle p=new Particle(); p.x=R.nextFloat()*1600-200; p.y=R.nextFloat()*900; p.vx= -20+R.nextFloat()*20; p.vy= -220- R.nextFloat()*120; return p; }
        static Particle snow(){ Particle p=new Particle(); p.snow=true; p.x=R.nextFloat()*1600-200; p.y=R.nextFloat()*900; p.vx= -10+R.nextFloat()*20; p.vy= -40- R.nextFloat()*20; return p; }
        void update(OrthographicCamera cam){ x+=vx*Gdx.graphics.getDeltaTime(); y+=vy*Gdx.graphics.getDeltaTime(); if (y < cam.position.y - cam.viewportHeight/2){ y = cam.position.y + cam.viewportHeight/2; x = cam.position.x - cam.viewportWidth/2 + R.nextFloat()*cam.viewportWidth; } }
        void draw(ShapeRenderer s){ if (snow){ s.setColor(Color.WHITE); s.circle(x,y,2);} else { s.setColor(Color.CYAN); s.rectLine(x,y,x+vx*0.04f,y+vy*0.04f,1.5f);} }
    }
}
