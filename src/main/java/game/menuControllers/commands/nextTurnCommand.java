package game.menuControllers.commands;

import game.gameSession.gameController;
import game.menuControllers.MenuManager;
import game.view.MenuView;

public class nextTurnCommand implements command {
    private final gameController Controller;

    public nextTurnCommand(gameController controller) {
        this.Controller = controller;
    }

    @Override
    public boolean Execute(String[] Args) {
        Controller.NextTurn();
        return true;
    }
}
