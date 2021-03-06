/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.ChaseCamera;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import mygame.GameMessage.*;

/**
 *
 * @author Fredrik Pettersson & Carl Borngrund
 */
public class GameServer extends BaseAppState {
    //Constants
    static final float WALKSPEED = 0.20f;
    static final float JUMPSPEED = 20f;
    static final float FALLSPEED = 30f;
    static final float GRAVITY = 30f;
    static final float RESYNC = 0.10f;
    static final float TREASURE_TIME = 5f;
    static final float SYNC_POINTS_TIME = 10f;
    static final int MAX_POINTS = 3;
    static final float RESTART_TIME = 20f;
    
    // 
    boolean gameRunning = false;
    
    // Variables we need
    private SimpleApplication sapp;
    
    private BulletAppState bulletAppState;
    private RigidBodyControl landscape;

    // ChaseCamera variable
    ChaseCamera chaseCam;
    
    // Player variables
    private ArrayList<Player> players = new ArrayList();
    
    // Network queue
    private ConcurrentLinkedQueue sendPacketQueue;
    
    //Time varible
    private float resyncTime = 0f;
    private float treasureTime = 0f;
    private float syncPointsTime = 0f;
    private float restartTime = 0f;
    
    //Treasure varibles
    TreasureClass currentTreasure;
    ArrayList<float[]> treasurePositions = new ArrayList<float[]>();
    boolean hasTreasure;

    /**
     * Function to set concurrent linked queue
     * @param q 
     */
    public void setConcurrentQ(ConcurrentLinkedQueue q) {
        this.sendPacketQueue = q;
    }
    
    @Override
    protected void initialize(Application app) {
        sapp = (SimpleApplication) app;
        Util.print("STARTED GAME (Server)");
        
    /** Set up Physics */
    bulletAppState = new BulletAppState();
    sapp.getStateManager().attach(bulletAppState);
    
    setUpLight();    
    
    /* Create landscape */
    Landscape landScape = new Landscape(sapp, bulletAppState);
    
    
    //Setup all the treasure positions
    setupTreasurePositions();
    
    //Create a treasure randomly
    spawnTreasure();
        
    // Set game as running
    gameRunning = true;
    
    }

    /**
     * Add player to environment and update everyone
     * @param conn 
     * @param newPlayerId 
     */
    public void addPlayer(HostedConnection conn, int newPlayerId){
        Player newPlayer = new Player(sapp, newPlayerId, bulletAppState);
        players.add(newPlayer);
        
        for(Player p : players) {
            float posX = p.player.getPhysicsLocation().getX();
            float posY = p.player.getPhysicsLocation().getY();
            float posZ = p.player.getPhysicsLocation().getZ();
            int playerId = p.playerId;
            int points = p.points;
            // Send all player to new player
            CreatePlayerMsg createPlayer = new CreatePlayerMsg(playerId, posX, posY, posZ, points);
            InternalMessage m = new InternalMessage(Filters.in(conn), createPlayer);
            System.out.println("sent create player message");
            sendPacketQueue.add(m);
        }
        
        // Send new player to everyone except new player
        float posX = newPlayer.player.getPhysicsLocation().getX();
        float posY = newPlayer.player.getPhysicsLocation().getY();
        float posZ = newPlayer.player.getPhysicsLocation().getZ();
        CreatePlayerMsg createPlayer = new CreatePlayerMsg(newPlayerId, posX, posY, posZ, 0);
        InternalMessage m = new InternalMessage(Filters.notEqualTo(conn), createPlayer);
        sendPacketQueue.add(m);
    }
    
    public void removePlayer(HostedConnection conn, int id) {
        Player tempPlayer = null;
        for (Player p : players) {
            if (p.playerId == id) {
                tempPlayer = p;
                break;
            }
        }
        try {
            players.remove(tempPlayer);
            tempPlayer.getNode().detachAllChildren();
            bulletAppState.getPhysicsSpace().remove(tempPlayer.gc);
            //Send message to clients to remove same id user
            ClientLeaveMsg m = new ClientLeaveMsg(id);
            InternalMessage im = new InternalMessage(Filters.notEqualTo(conn), m);
            sendPacketQueue.add(im);
            
      
        } catch (NullPointerException e) {
            System.out.println("Could not remove user. " + e);
        } 
        
        
    }
    
