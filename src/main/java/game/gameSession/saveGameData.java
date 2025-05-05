package game.gameSession;

import java.util.List;
import java.util.Map;

public class saveGameData {
//    public String FileName;
    public List<String> Players;             // just the usernames
    public Map<String,Integer> PlayersMaps;  // username → chosen map
    public String MainPlayer;                // username
    public int Day, Hour, Minute;            // current time
}