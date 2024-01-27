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
        return LegacyComponentSerializer.legacyAmpersand().deserialize( s );
    }
}
