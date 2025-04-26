package game.menuControllers.commands;

import game.menuControllers.commands.command;
import game.menuControllers.MenuManager;
import game.models.userManager;
import game.view.MenuView;

import java.util.List;

public class changePasswordCommand implements command {
    private final userManager Users;
    private final MenuView View;
    private final MenuManager Menus;

    public changePasswordCommand(userManager users, MenuView view, MenuManager menus) {
        this.Users = users;
        this.View  = view;
        this.Menus = menus;
    }

    @Override
    public boolean Execute(String[] Args) {
        String current = Menus.getCurrentUser();
        String old = View.Prompt("Old password: ");
        if ("exit".equalsIgnoreCase(old)) {
            View.ShowMessage("Operation cancelled.");
            return true;
        }
        if (!Users.Authenticate(current, old)) {
            View.ShowMessage("old password is not correct!");
            return true;
        }

        String nw = View.Prompt("New password: ");
        if ("exit".equalsIgnoreCase(nw)) {
            View.ShowMessage("Operation cancelled.");
            return true;
        }
        if (nw.equals(old)) {
            View.ShowMessage("old password and new password are the same");
            return true;
        }

        List<String> errs = new registerCommand(null,null).validatePassword(nw);
        if (!errs.isEmpty()) {
            errs.forEach(View::ShowMessage);
            return true;
        }

        Users.ResetPassword(current, nw);
        View.ShowMessage("password changed.");
        return true;
    }
}
