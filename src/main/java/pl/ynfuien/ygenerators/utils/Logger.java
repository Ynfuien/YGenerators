package pl.ynfuien.ygenerators.utils;

import org.bukkit.Bukkit;

public class Logger {
    private static String prefix;

    public static void setPrefix(String prefix) {
        Logger.prefix = prefix;
    }

    public static void log(String message) {
        Messages.send(Bukkit.getConsoleSender(),  prefix + message);
    }

    public static void logWarning(String message) {
        log("&e" + message);
    }

    public static void logError(String message) {
        log("&c" + message);
    }
}
