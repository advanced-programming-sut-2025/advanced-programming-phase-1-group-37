package com.common;

import com.esotericsoftware.kryo.Kryo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared protocol & models between client and server.
 * NOTE: Registration order must match on client and server.
 */
public final class Network {
    public static final int TCP_PORT = 54555;
    public static final int UDP_PORT = 54777;

    public static void register(Kryo kryo) {
        // Collections
        kryo.register(ArrayList.class);
        kryo.register(HashMap.class);
        kryo.register(ConcurrentHashMap.class);

        // Enums
        kryo.register(Weather.class);
        kryo.register(InteractionType.class);

        // Models
        kryo.register(PlayerInfo.class);
        kryo.register(LobbyInfo.class);
        kryo.register(PlayerPosition.class);
        kryo.register(NPCPosition.class);
        kryo.register(WorldItem.class);
        kryo.register(ScoreEntry.class);

        // Auth
        kryo.register(LoginRequest.class);
        kryo.register(LoginResponse.class);
        kryo.register(SignupRequest.class);
        kryo.register(SignupResponse.class);
        kryo.register(DCReconnectRequest.class);

        // Lobby
        kryo.register(LobbyCreateRequest.class);
        kryo.register(LobbyJoinRequest.class);
        kryo.register(LobbyJoinResponse.class);
        kryo.register(LobbyLeaveRequest.class);
        kryo.register(LobbyListRequest.class);
        kryo.register(LobbyListResponse.class);
        kryo.register(LobbyStartGameRequest.class);
        kryo.register(LobbyUpdate.class);
        kryo.register(GameStartedNotification.class);

        // Online List
        kryo.register(OnlineListRequest.class);
        kryo.register(OnlineListUpdate.class);
        kryo.register(OnlineListUpdate.OnlineEntry.class);

        // Gameplay
        kryo.register(MapUpdate.class);
        kryo.register(PlayerMove.class);
        kryo.register(PlayerReaction.class);
        kryo.register(PlayerInteraction.class);

        // Social / Vote / Scoreboard
        kryo.register(ChatMessage.class);
        kryo.register(TagToast.class);
        kryo.register(VoteStart.class);
        kryo.register(VoteCast.class);
        kryo.register(VoteUpdate.class);
        kryo.register(VoteResult.class);
        kryo.register(ScoreboardUpdate.class);

        // Shop
        kryo.register(ShopStockRequest.class);
        kryo.register(ShopStockUpdate.class);
        kryo.register(ShopBuyRequest.class);
        kryo.register(ShopBuyResponse.class);

        // Day/Weather
        kryo.register(NextDayRequest.class);
        kryo.register(NextDayBroadcast.class);

        // Trades + Inventory
        kryo.register(TradeOffer.class);
        kryo.register(TradeListRequest.class);
        kryo.register(TradeListResponse.class);
        kryo.register(TradeDecision.class);
        kryo.register(TradeUpdate.class);
        kryo.register(InventoryUpdate.class);

        // Missions
        kryo.register(MissionListRequest.class);
        kryo.register(MissionDef.class);
        kryo.register(MissionState.class);
        kryo.register(MissionJoinLeave.class);
        kryo.register(MissionUpdate.class);

        // General
        kryo.register(PopupNotification.class);

        // --- NEW: Emotes / Trade History / Save-Load ---
        kryo.register(EmoteRequest.class);

        kryo.register(TradeRecord.class);
        kryo.register(TradeHistoryRequest.class);
        kryo.register(TradeHistoryResponse.class);

        kryo.register(SaveGameRequest.class);
        kryo.register(SaveGameResponse.class);
        kryo.register(LoadGameRequest.class);
        kryo.register(LoadGameResponse.class);
    }

    // ---------- Enums ----------
    public enum Weather { SUNNY, RAIN, STORM, SNOW }
    public enum InteractionType { HUG, GIFT, MARRY }

    // ---------- Models ----------
    public static class PlayerInfo {
        public String username, nickname;
        public int money = 500, missionsCompleted, skillLevelSum;
        public float energy = 100f;
        public int lobbyId = -1;
        public Map<String,Integer> inventory;
        public PlayerInfo() {}
        public PlayerInfo(String u, String n) { this.username = u; this.nickname = n; }
    }

    public static class LobbyInfo {
        public int id;
        public String name;
        public boolean isPrivate;
        public boolean gameStarted;
        public int capacity = 4;
        public int playerCount;
    }

    public static class PlayerPosition {
        public String username;
        public float x, y;
        public PlayerPosition() {}
        public PlayerPosition(String u, float x, float y) { this.username = u; this.x = x; this.y = y; }
    }

    public static class NPCPosition {
        public String npcId;
        public float x, y;
        public NPCPosition() {}
        public NPCPosition(String id, float x, float y) { this.npcId = id; this.x = x; this.y = y; }
    }

