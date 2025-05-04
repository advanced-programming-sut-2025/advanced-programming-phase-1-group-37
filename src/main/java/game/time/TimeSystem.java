package game.time;

/**
 * Simple in‐game clock.
 * - Starts at Day 1, 09:00
 * - Advances by one hour per turn
 */
public class TimeSystem {

    public TimeSystem(int day, int hour, int minute) {
        this.Day = day;
        this.Hour = hour;
        this.Minute = minute;
    }
    public enum Season { SPRING, SUMMER, FALL, WINTER }

    private int Day;     // absolute day count, starting at 1
    private int Hour;    // 0–23
    private int Minute;  // always 0 for now

    private static final int HOURS_PER_DAY    = 24;
    private static final int DAYS_PER_SEASON  = 28;
    private static final int SEASONS_IN_YEAR  = Season.values().length;

    // names for day-of-week; Day 1 → Saturday, Day 2 → Sunday, etc.
    private static final String[] WEEK_DAYS = {
            "Saturday", "Sunday", "Monday", "Tuesday",
            "Wednesday", "Thursday", "Friday"
    };

    public TimeSystem() {
        Reset();
    }

    /** Reset to the very beginning: Day 1, 09:00 */
    public void Reset() {
        Day    = 1;
        Hour   = 9;
        Minute = 0;
    }

    /** Advance time by one turn (one hour), rolling day/season as needed */
    public void AdvanceTurn() {
        Hour++;
        if (Hour >= HOURS_PER_DAY) {
            Hour = 0;
            Day++;
        }
    }

    /** -- NEW ACCESSORS BELOW -- */

    /** 1. Current time only, e.g. “09:00” */
    public String GetTime() {
        return String.format("%02d:%02d", Hour, Minute);
    }

    /** 2. Current date only, e.g. “Day 42” */
    public String GetDate() {
        return "Day " + Day;
    }

    /** 3. Combined date + time, e.g. “Day 42 09:00” */
    public String GetDateTime() {
        return GetDate() + " " + GetTime();
    }

    /** 4. Day of the week, cycling every 7 days */
    public String GetDayOfWeek() {
        // Day 1 → WEEK_DAYS[0], Day 2 → WEEK_DAYS[1], … Day 8 → WEEK_DAYS[0] again
        int idx = (Day - 1) % WEEK_DAYS.length;
        return WEEK_DAYS[idx];
    }

    /** 5. Current season (SPRING, SUMMER, FALL, WINTER) */
    public Season GetSeason() {
        int seasonIndex = ((Day - 1) / DAYS_PER_SEASON) % SEASONS_IN_YEAR;
        return Season.values()[seasonIndex];
    }

    // Optional helper if you want the name as string
    public String GetSeasonName() {
        return GetSeason().toString();
    }

    /** Existing human‐readable now, e.g. “Day 1 SPRING 09:00” */
    public String Now() {
        return String.format(
                "Day %d %s %02d:%02d",
                Day,
                GetSeason(),
                Hour,
                Minute
        );
    }

    public void SetDay(int day) {
        Day = day;
    }

    public void SetHour(int hour) {
        Hour = hour;
    }

    public void SetMinute(int minute) {
        Minute = minute;
    }

    // Getters in case you need raw values elsewhere:
    public int GetDay()     { return Day; }
    public int GetHour()    { return Hour; }
    public int GetMinute()  { return Minute; }
}
