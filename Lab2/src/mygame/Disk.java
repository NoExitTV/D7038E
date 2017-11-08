/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
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
    SimpleApplication sapp;
    
    public Disk(Vector3f initSpeed, float posX, float posY, SimpleApplication sapp){
        this.speed = initSpeed;
        this.posX = posX;
        this.posY = posY;
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
    
    public void frameCollision(float radius){
        if(posX + radius >= FREE_AREA_WIDTH/2 || posX - radius < -FREE_AREA_WIDTH/2){
            Vector3f currSpeed = this.getSpeed();
            speed.setX(speed.getX()*-1);
        }
        if(posY + radius >= FREE_AREA_WIDTH/2 || posY - radius < -FREE_AREA_WIDTH/2){
            Vector3f currSpeed = this.getSpeed();
            speed.setY(speed.getY()*-1);
        }
    }
}
