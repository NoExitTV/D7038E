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
    float mass;
    Material mat;
    SimpleApplication sapp;
    
    public Disk(Vector3f initSpeed, float posX, float posY, float radius, Material mat, SimpleApplication sapp){
        this.speed = initSpeed;
        this.posX = posX;
        this.posY = posY;
        this.radius = radius;
        this.mat = mat;
        this.sapp = sapp;
        this.mass = radius;
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
    
    public boolean checkCollisionWith(Disk otherDisk) {
       float myPosX = diskNode.getLocalTranslation().getX();
        float myPosY = diskNode.getLocalTranslation().getY();
        float otherPosX = otherDisk.diskNode.getLocalTranslation().getX();
        float otherPosY = otherDisk.diskNode.getLocalTranslation().getY();
        float otherRadius = otherDisk.radius;
        double hypo = Math.pow((myPosX-otherPosX), 2) + Math.pow((myPosY-otherPosY), 2);
        double distance = Math.pow((this.radius+otherDisk.radius), 2);
        
        if(hypo <= distance) {
            return true;
        }
        return false;
    }
    
    public void cylinderCollision(Disk otherDisk, float deltaT) {

        //if(checkCollisionWith(otherDisk)) {
            float myPosX = diskNode.getLocalTranslation().getX();
            float myPosY = diskNode.getLocalTranslation().getY();
            float otherPosX = otherDisk.diskNode.getLocalTranslation().getX();
            float otherPosY = otherDisk.diskNode.getLocalTranslation().getY();
            //float otherRadius = otherDisk.radius;

            double hypo = Math.pow((myPosX-otherPosX), 2) + Math.pow((myPosY-otherPosY), 2);
            double distance = Math.pow((this.radius+otherDisk.radius), 2);

            System.out.println("##COLLISION##");

            //Get normal vector
            Vector3f nVector = new Vector3f(myPosX - otherPosX, myPosY-otherPosY, 0);
            //Get magnitude of the normal vector
            float nMagn = (float) Math.sqrt(Math.pow(nVector.getX(), 2) + Math.pow(nVector.getY(), 2));
            //Calculate unit normal vecot
            Vector3f unVector = new Vector3f(nVector.getX()/nMagn, nVector.getY()/nMagn, 0);
            //Unit tangent vector
            Vector3f utVector = new Vector3f(unVector.getY()*-1, unVector.getX(), 0);

            //Calculate tangent and normal speed for both disks
            float v1n = unVector.dot(this.speed);
            float v1t = utVector.dot(this.speed);
            float v2n = unVector.dot(otherDisk.speed);
            float v2t = utVector.dot(otherDisk.speed);

            //Calculate normal speed after the collision (tangent speed stays the same)
            float v1nAfter = (v1n*(this.mass-otherDisk.mass)+2*otherDisk.mass*v2n)/(this.mass+otherDisk.mass);
            float v2nAfter = (v2n*(otherDisk.mass-this.mass)+2*this.mass*v1n)/(this.mass+otherDisk.mass);

            //Convert scalar normal and tangent speed into vectors
            Vector3f v1nVector = new Vector3f(unVector.mult(v1nAfter));
            Vector3f v1tVector = new Vector3f(utVector.mult(v1t));

            Vector3f v2nVector = new Vector3f(unVector.mult(v2nAfter));
            Vector3f v2tVector = new Vector3f(unVector.mult(v2t));

            //Find final velocity vector and set speed
            Vector3f v1Final = new Vector3f(v1nVector.add(v1tVector));
            Vector3f v2Final = new Vector3f(v2nVector.add(v2tVector));
            this.setSpeed(v1Final);
            otherDisk.setSpeed(v2Final); 
            
            this.diskNode.move(this.getSpeed().getX()*deltaT, this.getSpeed().getY()*deltaT, 0);
            otherDisk.diskNode.move(otherDisk.getSpeed().getX()*deltaT, otherDisk.getSpeed().getY()*deltaT, 0);
        //}
    }
    
    public void moveBeforeCollision(Disk otherDisk, float tpf) {
        float deltaT = 0;
        while(checkCollisionWith(otherDisk)){
            this.diskNode.move(this.getSpeed().getX()*tpf*-1, this.getSpeed().getY()*tpf*-1, 0);
            otherDisk.diskNode.move(otherDisk.getSpeed().getX()*tpf*-1, otherDisk.getSpeed().getY()*tpf*-1, 0);
            deltaT += tpf;
        }
        System.out.println(deltaT);
        cylinderCollision(otherDisk, deltaT*1.1f);
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
