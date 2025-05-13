import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;

// --- Enum تعریف انواع کاشی‌ها و عناصر نقشه ---
enum TileType {
    GRASS("چمن"),
    WATER("آب"),
    SOIL("خاک"), // زمین شخم زده شده
    STONE_PATH("مسیر سنگی"),
    WOOD_PATH("مسیر چوبی"),
    FENCE("نرده"),
    HOUSE_WALL("دیوار خانه"),
    HOUSE_FLOOR("کف خانه"),
    GREENHOUSE_AREA("محدوده گلخانه"),
    CABIN_AREA("محدوده کلبه"),
    QUARRY_AREA("محدوده معدن سنگ"),
    NPC_VILLAGE_GROUND("زمین دهکده NPC"),
    NPC_BUILDING("ساختمان NPC"),
    EMPTY("خالی"); // برای مرزها یا مناطق غیر قابل دسترس

    private final String persianName;

    TileType(String persianName) {
        this.persianName = persianName;
    }

    public String getPersianName() {
        return persianName;
    }

    @Override
    public String toString() {
        return persianName;
    }
}

enum MapElementType {
    TREE("درخت"),
    ROCK("سنگ"),
    FORAGING_ITEM("آیتم جمع‌آوری شدنی"),
    CROP("محصول کشاورزی"),
    BUILDING_COMPONENT("بخش ساختمان"); // برای کلبه، گلخانه

    private final String persianName;

    MapElementType(String persianName) {
        this.persianName = persianName;
    }

    public String getPersianName() {
        return persianName;
    }

    @Override
    public String toString() {
        return persianName;
    }
}

// --- کلاس‌های پایه برای عناصر نقشه ---
abstract class MapElement {
    protected String name;
    protected MapElementType elementType;
    protected char charRepresentation; // کاراکتر برای نمایش در نقشه متنی

    public MapElement(String name, MapElementType elementType, char charRepresentation) {
        this.name = name;
        this.elementType = elementType;
        this.charRepresentation = charRepresentation;
    }

    public String getName() {
        return name;
    }

    public MapElementType getElementType() {
        return elementType;
    }

    public char getCharRepresentation() {
        return charRepresentation;
    }

    @Override
    public String toString() {
        return name;
    }
}

class Tree extends MapElement {
    public Tree(String treeTypeName) {
        super(treeTypeName, MapElementType.TREE, 'T');
        // می‌توان ویژگی‌های بیشتری مانند نوع درخت، مرحله رشد و غیره اضافه کرد
    }
    public Tree() {
        this("درخت بلوط");
    }
}

class Rock extends MapElement {
    public Rock(String rockTypeName) {
        super(rockTypeName, MapElementType.ROCK, 'R');
        // می‌توان ویژگی‌های بیشتری مانند نوع سنگ، سختی و غیره اضافه کرد
    }
    public Rock() {
        this("سنگ معمولی");
    }
}

class ForagingItem extends MapElement {
    public ForagingItem(String itemName) {
        super(itemName, MapElementType.FORAGING_ITEM, 'F');
        // می‌توان ویژگی‌های بیشتری مانند فصل، ارزش و غیره اضافه کرد
    }
    public ForagingItem() {
        this("قارچ وحشی");
    }
}

class BuildingComponent extends MapElement {
    public BuildingComponent(String componentName) {
        super(componentName, MapElementType.BUILDING_COMPONENT, 'B');
    }
}

// --- کلاس کاشی نقشه ---
class Tile {
    private int x;
    private int y;
    private TileType baseType;
    private MapElement mapElement; // عنصری که روی این کاشی قرار دارد (مثلا درخت، سنگ)
    private boolean isFarmable; // آیا می‌توان روی این کاشی کشاورزی کرد؟
    private boolean isWalkable; // آیا بازیکن می‌تواند روی این کاشی راه برود؟

