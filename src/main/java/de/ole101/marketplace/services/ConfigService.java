package de.ole101.marketplace.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class ConfigService {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private final File pluginFolder;

    public ConfigService() {
        this.pluginFolder = new File("plugins/ItemMarketplace");
        if (this.pluginFolder.mkdirs()) {
            log.info("ItemMarketplace plugin folder created successfully");
        }
    }

    public <T> T loadConfig(Class<T> configClass, String fileName) {
        File configFile = new File(this.pluginFolder, fileName);
        if (!configFile.exists()) {
            try {
                return createConfig(configFile, configClass);
            } catch (Exception exception) {
                throw new RuntimeException("Failed to save config file: " + exception.getMessage());
            }
        }

        return readConfig(configFile, configClass);
    }

    public <T> void saveConfig(T config, String fileName) {
        File configFile = new File(this.pluginFolder, fileName);
        saveConfig(configFile, config);
    }

    private <T> T readConfig(File file, Class<T> clazz) {
        try (FileReader reader = new FileReader(file, UTF_8)) {
            return GSON.fromJson(reader, clazz);
        } catch (Exception exception) {
            log.info("Read failed, try creating new config: {}", exception.getMessage());
            return createConfig(file, clazz);
        }
    }

    private <T> T createConfig(File file, Class<T> clazz) {
        try {
            T config = clazz.getConstructor().newInstance();
            saveConfig(file, config);
            return config;
        } catch (Exception exception) {
            throw new RuntimeException("Failed to create config file: " + exception.getMessage());
        }
    }

    private <T> void saveConfig(File file, T object) {
        try (FileWriter writer = new FileWriter(file, UTF_8)) {
            writer.write(GSON.toJson(object));
        } catch (Exception exception) {
            throw new RuntimeException("Failed to save config file: " + exception.getMessage());
        }
    }
}