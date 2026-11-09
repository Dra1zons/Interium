package interium.fabric;
import interium.fabric.delta.DeltaFont;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
final class FontProbe extends Screen {
    private DeltaFont[] fonts;
    FontProbe(){super(Text.literal("Delta font test"));}
    @Override protected void init(){
        try{fonts=new DeltaFont[]{DeltaFont.load(client,"onest_regular"), DeltaFont.load(client,"sf_regular"), DeltaFont.load(client,"sf_medium"),DeltaFont.load(client,"gt_regular"),DeltaFont.load(client,"icons")};}
        catch(Exception e){throw new RuntimeException(e);}
    }
    @Override public void render(DrawContext c,int x,int y,float d){
        c.fill(0,0,width,height,0xFF161824);
        for(int i=0;i<fonts.length;i++) fonts[i].draw(c,(i==4?"ABCDEFG": "Delta Client - Onest Привет 1.21.11"),20,20+i*38,20,0xFFFFFFFF);
    }
}
