package interium.fabric;

import interium.mixin.MinecraftClientAccess;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import org.lwjgl.glfw.GLFW;

/** Experimental client with a mix of standalone modules and Delta-adapted features. */
public final class InteriumClient implements ClientModInitializer {
    static boolean tntTimerEnabled, autoClickerEnabled, fastPlaceEnabled, middleClickEnabled;
    static boolean sprintEnabled, safeWalkEnabled, antiFireballEnabled, chestStealerEnabled, bedBreakerEnabled, bedRadarEnabled, triggerBotEnabled, playerRadarEnabled, scaffoldEnabled, autoArmorEnabled;
    static boolean noSlowEnabled, fullBrightEnabled, autoRespawnEnabled, deathCoordsEnabled, auraEnabled, velocityEnabled, autoTotemEnabled, crystalBreakerEnabled, crystalPlacerEnabled, speedEnabled, targetStrafeEnabled, noFriendDamageEnabled, autoExpEnabled;
    static boolean flightEnabled, guiMoveEnabled, noJumpDelayEnabled, noPushEnabled, freeCameraEnabled;
    private static final FreeCamera freeCamera=new FreeCamera();
    public static boolean freeCameraActive(){return freeCamera.active();}
    public static void clearFreeCameraInput(){if(freeCamera.active())freeCamera.clearBodyInput();}
    public static boolean freeCameraLook(Entity e,double x,double y){return freeCamera.look(e,x,y);}
    static void toggleFreeCamera(){freeCameraEnabled=!freeCameraEnabled;freeCamera.sync(MinecraftClient.getInstance());}
    static boolean physicalPressed(MinecraftClient c,KeyBinding key){
        var bound=InputUtil.fromTranslationKey(key.getBoundKeyTranslationKey());
        if(bound.getCode()<0)return false;
        if(bound.getCategory()==InputUtil.Type.KEYSYM)return InputUtil.isKeyPressed(c.getWindow(),bound.getCode());
        return bound.getCategory()==InputUtil.Type.MOUSE && GLFW.glfwGetMouseButton(c.getWindow().getHandle(),bound.getCode())==GLFW.GLFW_PRESS;
    }
    private final Flight flight=new Flight();
    private final Aura aura=new Aura();
    private final Speed speed=new Speed();
    private final TargetStrafe targetStrafe=new TargetStrafe();
    private final AutoExp autoExp=new AutoExp();
    private final AutoTotem autoTotem=new AutoTotem();
    private final CrystalBreaker crystalBreaker=new CrystalBreaker();
    private final CrystalPlacer crystalPlacer=new CrystalPlacer();
    public static boolean cancelVelocity(){return velocityEnabled;}
    public static boolean blockFriendAttack(PlayerEntity player,Entity target){
        return noFriendDamageEnabled && player==MinecraftClient.getInstance().player && target instanceof PlayerEntity && InteriumState.friends.contains(target.getUuid());
    }
    private boolean deathAnnounced;
    private int respawnDelay;
    private final BedFeatures bedFeatures = new BedFeatures();
    private final Scaffold scaffold = new Scaffold();
    private final AutoArmor autoArmor = new AutoArmor();
    private KeyBinding openMenu, autoExpHold;
    private int attackAccumulator, fireballCooldown, chestCooldown;
    private boolean previousMiddle;
    private boolean fontProbeShown;

