package me.knighthat.plugin.item;

import lombok.Getter;
import me.knighthat.plugin.data.DataHandler;
import me.knighthat.plugin.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MagnetItem extends ItemStack {

    public static boolean isPluginItem( @Nullable ItemStack item ) {
        if ( item == null )
            return false;

        return item.getItemMeta()
                   .getPersistentDataContainer()
                   .has( DataHandler.KEY, PersistentDataType.BOOLEAN );
    }

    public static boolean hasProperties( @Nullable ItemStack item ) {
        if ( item == null )
            return false;

        return item.getItemMeta()
                   .getPersistentDataContainer()
                   .has( DataHandler.PROPERTY_KEY, DataHandler.PROPERTY_TYPE );
    }

    @Getter
    @NotNull
    private final MagnetProperties properties;

    public MagnetItem() {
        super( Material.AIR, 1 );
        this.properties = new MagnetProperties();

        editMeta( meta -> {
            // Disable descriptions
            meta.addItemFlags( ItemFlag.values() );

            // Make it unbreakable
            meta.setUnbreakable( true );
        } );
    }

    private @NotNull Component color( @NotNull String s ) {
        s = s.replace( "%tier%", properties.getTierName() );
        return LegacyComponentSerializer.legacyAmpersand().deserialize( s );
    }

    private boolean isValidNumber( @Nullable Object obj ) {
        // Always assume the input object is NOT a positive number.
        boolean result = false;

        // If obj is null, then it'll return default value.
        if ( obj != null ) {
            String objStr = String.valueOf( obj );
            try {
                result = Double.parseDouble( objStr ) > 0d;
            } catch ( NumberFormatException ignored ) {
            }
        }

        return result;
    }

    public void setDescription( @NotNull ConfigurationSection description ) {
        String materialStr = description.getString( "material", "" );
        Material material = Material.matchMaterial( materialStr );
        if ( material == null )
            throw new IllegalArgumentException( materialStr + " is NOT a valid material!" );
        setType( material );

        String name = description.getString( "name", "" );
        List<String> lore = description.getStringList( "lore" );

        editMeta( meta -> {
            meta.displayName( color( name ) );
            meta.lore(
                    lore.stream()
                        .map( this::color )
                        .toList()
            );
        } );
    }

    public void setProperties( @NotNull ConfigurationSection properties ) {
        String tierName = properties.getString( "tier_name", "" );
        this.properties.setTierName( tierName );

        MagnetProperties.Area area = this.properties.getArea();
        for (String axis : new String[]{ "x", "y", "z" }) {

            Object obj = properties.get( axis );
            if ( !isValidNumber( obj ) ) {
                String path = properties.getCurrentPath() + "." + axis;
                Logger.error( "Value of " + path + " must be a positive number! (" + obj + ")" );
                return;
            }

            double value = Double.parseDouble( String.valueOf( obj ) );
            if ( value < 0 )
                throw new NumberFormatException();

            switch (axis) {
                case "x" -> area.setX( value );
                case "y" -> area.setY( value );
                case "z" -> area.setZ( value );
            }
        }
    }

    public @NotNull Component toComponent() {
        return Component.text( "MagnetItem[material=%material,name=%name,lore=[%lore],properties=%properties]" )
                        .replaceText( builder -> builder.matchLiteral( "%material" ).replacement( getType().name() ) )
                        .replaceText( builder -> builder.matchLiteral( "%name" ).replacement( displayName() ) )
                        .replaceText( builder -> {
                            if ( lore() == null )
                                return;

                            TextComponent.Builder lore = Component.text();
                            for (Component line : lore())
                                lore.append( line )
                                    .append( Component.text( ", " ) );

                            builder.matchLiteral( "%lore" ).replacement( lore.build() );
                        } )
                        .replaceText( builder -> builder.matchLiteral( "%properties" ).replacement( properties.toComponent() ) );
    }
}
