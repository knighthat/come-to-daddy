package me.knighthat.plugin.recipe;

import lombok.Getter;
import lombok.Setter;
import me.knighthat.plugin.item.MagnetItem;
import me.knighthat.plugin.logging.Logger;
import me.knighthat.plugin.utils.Debuggable;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ShapedRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Recipe implements Debuggable {

    @NotNull
    public static final Map<String, Recipe> RECIPES = new HashMap<>();

    @NotNull
    private final String                   id;
    @NotNull
    private final CraftingType             type;
    @NotNull
    private final List<String>             shape;
    @NotNull
    private final Map<Character, Material> ingredients;
    @NotNull
    private final MagnetItem               result;

    @Setter
    private boolean valid;

    public Recipe( @NotNull String id ) {
        this.id = id;
        this.type = CraftingType.CRAFTING;
        this.shape = new ArrayList<>();
        this.ingredients = new HashMap<>();
        this.result = new MagnetItem();
        this.valid = true;
    }

    public void setIngredients( @NotNull ConfigurationSection ingredients ) {
        for (String key : ingredients.getKeys( false )) {
            if ( key.length() != 1 ) {
                Logger.error( key + " MUST be a single character!" );
                continue;
            }

            String materialRaw = ingredients.getString( key, "" );
            Material material = Material.getMaterial( materialRaw );
            if ( material == null ) {
                Logger.error( materialRaw + " is not a valid material" );
                Logger.error( "Visit https://jd.papermc.io/paper/1.20/org/bukkit/material/package-summary.html" );
                return;
            }

            this.ingredients.put( key.charAt( 0 ), material );
        }
    }

    public @NotNull CraftingRecipe getRecipe( @NotNull NamespacedKey namespacedKey ) {
        if ( shape.isEmpty() )
            throw new IllegalArgumentException( "No shape provided" );

        result.editMeta( meta -> {
            // Disable descriptions
            meta.addItemFlags( ItemFlag.values() );

            // Make it unbreakable
            meta.setUnbreakable( true );
        } );

        ShapedRecipe recipe = new ShapedRecipe( namespacedKey, result );
        recipe.shape( shape.toArray( new String[3] ) );
        ingredients.forEach( recipe::setIngredient );

        RECIPES.put( id, this );

        return recipe;
    }
}
