package game.menuControllers;

import game.menuControllers.commands.command;

import java.util.Map;

public class Menu {
    private final String MenuName;
    private final Map<String, command> Commands;

    public Menu(String MenuName, Map<String, command> Commands) {
        this.MenuName = MenuName;
        this.Commands = Commands;
    }

    public String GetMenuName() {
        return MenuName;
    }

    public Map<String, command> GetCommands() {
        return Commands;
    }
}
