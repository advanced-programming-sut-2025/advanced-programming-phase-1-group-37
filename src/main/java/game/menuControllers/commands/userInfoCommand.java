package game.menuControllers.commands;

import game.menuControllers.commands.command;
import game.menuControllers.MenuManager;
import game.models.userManager;
import game.view.MenuView;

public class userInfoCommand implements command {
    private final userManager Users;
    private final MenuView View;
    private final MenuManager Menus;

    public userInfoCommand(userManager users, MenuView view, MenuManager menus) {
        this.Users = users;
        this.View  = view;
        this.Menus = menus;
    }

    @Override
    public boolean Execute(String[] Args) {
        String u = Menus.getCurrentUser();
        var usr = Users.GetUser(u);
        View.ShowMessage("Username: " + usr.GetUsername());
        View.ShowMessage("Nickname: " + usr.GetNickname());
        View.ShowMessage("Email:    " + usr.GetEmail());
        View.ShowMessage("Gender:   " + usr.GetGender());
        return true;
    }
}
