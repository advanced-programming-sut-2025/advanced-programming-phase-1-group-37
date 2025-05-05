package game.gameSession;

import game.menuControllers.MenuManager;
import game.models.user;
import game.time.TimeSystem;
import game.view.MenuView;

public class gameController {
    private MenuManager Menus;
    private final MenuView View;
    private TimeSystem Time;
    private gameSession Session;

    private int currentPlayerIndex;
    private boolean inGame;

    public gameController(MenuManager menus, MenuView view, TimeSystem time) {
        this.Menus = menus;
        this.View  = view;
        this.Time  = time;
    }

    /** Kick off a brand‐new game session */
    public void StartNewSession(gameSession session) {
        this.Session            = session;
        this.currentPlayerIndex = 0;
        this.inGame             = true;
        Time.Reset();  // Day 1, 09:00, SPRING
        beginTurnLoop();
    }

    /** Load an existing session */
    public void LoadSession(gameSession session, user loader) {
        /**
         System.out.println("hour in controller is : " + session.GetInfo().GetClock().GetHour());
         */
        this.Session            = session;
        this.currentPlayerIndex = session.GetInfo().GetPlayers().indexOf(loader);
        Time = session.GetInfo().GetClock();
//        Menus.SetClock(session.GetInfo().GetClock());
        session.GetInfo().SetMainPlayer(loader);
        this.inGame             = true;
        beginTurnLoop();
    }

    /** Called by NextTurnCommand to advance to the next player */
    public void NextTurn() {
        if (!inGame) return;

        endTurn();

        int count = Session.GetInfo().GetPlayers().size();
        // if we're on the last player, next turn should advance the clock
        boolean wasLast = (currentPlayerIndex == count - 1);

        currentPlayerIndex = (currentPlayerIndex + 1) % count;

        if (wasLast) {
            Time.AdvanceTurn();
            /**
             System.out.println("hour after turn is : " + Time.GetHour());
             */

        }

        StartTurn();
        Menus.SwitchToInGameMenu();
    }

    /** Called by ExitGameCommand or ForceTerminateCommand */
    public void ExitGame() {
        inGame  = false;
        Session = null;
        Menus.SwitchToMainMenu();
    }

    // --------------------------------
    // Internal turn management
    // --------------------------------

    private void beginTurnLoop() {
        StartTurn();
//        Menus.SwitchToInGameMenu();
    }

    private void StartTurn() {
        user player = Session.GetInfo().GetPlayers().get(currentPlayerIndex);
        /**
         System.out.println("index is : " + currentPlayerIndex);
         */

        Menus.setCurrentUser(player.GetUsername());
        /**
         * this might be needed later
         * Session.GetInfo().SetMainPlayer(player);
         */

        View.ShowMessage(
                "=== " + player.GetUsername() +
                        "'s turn (" + Time.Now() + ") ==="
        );
    }

    private void endTurn() {
        // placeholder: any end‐of‐turn effects go here
    }

    /** Whether the session is still running */
    public boolean IsInGame() {
        return inGame;
    }

    public void SetMenuManager(MenuManager menus) {
        this.Menus = menus;
    }

    public TimeSystem GetClock() {
        return Time;
    }
}
