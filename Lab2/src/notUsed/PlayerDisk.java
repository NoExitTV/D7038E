/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package notUsed;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Cylinder;

/**
 *
 * @author NoExit
 */
public class PlayerDisk {
   Node playerNode = new Node("player node");
   Material playerMat;
   Geometry cylinder;
   
   public PlayerDisk(float PLAYER_R, float PLAYER_HEIGHT, Material playerMat){
       this.playerMat = playerMat;
       Cylinder cylMesh = new Cylinder(40, 40, PLAYER_R, PLAYER_HEIGHT, true);
       this.cylinder = new Geometry("player cylinder", cylMesh);
       playerMat.setColor("Color", ColorRGBA.Blue);
       cylinder.setMaterial(playerMat);

       playerNode.attachChild(cylinder);

   }
   public Node getNode(){
       return playerNode;
   }
   
   public Geometry getCylinder(){
       return cylinder;
   }
}
