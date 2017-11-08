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
    
    public NegativeDisk(Vector3f initSpeed, float posX, float posY, SimpleApplication sapp) {
        super(initSpeed, posX, posY, sapp);
        //Create disk and add to rootNode
        createDisk();
    }
    
    public void createDisk(){
        
       Material negDiskMat = new Material(sapp.getAssetManager(),
          "Common/MatDefs/Misc/Unshaded.j3md");
       Cylinder cylMesh = new Cylinder(40, 40, NEGDISK_R, DISK_HEIGHT, true);
       Geometry cylinder = new Geometry("player cylinder", cylMesh);
       negDiskMat.setColor("Color", ColorRGBA.Red);
       cylinder.setMaterial(negDiskMat);
       
       //Create node
       diskNode = new Node("negDiskNode");
       diskNode.attachChild(cylinder);
       
       //Set location
       diskNode.setLocalTranslation(posX, posY, DISK_HEIGHT/2);
       
       //Add diskNode to rootNode
       sapp.getRootNode().attachChild(diskNode);
    }
    
}
