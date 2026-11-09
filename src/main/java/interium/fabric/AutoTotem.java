package interium.fabric;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

/** Offhand protection for 1.21.11; moves items with normal inventory clicks. */
final class AutoTotem {
    private int cooldown, restoreSlot=-1;
    private ItemStack restoreStack=ItemStack.EMPTY;
    private Object lastPlayer;
    void tick(MinecraftClient c) {
        if(c.player != lastPlayer){lastPlayer=c.player;forget();}
        if (!InteriumClient.autoTotemEnabled || c.player==null || c.world==null || c.interactionManager==null || !c.player.isAlive()) {forget();return;}
        if(c.currentScreen!=null || c.player.currentScreenHandler!=c.player.playerScreenHandler)return;
        if(cooldown>0){cooldown--;return;}
        if(InteriumState.totemPauseEating && c.player.isUsingItem() && c.player.getActiveItem().get(DataComponentTypes.FOOD)!=null)return;
        ScreenHandler h=c.player.playerScreenHandler;
        if(restoreSlot!=-1 && !ItemStack.areEqual(h.getSlot(restoreSlot).getStack(),restoreStack))forget();
        float health=c.player.getHealth()+c.player.getAbsorptionAmount();
        ItemStack off=c.player.getOffHandStack();
        if(health<=InteriumState.totemHealth){
            if(off.isOf(Items.TOTEM_OF_UNDYING))return;
            int slot=findTotem(h,InteriumState.totemPreferPlain);
            if(slot<0)return;
            if(InteriumState.totemRestore && restoreSlot<0 && !off.isEmpty()){
                restoreSlot=slot;restoreStack=off.copy();
            }
            c.interactionManager.clickSlot(h.syncId,slot,40,SlotActionType.SWAP,c.player);cooldown=8;
        }else if(health>=InteriumState.totemHealth+.5f && restoreSlot>=0){
            if(InteriumState.totemRestore && (off.isEmpty()||off.isOf(Items.TOTEM_OF_UNDYING)) && ItemStack.areEqual(h.getSlot(restoreSlot).getStack(),restoreStack)){
                c.interactionManager.clickSlot(h.syncId,restoreSlot,40,SlotActionType.SWAP,c.player);cooldown=8;
            }
            restoreSlot=-1;restoreStack=ItemStack.EMPTY;
        }
    }
    private int findTotem(ScreenHandler h,boolean preferPlain){
        int fallback=-1;
        for(int i=9;i<=44;i++){
            ItemStack s=h.getSlot(i).getStack();if(!s.isOf(Items.TOTEM_OF_UNDYING))continue;
            if(!preferPlain || !s.hasGlint())return i;
            if(fallback<0)fallback=i;
        }
        return fallback;
    }
    private void forget(){cooldown=0;restoreSlot=-1;restoreStack=ItemStack.EMPTY;}
}
