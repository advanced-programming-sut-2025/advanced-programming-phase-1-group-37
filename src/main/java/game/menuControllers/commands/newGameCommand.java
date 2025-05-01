package game.menuControllers.commands;

import game.core.GameEngine;
import game.gameSession.gameSession;
import game.menuControllers.MenuManager;
import game.models.user;
import game.models.userManager;
import game.view.MenuView;

import java.util.ArrayList;
import java.util.List;

public class newGameCommand implements command {
    private final userManager Users;
    private final MenuView View;
    private final MenuManager Menus;
    private List<user> PlayersInGame = new ArrayList<>();

    public newGameCommand(userManager users, MenuView view, MenuManager menus) {
        this.Users = users;
        this.View = view;
        this.Menus = menus;
    }

    @Override
    public boolean Execute(String[] Args) {
        // Get number of players (between 2 and 4)
        int numPlayers = getNumberOfPlayers();
        if (numPlayers == 0) {
            return true;
        }

        // Get the first player (the user creating the game)
        String player1 = Menus.getCurrentUser();
        PlayersInGame.add(Users.GetUser(player1));
        View.ShowMessage("Player 1 : " + player1);

        // Get the remaining players' usernames
        for (int i = 2; i <= numPlayers; i++) {
            String player = getUsernameForPlayer(i);
            if (player == null) {
                return true;
            }
            PlayersInGame.add(Users.GetUser(player));
        }

        // Proceed with the game logic here...
        gameSession GameSession = new gameSession(PlayersInGame, Users.GetUser(player1));
        GameEngine.SetCurrentGameSession(GameSession);
        AddGameSessionForPlayers(PlayersInGame, GameSession);
        Menus.setCurrentGamePlayers(PlayersInGame);
        Menus.SwitchToMapMenu();
        return true;
    }

    private int getNumberOfPlayers() {
        while (true) {
            String input = promptOrExit("Enter number of players (2-4): ");
            if (input == null) return 0; // exit condition
            try {
                int numPlayers = Integer.parseInt(input);
                if (numPlayers >= 2 && numPlayers <= 4) {
                    return numPlayers;
                } else {
                    View.ShowMessage("Invalid number of players! Please enter a number between 2 and 4.");
                }
            } catch (NumberFormatException e) {
                View.ShowMessage("Invalid input! Please enter a valid number between 2 and 4.");
            }
        }
    }

    private String getUsernameForPlayer(int playerNumber) {
        String playerUsername;
        while (true) {
            playerUsername = promptOrExit("Player " + playerNumber + " : ");
            if (playerUsername == null) return null; // exit condition

            if (Users.GetUser(playerUsername) != null) {
                if (IsUserInGame(playerUsername)) {
                    View.ShowMessage(playerUsername + " is already in the game!");
                }

                else return playerUsername;
            }

            else if (playerUsername.isEmpty()) {
                View.ShowMessage("this field can not be empty!");
            }

            else {
                View.ShowMessage("Username \"" + playerUsername + "\" does not exist!");
            }
        }
    }

    // Implementing promptOrExit method
    private String promptOrExit(String prompt) {
        String input = View.Prompt(prompt);
        if ("exit".equalsIgnoreCase(input)) {
            View.ShowMessage("Operation cancelled.");
            return null;
        }
        return input;
    }

    private boolean IsUserInGame(String player) {
        for (user user : PlayersInGame) {
            if (user == Users.GetUser(player)) {
                return true;
            }
        }
        return false;
    }

    private void AddGameSessionForPlayers(List<user> players, gameSession GameSession) {
        for (user User : players) {
            User.SetCurrentGameSession(GameSession);
        }
    }
}
