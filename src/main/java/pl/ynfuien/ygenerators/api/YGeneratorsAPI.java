package pl.ynfuien.ygenerators.api;

import pl.ynfuien.ygenerators.YGenerators;
import pl.ynfuien.ygenerators.core.Doubledrop;
import pl.ynfuien.ygenerators.core.Generators;
import pl.ynfuien.ygenerators.core.placedgenerators.PlacedGenerators;

public class YGeneratorsAPI {
    private static YGenerators instance;

    public static void setInstance(YGenerators instance) {
        YGeneratorsAPI.instance = instance;
    }

    /**
     * @return Doubledrop instance with which you can get / set / modify time left and multiplayer.
     */
    public static Doubledrop getDoubledrop() {
        return instance.getDoubledrop();
    }

    /**
     * @return Generators instance which contains loaded generators from generators.yml and also some values from config.yml.
     */
    public static Generators getGenerators() {
        return instance.getGenerators();
    }

    /**
     * @return PlacedGenerators instance with all placed generators over the worlds.
     */
    public static PlacedGenerators getPlacedGenerators() {
        return instance.getPlacedGenerators();
    }
}
