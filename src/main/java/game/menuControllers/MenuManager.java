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

        // start in login/register
        this.CurrentMenu = AuthMenu;
        View.DisplayMenu(CurrentMenu.GetMenuName(),
                CurrentMenu.GetCommands().keySet());
    }

    private Menu BuildAuthMenu() {
        Map<String, command> cmds = new LinkedHashMap<>();
        cmds.put("register",        new registerCommand(Users, View));
        cmds.put("login",           new loginCommand(Users, View, this));
        cmds.put("forget password", new forgetPasswordCommand(Users, View));
        cmds.put("enter menu",      new enterMenuCommand(this, View));
        cmds.put("exit",            new ExitCommand());
        return new Menu("Login/Register", cmds);
    }

    private Menu BuildMainMenu() {
        Map<String, command> cmds = new LinkedHashMap<>();
        cmds.put("enter menu", new enterMenuCommand(this, View));
        cmds.put("menu",       new showCurrentMenuCommand(this, View));
        // … other main‐menu commands …
        cmds.put("exit",       new ExitCommand());
        return new Menu("Main", cmds);
    }

    /** Called by loginCommand on success */
    public void SwitchToMainMenu() {
        this.CurrentMenu = MainMenu;
        View.DisplayMenu(CurrentMenu.GetMenuName(),
                CurrentMenu.GetCommands().keySet());
    }

    public void EnterMenu(String name) {
        if ("Login/Register".equalsIgnoreCase(name)) {
            View.ShowMessage("you can't go there from here!");
        } else if ("Main".equalsIgnoreCase(name)) {
            if (CurrentMenu == AuthMenu) {
                View.ShowMessage("you can't go there from here!");
            } else {
                CurrentMenu = MainMenu;
                View.DisplayMenu(CurrentMenu.GetMenuName(),
                        CurrentMenu.GetCommands().keySet());
            }
        } else {
            View.ShowMessage("menu doesn't exist!");
        }
    }

    public void Start() {
        boolean running = true;
        while (running) {
            String line = View.Prompt("> ");
            command cmd = CurrentMenu.GetCommands().get(line.trim());
            if (cmd != null) {
                running = cmd.Execute(new String[]{ line });
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
