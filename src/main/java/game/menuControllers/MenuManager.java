// src/main/java/game/menuControllers/MenuManager.java
package game.menuControllers;

import game.menuControllers.commands.*;
import game.models.userManager;
import game.view.MenuView;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MenuManager {
    private final MenuView View;
    private final userManager Users;
    private Menu CurrentMenu;

    private final Menu AuthMenu;
    private final Menu MainMenu;

    public MenuManager(MenuView View, userManager Users) {
        this.View  = View;
        this.Users = Users;

        this.AuthMenu = BuildAuthMenu();
        this.MainMenu = BuildMainMenu();

        this.CurrentMenu = AuthMenu;
        View.DisplayMenu(CurrentMenu.GetMenuName(), CurrentMenu.GetCommands().keySet());
    }

    private Menu BuildAuthMenu() {
        Map<String, command> Commands = new LinkedHashMap<>();
        Commands.put("register",   new registerCommand(Users, View));
        Commands.put("login",      new loginCommand(Users, View, this));
        Commands.put("enter menu", new enterMenuCommand(this, View));
        Commands.put("exit",       new ExitCommand());
        return new Menu("Login/Register", Commands);
    }

    private Menu BuildMainMenu() {
        Map<String, command> Commands = new LinkedHashMap<>();
        Commands.put("enter menu", new enterMenuCommand(this, View));
        Commands.put("menu",       new showCurrentMenuCommand(this, View));
        // add other main‐menu commands here
        Commands.put("exit",       new ExitCommand());
        return new Menu("Main", Commands);
    }

    public void SwitchToMainMenu() {
        this.CurrentMenu = MainMenu;
        View.DisplayMenu(CurrentMenu.GetMenuName(), CurrentMenu.GetCommands().keySet());
    }

    public void EnterMenu(String name) {
        if ("Login/Register".equalsIgnoreCase(name)) {
            View.ShowMessage("you can't go there from here!");
        } else if ("Main".equalsIgnoreCase(name)) {
            if (CurrentMenu == AuthMenu) {
                View.ShowMessage("you can't go there from here!");
            } else {
                CurrentMenu = MainMenu;
                View.DisplayMenu(CurrentMenu.GetMenuName(), CurrentMenu.GetCommands().keySet());
            }
        } else {
            View.ShowMessage("menu doesn't exist!");
        }
    }

    public void Start() {
        boolean Running = true;
        while (Running) {
            String Line = View.Prompt("> ");
            command Cmd = CurrentMenu.GetCommands().get(Line.trim());
            if (Cmd != null) {
                Running = Cmd.Execute(new String[]{ Line });
            } else {
                View.ShowMessage("invalid command!");
            }
        }
        View.ShowMessage("Goodbye!");
    }

    public String ShowCurrentMenu() {
        return CurrentMenu.GetMenuName();
    }
}
