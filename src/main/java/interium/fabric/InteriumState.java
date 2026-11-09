package interium.fabric;

import interium.core.Theme;
import interium.core.ThemeStore;
import net.fabricmc.loader.api.FabricLoader;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Non-executable local preferences; module enable states intentionally do not persist. */
public final class InteriumState {
    private static final Path DIR=FabricLoader.getInstance().getConfigDir().resolve("interium");
    private static final Path SETTINGS=DIR.resolve("settings.properties"), THEME=DIR.resolve("theme.properties");
    static Theme theme=Theme.DEFAULT;
    static int cps=10;
    static boolean middleFriend=true, nightVision=false;
    static float gammaLevel=4.0f;
    static float freeCameraSpeed=.5f;
    static boolean tntTimerTicks=false;
    static float auraRange=3.0f, auraExtra=.5f;
    static int auraPriority=0;
    static float auraTurnSpeed=60f, speedValue=.36f;
    static boolean auraLock=true;
    static int speedMode=0;
    static float targetStrafeRadius=2.5f,targetStrafeSpeed=.25f;
    static boolean targetStrafeClockwise=true,targetStrafeInput=true;
    static float totemHealth=6f;
    static float crystalRange=4.5f,crystalMinDistance=3.5f,crystalMinHealth=12f;
    static boolean crystalPlaceAnimals=false;
    static boolean totemRestore=true,totemPreferPlain=true,totemPauseEating=false,auraShieldBreak=true;
    static boolean auraPlayers=true,auraHostiles=false,auraAnimals=false,auraOnlyCrits=true,auraAdaptive=true,auraStopUsing=true;
    // Same -100 + mouse-button encoding as Delta. -1 means no bind.
    static final int[][] binds=new int[InteriumScreen.MODULES.length][];
    static {
        for(int i=0;i<binds.length;i++){binds[i]=new int[InteriumScreen.MODULES[i].length];Arrays.fill(binds[i],-1);}
    }
    static final Set<UUID> friends=new HashSet<>();
    private InteriumState() {}
    static void load() {
        try { if(Files.exists(THEME)) theme=ThemeStore.load(THEME); } catch(Exception e) { System.err.println("Interium: ignoring invalid theme: "+e.getMessage()); }
        Properties p=new Properties();
        try (InputStream in=Files.newInputStream(SETTINGS)) {
            p.load(in);
            tntTimerTicks=Boolean.parseBoolean(p.getProperty("tntTimer.ticks","false"));
            try{float v=Float.parseFloat(p.getProperty("freeCamera.speed","0.5"));freeCameraSpeed=Float.isFinite(v)?Math.max(.1f,Math.min(2f,v)):.5f;}catch(NumberFormatException ignored){freeCameraSpeed=.5f;}
            try { cps=Math.max(1,Math.min(20,Integer.parseInt(p.getProperty("cps","10")))); } catch(NumberFormatException ignored) { cps=10; }
            middleFriend=!"pearl".equals(p.getProperty("middleMode"));
            nightVision="nightVision".equals(p.getProperty("fullBright.mode"));
            try {float g=Float.parseFloat(p.getProperty("fullBright.gamma","4.0"));gammaLevel=Float.isFinite(g)?Math.max(1f,Math.min(8f,g)):4f;}catch(NumberFormatException ignored){gammaLevel=4f;}
            try{float v=Float.parseFloat(p.getProperty("aura.range","3.0"));auraRange=Float.isFinite(v)?Math.max(.1f,Math.min(6f,v)):3f;}catch(NumberFormatException ignored){auraRange=3f;}
            try{float v=Float.parseFloat(p.getProperty("aura.extra","0.5"));auraExtra=Float.isFinite(v)?Math.max(.1f,Math.min(3f,v)):.5f;}catch(NumberFormatException ignored){auraExtra=.5f;}
            try{auraPriority=Math.max(0,Math.min(2,Integer.parseInt(p.getProperty("aura.priority","0"))));}catch(NumberFormatException ignored){auraPriority=0;}
            auraPlayers=Boolean.parseBoolean(p.getProperty("aura.players","true"));auraHostiles=Boolean.parseBoolean(p.getProperty("aura.hostiles","false"));auraAnimals=Boolean.parseBoolean(p.getProperty("aura.animals","false"));auraOnlyCrits=Boolean.parseBoolean(p.getProperty("aura.crits","true"));auraAdaptive=Boolean.parseBoolean(p.getProperty("aura.adaptive","true"));auraStopUsing=Boolean.parseBoolean(p.getProperty("aura.stopUsing","true"));
            try{float v=Float.parseFloat(p.getProperty("totem.health","6.0"));totemHealth=Float.isFinite(v)?Math.max(1f,Math.min(20f,v)):6f;}catch(NumberFormatException ignored){totemHealth=6f;}
            totemRestore=Boolean.parseBoolean(p.getProperty("totem.restore","true"));totemPreferPlain=Boolean.parseBoolean(p.getProperty("totem.plain","true"));totemPauseEating=Boolean.parseBoolean(p.getProperty("totem.pauseEating","false"));auraShieldBreak=Boolean.parseBoolean(p.getProperty("aura.shieldBreak","true"));
            try{float v=Float.parseFloat(p.getProperty("crystal.range","4.5"));crystalRange=Float.isFinite(v)?Math.max(3f,Math.min(6f,v)):4.5f;}catch(NumberFormatException ignored){crystalRange=4.5f;}
            try{float v=Float.parseFloat(p.getProperty("crystal.minDistance","3.5"));crystalMinDistance=Float.isFinite(v)?Math.max(0f,Math.min(6f,v)):3.5f;}catch(NumberFormatException ignored){crystalMinDistance=3.5f;}
            try{float v=Float.parseFloat(p.getProperty("crystal.minHealth","12"));crystalMinHealth=Float.isFinite(v)?Math.max(0f,Math.min(20f,v)):12f;}catch(NumberFormatException ignored){crystalMinHealth=12f;}
            crystalPlaceAnimals=Boolean.parseBoolean(p.getProperty("crystal.placeAnimals","false"));
            try{float v=Float.parseFloat(p.getProperty("aura.turnSpeed","60"));auraTurnSpeed=Float.isFinite(v)?Math.max(10f,Math.min(180f,v)):60f;}catch(NumberFormatException ignored){auraTurnSpeed=60f;}
            auraLock=Boolean.parseBoolean(p.getProperty("aura.lock","true"));
            try{float v=Float.parseFloat(p.getProperty("speed.value","0.36"));speedValue=Float.isFinite(v)?Math.max(.2f,Math.min(.8f,v)):.36f;}catch(NumberFormatException ignored){speedValue=.36f;}
            try{speedMode=Math.max(0,Math.min(1,Integer.parseInt(p.getProperty("speed.mode","0"))));}catch(NumberFormatException ignored){speedMode=0;}
            try{float v=Float.parseFloat(p.getProperty("targetStrafe.radius","2.5"));targetStrafeRadius=Float.isFinite(v)?Math.max(1f,Math.min(5f,v)):2.5f;}catch(NumberFormatException ignored){targetStrafeRadius=2.5f;}
            try{float v=Float.parseFloat(p.getProperty("targetStrafe.speed","0.25"));targetStrafeSpeed=Float.isFinite(v)?Math.max(.1f,Math.min(.6f,v)):.25f;}catch(NumberFormatException ignored){targetStrafeSpeed=.25f;}
            targetStrafeClockwise=Boolean.parseBoolean(p.getProperty("targetStrafe.clockwise","true"));targetStrafeInput=Boolean.parseBoolean(p.getProperty("targetStrafe.requireInput","true"));
            for(int i=0;i<binds.length;i++)for(int j=0;j<binds[i].length;j++){
                try{int k=Integer.parseInt(p.getProperty("bind."+i+"."+j,"-1"));
                    if(k==-1||(k>=-100&&k<=-93)||(k>=32&&k<=348))binds[i][j]=k;
                }catch(NumberFormatException ignored){}
            }
            for(String value:p.getProperty("friends","").split(",")) try { friends.add(UUID.fromString(value)); } catch(IllegalArgumentException ignored) {}
        } catch (NoSuchFileException ignored) {} catch(IOException | IllegalArgumentException e) { System.err.println("Interium: could not read settings: "+e.getMessage()); }
    }
    static void save() {
        try {
            Files.createDirectories(DIR); Properties p=new Properties();
            p.setProperty("tntTimer.ticks",Boolean.toString(tntTimerTicks));
            p.setProperty("freeCamera.speed",Float.toString(freeCameraSpeed));
            p.setProperty("cps",String.valueOf(cps)); p.setProperty("middleMode",middleFriend?"friend":"pearl");
            p.setProperty("fullBright.mode",nightVision?"nightVision":"gamma");p.setProperty("fullBright.gamma",String.valueOf(gammaLevel));
            p.setProperty("aura.range",Float.toString(auraRange));p.setProperty("aura.extra",Float.toString(auraExtra));p.setProperty("aura.priority",Integer.toString(auraPriority));
            p.setProperty("aura.players",Boolean.toString(auraPlayers));p.setProperty("aura.hostiles",Boolean.toString(auraHostiles));p.setProperty("aura.animals",Boolean.toString(auraAnimals));p.setProperty("aura.crits",Boolean.toString(auraOnlyCrits));p.setProperty("aura.adaptive",Boolean.toString(auraAdaptive));p.setProperty("aura.stopUsing",Boolean.toString(auraStopUsing));
            p.setProperty("totem.health",Float.toString(totemHealth));p.setProperty("totem.restore",Boolean.toString(totemRestore));p.setProperty("totem.plain",Boolean.toString(totemPreferPlain));p.setProperty("totem.pauseEating",Boolean.toString(totemPauseEating));p.setProperty("aura.shieldBreak",Boolean.toString(auraShieldBreak));
            p.setProperty("crystal.range",Float.toString(crystalRange));p.setProperty("crystal.minDistance",Float.toString(crystalMinDistance));p.setProperty("crystal.minHealth",Float.toString(crystalMinHealth));
            p.setProperty("crystal.placeAnimals",Boolean.toString(crystalPlaceAnimals));
            p.setProperty("aura.turnSpeed",Float.toString(auraTurnSpeed));p.setProperty("aura.lock",Boolean.toString(auraLock));
            p.setProperty("speed.value",Float.toString(speedValue));p.setProperty("speed.mode",Integer.toString(speedMode));
            p.setProperty("targetStrafe.radius",Float.toString(targetStrafeRadius));p.setProperty("targetStrafe.speed",Float.toString(targetStrafeSpeed));p.setProperty("targetStrafe.clockwise",Boolean.toString(targetStrafeClockwise));p.setProperty("targetStrafe.requireInput",Boolean.toString(targetStrafeInput));
            for(int i=0;i<binds.length;i++)for(int j=0;j<binds[i].length;j++)p.setProperty("bind."+i+"."+j,String.valueOf(binds[i][j]));
            List<String> ids=new ArrayList<>(); for(UUID id:friends) ids.add(id.toString()); Collections.sort(ids);p.setProperty("friends",String.join(",",ids));
            Path tmp=Files.createTempFile(DIR,"settings-",".tmp");
            try { try(OutputStream out=Files.newOutputStream(tmp)){p.store(out,"Interium preferences");} Files.move(tmp,SETTINGS,StandardCopyOption.REPLACE_EXISTING); }
            finally { Files.deleteIfExists(tmp); }
            ThemeStore.save(THEME,theme);
        } catch(IOException e){System.err.println("Interium: could not save settings: "+e.getMessage());}
    }
    static int panelColor(){return (Math.round(theme.getOpacityPercent()*255f/100f)<<24)|theme.getPanel();}
    static int accent(){return 0xFF000000|theme.getAccent();}
}
