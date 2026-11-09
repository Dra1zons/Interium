package interium.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Configuration model only: this class does not implement gameplay. */
public final class ModuleSpec {
    private final String name, category;
    private final List<String> modes;
    private final Map<String,Map<String,String>> settings = new LinkedHashMap<String,Map<String,String>>();
    private String selectedMode;
    private boolean enabled;
    public ModuleSpec(String category,String name,List<String> modes) {
        if (category==null || category.trim().isEmpty() || name==null || name.trim().isEmpty())
            throw new IllegalArgumentException("Category and name required");
        if (modes==null || modes.isEmpty()) throw new IllegalArgumentException("At least one mode required");
        ArrayList<String> copy=new ArrayList<String>();
        for (String mode:modes) {
            if (mode==null || mode.trim().isEmpty() || copy.contains(mode)) throw new IllegalArgumentException("Invalid or duplicate mode");
            copy.add(mode); settings.put(mode,new LinkedHashMap<String,String>());
        }
        this.category=category; this.name=name; this.modes=Collections.unmodifiableList(copy);
        this.selectedMode=copy.get(0);
    }
    public String getName(){return name;}
    public String getCategory(){return category;}
    public List<String> getModes(){return modes;}
    public String getSelectedMode(){return selectedMode;}
    public boolean isEnabled(){return enabled;}
    public void setEnabled(boolean value){enabled=value;}
    public void selectMode(String mode){if (!settings.containsKey(mode)) throw new IllegalArgumentException("Unknown mode: "+mode);selectedMode=mode;}
    public void setSetting(String key,String value){
        if (key==null || key.trim().isEmpty() || value==null) throw new IllegalArgumentException("Invalid setting");
        settings.get(selectedMode).put(key,value);
    }
    public Map<String,String> getSettings(){return Collections.unmodifiableMap(settings.get(selectedMode));}
}
