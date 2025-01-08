package pl.ynfuien.ygenerators.updater;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import pl.ynfuien.ydevlib.messages.YLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class Updater {
    private final Plugin plugin;
    private String apiURL = "https://api.github.com/repos/%s/%s/releases";

    public Updater(Plugin plugin, String user, String repository) {
        this.plugin = plugin;
        apiURL = String.format(apiURL, user, repository);
    }

    // Gets the latest plugin version from the latest release tag on GitHub
    public String getLatestVersion() {
        try {
            // Connect to the URL using java's native library
            URL url = new URL(apiURL);
            URLConnection request = url.openConnection();
            request.connect();

            // Convert to a JSON element
            JsonElement element = JsonParser.parseReader(new InputStreamReader((InputStream) request.getContent()));

            // Get array of releases
            JsonArray array = element.getAsJsonArray();
            // Get last release
            JsonObject release = array.get(0).getAsJsonObject();
            // Get its tag and return it
            return release.get("tag_name").getAsString();
        } catch (IOException e) {
            return null;
        }
    }

    // Gets the latest release title
    public String getLatestUpdateTitle() {
        try {
            // Connect to the URL using java's native library
            URL url = new URL(apiURL);
            URLConnection request = url.openConnection();
            request.connect();

            // Convert to a JSON element
            JsonElement element = JsonParser.parseReader(new InputStreamReader((InputStream) request.getContent()));

            // Get array of releases
            JsonArray array = element.getAsJsonArray();
            // Get last release
            JsonObject release = array.get(0).getAsJsonObject();

            // Get its name and return it
            return release.get("name").getAsString();
        } catch (IOException e) {
            return null;
        }
    }

    // Returns whether provided version is newer than current
    private boolean isNewerVersion(int version) {
        String stringVersion = plugin.getDescription().getVersion();

        int currentVersion = convertVersionToInt(stringVersion);

        return version > currentVersion;
    }

    // Converts string version to int
    private int convertVersionToInt(String version) {
        if (version == null) return -1;
        return Integer.parseInt(version.replace(".", ""));
    }

    // Checks update
    public void checkUpdate() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String latestVersion = getLatestVersion();
            if (latestVersion == null) return;

            int lastReleaseVersion = convertVersionToInt(latestVersion);
            if (!isNewerVersion(lastReleaseVersion)) return;

            YLogger.info("&9===================================================");
            YLogger.info("");
            YLogger.info("&3New version of the plugin is available!");
            YLogger.info("");
            YLogger.info("&bCurrent version: &c" + plugin.getDescription().getVersion());
            YLogger.info("&bNew version: &a" + latestVersion);
            String title = getLatestUpdateTitle();
            if (title != null) {
                YLogger.info("&bUpdate title: &f" + title);
            }
            YLogger.info("");
            YLogger.info("&bLink:");
            YLogger.info(" &3" + plugin.getDescription().getWebsite() + "/releases");
            YLogger.info("");
            YLogger.info("&9===================================================");
        });
    }
}
