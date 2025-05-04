package game.menuControllers.commands;

import game.time.TimeSystem;
import game.view.MenuView;

public class dayCommand implements command {
    private final TimeSystem Clock;
    private final MenuView View;

    public dayCommand(TimeSystem clock, MenuView view) {
        this.Clock = clock;
        this.View  = view;
    }

    @Override
    public boolean Execute(String[] Args) {
        View.ShowMessage( Clock.GetDayOfWeek() );
        return true;
    }
}
