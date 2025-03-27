package pl.ynfuien.ygenerators.core;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.ynfuien.ygenerators.Lang;
import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.storage.Database;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class Doubledrop {
    private YGenerators instance;
    private Database database;

    private int timeLeft = 0;
    private float multiplayer = 2;

    private ScheduledTask intervalTask = null;

    public Doubledrop(YGenerators instance) {
        this.instance = instance;
    }

    /**
     * Internal method for loading this class.
     */
    public boolean load(Database database) {
        this.database = database;

        Values values = database.getDoubledrop();
        if (values == null) return false;

        setTimeLeft(values.timeLeft());
        setMultiplayer(values.multiplayer());
        return true;
    }

    private boolean save() {
        if (database == null) return false;

        return database.setDoubledrop(timeLeft, multiplayer);
    }

    /**
     * Checks whether doubledrop is active.
     */
    public boolean isActive() {
        return timeLeft == -1 || timeLeft > 0;
    }

    /**
     * @return Time left in minutes
     */
    public long getTimeLeft() {
        return timeLeft;
    }

    /**
     * @return DOUBLEDROP_TIME or DOUBLEDROP_TIME_INFINITY message with replaced {hours} and {minutes} placeholders
     */
    public String getFormattedTimeLeft() {
        if (timeLeft == -1) return Lang.Message.DOUBLEDROP_TIME_INFINITY.get();

        int hours = timeLeft / 60;
        int minutes = timeLeft % 60;

        HashMap<String, Object> placeholders = new HashMap<>();
        placeholders.put("hours", hours);
        placeholders.put("minutes", minutes);

        return Lang.Message.DOUBLEDROP_TIME.get(placeholders);
    }

    /**
     * @return Current doubledrop multiplayer
     */
    public double getMultiplayer() {
        return multiplayer;
    }

    /**
     * Adds provided to the current doubledrop time.
     * @param time Time in minutes
     */
    public void addTime(int time) {
        if (timeLeft == -1) return;
        setTimeLeft(timeLeft + time);
    }

    /**
     * Sets the time of the doubledrop.
     * @param timeLeft Time in minutes
     */
    public void setTimeLeft(int timeLeft) {
        if (timeLeft < -1) timeLeft = -1;
        this.timeLeft = timeLeft;

        save();
        if (timeLeft < 1) {
            stopInterval();
            return;
        }
        if (intervalTask != null) return;

        intervalTask = Bukkit.getAsyncScheduler().runAtFixedRate(instance, (task) -> {
            // Cancel interval
            if (this.timeLeft < 1) {
                stopInterval();
                return;
            }

            // Decrease time and save it
            this.timeLeft -= 1;
            save();

            // Cancel interval and broadcast message about it
            if (this.timeLeft < 1) {
                stopInterval();

                Lang.Message.DOUBLEDROP_END.send(Bukkit.getConsoleSender());
                for (Player p : Bukkit.getOnlinePlayers()) Lang.Message.DOUBLEDROP_END.send(p);
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    /**
     * Stops interval decreasing the time left.
     */
    public void stopInterval() {
        if (intervalTask == null) return;

        intervalTask.cancel();
        intervalTask = null;
    }

    /**
     * Removes provided time from the time left. It will be set to 0 if removed time is greater than current time left.
     * @param time Time in minutes.
     */
    public void removeTime(int time) {
        if (timeLeft == -1) return;
        if (timeLeft - time < 0) time = timeLeft;
        setTimeLeft(timeLeft - time);
    }

    /**
     * Sets doubledrop multiplayer.
     */
    public void setMultiplayer(float multiplayer) {
        if (multiplayer < 0) multiplayer = 0;
        this.multiplayer = multiplayer;

        save();
    }

    /**
     * Values record with time left and multiplayer. Used only between this class and Database.
     */
    public record Values(int timeLeft, float multiplayer) {};
}
