// src/main/java/game/menuControllers/commands/loginCommand.java
package game.menuControllers.commands;

import game.menuControllers.commands.command;
import game.menuControllers.MenuManager;
import game.models.userManager;
import game.view.MenuView;

public class loginCommand implements command {
    private final userManager Users;
    private final MenuView View;
    private final MenuManager Menus;

    public loginCommand(userManager users,
                        MenuView view,
                        MenuManager menus) {
        this.Users = users;
        this.View  = view;
        this.Menus = menus;
    }

    @Override
    public boolean Execute(String[] Args) {
        String u = promptOrExit("Username: "); if (u == null) return true;
        String p = promptOrExit("Password: "); if (p == null) return true;

        if (Users.Authenticate(u, p)) {
            View.ShowMessage("✔ Welcome back, " + u + "!");
            Menus.SwitchToMainMenu();
        } else {
            View.ShowMessage("✘ Invalid credentials.");
        }
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
