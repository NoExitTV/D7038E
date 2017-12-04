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
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import mygame.GameMessage.*;

class GameClient extends BaseAppState {

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
    
    static final int NUMBER_OF_PLAYERS = 1;
    
    ArrayList<Disk> diskList = new ArrayList<Disk>();
    ArrayList<PlayerDisk> playerDiskList = new ArrayList<PlayerDisk>();
    public int yourID = -1;
    
    PlayerDisk myPlayer;
    private ArrayList<keyList> playerKeys = new ArrayList<keyList>();
    
    BitmapText HUDtext;
    
    private ConcurrentLinkedQueue sendPacketQueue;
    
    
    //Helper class for positions
    class keyList {
        int left;
        int down;
        int right;
        int up;
    
        public keyList(int left, int down, int right, int up) {
            this.left = left;
            this.down = down;
            this.right = right;
            this.up = up;
        }
        
        public int getLeft() {
            return left;
        }
        
        public int getDown() {
            return down;
        }
        
        public int getRight() {
            return right;
        }
        
        public int getUp() {
            return up;
        }
    }
 
    public void setConcurrentQ(ConcurrentLinkedQueue q) {
        this.sendPacketQueue = q;
    }
    public void setID(int id) {
        yourID = id;
    }
    
    @Override
    protected void initialize(Application app) {
        sapp = (SimpleApplication) app;
        System.out.println("Game: initialize");
        initKeys();
        
    }

    @Override
    protected void cleanup(Application app) {
        System.out.println("Game: cleanup");
    }

    @Override
    protected void onEnable() {
        System.out.println("Game: onEnable");
        System.out.println("(Creating the scenegraph etc from scratch)");
        
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
        
        //Create keyboard mapping for player
        System.out.println("BIND KEYS");
        sapp.getInputManager().addMapping("left",      new KeyTrigger(playerKeys.get(0).getLeft()));
        sapp.getInputManager().addMapping("down",      new KeyTrigger(playerKeys.get(0).getDown()));
        sapp.getInputManager().addMapping("right",     new KeyTrigger(playerKeys.get(0).getRight()));
        sapp.getInputManager().addMapping("up",        new KeyTrigger(playerKeys.get(0).getUp()));
        sapp.getInputManager().addListener(analogListener, "left", "down", "right", "up");
    }

    public void createPositiveDisk(int diskID, float posX, float posY, float speedX, float speedY){
        //Create positive disk material
        Material posDiskMat = new Material(sapp.getAssetManager(),
          "Common/MatDefs/Misc/Unshaded.j3md");
        posDiskMat.setColor("Color", ColorRGBA.Green);
        
        Vector3f tempVector = new Vector3f(speedX, speedY, 0);
        PositiveDisk pDisk = new PositiveDisk(diskID, tempVector, posX, posY, POSDISK_R, posDiskMat, sapp);
        diskList.add(pDisk);
        
    }
    public void createNegativeDisk(int diskID, float posX, float posY, float speedX, float speedY){
        //Create negative disks material
        Material negDiskMat = new Material(sapp.getAssetManager(),
          "Common/MatDefs/Misc/Unshaded.j3md");
        negDiskMat.setColor("Color", ColorRGBA.Red);
        
        Vector3f tempVector = new Vector3f(speedX, speedY, 0);
        NegativeDisk nDisk = new NegativeDisk(diskID, tempVector, posX, posY, NEGDISK_R, negDiskMat, sapp);
        diskList.add(nDisk);
    }

    public void createPlayerDisk(int diskID, float posX, float posY) {
 
        System.out.println("CREATED PLAYER");
        
        //Create player disk material
        Material playDiskMat = new Material(sapp.getAssetManager(),
          "Common/MatDefs/Misc/Unshaded.j3md");
        playDiskMat.setColor("Color", ColorRGBA.Blue);
        
        Vector3f tempVector = new Vector3f(0, 0, 0);
        PlayerDisk pDisk = new PlayerDisk(diskID, tempVector, posX, posY, PLAYER_R, playDiskMat, sapp, Integer.toString(diskID));
    
        diskList.add(pDisk);
        playerDiskList.add(pDisk);
        
        /**
         * Check that this is is received correct...
         */
        if(diskID == yourID) {
           //Variable used to move player and such in this class
            myPlayer = pDisk; 
        }
    }
    
