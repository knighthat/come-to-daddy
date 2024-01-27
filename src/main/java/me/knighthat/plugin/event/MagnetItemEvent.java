package me.knighthat.plugin.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class MagnetItemEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    private final ItemStack item;
    private       boolean   isCancelled;

    public MagnetItemEvent( @NotNull Player who, @NotNull ItemStack item ) {
        super( who, false );
        this.item = item;
    }

    public @NotNull ItemStack getItem() {return this.item;}

    @Override
    public boolean isCancelled() {return this.isCancelled;}

    @Override
    public void setCancelled( boolean b ) {this.isCancelled = b;}

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
