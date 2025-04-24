package game.menuControllers.commands;

// src/main/java/game/menuControllers/commands/ShowCurrentMenuCommand.java

import game.menuControllers.commands.command;
import game.menuControllers.MenuManager;
import game.view.MenuView;

public class showCurrentMenuCommand implements command {
    private final MenuManager Menus;
    private final MenuView View;

    public showCurrentMenuCommand(MenuManager menus, MenuView view) {
        this.Menus = menus;
        this.View  = view;
    }

    @Override
    public boolean Execute(String[] Args) {
        View.ShowMessage("You are in the \"" + Menus.ShowCurrentMenu() + "\" menu.");
        return true;
    }
}