    public Tile(int x, int y, TileType baseType) {
        this.x = x;
        this.y = y;
        this.baseType = baseType;
        this.mapElement = null; // به طور پیش‌فرض هیچ عنصری روی کاشی نیست
        this.isFarmable = (baseType == TileType.GRASS || baseType == TileType.SOIL);
        this.isWalkable = (baseType != TileType.WATER && baseType != TileType.HOUSE_WALL && baseType != TileType.FENCE && baseType != TileType.EMPTY && baseType != TileType.NPC_BUILDING );
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public TileType getBaseType() {
        return baseType;
    }

    public void setBaseType(TileType baseType) {
        this.baseType = baseType;
        this.isFarmable = (baseType == TileType.GRASS || baseType == TileType.SOIL);
        this.isWalkable = (baseType != TileType.WATER && baseType != TileType.HOUSE_WALL && baseType != TileType.FENCE && baseType != TileType.EMPTY && baseType != TileType.NPC_BUILDING);
    }

    public MapElement getMapElement() {
        return mapElement;
    }

    public void setMapElement(MapElement mapElement) {
        this.mapElement = mapElement;
        if (mapElement != null) { // اگر عنصری روی کاشی قرار گرفت، دیگر قابل راه رفتن نیست (مگر اینکه عنصر خاصی باشد)
            this.isWalkable = false; // این منطق ممکن است نیاز به بهبود داشته باشد
        }
    }

    public void removeMapElement() {
        this.mapElement = null;
        // پس از حذف عنصر، قابلیت راه رفتن به حالت اولیه کاشی برمی‌گردد
        this.isWalkable = (baseType != TileType.WATER && baseType != TileType.HOUSE_WALL && baseType != TileType.FENCE && baseType != TileType.EMPTY && baseType != TileType.NPC_BUILDING);
    }

    public boolean isFarmable() {
        return isFarmable;
    }

    public boolean isWalkable() {
        // اگر عنصری روی کاشی باشد و آن عنصر قابل عبور نباشد، کاشی قابل راه رفتن نیست
        if (mapElement != null && (mapElement instanceof Tree || mapElement instanceof Rock || mapElement instanceof BuildingComponent)) {
            return false;
        }
        return isWalkable;
    }

    public void setWalkable(boolean walkable) {
        isWalkable = walkable;
    }


    public char getRepresentation() {
        if (mapElement != null) {
            return mapElement.getCharRepresentation();
        }
        switch (baseType) {
            case GRASS: return '.';
            case WATER: return '~';
            case SOIL: return '_';
            case STONE_PATH: return ':';
            case WOOD_PATH: return '=';
            case FENCE: return '#';
            case HOUSE_WALL: return 'H';
            case HOUSE_FLOOR: return 'h';
            case GREENHOUSE_AREA: return 'G';
            case CABIN_AREA: return 'C';
            case QUARRY_AREA: return 'Q';
            case NPC_VILLAGE_GROUND: return 'v';
            case NPC_BUILDING: return 'N';
            case EMPTY: return ' ';
            default: return '?';
        }
    }
}

// --- کلاس بازیکن ---
class Player {
    private String name;
    private int x;
    private int y;
    private int energy;
    private Farm currentFarm; // مزرعه فعلی بازیکن

    public Player(String name, int startX, int startY, Farm farm) {
        this.name = name;
        this.x = startX;
        this.y = startY;
        this.energy = 200; // انرژی اولیه
        this.currentFarm = farm;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    public String getName() {
        return name;
    }

    public int getEnergy() {
        return energy;
    }

    public void setCurrentFarm(Farm farm) {
        this.currentFarm = farm;
    }

    public Farm getCurrentFarm() {
        return currentFarm;
    }

    public void moveTo(int newX, int newY) {
        this.x = newX;
        this.y = newY;
    }

    public void decreaseEnergy(int amount) {
        this.energy -= amount;
        if (this.energy < 0) {
            this.energy = 0;
            System.out.println(name + " غش کرد!");
            // منطق غش کردن و رفتن به روز بعد
        }
    }

    public void restoreEnergy() {
        this.energy = 200; // یا مقدار ماکسیمم انرژی
    }

    @Override
    public String toString() {
        return "بازیکن: " + name + " در (" + x + "," + y + ") با انرژی " + energy;
    }
}


// --- کلاس مزرعه ---
class Farm {
    private String farmName;
    private Tile[][] tiles; // آرایه دو بعدی از کاشی‌ها
    private int width;
    private int height;
    private Player owner; // صاحب مزرعه

    // عناصر ثابت مزرعه
    private static final int LAKE_X = 5;
    private static final int LAKE_Y = 15;
    private static final int LAKE_WIDTH = 5;
    private static final int LAKE_HEIGHT = 3;

