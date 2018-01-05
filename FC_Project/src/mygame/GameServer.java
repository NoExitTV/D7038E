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
import com.jme3.network.Filter;
import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import mygame.GameMessage.*;

/**
 *
 * @author NoExit
 */
public class GameServer extends BaseAppState {
    //Constants
    static final float WALKSPEED = 0.20f;
    static final float JUMPSPEED = 20f;
    static final float FALLSPEED = 30f;
    static final float GRAVITY = 30f;
    static final float SEND_AUDIO_TIME = 30f;
    static final float RESYNC = 1f;
    
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
    private float time = 0f;
    private float resyncTime = 0f;

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
    
    }

    /**
     * Add player to environment and update everyone
     * @param conn 
     */
    public void addPlayer(HostedConnection conn, int newPlayerId){
        Player newPlayer = new Player(sapp, newPlayerId, bulletAppState);
        players.add(newPlayer);
        
        for(Player p : players) {
            float posX = p.player.getPhysicsLocation().getX();
            float posY = p.player.getPhysicsLocation().getY();
            float posZ = p.player.getPhysicsLocation().getZ();
            int playerId = p.playerId;
            // Send all player to new player
            CreatePlayerMsg createPlayer = new CreatePlayerMsg(playerId, posX, posY, posZ);
            InternalMessage m = new InternalMessage(Filters.in(conn), createPlayer);
            System.out.println("sent create player message");
            sendPacketQueue.add(m);
        }
        
        // Send new player to everyone except new player
        float posX = newPlayer.player.getPhysicsLocation().getX();
        float posY = newPlayer.player.getPhysicsLocation().getY();
        float posZ = newPlayer.player.getPhysicsLocation().getZ();
        CreatePlayerMsg createPlayer = new CreatePlayerMsg(newPlayerId, posX, posY, posZ);
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
            }
        }
    }
    
    public void characterJump(int playerId) {
        for(Player p : players) {
            if(p.playerId == playerId) {
                p.getCharacterControl().jump();
            }
        }
    }
    
    public void resyncPlayer(int playerId, Vector3f playerPos) {
        for(Player p : players) {
            if(p.playerId == playerId) {
                p.getCharacterControl().setPhysicsLocation(playerPos);
                System.out.println("Resynced player: "+playerId);   // REMOVE THIS PRINT LATER...
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

   /**
    * Update function
    * @param tpf 
    */
    @Override
    public void update(float tpf) {
        time += tpf;
        resyncTime += tpf;
        
        if (time > SEND_AUDIO_TIME) {
            AudioMsg m = new AudioMsg("CLOCK");
            InternalMessage im = new InternalMessage(null, m);
            sendPacketQueue.add(im);
            time = 0f;
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
      
        for (Player p : players) {
            p.getCharacterControl().setWalkDirection(p.getWalkDirection()); // THIS IS WHERE THE WALKING HAPPENS
        }
    }
}
