package interium.fabric;

import interium.core.CameraMotion;
import interium.mixin.InputAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec2f;

/** Experimental local camera. Does not freeze or teleport the server-side body. */
final class FreeCamera {
    private ClientPlayerEntity owner;
    private ClientWorld world;
    private OtherClientPlayerEntity camera;
    private Entity previousCamera;
    private Perspective previousPerspective;
    boolean active(){return camera!=null;}
    void sync(MinecraftClient c) {
        if(active() && (c.player!=owner || c.world!=world || !owner.isAlive() || owner.isRemoved()
                || owner.hasVehicle() || owner.isSleeping() || c.getCameraEntity()!=camera)) {
            InteriumClient.freeCameraEnabled=false;stop(c);return;
        }
        if(!InteriumClient.freeCameraEnabled){stop(c);return;}
        if(!active()) {
            if(c.player==null || c.world==null || !c.player.isAlive() || c.player.hasVehicle() || c.player.isSleeping()) {
                InteriumClient.freeCameraEnabled=false;return;
            }
            if(c.currentScreen!=null || !c.isWindowFocused())return;
            // Do not take an active camera owned by another mod.
            if(c.getCameraEntity()!=c.player){InteriumClient.freeCameraEnabled=false;return;}
            owner=c.player;world=c.world;previousCamera=c.getCameraEntity();previousPerspective=c.options.getPerspective();
            camera=new OtherClientPlayerEntity(world,owner.getGameProfile());
            camera.refreshPositionAndAngles(owner.getX(),owner.getY(),owner.getZ(),owner.getYaw(),owner.getPitch());
            camera.noClip=true;camera.setNoGravity(true);previousPosition();
            owner.setSprinting(false);
            if(c.interactionManager!=null){
                c.interactionManager.cancelBlockBreaking();
                if(owner.isUsingItem())c.interactionManager.stopUsingItem(owner);
            }
            c.options.setPerspective(Perspective.FIRST_PERSON);c.setCameraEntity(camera);
        }
        clearBodyInput();previousPosition();
        if(c.currentScreen!=null || !c.isWindowFocused())return;
        double side=(InteriumClient.physicalPressed(c,c.options.leftKey)?1:0)-(InteriumClient.physicalPressed(c,c.options.rightKey)?1:0);
        double forward=(InteriumClient.physicalPressed(c,c.options.forwardKey)?1:0)-(InteriumClient.physicalPressed(c,c.options.backKey)?1:0);
        double up=(InteriumClient.physicalPressed(c,c.options.jumpKey)?1:0)-(InteriumClient.physicalPressed(c,c.options.sneakKey)?1:0);
        var delta=CameraMotion.step(side,forward,up,camera.getYaw(),InteriumState.freeCameraSpeed);
        var offset=CameraMotion.clampOffset(camera.getX()+delta.x()-owner.getX(),camera.getY()+delta.y()-owner.getY(),camera.getZ()+delta.z()-owner.getZ(),64);
        camera.setPosition(owner.getX()+offset.x(),owner.getY()+offset.y(),owner.getZ()+offset.z());
    }
    void clearBodyInput(){
        if(owner==null)return;
        owner.input.playerInput=PlayerInput.DEFAULT;
        ((InputAccess)owner.input).interium$setMovementVector(Vec2f.ZERO);
    }
    boolean look(Entity entity,double x,double y){
        if(!active() || entity!=owner)return false;
        camera.changeLookDirection(x,y);return true;
    }
    private void previousPosition(){
        camera.lastX=camera.lastRenderX=camera.getX();camera.lastY=camera.lastRenderY=camera.getY();camera.lastZ=camera.lastRenderZ=camera.getZ();
        camera.lastYaw=camera.getYaw();camera.lastPitch=camera.getPitch();
    }
    private void stop(MinecraftClient c){
        if(!active())return;
        clearBodyInput();
        if(c.getCameraEntity()==camera){
            Entity restore=(c.player==owner && c.world==world && previousCamera!=null && !previousCamera.isRemoved())?previousCamera:c.player;
            c.setCameraEntity(restore);
        }
        if(c.options.getPerspective()==Perspective.FIRST_PERSON && previousPerspective!=null)c.options.setPerspective(previousPerspective);
        camera=null;owner=null;world=null;previousCamera=null;previousPerspective=null;
    }
}