    private static final int GREENHOUSE_X = 20;
    private static final int GREENHOUSE_Y = 5;
    private static final int GREENHOUSE_WIDTH = 6; // بدون احتساب دیوار
    private static final int GREENHOUSE_HEIGHT = 5; // بدون احتساب دیوار

    public static final int CABIN_X = 2;
    public static final int CABIN_Y = 2;
    public static final int CABIN_WIDTH = 4;
    public static final int CABIN_HEIGHT = 4;

    private static final int QUARRY_X = 25;
    private static final int QUARRY_Y = 20;
    private static final int QUARRY_WIDTH = 4;
    private static final int QUARRY_HEIGHT = 4;


    public Farm(String farmName, int width, int height, Player owner, int farmType) {
        this.farmName = farmName;
        this.width = width;
        this.height = height;
        this.owner = owner;
        this.tiles = new Tile[height][width];
        initializeFarm(farmType);
        placeRandomElements();
    }

    private void initializeFarm(int farmType) {
        // مقداردهی اولیه تمام کاشی‌ها به چمن
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                tiles[y][x] = new Tile(x, y, TileType.GRASS);
            }
        }

        // قرار دادن عناصر ثابت بر اساس نوع مزرعه
        // مثال ساده: همه مزرعه‌ها یک دریاچه، گلخانه، کلبه و معدن سنگ دارند
        // در پیاده‌سازی واقعی، این بخش بر اساس farmType متفاوت خواهد بود

        // دریاچه
        for (int r = LAKE_Y; r < LAKE_Y + LAKE_HEIGHT; r++) {
            for (int c = LAKE_X; c < LAKE_X + LAKE_WIDTH; c++) {
                if (isValidCoordinate(c, r)) {
                    tiles[r][c].setBaseType(TileType.WATER);
                }
            }
        }

        // گلخانه (محدوده)
        for (int r = GREENHOUSE_Y; r < GREENHOUSE_Y + GREENHOUSE_HEIGHT; r++) {
            for (int c = GREENHOUSE_X; c < GREENHOUSE_X + GREENHOUSE_WIDTH; c++) {
                if (isValidCoordinate(c, r)) {
                    tiles[r][c].setBaseType(TileType.GREENHOUSE_AREA);
                    // دیوارهای گلخانه را می‌توان به صورت جداگانه اضافه کرد
                }
            }
        }
        // اضافه کردن دیوارهای گلخانه
        addBuildingWalls(GREENHOUSE_X -1, GREENHOUSE_Y -1, GREENHOUSE_WIDTH + 2, GREENHOUSE_HEIGHT + 2, TileType.HOUSE_WALL);


        // کلبه (محدوده)
        for (int r = CABIN_Y; r < CABIN_Y + CABIN_HEIGHT; r++) {
            for (int c = CABIN_X; c < CABIN_X + CABIN_WIDTH; c++) {
                if (isValidCoordinate(c, r)) {
                    tiles[r][c].setBaseType(TileType.HOUSE_FLOOR); // کف کلبه
                }
            }
        }
        // اضافه کردن دیوارهای کلبه
        addBuildingWalls(CABIN_X -1, CABIN_Y -1, CABIN_WIDTH + 2, CABIN_HEIGHT + 2, TileType.HOUSE_WALL);
        // ورودی کلبه
        int doorX = CABIN_X + CABIN_WIDTH / 2;
        int doorY = CABIN_Y + CABIN_HEIGHT;  // دقیقاً همان یال پایینی دیوار

        if (isValidCoordinate(doorX, doorY)) {
            tiles[doorY][doorX].setBaseType(TileType.HOUSE_FLOOR);
            tiles[doorY][doorX].setWalkable(true);
        }


        // معدن سنگ (محدوده)
        for (int r = QUARRY_Y; r < QUARRY_Y + QUARRY_HEIGHT; r++) {
            for (int c = QUARRY_X; c < QUARRY_X + QUARRY_WIDTH; c++) {
                if (isValidCoordinate(c, r)) {
                    tiles[r][c].setBaseType(TileType.QUARRY_AREA);
                }
            }
        }
        // مرزهای نقشه
        for (int y = 0; y < height; y++) {
            if (isValidCoordinate(0, y)) tiles[y][0].setBaseType(TileType.EMPTY);
            if (isValidCoordinate(width - 1, y)) tiles[y][width - 1].setBaseType(TileType.EMPTY);
        }
        for (int x = 0; x < width; x++) {
            if (isValidCoordinate(x, 0)) tiles[0][x].setBaseType(TileType.EMPTY);
            if (isValidCoordinate(x, height - 1)) tiles[height - 1][x].setBaseType(TileType.EMPTY);
        }
    }

