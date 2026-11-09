package interium.fabric;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.registry.Registries;
import java.util.*;
/** Delta EnvironmentWidget's target/armor priority and observed hand history.
 * Cooldown labels are source heuristics, not server-authoritative cooldowns.
 */
final class DeltaEnvironment {
 static final EquipmentSlot[] ARMOR={EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};
 static final class Entry {final ItemStack stack;int hand;long used,missing;Entry(ItemStack stack,int hand){this.stack=stack.copy();this.stack.setDamage(0);this.hand=hand;}int seconds(long now){int total=cooldown(stack);return total==0||used==0?0:Math.max(0,(int)Math.ceil((total*1000L-(now-used))/1000d));}}
 static final class Data {final UUID id;String name;net.minecraft.util.Identifier skin;float hp;long seen;final ItemStack[] armor={ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY};final LinkedHashMap<Item,Entry> history=new LinkedHashMap<>();final ItemStack[] hands={ItemStack.EMPTY,ItemStack.EMPTY};Data(UUID id){this.id=id;}}
 static final Map<UUID,Data> data=new HashMap<>();static List<UUID> shown=List.of();private static UUID current,previous;private static Object world;
 static void tick(MinecraftClient c,LivingEntity target){if(c.world!=world){world=c.world;data.clear();shown=List.of();current=null;previous=null;}if(c.world==null||c.player==null)return;
  if(target instanceof PlayerEntity p&&!p.getUuid().equals(current)){previous=current;current=p.getUuid();}
  var players=c.world.getPlayers().stream().filter(p->p!=c.player&&p.isAlive()&&!InteriumState.friends.contains(p.getUuid())).sorted(Comparator.comparingInt((PlayerEntity p)->p.getUuid().equals(current)?0:p.getUuid().equals(previous)?1:2).thenComparingInt(p->-armorScore(p)).thenComparing(p->p.getName().getString(),String.CASE_INSENSITIVE_ORDER)).limit(2).toList();long now=System.currentTimeMillis();shown=players.stream().map(PlayerEntity::getUuid).toList();
  for(var p:players){Data d=data.computeIfAbsent(p.getUuid(),Data::new);d.name=p.getName().getString();d.hp=p.getHealth();d.skin=p instanceof net.minecraft.client.network.AbstractClientPlayerEntity cp?cp.getSkin().body().texturePath():null;d.seen=now;for(int i=0;i<4;i++)d.armor[i]=p.getEquippedStack(ARMOR[i]).copy();ItemStack[] hands={p.getMainHandStack(),p.getOffHandStack()};
   for(var entry:d.history.values())if(entry.missing==0&&entry.stack.getCount()==1&&d.hands[entry.hand].isOf(entry.stack.getItem())&&hands[entry.hand].isEmpty())entry.missing=now;
   for(int hand=0;hand<2;hand++){ItemStack stack=hands[hand];if(priority(stack)>0){Entry entry=d.history.remove(stack.getItem());if(entry!=null){boolean dropped=!c.world.getEntitiesByClass(ItemEntity.class,p.getBoundingBox().expand(2),item->item.getStack().isOf(stack.getItem())).isEmpty();if(stack.getCount()<entry.stack.getCount()&&cooldown(stack)>0&&!dropped)entry.used=now;entry.stack.setCount(stack.getCount());entry.hand=hand;entry.missing=0;d.history.put(stack.getItem(),entry);}else{if(d.history.size()>=9){var remove=d.history.entrySet().stream().filter(e->priority(e.getValue().stack)<priority(stack)).findFirst();remove.ifPresent(e->d.history.remove(e.getKey()));}if(d.history.size()<9)d.history.put(stack.getItem(),new Entry(stack,hand));}}d.hands[hand]=stack.copy();}
   d.history.values().removeIf(e->e.missing!=0&&now-e.missing>10000);
  }data.values().removeIf(d->!shown.contains(d.id)&&now-d.seen>30000);
 }
 private static int armorScore(PlayerEntity p){int sum=0;for(var slot:ARMOR){String n=Registries.ITEM.getId(p.getEquippedStack(slot).getItem()).getPath();sum+=n.startsWith("netherite_")?4:n.startsWith("diamond_")?3:n.startsWith("iron_")?2:n.startsWith("leather_")?1:0;}return sum;}
 private static int priority(ItemStack s){if(s.isOf(Items.DRIED_KELP)||s.isOf(Items.NETHERITE_SCRAP)||s.isOf(Items.SNOWBALL)||s.isOf(Items.SUGAR)||s.isOf(Items.PHANTOM_MEMBRANE)||s.isOf(Items.WIND_CHARGE)||s.isOf(Items.ENDER_EYE))return 3;if(s.contains(DataComponentTypes.FOOD))return 2;return s.isOf(Items.EXPERIENCE_BOTTLE)||s.isOf(Items.FIRE_CHARGE)?1:0;}
 private static int cooldown(ItemStack s){if(s.isOf(Items.ENCHANTED_GOLDEN_APPLE))return 150;if(s.isOf(Items.SUGAR)||s.isOf(Items.ENDER_EYE)||s.isOf(Items.FIRE_CHARGE))return 60;if(s.isOf(Items.GOLDEN_APPLE))return 30;if(s.isOf(Items.DRIED_KELP))return 25;return s.isOf(Items.NETHERITE_SCRAP)?15:0;}
}
