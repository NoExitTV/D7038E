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
import com.jme3.scene.shape.Sphere;
import java.util.ArrayList;
import static mygame.GameClient.*;


/**
 *
 * @author Fredrik Pettersson
 */
public class PositiveDisk extends Disk{
    ArrayList<Geometry> pointList = new ArrayList<Geometry>();
    Node pointNode = new Node();

    public PositiveDisk(int id, Vector3f initSpeed, float posX, float posY, float radius, Material mat, SimpleApplication sapp) {
        super(id, initSpeed, posX, posY, radius, mat, sapp);
        //Create disk and add to rootNode
        createDisk();
        createPoints();
        this.points = 5;
        this.isPlayer = false;
    }

    @Override
    public void givePointsTo(Disk otherDisk) {
        if(otherDisk.isPlayer && this.points > 0) {
            otherDisk.getPoints(this.points);
            this.points -= 1;
            pointList.remove(0).removeFromParent();
        }
    }

    @Override
    public void getPoints(int points) {
        //Should not do anything in positiveDisk class
    }
    public Geometry createPointGeometry() {
        Sphere point = new Sphere(32, 32, 2f, true, false);
        Geometry whitePoint = new Geometry("points", point);
        Material white = new Material(sapp.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        white.setColor("Color", ColorRGBA.White);
        whitePoint.setMaterial(white);
        return whitePoint;
    }
    
    public void createPoints() {
        
        //Create 5 white points and add to pointList
        for(int i=0; i<5; i++) {
            pointList.add(createPointGeometry());
        }
        
        //Set location for each point
        pointList.get(0).setLocalTranslation(0, 0, 0);
        pointList.get(1).setLocalTranslation(-5, 5, 0);
        pointList.get(2).setLocalTranslation(5, 5, 0);
        pointList.get(3).setLocalTranslation(-5, -5, 0);
        pointList.get(4).setLocalTranslation(5, -5, 0);
        
        //Add each point to the pointNode
        for(int i=0; i<pointList.size(); i++) {
            pointNode.attachChild(pointList.get(i));
        }
        
        //Set location for pointNode
        pointNode.setLocalTranslation(0, 0, DISK_HEIGHT/2);
        diskNode.attachChild(pointNode);
    }
}
