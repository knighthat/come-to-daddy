package me.knighthat.plugin.data;

import me.knighthat.plugin.item.MagnetProperties;
import me.knighthat.plugin.logging.Logger;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.io.*;

public class PropertyDataType implements PersistentDataType<byte[], MagnetProperties> {

    @Override
    public @NotNull Class<byte[]> getPrimitiveType() { return byte[].class; }

    @Override
    public @NotNull Class<MagnetProperties> getComplexType() { return MagnetProperties.class; }

    @Override
    public byte @NotNull [] toPrimitive( @NotNull MagnetProperties properties, @NotNull PersistentDataAdapterContext persistentDataAdapterContext ) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream( baos );

            oos.writeObject( properties );
            oos.flush();

            return baos.toByteArray();
        } catch ( IOException e ) {
            Logger.exception( "Failed to encode properties!", e );
            return new byte[0];
        }
    }

    @Override
    public @NotNull MagnetProperties fromPrimitive( byte @NotNull [] bytes, @NotNull PersistentDataAdapterContext persistentDataAdapterContext ) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream( bytes );
            ObjectInputStream ois = new ObjectInputStream( bais );

            return (MagnetProperties) ois.readObject();
        } catch ( IOException | ClassNotFoundException e ) {
            Logger.exception( "Failed to decode byte array!", e );
            return MagnetProperties.DEFAULT;
        }
    }
}
