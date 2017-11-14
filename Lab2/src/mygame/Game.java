package mygame;
        
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

class Game extends BaseAppState {

    private Geometry geomBox;
    private SimpleApplication sapp;
    private boolean needCleaning = false;
    
    // thickness of the sides of the frame
    static final float FRAME_THICKNESS = 24f; 
    // width (and height) of the free area inside the frame, where disks move
    static final float FREE_AREA_WIDTH = 492f; 
    // total outer width (and height) of the frame
    static final float FRAME_SIZE = FREE_AREA_WIDTH + 2f * FRAME_THICKNESS;
    // Disk height
    static final float DISK_HEIGHT = FRAME_THICKNESS;

    // next three constants define initial positions for disks
    static final float PLAYER_COORD = FREE_AREA_WIDTH / 6;
    static final float POSNEG_MAX_COORD = FREE_AREA_WIDTH / 3;
    static final float POSNEG_BETWEEN_COORD = PLAYER_COORD;

    static final float PLAYER_R = 20f; // radius of a player's disk
    static final float POSDISK_R = 16f; // radius of a positive disk
    static final float NEGDISK_R = 16f; // radius of a negative disk
    
    static final float FRICTION = 0.001f;
    
    static final float MIN_START_SPEED = -5f;
    static final float MAX_START_SPEED = 5f;
    
    ArrayList<Disk> diskList;
    ArrayList<float[]> posPos = new ArrayList<float[]>();
    ArrayList<float[]> negPos = new ArrayList<float[]>();
    PlayerDisk playerVariable;
    
    BitmapText scoreText;

    @Override
    protected void initialize(Application app) {
        sapp = (SimpleApplication) app;
        System.out.println("Game: initialize");
        initKeys();
        initPositions();
    }

    @Override
    protected void cleanup(Application app) {
        System.out.println("Game: cleanup");
    }

    @Override
    protected void onEnable() {
        System.out.println("Game: onEnable");
        if (needCleaning) {
            System.out.println("(Cleaning up)");
            sapp.getRootNode().detachAllChildren();
            needCleaning = false;
        }
        System.out.println("(Creating the scenegraph etc from scratch)");

        diskList = new ArrayList<Disk>();
        
        //Create text print
        BitmapFont myFont
                = sapp.getAssetManager()
                        .loadFont("Interface/Fonts/Console.fnt");
        scoreText = new BitmapText(myFont, false);
        scoreText.setSize(myFont.getCharSet().getRenderedSize() * 2);
        scoreText.setColor(ColorRGBA.White);
        scoreText.setLocalTranslation(10, FREE_AREA_WIDTH, 0);
        sapp.getGuiNode().attachChild(scoreText);
        scoreText.setText("Player 1: 0p");
        
        //Create frame and add to rootNode
        Frame frame = new Frame(sapp);
        
        //Create negative disks material
        Material negDiskMat = new Material(sapp.getAssetManager(),
          "Common/MatDefs/Misc/Unshaded.j3md");
        negDiskMat.setColor("Color", ColorRGBA.Red);
        
        //Create positive disk material
        Material posDiskMat = new Material(sapp.getAssetManager(),
          "Common/MatDefs/Misc/Unshaded.j3md");
        posDiskMat.setColor("Color", ColorRGBA.Green);
        
        //Random used to generate random start speed
        Random r = new Random();
        
        //Create negative disks
        for(int i=0; i<negPos.size(); i++){
            //Randomize speed in vector here...
            float randomX = MIN_START_SPEED + r.nextFloat() * (MAX_START_SPEED - MIN_START_SPEED);
            float randomY = MIN_START_SPEED + r.nextFloat() * (MAX_START_SPEED - MIN_START_SPEED);
            Vector3f nVector = new Vector3f(randomX, randomY, 0f);
            NegativeDisk nDisk = new NegativeDisk(nVector, negPos.get(i)[0], negPos.get(i)[1], NEGDISK_R, negDiskMat, sapp);
            diskList.add(nDisk);
        }
        
        //Create positive disks
        for(int i=0; i<posPos.size(); i++){
            //Randomize speed in vector here...
            float randomX = MIN_START_SPEED + r.nextFloat() * (MAX_START_SPEED - MIN_START_SPEED);
            float randomY = MIN_START_SPEED + r.nextFloat() * (MAX_START_SPEED - MIN_START_SPEED);
            Vector3f pVector = new Vector3f(randomX, randomY, 0f);
            PositiveDisk pDisk = new PositiveDisk(pVector, posPos.get(i)[0], posPos.get(i)[1], POSDISK_R, posDiskMat, sapp);
            diskList.add(pDisk);
        }
       
        
        //Create player disk
        Material playDiskMat = new Material(sapp.getAssetManager(),
          "Common/MatDefs/Misc/Unshaded.j3md");
        playDiskMat.setColor("Color", ColorRGBA.Blue);
        
        Vector3f playVector = new Vector3f(0f, 0f, 0f);
        PlayerDisk playDisk1 = new PlayerDisk(playVector, 0f, 0f, PLAYER_R, playDiskMat, sapp, "1");
        diskList.add(playDisk1);
        
        this.playerVariable = playDisk1;
    }

