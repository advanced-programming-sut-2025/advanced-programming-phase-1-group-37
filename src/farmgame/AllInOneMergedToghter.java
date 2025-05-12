import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

class Item {
    String name;
    int basePrice;
    String quality = "Regular"; // Can be Regular, Silver, Gold, Iridium
    boolean edible;
    int energyValue;

    public Item(String name, int basePrice, boolean edible, int energyValue) {
        this.name = name;
        this.basePrice = basePrice;
        this.edible = edible;
        this.energyValue = energyValue;
    }

    public String getName() {
        return name;
    }

    public int getSellPrice() {
        double multiplier = 1.0;
        switch (quality) {
            case "Silver": multiplier = 1.25; break;
            case "Gold": multiplier = 1.5; break;
            case "Iridium": multiplier = 2.0; break;
        }
        // Sell price for buyable items is often half the buy price.
        // For simplicity, just applying quality multiplier to base price here.
        // Base price itself might need adjustment based on source (buy/craft/find).
        int sellPrice = (int) (basePrice * multiplier);
        // Example: If basePrice is buy price, sell price is half
        // int sellPrice = (int) ((basePrice / 2.0) * multiplier);
        return sellPrice > 0 ? sellPrice : 1; // Ensure sell price is at least 1 if base is low
    }


    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    @Override
    public String toString() {
        return name + (quality.equals("Regular") ? "" : " (" + quality + ")");
    }

    public boolean isEdible() {
        return edible;
    }

    public int getEnergyValue() {
        return energyValue;
    }
}

class Player {
    String username;
    String nickname;
    String gender;
    int money;
    List<Item> inventory;
    Map<String, Integer> playerFriendshipPoints;
    Map<String, Integer> npcFriendshipPoints;
    Map<Integer, GiftInteraction> pendingGiftRatings;
    Map<String, List<GiftInteraction>> playerGiftHistory;
    Map<String, List<Item>> npcGiftHistory;
    List<Quest> activeQuests;
    Map<Integer, TradeOffer> pendingTradeOffers;
    List<TradeOffer> tradeHistory;
    Player spouse = null;
    Location currentLocation;
    int energy;
    int maxEnergy = 200;
    boolean passedOut = false; // Flag for passing out

    private static int nextGiftId = 1;
    private static int nextTradeId = 1;


    public Player(String username, String nickname, String gender, int initialMoney, Location startLocation) {
        this.username = username;
        this.nickname = nickname;
        this.gender = gender;
        this.money = initialMoney;
        this.inventory = new ArrayList<>();
        this.playerFriendshipPoints = new HashMap<>();
        this.npcFriendshipPoints = new HashMap<>();
        this.pendingGiftRatings = new HashMap<>();
        this.playerGiftHistory = new HashMap<>();
        this.npcGiftHistory = new HashMap<>();
        this.activeQuests = new ArrayList<>();
        this.pendingTradeOffers = new HashMap<>();
        this.tradeHistory = new ArrayList<>();
        this.currentLocation = startLocation;
        this.energy = maxEnergy;
    }

    public boolean consumeEnergy(int amount) {
        if (this.energy >= amount) {
            this.energy -= amount;
            System.out.println(username + " consumed " + amount + " energy. Remaining: " + this.energy);
            if (this.energy <= 0) {
                passOut();
            }
            return true;
        } else {
            System.out.println("Error: Not enough energy for this action. Required: " + amount + ", Available: " + this.energy);
            // Optionally, pass out immediately if any action is attempted with insufficient energy
            // passOut();
            return false;
        }
    }

    public void restoreEnergy(int amount) {
        this.energy = Math.min(this.energy + amount, this.maxEnergy);
         System.out.println(username + " restored " + amount + " energy. Current: " + this.energy);
    }

    public void restoreFullEnergy() {
         if (passedOut) {
             this.energy = (int) (this.maxEnergy * 0.75);
             System.out.println(username + " woke up exhausted. Energy restored to 75%: " + this.energy);
             this.passedOut = false; // Reset flag for the new day
         } else {
             this.energy = this.maxEnergy;
             System.out.println(username + " energy fully restored: " + this.energy);
         }
    }


    private void passOut() {
        System.out.println(username + " ran out of energy and passed out!");
        this.energy = 0;
        this.passedOut = true;
        // Additional logic needed: skip rest of the day, wake up at current location
        // This requires integration with the game loop and time system.
    }

    public void eat(String itemName) {
        Item itemToEat = null;
        int index = -1;
        for (int i = 0; i < inventory.size(); i++) {
            Item item = inventory.get(i);
            if (item.getName().equalsIgnoreCase(itemName) && item.isEdible()) {
                itemToEat = item;
                index = i;
                break;
            }
        }

        if (itemToEat != null) {
            inventory.remove(index);
            int energyGain = itemToEat.getEnergyValue();
            restoreEnergy(energyGain);
            System.out.println(username + " ate " + itemName + " and gained " + energyGain + " energy.");
            // Apply buffs if any (not implemented here)
        } else {
            System.out.println("Error: Cannot eat '" + itemName + "'. Item not found or not edible.");
        }
    }


    public void addItem(Item item, int count) {
        for (int i = 0; i < count; i++) {
            inventory.add(item);
        }
        System.out.println(count + "x " + item.getName() + " added to " + username + "'s inventory.");
    }

    public boolean removeItem(String itemName, int count) {
        int removedCount = 0;
        List<Item> toRemove = new ArrayList<>();
        for (Item item : inventory) {
            if (item.getName().equalsIgnoreCase(itemName) && removedCount < count) {
                toRemove.add(item);
                removedCount++;
            }
        }
        if (removedCount == count) {
            inventory.removeAll(toRemove);
            System.out.println(count + "x " + itemName + " removed from " + username + "'s inventory.");
            return true;
        } else {
            System.out.println("Error: Not enough '" + itemName + "' in inventory (found " + removedCount + ", needed " + count + ").");
            return false;
        }
    }

    public int getItemCount(String itemName) {
        int count = 0;
        for (Item item : inventory) {
            if (item.getName().equalsIgnoreCase(itemName)) {
                count++;
            }
        }
        return count;
    }

     public void addMoney(int amount) {
        if (spouse != null) {
             if (this == spouse.spouse) {
                 spouse.money += amount;
                 System.out.println(amount + "g added to shared account (" + username + " & " + spouse.username + "). Current balance: " + spouse.money + "g");
             }
        } else {
            this.money += amount;
            System.out.println(amount + "g added to " + username + ". Current balance: " + money + "g");
        }
    }

    public boolean spendMoney(int amount) {
         int currentBalance = getMoney(); // Use getter to handle spouse case
        if (currentBalance >= amount) {
             if (spouse != null) {
                 spouse.money -= amount;
                  System.out.println(amount + "g spent from shared account (" + username + " & " + spouse.username + "). Remaining balance: " + spouse.money + "g");
             } else {
                 this.money -= amount;
                 System.out.println(amount + "g spent by " + username + ". Remaining balance: " + money + "g");
             }
            return true;
        } else {
            System.out.println("Error: Not enough money. Required: " + amount + "g, Available: " + currentBalance + "g");
            return false;
        }
    }

     public int getMoney() {
         return (spouse != null) ? spouse.money : this.money;
     }


    public int getPlayerFriendshipLevel(String otherPlayerUsername) {
        int points = playerFriendshipPoints.getOrDefault(otherPlayerUsername, 0);
        if (points < 100) return 0;
        if (points < 300) return 1;
        if (points < 800) return 2;
        if (points < 1800) return 3;
        return 4;
    }

     public void increasePlayerFriendship(String otherPlayerUsername, int amount) {
         if (this.username.equals(otherPlayerUsername)) return;

         int currentPoints = playerFriendshipPoints.getOrDefault(otherPlayerUsername, 0);
         playerFriendshipPoints.put(otherPlayerUsername, currentPoints + amount);
         System.out.println("Friendship between " + this.username + " and " + otherPlayerUsername + " increased by " + amount + " points.");

         Player otherPlayer = GameManager.findPlayer(otherPlayerUsername);
         if (otherPlayer != null && !otherPlayer.playerFriendshipPoints.containsKey(this.username) || otherPlayer.playerFriendshipPoints.get(this.username) != currentPoints + amount) {
              otherPlayer.playerFriendshipPoints.put(this.username, currentPoints + amount);
         }
     }

     public void decreasePlayerFriendship(String otherPlayerUsername, int amount) {
        if (this.username.equals(otherPlayerUsername)) return;

        int currentPoints = playerFriendshipPoints.getOrDefault(otherPlayerUsername, 0);
        int newPoints = Math.max(0, currentPoints - amount);

        if (newPoints == 0 && currentPoints > 0) {
             int oldLevel = getPlayerFriendshipLevel(otherPlayerUsername);
             playerFriendshipPoints.put(otherPlayerUsername, newPoints);
             int newLevel = getPlayerFriendshipLevel(otherPlayerUsername);
             if (newLevel < oldLevel) {
                 System.out.println("Friendship level between " + this.username + " and " + otherPlayerUsername + " decreased.");
             }
         } else {
             playerFriendshipPoints.put(otherPlayerUsername, newPoints);
         }
         System.out.println("Friendship between " + this.username + " and " + otherPlayerUsername + " decreased by " + amount + " points (or reached 0).");

         Player otherPlayer = GameManager.findPlayer(otherPlayerUsername);
          if (otherPlayer != null && (!otherPlayer.playerFriendshipPoints.containsKey(this.username) || otherPlayer.playerFriendshipPoints.get(this.username) != newPoints)) {
              otherPlayer.playerFriendshipPoints.put(this.username, newPoints);
          }
     }


