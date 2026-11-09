package interium.core;
import java.util.Locale;
public final class TntTimerTest {
    static int checks;
    static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        checks=0;
        int[] input={-1,0,1,2,19,20,79,80,1200,1201,72000,72001,Integer.MAX_VALUE};
        String[] expected={"0.0 s","0.0 s","0.0 s","0.1 s","0.9 s","1.0 s","3.9 s","4.0 s","60.0 s","1 m","60 m","1 h","29826 h"};
        for(int i=0;i<input.length;i++){check(expected[i].equals(TntTimer.format(input[i])),"seconds boundary "+input[i]);check((Math.max(0L,(long)input[i])+" t").equals(TntTimer.format(input[i],true)),"ticks "+input[i]);}
        Locale before=Locale.getDefault();
        try{Locale.setDefault(Locale.GERMANY);check("3.9 s".equals(TntTimer.format(79)),"locale independence");}finally{Locale.setDefault(before);}
        for(int tick=0;tick<=80;tick++){
            String expectedValue=(tick/20)+"."+((tick%20)/2)+" s";
            check(expectedValue.equals(TntTimer.format(tick)),"default fuse "+tick);
        }
        System.out.println("PASS: "+checks+" TNT-format checks (not rendering tests)");
    }
}
