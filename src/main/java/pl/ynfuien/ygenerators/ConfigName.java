package pl.ynfuien.ygenerators;

import pl.ynfuien.ydevlib.config.ConfigObject;

public enum ConfigName implements ConfigObject.Name {
    LANG,
    CONFIG,
    GENERATORS;

    @Override
    public String getFileName() {
        return name().toLowerCase().replace('_', '-') + ".yml";
    }
}