    public static class WorldItem {
        public String id;   // e.g., "apple-7"
        public String type; // e.g., "Apple"
        public float x, y;
    }

    public static class ScoreEntry {
        public String username, nickname;
        public int money, missionsCompleted, skillLevelSum;
    }

    // ---------- Auth ----------
    public static class LoginRequest { public String username, passwordHash; }
    public static class LoginResponse { public boolean success; public String message; public String sessionId; public PlayerInfo player; }
    public static class SignupRequest { public String username, passwordHash, nickname; }
    public static class SignupResponse { public boolean success; public String message; }
    public static class DCReconnectRequest { public String sessionId; }

    // ---------- Lobby ----------
    public static class LobbyCreateRequest { public String name; public boolean isPrivate; public String password; public boolean isVisible; }
    public static class LobbyJoinRequest { public int lobbyId; public String password; }
    public static class LobbyJoinResponse { public boolean success; public String message; }
    public static class LobbyLeaveRequest {}
    public static class LobbyListRequest {}
    public static class LobbyListResponse { public List<LobbyInfo> lobbies; }
    public static class LobbyStartGameRequest {}
    public static class LobbyUpdate { public int lobbyId = -1; public String lobbyName; public List<PlayerInfo> players; }
    public static class GameStartedNotification { public MapUpdate initialMapState; }

    // ---------- Online List ----------
    public static class OnlineListRequest {}
    public static class OnlineListUpdate {
        public static class OnlineEntry {
            public String username, nickname, lobbyName;
            public int lobbyId;
            public boolean online;
        }
        public List<OnlineEntry> online;
    }

    // ---------- Gameplay ----------
    public static class MapUpdate {
        public List<PlayerPosition> positions;
        public List<NPCPosition> npcs;
        public List<WorldItem> items; // world pickups (e.g. apples)
        public long serverTimeMillis;
        public int hourOfDay;
        public Weather weather;
    }
    public static class PlayerMove { public float x, y; }
    public static class PlayerReaction { public String username; public String reactionText; }
    public static class PlayerInteraction { public String targetUsername; public InteractionType type; public String item; }

    // ---------- Social / Vote / Scoreboard ----------
    public static class ChatMessage { public String from, to, text; public boolean isPrivate; public boolean global; public List<String> mentions; }
    public static class TagToast { public String from; public String text; }
    public static class VoteStart { public String type, targetUsername, subject; }
    public static class VoteCast { public boolean vote; }
    public static class VoteUpdate { public Map<String, Boolean> votes; public long timeLeftMillis; public String type, targetUsername, subject; }
    public static class VoteResult { public String message; }
    public static class ScoreboardUpdate { public List<ScoreEntry> entries; }

    // ---------- Shop ----------
    public static class ShopStockRequest {}
    public static class ShopStockUpdate { public Map<String,Integer> stock; public Map<String,Integer> prices; public Map<String,Integer> myDailyBought; }
    public static class ShopBuyRequest { public String item; public int amount; }
    public static class ShopBuyResponse { public boolean success; public String message; }

    // ---------- Day/Weather ----------
    public static class NextDayRequest {}
    public static class NextDayBroadcast { public int newHourOfDay; public Weather newWeather; }

    // ---------- Trades + Inventory ----------
    public static class TradeOffer {
        public int id;
        public String from, to;
        public Map<String,Integer> bundle;
        public int price;
        public String note;
    }
    public static class TradeListRequest {}
    public static class TradeListResponse { public List<TradeOffer> pending; }
    public static class TradeDecision { public int id; public boolean accept; }
    public static class TradeUpdate { public String message; }
    public static class InventoryUpdate { public String username; public Map<String,Integer> inv; public int money; }

    // ---------- Missions ----------
    public static class MissionListRequest {}
    public static class MissionDef { public String id, title, description; public int capacity; public List<String> members; }
    public static class MissionState { public String id, title, progress; public List<String> members; }
    public static class MissionJoinLeave { public String missionId; public boolean join; }
    public static class MissionUpdate { public List<MissionDef> available; public List<MissionState> active; }

    // ---------- General Notifications ----------
    public static class PopupNotification {
        public String message;
        public PopupNotification() {}
        public PopupNotification(String message) { this.message = message; }
    }

    // ---------- NEW Packets ----------

    /** Client -> Server: send an emote/reaction (emoji or short text). */
    public static class EmoteRequest { public String text; }

    /** Record of a completed (accepted) trade. */
    public static class TradeRecord implements java.io.Serializable {
        public int id;
        public String from, to;
        public Map<String,Integer> bundle;
        public int price;
        public long at;
        public boolean accepted;
    }
    public static class TradeHistoryRequest {}
    public static class TradeHistoryResponse { public List<TradeRecord> records; }

    // Save/Load
    public static class SaveGameRequest {}
    public static class SaveGameResponse { public boolean success; public String message; }
    public static class LoadGameRequest {}
    public static class LoadGameResponse { public boolean success; public String message; }
}
