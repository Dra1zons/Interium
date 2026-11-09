package interium.fabric;

import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;
import java.io.*;
import java.util.*;

/** Local preferences only. No server/backend access and no executable config. */
public final class DeltaVisualSettings {
    private static final Path FILE=FabricLoader.getInstance().getConfigDir().resolve("interium/delta-visuals.properties");
    private static final Properties DATA=new Properties();
    private static final Map<String,Float> NUMBERS=new java.util.concurrent.ConcurrentHashMap<>();
    public static boolean targetEsp=true;
    public static boolean hud=true,crosshair=false,viewModel=false,removals=false,seeInvisibles=false,aspectRatio=false,swingAnimation=false,pointers=false,shulkerPreview=false,itemPhysic=false,entityBox=false,entityEsp=false,blockEsp=false,soundEsp=false,predictions=false,chinaHat=false,boardSpoofer=false,handsShader=false,shaderEsp=false;
    static void load(){
        try(var in=Files.newInputStream(FILE)){DATA.load(in);NUMBERS.clear();}
        catch(NoSuchFileException ignored){}catch(IOException|IllegalArgumentException e){System.err.println("Interium Delta preferences: "+e.getMessage());DATA.clear();}
    }
    static boolean flag(String key,boolean fallback){return Boolean.parseBoolean(DATA.getProperty(key,Boolean.toString(fallback)));}
    static float number(String key,float fallback,float min,float max){
        String raw=DATA.getProperty(key);if(raw==null)return fallback;
        Float n=NUMBERS.get(key);if(n==null){try{n=Float.parseFloat(raw);}catch(NumberFormatException e){n=Float.NaN;}NUMBERS.put(key,n);}
        return Float.isFinite(n)?Math.max(min,Math.min(max,n)):fallback;
    }
    static String string(String key,String fallback){return DATA.getProperty(key,fallback);}
    static void set(String key,Object value){DATA.setProperty(key,String.valueOf(value));NUMBERS.remove(key);}
    static void toggle(String key,boolean fallback){set(key,!flag(key,fallback));}
    static void save(){
        try{Files.createDirectories(FILE.getParent());Path temp=Files.createTempFile(FILE.getParent(),"delta-",".tmp");
            try{try(var out=Files.newOutputStream(temp)){DATA.store(out,"Delta visual preferences; module activation is not persisted");}Files.move(temp,FILE,StandardCopyOption.REPLACE_EXISTING);}finally{Files.deleteIfExists(temp);}}
        catch(IOException e){System.err.println("Interium: could not save Delta preferences: "+e.getMessage());}
    }
    static int panelRgb(){return (int)number("theme.panel.rgb",0x15151b,0,0xffffff);}
    static float pulse(){float strength=number("theme.pulse",.35f,0,1);double t=(System.nanoTime()/1_000_000_000.0)*number("theme.speed",.65f,.2f,2);return 1-strength*.5f+strength*.5f*(float)Math.sin(t*2.0);}
    static float motion(){return number("theme.motion",1,0,2);}
    static boolean light(){return flag("theme.light",false);}
    public static int primary(){return (int)(long)number("theme.rgb",0x9878f8,0,0xffffff)|0xff000000;}
    static int text(){return light()?0xff111216:0xffffffff;}
    static int background(){int a=Math.round(number("theme.opacity",.86f,.2f,1f)*255);return a<<24|(light()?0xe9edf5:panelRgb());}
}
