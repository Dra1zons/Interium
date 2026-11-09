package interium.fabric;

import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;
import java.io.*;
import java.util.*;

/** Local preferences only. No server/backend access and no executable config. */
public final class DeltaVisualSettings {
    private static final Path FILE=FabricLoader.getInstance().getConfigDir().resolve("interium/delta-visuals.properties");
    private static final Properties DATA=new Properties();
    public static boolean hud=true,crosshair=false,viewModel=false,removals=false,seeInvisibles=false,aspectRatio=false,swingAnimation=false,pointers=false,shulkerPreview=false,itemPhysic=false;
    static void load(){
        try(var in=Files.newInputStream(FILE)){DATA.load(in);}
        catch(NoSuchFileException ignored){}catch(IOException|IllegalArgumentException e){System.err.println("Interium Delta preferences: "+e.getMessage());DATA.clear();}
    }
    static boolean flag(String key,boolean fallback){return Boolean.parseBoolean(DATA.getProperty(key,Boolean.toString(fallback)));}
    static float number(String key,float fallback,float min,float max){
        try{float n=Float.parseFloat(DATA.getProperty(key,Float.toString(fallback)));return Float.isFinite(n)?Math.max(min,Math.min(max,n)):fallback;}catch(NumberFormatException e){return fallback;}
    }
    static String string(String key,String fallback){return DATA.getProperty(key,fallback);}
    static void set(String key,Object value){DATA.setProperty(key,String.valueOf(value));}
    static void toggle(String key,boolean fallback){set(key,!flag(key,fallback));}
    static void save(){
        try{Files.createDirectories(FILE.getParent());Path temp=Files.createTempFile(FILE.getParent(),"delta-",".tmp");
            try{try(var out=Files.newOutputStream(temp)){DATA.store(out,"Delta visual preferences; module activation is not persisted");}Files.move(temp,FILE,StandardCopyOption.REPLACE_EXISTING);}finally{Files.deleteIfExists(temp);}}
        catch(IOException e){System.err.println("Interium: could not save Delta preferences: "+e.getMessage());}
    }
    static boolean light(){return flag("theme.light",false);}
    static int primary(){return (int)(long)number("theme.rgb",0x859cff,0,0xffffff)|0xff000000;}
    static int text(){return light()?0xff111216:0xffffffff;}
    static int background(){int a=Math.round(number("theme.opacity",.78f,.2f,1f)*255);return a<<24|(light()?0xe9edf5:0x111220);}
}
