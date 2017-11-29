/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import static mygame.GameClient.*;

/**
 *
 * @author Fredrik Pettersson
 */
public class Frame extends Node {
    AssetManager assetManager;
    SimpleApplication sapp;
    
    // Geometrys
    protected Geometry floor;
    protected Geometry leftFrame;
    protected Geometry topFrame;
    protected Geometry rightFrame;
    protected Geometry bottomFrame;
    
    public Frame(SimpleApplication sapp){
        this.sapp = sapp;
        assetManager = this.sapp.getAssetManager();
        createFrame();
    }
    
    public void createFrame(){
        // Create white play area (floor)
        Box floorMesh = new Box(FREE_AREA_WIDTH/2, FREE_AREA_WIDTH/2, 0.5f);
        floor = new Geometry("board floor", floorMesh);
        Material mat = new Material(assetManager,
          "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.White);
        floor.setMaterial(mat);
        // Create node for floor
        Node floorNode = new Node("floor node");
        floorNode.attachChild(floor);
        //Create material for frames
        Material frameMat = new Material(assetManager,
          "Common/MatDefs/Misc/Unshaded.j3md");
        frameMat.setColor("Color", ColorRGBA.Brown);
        
        //Create mesh for all frames
        Box frameMesh = new Box(FREE_AREA_WIDTH/2+FRAME_THICKNESS, FRAME_THICKNESS/2, FRAME_THICKNESS/2);
        
        //Create top frame
        topFrame = new Geometry("top frame", frameMesh);
        topFrame.setMaterial(frameMat);
        // Create node for topFrame
        Node topFrameNode = new Node("top frame node");
        topFrameNode.attachChild(topFrame);
        
        //Create bottom frame
        bottomFrame = new Geometry("top frame", frameMesh);
        bottomFrame.setMaterial(frameMat);
        // Create node for topFrame
        Node bottomFrameNode = new Node("bottom frame node");
        bottomFrameNode.attachChild(bottomFrame);
        
        //Create left frame
        leftFrame = new Geometry("left frame", frameMesh);
        leftFrame.setMaterial(frameMat);
        // Create node for topFrame
        Node leftFrameNode = new Node("left frame node");
        leftFrameNode.attachChild(leftFrame);
        leftFrameNode.rotate(0, 0, (float) (Math.PI/2));
        
        //Create right frame
        rightFrame = new Geometry("right frame", frameMesh);
        rightFrame.setMaterial(frameMat);
        // Create node for topFrame
        Node rightFrameNode = new Node("right frame node");
        rightFrameNode.attachChild(rightFrame);
        rightFrameNode.rotate(0, 0, (float) (Math.PI/2));
        
        //Set locations
        floorNode.setLocalTranslation(0, 0, -DISK_HEIGHT/2);
        topFrameNode.setLocalTranslation(0, FREE_AREA_WIDTH/2+FRAME_THICKNESS/2, 0);
        bottomFrameNode.setLocalTranslation(0, -FREE_AREA_WIDTH/2-FRAME_THICKNESS/2, 0);
        leftFrameNode.setLocalTranslation(-FREE_AREA_WIDTH/2-FRAME_THICKNESS/2, 0, 0);
        rightFrameNode.setLocalTranslation(FREE_AREA_WIDTH/2+FRAME_THICKNESS/2, 0, 0);
        
        // Add frameNodes to rootnode
        sapp.getRootNode().attachChild(floorNode);
        sapp.getRootNode().attachChild(bottomFrameNode);
        sapp.getRootNode().attachChild(topFrameNode);
        sapp.getRootNode().attachChild(leftFrameNode);
        sapp.getRootNode().attachChild(rightFrameNode);
    }
}