    public void talkToPlayer(Player otherPlayer, String message) {
        if (!isNear(otherPlayer.currentLocation)) {
            System.out.println("Error: Must be near player " + otherPlayer.username + " to talk.");
            return;
        }
        if (!consumeEnergy(1)) return; // Consume energy for talking

        System.out.println(this.username + " says to " + otherPlayer.username + ": " + message);
        increasePlayerFriendship(otherPlayer.username, 20);
        // otherPlayer.increasePlayerFriendship(this.username, 20); // Handled by increasePlayerFriendship symmetry
    }

    public void sendGiftToPlayer(Player receiver, String itemName, int amount) {
        if (!isNear(receiver.currentLocation)) {
            System.out.println("Error: Must be near player " + receiver.username + " to give a gift.");
            return;
        }
        int currentFriendshipLevel = getPlayerFriendshipLevel(receiver.username);
        if (currentFriendshipLevel < 1) {
            System.out.println("Error: Must be at least friendship level 1 to give gifts.");
            return;
        }

        int availableCount = getItemCount(itemName);
        if (availableCount < amount) {
            System.out.println("Error: Not enough '" + itemName + "' to gift.");
            return;
        }

        Item itemToSend = null;
        for(Item item : inventory) {
            if(item.getName().equalsIgnoreCase(itemName)) {
                itemToSend = item;
                break;
            }
        }

        if (itemToSend == null) {
             System.out.println("Error: Item '" + itemName + "' not found.");
             return;
        }

        if (!consumeEnergy(2)) return; // Consume energy for gifting

        if (removeItem(itemName, amount)) {
            int giftId = nextGiftId++;
            GiftInteraction gift = new GiftInteraction(giftId, this.username, receiver.username, itemToSend, amount);
            receiver.receiveGiftFromPlayer(gift);

            playerGiftHistory.computeIfAbsent(receiver.username, k -> new ArrayList<>()).add(gift);

            System.out.println(this.username + " gifted " + amount + "x " + itemName + " to " + receiver.username + ".");
        }
    }

     public void receiveGiftFromPlayer(GiftInteraction gift) {
         pendingGiftRatings.put(gift.id, gift);
         playerGiftHistory.computeIfAbsent(gift.senderUsername, k -> new ArrayList<>()).add(gift);
         System.out.println(this.username + " received a gift (" + gift.item.getName() + " x" + gift.amount + ") from " + gift.senderUsername + ". Please rate it.");
         addItem(gift.item, gift.amount);
     }

    public void rateGift(int giftId, int rating) {
        if (!pendingGiftRatings.containsKey(giftId)) {
            System.out.println("Error: No pending gift with ID " + giftId + " to rate.");
            return;
        }
        if (rating < 1 || rating > 5) {
            System.out.println("Error: Rating must be between 1 and 5.");
            return;
        }

        GiftInteraction gift = pendingGiftRatings.remove(giftId);
        gift.rating = rating;

        int friendshipChange = 15 + 30 * (rating - 3);

        System.out.println(this.username + " rated the gift (" + gift.item.getName() + ") from " + gift.senderUsername + " with " + rating + " stars.");

        increasePlayerFriendship(gift.senderUsername, friendshipChange);
    }

     public void showPendingGifts() {
         System.out.println("--- Pending Gift Ratings for " + username + " ---");
         if (pendingGiftRatings.isEmpty()) {
             System.out.println("No gifts waiting for rating.");
             return;
         }
         for (GiftInteraction gift : pendingGiftRatings.values()) {
             System.out.println("ID: " + gift.id + " | From: " + gift.senderUsername + " | Item: " + gift.item.getName() + " | Amount: " + gift.amount);
         }
         System.out.println("-------------------------------------------");
     }

     public void showGiftHistory(String otherPlayerUsername) {
         System.out.println("--- Gift History between " + username + " and " + otherPlayerUsername + " ---");
         List<GiftInteraction> history = playerGiftHistory.get(otherPlayerUsername);
         if (history == null || history.isEmpty()) {
             System.out.println("No gift history found.");
             return;
         }
         for (GiftInteraction gift : history) {
             String direction = gift.senderUsername.equals(this.username) ? "Sent to " + gift.receiverUsername : "Received from " + gift.senderUsername;
             String ratingInfo = gift.rating != 0 ? " | Rating: " + gift.rating : " | Pending Rating";
             System.out.println("ID: " + gift.id + " | " + direction + " | Item: " + gift.item.getName() + " | Amount: " + gift.amount + ratingInfo);
         }
         System.out.println("-------------------------------------------------------");
     }


    public void hugPlayer(Player otherPlayer) {
        if (!isNear(otherPlayer.currentLocation)) {
            System.out.println("Error: Must be near player " + otherPlayer.username + " to hug.");
            return;
        }
        int currentFriendshipLevel = getPlayerFriendshipLevel(otherPlayer.username);
        if (currentFriendshipLevel < 2) {
            System.out.println("Error: Must be at least friendship level 2 to hug.");
            return;
        }
        if (!consumeEnergy(1)) return; // Consume energy for hugging

        System.out.println(this.username + " hugged " + otherPlayer.username + ".");
        increasePlayerFriendship(otherPlayer.username, 60);
    }

    public void giveFlower(Player otherPlayer) {
        if (!isNear(otherPlayer.currentLocation)) {
            System.out.println("Error: Must be near player " + otherPlayer.username + " to give a flower.");
            return;
        }
         int currentFriendshipLevel = getPlayerFriendshipLevel(otherPlayer.username);
         int otherFriendshipLevel = otherPlayer.getPlayerFriendshipLevel(this.username);

         int pointsNeededForLevel3 = 800;
         if (currentFriendshipLevel != 2 || otherFriendshipLevel != 2 ||
             playerFriendshipPoints.getOrDefault(otherPlayer.username, 0) < pointsNeededForLevel3 ||
             otherPlayer.playerFriendshipPoints.getOrDefault(this.username, 0) < pointsNeededForLevel3) {
             System.out.println("Error: Conditions not met to give flower and reach level 3 friendship (both must be level 2 with enough points).");
             return;
         }


        String flowerName = "Bouquet";
        if (getItemCount(flowerName) < 1) {
            System.out.println("Error: You don't have a Bouquet to give.");
            return;
        }
         if (!consumeEnergy(5)) return; // Consume energy for giving bouquet

        if (removeItem(flowerName, 1)) {
            Item flowerItem = new Item(flowerName, 100, false, 0); // Recreate item instance

            otherPlayer.addItem(flowerItem, 1);
            System.out.println(this.username + " gave a bouquet to " + otherPlayer.username + ". Friendship level increased to 3!");

            playerFriendshipPoints.put(otherPlayer.username, pointsNeededForLevel3);
            otherPlayer.playerFriendshipPoints.put(this.username, pointsNeededForLevel3);

        }
    }

    public void proposeMarriage(Player targetPlayer, String ringName) {
        if (!isNear(targetPlayer.currentLocation)) {
            System.out.println("Error: Must be near player " + targetPlayer.username + " to propose.");
            return;
        }
        if (this.spouse != null || targetPlayer.spouse != null) {
            System.out.println("Error: One or both players are already married.");
            return;
        }
        if (this.gender.equalsIgnoreCase(targetPlayer.gender)) {
            System.out.println("Error: Players must have different genders to marry.");
            return;
        }
        int currentFriendshipLevel = getPlayerFriendshipLevel(targetPlayer.username);
        if (currentFriendshipLevel < 3) {
            System.out.println("Error: Must be at least friendship level 3 to propose.");
            return;
        }
         int pointsNeededForLevel4 = 1800;
         if (playerFriendshipPoints.getOrDefault(targetPlayer.username, 0) < pointsNeededForLevel4) {
             System.out.println("Error: Not enough friendship points to propose.");
             return;
         }


        if (getItemCount(ringName) < 1) {
            System.out.println("Error: You don't have a '" + ringName + "' to propose with.");
            return;
        }

        if (!consumeEnergy(10)) return; // Consume energy for proposing

        System.out.println(this.username + " proposed to " + targetPlayer.username + " with a '" + ringName + "'.");
        GameManager.addPendingMarriageProposal(targetPlayer, this, ringName);
    }

     public void receiveMarriageProposal(Player proposer, String ringName) {
         System.out.println(this.username + ", player " + proposer.username + " has proposed marriage to you!");
         System.out.println("Use 'respond (-accept | -reject) -u " + proposer.username + "' to answer.");
     }

