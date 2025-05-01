package game.menuControllers.commands;

import game.core.GameEngine;
import game.gameSession.gameSession;
import game.menuControllers.MenuManager;
import game.models.user;
import game.view.MenuView;

import java.util.ArrayList;
import java.util.List;

public class selectGameMapCommand implements command {
    private final MenuView View;
    private final MenuManager Menus;
    private final List<Integer> Choices = new ArrayList<Integer>();
    private static final int MAX_MAPS = 10;

    public selectGameMapCommand(MenuView view, MenuManager menus) {
        this.View  = view;
        this.Menus = menus;
    }

    @Override
    public boolean Execute(String[] Args) {
        List<user> Players = Menus.getCurrentGamePlayers();
        if (Players == null || Players.isEmpty()) {
            View.ShowMessage("No game in progress.");
            Menus.SwitchToMapMenu();
            return true;
        }

        View.ShowMessage("\"enter a map number (1-10)\"");

        for (user Player : Players) {
            while (true) {
                String In = PromptOrExit(Player.GetUsername() + " map: ");
                if (In == null) {
                    return true;
                }
                try {
                    int Choice = Integer.parseInt(In);
                    if (Choice >= 1 && Choice <= MAX_MAPS) {
                        Choices.add(Choice);
                        break;
                    } else {
                        View.ShowMessage("Invalid map number! Must be 1 to " + MAX_MAPS + ".");
                    }
                } catch (NumberFormatException e) {
                    View.ShowMessage("Please enter a number 1–" + MAX_MAPS + ".");
                }
            }
        }

        gameSession GameSession = GameEngine.GetCurrentGameSession();
        int size = Players.size();
        for (int i = 0; i < size; i++) {
            GameSession.GetInfo().SetPlayerMap(Players.get(i), Choices.get(i));
        }

        /**
         * change this later
         */
        Menus.SwitchToMainMenu();

        return true;

    }

    private String PromptOrExit(String Prompt) {
        String Input = View.Prompt(Prompt);
        if ("exit".equalsIgnoreCase(Input)) {
            View.ShowMessage("Operation cancelled.");
            return null;
        }
        return Input;
    }
}
