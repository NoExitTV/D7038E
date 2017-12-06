/**
 *
 * @author Fredrik Pettersson & Carl Borngrund
 */
package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;


public class NegativeDisk extends Disk{
    
    public NegativeDisk(int id, Vector3f initSpeed, float posX, float posY, float radius, Material mat, SimpleApplication sapp) {
        super(id, initSpeed, posX, posY, radius, mat, sapp);
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
