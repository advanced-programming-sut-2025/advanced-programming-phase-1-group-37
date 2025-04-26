// src/main/java/game/menuControllers/MenuManager.java
package game.menuControllers;

import game.menuControllers.commands.*;
import game.models.userManager;
import game.view.MenuView;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MenuManager {
    private final MenuView View;
    private final userManager Users;
    private Menu CurrentMenu;
    private String CurrentUser;

    private final Menu AuthMenu;
    private final Menu MainMenu;
    private final Menu ProfileMenu;

    private static final List<String> REGISTER_LOGIN_MENUS = List.of();
    private static final List<String> MAIN_MENUS = List.of("game", "profile", "avatar");
    private static final List<String> PROFILE_MENUS = List.of("main");
    private static final List<String> ALL_MENUS = List.of("register/login", "main", "game", "profile", "avatar");

    public MenuManager(MenuView View, userManager Users) {
        this.View  = View;
        this.Users = Users;

        this.AuthMenu    = buildAuthMenu();
        this.MainMenu    = buildMainMenu();
        this.ProfileMenu = buildProfileMenu();

        this.CurrentMenu = AuthMenu;
        View.DisplayMenu(CurrentMenu.GetMenuName(), CurrentMenu.GetCommands().keySet());
    }

    private Menu buildAuthMenu() {
        Map<String, command> cmds = new LinkedHashMap<>();
        cmds.put("register",         new registerCommand(Users, View));
        cmds.put("login",            new loginCommand(Users, View, this));
        cmds.put("forget password",  new forgetPasswordCommand(Users, View));
        cmds.put("enter menu",       new enterMenuCommand(this, View));
        cmds.put("show menu",        new showCurrentMenuCommand(this, View));
        cmds.put("exit",             new ExitCommand());
        return new Menu("Login/Register", cmds);
    }

    private Menu buildMainMenu() {
        Map<String, command> cmds = new LinkedHashMap<>();
        cmds.put("enter menu",     new enterMenuCommand(this, View));
        cmds.put("show menu",      new showCurrentMenuCommand(this, View));
        cmds.put("user logout",    new userLogoutCommand(this, View));
        cmds.put("exit",           new ExitCommand());
        return new Menu("Main", cmds);
    }

    private Menu buildProfileMenu() {
        Map<String, command> cmds = new LinkedHashMap<>();
        cmds.put("change username", new changeUsernameCommand(Users, View, this));
        cmds.put("change nickname", new changeNicknameCommand(Users, View, this));
        cmds.put("change email",    new changeEmailCommand(Users, View, this));
        cmds.put("change password", new changePasswordCommand(Users, View, this));
        cmds.put("user info",       new userInfoCommand(Users, View, this));
        cmds.put("enter menu",      new enterMenuCommand(this, View));    // moved before show menu
        cmds.put("show menu",       new showCurrentMenuCommand(this, View));
        cmds.put("exit",            new ExitCommand());
        return new Menu("Profile", cmds);
    }

    public void setCurrentUser(String username) {
        this.CurrentUser = username;
    }

    public void SwitchToMainMenu() {
        this.CurrentMenu = MainMenu;
        View.DisplayMenu(CurrentMenu.GetMenuName(), CurrentMenu.GetCommands().keySet());
    }

    public void SwitchToProfileMenu() {
        this.CurrentMenu = ProfileMenu;
        View.DisplayMenu(CurrentMenu.GetMenuName(), CurrentMenu.GetCommands().keySet());
    }

    public void EnterMenu(String name) {
        String cur = CurrentMenu.GetMenuName();

        if ("Login/Register".equals(cur)) {
            if (REGISTER_LOGIN_MENUS.contains(name)) {
                return;
            }

            else if (name.equals("register/login")) {
                View.ShowMessage("you are already in this menu");
                return;
            }

            else if (ALL_MENUS.contains(name)) {
                View.ShowMessage("you can't go there from here!");
                return;
            }

            else {
                View.ShowMessage("menu doesn't exist!");
                return;
            }
//            if (matchesAny(name, "main", "game", "profile", "avatar", "register/login")) {
//                View.ShowMessage("you can't go there from here!");
//            } else {
//                View.ShowMessage("menu doesn't exist!");
//            }
//            return;
        }

        if ("Main".equals(cur)) {
            if (MAIN_MENUS.contains(name)) {
                switch (name) {
                    case "game":
                        return;

                    case "profile":
                        SwitchToProfileMenu();
                        return;

                    case "avatar":
                        return;
                }
            }

            else if (name.equals("main")) {
                View.ShowMessage("you are already in this menu");
                return;
            }

            else if (ALL_MENUS.contains(name)) {
                View.ShowMessage("you can't go there from here!");
                return;
            }

            else {
                View.ShowMessage("menu doesn't exist!");
                return;
            }
//            switch (name.toLowerCase()) {
//                case "profile":
//                    SwitchToProfileMenu(); return;
//                case "game":
//                case "avatar":
//                    View.ShowMessage("you can't go there from here!");
//                    return;
//                case "main":
//                case "register/login":
//                    View.ShowMessage("you can't go there from here!");
//                    return;
//                default:
//                    View.ShowMessage("menu doesn't exist!");
//                    return;
//            }
        }

        if ("Profile".equals(cur)) {
            if (PROFILE_MENUS.contains(name)) {
                switch (name) {
                    case "main":
                        SwitchToMainMenu();
                        return;
                }
            }

            else if (name.equals("profile")) {
                View.ShowMessage("you are already in this menu");
                return;
            }

            else if (ALL_MENUS.contains(name)) {
                View.ShowMessage("you can't go there from here!");
                return;
            }

            else {
                View.ShowMessage("menu doesn't exist!");
                return;
            }

//            if ("main".equals(name)) {
//                SwitchToMainMenu();
//            } else {
//                View.ShowMessage("you can't go there from here!");
//            }
        }
    }

    private boolean matchesAny(String input, String... options) {
        for (String o : options) {
            if (o.equalsIgnoreCase(input)) return true;
        }
        return false;
    }

    public void Logout() {
        try { Files.deleteIfExists(Path.of("session.txt")); }
        catch (Exception ignored) {}
        this.CurrentUser = null;
        this.CurrentMenu = AuthMenu;
        View.DisplayMenu(CurrentMenu.GetMenuName(), CurrentMenu.GetCommands().keySet());
    }

    public void Start() {
        boolean running = true;
        while (running) {
            String line = View.Prompt("> ").trim();
            command cmd = CurrentMenu.GetCommands().get(line);
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

    /** Returns the currently logged-in username */
    public String getCurrentUser() {
        return CurrentUser;
    }
}
