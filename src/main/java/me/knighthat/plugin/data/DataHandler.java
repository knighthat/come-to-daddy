package me.knighthat.plugin.data;

import me.knighthat.plugin.item.MagnetProperties;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public class DataHandler {

    @NotNull
    public static final PropertyDataType PROPERTY_TYPE;
    public static       NamespacedKey    PROPERTY_KEY;

    @NotNull
    @Deprecated( since = "0.2.0", forRemoval = true )
    @ApiStatus.ScheduledForRemoval( inVersion = "0.4.0" )
    public static NamespacedKey KEY;

    static {
        PROPERTY_TYPE = new PropertyDataType();
    }

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

    /**
     * Inject MagnetProperties to the provided container
     *
     * @param container  PersistentDataContainer that holds serialized data
     * @param properties MagnetProperties class to serialize and store inside container
     */
    public static void push( @NotNull PersistentDataContainer container, @NotNull MagnetProperties properties ) { container.set( PROPERTY_KEY, PROPERTY_TYPE, properties ); }

    /**
     * Inject MagnetProperties to the provided ItemMeta.
     * Remember to update ItemStack with ItemStack.setItemMeta().
     *
     * @param meta       PersistentDataContainer holder
     * @param properties MagnetProperties class to serialize and store inside container
     */
    public static void push( @NotNull ItemMeta meta, @NotNull MagnetProperties properties ) { push( meta.getPersistentDataContainer(), properties ); }

    /**
     * Inject MagnetProperties to the provided ItemStack.
     * This function automatically updates ItemStack once the injection is done.
     *
     * @param item       ItemStack to inject MagnetProperties
     * @param properties MagnetProperties class to serialize and store inside container
     */
    public static void push( @NotNull ItemStack item, @NotNull MagnetProperties properties ) { item.editMeta( meta -> push( meta, properties ) ); }

    /**
     * Extract MagnetProperties from the provided container.
     *
     * @param container PersistentDataContainer that holds serialized data
     *
     * @return either serialized data stored inside container or MagnetProperties#DEFAULT
     *
     * @see MagnetProperties
     */
    public static @NotNull MagnetProperties pull( @NotNull PersistentDataContainer container ) { return container.getOrDefault( PROPERTY_KEY, PROPERTY_TYPE, MagnetProperties.DEFAULT ); }

    /**
     * Extract MagnetProperties from the provided ItemMeta.
     *
     * @param meta PersistentDataContainer holder
     *
     * @return either serialized data stored inside container or MagnetProperties#DEFAULT
     *
     * @see MagnetProperties
     */
    public static @NotNull MagnetProperties pull( @NotNull ItemMeta meta ) { return pull( meta.getPersistentDataContainer() ); }

    /**
     * Extract MagnetProperties from the provided ItemStack.
     *
     * @param item ItemStack to extract MagnetProperties
     *
     * @return either serialized data stored inside container or MagnetProperties#DEFAULT
     *
     * @see MagnetProperties
     */
    public static @NotNull MagnetProperties pull( @NotNull ItemStack item ) { return pull( item.getItemMeta() ); }
}