    public void respondToProposal(Player proposer, boolean accept) {
         MarriageProposal proposal = GameManager.findPendingProposal(this, proposer);
         if (proposal == null) {
             System.out.println("Error: No pending marriage proposal found from " + proposer.username);
             return;
         }

         GameManager.removePendingProposal(this, proposer); // Remove proposal whether accepted or rejected


        if (accept) {
            System.out.println(this.username + " accepted the marriage proposal from " + proposer.username + ".");

             if (proposer.spouse != null || this.spouse != null ||
                 proposer.gender.equalsIgnoreCase(this.gender) ||
                 proposer.getPlayerFriendshipLevel(this.username) < 3 ||
                 proposer.playerFriendshipPoints.getOrDefault(this.username, 0) < 1800) {
                 System.out.println("Error: Marriage conditions are no longer met.");
                 return;
             }


             Item ringItem = null;
             boolean ringRemoved = false;
             List<Item> proposerInventory = proposer.inventory;
             String ringName = proposal.ringName;
             for (int i = 0; i < proposerInventory.size(); i++) {
                 Item item = proposerInventory.get(i);
                 if (item.getName().equalsIgnoreCase(ringName)) {
                     ringItem = item;
                     proposerInventory.remove(i);
                     ringRemoved = true;
                     break;
                 }
             }


             if (!ringRemoved || ringItem == null) {
                 System.out.println("Error: The proposal ring ('" + ringName + "') was not found in " + proposer.username + "'s inventory!");
                 return;
             }


            this.spouse = proposer;
            proposer.spouse = this;

            this.addItem(ringItem, 1);

            int pointsForLevel4 = 1800;
            this.playerFriendshipPoints.put(proposer.username, pointsForLevel4);
            proposer.playerFriendshipPoints.put(this.username, pointsForLevel4);

             int totalMoney = this.money + proposer.money;
             this.money = totalMoney;
             proposer.money = totalMoney;
             System.out.println("Money merged. Shared balance: " + this.money + "g");


            System.out.println(this.username + " and " + proposer.username + " are now married!");
            System.out.println("Your farm lands are now shared.");


        } else {
            System.out.println(this.username + " rejected the marriage proposal from " + proposer.username + ".");
            this.playerFriendshipPoints.put(proposer.username, 0);
            proposer.playerFriendshipPoints.put(this.username, 0);
            System.out.println("Friendship level between you dropped to 0.");

            if (proposer.gender.equalsIgnoreCase("Male")) { // Assuming "Male" or "Female"
                System.out.println(proposer.username + "'s energy will be halved for the next 7 days.");
                // Apply energy debuff effect (requires effect system)
                // proposer.applyEffect(new EnergyDebuff(7));
            }
        }
    }


    public int getNPCFriendshipLevel(String npcName) {
        int points = npcFriendshipPoints.getOrDefault(npcName, 0);
        return Math.min(points / 200, 3);
    }

    public void increaseNPCFriendship(String npcName, int amount) {
        int currentPoints = npcFriendshipPoints.getOrDefault(npcName, 0);
        int newPoints = Math.min(currentPoints + amount, 799);
        npcFriendshipPoints.put(npcName, newPoints);
        System.out.println("Friendship with " + npcName + " increased by " + amount + ". Current points: " + newPoints);
    }

    public void meetNPC(NPC npc) {
        if (!isNear(npc.currentLocation)) {
            System.out.println("Error: Must be near " + npc.name + " to meet.");
            return;
        }
        if (!consumeEnergy(1)) return; // Consume energy for meeting

        String dialogue = npc.getRandomDialogue();
        System.out.println(npc.name + ": \"" + dialogue + "\"");

        increaseNPCFriendship(npc.name, 20);
    }

    public void sendGiftToNPC(NPC npc, String itemName) {
        if (!isNear(npc.currentLocation)) {
            System.out.println("Error: Must be near " + npc.name + " to give a gift.");
            return;
        }

        Item itemToSend = null;
        for (Item item : inventory) {
            if (item.getName().equalsIgnoreCase(itemName)) {
                // Check if item is a tool - cannot gift tools (requires Tool class or identifier)
                // if (item.isTool()) {
                //     System.out.println("Error: Cannot gift tools.");
                //     return;
                // }
                itemToSend = item;
                break;
            }
        }

        if (itemToSend == null) {
            System.out.println("Error: Item '" + itemName + "' not found in your inventory.");
            return;
        }

        if (!consumeEnergy(2)) return; // Consume energy for gifting NPC

        if (removeItem(itemName, 1)) {
            System.out.println(this.username + " gifted " + itemName + " to " + npc.name + ".");
            npc.receiveGift(this, itemToSend);

            npcGiftHistory.computeIfAbsent(npc.name, k -> new ArrayList<>()).add(itemToSend);
        }
    }

     public void showNPCFriendships() {
         System.out.println("--- " + username + "'s NPC Friendships ---");
         if (npcFriendshipPoints.isEmpty()) {
             System.out.println("No friendships established yet.");
             return;
         }
         for (Map.Entry<String, Integer> entry : npcFriendshipPoints.entrySet()) {
             String npcName = entry.getKey();
             int points = entry.getValue();
             int level = getNPCFriendshipLevel(npcName);
             System.out.println(npcName + ": " + points + " points (Level " + level + ")");
         }
         System.out.println("---------------------------------------");
     }


    public void listAvailableQuests(NPC npc) {
         if (!isNear(npc.currentLocation)) {
             System.out.println("Error: Must be near " + npc.name + " to view quests.");
             return;
         }
        System.out.println("--- Available Quests from " + npc.name + " for " + username + " ---");
        boolean foundActive = false;
        int index = 1;

        System.out.println("Active Quests:");
        for (Quest quest : activeQuests) {
            if (quest.giverNpcName.equals(npc.name)) {
                System.out.println("  " + index + ". " + quest.description + " (Reward: " + quest.rewardDescription + ")");
                quest.displayIndex = index;
                foundActive = true;
                index++;
            }
        }
         if (!foundActive) {
             System.out.println("  (None active from this NPC)");
         }

        System.out.println("\nNew Possible Quests:");
        boolean foundNew = false;
        int currentNpcFriendshipLevel = getNPCFriendshipLevel(npc.name);
        for (Quest npcQuest : npc.availableQuests) {
            boolean alreadyTaken = activeQuests.stream().anyMatch(q -> q.id == npcQuest.id);
            boolean alreadyCompleted = npc.isQuestCompletedGlobally(npcQuest.id); // Check if anyone completed it

             boolean conditionsMet = false;
             if (!alreadyCompleted && !alreadyTaken && npcQuest.requiredFriendshipLevel <= currentNpcFriendshipLevel) {
                 if (npcQuest.activationCondition == null || npcQuest.activationCondition.equals("immediate")) {
                     conditionsMet = true;
                 } else if (npcQuest.activationCondition.startsWith("time:")) {
                     // int daysPassed = GameManager.getDaysPassed();
                     // int requiredDays = Integer.parseInt(npcQuest.activationCondition.split(":")[1]);
                     // if (daysPassed >= requiredDays) conditionsMet = true;
                     conditionsMet = true; // Simplified time check
                 }
             }


            if (conditionsMet) {
                System.out.println("  - " + npcQuest.description + " (Reward: " + npcQuest.rewardDescription + ")");
                foundNew = true;
            }
        }
         if (!foundNew) {
             System.out.println("  (None available at the moment)");
         }

        System.out.println("--------------------------------------------------");
    }

     public void acceptQuest(Quest quest) {
         if (!activeQuests.stream().anyMatch(q -> q.id == quest.id)) {
             activeQuests.add(quest);
             System.out.println("Quest accepted: '" + quest.description + "'");
         } else {
              System.out.println("Quest '" + quest.description + "' is already active.");
         }
     }

    public void finishQuest(int questIndex) {
        Quest questToFinish = null;
        for (Quest quest : activeQuests) {
            if (quest.displayIndex == questIndex) {
                questToFinish = quest;
                break;
            }
        }

        if (questToFinish == null) {
            System.out.println("Error: No active quest found with index " + questIndex + ".");
            return;
        }

        NPC giverNpc = GameManager.findNPC(questToFinish.giverNpcName);
        if (giverNpc == null || !isNear(giverNpc.currentLocation)) {
            System.out.println("Error: Must be near " + (giverNpc != null ? giverNpc.name : "the quest giver") + " to finish the quest.");
            return;
        }

         if (giverNpc.isQuestCompletedGlobally(questToFinish.id)) {
             System.out.println("Error: Quest '" + questToFinish.description + "' has already been completed by " + questToFinish.completedByPlayer + ".");
             activeQuests.remove(questToFinish); // Remove from active list if completed by someone else
             return;
         }

        boolean hasItems = true;
        if (questToFinish.requiredItems != null) {
            for (Map.Entry<String, Integer> entry : questToFinish.requiredItems.entrySet()) {
                if (getItemCount(entry.getKey()) < entry.getValue()) {
                    System.out.println("Error: Missing required items for '" + questToFinish.description + "'. Need " + entry.getValue() + "x " + entry.getKey() + ".");
                    hasItems = false;
                    break;
                }
            }
        }

        if (hasItems) {
             if (!consumeEnergy(5)) return; // Consume energy for finishing quest

            if (questToFinish.requiredItems != null) {
                for (Map.Entry<String, Integer> entry : questToFinish.requiredItems.entrySet()) {
                    removeItem(entry.getKey(), entry.getValue());
                }
            }

            System.out.println("Quest '" + questToFinish.description + "' completed successfully!");
            activeQuests.remove(questToFinish);


            System.out.println("Reward received: " + questToFinish.rewardDescription);
            questToFinish.grantReward(this, giverNpc);

             giverNpc.markQuestAsCompletedForPlayer(questToFinish.id, this.username);

        }
    }

