package mygame;
        
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import mygame.GameMessage.*;

class GameServer extends BaseAppState {

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
    
    static final float FRICTION = 2f;
    
    static final float MIN_START_SPEED = -5f;
    static final float MAX_START_SPEED = 5f;
    
    public int NUMBER_OF_PLAYERS = 0;
    
    ArrayList<Disk> diskList;
    ArrayList<float[]> posPos = new ArrayList<float[]>();
    ArrayList<float[]> negPos = new ArrayList<float[]>();
    ArrayList<float[]> playPos = new ArrayList<float[]>();
    
    ArrayList<PlayerDisk> players;
    ArrayList<NegativeDisk> nDiskList;
    ArrayList<PositiveDisk> pDiskList;
    
    BitmapText HUDtext;
    
    private ConcurrentLinkedQueue sendPacketQueue;
    
    private static float RESYNC = 0.5f;
    private float updateCount = 0f;
    
    public void setConcurrentQ(ConcurrentLinkedQueue q) {
        this.sendPacketQueue = q;
    }
    
    @Override
    protected void initialize(Application app) {
        sapp = (SimpleApplication) app;
        System.out.println("GameServer: initialize");
        initPositions();
        initGameState();
    }

    @Override
    protected void cleanup(Application app) {
        System.out.println("GameServer: cleanup");
    }

    public void initGameState() {
        diskList = new ArrayList<Disk>();
        nDiskList = new ArrayList<NegativeDisk>();
        pDiskList = new ArrayList<PositiveDisk>();
        players = new ArrayList<PlayerDisk>();
        
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
        
        //
        int diskID = 10;

        //Create negative disks
        for(int i=0; i<negPos.size(); i++){
            //Randomize speed in vector here...
            float randomX = MIN_START_SPEED + r.nextFloat() * (MAX_START_SPEED - MIN_START_SPEED);
            float randomY = MIN_START_SPEED + r.nextFloat() * (MAX_START_SPEED - MIN_START_SPEED);
            Vector3f nVector = new Vector3f(randomX, randomY, 0f);
            NegativeDisk nDisk = new NegativeDisk(diskID, nVector, negPos.get(i)[0], negPos.get(i)[1], NEGDISK_R, negDiskMat, sapp);
            diskList.add(nDisk);
            nDiskList.add(nDisk);
            diskID += 1;
        }
        
        //Create positive disks
        for(int i=0; i<posPos.size(); i++){
            //Randomize speed in vector here...
            float randomX = MIN_START_SPEED + r.nextFloat() * (MAX_START_SPEED - MIN_START_SPEED);
            float randomY = MIN_START_SPEED + r.nextFloat() * (MAX_START_SPEED - MIN_START_SPEED);
            Vector3f pVector = new Vector3f(randomX, randomY, 0f);
            PositiveDisk pDisk = new PositiveDisk(diskID, pVector, posPos.get(i)[0], posPos.get(i)[1], POSDISK_R, posDiskMat, sapp);
            diskList.add(pDisk);
            pDiskList.add(pDisk);
            diskID += 1;
        }
    }
    
