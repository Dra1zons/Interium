package interium.core;
/** Pure numerical rules shared by the actual HUD/trajectory renderer and tests. */
public final class VisualMath {
 private VisualMath(){}
 public record Step(double dx,double dy,double dz,double vx,double vy,double vz){}
 public static double drag(boolean thrown,boolean water){return water?thrown?.7999999144424994:.6000000001891753:.990000120151185;}
 public static Step step(double x,double y,double z,boolean thrown,boolean potion,boolean water){double drag=drag(thrown,water);if(thrown){double gravity=potion?.05000000070627959:.030000000582077163;x*=drag;y=(y-gravity)*drag;z*=drag;return new Step(x,y,z,x,y,z);}return new Step(x,y,z,x*drag,y*drag-.05000000070627959,z*drag);}
 public static float bowPull(int ticks){float f=Math.max(0,ticks)/20f;return Math.min((f*f+2*f)/3,1);}
 public static float position(float coordinate,float screen,float size){if(!Float.isFinite(coordinate)||!Float.isFinite(screen)||!Float.isFinite(size)||screen<=size)return 0;return Math.max(0,Math.min(1,coordinate/(screen-size)));}
 public static int packed(float value,int precision){if(!Float.isFinite(value)||value<=0)return 0;return Math.min(32767,Math.round(value*precision));}
}