    public void startPlayerTrade() {
        System.out.println("--- Player Trade Menu ---");
        System.out.println("Available commands:");
        System.out.println("  trade -u <username> -t <type> -i <item> -a <amount> [-p <price>] [-ti <targetItem> -ta <targetAmount>]");
        System.out.println("  trade list");
        System.out.println("  trade response (-accept | -reject) -i <id>");
        System.out.println("  trade history");
        System.out.println("--------------------------");
         showNewTradeOffers();
    }

     private void showNewTradeOffers() {
         System.out.println("--- New Trade Offers for " + username + " ---");
         boolean foundNew = false;
         // Need a way to track 'new' offers, using pending list for now
         List<TradeOffer> offersToShow = GameManager.getPendingOffersForPlayer(username);
         if (offersToShow != null && !offersToShow.isEmpty()) {
             for (TradeOffer offer : offersToShow) {
                  if (pendingTradeOffers.containsKey(offer.id)) { // Only show if it's still pending for this player
                     System.out.println("ID: " + offer.id + " | From: " + offer.offeringPlayerUsername + " | Type: " + offer.type);
                     System.out.println("    Offer/Request: " + offer.amount + "x " + offer.itemName);
                     if (offer.price > 0) {
                         System.out.println("    For: " + offer.price + "g");
                     } else if (offer.targetItemName != null) {
                         System.out.println("    For: " + offer.targetAmount + "x " + offer.targetItemName);
                     }
                     foundNew = true;
                  }
             }
         }

         if (!foundNew) {
             System.out.println("No new trade offers.");
         }
         System.out.println("-------------------------------------------");
     }


    public void createTradeOffer(String targetUsername, String type, String itemName, int amount, int price, String targetItemName, int targetAmount) {
         Player targetPlayer = GameManager.findPlayer(targetUsername);
         if (targetPlayer == null) {
             System.out.println("Error: Player '" + targetUsername + "' not found.");
             return;
         }
         if (targetUsername.equals(this.username)) {
              System.out.println("Error: Cannot trade with yourself.");
              return;
         }


        if (amount <= 0 || (price < 0) || (targetAmount < 0)) {
            System.out.println("Error: Invalid amount/price for trade.");
            return;
        }
        if (price > 0 && targetItemName != null) {
            System.out.println("Error: Cannot request both money and items as payment.");
            return;
        }

        if (type.equalsIgnoreCase("offer")) {
            if (getItemCount(itemName) < amount) {
                System.out.println("Error: Not enough '" + itemName + "' to offer.");
                return;
            }
        }
        else if (type.equalsIgnoreCase("request")) {
            if (price > 0 && getMoney() < price) {
                 System.out.println("Error: Not enough money to back this request.");
                 return;
            }
             if (targetItemName != null && getItemCount(targetItemName) < targetAmount) {
                 System.out.println("Error: Not enough '" + targetItemName + "' to offer in exchange for the request.");
                 return;
             }
        } else {
             System.out.println("Error: Invalid trade type (must be 'offer' or 'request').");
             return;
        }

        if (!consumeEnergy(3)) return; // Consume energy for creating offer


        int tradeId = nextTradeId++;
        TradeOffer offer = new TradeOffer(tradeId, this.username, targetUsername, type, itemName, amount, price, targetItemName, targetAmount);

         GameManager.addPendingTradeOffer(targetUsername, offer);

        tradeHistory.add(offer);

        System.out.println("Trade offer/request (ID " + tradeId + ") sent to " + targetUsername + ".");
    }

     public void receiveTradeOffer(TradeOffer offer) {
         pendingTradeOffers.put(offer.id, offer);
         System.out.println("Received a new trade offer/request (ID " + offer.id + ") from " + offer.offeringPlayerUsername + ".");
     }

     public void listPendingTradeOffers() {
         System.out.println("--- Pending Trade Offers/Requests for " + username + " ---");
         if (pendingTradeOffers.isEmpty()) {
             System.out.println("No pending offers.");
             return;
         }
         for (TradeOffer offer : pendingTradeOffers.values()) {
             System.out.println("ID: " + offer.id + " | From: " + offer.offeringPlayerUsername + " | Type: " + offer.type);
             System.out.println("    Offer/Request: " + offer.amount + "x " + offer.itemName);
             if (offer.price > 0) {
                 System.out.println("    For: " + offer.price + "g");
             } else if (offer.targetItemName != null) {
                 System.out.println("    For: " + offer.targetAmount + "x " + offer.targetItemName);
             }
             System.out.println("---");
         }
         System.out.println("-------------------------------------------------------------");
     }


    public void respondToTradeOffer(int tradeId, boolean accept) {
        if (!pendingTradeOffers.containsKey(tradeId)) {
            System.out.println("Error: No pending offer with ID " + tradeId + " found for you.");
            return;
        }

        TradeOffer offer = pendingTradeOffers.remove(tradeId);
        Player offeringPlayer = GameManager.findPlayer(offer.offeringPlayerUsername);

        if (offeringPlayer == null) {
             System.out.println("Error: Offering player '" + offer.offeringPlayerUsername + "' not found (maybe offline?). Trade failed.");
             offer.status = "Failed";
             tradeHistory.add(offer);
             GameManager.removePendingOfferGlobally(tradeId); // Clean up global list
             return;
        }

        if (!consumeEnergy(1)) { // Consume energy for responding
             pendingTradeOffers.put(tradeId, offer); // Put back if no energy
             return;
        }


        if (accept) {
            System.out.println(this.username + " accepted trade offer ID " + tradeId + " from " + offeringPlayer.username + ".");
            boolean success = executeTrade(offer, offeringPlayer);
            if (success) {
                offer.status = "Accepted";
                 increasePlayerFriendship(offeringPlayer.username, 50);
                 // offeringPlayer.increasePlayerFriendship(this.username, 50); // Handled by symmetry
                 System.out.println("Trade successful!");
            } else {
                offer.status = "Failed";
                 decreasePlayerFriendship(offeringPlayer.username, 30);
                 // offeringPlayer.decreasePlayerFriendship(this.username, 30); // Handled by symmetry
                 System.out.println("Trade failed (likely due to insufficient resources/money on either side).");
            }
        } else {
            System.out.println(this.username + " rejected trade offer ID " + tradeId + " from " + offeringPlayer.username + ".");
            offer.status = "Rejected";
             decreasePlayerFriendship(offeringPlayer.username, 30);
             // offeringPlayer.decreasePlayerFriendship(this.username, 30); // Handled by symmetry
        }

        tradeHistory.add(offer);
        GameManager.removePendingOfferGlobally(tradeId); // Clean up global list
        // Notify offering player (requires GameManager mechanism)
        // offeringPlayer.notifyTradeResult(offer);
    }

    private boolean executeTrade(TradeOffer offer, Player offeringPlayer) {
        Player receivingPlayer = this;
        Player itemGiver, itemReceiver, paymentGiver, paymentReceiver;

        if (offer.type.equalsIgnoreCase("offer")) {
            itemGiver = offeringPlayer;
            itemReceiver = receivingPlayer;
            paymentGiver = receivingPlayer;
            paymentReceiver = offeringPlayer;
        } else {
            itemGiver = receivingPlayer;
            itemReceiver = offeringPlayer;
            paymentGiver = offeringPlayer;
            paymentReceiver = receivingPlayer;
        }

        if (itemGiver.getItemCount(offer.itemName) < offer.amount) {
            System.out.println("Execution Error: " + itemGiver.username + " no longer has enough " + offer.itemName + ".");
            return false;
        }

        if (offer.price > 0) {
            if (paymentGiver.getMoney() < offer.price) {
                System.out.println("Execution Error: " + paymentGiver.username + " does not have enough money.");
                return false;
            }
        } else if (offer.targetItemName != null) {
            if (paymentGiver.getItemCount(offer.targetItemName) < offer.targetAmount) {
                System.out.println("Execution Error: " + paymentGiver.username + " does not have enough " + offer.targetItemName + ".");
                return false;
            }
        }


        Item itemToTransfer = null;
         for(Item item : itemGiver.inventory) {
             if (item.getName().equalsIgnoreCase(offer.itemName)) {
                 itemToTransfer = new Item(item.name, item.basePrice, item.edible, item.energyValue); // Create copy
                 itemToTransfer.setQuality(item.getQuality()); // Preserve quality
                 break;
             }
         }
         if (itemToTransfer == null) return false;

        if (!itemGiver.removeItem(offer.itemName, offer.amount)) return false;
        itemReceiver.addItem(itemToTransfer, offer.amount);

        if (offer.price > 0) {
            if (!paymentGiver.spendMoney(offer.price)) return false;
            paymentReceiver.addMoney(offer.price);
        } else if (offer.targetItemName != null) {
             Item paymentItemToTransfer = null;
             for(Item item : paymentGiver.inventory) {
                 if (item.getName().equalsIgnoreCase(offer.targetItemName)) {
                     paymentItemToTransfer = new Item(item.name, item.basePrice, item.edible, item.energyValue);
                     paymentItemToTransfer.setQuality(item.getQuality());
                     break;
                 }
             }
             if (paymentItemToTransfer == null) return false;

            if (!paymentGiver.removeItem(offer.targetItemName, offer.targetAmount)) return false;
            paymentReceiver.addItem(paymentItemToTransfer, offer.targetAmount);
        }

        return true;
    }


