/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 *
 * @author Fredrik Pettersson & Carl Borngrund
 */
public class Landscape {
    
    SimpleApplication sapp;
    BulletAppState bulletAppState;
    private Spatial sceneModel;
    private RigidBodyControl landscape;
    
    /**
     * This class creates the landscape and adds itself to the root node and BulletAppState
     * @param sapp
     * @param bulletAppState 
     */
    public Landscape(SimpleApplication sapp, BulletAppState bulletAppState) {
        this.sapp = sapp;
        this.bulletAppState = bulletAppState;
        
        // We load the scene from the zip file and adjust its size.
        sapp.getAssetManager().registerLocator("assets/town.zip", ZipLocator.class);
        sceneModel = sapp.getAssetManager().loadModel("main.scene");
        sceneModel.setLocalScale(2f);

        // We set up collision detection for the scene by creating a
        // compound collision shape and a static RigidBodyControl with mass zero.
        CollisionShape sceneShape =
                CollisionShapeFactory.createMeshShape((Node) sceneModel);
        landscape = new RigidBodyControl(sceneShape, 0);
        sceneModel.addControl(landscape);
        
        /* Add itself to root node */
        sapp.getRootNode().attachChild(sceneModel);
        bulletAppState.getPhysicsSpace().add(landscape);
    }
}
