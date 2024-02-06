package me.knighthat.plugin.item;

import lombok.Data;
import me.knighthat.plugin.utils.Debuggable;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;

@Data
public class MagnetProperties implements Serializable, Debuggable {

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

    @Data
    public static class Area implements Serializable, Debuggable {

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
    }
}
