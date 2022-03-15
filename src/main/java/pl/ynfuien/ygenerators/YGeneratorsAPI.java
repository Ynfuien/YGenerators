package pl.ynfuien.ygenerators;

import pl.ynfuien.ygenerators.data.Doubledrop;
import pl.ynfuien.ygenerators.data.Generators;
import pl.ynfuien.ygenerators.generators.Database;

public class YGeneratorsAPI {
    private static YGenerators instance;

    public static void setInstance(YGenerators instance) {
        YGeneratorsAPI.instance = instance;
    }

    /**
     * @return Double drop instance
     */
    public static Doubledrop getDoubledrop() {
        return instance.getGenerators().getDoubledrop();
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
    public static Database getDatabase() {
        return instance.getDatabase();
    }
}