    private void addBuildingWalls(int startX, int startY, int buildingWidth, int buildingHeight, TileType wallType) {
        for (int r = startY; r < startY + buildingHeight; r++) {
            for (int c = startX; c < startX + buildingWidth; c++) {
                if (isValidCoordinate(c, r)) {
                    // فقط دیوارها را در لبه‌ها قرار بده
                    if (r == startY || r == startY + buildingHeight - 1 || c == startX || c == startX + buildingWidth - 1) {
                        tiles[r][c].setBaseType(wallType);
                    }
                }
            }
        }
    }


    private void placeRandomElements() {
        Random random = new Random();
        int numberOfTrees = 10 + random.nextInt(11); // 10 تا 20 درخت
        int numberOfRocks = 15 + random.nextInt(11); // 15 تا 25 سنگ
        int numberOfForagingItems = 5 + random.nextInt(6); // 5 تا 10 آیتم جمع‌آوری

        placeRandomElement(MapElementType.TREE, numberOfTrees);
        placeRandomElement(MapElementType.ROCK, numberOfRocks);
        placeRandomElement(MapElementType.FORAGING_ITEM, numberOfForagingItems);
    }

    private void placeRandomElement(MapElementType type, int count) {
        Random random = new Random();
        for (int i = 0; i < count; ) {
            int randX = random.nextInt(width);
            int randY = random.nextInt(height);

            if (isValidCoordinate(randX, randY) &&
                    tiles[randY][randX].getBaseType() == TileType.GRASS && // فقط روی چمن
                    tiles[randY][randX].getMapElement() == null) { // و اگر خالی باشد

                switch (type) {
                    case TREE:
                        tiles[randY][randX].setMapElement(new Tree());
                        break;
                    case ROCK:
                        tiles[randY][randX].setMapElement(new Rock());
                        break;
                    case FORAGING_ITEM:
                        tiles[randY][randX].setMapElement(new ForagingItem());
                        break;
                    default:
                        break;
                }
                i++;
            }
        }
    }


    public Tile getTile(int x, int y) {
        if (isValidCoordinate(x, y)) {
            return tiles[y][x];
        }
        return null; // یا یک کاشی "خارج از محدوده" برگردانید
    }

    public boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Player getOwner() {
        return owner;
    }

    public void displayFarm(Player playerOnMap) {
        System.out.println("مزرعه: " + farmName + " (متعلق به: " + (owner != null ? owner.getName() : "بی‌صاحب") + ")");
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (playerOnMap != null && playerOnMap.getX() == x && playerOnMap.getY() == y) {
                    System.out.print("P "); // نمایش بازیکن
                } else {
                    System.out.print(tiles[y][x].getRepresentation() + " ");
                }
            }
            System.out.println();
        }
    }
}

// --- کلاس نقشه کلی بازی ---
class GameMap {
    private List<Farm> farms; // لیست مزارع بازیکنان
    private Tile[][] npcVillage; // دهکده NPCها
    private int npcVillageWidth = 20; // مثال
    private int npcVillageHeight = 20; // مثال
    private Player activePlayer; // بازیکنی که در حال حاضر نوبت اوست

    // ابعاد کلی نقشه بزرگتر (مثال)
    // فرض می‌کنیم هر مزرعه 30x30 و دهکده 20x20 است
    // و مزارع در گوشه‌ها و دهکده در وسط قرار دارند
    private int totalWidth;
    private int totalHeight;
    private Tile[][] overallMap; // نقشه کلی که شامل مزارع و دهکده است

    public GameMap(List<Player> players, List<Integer> farmTypes) {
        if (players.size() > 4) {
            throw new IllegalArgumentException("حداکثر 4 بازیکن پشتیبانی می‌شود.");
        }
        if (players.size() != farmTypes.size()) {
            throw new IllegalArgumentException("تعداد بازیکنان و انواع مزرعه باید برابر باشد.");
        }

        this.farms = new ArrayList<>();
        int farmWidth = 30; // عرض پیش‌فرض مزرعه
        int farmHeight = 30; // ارتفاع پیش‌فرض مزرعه

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            Farm farm = new Farm("مزرعه " + p.getName(), farmWidth, farmHeight, p, farmTypes.get(i));
            this.farms.add(farm);
            p.setCurrentFarm(farm); // اختصاص مزرعه به بازیکن
            // موقعیت اولیه بازیکن در کلبه مزرعه خودش
            p.moveTo(Farm.CABIN_X + Farm.CABIN_WIDTH / 2, Farm.CABIN_Y + Farm.CABIN_HEIGHT / 2);
        }

