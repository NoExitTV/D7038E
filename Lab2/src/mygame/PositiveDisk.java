/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;

/**
 *
 * @author Fredrik Pettersson
 */
public class PositiveDisk extends Disk{

    public PositiveDisk(Vector3f initSpeed, float posX, float posY, float radius, Material mat, SimpleApplication sapp) {
        super(initSpeed, posX, posY, radius, mat, sapp);
        //Create disk and add to rootNode
        createDisk();
        this.points = 5;
        this.isPlayer = false;
    }

    @Override
    public void givePointsTo(Disk otherDisk) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void getPoints(int points) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
