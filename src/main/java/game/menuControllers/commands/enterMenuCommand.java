// src/main/java/game/menuControllers/commands/enterMenuCommand.java
package game.menuControllers.commands;

import game.menuControllers.commands.command;
import game.menuControllers.MenuManager;
import game.view.MenuView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class enterMenuCommand implements command {
    private final MenuManager Menus;
    private final MenuView View;

    // sub-menus accessible from Main
    private static final List<String> MAIN_MENUS = List.of("game", "profile", "avatar");
    private static final List<String> PROFILE_MENUS = List.of("main");

    public enterMenuCommand(MenuManager menus, MenuView view) {
        this.Menus = menus;
        this.View  = view;
    }

    @Override
    public boolean Execute(String[] Args) {
        String current = Menus.ShowCurrentMenu();
        List<String> available;
        if ("Login/Register".equals(current)) {
            available = Collections.emptyList();
        } else if ("Main".equals(current)) {
            available = MAIN_MENUS;
        } else if ("Profile".equals(current)) {
            available = PROFILE_MENUS;
        } else {
            // for any other menus not implemented yet, no submenus
            available = Collections.emptyList();
        }

        // show the available menus for the current context
        View.ShowMessage("Available menus: " + available);

        String menuName = View.Prompt("Menu name: ").trim();
        if ("exit".equalsIgnoreCase(menuName)) {
            View.ShowMessage("Operation cancelled.");
            return true;
        }

        Menus.EnterMenu(menuName);
        return true;
    }
}