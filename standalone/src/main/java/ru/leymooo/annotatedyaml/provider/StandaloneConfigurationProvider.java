package ru.leymooo.annotatedyaml.provider;

import ru.leymooo.annotatedyaml.bukkit.configuration.ConfigurationSection;
import ru.leymooo.annotatedyaml.bukkit.configuration.InvalidConfigurationException;
import ru.leymooo.annotatedyaml.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.DumperOptions;
import ru.leymooo.annotatedyaml.ConfigurationProvider;
import ru.leymooo.annotatedyaml.ConfigurationSettingsSerializer;
import ru.leymooo.annotatedyaml.ConfigurationUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

public class StandaloneConfigurationProvider implements ConfigurationProvider {

    private final File file;
    private YamlConfiguration yamlConfiguration;
    private ConfigurationSettingsSerializer serializer;
    private boolean fileLoadedWithoutErrors;


    public StandaloneConfigurationProvider(File file) {
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
        createSerializer();
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

    private void createSerializer() {
        this.serializer = new ConfigurationSettingsSerializer() {

            @Override
            public int getIndent() {
                return yamlConfiguration.options().indent();
            }

            @Override
            public String serialize(String key, Object object) {
                Map<String, Object> map = new LinkedHashMap<>(1);
                map.put(key, object);
                return yamlConfiguration.yaml.dump(map);
            }

            @Override
            public String getLineBreak() {
                return yamlConfiguration.yamlOptions.getLineBreak().getString();
            }
        };
    }

    @Override
    public ConfigurationSettingsSerializer getConfigurationSettingsSerializer() {
        yamlConfiguration.yamlOptions.setIndent(yamlConfiguration.options().indent());
        yamlConfiguration.yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yamlConfiguration.yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        return serializer;
    }
}
