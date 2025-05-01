package game.menuControllers.commands;

import game.core.GameEngine;
import game.gameSession.gameSession;
import game.menuControllers.MenuManager;
import game.models.user;
import game.models.userManager;
import game.view.MenuView;

public class loadGameCommand implements command {
    private final userManager Users;
    private final MenuView View;
    private final MenuManager Menus;
//    private final user MainPlayer;

    public loadGameCommand(userManager users ,MenuManager menus, MenuView view) {
        this.Users = users;
        this.Menus = menus;
        this.View  = view;
//        this.MainPlayer = mainPlayer;
    }

    @Override
    public boolean Execute(String[] Args) {
        user MainPlayer = Users.GetUser(Menus.getCurrentUser());
        if (MainPlayer.GetCurrentGameSession() == null) {
            View.ShowMessage("\"there is no saved game\"");
            return true;
        }

        else {
            gameSession GameSession = MainPlayer.GetCurrentGameSession();
            GameSession.GetInfo().SetMainPlayer(MainPlayer);
            GameEngine.SetCurrentGameSession(GameSession);
        }
        return true;
    }


    // Helper to allow typing "exit" to cancel
//    private String PromptOrExit(String prompt) {
//        String Input = View.Prompt(prompt);
//        if ("exit".equalsIgnoreCase(Input)) {
//            View.ShowMessage("Operation cancelled.");
//            return null;
//        }
//        return Input;
//    }
}
