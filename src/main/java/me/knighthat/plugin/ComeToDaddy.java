package me.knighthat.plugin;

import me.knighthat.plugin.data.DataHandler;
import me.knighthat.plugin.event.EventListener;
import me.knighthat.plugin.item.MagnetItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class ComeToDaddy extends JavaPlugin {


    public ComeToDaddy() {
        DataHandler.KEY = new NamespacedKey( this, "ComeToDaddy" );
    }

    @Override
    public void onEnable() {
        // Register event listener
        getServer().getPluginManager().registerEvents( new EventListener( this ), this );

        // Register command
        //getCommand( "ctd" ).setExecutor( new CommandManager( this ) );

        ShapedRecipe recipe = new ShapedRecipe( DataHandler.KEY, new MagnetItem() );
        recipe.shape(
                "LII",
                "  I",
                "RII"
        );
        recipe.setIngredient( 'L', Material.LAPIS_LAZULI );
        recipe.setIngredient( 'I', Material.IRON_INGOT );
        recipe.setIngredient( 'R', Material.REDSTONE );

        getServer().addRecipe( recipe );
    }
}