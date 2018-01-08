package mygame;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import static mygame.GameClient.JUMPSPEED;
import static mygame.GameClient.FALLSPEED;
import static mygame.GameClient.GRAVITY;



public class Player {
    int playerId;
    Node playerNode;
    AnimControl animationControl;
    AnimChannel animationChannel;
    float airTime = 0;
    CapsuleCollisionShape capsuleShape;
    CharacterControl player;
    SimpleApplication sapp;
    BulletAppState bulletAppState;
    Vector3f walkDirection;
    float setPointX;
    float setPointY;
    float setPointZ;
    GhostControl gc;
    RigidBodyControl rigidBody;
    
    
    public Player(SimpleApplication sapp, int id, BulletAppState bulletAppState) {
        playerId = id;
        this.sapp = sapp;
        this.bulletAppState = bulletAppState;
        playerNode = (Node) sapp.getAssetManager().loadModel("Models/Oto/Oto.mesh.xml");

        capsuleShape = new CapsuleCollisionShape(1.5f, 2f);
        
        player = new CharacterControl(capsuleShape, 0.05f);
        player.setJumpSpeed(JUMPSPEED);
        player.setFallSpeed(FALLSPEED);
        player.setGravity(GRAVITY);
        
        playerNode.addControl(player);
        playerNode.setLocalScale(0.5f);
        player.setPhysicsLocation(new Vector3f(0, 0.5f, 0));

        animationControl = playerNode.getControl(AnimControl.class);
        animationChannel = animationControl.createChannel();
        
        sapp.getRootNode().attachChild(playerNode);
        bulletAppState.getPhysicsSpace().add(player);
        
        walkDirection = new Vector3f(0,0,0);
        
        // Set initial setpoint values
        setPointX = player.getPhysicsLocation().getX();
        setPointY = player.getPhysicsLocation().getY();
        setPointZ = player.getPhysicsLocation().getZ();
        
        
        gc = new GhostControl(capsuleShape);
        gc.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_10);
        //gc.addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
        
        // Remove default collision group
        gc.removeCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_01);
        
        playerNode.addControl(gc);
        bulletAppState.getPhysicsSpace().add(gc);
        
        this.bulletAppState.setDebugEnabled(true);
    }
    
    public Node getNode() {
        return playerNode;
    }
    
    public CharacterControl getCharacterControl() {
        return player;
    }
    
    public AnimChannel getAnimationChannel() {
        return animationChannel;
    }
    
    public void setWalkDrirection(float posX, float posY, float posZ) {
        walkDirection.setX(posX);
        walkDirection.setY(posY);
        walkDirection.setZ(posZ);
    }
    
    public void setWalkDirection(Vector3f walkDirection) {
        this.walkDirection = walkDirection;
    }
    
    public Vector3f getWalkDirection() {
        return walkDirection;
    }
}
