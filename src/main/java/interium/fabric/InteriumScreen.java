package interium.fabric;

import com.viaversion.viafabricplus.ViaFabricPlus;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import java.util.Locale;

/** Translucent five-column module screen. Uses Minecraft's normal screen blur, not a refraction shader. */
public final class InteriumScreen extends Screen {
    private static final String[] CATEGORIES={"Combat","Movement","Visuals","Player","World"};
    static final String[][] MODULES={{"Auto Clicker","Anti Fireball","TriggerBot","Aura","Velocity","Auto Totem","Crystal Breaker","Crystal Place","No Friend Damage","Auto EXP"},{"Sprint","No Slow Down","Speed","Target Strafe","Flight","Gui Move","No Jump Delay","No Push","Free Camera"},{"TNT Timer","Bed Radar","Player Radar","Full Bright","Interface","Crosshair","View Model","Removals"},{"Middle Click","Chest Stealer","Auto Armor","Auto Respawn","Death Coords"},{"Fast Place","SafeWalk","Bed Breaker","Scaffold"}};
    private TextFieldWidget search;
    private String searchText="";
    private int cols,left,top,colWidth,gap=6,panelHeight,expanded=-1;
    public InteriumScreen(){super(Text.literal("Interium"));}
    @Override protected void init(){
        search=addDrawableChild(new TextFieldWidget(textRenderer,Math.max(15,width-139),14,116,12,Text.literal("Search modules")));
        search.setDrawsBackground(false); search.setMaxLength(40); search.setPlaceholder(Text.literal("Search..."));
        search.setText(searchText);search.setChangedListener(v->searchText=v);
    }
    private void layout() {
        cols=width>=390?5:3; colWidth=Math.max(54,(width-24-gap*(cols-1))/cols);
        colWidth=Math.min(colWidth,155); left=Math.max(5,(width-cols*colWidth-gap*(cols-1))/2); top=53;
        int rows=(CATEGORIES.length+cols-1)/cols;
        panelHeight=Math.max(65,Math.min(230,(height-top-31-gap*(rows-1))/rows));
    }
    private int x(int i){return left+(i%cols)*(colWidth+gap);}
    private int y(int i){return top+(i/cols)*(panelHeight+gap);}
    private boolean match(String name){return name.toLowerCase(Locale.ROOT).contains(searchText.trim().toLowerCase(Locale.ROOT));}
    static boolean enabled(int i,int j){return switch(i){
        case 0 -> switch(j){case 0->InteriumClient.autoClickerEnabled;case 1->InteriumClient.antiFireballEnabled;case 2->InteriumClient.triggerBotEnabled;case 3->InteriumClient.auraEnabled;case 4->InteriumClient.velocityEnabled;case 5->InteriumClient.autoTotemEnabled;case 6->InteriumClient.crystalBreakerEnabled;case 7->InteriumClient.crystalPlacerEnabled;case 8->InteriumClient.noFriendDamageEnabled;case 9->InteriumClient.autoExpEnabled;default->false;};
        case 1 -> switch(j){case 0->InteriumClient.sprintEnabled;case 1->InteriumClient.noSlowEnabled;case 2->InteriumClient.speedEnabled;case 3->InteriumClient.targetStrafeEnabled;case 4->InteriumClient.flightEnabled;case 5->InteriumClient.guiMoveEnabled;case 6->InteriumClient.noJumpDelayEnabled;case 7->InteriumClient.noPushEnabled;case 8->InteriumClient.freeCameraEnabled;default->false;};
        case 2 -> switch(j){case 0->InteriumClient.tntTimerEnabled;case 1->InteriumClient.bedRadarEnabled;case 2->InteriumClient.playerRadarEnabled;case 3->InteriumClient.fullBrightEnabled;case 4->DeltaVisualSettings.hud;case 5->DeltaVisualSettings.crosshair;case 6->DeltaVisualSettings.viewModel;case 7->DeltaVisualSettings.removals;default->false;};
        case 3 -> switch(j){case 0->InteriumClient.middleClickEnabled;case 1->InteriumClient.chestStealerEnabled;case 2->InteriumClient.autoArmorEnabled;case 3->InteriumClient.autoRespawnEnabled;case 4->InteriumClient.deathCoordsEnabled;default->false;};
        case 4 -> switch(j){case 0->InteriumClient.fastPlaceEnabled;case 1->InteriumClient.safeWalkEnabled;case 2->InteriumClient.bedBreakerEnabled;case 3->InteriumClient.scaffoldEnabled;default->false;};default->false;};}
    static void toggle(int i,int j){switch(i){
        case 0->{switch(j){case 0->InteriumClient.autoClickerEnabled=!InteriumClient.autoClickerEnabled;case 1->InteriumClient.antiFireballEnabled=!InteriumClient.antiFireballEnabled;case 2->InteriumClient.triggerBotEnabled=!InteriumClient.triggerBotEnabled;case 3->InteriumClient.auraEnabled=!InteriumClient.auraEnabled;case 4->InteriumClient.velocityEnabled=!InteriumClient.velocityEnabled;case 5->InteriumClient.autoTotemEnabled=!InteriumClient.autoTotemEnabled;case 6->InteriumClient.crystalBreakerEnabled=!InteriumClient.crystalBreakerEnabled;case 7->InteriumClient.crystalPlacerEnabled=!InteriumClient.crystalPlacerEnabled;case 8->InteriumClient.noFriendDamageEnabled=!InteriumClient.noFriendDamageEnabled;case 9->InteriumClient.autoExpEnabled=!InteriumClient.autoExpEnabled;}}
        case 1->{switch(j){case 0->InteriumClient.sprintEnabled=!InteriumClient.sprintEnabled;case 1->InteriumClient.noSlowEnabled=!InteriumClient.noSlowEnabled;case 2->InteriumClient.speedEnabled=!InteriumClient.speedEnabled;case 3->InteriumClient.targetStrafeEnabled=!InteriumClient.targetStrafeEnabled;case 4->InteriumClient.flightEnabled=!InteriumClient.flightEnabled;case 5->InteriumClient.guiMoveEnabled=!InteriumClient.guiMoveEnabled;case 6->InteriumClient.noJumpDelayEnabled=!InteriumClient.noJumpDelayEnabled;case 7->InteriumClient.noPushEnabled=!InteriumClient.noPushEnabled;case 8->InteriumClient.toggleFreeCamera();}}
        case 2->{switch(j){case 0->InteriumClient.tntTimerEnabled=!InteriumClient.tntTimerEnabled;case 1->InteriumClient.bedRadarEnabled=!InteriumClient.bedRadarEnabled;case 2->InteriumClient.playerRadarEnabled=!InteriumClient.playerRadarEnabled;case 3->InteriumClient.fullBrightEnabled=!InteriumClient.fullBrightEnabled;case 4->DeltaVisualSettings.hud=!DeltaVisualSettings.hud;case 5->DeltaVisualSettings.crosshair=!DeltaVisualSettings.crosshair;case 6->DeltaVisualSettings.viewModel=!DeltaVisualSettings.viewModel;case 7->DeltaVisualSettings.removals=!DeltaVisualSettings.removals;}}
        case 3->{switch(j){case 0->InteriumClient.middleClickEnabled=!InteriumClient.middleClickEnabled;case 1->InteriumClient.chestStealerEnabled=!InteriumClient.chestStealerEnabled;case 2->InteriumClient.autoArmorEnabled=!InteriumClient.autoArmorEnabled;case 3->InteriumClient.autoRespawnEnabled=!InteriumClient.autoRespawnEnabled;case 4->InteriumClient.deathCoordsEnabled=!InteriumClient.deathCoordsEnabled;}}
        case 4->{switch(j){case 0->InteriumClient.fastPlaceEnabled=!InteriumClient.fastPlaceEnabled;case 1->InteriumClient.safeWalkEnabled=!InteriumClient.safeWalkEnabled;case 2->InteriumClient.bedBreakerEnabled=!InteriumClient.bedBreakerEnabled;case 3->InteriumClient.scaffoldEnabled=!InteriumClient.scaffoldEnabled;}}
    }DeltaHud.notifyModule(MODULES[i][j],enabled(i,j));}
    private String fit(String text,int pixels){if(textRenderer.getWidth(text)<=pixels)return text;return textRenderer.trimToWidth(text,Math.max(1,pixels-7))+"..";}
    private int rowY(int i,int index){int offset=0;for(int j=0;j<index;j++)if(match(MODULES[i][j]))offset+=21+(expanded==i*10+j?17:0);return y(i)+25+offset;}
    private static void rounded(DrawContext c,int x,int y,int w,int h,int color){
        if(w<6||h<6){c.fill(x,y,x+w,y+h,color);return;}
        c.fill(x+3,y,x+w-3,y+1,color);
        c.fill(x+1,y+1,x+w-1,y+3,color);
        c.fill(x,y+3,x+w,y+h-3,color);
        c.fill(x+1,y+h-3,x+w-1,y+h-1,color);
        c.fill(x+3,y+h-1,x+w-3,y+h,color);
    }
    private int tint(int alpha,int rgb){return alpha<<24|(rgb&0xFFFFFF);}
    private void glass(DrawContext c,int x,int y,int w,int h){
        rounded(c,x,y,w,h,0x606B7791);
        int base=InteriumState.panelColor();
        rounded(c,x+1,y+1,w-2,h-2,base);
        c.fill(x+4,y+2,x+w-4,y+3,0x35FFFFFF);
    }
    @Override public void render(DrawContext c,int mx,int my,float delta) {
        layout();int accent=InteriumState.accent();
        c.drawTextWithShadow(textRenderer,"INTERIUM",left,13,0xFFF7F8FF);
        c.drawTextWithShadow(textRenderer,"MODULES",left,37,0xFFCED3E2);
        glass(c,width-145,8,137,23);
        for(int i=0;i<CATEGORIES.length;i++) {
            int x=x(i),y=y(i);glass(c,x,y,colWidth,panelHeight);
            rounded(c,x+6,y+9,5,5,accent);
            c.drawTextWithShadow(textRenderer,fit(CATEGORIES[i],colWidth-25),x+16,y+7,0xFFF8F8FF);
            c.fill(x+6,y+20,x+colWidth-6,y+21,0x36FFFFFF);
            for(int j=0;j<MODULES[i].length;j++) {
                String name=MODULES[i][j];if(!match(name))continue;int ry=rowY(i,j);
                if(ry+18>y+panelHeight-3)continue;
                boolean on=enabled(i,j), hover=mx>=x+4&&mx<x+colWidth-4&&my>=ry&&my<ry+19;
                int rgb=accent&0xFFFFFF;
                if(on)rounded(c,x+4,ry,colWidth-8,19,tint(0x54,rgb));
                else if(hover)rounded(c,x+4,ry,colWidth-8,19,0x34FFFFFF);
                rounded(c,x+8,ry+7,5,5,on?accent:0xFF788091);
                boolean hasSettings=(i==0&&j==0)||(i==3&&j==0);
                c.drawTextWithShadow(textRenderer,fit(name,colWidth-(hasSettings?26:19)),x+14,ry+6,0xFFF3F4FA);
                if(hasSettings)c.drawTextWithShadow(textRenderer,"..",x+colWidth-14,ry+5,0xFFBEC2D1);
                if(expanded==i*10+j && hasSettings && ry+35<y+panelHeight) {
                    String value=i==3?(InteriumState.middleFriend?"Friend":"Pearl"):"CPS: "+InteriumState.cps;
                    c.fill(x+11,ry+22,x+12,ry+34,tint(0xC0,rgb));
                    c.drawTextWithShadow(textRenderer,fit(value,colWidth-25),x+17,ry+24,accent);
                }
            }
        }
        int bottom=Math.min(height-20,top+((CATEGORIES.length+cols-1)/cols)*(panelHeight+gap));
        glass(c,left,bottom,66,18);c.drawTextWithShadow(textRenderer,"Themes",left+8,bottom+5,0xFFF5F5FF);
        if(left+157<width){glass(c,left+71,bottom,86,18);c.drawTextWithShadow(textRenderer,"Via versions",left+77,bottom+5,0xFFF5F5FF);}
        super.render(c,mx,my,delta);
    }
    @Override public boolean mouseClicked(Click click,boolean doubled) {
        if(super.mouseClicked(click,doubled))return true;
        layout();double mx=click.x(),my=click.y();int button=click.button();
        for(int i=0;i<CATEGORIES.length;i++) for(int j=0;j<MODULES[i].length;j++) {
            if(!match(MODULES[i][j]))continue;int x=x(i),ry=rowY(i,j),y=y(i);if(ry+18>y+panelHeight-3)continue;
            boolean hasSettings=(i==0&&j==0)||(i==3&&j==0);
            if(mx>=x+4&&mx<x+colWidth-4&&my>=ry&&my<ry+19) {
                if(button==0){toggle(i,j);return true;} if(button==1&&hasSettings){int id=i*10+j;expanded=expanded==id?-1:id;return true;}
            }
            if(expanded==i*10+j&&hasSettings&&mx>=x+4&&mx<x+colWidth-4&&my>=ry+20&&my<ry+36){
                if(i==3)InteriumState.middleFriend=!InteriumState.middleFriend;
                else InteriumState.cps=Math.max(1,Math.min(20,InteriumState.cps+(button==1?-1:1)));
                InteriumState.save();return true;
            }
        }
        int bottom=Math.min(height-20,top+((CATEGORIES.length+cols-1)/cols)*(panelHeight+gap));
        if(button==0&&my>=bottom&&my<bottom+18){
            if(mx>=left&&mx<left+66){client.setScreen(new ThemeEditorScreen(this));return true;}
            if(mx>=left+71&&mx<left+157&&left+157<width){if(ViaFabricPlus.getImpl()!=null)ViaFabricPlus.getImpl().openProtocolSelectionScreen(this);return true;}
        }
        return false;
    }
    @Override public boolean shouldPause(){return false;}
}
