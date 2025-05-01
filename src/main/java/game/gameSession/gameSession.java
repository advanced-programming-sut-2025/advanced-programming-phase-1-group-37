package game.gameSession;

import game.models.user;

import java.util.List;

public class gameSession {
    private final gameInfo Info;

    public gameSession(List<user> players, user creator) {
        this.Info = new gameInfo(players, creator);
    }

    /** Access the underlying GameInfo */
    public gameInfo GetInfo() {
        return Info;
    }
}
