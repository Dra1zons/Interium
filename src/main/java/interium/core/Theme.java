package interium.core;

import java.util.Objects;

/** Immutable visual settings, independent of Minecraft's rendering engine. */
public final class Theme {
    public static final Theme DEFAULT = new Theme("Amethyst", 0xA792FF, 0x151621, 78, 20);
    private final String name;
    private final int accent, panel, opacityPercent, blur;

    public Theme(String name, int accent, int panel, int opacityPercent, int blur) {
        Objects.requireNonNull(name, "name");
        if (name.trim().isEmpty()) throw new IllegalArgumentException("Empty theme name");
        if ((accent & 0xFF000000) != 0 || (panel & 0xFF000000) != 0)
            throw new IllegalArgumentException("Colors must be RGB");
        if (opacityPercent < 0 || opacityPercent > 100 || blur < 0 || blur > 30)
            throw new IllegalArgumentException("Opacity or blur outside range");
        this.name=name; this.accent=accent; this.panel=panel;
        this.opacityPercent=opacityPercent; this.blur=blur;
    }
    public String getName() { return name; }
    public int getAccent() { return accent; }
    public int getPanel() { return panel; }
    public int getOpacityPercent() { return opacityPercent; }
    public int getBlur() { return blur; }
    @Override public boolean equals(Object x) {
        if (!(x instanceof Theme)) return false;
        Theme t=(Theme)x;
        return name.equals(t.name) && accent==t.accent && panel==t.panel && opacityPercent==t.opacityPercent && blur==t.blur;
    }
    @Override public int hashCode() { return Objects.hash(name,accent,panel,opacityPercent,blur); }
}
