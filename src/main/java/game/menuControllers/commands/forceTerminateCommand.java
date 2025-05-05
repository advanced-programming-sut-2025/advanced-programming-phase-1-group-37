package game.menuControllers.commands;

import game.core.GameEngine;
import game.gameSession.gameController;
import game.gameSession.gameSession;
import game.menuControllers.MenuManager;
import game.models.user;
import game.view.MenuView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class forceTerminateCommand implements command {
    private final MenuManager Menus;
    private final MenuView    View;
    private final gameController Controller;

    public forceTerminateCommand(MenuManager menus, MenuView view, gameController controller) {
        this.Menus = menus;
        this.View  = view;
        this.Controller = controller;
    }

    @Override
    public boolean Execute(String[] Args) {
        gameSession session = GameEngine.GetCurrentGameSession();
        if (session == null) {
            View.ShowMessage("No game in progress.");
            return true;
        }

        String currentUser = Menus.getCurrentUser();
        List<user> players = session.GetInfo().GetPlayers();

        // Player who initiated the termination
//        user initiator = session.GetInfo().GetPlayers().get(0); // Assuming the first player started this
        List<String> votes = new ArrayList<>();
//        votes.add(initiator.GetUsername() + ": agree");  // Initiator's vote is "agree" by default

        View.ShowMessage("\"voting to terminate game (agree/disagree)\"");

        boolean allAgreed = true;

        // Ask the other players for their vote
        for (user player : players) {
            if (player.GetUsername().equals(currentUser)) {
                View.ShowMessage(currentUser + " : agree");
                continue;
            }
            String vote = promptForVote(player);
            if (vote.equals("disagree")) {
                allAgreed = false;
            }
//            votes.add(player.GetUsername() + ": " + vote);
        }

        // Check if all players agree
//        boolean allAgreed = votes.stream().allMatch(vote -> vote.contains("agree"));

        if (allAgreed) {
            View.ShowMessage("All players agreed, the game will terminate now!");

            // Delete the save file
            String saveFileName = GameEngine.GetCurrentSaveFileName() + ".json";
            File saveFile = new File(saveFileName);
            if (saveFile.exists()) {
                saveFile.delete();
                View.ShowMessage("Game save file deleted.");
            }

            // Set CurrentGameSession to null for all players
            for (user player : players) {
                player.SetCurrentGameSession(null);
            }

            // Terminate the game
            Controller.ExitGame();
        } else {
            View.ShowMessage("Termination was not successful.");
        }

        return true;
    }

    private String promptForVote(user player) {
        while (true) {
            String vote = View.Prompt(player.GetUsername() + " : ");
            if ("agree".equalsIgnoreCase(vote) || "disagree".equalsIgnoreCase(vote)) {
                return vote;
            }
            View.ShowMessage("Invalid input. Please enter 'agree' or 'disagree'.");
        }
    }
}
