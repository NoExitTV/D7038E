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
    
    static final int NUMBER_OF_PLAYERS = 9;
    
    ArrayList<Disk> diskList;
    ArrayList<float[]> posPos = new ArrayList<float[]>();
    ArrayList<float[]> negPos = new ArrayList<float[]>();
    ArrayList<float[]> playPos = new ArrayList<float[]>();
    
    ArrayList<PlayerDisk> players = new ArrayList<PlayerDisk>();
    private ArrayList<keyList> playerKeys = new ArrayList<keyList>();
    
    BitmapText HUDtext;
    
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
        
        
        /**
         * Try to create NUMBER_OF_PLAYERS players
         */
        for(int i=0; i<NUMBER_OF_PLAYERS; i++) {
            
            //Create random index to get a starting position from the playPost list
            int playCoord = r.nextInt(playPos.size());
            //Create player and add to diskList
            //Select starting position from the playPos list with random playCoord index
            Vector3f playVector = new Vector3f(0f, 0f, 0f);
            float[] pos = playPos.remove(playCoord);
            PlayerDisk playDisk = new PlayerDisk(playVector, pos[0], pos[1], PLAYER_R, playDiskMat, sapp, Integer.toString(i));
            diskList.add(playDisk);

            //Variable used to move player and such in this class
            players.add(playDisk);
            
            //Create keyboard mapping for player
            sapp.getInputManager().addMapping("left" + i,      new KeyTrigger(playerKeys.get(i).getLeft()));
            sapp.getInputManager().addMapping("down" + i,      new KeyTrigger(playerKeys.get(i).getDown()));
            sapp.getInputManager().addMapping("right" + i,     new KeyTrigger(playerKeys.get(i).getRight()));
            sapp.getInputManager().addMapping("up" + i,        new KeyTrigger(playerKeys.get(i).getUp()));
            sapp.getInputManager().addListener(analogListener, "left" + i, "down" + i, "right" + i, "up" + i);
        }
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
            String hud = "";
            for(int i=0; i<NUMBER_OF_PLAYERS; i++){
                hud += "Player"+i+": "+players.get(i).returnPoints()+"p\n";
            }
            HUDtext.setText(hud);
        }
    }
    /** Custom Keybinding: Map named actions to inputs. */
    private void initKeys() {
        //add left, down, right, up
        playerKeys.add(new keyList(KeyInput.KEY_A, KeyInput.KEY_S, KeyInput.KEY_D, KeyInput.KEY_W));
        playerKeys.add(new keyList(KeyInput.KEY_F, KeyInput.KEY_G, KeyInput.KEY_H, KeyInput.KEY_T));
        playerKeys.add(new keyList(KeyInput.KEY_J, KeyInput.KEY_K, KeyInput.KEY_L, KeyInput.KEY_I));
        
        //add left, down, right, up
        playerKeys.add(new keyList(KeyInput.KEY_A, KeyInput.KEY_S, KeyInput.KEY_D, KeyInput.KEY_W));
        playerKeys.add(new keyList(KeyInput.KEY_F, KeyInput.KEY_G, KeyInput.KEY_H, KeyInput.KEY_T));
        playerKeys.add(new keyList(KeyInput.KEY_J, KeyInput.KEY_K, KeyInput.KEY_L, KeyInput.KEY_I));
        
        //add left, down, right, up
        playerKeys.add(new keyList(KeyInput.KEY_A, KeyInput.KEY_S, KeyInput.KEY_D, KeyInput.KEY_W));
        playerKeys.add(new keyList(KeyInput.KEY_F, KeyInput.KEY_G, KeyInput.KEY_H, KeyInput.KEY_T));
        playerKeys.add(new keyList(KeyInput.KEY_J, KeyInput.KEY_K, KeyInput.KEY_L, KeyInput.KEY_I));
        
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
            
            //Move every players
            for(int i=0; i<NUMBER_OF_PLAYERS; i++){
                if(name.equals("up"+i)) {
                    players.get(i).accelerateUp();
                }
                if(name.equals("down"+i)){
                    players.get(i).accelerateDown();
                }
                if(name.equals("left"+i)){
                    players.get(i).accelerateLeft();
                }
                if(name.equals("right"+i)){
                    players.get(i).accelerateRight();
                }
            }                     
        }
    };
}