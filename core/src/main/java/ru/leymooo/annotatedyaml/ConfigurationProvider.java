package ru.leymooo.annotatedyaml;

import java.io.File;
import java.util.Map;

public interface ConfigurationProvider {

    File getConfigFile();

    Object get(String path);

    Map<String, Object> getValues(boolean deep);

    Map<String, Object> getValues(Object section, boolean deep);

    boolean isConfigurationSection(String path);

    boolean isConfigurationSection(Object object);

    boolean isFileSuccessfullyLoaded();

    void reloadFileFromDisk();

    ConfigurationSettingsSerializer getConfigurationSettingsSerializer();
}
