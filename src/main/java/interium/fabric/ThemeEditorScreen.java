package interium.fabric;

import interium.core.Theme;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/** Edit RGB colors, opacity and presets. Blur remains the game's normal screen blur. */
public final class ThemeEditorScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget accentField,panelField;
    private int left,top,w,opacity;
    private String error="";
    private static final String[][] PRESETS={{"Amethyst","A792FF","151621"},{"Ocean","6FCDF3","10232C"},{"Ember","F2AA8D","251C1C"}};
    public ThemeEditorScreen(Screen parent){super(Text.literal("Theme editor"));this.parent=parent;opacity=InteriumState.theme.getOpacityPercent();}
    @Override protected void init(){
        w=Math.min(320,width-20);left=(width-w)/2;top=Math.max(6,(height-180)/2);
        accentField=addDrawableChild(new TextFieldWidget(textRenderer,left+12,top+65,w/2-18,19,Text.literal("Accent RGB")));
        panelField=addDrawableChild(new TextFieldWidget(textRenderer,left+w/2+5,top+65,w/2-17,19,Text.literal("Panel RGB")));
        accentField.setMaxLength(6);panelField.setMaxLength(6);
        accentField.setText(String.format("%06X",InteriumState.theme.getAccent()));panelField.setText(String.format("%06X",InteriumState.theme.getPanel()));
    }
    private void button(DrawContext m,int x,int y,int bw,String text,int color){m.fill(x,y,x+bw,y+18,0xFF29283B);m.drawTextWithShadow(textRenderer,text,x+5,y+5,color);}
    @Override public void render(DrawContext m,int mx,int my,float delta){
        int ac=InteriumState.accent();m.fill(left,top,left+w,top+174,0xF0181825);m.fill(left,top,left+w,top+2,ac);
        m.drawTextWithShadow(textRenderer,"Theme editor",left+12,top+9,0xFFF7F5FF);
        m.drawTextWithShadow(textRenderer,"Presets",left+12,top+24,0xFFAAA7B8);
        int bw=(w-28)/3;
        for(int i=0;i<3;i++)button(m,left+12+i*(bw+2),top+36,bw,PRESETS[i][0],0xFFF7F5FF);
        m.drawTextWithShadow(textRenderer,"Accent (RRGGBB)",left+12,top+56,0xFFAAA7B8);
        m.drawTextWithShadow(textRenderer,"Panel (RRGGBB)",left+w/2+5,top+56,0xFFAAA7B8);
        m.drawTextWithShadow(textRenderer,"Opacity: "+opacity+"%",left+12,top+96,0xFFF7F5FF);
        button(m,left+w-75,top+91,28,"-",ac);button(m,left+w-42,top+91,28,"+",ac);
        m.drawTextWithShadow(textRenderer,"Blur: Minecraft default",left+12,top+120,0xFFAAA7B8);
        if(!error.isEmpty())m.drawTextWithShadow(textRenderer,error,left+12,top+134,0xFFFF8888);
        button(m,left+w-130,top+150,52,"Cancel",0xFFF7F5FF);button(m,left+w-70,top+150,58,"Save",ac);
        super.render(m,mx,my,delta);
    }
    private boolean inside(double x,double y,int bx,int by,int bw,int bh){return x>=bx&&x<bx+bw&&y>=by&&y<by+bh;}
    @Override public boolean mouseClicked(Click click,boolean doubled){
        if(super.mouseClicked(click,doubled))return true;
        if(click.button()!=0)return false;double x=click.x(),y=click.y();int bw=(w-28)/3;
        for(int i=0;i<3;i++)if(inside(x,y,left+12+i*(bw+2),top+36,bw,18)){accentField.setText(PRESETS[i][1]);panelField.setText(PRESETS[i][2]);return true;}
        if(inside(x,y,left+w-75,top+91,28,18)){opacity=Math.max(10,opacity-10);return true;}
        if(inside(x,y,left+w-42,top+91,28,18)){opacity=Math.min(100,opacity+10);return true;}
        if(inside(x,y,left+w-130,top+150,52,18)){close();return true;}
        if(inside(x,y,left+w-70,top+150,58,18)){
            String a=accentField.getText(),p=panelField.getText();
            if(!a.matches("[0-9A-Fa-f]{6}")||!p.matches("[0-9A-Fa-f]{6}")){error="Use 6 hexadecimal digits";return true;}
            InteriumState.theme=new Theme("Custom",Integer.parseInt(a,16),Integer.parseInt(p,16),opacity,InteriumState.theme.getBlur());
            InteriumState.save();close();return true;
        }
        return false;
    }
    @Override public void close(){client.setScreen(parent);}
    @Override public boolean shouldPause(){return false;}
}
