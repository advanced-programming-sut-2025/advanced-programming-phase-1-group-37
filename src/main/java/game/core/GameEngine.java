package game.core;

import game.menuControllers.MenuManager;
import game.models.userManager;
import game.view.consoleMenuView;

public class GameEngine {
    public static void main(String[] args) {
        // 1. Create your single UserManager
        userManager Users = new userManager();

        // 2. Create your view
        consoleMenuView View = new consoleMenuView();

        // 3. Pass both into the MenuManager
        MenuManager Menus = new MenuManager(View, Users);

        // 4. Kick off the menu loop
        Menus.Start();
    }
}