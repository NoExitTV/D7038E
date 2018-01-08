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
    static final float WALKSPEED = 0.20f;
    static final float JUMPSPEED = 20f;
    static final float FALLSPEED = 30f;
    static final float GRAVITY = 30f;
    static final float RESYNC = 1f;
    static final float CAM_ROTATE_VERTICAL = 0.9f;
    static final float CAM_ROTATE_HORIZONTAL = 1.8f;
    static final float SYNC_WALK_SPEED = 0.283f;
    
    
    // Time variables
    float tSinceResync = 0f;
    
    
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
    
    //Treasure varibles
    TreasureClass currentTreasure;
   
    
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
            
            // Enable the game when localPlayer have been created
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
            bulletAppState.getPhysicsSpace().remove(tempPlayer.gc);

        } catch (NullPointerException e) {
            System.out.println("Could not remove user. " + e);
        } 

    }
    
    public void spawnTreasure(float[] positions, int points) {
        System.out.println("TREASURE SPAWNED!!!");  // REMOVE
        
        currentTreasure = new TreasureClass(sapp, bulletAppState, positions[0], positions[1], positions[2], points);
    }
    
    void captureTreasure(int playerId) {
        sapp.getRootNode().detachChild(currentTreasure.boxNode);
        bulletAppState.getPhysicsSpace().remove(currentTreasure.gc);
        
        for(Player p : players) {
            if(p.playerId == playerId) {
                p.givePoints(currentTreasure.points);
                break;
            }
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
    
    public void syncWalkDirection(int playerId, Vector3f walkDirection) {
        for(Player p : players) {
            
            // Only sync remote players and not the local player
            if(p.playerId == playerId && p.playerId != localPlayer.playerId) {
                //p.setWalkDirection(walkDirection);
                p.getCharacterControl().setWalkDirection(walkDirection);
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
    
    public void resyncAllPlayers(int[] idArray, float[][] posArray) {
        // This is not optimized! O(n^2) time!
        for(int i=0; i<idArray.length; i++) {
            for(Player p: players) {
                
                if(p.playerId == idArray[i]) {
                    
                    float posX = posArray[i][0];
                    float posY = posArray[i][1];
                    float posZ = posArray[i][2];
                    
                    // Here we update the setPoint values instead of the "real" positions
                    p.setPointX = posX;
                    p.setPointY = posY;
                    p.setPointZ = posZ;
                }
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
        sapp.getInputManager().addMapping("CamUp", new KeyTrigger(KeyInput.KEY_UP));
        sapp.getInputManager().addMapping("CamDown", new KeyTrigger(KeyInput.KEY_DOWN));
        sapp.getInputManager().addMapping("CamLeft", new KeyTrigger(KeyInput.KEY_LEFT));
        sapp.getInputManager().addMapping("CamRight", new KeyTrigger(KeyInput.KEY_RIGHT));
        sapp.getInputManager().addListener(actionListener, "CharLeft", "CharRight");
        sapp.getInputManager().addListener(actionListener, "CharForward", "CharBackward");
        sapp.getInputManager().addListener(actionListener, "CharJump");
        sapp.getInputManager().addListener(analogListener, "CamUp", "CamDown", "CamLeft", "CamRight");
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
               
               // Make local character jump
               localPlayer.getCharacterControl().jump();
            }
            
            /**
             * Calculate walk direction here
             */
            Vector3f camDir = sapp.getCamera().getDirection().clone().multLocal(WALKSPEED);
            Vector3f camLeft = sapp.getCamera().getLeft().clone().multLocal(WALKSPEED);
            
            camDir.y = 0;
            camLeft.y = 0;
            Vector3f walkDirection = new Vector3f(localPlayer.getWalkDirection().getX(), localPlayer.getWalkDirection().getY(), localPlayer.getWalkDirection().getZ());
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

            localPlayer.getCharacterControl().setWalkDirection(walkDirection);
            
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
             
             if(name.equals("CamUp")) {
                 if(chaseCam.getVerticalRotation() < Math.PI/2 - CAM_ROTATE_VERTICAL*2/3) {
                     chaseCam.setDefaultVerticalRotation(chaseCam.getVerticalRotation() + CAM_ROTATE_VERTICAL*tpf);
                 }
             }
             if(name.equals("CamDown")) {
                 if(chaseCam.getVerticalRotation() > 0) {
                     chaseCam.setDefaultVerticalRotation(chaseCam.getVerticalRotation() - CAM_ROTATE_VERTICAL*tpf);
                 }
             }
             if(name.equals("CamLeft")) {
                 chaseCam.setDefaultHorizontalRotation(chaseCam.getHorizontalRotation() - CAM_ROTATE_HORIZONTAL*tpf);
             }
             if(name.equals("CamRight")) {
                 chaseCam.setDefaultHorizontalRotation(chaseCam.getHorizontalRotation() + CAM_ROTATE_HORIZONTAL*tpf);
             }
         }
    };

    /**
     * This function calculates the direciton between setPoint and real values
     * and makes the player walk in that direction
     * @param playerId
     * @param walkDirection 
     */
    private void walkPlayer(int playerId, Vector3f walkDirection) {
        for(Player p : players) {
            if(p.playerId == playerId) {              
                Vector3f destination = new Vector3f(p.setPointX, p.setPointY, p.setPointZ);
                Vector3f origin = new Vector3f(p.player.getPhysicsLocation().getX(), p.player.getPhysicsLocation().getY(), p.player.getPhysicsLocation().getZ()); 
                Vector3f dir = destination.subtract(origin).normalizeLocal();

                Vector3f walk = new Vector3f(dir.getX(), 0, dir.getZ()).multLocal(SYNC_WALK_SPEED);
                p.getCharacterControl().setWalkDirection(walk);
            }
        }
    }
    
    void forceResyncPlayer(int playerId, Vector3f newPos) {
        for(Player p : players) {
            if(p.playerId == playerId) {
                
                // Teleport player and set new setpoint values
                p.getCharacterControl().setPhysicsLocation(newPos);
                p.setPointX = newPos.getX();
                p.setPointY = newPos.getY();
                p.setPointZ = newPos.getZ();
                
            }
        }
    }
    
   /**
    * Update function
    * @param tpf 
    */
    @Override
    public void update(float tpf) {
        
        // Check if localPlayer captured the treasure
        if(localPlayer.gc.getOverlappingCount() > 0 && this.isEnabled()) {
            
            captureTreasure(localPlayer.playerId);
            
            CaptureTreasureMsg m = new CaptureTreasureMsg(localPlayer.playerId);
            InternalMessage im = new InternalMessage(null, m);
            sendPacketQueue.add(im);
        }
        
        for (Player p : players) {
            
            /*
            Do not move the local player...
            Only move remote players
            */
            if(p.playerId != localPlayer.playerId) {
                
                /**
                * Make player move smoothly
                */
                Vector3f setPointVector = new Vector3f(p.setPointX, p.setPointY, p.setPointZ);
                double hypo = Math.pow((p.setPointX-p.getCharacterControl().getPhysicsLocation().getX()), 2) + Math.pow((p.setPointZ-p.getCharacterControl().getPhysicsLocation().getZ()), 2);

                // In here we make the player walk to the setPoint value obtaines from the server
                if(hypo > 5) {
                    walkPlayer(p.playerId, setPointVector); 
                }
                else if(hypo < 0.5) {
                    p.getCharacterControl().setWalkDirection(new Vector3f(0,0,0)); 
                }
                else {
                   p.getCharacterControl().setWalkDirection(p.getCharacterControl().getWalkDirection()); 
                }
            }            
            
            // Update player animation
            if (!p.getCharacterControl().onGround()) { // use !character.isOnGround() if the character is a BetterCharacterControl type.
                p.airTime += tpf;
            } else {
                p.airTime = 0;
            }

            if (p.getCharacterControl().getWalkDirection().lengthSquared() == 0) { //Use lengthSquared() (No need for an extra sqrt())
                if (!"stand".equals(p.getAnimationChannel().getAnimationName())) {
                  p.getAnimationChannel().setAnim("stand", 1f);
                }
            } else {
                p.getCharacterControl().setViewDirection(p.getCharacterControl().getWalkDirection());
                if (p.airTime > .3f) {
                  if (!"stand".equals(p.getAnimationChannel().getAnimationName())) {
                    p.getAnimationChannel().setAnim("stand");
                  }
                } else if (!"Walk".equals(p.getAnimationChannel().getAnimationName())) {
                  p.getAnimationChannel().setAnim("Walk", 0.7f);
                  p.getAnimationChannel().setSpeed(2.5f);
                }
              }
            
            // Resync localPlayer to the server
            if(p.playerId == localPlayer.playerId && tSinceResync >= RESYNC) {
                float posX = p.getCharacterControl().getPhysicsLocation().getX();
                float posY = p.getCharacterControl().getPhysicsLocation().getY();
                float posZ = p.getCharacterControl().getPhysicsLocation().getZ();
                ResyncPlayerPositionMsg rPosMsg = new ResyncPlayerPositionMsg(p.playerId, posX, posY, posZ);
                InternalMessage im = new InternalMessage(null, rPosMsg);
                sendPacketQueue.add(im);
                
                tSinceResync = 0f;
                
            }
            
            tSinceResync += tpf;
        }
    }
}
