/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.SimpleApplication;
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
    }
    
    public Vector3f getPosition() {
        return boxNode.getLocalTranslation();
    }
}