     public void showTradeHistory() {
         System.out.println("--- Trade History for " + username + " ---");
         if (tradeHistory.isEmpty()) {
             System.out.println("No trade history found.");
             return;
         }
         for (TradeOffer offer : tradeHistory) {
             String role = offer.offeringPlayerUsername.equals(this.username) ? "Offerer" : "Target";
             String otherParty = role.equals("Offerer") ? offer.targetPlayerUsername : offer.offeringPlayerUsername;
             System.out.println("ID: " + offer.id + " | Your Role: " + role + " | Other Party: " + otherParty + " | Status: " + offer.status);
             System.out.println("    Type: " + offer.type + " | Main Item: " + offer.amount + "x " + offer.itemName);
             if (offer.price > 0) {
                 System.out.println("    Payment: " + offer.price + "g");
             } else if (offer.targetItemName != null) {
                 System.out.println("    Payment: " + offer.targetAmount + "x " + offer.targetItemName);
             }
             System.out.println("---");
         }
         System.out.println("---------------------------------------");
     }


    public void purchaseFromShop(Shop shop, String productName, int count) {
        if (!shop.isOpen()) {
            System.out.println("Error: Shop '" + shop.name + "' is currently closed.");
            return;
        }
         if (!isNear(shop.location)) {
             System.out.println("Error: Must be near the shop '" + shop.name + "' to purchase.");
             return;
         }

        Shop.ShopItem shopItem = shop.findItem(productName);
        if (shopItem == null) {
            System.out.println("Error: Product '" + productName + "' not found in " + shop.name + ".");
            return;
        }

        if (shopItem.dailyStock < count) {
            System.out.println("Error: Not enough daily stock for '" + productName + "' (Available: " + shopItem.dailyStock + ").");
            return;
        }

        int totalCost = shopItem.price * count;
        if (!spendMoney(totalCost)) {
            return;
        }

        if (!consumeEnergy(1)) { // Consume energy for purchasing
             addMoney(totalCost); // Refund if no energy
             return;
        }


        shopItem.dailyStock -= count;

        Item purchasedItem = new Item(shopItem.item.getName(), shopItem.item.basePrice, shopItem.item.edible, shopItem.item.energyValue);
        addItem(purchasedItem, count);

        System.out.println(username + " purchased " + count + "x " + productName + " for " + totalCost + "g from " + shop.name + ".");
    }

    public void sellItemViaShippingBin(String itemName, int count, ShippingBin bin) {
         if (!isNear(bin.location)) {
             System.out.println("Error: Must be near the Shipping Bin to sell.");
             return;
         }

        int availableCount = 0;
        List<Item> itemsToSell = new ArrayList<>();
        List<Item> remainingInventory = new ArrayList<>();
        int foundCount = 0;

        // Iterate backwards to safely remove while iterating
        for (int i = inventory.size() - 1; i >= 0; i--) {
            Item item = inventory.get(i);
            if (item.getName().equalsIgnoreCase(itemName) && foundCount < count) {
                itemsToSell.add(item);
                foundCount++;
            } else {
                 // This logic is flawed for removal, let's rethink
            }
        }
         // Simpler approach: find items first, then remove
         itemsToSell.clear();
         List<Item> itemsToRemoveFromInv = new ArrayList<>();
         for(Item item : inventory) {
             if(item.getName().equalsIgnoreCase(itemName) && itemsToSell.size() < count) {
                 itemsToSell.add(item);
                 itemsToRemoveFromInv.add(item); // Mark for removal
             }
         }
         availableCount = itemsToSell.size();


        if (availableCount == 0) {
            System.out.println("Error: You don't have any '" + itemName + "' to sell.");
            return;
        }

         if (count > availableCount) {
              System.out.println("Error: You only have " + availableCount + "x '" + itemName + "' to sell (requested " + count + "). Selling " + availableCount + " instead.");
              // Adjust count to what's available
              count = availableCount;
         } else {
              // If count <= availableCount, we only need to remove 'count' items.
              // The itemsToSell list currently has 'availableCount' items. We need to trim it.
              itemsToSell = itemsToSell.subList(0, count);
              itemsToRemoveFromInv = itemsToRemoveFromInv.subList(0, count);
         }


         if (!consumeEnergy(2 * count)) { // Consume energy per item sold
              return;
         }


         inventory.removeAll(itemsToRemoveFromInv);


         bin.addItems(this.username, itemsToSell);

         System.out.println(username + " placed " + count + "x " + itemName + " in the Shipping Bin.");
         System.out.println("Payment will be received tomorrow morning.");
    }


    public boolean isNear(Location otherLocation) {
        if (currentLocation == null || otherLocation == null) return false;
        int dx = Math.abs(currentLocation.x - otherLocation.x);
        int dy = Math.abs(currentLocation.y - otherLocation.y);
        return dx <= 1 && dy <= 1 && !(dx == 0 && dy == 0);
    }

    @Override
    public String toString() {
        return "Player{" +
               "username='" + username + '\'' +
               ", money=" + getMoney() + "g" +
               ", energy=" + energy + "/" + maxEnergy +
               ", spouse=" + (spouse != null ? spouse.username : "None") +
               '}';
    }
}

class Location {
    int x;
    int y;

    public Location(int x, int y) {
        this.x = x;
        this.y = y;
    }
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
         Location location = (Location) o;
         return x == location.x && y == location.y;
     }

     @Override
     public int hashCode() {
         return 31 * x + y;
     }
}

class GiftInteraction {
    int id;
    String senderUsername;
    String receiverUsername;
    Item item;
    int amount;
    int rating;

    public GiftInteraction(int id, String sender, String receiver, Item item, int amount) {
        this.id = id;
        this.senderUsername = sender;
        this.receiverUsername = receiver;
        this.item = item;
        this.amount = amount;
        this.rating = 0;
    }
}


class Quest {
    int id;
    String giverNpcName;
    String description;
    Map<String, Integer> requiredItems;
    String rewardDescription;
    Object reward;
    int requiredFriendshipLevel;
    String activationCondition;
    boolean isCompletedGlobally = false;
    String completedByPlayer = null;

    transient int displayIndex = -1;


    private static int nextId = 1;

    public Quest(String giverNpcName, String description, Map<String, Integer> requiredItems, String rewardDescription, Object reward, int requiredFriendshipLevel, String activationCondition) {
        this.id = nextId++;
        this.giverNpcName = giverNpcName;
        this.description = description;
        this.requiredItems = requiredItems;
        this.rewardDescription = rewardDescription;
        this.reward = reward;
        this.requiredFriendshipLevel = requiredFriendshipLevel;
        this.activationCondition = activationCondition;
    }

    public void grantReward(Player player, NPC npc) {
         if (isCompletedGlobally) {
             System.out.println("Reward for quest '" + description + "' already claimed by " + completedByPlayer + ".");
             return;
         }

        int npcFriendshipLevel = player.getNPCFriendshipLevel(npc.name);
        double rewardMultiplier = (npcFriendshipLevel >= 2) ? 2.0 : 1.0;

        if (reward instanceof Integer) {
            int moneyReward = (int) ((Integer) reward * rewardMultiplier);
            player.addMoney(moneyReward);
        } else if (reward instanceof Item) {
             int amount = (int) Math.round(1 * rewardMultiplier);
             if (amount > 0) player.addItem((Item) reward, amount);
        } else if (reward instanceof String && reward.equals("FriendshipLevel")) {
             int currentPoints = player.npcFriendshipPoints.getOrDefault(npc.name, 0);
             int currentLevel = player.getNPCFriendshipLevel(npc.name);
             int pointsForNextLevel = (currentLevel + 1) * 200;
             int pointsToAdd = Math.max(0, pointsForNextLevel - currentPoints);
             if (pointsToAdd > 0) {
                 player.increaseNPCFriendship(npc.name, pointsToAdd);
             } else {
                  player.increaseNPCFriendship(npc.name, 50); // Give some points even if level doesn't increase
             }
        } else if (reward instanceof List) {
             for(Object r : (List<?>)reward) {
                 if (r instanceof Item) {
                     int amount = (int) Math.round(1 * rewardMultiplier);
                      // Ensure rewardMultiplier logic makes sense for item lists (e.g., double amount?)
                      // For simplicity, let's just give the base amount regardless of multiplier here.
                     player.addItem((Item)r, 1); // Give 1 of each item in the list
                 }
             }
        } else if (reward instanceof String && ((String)reward).startsWith("Recipe:")) {
             String recipeName = ((String) reward).split(":")[1];
             System.out.println("Learned recipe: " + recipeName);
             // player.learnRecipe(recipeName); // Needs cooking system implementation
        }


         this.isCompletedGlobally = true;
         this.completedByPlayer = player.username;
    }
}

