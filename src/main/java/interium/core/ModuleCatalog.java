package interium.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Planned modules as interface metadata, not implemented gameplay modules. */
public final class ModuleCatalog {
    private final Map<String,ModuleSpec> modules=new LinkedHashMap<String,ModuleSpec>();
    public ModuleCatalog() {
        add("Combat","Auto Clicker","Inertia","Augustus","LiquidBounce","Delta");
        add("Combat","KillAura","Augustus","LiquidBounce","Delta");
        add("Combat","TriggerBot","Default"); add("Combat","Velocity","Augustus","LiquidBounce","Delta");
        add("Combat","AntiBot","Default"); add("Combat","Teams","Default");
        add("Combat","AntiFireball","Augustus"); add("Combat","Criticals","Default");
        add("Movement","Sprint","Default"); add("Movement","NoSlow","Default");
        add("Movement","Speed","Default"); add("Movement","Strafe","Default");
        add("Movement","Fly","Default"); add("Movement","Step","Default");
        add("Movement","LongJump","Default"); add("Movement","Spider","Default");
        add("Movement","NoWeb","Default");
        add("Visuals","Player ESP","Default"); add("Visuals","Bed ESP","Default");
        add("Visuals","NameTags","Default"); add("Visuals","Tracers","Default");
        add("Visuals","FullBright","Default"); add("Visuals","TNT Timer","Default");
        add("Visuals","Trajectories","Default"); add("Visuals","Attack Effects","Default");
        add("Player","NoFall","Default"); add("Player","Auto Armor","Default");
        add("Player","Chest Stealer","Default"); add("Player","Middle Click","Pearl","Friend");
        add("Player","Inventory Move","Default"); add("Player","Regen","Default");
        add("World","Scaffold","Augustus","LiquidBounce","Delta");
        add("World","SafeWalk","Default"); add("World","Bed Breaker","Default");
        add("World","Fast Place","Inertia","Augustus","LiquidBounce","Delta");
    }
    private void add(String category,String name,String... modes){
        if (modules.containsKey(name)) throw new IllegalStateException("Duplicate module: "+name);
        modules.put(name,new ModuleSpec(category,name,Arrays.asList(modes)));
    }
    public ModuleSpec get(String name){return modules.get(name);}
    public Map<String,ModuleSpec> all(){return Collections.unmodifiableMap(modules);}
}
