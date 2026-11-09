package interium.fabric;

/** Limits only Interium work; never changes the user's video settings or other mods. */
public final class DeltaPerformance {
 private DeltaPerformance(){}
 public static int quality(){return Math.round(DeltaVisualSettings.number("perf.quality",1,0,2));}
 public static String name(){return switch(quality()){case 0->"Lite";case 2->"Quality";default->"Balanced";};}
 public static int blurDivisor(){return quality()==2?2:4;}
 public static int glowLayers(){return quality()==0?0:quality()==1?1:3;}
 public static int scanBudget(){return switch(quality()){case 0->2048;case 2->12000;default->4096;};}
 public static void choose(int value){DeltaVisualSettings.set("perf.quality",Math.max(0,Math.min(2,value)));DeltaVisualSettings.save();}
 static void smoke(){
  String old=DeltaVisualSettings.string("perf.quality","1");
  try{for(int i=0;i<3;i++){DeltaVisualSettings.set("perf.quality",i);if(quality()!=i)throw new AssertionError("Profile selection");if(scanBudget()<1||blurDivisor()<2)throw new AssertionError("Invalid work limits");}
   DeltaVisualSettings.set("perf.quality",1);if(blurDivisor()!=4||glowLayers()!=1)throw new AssertionError("Balanced limits");
   DeltaVisualSettings.set("perf.quality",0);if(glowLayers()!=0)throw new AssertionError("Lite glow");
  }finally{DeltaVisualSettings.set("perf.quality",old);}
  System.out.println("INTERIUM_PERFORMANCE_POLICY_PASSED (work limits, not an FPS benchmark)");
 }
}