    public void updateWalkDirection(int playerId, Vector3f newWalkDirection) {
        for(Player p : players) {
            if(p.playerId == playerId) {
                p.setWalkDirection(newWalkDirection);
                break;
            }
        }
    }
    
    public void characterJump(int playerId) {
        for(Player p : players) {
            if(p.playerId == playerId) {
                p.getCharacterControl().jump();
                break;
            }
        }
    }
    
    public void resyncPlayer(int playerId, Vector3f playerPos) {
        for(Player p : players) {
            if(p.playerId == playerId) {
                p.getCharacterControl().setPhysicsLocation(playerPos);
                break;
            }
        }
    }
    
    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
        Util.print("GAME ENABLED (SERVER)");
    }

    @Override
    protected void onDisable() {
        Util.print("GAME DISABLED (SERVER)");
    }
    
    private void setUpLight() {
        // We add light so we see the scene
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(1.3f));
        sapp.getRootNode().addLight(al);

        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
        sapp.getRootNode().addLight(dl);
    }
    
    private void spawnTreasure() {
        float[] positions = treasurePositions.get((new Random()).nextInt(treasurePositions.size()));
        int points = 1;
        
        currentTreasure = new TreasureClass(sapp, bulletAppState, positions[0], positions[1], positions[2], points);
        hasTreasure = true;
        
        // Send info about treasure to all players
        sendTreasureMsg();
    }
    
    void sendTreasureMsg() {
        Vector3f pos = currentTreasure.getPosition();
        float[] treasurePos = new float[]{pos.getX(), pos.getY(), pos.getZ()};
        
        // Send message abot treasure to clients
        SpawnTreasureMsg m = new SpawnTreasureMsg(treasurePos, currentTreasure.points);
        InternalMessage im = new InternalMessage(null, m);
        sendPacketQueue.add(im);
    }
    
    /**
     * This function is called when a player connects
     * to send current treasure to connecting player
     * @param conn 
     */
    void sendTreasureMsg(HostedConnection conn) {
        
        // Only send currentTreasure if the game is running i.e. not in a restart period
        if(gameRunning) {
            Vector3f pos = currentTreasure.getPosition();
            float[] treasurePos = new float[]{pos.getX(), pos.getY(), pos.getZ()};

            // Send message abot treasure to clients
            SpawnTreasureMsg m = new SpawnTreasureMsg(treasurePos, currentTreasure.points);
            InternalMessage im = new InternalMessage(Filters.in(conn), m);
            sendPacketQueue.add(im);
        }
        
    }
    
    void captureTreasure(int playerId, HostedConnection conn) {
        sapp.getRootNode().detachChild(currentTreasure.boxNode);
        bulletAppState.getPhysicsSpace().remove(currentTreasure.gc);
        
        // Give points to player
        for(Player p : players) {
            if(p.playerId == playerId) {
                
                // Add points to player on server
                p.givePoints(currentTreasure.points);
                
                // Update points on clients
                RemoveTreasureMsg m = new RemoveTreasureMsg(playerId);
                InternalMessage im = new InternalMessage(Filters.notEqualTo(conn), m);
                sendPacketQueue.add(im);
                
                if(p.points >= MAX_POINTS){
                    
                    // Set end variables
                    gameRunning = false;
                    restartTime = 0f;
                    
                    // Send gameEndMsg
                    GameEndMsg endM = new GameEndMsg(p.playerId);
                    InternalMessage endIm = new InternalMessage(null, endM);
                    sendPacketQueue.add(endIm);
                }
                break;     
            }
        }        
        
        // Reset treasure variables
        hasTreasure = false;
        treasureTime = 0f;
    }
    
    private void setupTreasurePositions() {
        treasurePositions.add(new float[]{-90.92374f, 2.539936f, 90.56113f});
        treasurePositions.add(new float[]{-116.3291f, 2.5399363f, -14.177889f});
        treasurePositions.add(new float[]{-89.63092f, 2.9399276f, -63.67955f});
        treasurePositions.add(new float[]{-81.99335f, 2.539752f, -123.59012f});
        treasurePositions.add(new float[]{-2.9291267f, 2.539937f, -158.02039f});
        treasurePositions.add(new float[]{55.149002f, 2.5378268f, -191.48337f});
        treasurePositions.add(new float[]{133.49405f, 2.5399373f, -159.00938f});
        treasurePositions.add(new float[]{206.01678f, 2.5399377f, -119.36975f});
        treasurePositions.add(new float[]{302.57294f, 2.5399368f, -123.00379f});
        treasurePositions.add(new float[]{347.40292f, 2.5399377f, -48.368366f});
        treasurePositions.add(new float[]{335.80832f, 2.5399315f, 56.005623f});
        treasurePositions.add(new float[]{35.81703f, 2.5399368f, 137.38936f});
        treasurePositions.add(new float[]{128.88956f, 2.5383317f, 29.887632f});
        treasurePositions.add(new float[]{101.44606f, 2.9399302f, -44.275406f});
        treasurePositions.add(new float[]{46.077446f, 2.9399302f, -48.117783f});
        treasurePositions.add(new float[]{18.869473f, 2.939929f, 11.754691f});
    }
    
    private AnimEventListener animListener = new AnimEventListener() {
        @Override
        public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
            // This is already handled...
        }

        @Override
        public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
            // This is already handled...
        }   
    };
    
    private void resyncPoints() {
        int[][] tempArray = new int[players.size()][2];
            
            for (int i = 0; i < players.size(); i++) {
                tempArray[i][0] = players.get(i).playerId;
                tempArray[i][1] = players.get(i).points;
            }
            
            SyncPointsMsg m = new SyncPointsMsg(tempArray);
            InternalMessage im = new InternalMessage(null, m);
            sendPacketQueue.add(im);
    }

   /**
    * Update function
    * @param tpf 
    */
    @Override
    public void update(float tpf) {
        resyncTime += tpf;
        treasureTime += tpf;
        syncPointsTime += tpf;
        
        // Only count up treasure variable if server doesn't have a treasure
        //FIX LATER...
        
        // Handle game restarts
        if(!gameRunning) {
          restartTime += tpf;
          
          if(restartTime >= RESTART_TIME) {
              
              System.out.println("GAME RESTART");   // DELETE ME L8T3R...
              
              // Send game start to clients
              GameStartMsg gStartMsg = new GameStartMsg();
              InternalMessage im = new InternalMessage(null, gStartMsg);
              sendPacketQueue.add(im);
              
              // Send bell sound to clients
              AudioMsg am = new AudioMsg("CLOCK");
              InternalMessage audio_im = new InternalMessage(null, am);
              sendPacketQueue.add(audio_im);
              
              // Reset all client points
              for(Player p : players) {
                  p.points = 0;
              }
              
              // Sync points to all clients
              resyncPoints();
              
              // Set game as running & reset time variable
              gameRunning = true;
              restartTime = 0f;
          }
        }
        
        if (resyncTime > RESYNC) {
            
            // Create empty arrays to send in message
            int[] idArray = new int[players.size()];
            float[][] posArray = new float[players.size()][3];
            
            // Fill upp array with all player id's and positions
            for(int i=0; i < players.size(); i++) {
                Player currP = players.get(i);
                idArray[i] = currP.playerId;
                
                // Check if player have fallen outside the map
                if(currP.player.getPhysicsLocation().getY() <= -10f) {
                    
                    // Make all clients teleport player back to spawn
                    ForcePlayerResyncMsg fResMsg = new ForcePlayerResyncMsg(currP.playerId, 0, 5, 0);
                    InternalMessage im = new InternalMessage(null, fResMsg);
                    sendPacketQueue.add(im);
                    
                    // Teleport player to spawn
                    currP.player.setPhysicsLocation(new Vector3f(0, 5, 0));
                }
                
                posArray[i][0] = currP.player.getPhysicsLocation().getX();
                posArray[i][1] = currP.player.getPhysicsLocation().getY();
                posArray[i][2] = currP.player.getPhysicsLocation().getZ();
            }
            
            ResyncPositionsMsg rpMsg = new ResyncPositionsMsg(idArray, posArray);
            InternalMessage im = new InternalMessage(null, rpMsg);
            sendPacketQueue.add(im);
            
            resyncTime = 0f;
        }
        
        if(treasureTime > TREASURE_TIME && !hasTreasure && gameRunning) {
            spawnTreasure();
        }
        
        if(syncPointsTime > SYNC_POINTS_TIME) {
            resyncPoints();
            syncPointsTime = 0f;
        }
      
        for (Player p : players) {
            p.getCharacterControl().setWalkDirection(p.getWalkDirection()); // THIS IS WHERE THE WALKING HAPPENS
        }
    }
}
