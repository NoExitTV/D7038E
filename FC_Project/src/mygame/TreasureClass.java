/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;

/**
 *
 * @author Fredrik Pettersson & Carl Borngrund
 */
public class TreasureClass {
    Box box;
    Spatial texture;
    Material material;
    Node boxNode;
    int points;
    float x;
    float y;
    float z;
    GhostControl gc;
    BoxCollisionShape collisionShape;
    SimpleApplication sapp;
    BulletAppState bapp;
    AudioNode pulse_sound;
    AudioNode capture_sound;
    
    public TreasureClass(SimpleApplication sapp, BulletAppState bapp, float x, float y, float z, int points) {
        this.sapp = sapp;
        this.bapp = bapp;
        this.x = x;
        this.y = y;
        this.z = z;
        this.points = points;
        
        boxNode = new Node();
        boxNode.setLocalTranslation(this.x, this.y, this.z);
        collisionShape = new BoxCollisionShape(new Vector3f(1f, 1f, 1f));
        
        gc = new GhostControl(collisionShape);
        
        // Setup gc to only collide with other players
        gc.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_05);
        gc.addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_10);
        
        // Remove default collision group
        gc.removeCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_01);
        
        box = new Box(1f, 1f, 1f);
        texture = new Geometry("Box", box);
        material = new Material(sapp.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        material.setTexture("ColorMap", sapp.getAssetManager().loadTexture("Textures/book_texture.jpg"));
        texture.setMaterial(material);
        
        boxNode.attachChild(texture);
        boxNode.addControl(gc);
        
        bapp.getPhysicsSpace().add(gc);
        
        sapp.getRootNode().attachChild(boxNode);
        
        //Create the sound node
        pulse_sound = new AudioNode(sapp.getAssetManager(), "Sound/treasurePulse_MONO.ogg", AudioData.DataType.Buffer);
        capture_sound = new AudioNode(sapp.getAssetManager(), "Sound/captureTreasure_MONO.ogg", AudioData.DataType.Buffer);
        
        // Setup sound variables
        pulse_sound.setPositional(true);
        pulse_sound.setLooping(true);
        pulse_sound.setVolume(1);
        capture_sound.setPositional(true);
        capture_sound.setLooping(false);
        capture_sound.setVolume(1);
        
        // Add sound to boxNode
        boxNode.attachChild(pulse_sound);
        boxNode.attachChild(capture_sound);
    }
    
    public Vector3f getPosition() {
        return boxNode.getLocalTranslation();
    }
    
    public void playPulse(){
        pulse_sound.play();
    }
    
    public void stopPulse(){
        pulse_sound.stop();
    }
    
    public void playCapture(){
        capture_sound.playInstance();
    }
}
