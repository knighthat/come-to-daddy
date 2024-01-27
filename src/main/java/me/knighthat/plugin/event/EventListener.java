package me.knighthat.plugin.event;

import me.knighthat.plugin.ComeToDaddy;
import me.knighthat.plugin.data.DataHandler;
import me.knighthat.plugin.item.MagnetItem;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
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

    /*
     * Triggers when player holds and right-clicks {@link me.knighthat.plugin.item.}
     */
    @EventHandler( priority = EventPriority.HIGHEST )
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
    }

    /*
     * When a player activates his/her magnet, this handler
     * will create a repeated task that attracts items around him/her,
     * then put that player to 'USING_MAGNET' set.
     */
    @EventHandler
    public void onPlayerActivateMagnet( @NotNull MagnetActivateEvent event ) {
        Player player = event.getPlayer();

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

                    List<ItemStack> leftOvers = new ArrayList<>();
                    /*
                     * Here we check for nearby items, then attempt to put
                     * each of them into the player's inventory. Inventory#addItem()
                     * will return any item that can't be fit into the inventory.
                     */
                    for (Entity entity : player.getNearbyEntities( 5d, 5d, 5d )) {
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
                },
                0L,
                5L
        );

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
        Player player = event.getPlayer();
        for (ItemStack item : player.getInventory().getContents())
            if ( MagnetItem.isPluginItem( item ) ) {

                boolean isEnabled = DataHandler.extract( item );
                MagnetItemEvent itemEvent = isEnabled ?
                        new MagnetActivateEvent( player, item ) :
                        new MagnetDeactivateEvent( player, item );
                Bukkit.getServer().getPluginManager().callEvent( itemEvent );
            }
    }
}
