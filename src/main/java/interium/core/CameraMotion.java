package interium.core;

/** Pure camera mathematics: no Minecraft state or packets. */
public final class CameraMotion {
    private CameraMotion() {}
    public record Offset(double x,double y,double z) {}
    private static final Offset ZERO=new Offset(0,0,0);
    public static Offset step(double side,double forward,double vertical,double yaw,double speed) {
        if(!Double.isFinite(side)||!Double.isFinite(forward)||!Double.isFinite(vertical)||!Double.isFinite(yaw)||!Double.isFinite(speed)||speed<=0)return ZERO;
        double length=Math.hypot(Math.hypot(side,forward),vertical);
        if(length==0)return ZERO;
        if(length>1){side/=length;forward/=length;vertical/=length;}
        double angle=Math.toRadians(yaw),sin=Math.sin(angle),cos=Math.cos(angle);
        return new Offset((side*cos-forward*sin)*speed,vertical*speed,(forward*cos+side*sin)*speed);
    }
    public static Offset clampOffset(double x,double y,double z,double radius) {
        if(!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(z)||!Double.isFinite(radius)||radius<=0)return ZERO;
        double length=Math.hypot(Math.hypot(x,y),z);
        double scale=length>radius?radius/length:1;
        return new Offset(x*scale,y*scale,z*scale);
    }
}
