package me.knighthat.plugin.command.sub;

import me.knighthat.plugin.ComeToDaddy;
import me.knighthat.plugin.recipe.Recipe;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class DebugCommand extends SubCommand {

    public DebugCommand( @NotNull ComeToDaddy plugin ) { super( plugin ); }

    @Override
    public @NotNull String getName() { return "debug"; }

    @Override
    public @NotNull String getPermission() { return "debug"; }

    @Override
    public void execute( @NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args ) {
        if ( args.length < 2 ) {
            for (Recipe recipe : Recipe.RECIPES.values())
                sender.sendMessage( recipe.toComponent() );
        } else {
            for (Recipe recipe : Recipe.RECIPES.values())
                if ( recipe.getId().equalsIgnoreCase( args[1] ) )
                    sender.sendMessage( recipe.toComponent() );
        }
    }
}
