package game.gameSession;

import game.models.user;
import game.time.TimeSystem;

import java.util.List;

public class gameSession {
    private final gameInfo Info;

    public gameSession(List<user> players, user creator, TimeSystem clock) {
        this.Info = new gameInfo(players, creator, clock);
    }

    /** Access the underlying GameInfo */
    public gameInfo GetInfo() {
        return Info;
    }
}
