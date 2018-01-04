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
import java.util.concurrent.ConcurrentLinkedQueue;
import mygame.GameMessage.*;

/**
 *
 * @author NoExit
 */
public class GameServer extends BaseAppState {
    //Constants
    static final float WALKSPEED = 0.75f;
    static final float JUMPSPEED = 20f;
    static final float FALLSPEED = 30f;
    static final float GRAVITY = 30f;
    static final float SEND_AUDIO_TIME = 30f;
    
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
            float posX = p.getNode().getLocalTranslation().getX();
            float posY = p.getNode().getLocalTranslation().getY();
            float posZ = p.getNode().getLocalTranslation().getZ();
            int playerId = p.playerId;
            // Send all player to new player
            CreatePlayerMsg createPlayer = new CreatePlayerMsg(playerId, posX, posY, posZ);
            InternalMessage m = new InternalMessage(Filters.in(conn), createPlayer);
            System.out.println("sent create player message");
            sendPacketQueue.add(m);
        }
        
        // Send new player to everyone except new player
        float posX = newPlayer.getNode().getLocalTranslation().getX();
        float posY = newPlayer.getNode().getLocalTranslation().getY();
        float posZ = newPlayer.getNode().getLocalTranslation().getZ();
        CreatePlayerMsg createPlayer = new CreatePlayerMsg(newPlayerId, posX, posY, posZ);
        InternalMessage m = new InternalMessage(Filters.notEqualTo(conn), createPlayer);
        sendPacketQueue.add(m);
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
        
        if (time > SEND_AUDIO_TIME) {
            AudioMsg m = new AudioMsg("CLOCK");
            InternalMessage im = new InternalMessage(null, m);
            sendPacketQueue.add(im);
            time = 0f;
        }
        
    }
}
