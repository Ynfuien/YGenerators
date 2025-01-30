package pl.ynfuien.ygenerators.core;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
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
        this.instance = YGenerators.getInstance();
    }

    public boolean load(Database database) {
        this.database = database;

        Values values = database.getDoubledrop();
        if (values == null) return false;

        setTimeLeft(values.timeLeft());
        setMultiplayer(values.multiplayer());
        return true;
    }

    private boolean save() {
        return database.setDoubledrop(timeLeft, multiplayer);
    }

    public boolean isActive() {
        return timeLeft == -1 || timeLeft > 0;
    }

    public long getTimeLeft() {
        return timeLeft;
    }

    @NotNull
    public String getFormattedTimeLeft() {
        if (timeLeft == -1) return Lang.Message.DOUBLEDROP_TIME_INFINITY.get();

        int hours = timeLeft / 60;
        int minutes = timeLeft % 60;

        HashMap<String, Object> placeholders = new HashMap<>();
        placeholders.put("hours", hours);
        placeholders.put("minutes", minutes);

        return Lang.Message.DOUBLEDROP_TIME.get(placeholders);
    }

    public double getMultiplayer() {
        return multiplayer;
    }

    public void addTime(int time) {
        if (timeLeft == -1) return;
        setTimeLeft(timeLeft + time);
    }

    public void setTimeLeft(int timeLeft) {
        if (timeLeft < -1) timeLeft = -1;
        this.timeLeft = timeLeft;

        save();
        if (timeLeft < 1) {
            cancelInterval();
            return;
        }
        if (intervalTask != null) return;

        intervalTask = Bukkit.getAsyncScheduler().runAtFixedRate(instance, (task) -> {
            // Cancel interval
            if (this.timeLeft < 1) {
                cancelInterval();
                return;
            }

            // Decrease time and save it
            this.timeLeft -= 1;
            save();

            // Cancel interval and broadcast message about it
            if (this.timeLeft < 1) {
                cancelInterval();

                Lang.Message.DOUBLEDROP_END.send(Bukkit.getConsoleSender());
                for (Player p : Bukkit.getOnlinePlayers()) Lang.Message.DOUBLEDROP_END.send(p);
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    // Cancels interval
    public boolean cancelInterval() {
        if (intervalTask == null) return false;

        intervalTask.cancel();
        intervalTask = null;

        return true;
    }

    public void removeTime(int time) {
        if (timeLeft == -1) return;
        if (timeLeft - time < 0) time = timeLeft;
        setTimeLeft(timeLeft - time);
    }

    public void setMultiplayer(float multiplayer) {
        if (multiplayer < 0) multiplayer = 0;
        this.multiplayer = multiplayer;

        save();
    }

    public record Values(int timeLeft, float multiplayer) {};
}
