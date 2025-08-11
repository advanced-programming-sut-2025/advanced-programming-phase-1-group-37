package com.mygame.stardew;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.*;
import java.util.stream.Collectors;

// کلاس اصلی که بازی را اجرا می‌کند
public class Main extends ApplicationAdapter {

    //================================================================================
    // بخش گرافیک و رندرینگ LibGDX
    //================================================================================

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private Viewport viewport;
    private BitmapFont font;
    private Texture playerTexture;
    private Map<NpcName, Texture> npcTextures;


    //================================================================================
    // بخش منطق بازی
    //================================================================================

    private Player player;
    private GameDate currentDate;
    private WeatherCondition currentWeather;
    private final Map<NpcName, GdxNpc> npcs = new HashMap<>();
    private GameDataRegistry gameData;

    //================================================================================
    // بخش مدیریت UI و وضعیت بازی
    //================================================================================

    private DialogueBox dialogueBox;
    private QuestUI questUI;
    private GiftUI giftUI;
    private FriendshipUI friendshipUI;
    private NotificationManager notificationManager;
    private QuestAcceptanceBox questAcceptanceBox;
    private QuestRewardBox questRewardBox;


    private boolean isQuestUiVisible = false;
    private boolean isFriendshipUiVisible = false;
    private boolean isGiftUiVisible = false;

    // ثابت‌های بازی
    private static final float PLAYER_SPEED = 200f;
    private static final float INTERACTION_DISTANCE = 60f;

    @Override
    public void create() {
        // --- مقداردهی اولیه موتور گرافیکی ---
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        font.getData().setScale(1.2f);
        camera = new OrthographicCamera();
        viewport = new FitViewport(800, 600, camera);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);

        // --- بارگذاری تصاویر ---
        try {
            playerTexture = new Texture("player.png");
            npcTextures = new HashMap<>();
            npcTextures.put(NpcName.SEBASTIAN, new Texture("sebastian.png"));
            npcTextures.put(NpcName.ABIGAIL, new Texture("abigail.png"));
            npcTextures.put(NpcName.HARVEY, new Texture("harvey.png"));
            npcTextures.put(NpcName.LEAH, new Texture("leah.png"));
            npcTextures.put(NpcName.ROBIN, new Texture("robin.png"));
        } catch (Exception e) {
            Gdx.app.error("Texture Loading", "Could not load character sprites. Make sure they are in the assets folder.", e);
            Gdx.app.exit();
        }

        // --- مقداردهی اولیه منطق بازی ---
        gameData = new GameDataRegistry();
        currentDate = new GameDate(1, Season.SPRING, 1);
        player = new Player("Farmer", new Vector2(400, 300));
        currentWeather = WeatherCondition.SUNNY;

        // --- ساخت و رجیستر کردن منطق NPC ها ---
        Npc.registerNpc(new Sebastian(gameData));
        Npc.registerNpc(new Abigail(gameData));
        Npc.registerNpc(new Harvey(gameData));
        Npc.registerNpc(new Leah(gameData));
        Npc.registerNpc(new Robin(gameData));

        // --- ساخت نمونه‌های گرافیکی NPC ها ---
        npcs.put(NpcName.SEBASTIAN, new GdxNpc(Npc.getNpc(NpcName.SEBASTIAN), new Vector2(200, 400), npcTextures.get(NpcName.SEBASTIAN)));
        npcs.put(NpcName.ABIGAIL, new GdxNpc(Npc.getNpc(NpcName.ABIGAIL), new Vector2(600, 400), npcTextures.get(NpcName.ABIGAIL)));
        npcs.put(NpcName.HARVEY, new GdxNpc(Npc.getNpc(NpcName.HARVEY), new Vector2(200, 150), npcTextures.get(NpcName.HARVEY)));
        npcs.put(NpcName.LEAH, new GdxNpc(Npc.getNpc(NpcName.LEAH), new Vector2(600, 150), npcTextures.get(NpcName.LEAH)));
        npcs.put(NpcName.ROBIN, new GdxNpc(Npc.getNpc(NpcName.ROBIN), new Vector2(400, 500), npcTextures.get(NpcName.ROBIN)));

        // --- افزودن آیتم‌های اولیه برای تست ---
        player.addItem(gameData.getItem("Iron Ore"), 55);
        player.addItem(gameData.getItem("Pumpkin Pie"), 2);
        player.addItem(gameData.getItem("Gold Bar"), 2);
        player.addItem(gameData.getItem("Salmon"), 5);
        player.addItem(gameData.getItem("Hardwood"), 15);
        player.addItem(gameData.getItem("Stone"), 150);
        player.addItem(gameData.getItem("Wool"), 1);

        // --- ساخت اجزای UI ---
        dialogueBox = new DialogueBox();
        notificationManager = new NotificationManager();
        questUI = new QuestUI();
        giftUI = new GiftUI();
        friendshipUI = new FriendshipUI();
        questAcceptanceBox = new QuestAcceptanceBox();
        questRewardBox = new QuestRewardBox();
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        handleInput(deltaTime);
        notificationManager.update(deltaTime);

