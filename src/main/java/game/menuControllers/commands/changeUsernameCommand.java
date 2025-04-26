package game.menuControllers.commands;

import game.menuControllers.commands.command;
import game.menuControllers.MenuManager;
import game.models.userManager;
import game.view.MenuView;

public class changeUsernameCommand implements command {
    private final userManager Users;
    private final MenuView View;
    private final MenuManager Menus;

    public changeUsernameCommand(userManager users, MenuView view, MenuManager menus) {
        this.Users = users;
        this.View  = view;
        this.Menus = menus;
    }

    @Override
    public boolean Execute(String[] Args) {
        String current = Menus.getCurrentUser();
        String u = View.Prompt("New username: ").trim();
        if ("exit".equalsIgnoreCase(u)) {
            View.ShowMessage("Operation cancelled.");
            return true;
        }
        if (u.equalsIgnoreCase(current)) {
            View.ShowMessage("old username and new username are the same!");
            return true;
        }
        if (Users.GetUser(u) != null) {
            View.ShowMessage("username already in use!");
            return true;
        }
        // update in userManager
        if (Users.ChangeUsername(current, u)) {
            View.ShowMessage("username changed to \""+u+"\".");
            Menus.setCurrentUser(u);
        } else {
            View.ShowMessage("change failed.");
        }
        return true;
    }
}
