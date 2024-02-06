package me.knighthat.plugin.utils;

import me.knighthat.plugin.item.MagnetItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Debuggable {

    private @NotNull Component color( @NotNull String s ) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize( s );
    }

    private @NotNull Component toComponent( @Nullable Object obj ) {
        if ( obj instanceof ComponentLike component )
            return component.asComponent();
        else if ( obj != null )
            return color( obj.toString() );
        else
            return Component.empty();
    }

    private @NotNull Component toComponent( @Nullable List<?> list ) {
        if ( list == null )
            return Component.empty();

        TextComponent.Builder builder = Component.text();
        builder.append( color( "[" ) );

        for (int i = 0 ; i < list.size() ; i++) {

            builder.append( toComponent( list.get( i ) ) );

            if ( i < list.size() - 1 )
                builder.append( Component.text( "," ) );
        }

        builder.append( color( "]" ) );
        return builder.build();
    }

    private @NotNull Component toComponent( @Nullable Map<?, ?> map, @NotNull String delimiter ) {
        if ( map == null )
            return Component.empty();

        TextComponent.Builder builder = Component.text();

        int count = 0;
        for (Map.Entry<?, ?> entry : map.entrySet()) {

            builder.append( toComponent( entry.getKey() ) )
                   .append( color( delimiter ) )
                   .append( toComponent( entry.getValue() ) );

            if ( count < map.size() - 1 )
                builder.append( color( "," ) );

            count++;
        }

        return builder.build();

    }

    private @NotNull Component toComponent( @NotNull MagnetItem item ) {
        TextComponent.Builder builder = Component.text();

        List<Component> children = item.toComponent().children();
        for (int i = 0 ; i < children.size() ; i++) {

            if ( i == 2 ) {
                builder.append(
                        color( "name=" ).append( item.displayName() ).append( color( "," ) ),
                        color( "lore=" ).append( toComponent( item.lore() ) ).append( color( "," ) )
                );
            }

            builder.append( children.get( i ) );
        }

        return builder.build();
    }

    private @NotNull Component toComponent( @NotNull Field field ) throws IllegalAccessException {
        if ( field.getType().isEnum() ) {

            return color( ((Enum<?>) field.get( this )).name() );

        } else if ( MagnetItem.class.isAssignableFrom( field.getType() ) ) {

            return toComponent( (MagnetItem) field.get( this ) );

        } else if ( Debuggable.class.isAssignableFrom( field.getType() ) ) {

            return ((Debuggable) field.get( this )).toComponent();

        } else if ( List.class.isAssignableFrom( field.getType() ) ) {

            return toComponent( (List<?>) field.get( this ) );

        } else if ( Map.class.isAssignableFrom( field.getType() ) ) {

            Component map = toComponent( (Map<?, ?>) field.get( this ), ":" );
            return color( "{" ).append( map ).append( color( "}" ) );

        } else {
            return color( field.get( this ).toString() );
        }
    }

    default @NotNull Component toComponent() {
        Map<String, Component> fieldComponents = new HashMap<>();

        for (Field field : getClass().getDeclaredFields()) {
            if ( Modifier.isStatic( field.getModifiers() ) )
                continue;

            field.setAccessible( true );

            try {
                fieldComponents.put( field.getName(), toComponent( field ) );
            } catch ( Exception ignored ) {
            }
        }

        return Component.empty()
                        .append( color( getClass().getSimpleName() ) )
                        .append( color( "(" ) )
                        .append( toComponent( fieldComponents, "=" ) )
                        .append( color( ")" ) );
    }
}
