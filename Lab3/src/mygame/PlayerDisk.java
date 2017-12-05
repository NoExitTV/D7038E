/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import static mygame.GameClient.*;

/**
 *
 * @author NoExit
 */
public class PlayerDisk extends Disk{
    float accelerationConst = 50f;

    public PlayerDisk(int id, Vector3f initSpeed, float posX, float posY, float radius, Material mat, SimpleApplication sapp, String text) {
        super(id, initSpeed, posX, posY, radius, mat, sapp);
        //Create disk and add to rootNode
        createDisk();
        createPlayerText(text);
        this.points = 0;
        this.isPlayer = true;
    }

    public void createPlayerText(String text){
        Node textNode = new Node("playerText");
        BitmapFont font = sapp.getAssetManager()
                        .loadFont("Interface/Fonts/Console.fnt");
        BitmapText playerText = new BitmapText(font, false);
        playerText.setSize(font.getCharSet().getRenderedSize() * 2);
        playerText.setQueueBucket(RenderQueue.Bucket.Transparent);
        playerText.setText(text);
        textNode.attachChild(playerText);
        textNode.setLocalTranslation(0, radius/2, DISK_HEIGHT/2 + 1);
        diskNode.attachChild(textNode);
    }
    @Override
    public void givePointsTo(Disk otherDisk) {
        //Should not do anything in player
    }

    @Override
    public void getPoints(int points) {
        this.points += points;
    }
    
    public int returnPoints(){
        return points;
    }
    
    public void accelerateLeft(float tpf) {
        this.setSpeed(this.getSpeed().getX()-accelerationConst*tpf, this.getSpeed().getY());
        //this.setPointSpeedX -= accelerationConst;
    }
    public void accelerateRight(float tpf) {
        this.setSpeed(this.getSpeed().getX()+accelerationConst*tpf, this.getSpeed().getY());
        //this.setPointSpeedX += accelerationConst;
    }
    public void accelerateUp(float tpf) {
        this.setSpeed(this.getSpeed().getX(), this.getSpeed().getY()+accelerationConst*tpf);
        //this.setPointSpeedY += accelerationConst;
    }
    public void accelerateDown(float tpf) {
        this.setSpeed(this.getSpeed().getX(), this.getSpeed().getY()-accelerationConst*tpf);
        //this.setPointSpeedY -= accelerationConst;
    }
}
