package ru.leymooo.annotatedyaml;


import ru.leymooo.annotatedyaml.util.Validate;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigurationUtils {

    public static Logger LOGGER = Logger.getLogger("Minecraft");


    public static void save(Configuration configuration, ConfigurationProvider provider) {
        Validate.notNull(configuration, "configuration");
        Validate.notNull(provider, "provider");
        Validate.notNull(provider.getConfigFile(), "provider.getConfigFile()");

        List<String> outList = new ArrayList<>();
        dump(configuration, provider.getConfigurationSettingsSerializer(), outList, 0);
        try {
            File out = provider.getConfigFile();
            ru.leymooo.annotatedyaml.util.Files.createParentDirs(out);
            File tmpfile = new File(out.getParentFile(), "___tmpconfig");
            Files.write(tmpfile.toPath(), outList, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
            try {
                Files.move(tmpfile.toPath(), out.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmpfile.toPath(), out.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot save file ", e);
        }
    }

    private static List<String> dump(Configuration configuration, ConfigurationSettingsSerializer serializer, List<String> out,
                                     int currIndent) {
        try {
            String indent = repeat(" ", currIndent);

            ConfigOptions.Comment comments = configuration.getClass().getAnnotation(ConfigOptions.Comment.class);
            if (comments != null) {
                for (String comment : comments.value()) {
                    out.add(indent + (comment.startsWith("#") ? comment : "#" + comment));
                }
            }

            for (Field field : configuration.getClass().getDeclaredFields()) {
                if (field.getAnnotation(ConfigOptions.Ignore.class) != null) {
                    continue;
                }
                field.setAccessible(true);

                ConfigOptions.ConfigKey configKey = field.getAnnotation(ConfigOptions.ConfigKey.class);
                String key = configKey == null ? field.getName() : configKey.value();

                comments = field.getAnnotation(ConfigOptions.Comment.class);
                if (comments != null) {
                    for (String comment : comments.value()) {
                        out.add(indent + (comment.startsWith("#") ? comment : "#" + comment));
                    }
                }

                Object value = field.get(configuration);
                if (Configuration.class.isAssignableFrom(field.getType())) {
                    out.add("");
                    out.add(indent + key + ":");
                    dump((Configuration) value, serializer, out, currIndent + serializer.getIndent());
                } else {
                    out.addAll(Arrays.asList(upgradeIndent(serializer.serialize(key, value), serializer.getLineBreak(), indent)));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not dump config", e);
        }
        return out;
    }

    public static void load(Configuration configuration, ConfigurationProvider provider) {
        Validate.notNull(configuration, "configuration");
        Validate.notNull(provider, "provider");
        loadRecursive(configuration, provider, "");
    }

    private static void loadRecursive(Configuration configuration, ConfigurationProvider provider, String currPath) {
        for (Field field : configuration.getClass().getDeclaredFields()) {
            try {
                if (field.getAnnotation(ConfigOptions.Ignore.class) != null || field.getAnnotation(ConfigOptions.Final.class) != null) {
                    continue;
                }
                removeFinalFromField(field);
                field.setAccessible(true);
                Class<?> clazz = field.getType();
                ConfigOptions.ConfigKey configKey = field.getAnnotation(ConfigOptions.ConfigKey.class);
                String path = (currPath.isEmpty() ? "" : currPath + ".") + (configKey == null ? field.getName() : configKey.value());
                Object value = provider.get(path);
                if (provider.isConfigurationSection(path) && Configuration.class.isAssignableFrom(clazz)) {
                    loadRecursive((Configuration) field.get(configuration), provider, path);
                    continue;
                }
                if (provider.isConfigurationSection(path) && Map.class.isAssignableFrom(clazz)) {
                    value = transformMemorySectionToMap(provider.getValues(value, true), provider);
                } else if (clazz.isEnum() && value instanceof String) {
                    value = Enum.valueOf((Class<Enum>) clazz, (String) value);
                } else if (clazz.isArray() && value instanceof List) {
                    Class<?> clazz1 = field.getType().getComponentType();
                    List list = (List) value;
                    value = Array.newInstance(clazz1, list.size());
                    Object[] arr = (Object[]) value;
                    for (int i = 0; i < list.size(); i++) {
                        arr[i] = clazz.cast(list.get(i));
                    }
                }

                if (value == null) {
                    LOGGER.log(Level.WARNING, "Can't set value to '" + field + " (" + clazz.getSimpleName() + ")', because " +
                            "'" + path + "' is not set or null");
                    continue;
                }

                if (clazz.isInstance(value)) {
                    field.set(configuration, value);
                } else {
                    try {
                        field.set(configuration, value);
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING,
                                "Can't set '" + value + " (" + value.getClass().getSimpleName() + ")' to '"
                                        + field + " (" + clazz.getSimpleName() + ")' for path '" + path + "': " + e.getMessage());

                    }

                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING,
                        "Can't set value to '" + field + " (" + field.getType().getSimpleName() + ")': " + e.getMessage());
            }
        }

    }

    private static Map<String, Object> transformMemorySectionToMap(Map<String, Object> map, ConfigurationProvider provider) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (provider.isConfigurationSection(entry.getValue())) {
                entry.setValue(provider.getValues(entry.getValue(), true));
                transformMemorySectionToMap((Map<String, Object>) entry.getValue(), provider);
            }
        }
        return map;
    }


    private static void removeFinalFromField(Field field) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        int modifiers = field.getModifiers();
        if (Modifier.isFinal(modifiers)) {
            try {
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, modifiers & ~Modifier.FINAL);
            } catch (NoSuchFieldException e) {
                // Java 12 compatibility
                Method getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
                getDeclaredFields0.setAccessible(true);
                Field[] fields = (Field[]) getDeclaredFields0.invoke(Field.class, false);
                for (Field classField : fields) {
                    if ("modifiers".equals(classField.getName())) {
                        classField.setAccessible(true);
                        classField.set(field, modifiers & ~Modifier.FINAL);
                        break;
                    }
                }
            }
        }
    }


    private static String repeat(final String s, final int n) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(s);
        }
        return sb.toString();
    }

    private static String[] upgradeIndent(String original, String lineBreak, String indent) {
        String[] lines = original.split(lineBreak);
        for (int i = 0; i < lines.length; i++) {
            lines[i] = indent + lines[i];
        }
        return lines;
    }
}
