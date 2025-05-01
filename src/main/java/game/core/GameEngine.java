package game.core;

import game.gameSession.gameSession;
import game.menuControllers.MenuManager;
import game.models.userManager;
import game.view.MenuView;
import game.view.consoleMenuView;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class GameEngine {
    private static gameSession CurrentGameSession;

    public static void main(String[] args) throws Exception {
        userManager users = new userManager();
        MenuView view = new consoleMenuView();
        MenuManager menus = new MenuManager(view, users);


        // Auto‐login if session.txt exists
        Path session = Path.of("session.txt");
        if (Files.exists(session)) {
            String saved = Files.readString(session, StandardCharsets.UTF_8).trim();
            if (users.GetUser(saved) != null) {
                view.ShowMessage("Auto‐logged in as “" + saved + "”.");
                menus.setCurrentUser(saved);
                menus.SwitchToMainMenu();
            }
        }

        menus.Start();
    }

    public static void SetCurrentGameSession(gameSession currentGameSession) {
        CurrentGameSession = currentGameSession;
    }

    public static gameSession GetCurrentGameSession() {
        return CurrentGameSession;
    }
}