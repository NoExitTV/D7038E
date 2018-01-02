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
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

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
    
    private Spatial sceneModel;
    private BulletAppState bulletAppState;
    private RigidBodyControl landscape;
    //private CharacterControl player;
    private Vector3f walkDirection = new Vector3f();
    private boolean left = false, right = false, up = false, down = false;

    //Temporary vectors used on each frame.
    //They here to avoid instanciating new vectors on each frame
    private Vector3f camDir = new Vector3f();
    private Vector3f camLeft = new Vector3f();

    // ChaseCamera variable
    ChaseCamera chaseCam;
    
    // Movement
    private float airTime = 0;
    
    private Player localPlayer;
    
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

    // We load the scene from the zip file and adjust its size.
    sapp.getAssetManager().registerLocator("town.zip", ZipLocator.class);
    sceneModel = sapp.getAssetManager().loadModel("main.scene");
    sceneModel.setLocalScale(2f);

    // We set up collision detection for the scene by creating a
    // compound collision shape and a static RigidBodyControl with mass zero.
    CollisionShape sceneShape = CollisionShapeFactory.createMeshShape((Node) sceneModel);
    landscape = new RigidBodyControl(sceneShape, 0);
    sceneModel.addControl(landscape);
    
    //create player
    localPlayer = new Player(sapp, 1, bulletAppState);
    
    // We attach the scene and the player to the rootnode and the physics space,
    // to make them appear in the game world.
    sapp.getRootNode().attachChild(sceneModel);
    bulletAppState.getPhysicsSpace().add(landscape);
    
    // Add chase camera
    chaseCam = new ChaseCamera(sapp.getCamera(), localPlayer.getNode(), sapp.getInputManager());
    
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
                if (isPressed) left = true;
                else left = false;
            } 
            else if (name.equals("CharRight")) {
                if (isPressed) right = true;
                else right = false;
            } 
            else if (name.equals("CharForward")) {
                if (isPressed) up = true;
                else up = false;
            } 
            else if (name.equals("CharBackward")) {
                if (isPressed) down = true;
                else down = false;
            } 
            else if (name.equals("CharJump"))
                localPlayer.getCharacterControl().jump();
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
        Vector3f camDir = sapp.getCamera().getDirection().clone().multLocal(WALKSPEED);
        Vector3f camLeft = sapp.getCamera().getLeft().clone().multLocal(WALKSPEED);
        camDir.y = 0;
        camLeft.y = 0;
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

        if (!localPlayer.getCharacterControl().onGround()) { // use !character.isOnGround() if the character is a BetterCharacterControl type.
            airTime += tpf;
        } else {
            airTime = 0;
        }

        if (walkDirection.lengthSquared() == 0) { //Use lengthSquared() (No need for an extra sqrt())
            if (!"stand".equals(localPlayer.getAnimationChannel().getAnimationName())) {
              localPlayer.getAnimationChannel().setAnim("stand", 1f);
            }
        } else {
            localPlayer.getCharacterControl().setViewDirection(walkDirection);
            if (airTime > .3f) {
              if (!"stand".equals(localPlayer.getAnimationChannel().getAnimationName())) {
                localPlayer.getAnimationChannel().setAnim("stand");
              }
            } else if (!"Walk".equals(localPlayer.getAnimationChannel().getAnimationName())) {
              localPlayer.getAnimationChannel().setAnim("Walk", 0.7f);
              localPlayer.getAnimationChannel().setSpeed(2.5f);
            }
          }

        localPlayer.getCharacterControl().setWalkDirection(walkDirection); // THIS IS WHERE THE WALKING HAPPENS
    }
}
