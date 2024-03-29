package me.knighthat.plugin.event;

import me.knighthat.plugin.ComeToDaddy;
import me.knighthat.plugin.data.DataHandler;
import me.knighthat.plugin.item.MagnetItem;
import me.knighthat.plugin.item.MagnetProperties;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EventListener implements Listener {

    private static final @NotNull Set<Player> USING_MAGNET;

    static {
        USING_MAGNET = new HashSet<>();
    }

    @NotNull
    private final ComeToDaddy plugin;

    public EventListener( @NotNull ComeToDaddy plugin ) {
        this.plugin = plugin;
    }

    private @NotNull Component color( @NotNull String s ) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize( s );
    }

    private void collect( @NotNull Player player, double x, double y, double z ) {
        List<ItemStack> leftOvers = new ArrayList<>();
        /*
         * Here we check for nearby items, then attempt to put
         * each of them into the player's inventory. Inventory#addItem()
         * will return any item that can't be fit into the inventory.
         */
        for (Entity entity : player.getNearbyEntities( x, y, z )) {
            if ( !(entity instanceof Item item) )
                continue;

            ItemStack itemStack = item.getItemStack();
            leftOvers.addAll( player.getInventory().addItem( itemStack ).values() );

            item.remove();
        }

        /*
         * Items can't be put inside player's inventory will be 'gathered'
         * at that player's feet.
         */
        for (ItemStack item : leftOvers)
            player.getWorld().dropItem( player.getLocation(), item );
    }

    private void scheduleTask( @NotNull Player player, @NotNull MagnetProperties.Area area ) {
        USING_MAGNET.add( player );
        Bukkit.getScheduler().runTaskTimer(
                plugin,
                task -> {
                    /*
                     * If the player is no longer only or present in the 'USING_MAGNET' set,
                     * then the task should cancel itself.
                     */
                    if ( !USING_MAGNET.contains( player ) || !player.isOnline() ) {
                        task.cancel();
                        return;
                    }

                    collect( player, area.getX(), area.getY(), area.getZ() );
                },
                0L,
                5L
        );
    }

    /*
     * Triggers when player holds and right-clicks {@link me.knighthat.plugin.item.}
     */
    @Deprecated
    @EventHandler( priority = EventPriority.LOW )
    public void onShiftRightClick( @NotNull PlayerInteractEvent event ) {
        Player player = event.getPlayer();

        // Proceed only when player right-clicks and is crouching
        if ( !event.getAction().isRightClick() || !player.isSneaking() )
            return;
        // Check if item is plugin's Magnet
        ItemStack inHand = event.getItem();
        if ( !MagnetItem.isPluginItem( inHand ) )
            return;
        event.setCancelled( true );

        boolean isEnabled = !DataHandler.extract( inHand );

        MagnetItemEvent itemEvent = isEnabled ?
                new MagnetActivateEvent( player, inHand ) :
                new MagnetDeactivateEvent( player, inHand );
        Bukkit.getServer().getPluginManager().callEvent( itemEvent );

        if ( itemEvent.isCancelled() )
            return;

        DataHandler.inject( inHand, isEnabled );
        event.getPlayer().updateInventory();

        // Send deactivate message
        String msgPath = isEnabled ? "activate" : "deactivate";
        Component message = plugin.getMessages().message( msgPath );
        event.getPlayer().sendMessage( message );

        // This message only shows when player uses old magnet
        player.sendMessage( color( "&cThis magnet is deprecated and soon will be useless!" ) );
    }

    /*
     * When a player activates his/her magnet, this handler
     * will create a repeated task that attracts items around him/her,
     * then put that player to 'USING_MAGNET' set.
     */

    @EventHandler( priority = EventPriority.HIGHEST )
    public void onPlayerShiftRightClick( @NotNull PlayerInteractEvent event ) {
        Player player = event.getPlayer();
        ItemStack inHand = event.getItem();

        // Only proceed if a player shifts + right-clicks while holding MagnetItem
        if ( !event.getAction().isRightClick() ||
             !player.isSneaking() ||
             !MagnetItem.hasProperties( inHand ) ) {
            return;
        } else
            event.setCancelled( true );

        MagnetProperties properties = DataHandler.pull( inHand );
        boolean isEnabled = !properties.isActivated();

        MagnetItemEvent itemEvent = isEnabled ?
                new MagnetActivateEvent( player, inHand ) :
                new MagnetDeactivateEvent( player, inHand );
        Bukkit.getServer().getPluginManager().callEvent( itemEvent );
        if ( itemEvent.isCancelled() )
            return;

        properties.setActivated( isEnabled );
        DataHandler.push( inHand, properties );
        event.getPlayer().updateInventory();

        // Send (de)activate message
        String msgPath = isEnabled ? "activate" : "deactivate";
        Component message = plugin.getMessages().message( msgPath );
        event.getPlayer().sendMessage( message );
    }

    @EventHandler
    public void onPlayerActivateMagnet( @NotNull MagnetActivateEvent event ) {
        MagnetProperties.Area area;
        if ( MagnetItem.isPluginItem( event.getItem() ) )
            /*
             * This block handles old magnet item
             * However, this is marked deprecated and will
             * no longer getting update.
             * In the future, deprecated item will be assigned
             * new PersistentData of MagnetProperty.DEFAULT which
             * does not attract items.
             */
            area = new MagnetProperties.Area( 5d, 5d, 5d );
        else if ( MagnetItem.hasProperties( event.getItem() ) )
            area = DataHandler.pull( event.getItem() ).getArea();
        else
            /*
             * This block just a precaution step to ensure 'area'
             * isn't null.
             * The DEFAULT area is 0.
             */
            area = MagnetProperties.DEFAULT.getArea();

        scheduleTask( event.getPlayer(), area );
        event.getItem().editMeta( meta -> meta.addEnchant( Enchantment.DURABILITY, 1, true ) );
    }

    @EventHandler
    public void onPlayerDeactivateMagnet( @NotNull MagnetDeactivateEvent event ) {
        /*
         * Since the task already checks if the player still present in the
         * 'USING_MAGNET' set. Removing player from the set will result in the task getting canceled
         */
        USING_MAGNET.remove( event.getPlayer() );
        event.getItem().editMeta( ItemMeta::removeEnchantments );
    }

    @EventHandler
    public void onPlayerLogin( @NotNull PlayerJoinEvent event ) {
        /*
         * When a player joins the server, we want to go through
         * his/her inventory and look for MagnetItem by checking
         * PersistentDataContainer for traces of this plugin.
         *
         * Then create and call an appropriate MagnetEvent.
         *
         * If the item contains neither, we move on to the next item.
         */

        Player player = event.getPlayer();
        for (ItemStack item : player.getInventory().getContents()) {
            if ( item == null || item.getType() == Material.AIR )
                continue;

            boolean isEnabled;
            if ( MagnetItem.isPluginItem( item ) )
                isEnabled = DataHandler.extract( item );
            else if ( MagnetItem.hasProperties( item ) )
                isEnabled = DataHandler.pull( item ).isActivated();
            else
                continue;

            MagnetItemEvent itemEvent = isEnabled ?
                    new MagnetActivateEvent( player, item ) :
                    new MagnetDeactivateEvent( player, item );
            Bukkit.getServer().getPluginManager().callEvent( itemEvent );
        }
    }

    /*
     * A bug was introduced after extensive testing.
     *
     * Description: When a player dies with 'MagnetItem' activated,
     * all of his/her inventory will be restored at the time of respawn.
     * This happens due to the ongoing task, and the player is still present
     * in 'USING_MAGNET' set.
     *
     * Solution: Remove player from the set at the time of death by calling
     * MagnetDeactivateEvent
     */
    @EventHandler
    public void onPlayerDeath( @NotNull PlayerDeathEvent event ) {
        if ( event.getKeepInventory() )
            return;

        for (ItemStack item : event.getDrops()) {
            if ( !MagnetItem.isPluginItem( item ) )
                continue;

            boolean isEnabled = DataHandler.extract( item );
            if ( !isEnabled )
                break;

            DataHandler.inject( item, false );
            Bukkit.getServer()
                  .getPluginManager()
                  .callEvent( new MagnetDeactivateEvent( event.getPlayer(), item ) );

            break;
        }
    }
}
