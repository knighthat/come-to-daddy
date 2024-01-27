package me.knighthat.plugin.data;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class DataHandler {

    @NotNull
    public static NamespacedKey KEY;

    public static void inject( @NotNull PersistentDataContainer container, boolean isEnabled ) {
        container.set( KEY, PersistentDataType.BOOLEAN, isEnabled );
    }

    public static void inject( @NotNull ItemMeta meta, boolean isEnabled ) {
        inject( meta.getPersistentDataContainer(), isEnabled );
    }

    public static void inject( @NotNull ItemStack item, boolean isEnabled ) {
        item.editMeta( meta -> inject( meta, isEnabled ) );
    }

    public static boolean extract( @NotNull PersistentDataContainer container ) {
        return Boolean.TRUE.equals( container.get( KEY, PersistentDataType.BOOLEAN ) );
    }

    public static boolean extract( @NotNull ItemMeta meta ) {
        return extract( meta.getPersistentDataContainer() );
    }

    public static boolean extract( @NotNull ItemStack item ) {
        return extract( item.getItemMeta() );
    }
}