        Gdx.gl.glClearColor(0.1f, 0.5f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        // --- رندر کردن تصاویر شخصیت‌ها ---
        batch.begin();
        batch.draw(playerTexture, player.position.x - 20, player.position.y - 20, 40, 40);
        for (GdxNpc npc : npcs.values()) {
            npc.renderSprite(batch);
        }
        batch.end();

        // --- رندر کردن اشکال (علامت‌های بالای سر) ---
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (GdxNpc npc : npcs.values()) {
            npc.renderMarker(shapeRenderer, player, currentDate);
        }
        shapeRenderer.end();


        // --- رندر کردن متن و UI ---
        batch.begin();
        font.draw(batch, currentDate + " | " + currentWeather, 10, viewport.getWorldHeight() - 10);
        font.draw(batch, "Gold: " + player.gold + "g", viewport.getWorldWidth() - 150, viewport.getWorldHeight() - 10);
        font.draw(batch, "WASD: Move | E: Interact | G: Gift | J: Quests | F: Friends | N: Next Day", 10, 20);
        batch.end();

        // رندر UI ها
        if (dialogueBox.isVisible()) dialogueBox.render(batch, shapeRenderer);
        if (questAcceptanceBox.isVisible()) questAcceptanceBox.render(batch, shapeRenderer);
        if (questRewardBox.isVisible()) questRewardBox.render(batch, shapeRenderer);
        if (isQuestUiVisible) questUI.render(batch, shapeRenderer);
        if (isGiftUiVisible) giftUI.render(batch, shapeRenderer);
        if (isFriendshipUiVisible) friendshipUI.render(batch, shapeRenderer);

        notificationManager.render(batch);
    }

    private void handleInput(float deltaTime) {
        // اگر یک پنجره UI باز است، ورودی‌های دیگر را مسدود کن
        if (dialogueBox.isVisible() || isQuestUiVisible || isGiftUiVisible || isFriendshipUiVisible || questAcceptanceBox.isVisible() || questRewardBox.isVisible()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                dialogueBox.hide();
                questAcceptanceBox.hide();
                questRewardBox.hide();
                isQuestUiVisible = false;
                isGiftUiVisible = false;
                isFriendshipUiVisible = false;
            }
            if(isGiftUiVisible) giftUI.handleInput();
            if(questAcceptanceBox.isVisible()) questAcceptanceBox.handleInput();
            if(questRewardBox.isVisible()) questRewardBox.handleInput();
            return;
        }

