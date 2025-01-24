package pl.ynfuien.ygenerators.storage;

public class Storage {
    private static YVanish instance;
    private static Database database;

    public static void setup(YVanish instance) {
        Storage.instance = instance;
        Storage.database = instance.getDatabase();
    }



    public static Database getDatabase() {
        return database;
    }
}