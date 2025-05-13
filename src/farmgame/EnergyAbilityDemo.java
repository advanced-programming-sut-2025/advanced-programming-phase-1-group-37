import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

// Enum for different ability types
enum AbilityType {
    FARMING("Farming"), // کشاورزی
    MINING("Mining"),   // استخراج
    FORAGING("Foraging"), // طبیعت گردی
    FISHING("Fishing"); // ماهیگیری

    private final String displayName;

    AbilityType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static AbilityType fromString(String text) {
        for (AbilityType b : AbilityType.values()) {
            if (b.name().equalsIgnoreCase(text) || b.displayName.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}

// Class to manage player abilities
class Ability {
    private AbilityType type;
    private int level;
    private int experience;
    private static final int MAX_LEVEL = 4; // حداکثر سطح توانایی

    public Ability(AbilityType type) {
        this.type = type;
        this.level = 0; // سطح اولیه صفر است
        this.experience = 0; // تجربه اولیه صفر است
    }

    public AbilityType getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    public int getExperience() {
        return experience;
    }

    // Calculates the XP needed to reach the next level (cumulative)
    // بر اساس مستند: g = 100 * i + 50 واحد مورد نیاز برای رفتن به سطح i
    // i سطح هدف است.
    // برای رفتن به سطح 1 (از 0): 100*1 + 50 = 150
    // برای رفتن به سطح 2 (از 1): 100*2 + 50 = 250 (این مقدار XP برای خود سطح 2 است، نه مقدار مورد نیاز برای ارتقا از 1 به 2)
    // پس XP تجمعی برای رسیدن به سطح L برابر است با sum(100*i + 50) for i=1 to L
    private int getCumulativeXpForLevel(int targetLevel) {
        if (targetLevel <= 0) return 0;
        if (targetLevel > MAX_LEVEL) targetLevel = MAX_LEVEL;
        int cumulativeXp = 0;
        for (int i = 1; i <= targetLevel; i++) {
            cumulativeXp += (100 * i + 50);
        }
        return cumulativeXp;
    }


    public void addExperience(int amount) {
        if (level >= MAX_LEVEL) {
            System.out.println("Ability " + type.getDisplayName() + " is already at max level.");
            return;
        }

        this.experience += amount;
        System.out.println(amount + " XP added to " + type.getDisplayName() + ". Total XP: " + this.experience);

        // Check for level up
        while (this.level < MAX_LEVEL) {
            int xpNeededForNextLevelCumulative = getCumulativeXpForLevel(this.level + 1);
            if (this.experience >= xpNeededForNextLevelCumulative) {
                this.level++;
                System.out.println("Congratulations! " + type.getDisplayName() + " ability leveled up to " + this.level + "!");
                // Apply new level benefits (e.g., unlocking recipes)
                System.out.println("New crafting recipes for " + type.getDisplayName() + " unlocked.");
                if (type == AbilityType.MINING && level == 2) {
                    System.out.println("Mining special perk: Receive one extra ore per stone/mineral node.");
                }
                 if (this.level == MAX_LEVEL) {
                    System.out.println(type.getDisplayName() + " has reached the maximum level (" + MAX_LEVEL + ")!");
                    break; // Exit loop if max level is reached
                }
            } else {
                break; // Not enough XP for the next cumulative level
            }
        }
    }

    @Override
    public String toString() {
        int nextLevelXp = (level < MAX_LEVEL) ? getCumulativeXpForLevel(level + 1) : getCumulativeXpForLevel(MAX_LEVEL) ;
        String xpProgress = (level < MAX_LEVEL) ? (experience + "/" + nextLevelXp) : String.valueOf(experience);
        return type.getDisplayName() + ": Level " + level + " (XP: " + xpProgress + ")";
    }
}

// Class for items in the inventory
class InventoryItem {
    private String name;
    private int quantity;
    // Other item properties like price, type, etc., can be added

    public InventoryItem(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void addQuantity(int amount) {
        this.quantity += amount;
    }

    public void removeQuantity(int amount) {
        this.quantity -= amount;
        if (this.quantity < 0) {
            this.quantity = 0;
        }
    }

    @Override
    public String toString() {
        return name + ": " + quantity;
    }
}

// Class to manage the player's inventory
class Inventory {
    private Map<String, InventoryItem> items;
    private BackpackType currentBackpackType;

    // Enum for backpack types and their capacities
    public enum BackpackType {
        INITIAL("Initial Backpack", 12), // کوله اولیه
        LARGE("Large Backpack", 24),   // کوله بزرگ
        DELUXE("Deluxe Backpack", Integer.MAX_VALUE); // کوله دلوکس (ظرفیت بی‌نهایت برای سادگی)

        private final String typeName;
        private final int capacity; // Capacity in terms of *distinct* item slots

        BackpackType(String typeName, int capacity) {
            this.typeName = typeName;
            this.capacity = capacity;
        }

        public String getTypeName() {
            return typeName;
        }

        public int getCapacity() {
            return capacity;
        }
    }

    // Enum for trash can types and their refund percentages
    public enum TrashCanType {
        BASIC("Basic Trash Can", 0.0),      // سطل آشغال اولیه
        COPPER("Copper Trash Can", 0.15),   // سطل آشغال مسی
        STEEL("Steel Trash Can", 0.30),     // سطل آشغال آهنی
        GOLD("Gold Trash Can", 0.45),       // سطل آشغال طلایی
        IRIDIUM("Iridium Trash Can", 0.60); // سطل آشغال ایریدیومی

        private final String typeName;
        private final double refundPercentage;

        TrashCanType(String typeName, double refundPercentage) {
            this.typeName = typeName;
            this.refundPercentage = refundPercentage;
        }

        public String getTypeName() {
            return typeName;
        }

        public double getRefundPercentage() {
            return refundPercentage;
        }
    }

    private TrashCanType currentTrashCanType;


    public Inventory() {
        this.items = new HashMap<>();
        this.currentBackpackType = BackpackType.INITIAL; // Start with the initial backpack
        this.currentTrashCanType = TrashCanType.BASIC; // Start with the basic trash can
    }

    public void addItem(String itemName, int quantity) {
        if (items.containsKey(itemName)) {
            items.get(itemName).addQuantity(quantity);
            System.out.println(quantity + " " + itemName + "(s) added to inventory.");
        } else {
            if (items.size() < currentBackpackType.getCapacity()) {
                items.put(itemName, new InventoryItem(itemName, quantity));
                System.out.println(itemName + " (" + quantity + ") added to inventory.");
            } else {
                System.out.println("Error: Inventory is full! Cannot add new item type.");
            }
        }
    }

    public void removeItem(String itemName, int quantityToRemove) {
        if (items.containsKey(itemName)) {
            InventoryItem item = items.get(itemName);
            if (item.getQuantity() >= quantityToRemove) {
                item.removeQuantity(quantityToRemove);
                System.out.println(quantityToRemove + " " + itemName + "(s) removed from inventory.");
                if (item.getQuantity() == 0) {
                    items.remove(itemName); // Remove item type if quantity is zero
                    System.out.println(itemName + " completely removed from inventory.");
                }
            } else {
                System.out.println("Error: Not enough " + itemName + " in inventory to remove.");
            }
        } else {
            System.out.println("Error: Item " + itemName + " not found in inventory.");
        }
    }

    // Method to trash an item, potentially getting some money back
    public int trashItem(String itemName, int numberToTrash, int itemSellPrice) {
        if (!items.containsKey(itemName)) {
            System.out.println("Error: Item " + itemName + " not found in inventory.");
            return 0;
        }

        InventoryItem item = items.get(itemName);
        int actualTrashedCount = Math.min(numberToTrash, item.getQuantity());

        if (actualTrashedCount <= 0) {
             System.out.println("No " + itemName + " to trash.");
             return 0;
        }

        item.removeQuantity(actualTrashedCount);
        System.out.println(actualTrashedCount + " " + itemName + "(s) moved to trash.");

        if (item.getQuantity() == 0) {
            items.remove(itemName);
            System.out.println(itemName + " completely removed from inventory after trashing.");
        }

        int moneyRefunded = 0;
        if (currentTrashCanType.getRefundPercentage() > 0) {
            moneyRefunded = (int) (actualTrashedCount * itemSellPrice * currentTrashCanType.getRefundPercentage());
            System.out.println("Refunded " + moneyRefunded + " gold for trashing " + itemName + "(s).");
        } else {
            System.out.println(itemName + "(s) trashed with no refund.");
        }
        return moneyRefunded;
    }

     public void upgradeBackpack(BackpackType newType) {
        // In a real game, this would likely cost money/resources
        if (newType.getCapacity() > this.currentBackpackType.getCapacity()) {
            this.currentBackpackType = newType;
            System.out.println("Backpack upgraded to: " + newType.getTypeName() + " (Capacity: " + (newType.getCapacity() == Integer.MAX_VALUE ? "Unlimited" : newType.getCapacity()) + ")");
        } else {
            System.out.println("Cannot downgrade or upgrade to the same backpack type.");
        }
    }

    public void upgradeTrashCan(TrashCanType newType) {
        // This would also cost money/resources
        this.currentTrashCanType = newType;
        System.out.println("Trash can upgraded to: " + newType.getTypeName() + " (Refund: " + (newType.getRefundPercentage() * 100) + "%)");
    }


    public void showItems() {
        if (items.isEmpty()) {
            System.out.println("Inventory is empty.");
            return;
        }
        System.out.println("--- Inventory (" + items.size() + "/" + (currentBackpackType.getCapacity() == Integer.MAX_VALUE ? "Unlimited" : currentBackpackType.getCapacity()) + " slots used) ---");
        for (InventoryItem item : items.values()) {
            System.out.println("- " + item);
        }
        System.out.println("Current Trash Can: " + currentTrashCanType.getTypeName());
        System.out.println("--------------------------");
    }

    public boolean haveItem(String itemName, int quantity) {
        InventoryItem item = items.get(itemName);
        return item != null && item.getQuantity() >= quantity;
    }
}


// Player class to manage energy, abilities, and inventory
class Player {
    private String name;
    private int currentEnergy;
    private int maxEnergy;
    private Map<AbilityType, Ability> abilities;
    private Inventory inventory;
    private boolean isEnergyUnlimited; // Flag for unlimited energy cheat

    private static final int INITIAL_MAX_ENERGY = 200; // انرژی اولیه بازیکن
    public static final int ENERGY_PER_TURN_LIMIT = 50; // محدودیت انرژی قابل مصرف در هر نوبت

    public Player(String name) {
        this.name = name;
        this.maxEnergy = INITIAL_MAX_ENERGY;
        this.currentEnergy = this.maxEnergy; // شروع با انرژی کامل
        this.abilities = new HashMap<>();
        for (AbilityType type : AbilityType.values()) {
            abilities.put(type, new Ability(type));
        }
        this.inventory = new Inventory();
        this.isEnergyUnlimited = false;
    }

    public String getName() {
        return name;
    }

    public int getCurrentEnergy() {
        return currentEnergy;
    }

    public int getMaxEnergy() {
        return maxEnergy;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void showEnergy() {
        System.out.println(name + "'s Energy: " + currentEnergy + "/" + (isEnergyUnlimited ? "Unlimited" : maxEnergy));
    }

    // Method to consume energy for an action
    public boolean consumeEnergy(int amount) {
        if (isEnergyUnlimited) {
            System.out.println(name + " performed an action (unlimited energy).");
            return true; // Always successful if energy is unlimited
        }
        if (amount < 0) amount = 0; // Energy consumption cannot be negative

        if (currentEnergy >= amount) {
            currentEnergy -= amount;
            System.out.println(name + " used " + amount + " energy. Remaining: " + currentEnergy);
            return true;
        } else {
            System.out.println(name + " does not have enough energy (" + currentEnergy + ") for this action (needs " + amount + ").");
            // Implement passing out logic if energy reaches 0 elsewhere
            if (currentEnergy <= 0) {
                passOut();
            }
            return false;
        }
    }

    // Method for when the player passes out
    public void passOut() {
        System.out.println(name + " has passed out from exhaustion!");
        // Skip turn for the rest of the day (handled by game logic)
        // Wake up at the same spot (handled by game logic)
        // Tomorrow's energy will be 75% (handled at the start of the new day)
        currentEnergy = 0; // Ensure energy is 0
    }

    // Method to restore energy, e.g., by eating or sleeping
    public void restoreEnergy(int amount) {
        if (isEnergyUnlimited) return; // No need to restore if unlimited

        currentEnergy += amount;
        if (currentEnergy > maxEnergy) {
            currentEnergy = maxEnergy;
        }
        System.out.println(name + " restored " + amount + " energy. Current energy: " + currentEnergy);
    }

    // Method to set energy to full at the start of a new day
    public void newDayEnergyReset(boolean passedOutYesterday) {
        if (isEnergyUnlimited) {
             System.out.println(name + " starts the new day with unlimited energy.");
            return;
        }
        if (passedOutYesterday) {
            currentEnergy = (int) (maxEnergy * 0.75);
            System.out.println(name + " starts the new day with 75% energy due to passing out.");
        } else {
            currentEnergy = maxEnergy;
            System.out.println(name + " starts the new day with full energy.");
        }
    }


    public void setEnergy(int value) {
        if (isEnergyUnlimited) {
            System.out.println("Energy is set to unlimited. Cannot set to a specific value unless unlimited mode is turned off.");
            return;
        }
        if (value < 0) value = 0;
        if (value > maxEnergy) value = maxEnergy; // Cannot exceed max energy unless max energy itself is changed
        this.currentEnergy = value;
        System.out.println(name + "'s energy set to " + value + ".");
    }

    public void setUnlimitedEnergy(boolean unlimited) {
        this.isEnergyUnlimited = unlimited;
        if (unlimited) {
            System.out.println(name + "'s energy is now UNLIMITED. Turn limit also removed.");
            this.currentEnergy = this.maxEnergy; // Visually show full bar, though it's effectively infinite
        } else {
            System.out.println(name + "'s energy is no longer unlimited. Restored to " + currentEnergy + "/" + maxEnergy + ".");
        }
    }
    
    public boolean isEnergyUnlimited() {
        return isEnergyUnlimited;
    }

    public Ability getAbility(AbilityType type) {
        return abilities.get(type);
    }

    public void showAbilities() {
        System.out.println("--- " + name + "'s Abilities ---");
        for (Ability ability : abilities.values()) {
            System.out.println("- " + ability);
        }
        System.out.println("-----------------------");
    }

    // Example: Player performs an action that grants XP
    public void performFarmAction() {
        if (consumeEnergy(5)) { // Example energy cost
            System.out.println(name + " harvested a crop.");
            abilities.get(AbilityType.FARMING).addExperience(5); // 5 XP for farming
        }
    }

    public void performMineAction() {
        if (consumeEnergy(10)) { // Example energy cost
            System.out.println(name + " broke a rock.");
            abilities.get(AbilityType.MINING).addExperience(10); // 10 XP for mining
        }
    }
    // ... other actions
}


// Main class for demonstration
public class EnergyAbilityDemo {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Player player = new Player("Hero");

        System.out.println("Welcome, " + player.getName() + "!");

        // Game Loop (simplified)
        boolean gameRunning = true;
        int turnEnergySpent = 0; // انرژی مصرف شده در این نوبت

        while (gameRunning) {
            player.showEnergy();
            player.showAbilities();
            player.getInventory().showItems();
            System.out.println("\nAvailable commands:");
            System.out.println("  farm - Perform a farming action");
            System.out.println("  mine - Perform a mining action");
            System.out.println("  addxp <ability_type> <amount> - Add XP to an ability (e.g., addxp FARMING 50)");
            System.out.println("  eat <energy_amount> - Eat something to restore energy");
            System.out.println("  setenergy <value> - Cheat: Set current energy");
            System.out.println("  unlimitedenergy <on/off> - Cheat: Toggle unlimited energy");
            System.out.println("  additem <name> <quantity> - Add item to inventory (e.g., additem Stone 10)");
            System.out.println("  removeitem <name> <quantity> - Remove item from inventory");
            System.out.println("  trashitem <name> <quantity> <sell_price> - Trash item (e.g. trashitem Wood 5 2)");
            System.out.println("  upgradebackpack <INITIAL/LARGE/DELUXE> - Upgrade backpack");
            System.out.println("  upgradetrashcan <BASIC/COPPER/STEEL/GOLD/IRIDIUM> - Upgrade trash can");
            System.out.println("  passout - Simulate passing out");
            System.out.println("  newday [passed_out_yesterday:true/false] - Simulate start of a new day");
            System.out.println("  nextturn - End current turn (resets turn energy spent)");
            System.out.println("  quit - Exit game");
            System.out.print("Enter command: ");
            String command = scanner.nextLine().trim().toLowerCase();
            String[] parts = command.split(" ");

            if (parts.length == 0) continue;

            String action = parts[0];
            int energyCostThisAction = 0; // Cost for the current specific action

            // Check turn energy limit before processing actions that consume energy
            if (!player.isEnergyUnlimited() && turnEnergySpent >= Player.ENERGY_PER_TURN_LIMIT &&
                (action.equals("farm") || action.equals("mine") /* add other energy-consuming actions here */)) {
                System.out.println("You have reached your energy limit for this turn (" + Player.ENERGY_PER_TURN_LIMIT + " units). Use 'nextturn' to continue.");
                continue;
            }


            switch (action) {
                case "farm":
                    energyCostThisAction = 5; // Example cost
                    if (player.isEnergyUnlimited() || turnEnergySpent + energyCostThisAction <= Player.ENERGY_PER_TURN_LIMIT) {
                        player.performFarmAction(); // consumeEnergy is called within this
                        if(!player.isEnergyUnlimited() && player.getCurrentEnergy() > 0) turnEnergySpent += energyCostThisAction; // Only add if successful and not unlimited
                    } else {
                         System.out.println("Not enough turn energy allowance for this action.");
                    }
                    break;
                case "mine":
                    energyCostThisAction = 10; // Example cost
                     if (player.isEnergyUnlimited() || turnEnergySpent + energyCostThisAction <= Player.ENERGY_PER_TURN_LIMIT) {
                        player.performMineAction();
                        if(!player.isEnergyUnlimited() && player.getCurrentEnergy() > 0) turnEnergySpent += energyCostThisAction;
                    } else {
                         System.out.println("Not enough turn energy allowance for this action.");
                    }
                    break;
                case "addxp":
                    if (parts.length == 3) {
                        try {
                            AbilityType type = AbilityType.fromString(parts[1].toUpperCase());
                            int amount = Integer.parseInt(parts[2]);
                            if (type != null && amount > 0) {
                                player.getAbility(type).addExperience(amount);
                            } else {
                                System.out.println("Invalid ability type or amount for addxp.");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid amount for addxp. Must be a number.");
                        } catch (IllegalArgumentException e) {
                            System.out.println("Invalid ability type for addxp. Valid types: FARMING, MINING, FORAGING, FISHING");
                        }
                    } else {
                        System.out.println("Usage: addxp <ability_type> <amount>");
                    }
                    break;
                case "eat":
                    if (parts.length == 2) {
                        try {
                            int energyAmount = Integer.parseInt(parts[1]);
                            player.restoreEnergy(energyAmount);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid energy amount. Must be a number.");
                        }
                    } else {
                        System.out.println("Usage: eat <energy_amount>");
                    }
                    break;
                case "setenergy":
                    if (parts.length == 2) {
                        try {
                            int value = Integer.parseInt(parts[1]);
                            player.setEnergy(value);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid energy value. Must be a number.");
                        }
                    } else {
                        System.out.println("Usage: setenergy <value>");
                    }
                    break;
                case "unlimitedenergy":
                    if (parts.length == 2) {
                        if (parts[1].equals("on")) {
                            player.setUnlimitedEnergy(true);
                            turnEnergySpent = 0; // Reset turn energy if going unlimited
                        } else if (parts[1].equals("off")) {
                            player.setUnlimitedEnergy(false);
                        } else {
                            System.out.println("Usage: unlimitedenergy <on/off>");
                        }
                    } else {
                        System.out.println("Usage: unlimitedenergy <on/off>");
                    }
                    break;
                case "additem":
                    if (parts.length == 3) {
                        try {
                            String itemName = parts[1];
                            int quantity = Integer.parseInt(parts[2]);
                            player.getInventory().addItem(itemName, quantity);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid quantity. Must be a number.");
                        }
                    } else {
                        System.out.println("Usage: additem <name> <quantity>");
                    }
                    break;
                case "removeitem":
                    if (parts.length == 3) {
                        try {
                            String itemName = parts[1];
                            int quantity = Integer.parseInt(parts[2]);
                            player.getInventory().removeItem(itemName, quantity);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid quantity. Must be a number.");
                        }
                    } else {
                        System.out.println("Usage: removeitem <name> <quantity>");
                    }
                    break;
                case "trashitem":
                    if (parts.length == 4) {
                        try {
                            String itemName = parts[1];
                            int quantity = Integer.parseInt(parts[2]);
                            int sellPrice = Integer.parseInt(parts[3]); // Base sell price of the item
                            int refunded = player.getInventory().trashItem(itemName, quantity, sellPrice);
                            // In a real game, add 'refunded' to player's money
                            System.out.println("Player received " + refunded + " gold from trashing.");
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid quantity or sell price. Must be numbers.");
                        }
                    } else {
                        System.out.println("Usage: trashitem <name> <quantity> <base_sell_price>");
                    }
                    break;
                case "upgradebackpack":
                    if (parts.length == 2) {
                        try {
                            Inventory.BackpackType type = Inventory.BackpackType.valueOf(parts[1].toUpperCase());
                            player.getInventory().upgradeBackpack(type);
                        } catch (IllegalArgumentException e) {
                            System.out.println("Invalid backpack type. Available: INITIAL, LARGE, DELUXE");
                        }
                    } else {
                        System.out.println("Usage: upgradebackpack <INITIAL/LARGE/DELUXE>");
                    }
                    break;
                case "upgradetrashcan":
                     if (parts.length == 2) {
                        try {
                            Inventory.TrashCanType type = Inventory.TrashCanType.valueOf(parts[1].toUpperCase());
                            player.getInventory().upgradeTrashCan(type);
                        } catch (IllegalArgumentException e) {
                            System.out.println("Invalid trash can type. Available: BASIC, COPPER, STEEL, GOLD, IRIDIUM");
                        }
                    } else {
                        System.out.println("Usage: upgradetrashcan <BASIC/COPPER/STEEL/GOLD/IRIDIUM>");
                    }
                    break;
                case "passout":
                    player.passOut();
                    System.out.println("Turn ends. Use 'newday passed_out_yesterday:true' to simulate next day.");
                    turnEnergySpent = Player.ENERGY_PER_TURN_LIMIT; // Effectively ends the turn
                    break;
                case "newday":
                    boolean passedOut = false;
                    if (parts.length == 2 && parts[1].equalsIgnoreCase("passed_out_yesterday:true")) {
                        passedOut = true;
                    }
                    player.newDayEnergyReset(passedOut);
                    turnEnergySpent = 0; // Reset turn energy for the new day
                    System.out.println("A new day has begun!");
                    break;
                case "nextturn":
                    System.out.println(player.getName() + " ends their turn.");
                    // In a multiplayer game, this would switch to the next player.
                    // For single player, it just resets the turn energy allowance.
                    turnEnergySpent = 0;
                    System.out.println("Turn energy allowance reset.");
                    // If all players took a turn, game time would advance.
                    break;
                case "quit":
                    gameRunning = false;
                    System.out.println("Exiting game. Thanks for playing!");
                    break;
                default:
                    System.out.println("Unknown command.");
            }
            if (player.getCurrentEnergy() <= 0 && !player.isEnergyUnlimited() && !action.equals("passout") && !action.equals("newday")) {
                 player.passOut(); // Automatically pass out if energy hits 0 after an action
                 System.out.println("Turn ends due to passing out. Use 'newday passed_out_yesterday:true' to simulate next day.");
                 turnEnergySpent = Player.ENERGY_PER_TURN_LIMIT;
            }
             System.out.println("Energy spent this turn so far: " + turnEnergySpent + "/" + Player.ENERGY_PER_TURN_LIMIT + (player.isEnergyUnlimited() ? " (Unlimited Energy Active)" : ""));
        }
        scanner.close();
    }
}

