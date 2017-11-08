/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

/**
 *
 * @author Fredrik Pettersson
 */
public class Board extends SimpleApplication {
    
    // thickness of the sides of the frame
    static final float FRAME_THICKNESS = 24f; 
    // width (and height) of the free area inside the frame, where disks move
    static final float FREE_AREA_WIDTH = 492f; 
    // total outer width (and height) of the frame
    static final float FRAME_SIZE = FREE_AREA_WIDTH + 2f * FRAME_THICKNESS; 
    // Geometrys
    protected Geometry floor;
    protected Geometry leftFrame;
    protected Geometry topFrame;
    protected Geometry rightFrame;
    protected Geometry bottomFrame;
    
    // Nodes
    Node boardNode = new Node("board node");
    
    public static void main(String[] args) {
        Board app = new Board();
        app.start();
    }
    
    public void createBoard(){
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
        Box frameMesh = new Box(FREE_AREA_WIDTH/2, FRAME_THICKNESS, FRAME_THICKNESS);
        
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
        topFrameNode.setLocalTranslation(0, FREE_AREA_WIDTH/2, 0);
        bottomFrameNode.setLocalTranslation(0, -FREE_AREA_WIDTH/2, 0);
        leftFrameNode.setLocalTranslation(-FREE_AREA_WIDTH/2, 0, 0);
        rightFrameNode.setLocalTranslation(FREE_AREA_WIDTH/2, 0, 0);
        
        // Add things to boxNode
        boardNode.attachChild(floorNode);
        boardNode.attachChild(bottomFrameNode);
        boardNode.attachChild(topFrameNode);
        boardNode.attachChild(leftFrameNode);
        boardNode.attachChild(rightFrameNode);
        
        
    }

    @Override
    public void simpleInitApp() {
        flyCam.setMoveSpeed(500f);
        createBoard();
        
        rootNode.attachChild(boardNode);
        //boxNode.setLocalTranslation(0.0f, 0.0f, -500.0f);
        
        cam.setLocation(new Vector3f(-84f, 0.0f, 720f));
        cam.setRotation(new Quaternion(0.0f, 1.0f, 0.0f, 0.0f));
    }
    
    
}
