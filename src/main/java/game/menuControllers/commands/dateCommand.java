package game.menuControllers.commands;

import game.time.TimeSystem;
import game.view.MenuView;

public class dateCommand implements command {
    private final TimeSystem Clock;
    private final MenuView View;

    public dateCommand(TimeSystem clock, MenuView view) {
        this.Clock = clock;
        this.View  = view;
    }

    @Override
    public boolean Execute(String[] Args) {
        View.ShowMessage( Clock.GetDate() );
        return true;
    }
}
