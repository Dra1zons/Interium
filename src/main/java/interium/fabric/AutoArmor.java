package interium.fabric;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

/** Fill empty armour slots with the highest base-armour score available in inventory. */
final class AutoArmor {
    private int cooldown;
    void tick(MinecraftClient c) {
        if (!InteriumClient.autoArmorEnabled || c.player==null || c.interactionManager==null || c.currentScreen!=null
                || c.player.currentScreenHandler!=c.player.playerScreenHandler) {cooldown=0;return;}
        if(cooldown>0){cooldown--;return;}
        ScreenHandler h=c.player.playerScreenHandler;
        int best=-1; double bestScore=-1;
        for(int i=9;i<=44;i++) {
            ItemStack stack=h.getSlot(i).getStack(); if(stack.isEmpty())continue;
            EquipmentSlot slot=armorSlot(stack); if(slot==null || !c.player.getEquippedStack(slot).isEmpty())continue;
            final double[] score={0};
            stack.applyAttributeModifiers(slot,(attribute,modifier)->{if(attribute.equals(EntityAttributes.ARMOR))score[0]+=modifier.value();});
            if(score[0]>bestScore){best=i;bestScore=score[0];}
        }
        if(best>=0){c.interactionManager.clickSlot(h.syncId,best,0,SlotActionType.QUICK_MOVE,c.player);cooldown=8;}
    }
    private EquipmentSlot armorSlot(ItemStack s) {
        if(s.isIn(ItemTags.HEAD_ARMOR))return EquipmentSlot.HEAD;
        if(s.isIn(ItemTags.CHEST_ARMOR))return EquipmentSlot.CHEST;
        if(s.isIn(ItemTags.LEG_ARMOR))return EquipmentSlot.LEGS;
        if(s.isIn(ItemTags.FOOT_ARMOR))return EquipmentSlot.FEET;
        return null;
    }
}
