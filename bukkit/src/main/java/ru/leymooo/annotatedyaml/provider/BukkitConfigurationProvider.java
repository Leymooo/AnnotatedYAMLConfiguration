package ru.leymooo.annotatedyaml.provider;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.leymooo.annotatedyaml.ConfigurationProvider;
import ru.leymooo.annotatedyaml.ConfigurationSettingsSerializer;
import ru.leymooo.annotatedyaml.ConfigurationUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class BukkitConfigurationProvider implements ConfigurationProvider {

    private final File file;
    private BukkitConfigurationSettingsSerializer serializer;
    private YamlConfiguration yamlConfiguration;
    private boolean fileLoadedWithoutErrors;

    public BukkitConfigurationProvider(File file) {
        this.file = file;
        reloadFileFromDisk();
    }

    @Override
    public File getConfigFile() {
        return file;
    }

    @Override
    public void reloadFileFromDisk() {
        this.yamlConfiguration = new YamlConfiguration();
        try {
            this.fileLoadedWithoutErrors = false;
            this.yamlConfiguration.load(file);
            this.fileLoadedWithoutErrors = true;
        } catch (FileNotFoundException fnf) {

        } catch (IOException | InvalidConfigurationException e) {
            ConfigurationUtils.LOGGER.log(Level.SEVERE, "Cannot load " + file, e);
        }
        if (this.serializer != null) {
            this.serializer.setYamlConfiguration(yamlConfiguration);
        } else {
            this.serializer = new BukkitConfigurationSettingsSerializer(yamlConfiguration);
        }
    }

    public YamlConfiguration getYamlConfiguration() {
        return yamlConfiguration;
    }

    @Override
    public boolean isFileSuccessfullyLoaded() {
        return fileLoadedWithoutErrors;
    }

    @Override
    public Object get(String path) {
        return yamlConfiguration.isList(path) ? yamlConfiguration.getList(path) : yamlConfiguration.get(path);
    }

    @Override
    public Map<String, Object> getValues(boolean deep) {
        return yamlConfiguration.getValues(deep);
    }

    @Override
    public Map<String, Object> getValues(Object section, boolean deep) {
        if (section instanceof ConfigurationSection) {
            return ((ConfigurationSection) section).getValues(deep);
        }
        return new HashMap<>();
    }

    @Override
    public boolean isConfigurationSection(String path) {
        return yamlConfiguration.isConfigurationSection(path);
    }

    @Override
    public boolean isConfigurationSection(Object object) {
        return object instanceof ConfigurationSection;
    }

    @Override
    public ConfigurationSettingsSerializer getConfigurationSettingsSerializer() {
        return serializer;
    }
}
