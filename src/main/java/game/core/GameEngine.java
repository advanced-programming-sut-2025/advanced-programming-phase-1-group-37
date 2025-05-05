package game.core;

import game.gameSession.gameController;
import game.gameSession.gameSession;
import game.menuControllers.MenuManager;
import game.models.userManager;
import game.time.TimeSystem;
import game.view.MenuView;
import game.view.consoleMenuView;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class GameEngine {
    private static userManager users = new userManager();
//    private static MenuView view = new consoleMenuView();
    private static MenuManager menus = new MenuManager(users);
//    private static MenuManager menus;
    private static gameSession CurrentGameSession;
    private static TimeSystem clock = new TimeSystem();
//    private static gameController GameController = new gameController(view, clock);
    private static String FileName = null;

    public static void main(String[] args) throws Exception {
//        GameController.SetMenuManager(menus);
//        menus.SetGameController(GameController);
        MenuView view = menus.GetView();
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

    public static void SetCurrentSaveFileName(String fileName) {
        FileName = fileName;
    }

    public static String GetCurrentSaveFileName() {
        return FileName;
    }
}