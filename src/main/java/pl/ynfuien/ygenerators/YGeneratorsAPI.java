package pl.ynfuien.ygenerators;

import pl.ynfuien.ygenerators.core.Doubledrop;
import pl.ynfuien.ygenerators.core.Generators;
import pl.ynfuien.ygenerators.core.placedgenerators.PlacedGenerators;

public class YGeneratorsAPI {
    private static YGenerators instance;

    public static void setInstance(YGenerators instance) {
        YGeneratorsAPI.instance = instance;
    }

    /**
     * @return Double drop instance
     */
    public static Doubledrop getDoubledrop() {
        return instance.getDoubledrop();
    }

    /**
     * @return Generators instance
     */
    public static Generators getGenerators() {
        return instance.getGenerators();
    }

    /**
     * @return Generators database instance
     */
    public static PlacedGenerators getDatabase() {
        return instance.getPlacedGenerators();
    }
}