class NPC {
    String name;
    String job;
    Location currentLocation;
    List<String> dialogues;
    List<String> favoriteItems;
    List<Quest> availableQuests;
    Map<Integer, String> completedQuestsTracker;

    public NPC(String name, String job, Location location, List<String> dialogues, List<String> favorites) {
        this.name = name;
        this.job = job;
        this.currentLocation = location;
        this.dialogues = dialogues;
        this.favoriteItems = favorites;
        this.availableQuests = new ArrayList<>();
        this.completedQuestsTracker = new HashMap<>();
    }

    public void addQuest(Quest quest) {
        this.availableQuests.add(quest);
    }

    public String getRandomDialogue() {
        if (dialogues == null || dialogues.isEmpty()) {
            return "...";
        }
        Random rand = new Random();
        return dialogues.get(rand.nextInt(dialogues.size()));
    }


    public void receiveGift(Player giver, Item item) {
        int friendshipIncrease = 50;

        if (favoriteItems.stream().anyMatch(fav -> fav.equalsIgnoreCase(item.getName()))) {
            friendshipIncrease = 200;
            System.out.println(name + " loves this gift!");
        } else {
            System.out.println(name + ": \"Thanks, this is nice.\"");
        }

         giver.increaseNPCFriendship(this.name, friendshipIncrease);
    }

     public void markQuestAsCompletedForPlayer(int questId, String username) {
         if (!completedQuestsTracker.containsKey(questId)) {
             completedQuestsTracker.put(questId, username);
         }
         for (Quest q : availableQuests) {
             if (q.id == questId) {
                 q.isCompletedGlobally = true;
                 q.completedByPlayer = username;
                 break;
             }
         }
     }

     public boolean isQuestCompletedGlobally(int questId) {
         return completedQuestsTracker.containsKey(questId);
     }

}

class Shop {
    String name;
    String ownerName;
    Location location;
    int openHour;
    int closeHour;
    List<ShopItem> inventory;

    static class ShopItem {
        Item item;
        int price;
        int initialDailyStock;
        int dailyStock;

        public ShopItem(Item item, int price, int dailyStock) {
            this.item = item;
            this.price = price;
            this.initialDailyStock = dailyStock;
            this.dailyStock = dailyStock;
        }
    }

    public Shop(String name, String ownerName, Location location, int openHour, int closeHour) {
        this.name = name;
        this.ownerName = ownerName;
        this.location = location;
        this.openHour = openHour;
        this.closeHour = closeHour;
        this.inventory = new ArrayList<>();
    }

    public void addItemToInventory(Item item, int price, int dailyStock) {
        inventory.add(new ShopItem(item, price, dailyStock));
    }

    public void resetDailyStock() {
        for (ShopItem si : inventory) {
            si.dailyStock = si.initialDailyStock;
        }
        System.out.println("Daily stock reset for shop: " + name);
    }

    public boolean isOpen() {
        int currentHour = GameManager.getCurrentHour();
        return currentHour >= openHour && currentHour < closeHour;
    }

    public ShopItem findItem(String productName) {
        for (ShopItem si : inventory) {
            if (si.item.getName().equalsIgnoreCase(productName)) {
                return si;
            }
        }
        return null;
    }

    public void showAllProducts() {
        System.out.println("--- Products at " + name + " (Owner: " + ownerName + ") ---");
         System.out.println("Open: " + openHour + ":00 - " + closeHour + ":00 | Currently: " + (isOpen() ? "Open" : "Closed"));
        for (ShopItem si : inventory) {
            String stockInfo = (si.initialDailyStock == Integer.MAX_VALUE) ? "Unlimited" : si.dailyStock + "/" + si.initialDailyStock;
            System.out.println("- " + si.item.getName() + " | Price: " + si.price + "g | Stock: " + stockInfo);
        }
        System.out.println("---------------------------------------");
    }

    public void showAvailableProducts() {
        System.out.println("--- Available Products at " + name + " ---");
         if (!isOpen()) {
             System.out.println("Shop is currently closed.");
             System.out.println("---------------------------------------");
             return;
         }
        boolean found = false;
        for (ShopItem si : inventory) {
            if (si.dailyStock > 0) {
                 String stockInfo = (si.initialDailyStock == Integer.MAX_VALUE) ? "Unlimited" : String.valueOf(si.dailyStock);
                System.out.println("- " + si.item.getName() + " | Price: " + si.price + "g | Stock: " + stockInfo);
                found = true;
            }
        }
        if (!found) {
            System.out.println("All limited stock items sold out for today.");
        }
        System.out.println("---------------------------------------");
    }
}

class ShippingBin {
    Location location;
    Map<String, List<Item>> itemsByPlayer; // Player Username -> List of items

    public ShippingBin(Location location) {
        this.location = location;
        this.itemsByPlayer = new HashMap<>();
    }

    public void addItems(String playerName, List<Item> items) {
        itemsByPlayer.computeIfAbsent(playerName, k -> new ArrayList<>()).addAll(items);
    }

    public Map<String, Integer> processSales() {
        Map<String, Integer> playerEarnings = new HashMap<>();
        if (itemsByPlayer.isEmpty()) {
            return playerEarnings;
        }

        System.out.println("--- Processing Shipping Bin Sales ---");
        for (Map.Entry<String, List<Item>> entry : itemsByPlayer.entrySet()) {
            String playerName = entry.getKey();
            List<Item> items = entry.getValue();
            int totalValue = 0;
            for (Item item : items) {
                int sellPrice = item.getSellPrice();
                totalValue += sellPrice;
                System.out.println("Sold " + item + " for " + sellPrice + "g (Player: " + playerName + ")");
            }
            if (totalValue > 0) {
                playerEarnings.put(playerName, totalValue);
            }
        }
        System.out.println("-------------------------------------");

        itemsByPlayer.clear();
        return playerEarnings;
    }
}


class TradeOffer {
    int id;
    String offeringPlayerUsername;
    String targetPlayerUsername;
    String type;
    String itemName;
    int amount;
    int price;
    String targetItemName;
    int targetAmount;
    String status;

    public TradeOffer(int id, String offeringPlayer, String targetPlayer, String type, String itemName, int amount, int price, String targetItemName, int targetAmount) {
        this.id = id;
        this.offeringPlayerUsername = offeringPlayer;
        this.targetPlayerUsername = targetPlayer;
        this.type = type;
        this.itemName = itemName;
        this.amount = amount;
        this.price = price;
        this.targetItemName = targetItemName;
        this.targetAmount = targetAmount;
        this.status = "Pending";
    }
}

// Class to hold pending marriage proposals
class MarriageProposal {
    Player proposer;
    Player target;
    String ringName;

    public MarriageProposal(Player proposer, Player target, String ringName) {
        this.proposer = proposer;
        this.target = target;
        this.ringName = ringName;
    }
}


class GameManager {
    static List<Player> players = new ArrayList<>();
    static List<NPC> npcs = new ArrayList<>();
    static List<Shop> shops = new ArrayList<>();
    static List<ShippingBin> shippingBins = new ArrayList<>();
    static Map<Integer, TradeOffer> allPendingTradeOffers = new HashMap<>(); // Global list: ID -> Offer
     static List<MarriageProposal> pendingProposals = new ArrayList<>();


    static int currentHour = 9;
    static int currentDay = 1;
    static String currentSeason = "Spring"; // Example

    public static int getCurrentHour() { return currentHour; }
    public static int getCurrentDay() { return currentDay; }
    public static String getCurrentSeason() { return currentSeason; }


     public static Player findPlayer(String username) {
         for (Player p : players) {
             if (p.username.equalsIgnoreCase(username)) {
                 return p;
             }
         }
         return null;
     }

     public static NPC findNPC(String name) {
         for (NPC n : npcs) {
             if (n.name.equalsIgnoreCase(name)) {
                 return n;
             }
         }
         return null;
     }

     public static void addPendingTradeOffer(String targetUsername, TradeOffer offer) {
         allPendingTradeOffers.put(offer.id, offer);
         Player targetPlayer = findPlayer(targetUsername);
         if (targetPlayer != null) {
             targetPlayer.receiveTradeOffer(offer);
         } else {
              System.out.println("Warning: Target player " + targetUsername + " for trade offer " + offer.id + " not found.");
         }
     }

