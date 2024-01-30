package me.knighthat.plugin.file;

import lombok.Getter;
import me.knighthat.plugin.ComeToDaddy;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class PluginFile {

    @NotNull
    protected final ComeToDaddy plugin;
    @Getter
    @NotNull
    private final String fileName;
    @Getter
    @Nullable
    private File file;
    @Nullable
    private YamlConfiguration yaml;

    public PluginFile(@NotNull ComeToDaddy plugin, @NotNull String fileName) {
        this.plugin = plugin;
        this.fileName = fileName + ".yml";

        setup();
    }

    private void createIfNotExist() {
        if (file != null && !file.exists())
            plugin.saveResource(fileName, false);
    }

    private void setup() {
        if (file == null)
            this.file = new File(plugin.getDataFolder(), fileName);

        createIfNotExist();
        reload();
    }

    public void reload() {
        if (file == null)
            setup();
        createIfNotExist();

        this.yaml = YamlConfiguration.loadConfiguration(file);
    }

    public @NotNull YamlConfiguration get() {
        if (file == null || yaml == null)
            reload();

        assert this.yaml != null;
        return this.yaml;
    }
}
