package me.knighthat.plugin.item;

import lombok.Data;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.NotNull;

import java.beans.Transient;
import java.io.Serial;
import java.io.Serializable;

@Data
public class MagnetProperties implements Serializable {

    @NotNull
    public static final MagnetProperties DEFAULT;

    @Serial
    private static final long serialVersionUID = 1827462938472617848L;

    static {
        DEFAULT = new MagnetProperties();
    }

    @NotNull
    private final Area    area;
    @NotNull
    private       String  tierName;
    private       boolean activated;

    public MagnetProperties() {
        this.tierName = "";
        this.area = new Area();
        this.activated = false;
    }

    @Transient
    public @NotNull Component toComponent() {
        return Component.text( "MagnetProperties[tier=%tier,activated=%activated,area=%area]" )
                        .replaceText( builder -> builder.matchLiteral( "%tier" ).replacement( tierName ) )
                        .replaceText( builder -> builder.matchLiteral( "%activated" ).replacement( Component.text( activated ) ) )
                        .replaceText( builder -> builder.matchLiteral( "%area" ).replacement( area.toComponent() ) );
    }

    @Data
    public static class Area implements Serializable {

        @Serial
        private static final long serialVersionUID = 2716382736189372639L;

        @Positive
        private double x;
        @Positive
        private double y;
        @Positive
        private double z;

        public Area( double x, double y, double z ) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Area() {
            this( 0d, 0d, 0d );
        }

        @Transient
        public @NotNull Component toComponent() {
            return Component.text( "Area[x=%x,y=%y,z=%z]" )
                            .replaceText( builder -> builder.matchLiteral( "%x" ).replacement( Component.text( x ) ) )
                            .replaceText( builder -> builder.matchLiteral( "%y" ).replacement( Component.text( y ) ) )
                            .replaceText( builder -> builder.matchLiteral( "%z" ).replacement( Component.text( z ) ) );
        }
    }
}
