package game.menuControllers.commands;

import game.menuControllers.commands.command;
import game.menuControllers.MenuManager;
import game.models.userManager;
import game.view.MenuView;

public class changeNicknameCommand implements command {
    private final userManager Users;
    private final MenuView View;
    private final MenuManager Menus;

    public changeNicknameCommand(userManager users, MenuView view, MenuManager menus) {
        this.Users = users;
        this.View  = view;
        this.Menus = menus;
    }

    @Override
    public boolean Execute(String[] Args) {
        String current = Menus.getCurrentUser();
        String oldNick = Users.GetUser(current).GetNickname();
        String n = View.Prompt("New nickname: ").trim();
        if ("exit".equalsIgnoreCase(n)) {
            View.ShowMessage("Operation cancelled.");
            return true;
        }
        if (n.equals(oldNick)) {
            View.ShowMessage("old nickname and new nickname are the same!");
            return true;
        }
        Users.ChangeNickname(current, n);
        View.ShowMessage("nickname changed to \""+n+"\".");
        return true;
    }
}
