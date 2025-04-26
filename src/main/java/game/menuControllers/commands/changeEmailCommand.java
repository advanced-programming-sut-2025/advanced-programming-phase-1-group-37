package game.menuControllers.commands;

import game.menuControllers.commands.command;
import game.menuControllers.MenuManager;
import game.models.userManager;
import game.view.MenuView;

public class changeEmailCommand implements command {
    private final userManager Users;
    private final MenuView View;
    private final MenuManager Menus;

    public changeEmailCommand(userManager users, MenuView view, MenuManager menus) {
        this.Users = users;
        this.View  = view;
        this.Menus = menus;
    }

    @Override
    public boolean Execute(String[] Args) {
        String current = Menus.getCurrentUser();
        String oldEmail = Users.GetUser(current).GetEmail();
        String e = View.Prompt("New email: ").trim();
        if ("exit".equalsIgnoreCase(e)) {
            View.ShowMessage("Operation cancelled.");
            return true;
        }
        if (e.equalsIgnoreCase(oldEmail)) {
            View.ShowMessage("old email and new email are the same!");
            return true;
        }
        if (!Users.UpdateEmail(current, e)) {
            View.ShowMessage("invalid email format or already in use!");
        } else {
            View.ShowMessage("email changed to \""+e+"\".");
        }
        return true;
    }
}