    @Override public void onInitializeClient() {
        InteriumState.load();
        DeltaVisualSettings.load();
        KeyBinding.Category controls = KeyBinding.Category.create(Identifier.of("interium", "controls"));
        openMenu = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.interium.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, controls));
        autoExpHold = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.interium.auto_exp", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, controls));
        ClientTickEvents.START_CLIENT_TICK.register(freeCamera::sync);
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        HudRenderCallback.EVENT.register(this::renderHud);
    }
    /** Key-press events only. Never toggle binds while a GUI or chat is open. */
    public static void handleModuleBind(int key,int action){
        MinecraftClient client=MinecraftClient.getInstance();
        if(action!=GLFW.GLFW_PRESS||client.player==null||client.world==null||client.currentScreen!=null)return;
        for(int i=0;i<InteriumState.binds.length;i++)for(int j=0;j<InteriumState.binds[i].length;j++)
            if(InteriumState.binds[i][j]!=-1&&InteriumState.binds[i][j]==key)InteriumScreen.toggle(i,j);
    }
    public static boolean removeJumpDelay(net.minecraft.entity.LivingEntity e){return noJumpDelayEnabled && !freeCameraActive() && e==MinecraftClient.getInstance().player;}
    public static boolean avoidEntityPush(Entity a,Entity b){
        var player=MinecraftClient.getInstance().player;
        return noPushEnabled && !freeCameraActive() && player!=null && (a==player||b==player);
    }
    private static boolean guiMoveSyncing;
    /** Narrow GUI Move: only normal player inventory and standard chest screens. */
    public static void syncGuiMove(){
        var c=MinecraftClient.getInstance();
        boolean allowed=guiMoveEnabled && !freeCameraActive() && c.isWindowFocused() && (c.currentScreen instanceof net.minecraft.client.gui.screen.ingame.InventoryScreen || c.currentScreen instanceof GenericContainerScreen);
        if(!allowed && !guiMoveSyncing)return;
        boolean readPhysical=allowed || (c.currentScreen==null && c.isWindowFocused());
        for(var key:new KeyBinding[]{c.options.forwardKey,c.options.backKey,c.options.leftKey,c.options.rightKey,c.options.jumpKey}){
            var bound=InputUtil.fromTranslationKey(key.getBoundKeyTranslationKey());
            boolean pressed=readPhysical && bound.getCategory()==InputUtil.Type.KEYSYM && bound.getCode()>=0 && InputUtil.isKeyPressed(c.getWindow(),bound.getCode());
            if(readPhysical && bound.getCategory()==InputUtil.Type.MOUSE && bound.getCode()>=0)pressed=GLFW.glfwGetMouseButton(c.getWindow().getHandle(),bound.getCode())==GLFW.GLFW_PRESS;
            key.setPressed(pressed);
        }
        guiMoveSyncing=allowed;
    }
    public static boolean bypassItemSlow(){return noSlowEnabled;}
    public static boolean fullBrightGamma(){return fullBrightEnabled&&!InteriumState.nightVision;}
    public static double fullBrightGammaValue(){return InteriumState.gammaLevel;}
    public static boolean fullBrightNightVision(){return fullBrightEnabled&&InteriumState.nightVision;}
    public static boolean suppressMiddlePick(){ return middleClickEnabled && MinecraftClient.getInstance().world!=null && MinecraftClient.getInstance().currentScreen==null; }
    public static boolean useSafeWalk(PlayerEntity player){ return safeWalkEnabled && !freeCameraActive() && MinecraftClient.getInstance().player==player; }
    private void tick(MinecraftClient client) {
        if(!fontProbeShown && client.currentScreen instanceof net.minecraft.client.gui.screen.TitleScreen){
            if(Boolean.getBoolean("interium.fontProbe")){fontProbeShown=true;client.setScreen(new FontProbe());}
            else if(Boolean.getBoolean("interium.deltaProbe")){fontProbeShown=true;client.setScreen(new DeltaScreen());}
        }
        while (openMenu.wasPressed()) if (client.currentScreen == null) client.setScreen(new DeltaScreen());
        // Delta's death features must run while the death screen is open.
        if(client.player==null){deathAnnounced=false;respawnDelay=0;}
        else {
            boolean dying=client.player.deathTime>0;
            if(deathCoordsEnabled && dying && !deathAnnounced){
                var pos=client.player.getBlockPos();
                client.player.sendMessage(Text.literal("Вы погибли на координатах: ["+pos.getX()+", "+pos.getY()+", "+pos.getZ()+"]"),false);
                deathAnnounced=true;
            }
            if(!dying)deathAnnounced=false;
            if(autoRespawnEnabled && client.currentScreen instanceof DeathScreen && client.player.deathTime>=5){
                if(respawnDelay<=0){client.player.requestRespawn();respawnDelay=20;}else respawnDelay--;
            }else respawnDelay=0;
        }
        if(freeCameraActive()){
            autoExp.tick(client,false);aura.clear();
            attackAccumulator=0;fireballCooldown=0;chestCooldown=0;previousMiddle=false;return;
        }
        boolean expActive=autoExp.tick(client,autoExpHold.isPressed());
        bedFeatures.tick(client);
        scaffold.tick(client);
        autoArmor.tick(client);
        autoTotem.tick(client);
        if (client.player != null && client.interactionManager != null && chestStealerEnabled && client.currentScreen instanceof GenericContainerScreen && client.player.currentScreenHandler instanceof GenericContainerScreenHandler) {
            if (chestCooldown > 0) chestCooldown--; else { stealOne(client, (GenericContainerScreenHandler)client.player.currentScreenHandler); chestCooldown = 5; }
        } else chestCooldown=0;
        if (client.player == null || client.world == null || client.interactionManager == null || client.currentScreen != null) {
            attackAccumulator=0; previousMiddle=false; fireballCooldown=0; return;
        }
        if (fastPlaceEnabled) ((MinecraftClientAccess)client).interium$setItemUseCooldown(0);
        if (autoClickerEnabled && client.options.attackKey.isPressed()) {
            attackAccumulator += InteriumState.cps;
            if (attackAccumulator >= 20) { ((MinecraftClientAccess)client).interium$attack(); attackAccumulator -= 20; }
        } else attackAccumulator=0;
        // Delta Sprint: use forward movement, blindness and hunger/creative ability.
        if(sprintEnabled)client.player.setSprinting(client.player.input.getMovementInput().y>0
            && !client.player.hasStatusEffect(StatusEffects.BLINDNESS)
            && (client.player.getAbilities().invulnerable||client.player.getHungerManager().getFoodLevel()>6));
        if(!expActive){
            crystalBreaker.tick(client);
            crystalPlacer.tick(client);
            aura.tick(client);
        }else aura.clear();
        if(!flight.tick(client) && !targetStrafe.tick(client,aura.target()))speed.tick(client);
        if(!expActive && triggerBotEnabled && client.player.getAttackCooldownProgress(0.0f)>=0.95f && client.crosshairTarget instanceof EntityHitResult) {
            Entity target=((EntityHitResult)client.crosshairTarget).getEntity();
            if(target instanceof PlayerEntity && target!=client.player && target.isAlive() && !target.isSpectator()
                    && !client.player.isTeammate(target) && !InteriumState.friends.contains(target.getUuid())
                    && client.player.squaredDistanceTo(target)<=9.0) {
                client.interactionManager.attackEntity(client.player,target); client.player.swingHand(Hand.MAIN_HAND);
            }
        }
        if(fireballCooldown>0) fireballCooldown--;
        if(!expActive && antiFireballEnabled && fireballCooldown==0 && client.crosshairTarget instanceof EntityHitResult) {
            Entity target=((EntityHitResult)client.crosshairTarget).getEntity();
            if(target instanceof FireballEntity && target.isAlive() && client.player.squaredDistanceTo(target)<=9.0) {
                client.interactionManager.attackEntity(client.player,target); client.player.swingHand(Hand.MAIN_HAND); fireballCooldown=5;
            }
        }
        boolean middle = client.options.pickItemKey.isPressed();
        if (middleClickEnabled && middle && !previousMiddle) useMiddle(client);
        previousMiddle=middle;
    }
    /** Visible chest only, standard shift-click; intentionally no packets bypass or silent inventory. */
    private void stealOne(MinecraftClient client, GenericContainerScreenHandler handler) {
        for (int i=0;i<handler.getRows()*9;i++) {
            ItemStack stack=handler.getSlot(i).getStack();
            if(stack.isEmpty()) continue;
            if(client.player.getInventory().getEmptySlot()<0 && client.player.getInventory().getOccupiedSlotWithRoomForStack(stack)<0) continue;
            client.interactionManager.clickSlot(handler.syncId,i,0,SlotActionType.QUICK_MOVE,client.player);
            return;
        }
    }
    private void useMiddle(MinecraftClient client) {
        if (InteriumState.middleFriend) {
            if (!(client.crosshairTarget instanceof EntityHitResult)) return;
            Entity entity=((EntityHitResult)client.crosshairTarget).getEntity();
            if (!(entity instanceof PlayerEntity)) return;
            boolean added=InteriumState.friends.add(entity.getUuid());
            if (!added) InteriumState.friends.remove(entity.getUuid());
            InteriumState.save();
            client.player.sendMessage(Text.literal("Interium: " + entity.getName().getString() + (added?" added to friends":" removed from friends")),true);
        } else {
            int slot=-1;
            for (int i=0;i<9;i++) if (client.player.getInventory().getStack(i).getItem()==Items.ENDER_PEARL) {slot=i;break;}
            if (slot<0) {client.player.sendMessage(Text.literal("Interium: no pearl in hotbar"),true);return;}
            int old=client.player.getInventory().getSelectedSlot(); client.player.getInventory().setSelectedSlot(slot);
            try {client.interactionManager.interactItem(client.player,Hand.MAIN_HAND);}
            finally {client.player.getInventory().setSelectedSlot(old);}
        }
    }
    private void renderPlayerRadar(MinecraftClient c, DrawContext m) {
        if (!playerRadarEnabled || c.world == null || c.player == null) return;
        int y=110, shown=0;
        for(PlayerEntity p:c.world.getPlayers()) {
            if(p==c.player || !p.isAlive() || c.player.squaredDistanceTo(p)>1024) continue;
            int x=Math.max(2,m.getScaledWindowWidth()-200);
            String label=(InteriumState.friends.contains(p.getUuid())?"Friend ":"Player ")+p.getName().getString()+" "+(int)Math.sqrt(c.player.squaredDistanceTo(p))+"m";
            m.fill(x,y-2,x+194,y+12,0xC0181826); m.drawTextWithShadow(c.textRenderer,c.textRenderer.trimToWidth(label,184),x+5,y,0xFFF4F2FF);
            y+=15; if(++shown>=5) break;
        }
    }
    private void renderHud(DrawContext matrices,RenderTickCounter tickDelta) {
        MinecraftClient client=MinecraftClient.getInstance();
        if(!(client.currentScreen instanceof DeltaHudEditor))DeltaHud.INSTANCE.render(client,matrices,aura.target(),false);
        bedFeatures.render(client,matrices);
        renderPlayerRadar(client,matrices);

    }
}