        // --- حرکت بازیکن ---
        if (Gdx.input.isKeyPressed(Input.Keys.W)) player.position.y += PLAYER_SPEED * deltaTime;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) player.position.y -= PLAYER_SPEED * deltaTime;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) player.position.x -= PLAYER_SPEED * deltaTime;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) player.position.x += PLAYER_SPEED * deltaTime;

        player.position.x = Math.max(20, Math.min(player.position.x, viewport.getWorldWidth() - 20));
        player.position.y = Math.max(20, Math.min(player.position.y, viewport.getWorldHeight() - 20));

        GdxNpc targetNpc = findClosestNpc();

        // --- تعامل ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.E) && targetNpc != null) {
            // اولویت 1: دریافت پاداش
            if (tryClaimReward(targetNpc)) return;
            // اولویت 2: پذیرش ماموریت جدید
            if (tryAcceptQuest(targetNpc)) return;
            // اولویت 3: دیالوگ عادی
            dialogueBox.show(targetNpc.logic.getDialogue(player, currentDate, currentWeather), targetNpc.logic.getName());
            if (!player.hasTalkedToday(targetNpc.logic.getNameEnum())) {
                player.addFriendshipPoints(targetNpc.logic.getNameEnum(), 20, notificationManager);
                player.setTalkedToday(targetNpc.logic.getNameEnum());
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.G)) {
            if (targetNpc != null) {
                isGiftUiVisible = true;
                giftUI.setTargetNpc(targetNpc);
            } else {
                notificationManager.add("You need to be near an NPC to give a gift!");
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.J)) isQuestUiVisible = !isQuestUiVisible;
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) isFriendshipUiVisible = !isFriendshipUiVisible;

        if (Gdx.input.isKeyJustPressed(Input.Keys.N)) {
            currentDate.advanceDay();
            currentWeather = WeatherCondition.values()[new Random().nextInt(WeatherCondition.values().length)];
            player.startNewDay(gameData, notificationManager);
            notificationManager.add("A new day has begun: " + currentDate);
        }
    }

    private GdxNpc findClosestNpc() {
        GdxNpc closestNpc = null;
        float minDistance = INTERACTION_DISTANCE;
        for (GdxNpc npc : npcs.values()) {
            float distance = player.position.dst(npc.position);
            if (distance < minDistance) {
                minDistance = distance;
                closestNpc = npc;
            }
        }
        return closestNpc;
    }

    // نمایش پنجره دریافت پاداش
    private boolean tryClaimReward(GdxNpc targetNpc) {
        Optional<Quest> completableQuest = targetNpc.logic.getQuests().stream()
            .filter(q -> player.isQuestComplete(q))
            .findFirst();

        if (completableQuest.isPresent()) {
            questRewardBox.show(completableQuest.get());
            return true;
        }
        return false;
    }

    // نمایش پنجره پذیرش ماموریت
    private boolean tryAcceptQuest(GdxNpc targetNpc) {
        Optional<Quest> availableQuest = targetNpc.logic.getAvailableQuests(player, currentDate)
            .stream()
            .filter(q -> !player.isQuestActive(q.getId()))
            .findFirst();

        if (availableQuest.isPresent()) {
            questAcceptanceBox.show(availableQuest.get());
            return true;
        }
        return false;
    }


    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        playerTexture.dispose();
        for (Texture texture : npcTextures.values()) {
            texture.dispose();
        }
    }

    //================================================================================
    // کلاس‌های منطق بازی (Static Nested Classes)
    //================================================================================

    enum NpcName { SEBASTIAN, ABIGAIL, HARVEY, LEAH, ROBIN }
    enum Season { SPRING, SUMMER, FALL, WINTER }
    enum WeatherCondition { SUNNY, RAINY, STORMY, SNOWY }
    enum ItemType { RESOURCE, FOOD, MINERAL, CROP, ANIMAL_PRODUCT, FISH, EQUIPMENT, RECIPE, TOOL }

    static class Item {
        final String name; final ItemType type; final int sellPrice;
        public Item(String name, ItemType type, int sellPrice) { this.name = name; this.type = type; this.sellPrice = sellPrice; }
        public String getName() { return name; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; Item item = (Item) o; return name.equals(item.name) && type == item.type; }
        @Override public int hashCode() { return Objects.hash(name, type); }
    }

    static class GameDate {
        int day; Season season; int year;
        public GameDate(int day, Season season, int year) { this.day = day; this.season = season; this.year = year; }
        public void advanceDay() { day++; if (day > 28) { day = 1; switch (season) { case SPRING: season = Season.SUMMER; break; case SUMMER: season = Season.FALL; break; case FALL: season = Season.WINTER; break; case WINTER: season = Season.SPRING; year++; break; } } }
        @Override public String toString() { return season + " " + day + ", Year " + year; }
        public boolean isAfter(GameDate other) { if (other == null) return true; if (this.year > other.year) return true; if (this.year < other.year) return false; if (this.season.ordinal() > other.season.ordinal()) return true; if (this.season.ordinal() < other.season.ordinal()) return false; return this.day >= other.day; }
    }

    static class QuestReward {
        private final List<Item> items; private final int gold; private final int friendshipPoints;
        public QuestReward(List<Item> items, int gold, int friendshipPoints) { this.items = items != null ? new ArrayList<>(items) : new ArrayList<>(); this.gold = gold; this.friendshipPoints = friendshipPoints; }
        public QuestReward(int gold) { this(null, gold, 0); }
        public List<Item> getItems() { return items; } public int getGold() { return gold; } public int getFriendshipPoints() { return friendshipPoints; }
        @Override public String toString() { StringBuilder sb = new StringBuilder("Reward: "); if (gold > 0) sb.append(gold).append("g. "); if (friendshipPoints > 0) sb.append(friendshipPoints).append(" FP. "); if (!items.isEmpty()) { sb.append("Items: "); Map<String, Long> itemCounts = items.stream().collect(Collectors.groupingBy(Item::getName, Collectors.counting())); itemCounts.forEach((name, count) -> sb.append(count).append("x ").append(name).append(", ")); sb.setLength(sb.length() - 2); } return sb.toString(); }
    }

    static class Quest {
        final String id, title, description; final NpcName npcProvider; final Map<Item, Integer> requiredItems; final QuestReward reward; final int requiredFriendshipLevel; final GameDate activationDate; final boolean uniqueGlobalCompletion; private boolean completedByAnyPlayer = false;
        public Quest(String id, String title, String description, NpcName npcProvider, Map<Item, Integer> requiredItems, QuestReward reward, int requiredFriendshipLevel, GameDate activationDate, boolean uniqueGlobalCompletion) { this.id = id; this.title = title; this.description = description; this.npcProvider = npcProvider; this.requiredItems = requiredItems; this.reward = reward; this.requiredFriendshipLevel = requiredFriendshipLevel; this.activationDate = activationDate; this.uniqueGlobalCompletion = uniqueGlobalCompletion; }
        public String getId() { return id; } public String getTitle() { return title; } public NpcName getNpcProvider() { return npcProvider; } public Map<Item, Integer> getRequiredItems() { return requiredItems; } public QuestReward getReward() { return reward; }
        public boolean isAvailable(Player player, GameDate currentDate) { if (player.hasCompletedQuest(this.id)) return false; if (player.getFriendshipLevel(npcProvider) < requiredFriendshipLevel) return false; if (activationDate != null && !currentDate.isAfter(activationDate)) return false; if (uniqueGlobalCompletion && completedByAnyPlayer) return false; return true; }
        public void markAsGloballyCompleted() { if (uniqueGlobalCompletion) this.completedByAnyPlayer = true; }
    }

    static class Player {
        final String name; final Vector2 position; final Map<Item, Integer> inventory; final Map<NpcName, Integer> friendshipPoints; final Set<String> completedQuests; final Set<String> activeQuests; final Map<String, Map<Item, Integer>> questProgress; int gold; final Map<NpcName, Boolean> dailyTalked; final Map<NpcName, Boolean> dailyGifted;
        public Player(String name, Vector2 startPos) { this.name = name; this.position = startPos; this.inventory = new LinkedHashMap<>(); this.friendshipPoints = new HashMap<>(); this.completedQuests = new HashSet<>(); this.activeQuests = new HashSet<>(); this.questProgress = new HashMap<>(); this.gold = 500; this.dailyTalked = new HashMap<>(); this.dailyGifted = new HashMap<>(); for (NpcName npc : NpcName.values()) { friendshipPoints.put(npc, 0); dailyTalked.put(npc, false); dailyGifted.put(npc, false); } }
        public void addItem(Item item, int quantity) { if (item != null && quantity > 0) inventory.put(item, inventory.getOrDefault(item, 0) + quantity); }
        public boolean removeItem(Item item, int quantity) { if (item == null || quantity <= 0 || inventory.getOrDefault(item, 0) < quantity) return false; inventory.put(item, inventory.get(item) - quantity); if (inventory.get(item) == 0) inventory.remove(item); return true; }
        public int getItemCount(Item item) { return inventory.getOrDefault(item, 0); }
        public void addFriendshipPoints(NpcName npc, int points, NotificationManager nm) { int currentPoints = friendshipPoints.getOrDefault(npc, 0); friendshipPoints.put(npc, Math.min(currentPoints + points, 799)); nm.add("Friendship with " + npc + " +" + points + "!"); }
        public int getFriendshipPoints(NpcName npc) { return friendshipPoints.getOrDefault(npc, 0); }
        public int getFriendshipLevel(NpcName npc) { return friendshipPoints.getOrDefault(npc, 0) / 200; }
        public void acceptQuest(String questId) { activeQuests.add(questId); questProgress.put(questId, new HashMap<>()); }
        public int getQuestItemProgress(String questId, Item item) { return questProgress.getOrDefault(questId, new HashMap<>()).getOrDefault(item, 0); }
        public void addQuestItemProgress(String questId, Item item, int quantity) { Map<Item, Integer> progress = questProgress.get(questId); if (progress != null) { progress.put(item, progress.getOrDefault(item, 0) + quantity); } }
        public boolean isQuestComplete(Quest quest) { if (!isQuestActive(quest.getId())) return false; Map<Item, Integer> progress = questProgress.get(quest.getId()); if (progress == null) return false; for (Map.Entry<Item, Integer> required : quest.getRequiredItems().entrySet()) { if (progress.getOrDefault(required.getKey(), 0) < required.getValue()) return false; } return true; }
        public void completeQuest(Quest quest, NotificationManager nm) { activeQuests.remove(quest.getId()); completedQuests.add(quest.getId()); questProgress.remove(quest.getId()); addGold(quest.getReward().getGold(), nm); if (quest.getReward().getFriendshipPoints() > 0) addFriendshipPoints(quest.getNpcProvider(), quest.getReward().getFriendshipPoints(), nm); for (Item item : quest.getReward().getItems()) { addItem(item, 1); nm.add("Received: " + item.getName()); } if (getFriendshipLevel(quest.getNpcProvider()) >= 2) { nm.add("Friendship Lvl 2+ Bonus: Extra " + quest.getReward().getGold() + "g!"); addGold(quest.getReward().getGold(), nm); } }
        public boolean hasCompletedQuest(String questId) { return completedQuests.contains(questId); }
        public boolean isQuestActive(String questId) { return activeQuests.contains(questId); }
        public void addGold(int amount, NotificationManager nm) { if(amount > 0) { this.gold += amount; nm.add("+" + amount + "g"); } }
        public void startNewDay(GameDataRegistry gameData, NotificationManager nm) { for (NpcName npc : NpcName.values()) { dailyTalked.put(npc, false); dailyGifted.put(npc, false); } for (NpcName npcName : NpcName.values()) { if (getFriendshipLevel(npcName) >= 3 && Math.random() < 0.5) { Item giftFromNpc = Npc.getPossibleGiftFrom(npcName, gameData); if (giftFromNpc != null) { addItem(giftFromNpc, 1); nm.add(npcName + " sent you a gift: " + giftFromNpc.getName()); } } } }
        public boolean hasTalkedToday(NpcName npc) { return dailyTalked.getOrDefault(npc, false); }
        public void setTalkedToday(NpcName npc) { dailyTalked.put(npc, true); }
        public boolean hasGiftedToday(NpcName npc) { return dailyGifted.getOrDefault(npc, false); }
        public void setGiftedToday(NpcName npc) { dailyGifted.put(npc, true); }
    }

    static class GdxNpc {
        final Npc logic; final Vector2 position; final Texture texture;
        public GdxNpc(Npc logic, Vector2 position, Texture texture) { this.logic = logic; this.position = position; this.texture = texture; }
        public void renderSprite(SpriteBatch batch) { batch.draw(texture, position.x - 20, position.y - 20, 40, 40); }
        public void renderMarker(ShapeRenderer renderer, Player player, GameDate currentDate) {
            // اولویت 1: ماموریت کامل شده برای دریافت پاداش (سبز)
            if (logic.getQuests().stream().anyMatch(player::isQuestComplete)) {
                renderer.setColor(Color.GREEN);
                renderer.circle(position.x, position.y + 30, 7);
                return;
            }
            // اولویت 2: ماموریت جدید در دسترس (زرد)
            if (logic.getAvailableQuests(player, currentDate).stream().anyMatch(q -> !player.isQuestActive(q.getId()))) {
                renderer.setColor(Color.YELLOW);
                renderer.rect(position.x - 5, position.y + 25, 10, 10);
                return;
            }
            // اولویت 3: دیالوگ روزانه (سفید)
            if (!player.hasTalkedToday(logic.getNameEnum())) {
                renderer.setColor(Color.WHITE);
                renderer.circle(position.x, position.y + 30, 5);
            }
        }
    }

    static abstract class Npc {
        final NpcName name; final List<Item> favoriteItems = new ArrayList<>(); final List<Quest> quests = new ArrayList<>(); final List<Item> potentialGiftsToSend;
        private static final Map<NpcName, Npc> npcRegistry = new HashMap<>();
        public Npc(NpcName name, List<Item> potentialGiftsToSend) { this.name = name; this.potentialGiftsToSend = potentialGiftsToSend != null ? new ArrayList<>(potentialGiftsToSend) : new ArrayList<>(); }
        public NpcName getNameEnum() { return name; } public String getName() { return name.toString(); }
        public List<Quest> getQuests() { return quests; }
        public abstract String getDialogue(Player player, GameDate gameDate, WeatherCondition weather);
        public void receiveGift(Player player, Item item, int quantity, NotificationManager nm) {
            // بررسی اینکه آیا آیتم برای یک ماموریت فعال است یا نه
            Optional<Quest> relevantQuest = quests.stream()
                .filter(q -> player.isQuestActive(q.getId()) && q.getRequiredItems().containsKey(item) && !player.isQuestComplete(q))
                .findFirst();

            if (relevantQuest.isPresent()) {
                Quest quest = relevantQuest.get();
                int needed = quest.getRequiredItems().get(item);
                int alreadyHave = player.getQuestItemProgress(quest.getId(), item);
                int canTurnIn = Math.min(quantity, needed - alreadyHave);

                if (canTurnIn > 0) {
                    player.addQuestItemProgress(quest.getId(), item, canTurnIn);
                    nm.add(String.format("Quest progress for '%s': +%d %s", quest.getTitle(), canTurnIn, item.getName()));
                    int remainingQuantity = quantity - canTurnIn;
                    if (remainingQuantity > 0) {
                        giveFriendshipGift(player, item, remainingQuantity, nm);
                    }
                } else {
                    giveFriendshipGift(player, item, quantity, nm);
                }
            } else {
                giveFriendshipGift(player, item, quantity, nm);
            }
        }
        private void giveFriendshipGift(Player player, Item item, int quantity, NotificationManager nm) {
            if (item.type == ItemType.TOOL) { nm.add(name + ": I can't accept tools, but thanks!"); return; }
            if (!player.hasGiftedToday(this.name)) {
                int points = 50 * quantity;
                if (favoriteItems.contains(item)) { points = 200 * quantity; nm.add(name + " loved your gift!"); }
                else { nm.add(name + " liked your gift."); }
                player.addFriendshipPoints(this.name, points, nm);
                player.setGiftedToday(this.name);
            } else { nm.add(name + ": Another gift? One is enough for today!"); }
        }
        public List<Quest> getAvailableQuests(Player player, GameDate currentDate) { return quests.stream().filter(q -> q.isAvailable(player, currentDate)).collect(Collectors.toList()); }
        protected void addFavoriteItem(Item item) { if (item != null) this.favoriteItems.add(item); }
        protected void addQuest(Quest quest) { if (quest != null) this.quests.add(quest); }
        public static void registerNpc(Npc npc) { npcRegistry.put(npc.getNameEnum(), npc); }
        public static Npc getNpc(NpcName name) { return npcRegistry.get(name); }
        public static Item getPossibleGiftFrom(NpcName npcName, GameDataRegistry gameData) { Npc npc = getNpc(npcName); if (npc != null && !npc.potentialGiftsToSend.isEmpty()) { return npc.potentialGiftsToSend.get(new Random().nextInt(npc.potentialGiftsToSend.size())); } return null; }
        public static Optional<Quest> getQuestById(String questId) {
            for (Npc npc : npcRegistry.values()) {
                Optional<Quest> quest = npc.getQuests().stream().filter(q -> q.getId().equals(questId)).findFirst();
                if (quest.isPresent()) return quest;
            }
            return Optional.empty();
        }
    }

    static class Sebastian extends Npc {
        public Sebastian(GameDataRegistry gameData) { super(NpcName.SEBASTIAN, Collections.singletonList(gameData.getItem("Obsidian"))); addFavoriteItem(gameData.getItem("Wool")); addFavoriteItem(gameData.getItem("Pumpkin Pie")); Map<Item, Integer> q1Req = new HashMap<>(); q1Req.put(gameData.getItem("Iron Ore"), 50); addQuest(new Quest("SEB_Q1", "Iron for Projects", "Seb needs 50 Iron Ore.", NpcName.SEBASTIAN, q1Req, new QuestReward(Arrays.asList(gameData.getItem("Diamond"), gameData.getItem("Diamond")), 0, 0), 0, null, true)); Map<Item, Integer> q2Req = new HashMap<>(); q2Req.put(gameData.getItem("Pumpkin Pie"), 1); addQuest(new Quest("SEB_Q2", "Craving Pie", "Seb wants a Pumpkin Pie.", NpcName.SEBASTIAN, q2Req, new QuestReward(5000), 1, null, true)); Map<Item, Integer> q3Req = new HashMap<>(); q3Req.put(gameData.getItem("Stone"), 150); List<Item> quartzReward = new ArrayList<>(); for(int i=0; i<50; i++) quartzReward.add(gameData.getItem("Quartz")); addQuest(new Quest("SEB_Q3", "Cave Supplies", "Seb needs 150 Stone.", NpcName.SEBASTIAN, q3Req, new QuestReward(quartzReward,0,0), 0, new GameDate(15, Season.SPRING, 1), true)); }
        @Override public String getDialogue(Player p, GameDate d, WeatherCondition w) { return w == WeatherCondition.RAINY ? "Rainy days are the best." : "...Hmph."; }
    }
    static class Abigail extends Npc {
        public Abigail(GameDataRegistry gameData) { super(NpcName.ABIGAIL, Collections.singletonList(gameData.getItem("Amethyst"))); addFavoriteItem(gameData.getItem("Stone")); addFavoriteItem(gameData.getItem("Coffee")); Map<Item, Integer> q1Req = new HashMap<>(); q1Req.put(gameData.getItem("Gold Bar"), 1); addQuest(new Quest("ABI_Q1", "Shiny Gold", "Abigail wants a Gold Bar.", NpcName.ABIGAIL, q1Req, new QuestReward(null, 0, 200), 0, null, true)); }
        @Override public String getDialogue(Player p, GameDate d, WeatherCondition w) { return "Hey. You new in town?"; }
    }
    static class Harvey extends Npc {
        public Harvey(GameDataRegistry gameData) { super(NpcName.HARVEY, Collections.singletonList(gameData.getItem("Coffee"))); addFavoriteItem(gameData.getItem("Pickles")); addFavoriteItem(gameData.getItem("Wine")); Map<Item, Integer> q1Req = new HashMap<>(); q1Req.put(gameData.getItem("Salmon"), 1); addQuest(new Quest("HAR_Q1", "Healthy Diet", "Harvey needs a Salmon for research.", NpcName.HARVEY, q1Req, new QuestReward(null, 0, 200), 1, null, true)); }
        @Override public String getDialogue(Player p, GameDate d, WeatherCondition w) { return "Please take care of your health."; }
    }
    static class Leah extends Npc {
        public Leah(GameDataRegistry gameData) { super(NpcName.LEAH, Collections.singletonList(gameData.getItem("Salad"))); addFavoriteItem(gameData.getItem("Grapes")); addFavoriteItem(gameData.getItem("Wine")); Map<Item, Integer> q1Req = new HashMap<>(); q1Req.put(gameData.getItem("Hardwood"), 10); addQuest(new Quest("LEA_Q1", "Sculpting Material", "Leah needs 10 Hardwood.", NpcName.LEAH, q1Req, new QuestReward(500), 0, null, true)); }
        @Override public String getDialogue(Player p, GameDate d, WeatherCondition w) { return "Oh, hi. Just admiring the forest."; }
    }
    static class Robin extends Npc {
        public Robin(GameDataRegistry gameData) { super(NpcName.ROBIN, Collections.singletonList(gameData.getItem("Spaghetti"))); addFavoriteItem(gameData.getItem("Wood")); addFavoriteItem(gameData.getItem("Iron Bar")); Map<Item, Integer> q1Req = new HashMap<>(); q1Req.put(gameData.getItem("Wood"), 80); addQuest(new Quest("ROB_Q1", "Wood Delivery", "Robin needs 80 Wood.", NpcName.ROBIN, q1Req, new QuestReward(1000), 0, null, true)); }
        @Override public String getDialogue(Player p, GameDate d, WeatherCondition w) { return "Need anything built?"; }
    }

    static class GameDataRegistry {
        private final Map<String, Item> items = new HashMap<>();
        public GameDataRegistry() { addItem("Wood", ItemType.RESOURCE, 2); addItem("Stone", ItemType.RESOURCE, 2); addItem("Hardwood", ItemType.RESOURCE, 15); addItem("Iron Ore", ItemType.RESOURCE, 10); addItem("Gold Bar", ItemType.RESOURCE, 250); addItem("Iron Bar", ItemType.RESOURCE, 120); addItem("Diamond", ItemType.MINERAL, 750); addItem("Quartz", ItemType.MINERAL, 25); addItem("Amethyst", ItemType.MINERAL, 100); addItem("Obsidian", ItemType.MINERAL, 200); addItem("Pumpkin", ItemType.CROP, 320); addItem("Grapes", ItemType.CROP, 80); addItem("Wool", ItemType.ANIMAL_PRODUCT, 340); addItem("Pumpkin Pie", ItemType.FOOD, 385); addItem("Pizza", ItemType.FOOD, 600); addItem("Coffee", ItemType.FOOD, 150); addItem("Pickles", ItemType.FOOD, 100); addItem("Wine", ItemType.FOOD, 0); addItem("Salad", ItemType.FOOD, 110); addItem("Spaghetti", ItemType.FOOD, 120); addItem("Salmon", ItemType.FISH, 75); }
        private void addItem(String name, ItemType type, int price) { items.put(name.toLowerCase(), new Item(name, type, price)); }
        public Item getItem(String name) { return items.get(name.toLowerCase()); }
    }

    //================================================================================
    // کلاس‌های UI گرافیکی (Inner classes)
    //================================================================================

    class DialogueBox {
        private String currentText = ""; private String npcName = ""; private boolean visible = false; private final Rectangle box = new Rectangle(50, 50, 700, 100); private final GlyphLayout layout = new GlyphLayout();
        public void show(String text, String npcName) { this.currentText = text; this.npcName = npcName; this.visible = true; }
        public void hide() { this.visible = false; }
        public boolean isVisible() { return visible; }
        public void render(SpriteBatch batch, ShapeRenderer renderer) { if (!visible) return; renderer.begin(ShapeRenderer.ShapeType.Filled); renderer.setColor(0, 0, 0, 0.7f); renderer.rect(box.x, box.y, box.width, box.height); renderer.end(); renderer.begin(ShapeRenderer.ShapeType.Line); renderer.setColor(Color.WHITE); renderer.rect(box.x, box.y, box.width, box.height); renderer.end(); batch.begin(); font.setColor(Color.YELLOW); font.draw(batch, npcName + ":", box.x + 10, box.y + box.height - 15); font.setColor(Color.WHITE); layout.setText(font, currentText, Color.WHITE, box.width - 20, 1, true); font.draw(batch, layout, box.x + 10, box.y + box.height - 40); batch.end(); }
    }

    class NotificationManager {
        private final Queue<Notification> notifications = new LinkedList<>();
        public void add(String message) { notifications.add(new Notification(message)); }
        public void update(float delta) { if (!notifications.isEmpty()) { notifications.peek().timer -= delta; if (notifications.peek().timer <= 0) notifications.poll(); } }
        public void render(SpriteBatch batch) { batch.begin(); if (!notifications.isEmpty()) { Notification current = notifications.peek(); font.setColor(1, 1, 0, current.timer / 2f); font.draw(batch, current.message, 20, 580); font.setColor(Color.WHITE); } batch.end(); }
        private class Notification { String message; float timer = 2.0f; Notification(String message) { this.message = message; } }
    }

    class QuestUI {
        private final Rectangle box = new Rectangle(100, 100, 600, 400);
        public void render(SpriteBatch batch, ShapeRenderer renderer) {
            renderer.begin(ShapeRenderer.ShapeType.Filled); renderer.setColor(0.1f, 0.1f, 0.2f, 0.8f); renderer.rect(box.x, box.y, box.width, box.height); renderer.end();
            renderer.begin(ShapeRenderer.ShapeType.Line); renderer.setColor(Color.CYAN); renderer.rect(box.x, box.y, box.width, box.height); renderer.end();
            batch.begin();
            font.draw(batch, "Quest Journal (Press ESC to close)", box.x + 10, box.y + box.height - 20);
            float yPos = box.y + box.height - 60;
            font.setColor(Color.YELLOW); font.draw(batch, "--- Active Quests ---", box.x + 10, yPos); yPos -= 30;
            boolean hasActiveQuests = false;
            for (String questId : player.activeQuests) {
                Optional<Quest> questOpt = Npc.getQuestById(questId);
                if (questOpt.isPresent()) {
                    hasActiveQuests = true;
                    Quest quest = questOpt.get();
                    font.setColor(Color.WHITE);
                    font.draw(batch, "[" + quest.getId() + "] " + quest.getTitle() + " (" + quest.getNpcProvider() + ")", box.x + 20, yPos); yPos -= 20;
                    font.setColor(Color.LIGHT_GRAY);
                    for (Map.Entry<Item, Integer> entry : quest.getRequiredItems().entrySet()) {
                        int have = player.getQuestItemProgress(quest.getId(), entry.getKey());
                        int need = entry.getValue();
                        String progress = String.format("%s: %d / %d", entry.getKey().getName(), have, need);
                        font.draw(batch, progress, box.x + 30, yPos); yPos -= 20;
                    }
                    yPos -= 5;
                }
            }
            if (!hasActiveQuests) { font.setColor(Color.GRAY); font.draw(batch, "No active quests.", box.x + 20, yPos); yPos -= 30; }
            font.setColor(Color.YELLOW); font.draw(batch, "--- Completed Quests ---", box.x + 10, yPos); yPos -= 30;
            font.setColor(Color.GREEN);
            if (player.completedQuests.isEmpty()) { font.setColor(Color.GRAY); font.draw(batch, "No quests completed yet.", box.x + 20, yPos); }
            else { for (String questId : player.completedQuests) { font.draw(batch, "- " + questId, box.x + 20, yPos); yPos -= 20; if (yPos < box.y + 20) break; } }
            batch.end();
        }
    }

    class GiftUI {
        private final Rectangle box = new Rectangle(150, 150, 500, 300); private GdxNpc targetNpc; private int selectedIndex = 0; private int giftQuantity = 1;
        public void setTargetNpc(GdxNpc npc) { this.targetNpc = npc; this.selectedIndex = 0; this.giftQuantity = 1; }
        public void handleInput() {
            if (player.inventory.isEmpty()) return;
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) { selectedIndex = (selectedIndex - 1 + player.inventory.size()) % player.inventory.size(); giftQuantity = 1; }
            if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) { selectedIndex = (selectedIndex + 1) % player.inventory.size(); giftQuantity = 1; }
            Item selectedItem = new ArrayList<>(player.inventory.keySet()).get(selectedIndex);
            int maxQuantity = player.getItemCount(selectedItem);
            if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) giftQuantity = Math.min(maxQuantity, giftQuantity + 1);
            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) giftQuantity = Math.max(1, giftQuantity - 1);
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) { giveGift(); isGiftUiVisible = false; }
        }
        private void giveGift() { if (targetNpc == null || player.inventory.isEmpty()) return; Item selectedItem = new ArrayList<>(player.inventory.keySet()).get(selectedIndex); if (player.removeItem(selectedItem, giftQuantity)) targetNpc.logic.receiveGift(player, selectedItem, giftQuantity, notificationManager); }
        public void render(SpriteBatch batch, ShapeRenderer renderer) {
            if (targetNpc == null) return;
            renderer.begin(ShapeRenderer.ShapeType.Filled); renderer.setColor(0.2f, 0.1f, 0.1f, 0.8f); renderer.rect(box.x, box.y, box.width, box.height); renderer.end();
            renderer.begin(ShapeRenderer.ShapeType.Line); renderer.setColor(Color.ORANGE); renderer.rect(box.x, box.y, box.width, box.height); renderer.end();
            batch.begin();
            font.draw(batch, "Give a gift to " + targetNpc.logic.getName() + " (L/R to change amount)", box.x + 10, box.y + box.height - 20);
            if (player.inventory.isEmpty()) { font.draw(batch, "Your inventory is empty.", box.x + 20, box.y + box.height - 60); }
            else {
                float yPos = box.y + box.height - 60; int currentIndex = 0;
                for (Map.Entry<Item, Integer> entry : player.inventory.entrySet()) {
                    String text = entry.getKey().getName() + " (" + entry.getValue() + ")";
                    if (selectedIndex == currentIndex) { font.setColor(Color.YELLOW); text = "> " + text + "  [" + giftQuantity + "]"; }
                    else { font.setColor(Color.WHITE); }
                    font.draw(batch, text, box.x + 20, yPos);
                    yPos -= 25; currentIndex++; if (yPos < box.y + 20) break;
                }
            }
            batch.end();
        }
    }

    class FriendshipUI {
        private final Rectangle box = new Rectangle(150, 150, 500, 300);
        public void render(SpriteBatch batch, ShapeRenderer renderer) { renderer.begin(ShapeRenderer.ShapeType.Filled); renderer.setColor(0.1f, 0.2f, 0.2f, 0.8f); renderer.rect(box.x, box.y, box.width, box.height); renderer.end(); renderer.begin(ShapeRenderer.ShapeType.Line); renderer.setColor(Color.GREEN); renderer.rect(box.x, box.y, box.width, box.height); renderer.end(); batch.begin(); font.draw(batch, "Friendship Status (Press ESC to close)", box.x + 10, box.y + box.height - 20); float yPos = box.y + box.height - 60; for (NpcName npcName : NpcName.values()) { int points = player.getFriendshipPoints(npcName); int level = player.getFriendshipLevel(npcName); String line = String.format("%-15s | Level: %d | Points: %d / 799", npcName, level, points); font.draw(batch, line, box.x + 20, yPos); yPos -= 25; } batch.end(); }
    }

    class QuestAcceptanceBox {
        private Quest currentQuest; private boolean visible = false; private final Rectangle box = new Rectangle(100, 150, 600, 200);
        public void show(Quest quest) { this.currentQuest = quest; this.visible = true; }
        public void hide() { this.visible = false; }
        public boolean isVisible() { return visible; }
        public void handleInput() {
            if (Gdx.input.isKeyJustPressed(Input.Keys.Y)) { player.acceptQuest(currentQuest.getId()); notificationManager.add("Quest Accepted: " + currentQuest.getTitle()); hide(); }
            if (Gdx.input.isKeyJustPressed(Input.Keys.N)) { hide(); }
        }
        public void render(SpriteBatch batch, ShapeRenderer renderer) {
            if (!visible) return;
            renderer.begin(ShapeRenderer.ShapeType.Filled); renderer.setColor(0, 0, 0, 0.8f); renderer.rect(box.x, box.y, box.width, box.height); renderer.end();
            renderer.begin(ShapeRenderer.ShapeType.Line); renderer.setColor(Color.YELLOW); renderer.rect(box.x, box.y, box.width, box.height); renderer.end();
            batch.begin();
            font.draw(batch, "New Quest: " + currentQuest.getTitle(), box.x + 10, box.y + box.height - 20);
            font.draw(batch, currentQuest.getReward().toString(), box.x + 10, box.y + box.height - 50);
            font.draw(batch, "Accept Quest? (Y/N)", box.x + 10, box.y + 40);
            batch.end();
        }
    }

    class QuestRewardBox {
        private Quest currentQuest; private boolean visible = false; private final Rectangle box = new Rectangle(100, 150, 600, 200);
        public void show(Quest quest) { this.currentQuest = quest; this.visible = true; }
        public void hide() { this.visible = false; }
        public boolean isVisible() { return visible; }
        public void handleInput() {
            if (Gdx.input.isKeyJustPressed(Input.Keys.Y)) {
                player.completeQuest(currentQuest, notificationManager);
                if (currentQuest.uniqueGlobalCompletion) currentQuest.markAsGloballyCompleted();
                hide();
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.N)) { hide(); }
        }
        public void render(SpriteBatch batch, ShapeRenderer renderer) {
            if (!visible) return;
            renderer.begin(ShapeRenderer.ShapeType.Filled); renderer.setColor(0, 0, 0, 0.8f); renderer.rect(box.x, box.y, box.width, box.height); renderer.end();
            renderer.begin(ShapeRenderer.ShapeType.Line); renderer.setColor(Color.GREEN); renderer.rect(box.x, box.y, box.width, box.height); renderer.end();
            batch.begin();
            font.draw(batch, "Quest Complete: " + currentQuest.getTitle(), box.x + 10, box.y + box.height - 20);
            font.draw(batch, currentQuest.getReward().toString(), box.x + 10, box.y + box.height - 50);
            font.draw(batch, "Claim Reward? (Y/N)", box.x + 10, box.y + 40);
            batch.end();
        }
    }
}