      public static List<TradeOffer> getPendingOffersForPlayer(String username) {
          List<TradeOffer> playerOffers = new ArrayList<>();
          for (TradeOffer offer : allPendingTradeOffers.values()) {
              if (offer.targetPlayerUsername.equals(username) && offer.status.equals("Pending")) {
                  playerOffers.add(offer);
              }
          }
          return playerOffers;
      }

      public static void removePendingOfferGlobally(int tradeId) {
           allPendingTradeOffers.remove(tradeId);
      }

       public static void addPendingMarriageProposal(Player target, Player proposer, String ringName) {
           // Check if a proposal already exists between them
           for (MarriageProposal p : pendingProposals) {
               if ((p.proposer == proposer && p.target == target) || (p.proposer == target && p.target == proposer)) {
                   System.out.println("A marriage proposal already exists between " + proposer.username + " and " + target.username);
                   return;
               }
           }
           pendingProposals.add(new MarriageProposal(proposer, target, ringName));
           target.receiveMarriageProposal(proposer, ringName); // Notify target player
       }

       public static MarriageProposal findPendingProposal(Player target, Player proposer) {
           for (MarriageProposal p : pendingProposals) {
               if (p.proposer == proposer && p.target == target) {
                   return p;
               }
           }
           return null;
       }

       public static void removePendingProposal(Player target, Player proposer) {
           pendingProposals.removeIf(p -> p.proposer == proposer && p.target == target);
       }


    public static void advanceDay() {
         currentDay++;
         // Season change logic needed here (e.g., if currentDay > 28)
         currentHour = 6; // Start day at 6 AM
         System.out.println("\n--- Starting Day " + currentDay + " of " + currentSeason + " ---");

         for (ShippingBin bin : shippingBins) {
             Map<String, Integer> earnings = bin.processSales();
             for (Map.Entry<String, Integer> entry : earnings.entrySet()) {
                 Player player = findPlayer(entry.getKey());
                 if (player != null) {
                     player.addMoney(entry.getValue());
                 }
             }
         }

         for (Shop shop : shops) {
             shop.resetDailyStock();
         }

         for (Player p : players) {
             p.restoreFullEnergy();
         }

         // NPC daily schedules, foraging respawn, crop growth etc. would go here
         System.out.println("--------------------------------");
    }

    public static void advanceTime(int hours) {
        int nextHour = currentHour + hours;
        if (nextHour >= 24) { // Assuming day ends at midnight (or earlier based on game rules)
             advanceDay();
        } else {
             currentHour = nextHour;
             System.out.println("Time advanced to " + currentHour + ":00.");
        }
         // Check for shop open/close times based on new hour
    }


    public static void initializeNPCsAndQuests() {
        Item ironOre = new Item("Iron Ore", 10, false, 0);
        Item diamond = new Item("Diamond", 750, false, 0);
        Item pumpkinPie = new Item("Pumpkin Pie", 385, true, 175);
        Item stone = new Item("Stone", 2, false, 0);
        Item quartz = new Item("Quartz", 25, false, 0);
        Item goldBar = new Item("Gold Bar", 250, false, 0);
        Item coffee = new Item("Coffee", 150, true, 3);
        Item pumpkin = new Item("Pumpkin", 320, true, 0);
        Item wheat = new Item("Wheat", 25, false, 0);
        Item iridiumSprinkler = new Item("Iridium Sprinkler", 1000, false, 0);
        Item wine = new Item("Wine", 400, true, 50);
        Item pickles = new Item("Pickles", 100, true, 63);
        Item salmon = new Item("Salmon", 75, true, 38);
        Item salad = new Item("Salad", 110, true, 113);
        Item hardwood = new Item("Hardwood", 15, false, 0);
        Item wood = new Item("Wood", 2, false, 0);
        Item deluxeScarecrow = new Item("Deluxe Scarecrow", 0, false, 0);
        Item beeHouse = new Item("Bee House", 0, false, 0);
        Item ironBar = new Item("Iron Bar", 120, false, 0);
        Item spaghetti = new Item("Spaghetti", 120, true, 75);
        Item wool = new Item("Wool", 340, false, 0);
        Item pizza = new Item("Pizza", 300, true, 150);
        Item amethyst = new Item("Amethyst", 100, false, 0);
        Item blackberryCobbler = new Item("Blackberry Cobbler", 260, true, 175);
        Item grape = new Item("Grape", 80, true, 38);
        Item goatCheese = new Item("Goat Cheese", 400, true, 125);
        Item poppyseedMuffin = new Item("Poppyseed Muffin", 250, true, 150);
        Item stirFry = new Item("Stir Fry", 335, true, 200);
        Item peach = new Item("Peach", 140, true, 63);
        Item obsidian = new Item("Obsidian", 200, false, 0);
        Item voidEgg = new Item("Void Egg", 65, true, -75); // Note: Negative energy!


        NPC sebastian = new NPC("Sebastian", "Programmer", new Location(15, 25),
                List.of("...", "I'm busy.", "Don't bother me."),
                List.of(wool.name, pumpkinPie.name, pizza.name, obsidian.name, voidEgg.name));
        npcs.add(sebastian);

        Map<String, Integer> req1_1 = new HashMap<>(); req1_1.put(ironOre.name, 50);
        Quest q1_1 = new Quest(sebastian.name, "Deliver 50 Iron Ore", req1_1, "2 Diamonds", List.of(diamond, diamond), 0, "immediate");
        sebastian.addQuest(q1_1);

        Map<String, Integer> req1_2 = new HashMap<>(); req1_2.put(pumpkinPie.name, 1);
        Quest q1_2 = new Quest(sebastian.name, "Deliver a Pumpkin Pie", req1_2, "5,000g", 5000, 1, "immediate");
        sebastian.addQuest(q1_2);

        Map<String, Integer> req1_3 = new HashMap<>(); req1_3.put(stone.name, 150);
        List<Item> quartzReward = new ArrayList<>(); for(int i=0; i<50; i++) quartzReward.add(quartz);
        Quest q1_3 = new Quest(sebastian.name, "Deliver 150 Stone", req1_3, "50 Quartz", quartzReward, 0, "time:28");
        sebastian.addQuest(q1_3);


        NPC abigail = new NPC("Abigail", "Adventurer", new Location(20, 30),
                List.of("How's the weather?", "Looking for weird stuff.", "The mines are cool."),
                List.of(stone.name, ironOre.name, coffee.name, amethyst.name, blackberryCobbler.name, pumpkin.name));
        npcs.add(abigail);

         Map<String, Integer> req2_1 = new HashMap<>(); req2_1.put(goldBar.name, 1);
         Quest q2_1 = new Quest(abigail.name, "Deliver a Gold Bar", req2_1, "+1 Friendship Level", "FriendshipLevel", 0, "immediate");
         abigail.addQuest(q2_1);

         Map<String, Integer> req2_2 = new HashMap<>(); req2_2.put(pumpkin.name, 1);
         Quest q2_2 = new Quest(abigail.name, "Deliver a Pumpkin", req2_2, "500g", 500, 1, "immediate");
         abigail.addQuest(q2_2);

         Map<String, Integer> req2_3 = new HashMap<>(); req2_3.put(wheat.name, 50);
         Quest q2_3 = new Quest(abigail.name, "Deliver 50 Wheat", req2_3, "1 Iridium Sprinkler", iridiumSprinkler, 0, "time:40");
         abigail.addQuest(q2_3);

         NPC harvey = new NPC("Harvey", "Doctor", new Location(25, 20),
                 List.of("Health is important.", "Don't stress.", "Remember your vitamins."),
                 List.of(coffee.name, pickles.name, wine.name, "Super Meal", "Truffle Oil"));
         npcs.add(harvey);

         Map<String, Integer> req3_1 = new HashMap<>();
         req3_1.put(wheat.name, 12);
         Quest q3_1 = new Quest(harvey.name, "Deliver 12 Wheat", req3_1, "750g", 750, 0, "immediate");
         harvey.addQuest(q3_1);

         Map<String, Integer> req3_2 = new HashMap<>(); req3_2.put(salmon.name, 1);
         Quest q3_2 = new Quest(harvey.name, "Deliver a Salmon", req3_2, "+1 Friendship Level", "FriendshipLevel", 1, "immediate");
         harvey.addQuest(q3_2);

         Map<String, Integer> req3_3 = new HashMap<>(); req3_3.put(wine.name, 1);
         List<Item> saladReward = new ArrayList<>(); for(int i=0; i<5; i++) saladReward.add(salad);
         Quest q3_3 = new Quest(harvey.name, "Deliver a bottle of Wine", req3_3, "5 Salads", saladReward, 0, "time:35");
         harvey.addQuest(q3_3);


         NPC leah = new NPC("Leah", "Artist", new Location(30, 35),
                 List.of("Nature inspires me.", "Looking for wood.", "I love sculpting."),
                 List.of(salad.name, grape.name, wine.name, goatCheese.name, poppyseedMuffin.name, stirFry.name));
         npcs.add(leah);

         Map<String, Integer> req4_1 = new HashMap<>(); req4_1.put(hardwood.name, 10);
         Quest q4_1 = new Quest(leah.name, "Deliver 10 Hardwood", req4_1, "500g", 500, 0, "immediate");
         leah.addQuest(q4_1);

         Object salmonDinnerRecipe = "Recipe:Salmon Dinner";
         Map<String, Integer> req4_2 = new HashMap<>(); req4_2.put(salmon.name, 1);
         Quest q4_2 = new Quest(leah.name, "Deliver a Salmon", req4_2, "Salmon Dinner Recipe", salmonDinnerRecipe, 1, "immediate");
         leah.addQuest(q4_2);

         Map<String, Integer> req4_3 = new HashMap<>(); req4_3.put(wood.name, 200);
         List<Item> scarecrowReward = List.of(deluxeScarecrow, deluxeScarecrow, deluxeScarecrow);
         Quest q4_3 = new Quest(leah.name, "Deliver 200 Wood", req4_3, "3 Deluxe Scarecrows", scarecrowReward, 0, "time:56");
         leah.addQuest(q4_3);

         NPC robin = new NPC("Robin", "Carpenter", new Location(10, 15),
                 List.of("Building something?", "Need wood?", "So much work!"),
                 List.of(spaghetti.name, wood.name, ironBar.name, peach.name, hardwood.name));
         npcs.add(robin);

         Map<String, Integer> req5_1 = new HashMap<>(); req5_1.put(wood.name, 80);
         Quest q5_1 = new Quest(robin.name, "Deliver 80 Wood", req5_1, "1,000g", 1000, 0, "immediate");
         robin.addQuest(q5_1);

         Map<String, Integer> req5_2 = new HashMap<>(); req5_2.put(ironBar.name, 10);
          List<Item> beeHouseReward = List.of(beeHouse, beeHouse, beeHouse);
         Quest q5_2 = new Quest(robin.name, "Deliver 10 Iron Bars", req5_2, "3 Bee Houses", beeHouseReward, 1, "immediate");
         robin.addQuest(q5_2);

         Map<String, Integer> req5_3 = new HashMap<>(); req5_3.put(wood.name, 1000);
         Quest q5_3 = new Quest(robin.name, "Deliver 1000 Wood", req5_3, "25,000g", 25000, 0, "time:84");
         robin.addQuest(q5_3);
    }


