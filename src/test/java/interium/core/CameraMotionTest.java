package interium.core;
/** Numerical checks only, not Minecraft integration tests. */
public final class CameraMotionTest {
    static int checks;
    static void check(boolean condition,String msg){checks++;if(!condition)throw new AssertionError(msg);}
    static boolean close(double a,double b){return Math.abs(a-b)<1e-9;}
    public static void main(String[] args){
        checks=0;
        var rest=CameraMotion.step(0,0,0,0,.5);check(close(rest.x(),0)&&close(rest.y(),0)&&close(rest.z(),0),"rest");
        var north=CameraMotion.step(0,1,0,0,.5);check(close(north.x(),0)&&close(north.z(),.5),"forward");
        var west=CameraMotion.step(0,1,0,90,.5);check(close(west.x(),-.5)&&close(west.z(),0),"yaw");
        var left=CameraMotion.step(1,0,0,0,.5);check(close(left.x(),.5)&&close(left.z(),0),"side");
        var up=CameraMotion.step(0,0,1,0,.5);check(close(up.y(),.5),"up");
        var down=CameraMotion.step(0,0,-1,0,.5);check(close(down.y(),-.5),"down");
        var diag=CameraMotion.step(1,1,1,33,.5);check(close(Math.hypot(Math.hypot(diag.x(),diag.z()),diag.y()),.5),"3D normalization");
        var invalid=CameraMotion.step(Double.NaN,1,0,0,.5);check(close(invalid.x(),0)&&close(invalid.z(),0),"NaN");
        check(close(CameraMotion.step(0,1,0,0,-1).z(),0),"negative speed");
        check(close(CameraMotion.step(0,1,0,Double.POSITIVE_INFINITY,.5).z(),0),"infinite yaw");
        check(close(CameraMotion.clampOffset(100,0,0,64).x(),64),"range");
        var inside=CameraMotion.clampOffset(1,2,3,64);check(close(inside.x(),1)&&close(inside.y(),2)&&close(inside.z(),3),"unchanged inside");
        var origin=CameraMotion.clampOffset(0,0,0,64);check(close(origin.x(),0)&&close(origin.z(),0),"origin");
        var badRadius=CameraMotion.clampOffset(2,3,4,0);check(close(badRadius.x(),0)&&close(badRadius.y(),0),"invalid radius");
        check(close(CameraMotion.clampOffset(Double.NaN,0,0,64).x(),0),"invalid offset");
        for(int yaw=-360;yaw<=360;yaw+=15){var move=CameraMotion.step(1,1,0,yaw,2);check(close(Math.hypot(move.x(),move.z()),2),"yaw normalization "+yaw);}
        var cap=CameraMotion.clampOffset(100,100,100,64);check(close(Math.hypot(Math.hypot(cap.x(),cap.z()),cap.y()),64),"3D range");
        System.out.println("PASS: "+checks+" camera numerical checks (not game tests)");
    }
}
