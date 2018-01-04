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
import com.jme3.audio.AudioData.DataType;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.util.SkyFactory;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import mygame.GameMessage.*;

/**
 *
 * @author NoExit
 */
public class GameClient extends BaseAppState {
    //Constants
    static final float WALKSPEED = 0.75f;
    static final float JUMPSPEED = 20f;
    static final float FALLSPEED = 30f;
    static final float GRAVITY = 30f;
    
    
    // Variables we need
    private SimpleApplication sapp;
    
    private BulletAppState bulletAppState;
    private RigidBodyControl landscape;
    //private Vector3f walkDirection = new Vector3f();

    private boolean left = false, right = false, up = false, down = false;

    //Temporary vectors used on each frame.
    //They here to avoid instanciating new vectors on each frame
    private Vector3f camDir = new Vector3f();
    private Vector3f camLeft = new Vector3f();
    private AudioNode audio_gun;


    // ChaseCamera variable
    ChaseCamera chaseCam;
    
    private float airTime = 0;
    private ArrayList<Player> players = new ArrayList();
    private Player localPlayer;
    int localId;
    
    // Network queue
    private ConcurrentLinkedQueue sendPacketQueue;
   
    
    /**
     * Function to set concurrent linked queue
     * @param q 
     */
    public void setConcurrentQ(ConcurrentLinkedQueue q) {
        this.sendPacketQueue = q;
    }
    
    public void setID(int id) {
        localId = id;
    }
    
    @Override
    protected void initialize(Application app) {
        sapp = (SimpleApplication) app;
        System.out.println("STARTED GAME");
        
    /** Set up Physics */
    bulletAppState = new BulletAppState();
    sapp.getStateManager().attach(bulletAppState);

    // We re-use the flyby camera for rotation, while positioning is handled by physics
    sapp.getViewPort().setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
    sapp.getFlyByCamera().setMoveSpeed(100);
    sapp.getFlyByCamera().setEnabled(false);
    
    setUpKeys();
    setUpLight();    
    
    /* Create landscape */
    Landscape landScape = new Landscape(sapp, bulletAppState);
      
    setUpAudio();
    sapp.getRootNode().attachChild(SkyFactory.createSky(sapp.getAssetManager(), "Textures/Sky/Bright/BrightSky.dds", SkyFactory.EnvMapType.CubeMap));
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
        System.out.println("GAME ENABLED");
    }

    @Override
    protected void onDisable() {
        System.out.println("goodbye");
    }
    
    public void addPlayer(int playerId, float posX, float posY, float posZ) {
        Player tempPlayer = new Player(sapp, playerId, bulletAppState);
        tempPlayer.player.setPhysicsLocation(new Vector3f(posX, posY, posZ));
        players.add(tempPlayer);
        System.out.println("Created player with id " + playerId);
        if(tempPlayer.playerId == localId) {
            System.out.println("Assingning temp player to local player");
            localPlayer = tempPlayer;
            // Add chase camera
            chaseCam = new ChaseCamera(sapp.getCamera(), localPlayer.getNode(), sapp.getInputManager());
            setEnabled(true);
        }
        
    }
    
    public void removePlayer(int id) {
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

        } catch (NullPointerException e) {
            System.out.println("Could not remove user. " + e);
        } 

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
    
    public void setUpAudio() {
        //Create the sound node
        audio_gun = new AudioNode(sapp.getAssetManager(), "Sound/clock.ogg", DataType.Buffer);

        audio_gun.setPositional(false);
        audio_gun.setLooping(false);
        audio_gun.setVolume(2);
        sapp.getRootNode().attachChild(audio_gun);
    }
    
    public void playAudio(String m) {
        if (m.compareTo("CLOCK") == 0) {
            audio_gun.playInstance(); // play each instance once!
        }
    }
    
