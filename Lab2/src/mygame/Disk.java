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
public abstract class Disk {
    Vector3f speed;
    float posX;
    float posY;
    Node diskNode;
    float radius;
    Material mat;
    SimpleApplication sapp;
    
    public Disk(Vector3f initSpeed, float posX, float posY, float radius, Material mat, SimpleApplication sapp){
        this.speed = initSpeed;
        this.posX = posX;
        this.posY = posY;
        this.radius = radius;
        this.mat = mat;
        this.sapp = sapp;
    }
    
    public Node getNode(){
        return diskNode;
    }
    
    public void setSpeed(Vector3f speed){
        this.speed = speed;
    }
    
    public Vector3f getSpeed(){
        return speed;
    }
    
    public void cylinderCollision(Disk otherDisk) {
        float myPosX = diskNode.getLocalTranslation().getX();
        float myPosY = diskNode.getLocalTranslation().getY();
        float otherPosX = otherDisk.diskNode.getLocalTranslation().getX();
        float otherPosY = otherDisk.diskNode.getLocalTranslation().getY();
        float otherRadius = otherDisk.radius;
        
        double hypo = Math.pow((myPosX-otherPosX), 2) + Math.pow((myPosY-otherPosY), 2);
        double distance = Math.pow((radius+otherDisk.radius), 2);
       
        if(hypo <= distance) {
            System.out.println("##COLLISION##");
            this.speed.setX(this.speed.getX()*-1);
            this.speed.setY(this.speed.getY()*-1);
            otherDisk.speed.setX(otherDisk.speed.getX()*-1);
            otherDisk.speed.setY(otherDisk.speed.getY()*-1);
        }
        
    }
    public void frameCollision(float radius){
        if(diskNode.getLocalTranslation().getX() + radius >= FREE_AREA_WIDTH/2){
            speed.setX(speed.getX()*-1);
            diskNode.setLocalTranslation(FREE_AREA_WIDTH/2 - radius - 2f, diskNode.getLocalTranslation().getY(), 0);
        }
        else if(diskNode.getLocalTranslation().getX() - radius <= -FREE_AREA_WIDTH/2){
            speed.setX(speed.getX()*-1);
            diskNode.setLocalTranslation(-FREE_AREA_WIDTH/2 + radius + 2f, diskNode.getLocalTranslation().getY(), 0);
        }
        if (diskNode.getLocalTranslation().getY() + radius >= FREE_AREA_WIDTH/2) {
            speed.setY(speed.getY()*-1);
            diskNode.setLocalTranslation(diskNode.getLocalTranslation().getX(), FREE_AREA_WIDTH/2 - radius - 2f, 0);
        }
        else if(diskNode.getLocalTranslation().getY() - radius <= -FREE_AREA_WIDTH/2) {
            speed.setY(speed.getY()*-1);
            diskNode.setLocalTranslation(diskNode.getLocalTranslation().getX(), -FREE_AREA_WIDTH/2 + radius + 2f, 0);
        }
        /*
        if(posX + radius >= FREE_AREA_WIDTH/2 || posX - radius < -FREE_AREA_WIDTH/2){
            speed.setX(speed.getX()*-1);
        }
        if(posY + radius >= FREE_AREA_WIDTH/2 || posY - radius < -FREE_AREA_WIDTH/2){
            speed.setY(speed.getY()*-1);
        }
        */
    }
    public void createDisk(){
        
       //Material negDiskMat = new Material(sapp.getAssetManager(),
          //"Common/MatDefs/Misc/Unshaded.j3md");
       Cylinder cylMesh = new Cylinder(40, 40, radius, DISK_HEIGHT, true);
       Geometry cylinder = new Geometry("player cylinder", cylMesh);
       //negDiskMat.setColor("Color", ColorRGBA.Red);
       cylinder.setMaterial(this.mat);
       
       
       //Create node
       diskNode = new Node("negDiskNode");
       diskNode.attachChild(cylinder);
       
       //Set location
       diskNode.setLocalTranslation(posX, posY, DISK_HEIGHT/2);
       
       //Add diskNode to rootNode
       sapp.getRootNode().attachChild(diskNode);
    }
}
