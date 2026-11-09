package interium.fabric;
import net.minecraft.text.Text;
/** Per-entity frame data; never stored in a global entity map. */
public interface TntCountdownState {
    Text interium$getCountdown();
    double interium$getCountdownHeight();
    void interium$setCountdown(Text text,double height);
}
