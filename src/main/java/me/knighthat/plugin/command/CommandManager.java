package me.knighthat.plugin.command;

import me.knighthat.plugin.ComeToDaddy;
import me.knighthat.plugin.command.sub.ReloadCommand;
import me.knighthat.plugin.command.sub.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CommandManager implements CommandExecutor {

    @NotNull
    private static final Set<SubCommand> SUB_COMMANDS = new HashSet<>( 1 );

    @NotNull
    private final ComeToDaddy plugin;

    public CommandManager( @NotNull ComeToDaddy plugin ) {
        this.plugin = plugin;

        SUB_COMMANDS.add( new ReloadCommand( plugin ) );
    }

    @Override
    public boolean onCommand( @NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args ) {
        if ( args.length == 0 ) {
            sender.sendMessage(
                    plugin.getMessages()
                          .message( "command_usage" )
                          .replaceText( builder -> builder.matchLiteral( "%cmd%" ).replacement( alias ) )
            );
            return true;
        }

        Iterator<SubCommand> subcommands = SUB_COMMANDS.iterator();
        while (subcommands.hasNext()) {
            SubCommand sub = subcommands.next();

            if ( !sub.getName().equalsIgnoreCase( args[0] ) )
                continue;

            if ( !sub.hasPermission( sender ) ) {
                plugin.getMessages().sendMessage( sender, "no_permission" );
                continue;
            }

            sub.execute( sender, command, alias, args );
        }

        return true;
    }
}
