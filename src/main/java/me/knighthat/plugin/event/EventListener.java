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

import java.util.*;

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

    /**
     * Get nearby Item entities of provided Entity.
     * This method ignores items that have their delay
     * pickup still active.
     *
     * @param of entity to check for nearby items
     * @param x  range on X axis
     * @param y  range on Y axis
     * @param z  range on Z axis
     *
     * @return a list of nearby items
     */
    private @NotNull List<Item> getNearbyItems( Entity of, double x, double y, double z ) {
        List<Item> items = new ArrayList<>();
        for (Entity entity : of.getNearbyEntities( x, y, z ))
            if ( entity instanceof Item item && item.getPickupDelay() <= 0 )
                items.add( item );

        return items;
    }


    /**
     * This algorithm will attempt to put a list of items
     * into the player's inventory.
     * <p>
     * Usually, Inventory#addItem() is sufficient, but there's
     * a small bug, for some reason; offhand isn't included
     * in the checking process, causing it to be completely
     * ignored.
     * <p>
     * This piece of code handles that missing check before
     * handing the rest of the items to Inventory#addItem().
     *
     * @param to    player whose inventory will be used to add items
     * @param items list of Item entity to add.
     *
     * @return a map that contains index and items that can't be put into player's inventory
     */
    private @NotNull Map<Integer, ItemStack> addItems( @NotNull Player to, @NotNull List<Item> items ) {
        ItemStack[] stacks = new ItemStack[items.size()];

        for (int i = 0 ; i < stacks.length ; i++) {
            ItemStack item = items.get( i ).getItemStack();
            ItemStack offHand = to.getInventory().getItemInOffHand();

            /*
             * Comparing ItemStacks is a little bit different
             * from other classes, ItemStack#equals() will not
             * return true most of the time (even when items
             * are "similar" and false when they are completely
             * different in properties, ItemMeta, etc.)
             *
             * Therefore, ItemStack#isSimilar() was born.
             * If current item in the list matched with the one
             * in player's offhand.
             * There are 2 scenarios that can happen:
             * 1. Current item's amount plus offhand's amount
             *    is less than the maximum stack size.
             *    If so, we just have to set offhand's amount to
             *    the combined value and set item's amount to 0
             *    and won't be added to 'stacks'.
             * 2. The combined amount exceeds max stack size.
             *    We can set offhand's amount to max stack size
             *    while setting item's amount to the leftover amount.
             */
            if ( offHand.isSimilar( item ) ) {
                int maxSize = offHand.getMaxStackSize();
                int combined = offHand.getAmount() + item.getAmount();

                offHand.setAmount( Math.min( combined, maxSize ) );
                item.setAmount( Math.max( combined - maxSize, 0 ) );

                to.updateInventory();
            }

            if ( item.getAmount() > 0 )
                stacks[i] = item;
        }

        return to.getInventory().addItem( stacks );
    }

    /**
     * When this algorithm is called, it'll check for all items
     * around the provided player (range are defined by x, y,
     * and z axis's).
     * The next step is to add those items into that player's inventory.
     * This function also handles playing pickup animation and
     * setting leftover items to the remaining amount if that player's
     * inventory is full.
     *
     * @param player to check for nearby
     * @param x      range on X axis
     * @param y      range on Y axis
     * @param z      range on Z axis
     */
    private void collect( @NotNull Player player, double x, double y, double z ) {
        List<Item> nearby = getNearbyItems( player, x, y, z );
        Map<Integer, ItemStack> leftOvers = addItems( player, nearby );

        for (int i = 0 ; i < nearby.size() ; i++) {
            Item item = nearby.get( i );
            ItemStack leftOver = leftOvers.get( i );

            player.playPickupItemAnimation( item, 0 );

            if ( leftOver != null ) {
                item.setItemStack( leftOver );
                item.teleport( player );
            } else
                item.remove();
        }
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
