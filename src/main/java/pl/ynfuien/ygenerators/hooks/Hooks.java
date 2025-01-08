package pl.ynfuien.ygenerators.hooks;

import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.hooks.placeholderapi.PlaceholderAPIHook;
import pl.ynfuien.ygenerators.hooks.superiorskyblock2.SuperiorSkyblock2Hook;
import pl.ynfuien.ydevlib.messages.YLogger;
import pl.ynfuien.ygenerators.utils.Util;

public class Hooks {
    private static PlaceholderAPIHook papiHook = null;
    private static boolean papiHookEnabled = false;

    public static void load(YGenerators instance) {
        // Register PlaceholderAPI hook
        if (Util.isPapiEnabled()) {
            papiHook = new PlaceholderAPIHook(instance);
            papiHook.register();
            papiHookEnabled = true;
            YLogger.info("[Hooks] Successfully registered hook for PlaceholderAPI!");
        }

        // Register SuperiorSkyblock2 hook
        if (Util.isSS2Enabled()) {
            SuperiorSkyblock2Hook.load(instance);
            YLogger.info("[Hooks] Successfully registered hook for SuperiorSkyblock2!");
        }

    }

    public static boolean isPapiHookEnabled() {
        return papiHookEnabled;
    }

    public static boolean isSS2HookEnabled() {
        return SuperiorSkyblock2Hook.isEnabled();
    }
}
