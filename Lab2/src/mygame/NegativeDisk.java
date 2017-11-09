/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Cylinder;
import static mygame.Game.*;

/**
 *
 * @author Fredrik Pettersson
 */
public class NegativeDisk extends Disk{
    
    public NegativeDisk(Vector3f initSpeed, float posX, float posY, float radius, Material mat, SimpleApplication sapp) {
        super(initSpeed, posX, posY, radius, mat, sapp);
        //Create disk and add to rootNode
        createDisk();
    }
}
