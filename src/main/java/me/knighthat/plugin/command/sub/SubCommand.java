package me.knighthat.plugin.command.sub;

import lombok.AccessLevel;
import lombok.Getter;
import me.knighthat.plugin.ComeToDaddy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;

public abstract class SubCommand {

    @Getter( AccessLevel.PROTECTED )
    @NotNull
    private final ComeToDaddy plugin;

    public SubCommand( @NotNull ComeToDaddy plugin ) {
        this.plugin = plugin;
    }

    public abstract @NotNull String getName();

    public abstract @NotNull String getPermission();

    public abstract void execute( @NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args );

    public boolean hasPermission( @NotNull Permissible target, @NotNull String permission ) {
        return target.hasPermission( permission ) || target.isOp();
    }

    public boolean hasPermission( @NotNull Permissible target ) {
        return hasPermission( target, "cometodaddy.command." + getPermission() );
    }
}
