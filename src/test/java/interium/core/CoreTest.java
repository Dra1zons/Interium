package interium.core;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public final class CoreTest {
    private static int checks=0;
    private static void check(boolean condition,String name) { checks++; if (!condition) throw new AssertionError(name); }
    public static void main(String[] args) throws Exception {
        ModuleCatalog catalog=new ModuleCatalog();
        check(catalog.all().size()==35,"catalog count");
        for(String n:Arrays.asList("Aim Assist","AutoTool","Inventory Manager","BackTrack","AutoSoup","TimerRange","MoreKB","Blink","FastLadder","VClip","Fucker","FastBreak","FakeLag","Teleport"))
            check(catalog.get(n)==null,"excluded "+n);
        check(catalog.get("AntiFireball")!=null && catalog.get("Chest Stealer")!=null,"retained");
        ModuleSpec s=catalog.get("Scaffold"); s.selectMode("Augustus");s.setSetting("delay","4");s.selectMode("Delta");
        check(s.getSettings().isEmpty(),"mode isolation");s.setSetting("delay","8");s.selectMode("Augustus");
        check("4".equals(s.getSettings().get("delay")),"mode persistence");
        try{s.selectMode("Unknown");throw new AssertionError("unknown mode accepted");}catch(IllegalArgumentException expected){checks++;}
        try{s.getSettings().put("x","y");throw new AssertionError("mutable settings");}catch(UnsupportedOperationException expected){checks++;}
        check("4.0 s".equals(TntTimer.format(80)),"80 ticks");
        check("0.0 s".equals(TntTimer.format(1)),"reference truncation");
        check("0.0 s".equals(TntTimer.format(-1)),"negative clamping");
        check(MiddleClickAction.PEARL.isAvailable(true,false) && !MiddleClickAction.PEARL.isAvailable(false,true),"pearl predicate");
        check(MiddleClickAction.FRIEND.isAvailable(false,true) && !MiddleClickAction.FRIEND.isAvailable(true,false),"friend predicate");
        Path dir=Files.createTempDirectory("interium-tests-");Path file=dir.resolve("theme.properties");
        Theme t=new Theme("Тёмная",0x123456,0xABCDEF,80,17);ThemeStore.save(file,t);
        check(t.equals(ThemeStore.load(file)),"theme roundtrip");
        Files.write(file,"name=x\naccent=broken\npanel=151621\nopacity=80\nblur=20\n".getBytes(StandardCharsets.UTF_8));
        try{ThemeStore.load(file);throw new AssertionError("bad config accepted");}catch(java.io.IOException expected){checks++;}
        Files.delete(file);Files.delete(dir);
        System.out.println("PASS: "+checks+" checks");
        CameraMotionTest.main(args);
        TntTimerTest.main(args);
    }
}
