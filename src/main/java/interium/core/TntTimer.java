package interium.core;

/** Client fuse display at 20 ticks/second, not a wall-clock or server prediction. */
public final class TntTimer {
    private TntTimer() {}
    public static String format(int fuseTicks){return format(fuseTicks,false);}
    public static String format(int fuseTicks,boolean displayTicks){
        long ticks=Math.max(0L,(long)fuseTicks);
        if(displayTicks)return ticks+" t";
        // Preserve Time To Live's display boundaries and truncation, not rounding up.
        if(ticks>72000)return (ticks/72000)+" h";
        if(ticks>1200)return (ticks/1200)+" m";
        return (ticks/20)+"."+((ticks%20)/2)+" s";
    }
}
