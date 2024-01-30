package me.knighthat.plugin;

import lombok.Getter;
import me.knighthat.plugin.data.DataHandler;
import me.knighthat.plugin.event.EventListener;
import me.knighthat.plugin.file.MessageFile;
import me.knighthat.plugin.item.MagnetItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

@Getter
public class ComeToDaddy extends JavaPlugin {

    @NotNull
    private final MessageFile messages;

    public ComeToDaddy() {
        DataHandler.KEY = new NamespacedKey(this, "ComeToDaddy");

        this.messages = new MessageFile(this);
    }

    @Override
    public void onEnable() {
        // Register event listener
        getServer().getPluginManager().registerEvents(new EventListener(this), this);

        // Register command
        //getCommand( "ctd" ).setExecutor( new CommandManager( this ) );

        ShapedRecipe recipe = new ShapedRecipe(DataHandler.KEY, new MagnetItem());
        recipe.shape(
                "LII",
                "  I",
                "RII"
        );
        recipe.setIngredient('L', Material.LAPIS_LAZULI);
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('R', Material.REDSTONE);

        getServer().addRecipe(recipe);
    }
}
