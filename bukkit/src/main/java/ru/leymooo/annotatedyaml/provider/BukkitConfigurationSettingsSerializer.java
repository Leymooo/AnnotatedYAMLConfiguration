package ru.leymooo.annotatedyaml.provider;

import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;
import ru.leymooo.annotatedyaml.ConfigurationSettingsSerializer;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

public class BukkitConfigurationSettingsSerializer implements ConfigurationSettingsSerializer {

    private YamlConfiguration yamlConfiguration;
    private DumperOptions dumperOptions;
    private Representer representer;
    private Yaml yaml;

    public BukkitConfigurationSettingsSerializer(YamlConfiguration yamlConfiguration) {
        this.yamlConfiguration = yamlConfiguration;
    }

    private void updateYamlSettings() {
        if (dumperOptions == null || representer == null || yaml == null) {
            try {
                Class<?> clazz = yamlConfiguration.getClass();
                Field dumperOptionsField = clazz.getDeclaredField("yamlOptions"), representerField = clazz.getDeclaredField(
                        "yamlRepresenter"),
                        yamlField = clazz.getDeclaredField("yaml");
                dumperOptionsField.setAccessible(true);
                representerField.setAccessible(true);
                yamlField.setAccessible(true);
                this.dumperOptions = (DumperOptions) dumperOptionsField.get(yamlConfiguration);
                this.representer = (Representer) representerField.get(yamlConfiguration);
                this.yaml = (Yaml) yamlField.get(yamlConfiguration);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        dumperOptions.setIndent(yamlConfiguration.options().indent());
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        representer.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    }

    public void setYamlConfiguration(YamlConfiguration yamlConfiguration) {
        this.yamlConfiguration = yamlConfiguration;
        dumperOptions = null;
        representer = null;
        yaml = null;
        updateYamlSettings();
    }

    @Override
    public int getIndent() {
        return yamlConfiguration.options().indent();
    }

    @Override
    public String serialize(String key, Object object) {
        updateYamlSettings();
        Map<String, Object> map = new LinkedHashMap<>(1);
        map.put(key, object);
        return yaml.dump(map);
    }

    @Override
    public String getLineBreak() {
        return dumperOptions.getLineBreak().getString();
    }
}
