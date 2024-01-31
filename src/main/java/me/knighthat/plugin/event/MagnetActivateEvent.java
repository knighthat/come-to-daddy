package me.knighthat.plugin.event;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MagnetActivateEvent extends MagnetItemEvent {

    public MagnetActivateEvent( @NotNull Player who, @NotNull ItemStack item ) { super( who, item ); }
}
