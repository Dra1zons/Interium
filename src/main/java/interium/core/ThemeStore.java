package interium.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

/** Stores themes in a non-executable Java properties file. */
public final class ThemeStore {
    private ThemeStore() {}
    public static void save(Path path, Theme theme) throws IOException {
        Path target=path.toAbsolutePath();
        Path dir=target.getParent();
        Files.createDirectories(dir);
        Properties p=new Properties();
        p.setProperty("name",theme.getName());
        p.setProperty("accent",String.format("%06X",theme.getAccent()));
        p.setProperty("panel",String.format("%06X",theme.getPanel()));
        p.setProperty("opacity",Integer.toString(theme.getOpacityPercent()));
        p.setProperty("blur",Integer.toString(theme.getBlur()));
        Path tmp=Files.createTempFile(dir,"interium-theme-",".tmp");
        try {
            try (OutputStream out=Files.newOutputStream(tmp)) { p.store(out,"Interium theme"); }
            Files.move(tmp,target,StandardCopyOption.REPLACE_EXISTING);
        } finally { Files.deleteIfExists(tmp); }
    }
    public static Theme load(Path path) throws IOException {
        Properties p=new Properties();
        try (InputStream in=Files.newInputStream(path)) { p.load(in); }
        try {
            return new Theme(required(p,"name"),hex(p,"accent"),hex(p,"panel"),
                    Integer.parseInt(required(p,"opacity")),Integer.parseInt(required(p,"blur")));
        } catch (IllegalArgumentException e) { throw new IOException("Invalid Interium theme",e); }
    }
    private static String required(Properties p,String key) {
        String s=p.getProperty(key);
        if (s==null) throw new IllegalArgumentException("Missing property: "+key);
        return s;
    }
    private static int hex(Properties p,String key) {
        String s=required(p,key);
        if (!s.matches("[0-9A-Fa-f]{6}")) throw new IllegalArgumentException("Invalid RGB: "+key);
        return Integer.parseInt(s,16);
    }
}
