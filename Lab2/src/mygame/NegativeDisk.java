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
public class NegativeDisk extends Disk{
    
    public NegativeDisk(Vector3f initSpeed, float posX, float posY, float radius, Material mat, SimpleApplication sapp) {
        super(initSpeed, posX, posY, radius, mat, sapp);
        //Create disk and add to rootNode
        createDisk();
        this.points = -3;
        this.isPlayer = false;
    }

    @Override
    public void givePointsTo(Disk otherDisk) {
        if(otherDisk.isPlayer) {
            otherDisk.getPoints(this.points);
        }
    }

    @Override
    public void getPoints(int points) {
        //Should not do anything here on negative disks
    }
}
