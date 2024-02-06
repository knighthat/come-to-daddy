package me.knighthat.plugin.file;

import lombok.Getter;
import me.knighthat.plugin.ComeToDaddy;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

@Getter
public class MessageFile extends PluginFile {

    @NotNull
    private static Component prefix;

    public MessageFile( @NotNull ComeToDaddy plugin ) { super( plugin, "messages" ); }

    private @NotNull TextComponent color( @NotNull String s ) { return LegacyComponentSerializer.legacyAmpersand().deserialize( s ); }

    public @NotNull Component message( @NotNull String path ) {
        String msgRaw = get().getString( path, "" );
        return prefix.append( color( msgRaw ) );
    }

    public void sendMessage( @NotNull Audience audience, @NotNull String messagePath ) {
        audience.sendMessage( message( messagePath ) );
    }

    @Override
    public void reload() {
        super.reload();
        prefix = color( get().getString( "prefix", "" ) );
    }
}
