package me.knighthat.plugin.logging;

import org.jetbrains.annotations.NotNull;
import org.slf4j.event.Level;

public final class Logger {

    public static org.slf4j.Logger LOGGER;

    public static void log( @NotNull Level level, @NotNull String s ) {
        switch (level) {
            case ERROR -> LOGGER.error( s );
            case WARN -> LOGGER.warn( s );
            case INFO -> LOGGER.info( s );
            case DEBUG -> LOGGER.debug( s );
            case TRACE -> LOGGER.trace( s );
        }
    }

    public static void warn( @NotNull String s ) { log( Level.WARN, s ); }

    public static void error( @NotNull String s ) { log( Level.ERROR, s ); }

    public static void exception( @NotNull String s, @NotNull Exception e ) {
        if ( !s.isEmpty() )
            error( s );

        if ( !e.getMessage().isEmpty() )
            error( "Reason: " + e.getMessage() );
        
        if ( !e.getCause().getMessage().isEmpty() )
            error( "Cause: " + e.getCause().getMessage() );
    }
}
