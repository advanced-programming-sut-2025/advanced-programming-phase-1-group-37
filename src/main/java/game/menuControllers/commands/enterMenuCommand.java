// src/main/java/game/menuControllers/commands/enterMenuCommand.java
package game.menuControllers.commands;

import game.menuControllers.commands.command;
import game.menuControllers.MenuManager;
import game.view.MenuView;

import java.util.Arrays;

public class enterMenuCommand implements command {
    private final MenuManager Menus;
    private final MenuView View;

    public enterMenuCommand(MenuManager menus, MenuView view) {
        this.Menus = menus;
        this.View  = view;
    }

    @Override
    public boolean Execute(String[] Args) {
        String menuName = promptOrExit("Menu name: ");
        if (menuName == null) return true;
        Menus.EnterMenu(menuName);
        return true;
    }

    private String promptOrExit(String prompt) {
        String input = View.Prompt(prompt);
        if ("exit".equalsIgnoreCase(input)) {
            View.ShowMessage("Operation cancelled.");
            return null;
        }
        return input;
    }
}