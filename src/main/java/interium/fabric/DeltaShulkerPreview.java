package interium.fabric;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
/** Original 175x70 texture and 9x3 slot geometry; only server-provided CONTAINER data. */
public final class DeltaShulkerPreview {
 public static boolean draw(DrawContext c,ItemStack stack,int mx,int my){
  if(!DeltaVisualSettings.shulkerPreview||stack.isEmpty()||!(stack.getItem() instanceof BlockItem block)||!(block.getBlock() instanceof ShulkerBoxBlock))return false;
  var container=stack.get(DataComponentTypes.CONTAINER);if(container==null)return false;
  var mc=MinecraftClient.getInstance();int sw=mc.getWindow().getScaledWidth(),sh=mc.getWindow().getScaledHeight();
  float scale=Math.min(1f,Math.min(sw/175f,sh/70f));float x=Math.max(0,Math.min(mx+8,sw-175*scale)),y=Math.max(0,Math.min(my-86,sh-70*scale));
  var m=c.getMatrices();m.pushMatrix();m.translate(x,y);m.scale(scale,scale);drawAt(c,stack,0,0);m.popMatrix();return true;
 }
 public static void drawAt(DrawContext c,ItemStack stack,int x,int y){
  if(!(stack.getItem() instanceof BlockItem block))return;var container=stack.get(DataComponentTypes.CONTAINER);if(container==null)return;var mc=MinecraftClient.getInstance();var m=c.getMatrices();m.pushMatrix();m.translate(x,y);
  c.drawTexture(RenderPipelines.GUI_TEXTURED,Identifier.of("delta","pictures/minecraft/3x9.png"),0,0,0f,0f,175,70,256,256,0xff000000|block.getBlock().getDefaultMapColor().color);
  var stacks=container.stream().toList();for(int i=0;i<Math.min(27,stacks.size());i++){ItemStack item=stacks.get(i);if(!item.isEmpty()){int sx=10+i%9*18,sy=10+i/9*18;c.drawItem(item,sx,sy);c.drawStackOverlay(mc.textRenderer,item,sx,sy);}}
  m.popMatrix();
 }
}
