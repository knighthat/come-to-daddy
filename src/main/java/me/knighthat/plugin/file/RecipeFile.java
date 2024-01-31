package me.knighthat.plugin.file;

import com.google.common.base.Preconditions;
import me.knighthat.plugin.ComeToDaddy;
import me.knighthat.plugin.data.DataHandler;
import me.knighthat.plugin.item.MagnetItem;
import me.knighthat.plugin.logging.Logger;
import me.knighthat.plugin.recipe.Recipe;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecipeFile extends PluginFile {

    @NotNull
    @Unmodifiable
    private static final List<String> REQUIRED_PATH;

    static {
        REQUIRED_PATH = List.of(
                "recipe.type",
                "recipe.shape",
                "recipe.ingredients",
                "result.material",
                "result.name",
                "result.lore",
                "properties.tier_name",
                "properties.x",
                "properties.y",
                "properties.z"
        );
    }

    public RecipeFile( @NotNull ComeToDaddy plugin ) {
        super( plugin, "recipes" );
    }

    private @NotNull String quote( @NotNull String id ) {
        return "\"" + id + "\"";
    }

    private boolean verifyKeys( @NotNull String id ) {
        ConfigurationSection recipeSection = get().getConfigurationSection( id );
        Preconditions.checkNotNull( recipeSection );

        boolean containsAll = recipeSection.getKeys( true ).containsAll( REQUIRED_PATH );

        if ( !containsAll ) {

            List<String> missingKeys = new ArrayList<>( REQUIRED_PATH );
            missingKeys.removeAll( recipeSection.getKeys( true ) );

            String[] missing = missingKeys.toArray( String[]::new );
            Logger.error( "These paths are missing from recipe " + quote( id ) );
            Logger.error( "Missing key(s): " + Arrays.toString( missing ) );
        }

        return containsAll;
    }

    private void registerRecipe( @NotNull String id ) {
        ConfigurationSection recipeSection = get().getConfigurationSection( id );
        Preconditions.checkNotNull( recipeSection );

        Recipe recipe = new Recipe( id );
        recipe.getShape().addAll( recipeSection.getStringList( "recipe.shape" ) );

        ConfigurationSection ingredients = recipeSection.getConfigurationSection( "recipe.ingredients" );
        if ( ingredients == null )
            throw new NullPointerException( "recipe.ingredients of " + quote( id ) + " is null!" );
        else
            recipe.setIngredients( ingredients );

        MagnetItem result = recipe.getResult();

        ConfigurationSection properties = recipeSection.getConfigurationSection( "properties" );
        if ( properties == null )
            throw new NullPointerException( "properties of " + quote( id ) + " is null!" );
        else
            result.setProperties( properties );

        ConfigurationSection description = recipeSection.getConfigurationSection( "result" );
        if ( description == null )
            throw new NullPointerException( "result of " + quote( id ) + " is null!" );
        else
            result.setDescription( description );

        DataHandler.push( result, result.getProperties() );
        Bukkit.getServer().addRecipe( recipe.getRecipe( plugin ) );
    }

    @Override
    public void reload() {
        super.reload();

        ConfigurationSection section = get().getConfigurationSection( "" );
        if ( section == null ) {
            Logger.warn( "No recipe found! Please check your recipes.yml file" );
            return;
        }

        for (String id : section.getKeys( false )) {
            if ( !get().isConfigurationSection( id ) ) {
                Logger.error( quote( id ) + " is NOT a valid recipe!" );
                return;
            }

            if ( verifyKeys( id ) )
                registerRecipe( id );
        }

    }
}
