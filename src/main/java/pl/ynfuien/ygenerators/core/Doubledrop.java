package pl.ynfuien.ygenerators.core;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import pl.ynfuien.ygenerators.Lang;
import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.managers.config.ConfigManager;

import java.util.HashMap;

public class Doubledrop {
    private YGenerators instance;

    private long timeLeft = 0;
    private double multiplayer = -1;

    private BukkitTask interval = null;

    public Doubledrop(YGenerators instance) {
        this.instance = YGenerators.getInstance();
    }

    public boolean load(ConfigurationSection config) {
        // TO DO
        // Load from database
        return true;
    }

    // Gets whether double drop is active
    public boolean isActive() {
        return timeLeft == -1 || timeLeft > 0;
    }

    // Gets double drop time left
    public long getTimeLeft() {
        return timeLeft;
    }

    // Gets formatted double drop time left
    @NotNull
    public String getFormattedTimeLeft() {
        // Return infinity time format if double drop time is infinity
        if (timeLeft == -1) return Lang.Message.DOUBLEDROP_TIME_INFINITY.get();

        // Get hours from time left
        long hours = timeLeft / 60;
        // Get minutes from time left
        int minutes = (int)(timeLeft % 60);

        // Create hashmap for placeholders
        HashMap<String, Object> placeholders = new HashMap<>();
        // Add hours to placeholders
        placeholders.put("hours", hours);
        // Add minutes to placeholders
        placeholders.put("minutes", minutes);

        // Return time
        return Lang.Message.DOUBLEDROP_TIME.get(placeholders);
    }

    // Gets double drop multiplayer
    public double getMultiplayer() {
        return multiplayer;
    }

    // Adds time to double drop time left
    public void addTime(long time) {
        if (timeLeft == -1) return;
        setTimeLeft(timeLeft + time);
    }

    // Sets double drop time left
    public void setTimeLeft(long timeLeft) {
        // Set time left to -1 if it is lower than -1
        if (timeLeft < -1) timeLeft = -1;
        this.timeLeft = timeLeft;

        // Save new time in config
        saveConfig();
        // Return if time left is lower than 1
        if (timeLeft < 1) {
            cancelInterval();
            return;
        }
        // Return if interval is already set
        if (interval != null) return;

        // Get plugin instance
        YGenerators instance = YGenerators.getInstance();
        // Get bukkit scheduler
        BukkitScheduler scheduler = Bukkit.getScheduler();
        // Create interval
        interval = scheduler.runTaskTimerAsynchronously(instance, () -> {
            // Cancel interval if time left is lower than 1
            if (this.timeLeft < 1) {
                cancelInterval();
                return;
            }

            // Decrease time left
            this.timeLeft -= 1;
            // Save config
            saveConfig();

            // Cancel interval if time left is lower than 1
            if (this.timeLeft < 1) {
                cancelInterval();

                // Broadcast message to all players about end of double drop
                scheduler.runTask(instance, () -> {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        Lang.Message.DOUBLEDROP_END.send(p);
                    }
                     Lang.Message.DOUBLEDROP_END.send(Bukkit.getConsoleSender());
                });
            }
        }, 60 * 20, 60 * 20);
    }

    // Cancels interval
    public boolean cancelInterval() {
        if (interval == null) return false;

        interval.cancel();
        interval = null;

        return true;
    }

    // Removes time from double drop time left
    public void removeTime(long time) {
        if (timeLeft == -1) return;
        if (timeLeft - time < 0) time = timeLeft;
        setTimeLeft(timeLeft - time);
    }

    // Sets double drop multiplayer
    public void setMultiplayer(double multiplayer) {
        if (multiplayer < 0) multiplayer = 0;
        this.multiplayer = multiplayer;

        saveConfig();
    }

    private boolean save() {
        // TO DO
        // Save to the database
        return true;
//        String fileName = "doubledrop.yml";
//
//        FileConfiguration config = configManager.getConfig(fileName);
//        config.set("time-left", timeLeft);
//        if (multiplayer != -1) {
//            config.set("multiplayer", multiplayer);
//        }
//
//        try {
//            File file = new File(instance.getDataFolder(), fileName);
//            config.save(file);
//        } catch (IOException e) {
//            return false;
//        }
//
//        return true;
    }
}
