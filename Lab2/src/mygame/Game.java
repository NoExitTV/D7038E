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
    ArrayList<float[]> playPos = new ArrayList<float[]>();
    PlayerDisk player1;
    
    BitmapText HUDtext;
    
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
        
        //Create hud text that display player points
        BitmapFont myFont
                = sapp.getAssetManager()
                        .loadFont("Interface/Fonts/Console.fnt");
        HUDtext = new BitmapText(myFont, false);
        HUDtext.setSize(myFont.getCharSet().getRenderedSize() * 2);
        HUDtext.setColor(ColorRGBA.White);
        HUDtext.setLocalTranslation(5, FREE_AREA_WIDTH-FRAME_THICKNESS, 0);
        //Attach HUDtext to GuiNode to display text
        sapp.getGuiNode().attachChild(HUDtext);
        
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
        
        //Create player disk material
        Material playDiskMat = new Material(sapp.getAssetManager(),
          "Common/MatDefs/Misc/Unshaded.j3md");
        playDiskMat.setColor("Color", ColorRGBA.Blue);
        
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
        
        //Create random index to get a starting position from the playPost list
        int playCoord = r.nextInt(playPos.size());
        
        //Create player and add to diskList
        //Select starting position from the playPos list with random playCoord index
        Vector3f playVector = new Vector3f(0f, 0f, 0f);
        PlayerDisk playDisk1 = new PlayerDisk(playVector, playPos.get(playCoord)[0], playPos.get(playCoord)[1], PLAYER_R, playDiskMat, sapp, "1");
        diskList.add(playDisk1);
        
        //Variable used to move player and such in this class
        this.player1 = playDisk1;
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
            //Update hud text
            HUDtext.setText("Player1: "+player1.returnPoints()+"p");
        }
    }
    /** Custom Keybinding: Map named actions to inputs. */
    private void initKeys() {
        // You can map one or several inputs to one named action
        sapp.getInputManager().addMapping("left",    new KeyTrigger(KeyInput.KEY_G));
        sapp.getInputManager().addMapping("right",   new KeyTrigger(KeyInput.KEY_J));
        sapp.getInputManager().addMapping("up",      new KeyTrigger(KeyInput.KEY_Y));
        sapp.getInputManager().addMapping("down",    new KeyTrigger(KeyInput.KEY_H));
        sapp.getInputManager().addListener(analogListener,"left", "right", "up", "down");
    }

    private void initPositions() {
        
        //Define positive disk starting positions
        posPos.add(new float[]{-POSNEG_MAX_COORD, POSNEG_MAX_COORD});
        posPos.add(new float[]{0, POSNEG_MAX_COORD});
        posPos.add(new float[]{POSNEG_MAX_COORD, POSNEG_MAX_COORD});
        
        posPos.add(new float[]{-POSNEG_MAX_COORD, 0});
        posPos.add(new float[]{POSNEG_MAX_COORD, 0});
        
        posPos.add(new float[]{-POSNEG_MAX_COORD, -POSNEG_MAX_COORD});
        posPos.add(new float[]{0, -POSNEG_MAX_COORD});
        posPos.add(new float[]{POSNEG_MAX_COORD, -POSNEG_MAX_COORD});
        
        //Define negative disk starting positions
        negPos.add(new float[]{-POSNEG_BETWEEN_COORD, POSNEG_MAX_COORD});
        negPos.add(new float[]{POSNEG_BETWEEN_COORD, POSNEG_MAX_COORD});
        
        negPos.add(new float[]{-POSNEG_BETWEEN_COORD, -POSNEG_MAX_COORD});
        negPos.add(new float[]{POSNEG_BETWEEN_COORD, -POSNEG_MAX_COORD});
        
        negPos.add(new float[]{-POSNEG_MAX_COORD, POSNEG_BETWEEN_COORD});
        negPos.add(new float[]{-POSNEG_MAX_COORD, -POSNEG_BETWEEN_COORD});
        
        negPos.add(new float[]{POSNEG_MAX_COORD, POSNEG_BETWEEN_COORD});
        negPos.add(new float[]{POSNEG_MAX_COORD, -POSNEG_BETWEEN_COORD});
        
        //Define player disk starting positions
        playPos.add(new float[]{PLAYER_COORD, PLAYER_COORD});
        playPos.add(new float[]{0, PLAYER_COORD});
        playPos.add(new float[]{-PLAYER_COORD, PLAYER_COORD});
        
        playPos.add(new float[]{PLAYER_COORD, 0});
        playPos.add(new float[]{0, 0});
        playPos.add(new float[]{-PLAYER_COORD, 0});
        
        playPos.add(new float[]{PLAYER_COORD, -PLAYER_COORD});
        playPos.add(new float[]{0, -PLAYER_COORD});
        playPos.add(new float[]{-PLAYER_COORD, -PLAYER_COORD});
        
    }
    private AnalogListener analogListener = new AnalogListener() {
        public void onAnalog(String name, float value, float tpf) {
            if(name.equals("up")){
                player1.accelerateUp();
            }
            if(name.equals("down")) {
                player1.accelerateDown();
            }
            if(name.equals("left")){
                player1.accelerateLeft();
            }
            if(name.equals("right")){
                player1.accelerateRight();
            }
        }
    };
}