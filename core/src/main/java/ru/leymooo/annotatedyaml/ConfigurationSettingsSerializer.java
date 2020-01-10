package ru.leymooo.annotatedyaml;


public interface ConfigurationSettingsSerializer {

    int getIndent();

    String serialize(String key, Object object);

    String getLineBreak();
}
