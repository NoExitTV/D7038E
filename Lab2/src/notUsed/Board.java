/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package notUsed;

import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResults;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
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
    // radius of a player's disk
    static final float PLAYER_R = 20f;
    
    //Player object
    PlayerDisk player;
    
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
        Box frameMesh = new Box(FREE_AREA_WIDTH/2+FRAME_THICKNESS/2, FRAME_THICKNESS/2, FRAME_THICKNESS/2);
        
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
        topFrameNode.setLocalTranslation(0, FREE_AREA_WIDTH/2, FRAME_THICKNESS/2);
        bottomFrameNode.setLocalTranslation(0, -FREE_AREA_WIDTH/2, FRAME_THICKNESS/2);
        leftFrameNode.setLocalTranslation(-FREE_AREA_WIDTH/2, 0, FRAME_THICKNESS/2);
        rightFrameNode.setLocalTranslation(FREE_AREA_WIDTH/2, 0, FRAME_THICKNESS/2);
        
        // Add things to boxNode
        boardNode.attachChild(floorNode);
        boardNode.attachChild(bottomFrameNode);
        boardNode.attachChild(topFrameNode);
        boardNode.attachChild(leftFrameNode);
        boardNode.attachChild(rightFrameNode);
    }

    public void createPlayerObject(float radius, float height){
        Material playerMat = new Material(assetManager,
          "Common/MatDefs/Misc/Unshaded.j3md");
        
        //Create player object
        player = new PlayerDisk(radius, height, playerMat);
        
        //Attach playerNode to boardNode
        player.getNode().setLocalTranslation(0f, 0f, height/2);
        boardNode.attachChild(player.getNode());
    }
    
    /** Custom Keybinding: Map named actions to inputs. */
    private void initKeys() {
        // You can map one or several inputs to one named action
        inputManager.addMapping("moveY",    new KeyTrigger(KeyInput.KEY_Y));
        inputManager.addMapping("moveH",    new KeyTrigger(KeyInput.KEY_H));
        inputManager.addMapping("moveG",    new KeyTrigger(KeyInput.KEY_G));
        inputManager.addMapping("moveJ",    new KeyTrigger(KeyInput.KEY_J));
        // Add the names to the action listener.
        //inputManager.addListener(actionListener,"move");
        inputManager.addListener(analogListener,"moveY", "moveH", "moveG", "moveJ");
    }
    
    private AnalogListener analogListener = new AnalogListener() {
        public void onAnalog(String name, float value, float tpf) {
            if(name.equals("moveY")){
                player.getNode().move(0f, 0.5f+tpf, 0f);
                //boardNode.getChild("player node").move(0f, 1f, 0f);
            }
            if(name.equals("moveG")){
                player.getNode().move(-(0.5f+tpf), 0f, 0f);
                //boardNode.getChild("player node").move(0f, 1f, 0f);
            }
            if(name.equals("moveH")){
                player.getNode().move(0f, -(0.5f+tpf), 0f);
                //boardNode.getChild("player node").move(0f, 1f, 0f);
            }
            if(name.equals("moveJ")){
                player.getNode().move(0.5f+tpf, 0f, 0f);
                //boardNode.getChild("player node").move(0f, 1f, 0f);
            }
        }
    };
    
    private ActionListener actionListener = new ActionListener() {
        public void onAction(String name, boolean keyPressed, float tpf) {
            if (name.equals("someBind") && !keyPressed) {
                //Do something
            }
        }
    };
    
    public void simpleInitApp() {
        flyCam.setMoveSpeed(500f);
        
        //Create the board with frames
        createBoard();
        //Create player object
        createPlayerObject(PLAYER_R, FRAME_THICKNESS);
        //Initialize key bindings
        initKeys();
        
        rootNode.attachChild(boardNode);
        
        //Set cam location
        cam.setLocation(new Vector3f(-84f, 0.0f, 720f));
        cam.setRotation(new Quaternion(0.0f, 1.0f, 0.0f, 0.0f));
    
    }
    
    
}
