package me.knighthat.plugin.command.sub;

import me.knighthat.plugin.ComeToDaddy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand extends SubCommand {

    public ReloadCommand( @NotNull ComeToDaddy plugin ) { super( plugin ); }

    @Override
    public @NotNull String getName() { return "reload"; }

    @Override
    public @NotNull String getPermission() { return "reload"; }

    @Override
    public void execute( @NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args ) {
        getPlugin().getMessages().reload();
        getPlugin().getRecipes().reload();

        getPlugin().getMessages().sendMessage( sender, "reload" );
    }
}
