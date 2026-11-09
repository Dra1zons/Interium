package interium.core;
public final class VisualMathTest {
 private static int checks;
 private static void eq(double actual,double expected,String name){checks++;if(!Double.isFinite(actual)||Math.abs(actual-expected)>1e-9)throw new AssertionError(name+": "+actual+" != "+expected);}
 public static void main(String[] args){
 eq(VisualMath.bowPull(0),0,"empty bow");eq(VisualMath.bowPull(20),1,"full bow");eq(VisualMath.bowPull(40),1,"capped bow");eq(VisualMath.bowPull(-1),0,"negative time");
 eq(VisualMath.position(-20,100,10),0,"left edge");eq(VisualMath.position(200,100,10),1,"right edge");eq(VisualMath.position(45,100,10),.5,"middle");eq(VisualMath.position(10,10,20),0,"oversize");eq(VisualMath.position(Float.NaN,100,10),0,"nonfinite");
 eq(VisualMath.packed(10000,4),32767,"packed saturation");eq(VisualMath.packed(-10,64),0,"packed negative");eq(VisualMath.packed(Float.NaN,64),0,"packed nan");eq(VisualMath.packed(5,64),320,"packed radius");
 for(boolean thrown:new boolean[]{false,true})for(boolean potion:new boolean[]{false,true})for(boolean water:new boolean[]{false,true})for(int i=0;i<20;i++){double vx=i*.13,vy=.9-i*.09,vz=-i*.07,drag=water?(thrown?.7999999144424994:.6000000001891753):.990000120151185;var step=VisualMath.step(vx,vy,vz,thrown,potion,water);double gravity=potion?.05000000070627959:.030000000582077163;eq(step.dx(),thrown?vx*drag:vx,"travel x");eq(step.dy(),thrown?(vy-gravity)*drag:vy,"travel y");eq(step.dz(),thrown?vz*drag:vz,"travel z");eq(step.vx(),vx*drag,"next vx");eq(step.vy(),thrown?(vy-gravity)*drag:vy*drag-.05000000070627959,"next vy");eq(step.vz(),vz*drag,"next vz");}
 System.out.println("PASS: "+checks+" visual numerical checks (not game/rendering tests)");
 }
}
