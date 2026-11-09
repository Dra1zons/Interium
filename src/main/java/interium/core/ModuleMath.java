package interium.core;
public final class ModuleMath {
 private ModuleMath(){} public static double clamp(double v,double lo,double hi){return Double.isFinite(v)?Math.max(lo,Math.min(hi,v)):lo;}
 public static double snap(double v,double lo,double hi,double step){return clamp(lo+Math.round((clamp(v,lo,hi)-lo)/step)*step,lo,hi);}
 public static double wrap(double n){n%=360;if(n>=180)n-=360;if(n< -180)n+=360;return n;}
 public static double turn(double d,double cap,double fraction){return Math.copySign(Math.min(Math.abs(d),Math.min(cap,Math.abs(d)*fraction)),d);}
 public static double quantize(double d,double gcd){return gcd>0?d-d%gcd:d;}
 public static long interval(double a,double b,double u){double lo=clamp(Math.min(a,b),1,20),hi=clamp(Math.max(a,b),lo,20);return Math.round(1e9/(lo+(hi-lo)*clamp(u,0,1)));}
 public static double entityImpulse(double speed,int n){return clamp(speed,1,20)*.01*Math.max(0,n);}
 public static double[] direction(double x,double z,double speed){double n=Math.hypot(x,z);return n<1e-8?new double[]{0,0}:new double[]{x/n*speed,z/n*speed};}
}