    @Override
    protected void onDisable() {
        System.out.println("Game: onDisable");
        needCleaning = true;
        sapp.getRootNode().detachAllChildren();
        diskList.clear();
        playerDiskList.clear();
    }

    @Override
    public void update(float tpf) {
        for(Disk disk : diskList){  
            if(disk.id != yourID) {
               /**
             * Flytta disk mot börvärde i position
             */
            float setPointConstant = 1f;
            float currPosX = disk.getNode().getLocalTranslation().getX();
            float currPosY = disk.getNode().getLocalTranslation().getY();
            float newPosX = currPosX + setPointConstant*(disk.setPointX-currPosX);
            float newPosY = currPosY + setPointConstant*(disk.setPointY-currPosY);
            
            /**
             * Flytta hastighet mot börvärde
             */
            float currSpeedX = disk.getSpeed().getX();
            float currSpeedY = disk.getSpeed().getY();
            float newSpeedX = currSpeedX + setPointConstant*(disk.setPointSpeedX-currSpeedX);
            float newSpeedY = currSpeedY + setPointConstant*(disk.setPointSpeedY-currSpeedY);
            
             //disk.setSpeed(newSpeedX, newSpeedY);
            /**
             * Flytta disk mot nya uträknade är-värdet
             */
            disk.getNode().setLocalTranslation(newPosX+newSpeedX*tpf,
                    newPosY+newSpeedY*tpf, 0);
            
            disk.applyFrictionX();
            disk.applyFrictionY();
            
            /**
             * Dead reconing on newPos and newSpeed
             */
            disk.setPointX += disk.setPointSpeedX*tpf;
            disk.setPointY += disk.setPointSpeedY*tpf;
            }
            else {
                
                disk.diskNode.move(disk.getSpeed().getX()*tpf, disk.getSpeed().getY()*tpf, 0);
                disk.applyFrictionX();
                disk.applyFrictionY();
                
            }
            
            //Update hud text         
            String hud = "";
            for(int i=0; i<playerDiskList.size(); i++){
                if(playerDiskList.get(i).id == yourID) {
                    //hud += "You: "+playerDiskList.get(i).returnPoints()+"p\n";
                    hud += "You: "+(tpf)+"p\n";
                }
                else {
                    hud += "Player"+i+": "+playerDiskList.get(i).returnPoints()+"p\n";
                }
            }
            HUDtext.setText(hud);
        }
    }
    /** Custom Keybinding: Map named actions to inputs. */
    private void initKeys() {        
        //add left, down, right, up
        playerKeys.add(new keyList(KeyInput.KEY_J, KeyInput.KEY_K, KeyInput.KEY_L, KeyInput.KEY_I));
    }

    private AnalogListener analogListener = new AnalogListener() {
        int i=0;
        public void onAnalog(String name, float value, float tpf) {
            if(name.equals("up")) {
                myPlayer.accelerateUp();
            }
            if(name.equals("down")){
                myPlayer.accelerateDown();
            }
            if(name.equals("left")){
                myPlayer.accelerateLeft();
            }
            if(name.equals("right")){
                myPlayer.accelerateRight();
            }
            if(i>50){
               /**
               * Create message and send to server
               */
                int playerID = myPlayer.id;
                Vector3f speed = myPlayer.getSpeed();
                float posX = myPlayer.getNode().getLocalTranslation().getX();
                float posY = myPlayer.getNode().getLocalTranslation().getY();
                ClientVelocityUpdateMessage msg = new ClientVelocityUpdateMessage(speed, playerID, posX, posY);
                sendPacketQueue.add(new InternalMessage(null, msg));   
                
                i=0;
            }
            i += 1;
        }                     

    };
}