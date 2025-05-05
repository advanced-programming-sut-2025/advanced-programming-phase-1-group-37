// src/main/java/game/menuControllers/commands/exitGameCommand.java
package game.menuControllers.commands;

import game.core.GameEngine;
import game.gameSession.saveGameData;
import game.gameSession.gameController;
import game.gameSession.gameSession;
import game.menuControllers.MenuManager;
import game.time.TimeSystem;
import game.view.MenuView;
import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class exitGameCommand implements command {
    private final MenuManager    Menus;
    private final MenuView       View;
    private final Gson           Gson = new Gson();
    private final gameController Controller;

    public exitGameCommand(MenuManager menus, MenuView view, gameController controller) {
        this.Menus      = menus;
        this.View       = view;
        this.Controller = controller;
    }

    @Override
    public boolean Execute(String[] Args) {
        gameSession session = GameEngine.GetCurrentGameSession();
        if (session == null) {
            View.ShowMessage("No game in progress.");
            return true;
        }

        String currentUser = Menus.getCurrentUser();
        String mainPlayer  = session.GetInfo().GetMainPlayer().GetUsername();

        // Only the main player may exit & save
        if (!currentUser.equalsIgnoreCase(mainPlayer)) {
            View.ShowMessage("Only the main player can exit and save the game!");
            return true;
        }

        // Build a flat DTO for saving
        saveGameData dto = new saveGameData();
        // 1) players by username
        List<String> players = session.GetInfo().GetPlayers().stream()
                .map(u -> u.GetUsername())
                .collect(Collectors.toList());
        dto.Players = players;

        // 2) map choices
        Map<String,Integer> maps = new LinkedHashMap<>();
        for (String uname : players) {
            maps.put(uname, session.GetInfo().GetPlayerMap(
                    session.GetInfo().GetPlayers()
                            .stream()
                            .filter(u-> u.GetUsername().equals(uname))
                            .findFirst()
                            .get()
            ));
        }
        dto.PlayersMaps = maps;

        // 3) main player
        dto.MainPlayer = mainPlayer;

        TimeSystem clock = session.GetInfo().GetClock();
        dto.Day    = Controller.GetClock().GetDay();
        dto.Hour   = Controller.GetClock().GetHour();
        dto.Minute = Controller.GetClock().GetMinute();

        /**
         System.out.println("hour when exiting is : " + Controller.GetClock().GetHour());
         */

        // Serialize the DTO to disk
        String FileName;
        if(GameEngine.GetCurrentSaveFileName() != null) {
            FileName = GameEngine.GetCurrentSaveFileName();
        }
        else {
            FileName = mainPlayer + "_save";
        }
        String Path = FileName + ".json";
//        String filename = mainPlayer + "_save.json";
        try (Writer writer = new FileWriter(Path)) {
            Gson.toJson(dto, writer);
            View.ShowMessage("Game saved to " + FileName);
        } catch (IOException e) {
            View.ShowMessage("Failed to save game: " + e.getMessage());
        }

        // Delegate the exit and menu‐switch to the controller
        Controller.ExitGame();
        return true;
    }
}
