package game.menuControllers.commands;

import game.time.TimeSystem;
import game.view.MenuView;

public class timeCommand implements command {
    private final TimeSystem Clock;
    private final MenuView View;

    public timeCommand(TimeSystem clock, MenuView view) {
        this.Clock = clock;
        this.View  = view;
    }

    @Override
    public boolean Execute(String[] Args) {
        View.ShowMessage( Clock.GetTime() );
        return true;
    }
}
