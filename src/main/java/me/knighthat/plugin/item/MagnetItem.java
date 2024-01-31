package me.knighthat.plugin.item;

import me.knighthat.plugin.data.DataHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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
        super( Material.IRON_INGOT, 1 );

        editMeta( meta -> {
            // Display name
            meta.displayName( color( "&d&lMagnet" ) );

            // Lore
            List<Component> lore = new ArrayList<>( 1 );
            lore.add( color( "&8[Shift + Right-click] &7to toggle!" ) );
            meta.lore( lore );

            // Disable descriptions
            meta.addItemFlags( ItemFlag.values() );

            // Make it unbreakable
            meta.setUnbreakable( true );

            // Inject custom Persistent Data
            DataHandler.inject( meta, false );
        } );
    }

    private @NotNull Component color( @NotNull String s ) {
        s = s.replace( "%tier%", properties.getTierName() );
        return LegacyComponentSerializer.legacyAmpersand().deserialize( s );
    }

    private boolean isNegativeNumber( @Nullable Object obj ) {
        if ( obj == null )
            return false;

        String objStr = String.valueOf( obj );
        try {
            return Double.parseDouble( objStr ) < 0d;
        } catch ( NumberFormatException e ) {
            Logger.error( objStr + " is not a number!" );
            return false;
        }
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

        if ( isNegativeNumber( properties.get( "x" ) ) ||
             isNegativeNumber( properties.get( "y" ) ) ||
             isNegativeNumber( properties.get( "z" ) ) )
            return;

        MagnetProperties.Area area = this.properties.getArea();
        area.setX( properties.getDouble( "x" ) );
        area.setY( properties.getDouble( "y" ) );
        area.setZ( properties.getDouble( "z" ) );
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