        initializeNpcVillage();
        composeOverallMap(farmWidth, farmHeight);

        if (!players.isEmpty()) {
            this.activePlayer = players.get(0); // بازیکن اول شروع می‌کند
        }
    }

    private void initializeNpcVillage() {
        npcVillage = new Tile[npcVillageHeight][npcVillageWidth];
        for (int y = 0; y < npcVillageHeight; y++) {
            for (int x = 0; x < npcVillageWidth; x++) {
                npcVillage[y][x] = new Tile(x, y, TileType.NPC_VILLAGE_GROUND);
                // می‌توان ساختمان‌های NPC را در اینجا اضافه کرد
                if (x > 5 && x < 10 && y > 5 && y < 10) { // مثال: یک ساختمان NPC
                    npcVillage[y][x].setBaseType(TileType.NPC_BUILDING);
                }
            }
        }
    }

    private void composeOverallMap(int farmW, int farmH) {
        // محاسبه ابعاد کلی نقشه
        // فرض: دو ردیف و دو ستون از مزارع، با دهکده NPC در وسط
        // این بخش نیاز به طراحی دقیق‌تری برای چیدمان دارد
        // مثال ساده: همه چیز در یک ردیف قرار می‌گیرد
        totalWidth = farms.size() * farmW + npcVillageWidth;
        totalHeight = Math.max(farmH, npcVillageHeight);
        overallMap = new Tile[totalHeight][totalWidth];

        int currentXOffset = 0;
        // چیدن مزارع
        for (Farm farm : farms) {
            for (int y = 0; y < farm.getHeight(); y++) {
                for (int x = 0; x < farm.getWidth(); x++) {
                    if (farm.getTile(x,y) != null) {
                        overallMap[y][currentXOffset + x] = farm.getTile(x,y);
                    } else {
                        overallMap[y][currentXOffset + x] = new Tile(currentXOffset + x, y, TileType.EMPTY);
                    }
                }
            }
            currentXOffset += farm.getWidth();
        }

        // چیدن دهکده NPC
        for (int y = 0; y < npcVillageHeight; y++) {
            for (int x = 0; x < npcVillageWidth; x++) {
                if (y < totalHeight && currentXOffset + x < totalWidth) { // بررسی مرزها
                    overallMap[y][currentXOffset + x] = npcVillage[y][x];
                }
            }
        }
        // پر کردن فضای خالی احتمالی
        for (int r = 0; r < totalHeight; r++) {
            for (int c = 0; c < totalWidth; c++) {
                if (overallMap[r][c] == null) {
                    overallMap[r][c] = new Tile(c, r, TileType.EMPTY);
                }
            }
        }
    }


    public Player getActivePlayer() {
        return activePlayer;
    }

    public void setActivePlayer(Player player) {
        this.activePlayer = player;
    }

    public Farm getFarmByOwner(Player player) {
        for (Farm farm : farms) {
            if (farm.getOwner() == player) {
                return farm;
            }
        }
        return null;
    }

    public Tile getTileAtOverallMap(int x, int y) {
        if (x >= 0 && x < totalWidth && y >= 0 && y < totalHeight) {
            return overallMap[y][x];
        }
        return new Tile(x,y, TileType.EMPTY); // کاشی خالی برای خارج از محدوده
    }


    // --- منطق حرکت بازیکن ---
    public boolean canMoveTo(Player player, int targetX, int targetY) {
        // بررسی اینکه آیا بازیکن می‌تواند به مختصات مورد نظر برود
        // این تابع باید با توجه به نقشه کلی (overallMap) یا مزرعه فعلی بازیکن عمل کند
        // در این مثال ساده، فرض می‌کنیم حرکت فقط در مزرعه فعلی بازیکن است

        Farm playerFarm = player.getCurrentFarm();
        if (playerFarm == null) return false; // اگر بازیکن مزرعه‌ای ندارد

        if (!playerFarm.isValidCoordinate(targetX, targetY)) {
            System.out.println("خطا: مختصات خارج از محدوده مزرعه است.");
            return false;
        }

        Tile targetTile = playerFarm.getTile(targetX, targetY);
        if (targetTile == null || !targetTile.isWalkable()) {
            System.out.println("خطا: نمی‌توانید به این کاشی بروید (" + (targetTile != null ? targetTile.getBaseType() : "نامشخص") + ").");
            return false;
        }
        return true;
    }

    public PathInfo findShortestPath(Player player, int startX, int startY, int targetX, int targetY) {
        Farm farm = player.getCurrentFarm();
        if (farm == null || !farm.isValidCoordinate(startX, startY) || !farm.isValidCoordinate(targetX, targetY)) {
            return new PathInfo(Integer.MAX_VALUE, 0, null); // مسیر نامعتبر
        }

        int[][] distances = new int[farm.getHeight()][farm.getWidth()];
        int[][] turns = new int[farm.getHeight()][farm.getWidth()]; // برای محاسبه پیچ‌ها
        Point[][] predecessors = new Point[farm.getHeight()][farm.getWidth()]; // برای بازسازی مسیر

        for (int i = 0; i < farm.getHeight(); i++) {
            for (int j = 0; j < farm.getWidth(); j++) {
                distances[i][j] = Integer.MAX_VALUE;
                turns[i][j] = Integer.MAX_VALUE;
            }
        }

        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(startX, startY, 0, 0, null)); // x, y, distance, numTurns, previousDirection
        distances[startY][startX] = 0;
        turns[startY][startX] = 0;

        int[] dx = {0, 0, 1, -1, 1, 1, -1, -1}; // 8 جهت حرکت
        int[] dy = {1, -1, 0, 0, 1, -1, 1, -1};

        Point currentBestTarget = null;

        while (!queue.isEmpty()) {
            Point current = queue.poll();

            if (current.x == targetX && current.y == targetY) {
                if (currentBestTarget == null
                        || calculateEnergyNeeded(current.dist, current.numTurns, false)
                        < calculateEnergyNeeded(currentBestTarget.dist, currentBestTarget.numTurns, false)) {
                    currentBestTarget = current;
                }
                // ادامه جستجو برای یافتن مسیرهای بهتر (با پیچ کمتر برای مسافت یکسان)
            }


            for (int i = 0; i < 8; i++) {
                int nextX = current.x + dx[i];
                int nextY = current.y + dy[i];

                if (farm.isValidCoordinate(nextX, nextY) && farm.getTile(nextX, nextY).isWalkable()) {
                    int newDist = current.dist + 1;
                    int newTurns = current.numTurns;
                    Point.Direction currentDirection = Point.Direction.fromDelta(dx[i], dy[i]);

                    if (current.prevDir != null && current.prevDir != currentDirection) {
                        newTurns++;
                    }

                    // اولویت با مسافت کمتر، سپس با پیچ کمتر
                    if (newDist < distances[nextY][nextX] || (newDist == distances[nextY][nextX] && newTurns < turns[nextY][nextX])) {
                        distances[nextY][nextX] = newDist;
                        turns[nextY][nextX] = newTurns;
                        predecessors[nextY][nextX] = new Point(current.x, current.y); // ذخیره والد
                        queue.add(new Point(nextX, nextY, newDist, newTurns, currentDirection));
                    }
                }
            }
        }
        if (currentBestTarget != null) {
            List<Point> path = new ArrayList<>();
            Point step = new Point(targetX, targetY); // شروع از مقصد
            while (predecessors[step.y][step.x] != null) {
                path.add(0, step); // اضافه کردن به ابتدای لیست برای ترتیب درست
                step = predecessors[step.y][step.x];
                if (step.x == startX && step.y == startY) break; // رسیدن به مبدا
            }
            path.add(0, new Point(startX, startY)); // اضافه کردن نقطه شروع
            return new PathInfo(currentBestTarget.dist, currentBestTarget.numTurns, path);
        }

        return new PathInfo(Integer.MAX_VALUE, 0, null); // مسیر پیدا نشد
    }


    public int calculateEnergyNeeded(int distanceInTiles, int numberOfTurns, boolean useComplexFormula) {
        if (distanceInTiles == Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (useComplexFormula) {
            return (distanceInTiles + 10 * numberOfTurns) / 20;
        } else {
            return distanceInTiles / 20;
        }
    }

    public void walkPlayer(Player player, int targetX, int targetY, boolean useComplexEnergyFormula) {
        if (!canMoveTo(player, targetX, targetY)) { // ابتدا بررسی کلی که آیا مقصد قابل دسترس است
            System.out.println("نمی‌توان به مقصد حرکت کرد (مسدود یا خارج از محدوده).");
            return;
        }

        PathInfo pathInfo = findShortestPath(player, player.getX(), player.getY(), targetX, targetY);

        if (pathInfo.distance == Integer.MAX_VALUE) {
            System.out.println("خطا: مسیری به مقصد (" + targetX + "," + targetY + ") پیدا نشد.");
            return;
        }

        int energyNeeded = calculateEnergyNeeded(pathInfo.distance, pathInfo.turns, useComplexEnergyFormula);
        System.out.println("انرژی مورد نیاز برای رفتن به (" + targetX + "," + targetY + "): " + energyNeeded +
                " (مسافت: " + pathInfo.distance + " کاشی، پیچ‌ها: " + pathInfo.turns + ")");
        System.out.println("مسیر پیشنهادی:");
        if (pathInfo.pathPoints != null) {
            for (Point p : pathInfo.pathPoints) {
                System.out.print("(" + p.x + "," + p.y + ") -> ");
            }
            System.out.println("پایان");
        }


        System.out.print("آیا می‌خواهید حرکت کنید؟ (yes/no): ");
        Scanner scanner = new Scanner(System.in);
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if (confirmation.equals("yes")) {
            if (player.getEnergy() >= energyNeeded) {
                player.moveTo(targetX, targetY);
                player.decreaseEnergy(energyNeeded);
                System.out.println(player.getName() + " به (" + targetX + "," + targetY + ") حرکت کرد.");
            } else {
                System.out.println("انرژی کافی برای حرکت کامل وجود ندارد.");
                // منطق غش کردن هنگام مسیر (امتیازی)
                // در این حالت ساده، بازیکن حرکت نمی‌کند اگر انرژی کامل نداشته باشد
                // یا می‌توان تا جایی که انرژی دارد حرکت کند و سپس غش کند
                System.out.println(player.getName() + " در مکان فعلی باقی ماند.");
            }
        } else {
            System.out.println("حرکت لغو شد.");
        }
    }

    public void printMap(Player player, int centerX, int centerY, int size) {
        Farm currentFarm = player.getCurrentFarm();
        if (currentFarm == null) {
            System.out.println("بازیکن در حال حاضر در هیچ مزرعه‌ای نیست.");
            return;
        }

        System.out.println("نمایش نقشه اطراف (" + centerX + "," + centerY + ") با اندازه " + size + "x" + size + " برای " + player.getName());
        int halfSize = size / 2;
        int startX = centerX - halfSize;
        int startY = centerY - halfSize;
        int endX = centerX + halfSize + (size % 2 == 0 ? -1 : 0); // تنظیم برای زوج و فرد بودن سایز
        int endY = centerY + halfSize + (size % 2 == 0 ? -1 : 0);

        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                if (currentFarm.isValidCoordinate(x, y)) {
                    if (player.getX() == x && player.getY() == y) {
                        System.out.print("P "); // نمایش بازیکن
                    } else {
                        System.out.print(currentFarm.getTile(x, y).getRepresentation() + " ");
                    }
                } else {
                    System.out.print("  "); // فضای خالی برای خارج از محدوده
                }
            }
            System.out.println();
        }
    }

    public void helpReadingMap() {
        System.out.println("--- راهنمای خواندن نقشه ---");
        System.out.println("P : بازیکن");
        for (TileType type : TileType.values()) {
            Tile tempTile = new Tile(0, 0, type);
            System.out.println(tempTile.getRepresentation() + " : " + type.getPersianName());
        }
        for (MapElementType type : MapElementType.values()) {
            // یک نمونه از هر عنصر برای نمایش کاراکتر آن
            MapElement tempElement = null;
            switch (type) {
                case TREE: tempElement = new Tree("درخت نمونه"); break;
                case ROCK: tempElement = new Rock("سنگ نمونه"); break;
                case FORAGING_ITEM: tempElement = new ForagingItem("آیتم نمونه"); break;
                case BUILDING_COMPONENT: tempElement = new BuildingComponent("ساختمان نمونه"); break;
                // CROP نیاز به پیاده‌سازی جداگانه دارد
            }
            if (tempElement != null) {
                System.out.println(tempElement.getCharRepresentation() + " : " + type.getPersianName());
            }
        }
        System.out.println("--------------------------");
    }


    // کلاس کمکی برای نگهداری اطلاعات مسیر
    static class PathInfo {
        int distance;
        int turns;
        List<Point> pathPoints; // لیست نقاط مسیر

        public PathInfo(int distance, int turns, List<Point> pathPoints) {
            this.distance = distance;
            this.turns = turns;
            this.pathPoints = pathPoints;
        }
    }

    // کلاس کمکی برای نقاط در الگوریتم جستجوی مسیر
    static class Point {
        int x, y, dist, numTurns;
        Direction prevDir; // جهت حرکت قبلی برای محاسبه پیچ‌ها

        enum Direction { UP, DOWN, LEFT, RIGHT, UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT;
            public static Direction fromDelta(int dx, int dy) {
                if (dx == 0 && dy == -1) return UP;
                if (dx == 0 && dy == 1) return DOWN;
                if (dx == -1 && dy == 0) return LEFT;
                if (dx == 1 && dy == 0) return RIGHT;
                if (dx == -1 && dy == -1) return UP_LEFT;
                if (dx == 1 && dy == -1) return UP_RIGHT;
                if (dx == -1 && dy == 1) return DOWN_LEFT;
                if (dx == 1 && dy == 1) return DOWN_RIGHT;
                return null;
            }
        }


        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
        public Point(int x, int y, int dist, int numTurns, Direction prevDir) {
            this.x = x;
            this.y = y;
            this.dist = dist;
            this.numTurns = numTurns;
            this.prevDir = prevDir;
        }
    }


    public static void main(String[] args) {
        // --- تست پیاده‌سازی ---
        Player player1 = new Player("علی", 0, 0, null); // مختصات اولیه موقتی
        List<Player> players = new ArrayList<>();
        players.add(player1);

        List<Integer> farmTypes = new ArrayList<>();
        farmTypes.add(1); // نوع مزرعه برای بازیکن اول

        GameMap gameMap = new GameMap(players, farmTypes);
        Farm p1Farm = gameMap.getFarmByOwner(player1);

        if (p1Farm != null) {
            System.out.println("مزرعه بازیکن " + player1.getName() + " با موفقیت ساخته شد.");
            p1Farm.displayFarm(player1); // نمایش مزرعه با موقعیت بازیکن
        } else {
            System.out.println("خطا: مزرعه برای بازیکن ساخته نشد.");
            return;
        }

        gameMap.helpReadingMap();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n" + player1);
            p1Farm.displayFarm(player1); // نمایش نقشه مزرعه فعلی بازیکن
            System.out.println("دستورات: walk <x> <y> | printmap <x> <y> <size> | helpmap | exit");
            System.out.print("دستور را وارد کنید: ");
            String commandLine = scanner.nextLine().trim();
            String[] parts = commandLine.split(" ");
            String command = parts[0].toLowerCase();

            try {
                if (command.equals("exit")) {
                    break;
                } else if (command.equals("walk") && parts.length == 3) {
                    int targetX = Integer.parseInt(parts[1]);
                    int targetY = Integer.parseInt(parts[2]);
                    // در این مثال، فرمول پیچیده انرژی را به صورت پیش‌فرض false در نظر می‌گیریم
                    gameMap.walkPlayer(player1, targetX, targetY, false);
                } else if (command.equals("printmap") && parts.length == 4) {
                    int centerX = Integer.parseInt(parts[1]);
                    int centerY = Integer.parseInt(parts[2]);
                    int size = Integer.parseInt(parts[3]);
                    gameMap.printMap(player1, centerX, centerY, size);
                } else if (command.equals("helpmap")) {
                    gameMap.helpReadingMap();
                }
                else {
                    System.out.println("دستور نامعتبر یا تعداد آرگومان‌ها اشتباه است.");
                }
            } catch (NumberFormatException e) {
                System.out.println("خطا: ورودی عددی نامعتبر است.");
            } catch (Exception e) {
                System.out.println("خطای غیرمنتظره: " + e.getMessage());
                e.printStackTrace();
            }
        }
        scanner.close();
        System.out.println("بازی تمام شد.");
    }
}


