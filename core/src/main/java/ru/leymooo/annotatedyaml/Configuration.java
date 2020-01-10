package ru.leymooo.annotatedyaml;

public abstract class Configuration {

    private ConfigurationProvider provider;

    public Configuration(ConfigurationProvider provider) {
        this.provider = provider;
    }

    public Configuration() {
        provider = null;
    }

    public void save() {
        save(provider);
    }

    public void save(ConfigurationProvider provider) {
        ConfigurationUtils.save(this, provider);
    }

    public void load() {
        load(getConfigurationProvider());
    }

    public void load(ConfigurationProvider provider) {
        if (!provider.getConfigFile().exists()) {
            return;
        }
        ConfigurationUtils.load(this, provider);
    }

    public ConfigurationProvider getConfigurationProvider() {
        return provider;
    }

    public void setConfigurationProvider(ConfigurationProvider provider) {
        this.provider = provider;
    }
}
