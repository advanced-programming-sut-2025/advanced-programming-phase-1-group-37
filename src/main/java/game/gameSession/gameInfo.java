package game.gameSession;

import game.models.user;
import game.time.TimeSystem;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class gameInfo {
    /** All users participating in this session, in turn order */
    private final List<user> Players;

    /**
     * The “main” player who created or most recently loaded
     * this session.
     */
    private user MainPlayer;

    /**
     * For each player, which map number they chose.
     * Map numbers are 1…MAX_MAPS
     */
    private final Map<user,Integer> PlayersMaps = new LinkedHashMap<>();

    private final TimeSystem Clock;

    public gameInfo(List<user> Players, user Creator, TimeSystem clock) {
        this.Players    = Players;
        this.MainPlayer = Creator;
        this.Clock = clock;
    }

    /** Unmodifiable list of players */
    public List<user> GetPlayers() {
        return List.copyOf(Players);
    }

    /** Who started or last loaded this game */
    public user GetMainPlayer() {
        return MainPlayer;
    }

    /** Change who the main player is (only if they’re in the list) */
    public void SetMainPlayer(user u) {
        if (Players.contains(u)) {
            this.MainPlayer = u;
        }
    }

    /** Assign a chosen map to a user (only if they’re in the list) */
    public void SetPlayerMap(user u, int mapNumber) {
        if (Players.contains(u)) {
            PlayersMaps.put(u, mapNumber);
        }
    }

    /** Retrieve the map number a player chose (or null if not set) */
    public Integer GetPlayerMap(user u) {
        return PlayersMaps.get(u);
    }

    /** Immutable view of all map selections */
    public Map<user,Integer> GetPlayersMaps() {
        return Map.copyOf(PlayersMaps);
    }

    public TimeSystem GetClock() {
        return Clock;
    }
}