    public static void main(String[] args) {
        Location startLoc = new Location(0, 0);
        Location shopLoc = new Location(5, 5);
        Location npcLocAbigail = new Location(20, 30);
        Location binLoc = new Location(0, 1);

        Player player1 = new Player("Alice", "Ali_Gamer", "Female", 500, startLoc);
        Player player2 = new Player("Bob", "Bob_Pro", "Male", 500, startLoc);
        players.add(player1);
        players.add(player2);

        initializeNPCsAndQuests();

        Item wheatSeed = new Item("Wheat Seed", 10, false, 0);
        Item bouquet = new Item("Bouquet", 100, false, 0);
        Item mermaidPendant = new Item("Mermaid's Pendant", 5000, false, 0);
        Item diamond = new Item("Diamond", 750, false, 0);
        Item wood = new Item("Wood", 2, false, 0);
        Item stone = new Item("Stone", 2, false, 0);
        Item goldBar = new Item("Gold Bar", 250, false, 0);
        Item pumpkin = new Item("Pumpkin", 320, true, 0);


        Shop pierres = new Shop("Pierre's General Store", "Pierre", shopLoc, 9, 17);
        pierres.addItemToInventory(wheatSeed, 10, 50);
        pierres.addItemToInventory(bouquet, 100, 5);
         pierres.addItemToInventory(mermaidPendant, 5000, 1);
        shops.add(pierres);

        ShippingBin mainBin = new ShippingBin(binLoc);
        shippingBins.add(mainBin);


        System.out.println("--- Game Start ---");
        System.out.println(player1);
        System.out.println(player2);

        player1.addItem(wood, 20);
        player1.addItem(stone, 10);
        player1.addItem(bouquet, 1);
        player1.addItem(mermaidPendant, 1);
        player1.addItem(diamond, 2);
        player1.addItem(pumpkin, 1);
        player1.addItem(goldBar, 1);


        System.out.println("\n--- NPC Interaction Example ---");
        NPC abigail = findNPC("Abigail");
        if (abigail != null) {
            player1.currentLocation = new Location(19, 30);
            player1.meetNPC(abigail);
            player1.listAvailableQuests(abigail);

            Quest goldQuest = null;
            for(Quest q : abigail.availableQuests) if(q.description.contains("Gold Bar")) goldQuest = q;
            if(goldQuest != null) player1.acceptQuest(goldQuest);

            player1.listAvailableQuests(abigail); // Show active quest

            player1.finishQuest(1);
            player1.showNPCFriendships();

            player1.sendGiftToNPC(abigail, pumpkin.name);
            player1.showNPCFriendships();
        }
        System.out.println(player1);


        System.out.println("\n--- Shop Example ---");
        player1.currentLocation = new Location(5, 6);
        pierres.showAvailableProducts();
        player1.purchaseFromShop(pierres, wheatSeed.name, 5);
        pierres.showAvailableProducts();
        System.out.println(player1);
        System.out.println("Inventory: " + player1.inventory);


        System.out.println("\n--- Selling Example ---");
        player1.currentLocation = new Location(0, 0);
        player1.sellItemViaShippingBin(wood.name, 10, mainBin);
        System.out.println("Inventory: " + player1.inventory);


        System.out.println("\n--- Advancing Day ---");
        advanceDay(); // Process sales, reset energy/stock
        System.out.println(player1);


        System.out.println("\n--- Player Interaction Example ---");
        player1.currentLocation = new Location(0, 0);
        player2.currentLocation = new Location(1, 0);

        player1.talkToPlayer(player2, "Hello Bob!");
        System.out.println("Alice's friendship points with Bob: " + player1.playerFriendshipPoints.getOrDefault(player2.username, 0));

        player1.increasePlayerFriendship(player2.username, 100); // Reach level 1
        player1.sendGiftToPlayer(player2, diamond.name, 1);
        player2.showPendingGifts();
        player2.rateGift(1, 5);
        System.out.println("Alice's friendship points with Bob: " + player1.playerFriendshipPoints.getOrDefault(player2.username, 0));
        player1.showGiftHistory(player2.username);

        player1.increasePlayerFriendship(player2.username, 500); // Reach level 2
        player1.hugPlayer(player2);
        System.out.println("Alice's friendship points with Bob: " + player1.playerFriendshipPoints.getOrDefault(player2.username, 0));

        player1.increasePlayerFriendship(player2.username, 1000); // Reach points for level 3
        player1.giveFlower(player2);
        System.out.println("Alice's friendship points with Bob: " + player1.playerFriendshipPoints.getOrDefault(player2.username, 0));
        System.out.println("Alice's friendship level with Bob: " + player1.getPlayerFriendshipLevel(player2.username));


        System.out.println("\n--- Marriage Example ---");
        player1.increasePlayerFriendship(player2.username, 1000); // Reach points for level 4
        System.out.println("Alice's friendship points with Bob: " + player1.playerFriendshipPoints.getOrDefault(player2.username, 0));

        // Player 2 (Bob) proposes to Player 1 (Alice)
        player2.addItem(mermaidPendant, 1); // Give Bob the pendant
        player2.proposeMarriage(player1, mermaidPendant.name);
        player1.respondToProposal(player2, true); // Alice accepts

        System.out.println("Alice's status: " + player1);
        System.out.println("Bob's status: " + player2);
        System.out.println("Shared money: " + player1.getMoney());

        player1.purchaseFromShop(pierres, wheatSeed.name, 2); // Test shared money
        System.out.println("Shared money after Alice buys: " + player1.getMoney());
        System.out.println(player1);
        System.out.println(player2);


        System.out.println("\n--- Player Trade Example ---");
        player1.startPlayerTrade();
        player1.createTradeOffer(player2.username, "offer", diamond.name, 1, 1000, null, 0);
        player2.listPendingTradeOffers();
        player2.respondToTradeOffer(1, true);
        player1.showTradeHistory();
        player2.showTradeHistory();
        System.out.println("Alice: " + player1);
        System.out.println("Bob: " + player2);
        System.out.println("Alice's Diamonds: " + player1.getItemCount(diamond.name));
        System.out.println("Bob's Diamonds: " + player2.getItemCount(diamond.name));

        System.out.println("\n--- Energy Example ---");
        player1.consumeEnergy(190);
        player1.eat(pumpkinPie.name); // Assuming player1 has pumpkin pie
        player1.consumeEnergy(50); // Should pass out if not enough energy
        System.out.println(player1);
        advanceDay();
        System.out.println(player1); // Should have 75% energy if passed out


    }
}

