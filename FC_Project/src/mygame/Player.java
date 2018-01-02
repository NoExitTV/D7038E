package mygame;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
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
        player.setPhysicsLocation(new Vector3f(0, 3.5f, 0));

        System.out.println(playerNode);
        animationControl = playerNode.getControl(AnimControl.class);
        animationChannel = animationControl.createChannel();
        
        sapp.getRootNode().attachChild(playerNode);
        bulletAppState.getPhysicsSpace().add(player);

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
}
