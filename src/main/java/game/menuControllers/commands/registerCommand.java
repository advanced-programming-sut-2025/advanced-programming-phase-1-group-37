// src/main/java/game/menuControllers/commands/registerCommand.java
package game.menuControllers.commands;

import game.menuControllers.commands.command;
import game.models.userManager;
import game.view.MenuView;

public class registerCommand implements command {
    private final userManager Users;
    private final MenuView View;

    public registerCommand(userManager users, MenuView view) {
        this.Users = users;
        this.View  = view;
    }

    @Override
    public boolean Execute(String[] Args) {
        String u = promptOrExit("Username: "); if (u == null) return true;
        String p = promptOrExit("Password: "); if (p == null) return true;
        String e = promptOrExit("Email: ");    if (e == null) return true;
        String g = promptOrExit("Gender: ");   if (g == null) return true;
        String n = promptOrExit("Nickname: "); if (n == null) return true;

        if (Users.RegisterUser(u, p, e, g, n)) {
            View.ShowMessage("✔ Registered \"" + u + "\" successfully.");
        } else {
            View.ShowMessage("✘ Username \"" + u + "\" is already taken.");
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
