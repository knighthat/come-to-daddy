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

    /**
     * This list contains must have paths.
     */
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

    /**
     * Put the provided string to be quoted.
     * For example, "string" will become "\"string\"".
     *
     * @param id string to add quotes
     *
     * @return new string with two escaped double quotes at the beginning and the end.
     */
    private @NotNull String quote( @NotNull String id ) { return "\"" + id + "\""; }

    /**
     * This function will verify if the provided tier id has all the REQUIRED_PATH.
     * The plugin will show an error message if any of these paths is missing from 'recipes.yml'.
     *
     * @param recipeSection configuration section that contains recipe
     *
     * @return whether the section contains all the necessary paths
     */
    private boolean verifyKeys( @NotNull ConfigurationSection recipeSection ) {
        /*
         * If 'recipeSection' doesn't have enough paths, the return value is false.
         * Excessive paths from recipeSection will not affect the return result.
         */
        boolean containsAll = recipeSection.getKeys( true ).containsAll( REQUIRED_PATH );

        if ( !containsAll ) {

            /*
             * Since 'REQUIRED_PATHS' is @Unmodifiable.
             * A new list must be created.
             *
             * This will also prevent the accidental modification
             * to the actual list causing unwanted results.
             */
            List<String> missingKeys = new ArrayList<>( REQUIRED_PATH );
            /*
             * List.removeAll() only remove what is possible.
             * If value doesn't exist in the list, it'll skip it.
             */
            missingKeys.removeAll( recipeSection.getKeys( true ) );

            /*
             * Java has a built-in function Arrays.toString() that convert an array
             * object to '[obj1,obj2,obj3,...]' string
             */
            String[] missing = missingKeys.toArray( String[]::new );
            Logger.error( "These paths are missing from recipe " + quote( id ) );
            Logger.error( "Missing key(s): " + Arrays.toString( missing ) );
        }

        return containsAll;
    }

    /**
     * This function will extract recipe's information and compile it
     * into an actual CraftingRecipe with MagnetItem being a result.
     *
     * @param recipeSection configuration section that contains recipe
     */
    private void registerRecipe( @NotNull ConfigurationSection recipeSection ) {
        Recipe recipe = new Recipe( recipeSection.getName() );
        MagnetItem result = recipe.getResult();

        /*
         * 'recipe.ingredients', 'properties', and 'result' paths are handled
         * externally by either Recipe or MagnetItem.
         *
         * This for-loop is here to prevent duplicate code (see commit 55ed7f3)
         */
        String[] paths = { "recipe.ingredients", "properties", "result" };
        for (String p : paths) {

            ConfigurationSection section = recipeSection.getConfigurationSection( p );
            if ( section == null ) {
                Logger.error( "%s.%s is null!".formatted( recipe.getId(), p ) );
                // Skip the registration if one of the paths is missing.
                return;
            }

            if ( p.equals( paths[0] ) )
                recipe.setIngredients( section );
            else if ( p.equals( paths[1] ) )
                result.setProperties( section );
            else
                result.setDescription( section );
        }
        recipe.getShape().addAll( recipeSection.getStringList( "recipe.shape" ) );

        /*
         * Minecraft Server throws IllegalArgumentException
         * if result.getType() returns Material.AIR
         * To prevent this, return from here if item is AIR
         */
        //if ( result.getType() == Material.AIR )
        //    return;

        // Encode MagnetProperties to MagnetItem using PersistentData
        DataHandler.push( result, result.getProperties() );
        Bukkit.getServer().addRecipe( recipe.getRecipe( plugin ) );
    }

    @Override
    public void reload() {
        super.reload();

        // Only applicable if recipe.yml is empty
        ConfigurationSection section = get().getConfigurationSection( "" );
        if ( section == null ) {
            Logger.warn( "No recipe found! Please check your recipes.yml file" );
            return;
        }

        /*
         * For each section (id) we check if it's valid (contains all required paths).
         * And then pass it in registerRecipe for registration
         */
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