    public void addPlayer(HostedConnection conn){
        //Create player disk material
        Material playDiskMat = new Material(sapp.getAssetManager(),
          "Common/MatDefs/Misc/Unshaded.j3md");
        playDiskMat.setColor("Color", ColorRGBA.Blue);
        
        //Random used to generate random start speed
        Random r = new Random();
        
        //Create random index to get a starting position from the playPost list
        int playCoord = r.nextInt(playPos.size());
        //Create player and add to diskList
        //Select starting position from the playPos list with random playCoord index
        Vector3f playVector = new Vector3f(0f, 0f, 0f);
        float[] pos = playPos.remove(playCoord);
        PlayerDisk playDisk = new PlayerDisk(NUMBER_OF_PLAYERS, playVector, pos[0], pos[1], PLAYER_R, playDiskMat, sapp, Integer.toString(NUMBER_OF_PLAYERS));
        NUMBER_OF_PLAYERS += 1;

        diskList.add(playDisk);

        //Variable used to move player and such in this class
        players.add(playDisk);
        
        for (Disk d : nDiskList) {
            int id = d.id;
            float posX = d.getNode().getLocalTranslation().getX();
            float posY = d.getNode().getLocalTranslation().getY();
            float speedX = d.getSpeed().getX();
            float speedY = d.getSpeed().getY();
            SendInitNegativeDisk msg = new SendInitNegativeDisk(id, posX, posY, speedX, speedY);
            InternalMessage m = new InternalMessage(Filters.in(conn), msg);
            sendPacketQueue.add(m);
        }
        
        for (Disk d : pDiskList) {
            int id = d.id;
            float posX = d.getNode().getLocalTranslation().getX();
            float posY = d.getNode().getLocalTranslation().getY();
            float speedX = d.getSpeed().getX();
            float speedY = d.getSpeed().getY();
            SendInitPositiveDisk msg = new SendInitPositiveDisk(id, posX, posY, speedX, speedY);
            InternalMessage m = new InternalMessage(Filters.in(conn), msg);
            sendPacketQueue.add(m);
        }
        
        for (Disk d : players) {
            int id = d.id;
            float posX = d.getNode().getLocalTranslation().getX();
            float posY = d.getNode().getLocalTranslation().getY();
            SendInitPlayerDisk msg = new SendInitPlayerDisk(id, posX, posY);
            InternalMessage m = new InternalMessage(Filters.in(conn), msg);
            sendPacketQueue.add(m);
        }
        
        //Send newly created playerDisk to all except conn
        int id = playDisk.id;
        float posX = playDisk.getNode().getLocalTranslation().getX();
        float posY = playDisk.getNode().getLocalTranslation().getY();
        SendInitPlayerDisk msg = new SendInitPlayerDisk(id, posX, posY);
        InternalMessage m = new InternalMessage(Filters.notEqualTo(conn), msg);
        sendPacketQueue.add(m);
    }
    
    public void restartGame(HostedConnection conn) {
        //Create player disk material
        Material playDiskMat = new Material(sapp.getAssetManager(),
          "Common/MatDefs/Misc/Unshaded.j3md");
        playDiskMat.setColor("Color", ColorRGBA.Blue);
        
        //Random used to generate random start speed
        Random r = new Random();
        
        //Create random index to get a starting position from the playPost list
        int playCoord = r.nextInt(playPos.size());
        //Create player and add to diskList
        //Select starting position from the playPos list with random playCoord index
        Vector3f playVector = new Vector3f(0f, 0f, 0f);
        float[] pos = playPos.remove(playCoord);
        PlayerDisk playDisk = new PlayerDisk(NUMBER_OF_PLAYERS, playVector, pos[0], pos[1], PLAYER_R, playDiskMat, sapp, Integer.toString(NUMBER_OF_PLAYERS));
        NUMBER_OF_PLAYERS += 1;

        diskList.add(playDisk);

        //Variable used to move player and such in this class
        players.add(playDisk);
        
        for (Disk d : nDiskList) {
            int id = d.id;
            float posX = d.getNode().getLocalTranslation().getX();
            float posY = d.getNode().getLocalTranslation().getY();
            float speedX = d.getSpeed().getX();
            float speedY = d.getSpeed().getY();
            SendInitNegativeDisk msg = new SendInitNegativeDisk(id, posX, posY, speedX, speedY);
            InternalMessage m = new InternalMessage(Filters.in(conn), msg);
            sendPacketQueue.add(m);
        }
        
        for (Disk d : pDiskList) {
            int id = d.id;
            float posX = d.getNode().getLocalTranslation().getX();
            float posY = d.getNode().getLocalTranslation().getY();
            float speedX = d.getSpeed().getX();
            float speedY = d.getSpeed().getY();
            SendInitPositiveDisk msg = new SendInitPositiveDisk(id, posX, posY, speedX, speedY);
            InternalMessage m = new InternalMessage(Filters.in(conn), msg);
            sendPacketQueue.add(m);
        }
        
        //Send newly created playerDisk to all except conn
        int id = playDisk.id;
        float posX = playDisk.getNode().getLocalTranslation().getX();
        float posY = playDisk.getNode().getLocalTranslation().getY();
        SendInitPlayerDisk msg = new SendInitPlayerDisk(id, posX, posY);
        InternalMessage m = new InternalMessage(null, msg);
        sendPacketQueue.add(m);
    }
    
