// src/main/java/game/menuControllers/commands/loadGameCommand.java
package game.menuControllers.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import game.core.GameEngine;
import game.gameSession.saveGameData;
import game.gameSession.gameController;
import game.gameSession.gameSession;
import game.menuControllers.MenuManager;
import game.models.user;
import game.models.userManager;
import game.time.TimeSystem;
import game.view.MenuView;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class loadGameCommand implements command {
    private final userManager    Users;
    private final MenuView       View;
    private final MenuManager    Menus;
    private final gameController Controller;
    private final Gson           Gson = new GsonBuilder().create();

    public loadGameCommand(
            userManager users,
            MenuManager menus,
            MenuView view,
            gameController controller
    ) {
        this.Users      = users;
        this.View       = view;
        this.Menus      = menus;
        this.Controller = controller;
    }

    @Override
    public boolean Execute(String[] Args) {
        String input = promptOrExit("Enter save file name: ");
        if (input == null) {
            return true;
        }

        String FileName = input;
        File file = new File(FileName + ".json");
        if (!file.exists()) {
            View.ShowMessage("Save file \"" + FileName + "\" not found.");
            return true;
        }

        saveGameData dto;
        try (Reader reader = new FileReader(file)) {
            dto = Gson.fromJson(reader, saveGameData.class);
        } catch (IOException e) {
            View.ShowMessage("Failed to load: " + e.getMessage());
            return true;
        }

        if (dto == null || dto.Players == null) {
            View.ShowMessage("Corrupted save file.");
            return true;
        }

//        dto.FileName = FileName;
        GameEngine.SetCurrentSaveFileName(FileName);

        // Rebuild player list
        List<user> players = dto.Players.stream()
                .map(Users::GetUser)
                .collect(Collectors.toList());
        if (players.contains(null)) {
            View.ShowMessage("One or more users in save do not exist.");
            return true;
        }

        TimeSystem Clock = new TimeSystem(dto.Day, dto.Hour, dto.Minute);

        // Construct new session
        gameSession session = new gameSession(players, Users.GetUser(Menus.getCurrentUser()), Clock);

        /**
         System.out.println("hour from save is : " + dto.Hour);
         */


        // Restore map choices
        Map<String,Integer> maps = dto.PlayersMaps != null
                ? dto.PlayersMaps
                : new LinkedHashMap<>();
        for (Map.Entry<String,Integer> e : maps.entrySet()) {
            user u = Users.GetUser(e.getKey());
            if (u != null) {
                session.GetInfo().SetPlayerMap(u, e.getValue());
            }
        }

        // Install the session globally
        GameEngine.SetCurrentGameSession(session);

        View.ShowMessage("Loaded \"" + FileName + "\" as main player: " + Menus.getCurrentUser());

        // Begin in-game loop
        Controller.LoadSession(session, Users.GetUser(Menus.getCurrentUser()));
        Menus.SwitchToInGameMenu();
        return true;
    }

    private String promptOrExit(String prompt) {
        String in = View.Prompt(prompt);
        if ("exit".equalsIgnoreCase(in)) {
            View.ShowMessage("Operation cancelled.");
            return null;
        }
        return in;
    }
}
