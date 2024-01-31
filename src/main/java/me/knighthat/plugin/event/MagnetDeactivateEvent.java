package me.knighthat.plugin.event;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MagnetDeactivateEvent extends MagnetItemEvent {

    public MagnetDeactivateEvent( @NotNull Player who, @NotNull ItemStack item ) { super( who, item ); }
}