    @Override
    protected void onEnable() {
        System.out.println("GameServer: onEnable");
        if (needCleaning) {
            System.out.println("(Cleaning up)");
            //sapp.getRootNode().detachAllChildren();
            needCleaning = false;
        }
        System.out.println("(Creating the scenegraph etc from scratch)");
        
        //Create frame and add to rootNode
        Frame frame = new Frame(sapp);
        
    }

    @Override
    protected void onDisable() {
        System.out.println("Game: onDisable");
        needCleaning = true;
        sapp.getRootNode().detachAllChildren();
        NUMBER_OF_PLAYERS = 0;
        diskList.clear();
        pDiskList.clear();
        nDiskList.clear();
        players.clear();
        
    }

    public void sendNewDiskVelocityAndSpeed(Disk disk){
        int diskId = disk.id;
        Vector3f speed = disk.getSpeed();
        float posX = disk.getNode().getLocalTranslation().getX();
        float posY = disk.getNode().getLocalTranslation().getY();
        
        //Send message
        UpdateDiskPosAndVelMessage pandv = new UpdateDiskPosAndVelMessage(diskId, speed, posX, posY);
        sendPacketQueue.add(new InternalMessage(null, pandv));
    }
    
    public void sendCollisionMessage(Disk disk1, Disk disk2) {
        int diskId1 = disk1.id;
        Vector3f speed1 = disk1.getSpeed();
        float posX1 = disk1.getNode().getLocalTranslation().getX();
        float posY1 = disk1.getNode().getLocalTranslation().getY();
        int diskId2 = disk2.id;
        Vector3f speed2 = disk2.getSpeed();
        float posX2 = disk2.getNode().getLocalTranslation().getX();
        float posY2 = disk2.getNode().getLocalTranslation().getY();
        
        //Send message
        CollisionMessage colM = new CollisionMessage(diskId1, speed1, posX1, posY1, diskId2, speed2, posX2, posY2);
        sendPacketQueue.add(new InternalMessage(null, colM));
    }
    @Override
    public void update(float tpf) {
        
        updateCount += tpf;
        
        for(Disk disk : diskList){           
            //Check for frame collision
            if(disk.frameCollision(disk.radius)) {
                sendNewDiskVelocityAndSpeed(disk); 
            }
            //send the info for the frame collision
            
            //Check for disk collision
            for(Disk disk2 : diskList){
                if(!disk.equals(disk2)) {
                    if(disk.checkCollisionWith(disk2)) {
                        float deltaT = disk.calcTSinceCollision(disk2, tpf);

                        disk.moveDisks(disk2, deltaT);

                        //Calculate collision and then move disks "deltaT" time forward
                        disk.cylinderCollision(disk2, deltaT*-1);

                        sendCollisionMessage(disk, disk2);
                    }
                }

                else{
                    disk.diskNode.move(disk.getSpeed().getX()*tpf, disk.getSpeed().getY()*tpf, 0);
                    disk.applyFrictionX(tpf);
                    disk.applyFrictionY(tpf);
                }
            } 
        }

        if(updateCount > RESYNC) {
            for(Disk disk : diskList) {
                sendNewDiskVelocityAndSpeed(disk);                
            }
            updateCount = 0;
        }
    }

    public void initPositions() {
        
        posPos.clear();
        negPos.clear();
        playPos.clear();
        
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
}