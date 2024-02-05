package me.knighthat.plugin.recipe;

import lombok.Getter;
import lombok.Setter;
import me.knighthat.plugin.item.MagnetItem;
import me.knighthat.plugin.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Recipe {

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

    public @NotNull CraftingRecipe getRecipe( @NotNull JavaPlugin plugin ) {
        if ( shape.isEmpty() )
            throw new IllegalArgumentException( "No shape provided" );

        result.editMeta( meta -> {
            // Disable descriptions
            meta.addItemFlags( ItemFlag.values() );

            // Make it unbreakable
            meta.setUnbreakable( true );
        } );

        NamespacedKey key = new NamespacedKey( plugin, id );

        ShapedRecipe recipe = new ShapedRecipe( key, result );
        recipe.shape( shape.toArray( new String[3] ) );
        ingredients.forEach( recipe::setIngredient );

        return recipe;
    }

    public @NotNull Component toComponent() {
        return Component.text( "Recipe[id=%id,type=%type,shape=[%shape],ingredients=[%ingredients],result=%result]" )
                        .replaceText( builder -> builder.matchLiteral( "%id" ).replacement( id ) )
                        .replaceText( builder -> builder.matchLiteral( "%type" ).replacement( type.name() ) )
                        .replaceText( builder -> {
                            TextComponent.Builder shape = Component.text();
                            for (String s : this.shape)
                                shape.append( Component.text( s ) )
                                     .append( Component.text( ", " ) );

                            builder.matchLiteral( "%shape" ).replacement( shape.build() );
                        } )
                        .replaceText( builder -> {
                            TextComponent.Builder ingredients = Component.text();
                            for (Map.Entry<Character, Material> entry : this.ingredients.entrySet()) {
                                ingredients.append( Component.text( entry.getKey() ) )
                                           .append( Component.text( ":" ) )
                                           .append( Component.text( entry.getValue().name() ) )
                                           .append( Component.text( ", " ) );

                            }
                            builder.matchLiteral( "%ingredients" ).replacement( ingredients.build() );
                        } )
                        .replaceText( builder -> builder.matchLiteral( "%result" ).replacement( result.toComponent() ) );
    }
}