    @Override
    protected void onDisable() {
        System.out.println("Game: onDisable");
        needCleaning = true;
    }

    @Override
    public void update(float tpf) {
        
        for(Disk disk : diskList){  
                     
            //Check for frame collision
            disk.frameCollision(disk.radius);
                        
            //Check for disk collision
            for(Disk disk2 : diskList){
                if(!disk.equals(disk2)) {
                    if(disk.checkCollisionWith(disk2)) {
                        disk.calcTSinceCollision(disk2, tpf);
                    }
                }
                else {
                    disk.diskNode.move(disk.getSpeed().getX()*tpf, disk.getSpeed().getY()*tpf, 0);
                    disk.applyFrictionX();
                    disk.applyFrictionY();
                    }
            }
            scoreText.setText("Player 1: "+Integer.toString(playerVariable.returnPoints())+"p");
            /*
            //Move disk
            disk.diskNode.setLocalTranslation(disk.diskNode.getLocalTranslation().getX() + disk.getSpeed().getX()*tpf
                ,disk.diskNode.getLocalTranslation().getY()+disk.getSpeed().getY()*tpf, 0);
            */
        }
    }
    /** Custom Keybinding: Map named actions to inputs. */
    private void initKeys() {
        // You can map one or several inputs to one named action
        sapp.getInputManager().addMapping("left",    new KeyTrigger(KeyInput.KEY_G));
        sapp.getInputManager().addMapping("right",   new KeyTrigger(KeyInput.KEY_J));
        sapp.getInputManager().addMapping("up",      new KeyTrigger(KeyInput.KEY_Y));
        sapp.getInputManager().addMapping("down",    new KeyTrigger(KeyInput.KEY_H));
        //sapp.getInputManager().addMapping("shoot",        new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        // Add the names to the action listener.
        //sapp.getInputManager().addListener(actionListener,"sysRotate", "toggle_F_B", "orbitRotate", "explode", "shoot");
        sapp.getInputManager().addListener(analogListener,"left", "right", "up", "down");
    }

    private void initPositions() {
        
        //Define positive disk positions
        posPos.add(new float[]{-POSNEG_MAX_COORD, POSNEG_MAX_COORD});
        posPos.add(new float[]{0, POSNEG_MAX_COORD});
        posPos.add(new float[]{POSNEG_MAX_COORD, POSNEG_MAX_COORD});
        
        posPos.add(new float[]{-POSNEG_MAX_COORD, 0});
        posPos.add(new float[]{POSNEG_MAX_COORD, 0});
        
        posPos.add(new float[]{-POSNEG_MAX_COORD, -POSNEG_MAX_COORD});
        posPos.add(new float[]{0, -POSNEG_MAX_COORD});
        posPos.add(new float[]{POSNEG_MAX_COORD, -POSNEG_MAX_COORD});
        
        //Define negative disk positions
        negPos.add(new float[]{-POSNEG_BETWEEN_COORD, POSNEG_MAX_COORD});
        negPos.add(new float[]{POSNEG_BETWEEN_COORD, POSNEG_MAX_COORD});
        
        negPos.add(new float[]{-POSNEG_BETWEEN_COORD, -POSNEG_MAX_COORD});
        negPos.add(new float[]{POSNEG_BETWEEN_COORD, -POSNEG_MAX_COORD});
        
        negPos.add(new float[]{-POSNEG_MAX_COORD, POSNEG_BETWEEN_COORD});
        negPos.add(new float[]{-POSNEG_MAX_COORD, -POSNEG_BETWEEN_COORD});
        
        negPos.add(new float[]{POSNEG_MAX_COORD, POSNEG_BETWEEN_COORD});
        negPos.add(new float[]{POSNEG_MAX_COORD, -POSNEG_BETWEEN_COORD});
        
    }
    private AnalogListener analogListener = new AnalogListener() {
        public void onAnalog(String name, float value, float tpf) {
            if(name.equals("up")){
                playerVariable.accelerateUp();
            }
            if(name.equals("down")) {
                playerVariable.accelerateDown();
            }
            if(name.equals("left")){
                playerVariable.accelerateLeft();
            }
            if(name.equals("right")){
                playerVariable.accelerateRight();
            }
        }
    };
}