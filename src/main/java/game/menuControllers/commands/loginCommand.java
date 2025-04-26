// src/main/java/game/menuControllers/commands/loginCommand.java
package game.menuControllers.commands;

import game.menuControllers.commands.command;
import game.menuControllers.MenuManager;
import game.models.userManager;
import game.view.MenuView;

import java.io.FileWriter;
import java.io.IOException;

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
        String u = View.Prompt("Username: ");
        if ("exit".equalsIgnoreCase(u)) return true;

        String p = View.Prompt("Password: ");
        if ("exit".equalsIgnoreCase(p)) return true;

        if (Users.Authenticate(u, p)) {
            View.ShowMessage("✔ Welcome back, " + u + "!");
            // ask stay-logged-in
            String stay = View.Prompt("Stay logged in? (y/n): ").trim();
            if ("y".equalsIgnoreCase(stay)) {
                // persist to session.txt
                try (FileWriter fw = new FileWriter("session.txt")) {
                    fw.write(u);
                } catch (IOException e) {
                    View.ShowMessage("Warning: could not save session.");
                }
                View.ShowMessage("You will remain logged in on next launch.");
            }
            Menus.SwitchToMainMenu();
        } else {
            View.ShowMessage("✘ Invalid credentials.");
        }
        return true;
    }
}
