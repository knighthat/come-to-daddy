package me.knighthat.plugin.data;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public class DataHandler {

    @NotNull
    @Deprecated( since = "0.2.0", forRemoval = true )
    @ApiStatus.ScheduledForRemoval( inVersion = "0.4.0" )
    public static NamespacedKey KEY;

    @Deprecated( since = "0.2.0", forRemoval = true )
    @ApiStatus.ScheduledForRemoval( inVersion = "0.4.0" )
    public static void inject( @NotNull PersistentDataContainer container, boolean isEnabled ) {
        container.set( KEY, PersistentDataType.BOOLEAN, isEnabled );
    }

    @Deprecated( since = "0.2.0", forRemoval = true )
    @ApiStatus.ScheduledForRemoval( inVersion = "0.4.0" )
    public static void inject( @NotNull ItemMeta meta, boolean isEnabled ) {
        inject( meta.getPersistentDataContainer(), isEnabled );
    }

    @Deprecated( since = "0.2.0", forRemoval = true )
    @ApiStatus.ScheduledForRemoval( inVersion = "0.4.0" )
    public static void inject( @NotNull ItemStack item, boolean isEnabled ) {
        item.editMeta( meta -> inject( meta, isEnabled ) );
    }

    @Deprecated( since = "0.2.0", forRemoval = true )
    @ApiStatus.ScheduledForRemoval( inVersion = "0.4.0" )
    public static boolean extract( @NotNull PersistentDataContainer container ) {
        return Boolean.TRUE.equals( container.get( KEY, PersistentDataType.BOOLEAN ) );
    }

    @Deprecated( since = "0.2.0", forRemoval = true )
    @ApiStatus.ScheduledForRemoval( inVersion = "0.4.0" )
    public static boolean extract( @NotNull ItemMeta meta ) {
        return extract( meta.getPersistentDataContainer() );
    }

    @Deprecated( since = "0.2.0", forRemoval = true )
    @ApiStatus.ScheduledForRemoval( inVersion = "0.4.0" )
    public static boolean extract( @NotNull ItemStack item ) {
        return extract( item.getItemMeta() );
    }
}
