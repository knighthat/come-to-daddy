package me.knighthat.plugin;

import lombok.Getter;
import me.knighthat.plugin.command.CommandManager;
import me.knighthat.plugin.data.DataHandler;
import me.knighthat.plugin.event.EventListener;
import me.knighthat.plugin.file.MessageFile;
import me.knighthat.plugin.file.RecipeFile;
import me.knighthat.plugin.logging.Logger;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

@Getter
public class ComeToDaddy extends JavaPlugin {

    @NotNull
    private final MessageFile messages;
    @NotNull
    private final RecipeFile  recipes;

    public ComeToDaddy() {
        Logger.LOGGER = getSLF4JLogger();

        DataHandler.KEY = new NamespacedKey( this, "ComeToDaddy" );
        DataHandler.PROPERTY_KEY = new NamespacedKey( this, "magnet" );

        this.messages = new MessageFile( this );
        this.recipes = new RecipeFile( this );
    }

    @Override
    public void onEnable() {
        // Register event listener
        getServer().getPluginManager().registerEvents( new EventListener( this ), this );

        // Register command
        getCommand( "cometodaddy" ).setExecutor( new CommandManager( this ) );
    }
}
