package pl.ynfuien.ygenerators.hooks;

import org.bukkit.Bukkit;
import pl.ynfuien.ydevlib.messages.YLogger;
import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.hooks.placeholderapi.PlaceholderAPIHook;
import pl.ynfuien.ygenerators.hooks.superiorskyblock2.SuperiorSkyblock2Hook;

public class Hooks {
    private static PlaceholderAPIHook papiHook = null;

    public static void load(YGenerators instance) {
        // PAPI
        if (isPluginEnabled(Plugin.PAPI)) {
            papiHook = new PlaceholderAPIHook(instance);
            if (!papiHook.register()) {
                papiHook = null;
                YLogger.error("[Hooks] Something went wrong while registering PlaceholderAPI hook!");
            }
            else {
                YLogger.info("[Hooks] Successfully registered hook for PlaceholderAPI!");
            }
        }

        // SS2
        if (isPluginEnabled(Plugin.SS2)) {
            SuperiorSkyblock2Hook.load(instance);
            YLogger.info("[Hooks] Successfully registered hook for SuperioSkyblock2!");
        }
    }

    public static boolean isPluginEnabled(Plugin plugin) {
        return Bukkit.getPluginManager().isPluginEnabled(plugin.getName());
    }

    public enum Plugin {
        PAPI("PlaceholderAPI"),
        VAULT("Vault"),
        LUCKPERMS("LuckPerms"),
        SS2("SuperiorSkyblock2"),
        ;

        private final String name;
        Plugin(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
