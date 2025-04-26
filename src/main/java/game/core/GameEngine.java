package game.core;

import game.menuControllers.MenuManager;
import game.models.userManager;
import game.view.MenuView;
import game.view.consoleMenuView;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class GameEngine {
    public static void main(String[] args) {
        // 1) load users
        userManager users = new userManager();
        // 2) prepare view
        MenuView view = new consoleMenuView();
        // 3) build menus
        MenuManager menus = new MenuManager(view, users);

        // 4) check for saved “stay-logged-in” session
        Path sessionFile = Path.of("session.txt");
        if (Files.exists(sessionFile)) {
            try {
                String savedUser = Files.readString(sessionFile, StandardCharsets.UTF_8).trim();
                if (users.GetUser(savedUser) != null) {
                    view.ShowMessage("Auto-logged in as “" + savedUser + "”.");
                    menus.SwitchToMainMenu();
                }
            } catch (Exception ignored) {
                // if anything goes wrong, just ignore and start at login
            }
        }

        // 5) start interaction loop
        menus.Start();
    }
}