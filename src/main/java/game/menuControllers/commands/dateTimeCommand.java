package game.menuControllers.commands;

import game.time.TimeSystem;
import game.view.MenuView;

public class dateTimeCommand implements command {
    private final TimeSystem Clock;
    private final MenuView View;

    public dateTimeCommand(TimeSystem clock, MenuView view) {
        this.Clock = clock;
        this.View  = view;
    }

    @Override
    public boolean Execute(String[] Args) {
        View.ShowMessage( Clock.GetDateTime() );
        return true;
    }
}
