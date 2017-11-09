package mygame;
        
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import java.util.ArrayList;

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
    
    ArrayList<Disk> diskList;

    @Override
    protected void initialize(Application app) {
        sapp = (SimpleApplication) app;
        System.out.println("Game: initialize");
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
        
        //Create frame and add to rootNode
        Frame frame = new Frame(sapp);
        
        //Create negative disks
        Material negDiskMat = new Material(sapp.getAssetManager(),
          "Common/MatDefs/Misc/Unshaded.j3md");
        negDiskMat.setColor("Color", ColorRGBA.Red);
        
        Vector3f nVector = new Vector3f(50f, 0f, 0f);
        Vector3f nVector2 = new Vector3f(-50f, 0f, 0f);
        Vector3f nVector3 = new Vector3f(0f, 50f, 0f);
        NegativeDisk nDisk = new NegativeDisk(nVector, -100f, 0, NEGDISK_R, negDiskMat, sapp);
        NegativeDisk nDisk2 = new NegativeDisk(nVector2, 100f, 0, NEGDISK_R, negDiskMat, sapp);
        NegativeDisk nDisk3 = new NegativeDisk(nVector3, 0, 0, NEGDISK_R, negDiskMat,sapp);
        diskList.add(nDisk);
        diskList.add(nDisk2);
        diskList.add(nDisk3);
        
    }

    @Override
    protected void onDisable() {
        System.out.println("Game: onDisable");
        needCleaning = true;
    }

    @Override
    public void update(float tpf) {
        float mConst = tpf;
        //Move disks
        for(Disk disk : diskList){
            disk.diskNode.move(disk.getSpeed().getX()*tpf, disk.getSpeed().getY()*tpf, 0);
            //Set new position
            disk.posX = disk.diskNode.getLocalTranslation().getX();
            disk.posY = disk.diskNode.getLocalTranslation().getY();
            
            //Check for frame collision
            disk.frameCollision(NEGDISK_R);
            
            //Check for disk collision
            for(Disk disk2 : diskList){
                if(!disk.equals(disk2)) {
                   disk.cylinderCollision(disk2); 
                }
            }
        }
        /*
        for(int i=0; i<diskList.size(); i++) {
            Disk disk = diskList.get(i);
            
            disk.diskNode.move(disk.getSpeed().getX()*tpf, disk.getSpeed().getY()*tpf, 0);
            //Get new position
            Vector3f newPos = disk.diskNode.getLocalTranslation();
            //Set new position
            disk.posX = disk.diskNode.getLocalTranslation().getX();
            disk.posY = disk.diskNode.getLocalTranslation().getY();
            //disk.diskNode.setLocalTranslation(newPos.getX(), newPos.getY(), newPos.getZ());
            //Check for collision with frame
            disk.frameCollision(NEGDISK_R);
        }
        */
    }

}