package game.menuControllers.commands;

import game.menuControllers.MenuManager;
import game.view.MenuView;

public class userLogoutCommand implements command {
    private final MenuManager Menus;
    private final MenuView View;

    public userLogoutCommand(MenuManager menus, MenuView view) {
        this.Menus = menus;
        this.View  = view;
    }

    @Override
    public boolean Execute(String[] Args) {
        View.ShowMessage("Logging out...");
        Menus.Logout();
        return true;
    }
}
