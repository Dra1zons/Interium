package interium.fabric;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/** Partial Auto EXP: hold a separate key, use bottles from hotbar only. */
final class AutoExp {
    private int oldSlot=-1, bottleSlot=-1, cooldown;
    private boolean cancelledUntilRelease;
    private Object owner;
    boolean tick(MinecraftClient c,boolean held){
        if(c.player!=owner){owner=c.player;oldSlot=-1;bottleSlot=-1;cooldown=0;cancelledUntilRelease=false;}
        if(!held)cancelledUntilRelease=false;
        if(!InteriumClient.autoExpEnabled || !held || c.player==null || c.world==null || c.interactionManager==null || c.currentScreen!=null || !c.player.isAlive()){
            restore(c);return false;
        }
        if(cancelledUntilRelease || c.player.isUsingItem())return false;
        if(bottleSlot!=-1 && c.player.getInventory().getSelectedSlot()!=bottleSlot){
            oldSlot=-1;bottleSlot=-1;cancelledUntilRelease=true;return false;
        }
        if(bottleSlot==-1){
            int slot=-1;
            for(int i=0;i<9;i++)if(c.player.getInventory().getStack(i).isOf(Items.EXPERIENCE_BOTTLE)){slot=i;break;}
            if(slot<0)return false;
            oldSlot=c.player.getInventory().getSelectedSlot();bottleSlot=slot;
            c.player.getInventory().setSelectedSlot(slot);
        }
        if(!c.player.getMainHandStack().isOf(Items.EXPERIENCE_BOTTLE)){restore(c);cancelledUntilRelease=true;return false;}
        if(cooldown>0){cooldown--;return true;}
        float pitch=c.player.getPitch();
        try{
            c.player.setPitch(88f);
            var result=c.interactionManager.interactItem(c.player,Hand.MAIN_HAND);
            if(result.isAccepted())c.player.swingHand(Hand.MAIN_HAND);
        }finally{c.player.setPitch(pitch);}
        cooldown=3;
        return true;
    }
    private void restore(MinecraftClient c){
        if(c.player!=null && c.player==owner && oldSlot>=0 && bottleSlot>=0 && c.player.getInventory().getSelectedSlot()==bottleSlot)
            c.player.getInventory().setSelectedSlot(oldSlot);
        oldSlot=-1;bottleSlot=-1;cooldown=0;
    }
}
