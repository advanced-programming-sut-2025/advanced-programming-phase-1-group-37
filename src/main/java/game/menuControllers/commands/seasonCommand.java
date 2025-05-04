package game.menuControllers.commands;

import game.time.TimeSystem;
import game.view.MenuView;

public class seasonCommand implements command {
    private final TimeSystem Clock;
    private final MenuView View;

    public seasonCommand(TimeSystem clock, MenuView view) {
        this.Clock = clock;
        this.View  = view;
    }

    @Override
    public boolean Execute(String[] Args) {
        View.ShowMessage( Clock.GetSeasonName() );
        return true;
    }
}
