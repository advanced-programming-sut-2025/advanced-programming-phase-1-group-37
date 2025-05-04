package game.menuControllers.commands;

import game.menuControllers.MenuManager;
import game.view.MenuView;

public class forceTerminateCommand implements command {
    private final MenuManager Menus;
    private final MenuView View;

    public forceTerminateCommand(MenuManager menus, MenuView view) {
        this.Menus = menus;
        this.View  = view;
    }

    @Override
    public boolean Execute(String[] Args) {
        View.ShowMessage("Game forcefully terminated.");
        // e.g. clear any in-memory session here:
        // Menus.SetCurrentSession(null);
        Menus.SwitchToMainMenu();
        return true;
    }
}