    public void syncWalkDirection(int playerId, Vector3f waldDirection) {
        for(Player p : players) {
            if(p.playerId == playerId) {
                p.setWalkDirection(waldDirection);
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
    
    /** We over-write some navigational key mappings here, so we can
    * add physics-controlled walking and jumping: */
    private void setUpKeys() {
        sapp.getInputManager().addMapping("CharLeft", new KeyTrigger(KeyInput.KEY_A));
        sapp.getInputManager().addMapping("CharRight", new KeyTrigger(KeyInput.KEY_D));
        sapp.getInputManager().addMapping("CharForward", new KeyTrigger(KeyInput.KEY_W));
        sapp.getInputManager().addMapping("CharBackward", new KeyTrigger(KeyInput.KEY_S));
        sapp.getInputManager().addMapping("CharJump", new KeyTrigger(KeyInput.KEY_SPACE));
        sapp.getInputManager().addListener(actionListener, "CharLeft", "CharRight");
        sapp.getInputManager().addListener(actionListener, "CharForward", "CharBackward");
        sapp.getInputManager().addListener(actionListener, "CharJump");
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
    
    private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals("CharLeft")) {

                if (isPressed) {
                    left = true;
                }
                else {
                    left = false;
                }
            } 
            else if (name.equals("CharRight")) {
                if (isPressed) {
                    right = true;
                }
                else {
                    right = false;
                }
            } 
            else if (name.equals("CharForward")) {
                if (isPressed) {
                    up = true;
                }
                else {
                    up = false;
                }
            } 
            else if (name.equals("CharBackward")) {
                if (isPressed) {
                    down = true;
                }
                else {
                    down = false;
                }
            } 
            else if (name.equals("CharJump")){
               CharacterJumpMsg cjMsg = new CharacterJumpMsg(localPlayer.playerId);
               InternalMessage im = new InternalMessage(null, cjMsg);
               sendPacketQueue.add(im);
            }
            
            /**
             * Calculate walk direction here
             */
            Vector3f camDir = sapp.getCamera().getDirection().clone().multLocal(WALKSPEED);
            Vector3f camLeft = sapp.getCamera().getLeft().clone().multLocal(WALKSPEED);
            camDir.y = 0;
            camLeft.y = 0;
            Vector3f walkDirection = localPlayer.getWalkDirection();
            walkDirection.set(0, 0, 0);

            if (left) {
                walkDirection.addLocal(camLeft);
            }
            if (right) {
                walkDirection.addLocal(camLeft.negate());
            }
            if (up) {
                walkDirection.addLocal(camDir);
            }
            if (down) {
                walkDirection.addLocal(camDir.negate());
            }

            localPlayer.setWalkDirection(walkDirection);
            
            // Send new walkDirection message
            float posX = walkDirection.getX();
            float posY = walkDirection.getY();
            float posZ = walkDirection.getZ();
            NewWalkDirectionMsg nwMsg = new NewWalkDirectionMsg(localPlayer.playerId, posX, posY, posZ);
            InternalMessage msg = new InternalMessage(null, nwMsg);
            sendPacketQueue.add(msg);                
        }
    };           
    
    private AnalogListener analogListener = new AnalogListener() {
         public void onAnalog(String name, float value, float tpf) {
         }
    };

   /**
    * Update function
    * @param tpf 
    */
    @Override
    public void update(float tpf) {
        /*
        if (!localPlayer.getCharacterControl().onGround()) { // use !character.isOnGround() if the character is a BetterCharacterControl type.
            airTime += tpf;
        } else {
            airTime = 0;
        }

        if (localPlayer.getWalkDirection().lengthSquared() == 0) { //Use lengthSquared() (No need for an extra sqrt())
            if (!"stand".equals(localPlayer.getAnimationChannel().getAnimationName())) {
              localPlayer.getAnimationChannel().setAnim("stand", 1f);
            }
        } else {
            localPlayer.getCharacterControl().setViewDirection(localPlayer.getWalkDirection());
            if (airTime > .3f) {
              if (!"stand".equals(localPlayer.getAnimationChannel().getAnimationName())) {
                localPlayer.getAnimationChannel().setAnim("stand");
              }
            } else if (!"Walk".equals(localPlayer.getAnimationChannel().getAnimationName())) {
              localPlayer.getAnimationChannel().setAnim("Walk", 0.7f);
              localPlayer.getAnimationChannel().setSpeed(2.5f);
            }
          }
        */
        for (Player p : players) {
            // Walk all players
            p.getCharacterControl().setWalkDirection(p.getWalkDirection());
        } 
    }
}
