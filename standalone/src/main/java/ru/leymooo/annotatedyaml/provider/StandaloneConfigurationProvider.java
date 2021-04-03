package ru.leymooo.annotatedyaml.provider;

import org.yaml.snakeyaml.DumperOptions;
import ru.leymooo.annotatedyaml.ConfigurationProvider;
import ru.leymooo.annotatedyaml.ConfigurationSettingsSerializer;
import ru.leymooo.annotatedyaml.ConfigurationUtils;
import ru.leymooo.annotatedyaml.bukkit.configuration.ConfigurationSection;
import ru.leymooo.annotatedyaml.bukkit.configuration.InvalidConfigurationException;
import ru.leymooo.annotatedyaml.bukkit.configuration.file.YamlConfiguration;
import ru.leymooo.annotatedyaml.bukkit.configuration.serialization.ConfigurationSerializable;
import ru.leymooo.annotatedyaml.bukkit.configuration.serialization.ConfigurationSerialization;
import ru.leymooo.annotatedyaml.util.Validate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

public class StandaloneConfigurationProvider implements ConfigurationProvider {

    private File file;
    private YamlConfiguration yamlConfiguration;
    private ConfigurationSettingsSerializer serializer;
    private boolean fileLoadedWithoutErrors;

    public StandaloneConfigurationProvider() {
    }

    public StandaloneConfigurationProvider(File file) {
        this.file = file;
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
        if (yamlConfiguration == null) {
            reloadFileFromDisk();
        }
        return yamlConfiguration;
    }

    @Override
    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public boolean isFileSuccessfullyLoaded() {
        return fileLoadedWithoutErrors;
    }

    @Override
    public <T> T get(String path) {
        Object value = (yamlConfiguration.isList(path) ? yamlConfiguration.getList(path) : yamlConfiguration.get(path));
        return value == null ? null : (T) value;
    }

    @Override
    public void set(String path, Object value) {
        yamlConfiguration.set(path, value);
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
                if (object instanceof Enum) {
                    object = ((Enum<?>) object).name();
                }
                map.put(key, object);
                return yamlConfiguration.yaml.dump(map);
            }

            @Override
            public String getLineBreak() {
                return yamlConfiguration.yamlOptions.getLineBreak().getString();
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
            public void registerSerializable(Class clazz) {
                Validate.isTrue(ConfigurationSerializable.class.isAssignableFrom(clazz), "Class " + clazz + " does not implement ConfigurationSerializable");
                ConfigurationSerialization.registerClass((Class<? extends ConfigurationSerializable>) clazz);
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
