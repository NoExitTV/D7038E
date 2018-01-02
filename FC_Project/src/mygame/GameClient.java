/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
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
public class GameClient extends BaseAppState{
    
    // Variables we need
    private SimpleApplication sapp;
    
    private Spatial sceneModel;
    private BulletAppState bulletAppState;
    private RigidBodyControl landscape;
    private CharacterControl player;
    private Vector3f walkDirection = new Vector3f();
    private boolean left = false, right = false, up = false, down = false;

    //Temporary vectors used on each frame.
    //They here to avoid instanciating new vectors on each frame
    private Vector3f camDir = new Vector3f();
    private Vector3f camLeft = new Vector3f();

    
    @Override
    protected void initialize(Application app) {
        sapp = (SimpleApplication) app;
        System.out.println("STARTED GAME");
        
        /** Set up Physics */
    bulletAppState = new BulletAppState();
    sapp.getStateManager().attach(bulletAppState);
    //bulletAppState.getPhysicsSpace().enableDebug(assetManager);

    // We re-use the flyby camera for rotation, while positioning is handled by physics
    sapp.getViewPort().setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
    sapp.getFlyByCamera().setMoveSpeed(100);
    setUpKeys();
    setUpLight();

    // We load the scene from the zip file and adjust its size.
    sapp.getAssetManager().registerLocator("town.zip", ZipLocator.class);
    sceneModel = sapp.getAssetManager().loadModel("main.scene");
    sceneModel.setLocalScale(2f);

    // We set up collision detection for the scene by creating a
    // compound collision shape and a static RigidBodyControl with mass zero.
    CollisionShape sceneShape =
            CollisionShapeFactory.createMeshShape((Node) sceneModel);
    landscape = new RigidBodyControl(sceneShape, 0);
    sceneModel.addControl(landscape);

    // We set up collision detection for the player by creating
    // a capsule collision shape and a CharacterControl.
    // The CharacterControl offers extra settings for
    // size, stepheight, jumping, falling, and gravity.
    // We also put the player in its starting position.
    //CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 6f, 1);
    CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(3f, 4f);
    player = new CharacterControl(capsuleShape, 0.05f);
    player.setJumpSpeed(20);
    player.setFallSpeed(30);
    player.setGravity(30);    
    

    /* Create model */
    Node playerModel = (Node) sapp.getAssetManager().loadModel("Models/Oto/Oto.mesh.xml");
    playerModel.addControl(player);
    playerModel.setLocalScale(0.4f);
    
    player.setPhysicsLocation(new Vector3f(0, 100, 0));
    
    // We attach the scene and the player to the rootnode and the physics space,
    // to make them appear in the game world.
    sapp.getRootNode().attachChild(sceneModel);
    sapp.getRootNode().attachChild(playerModel);
    bulletAppState.getPhysicsSpace().add(landscape);
    bulletAppState.getPhysicsSpace().add(player);
    }

    @Override
    protected void cleanup(Application app) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void onEnable() {
        System.out.println("GAME ENABLED");
    }

    @Override
    protected void onDisable() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        sapp.getInputManager().addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        sapp.getInputManager().addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        sapp.getInputManager().addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
        sapp.getInputManager().addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
        sapp.getInputManager().addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        sapp.getInputManager().addListener(actionListener, "Left");
        sapp.getInputManager().addListener(actionListener, "Right");
        sapp.getInputManager().addListener(actionListener, "Up");
        sapp.getInputManager().addListener(actionListener, "Down");
        sapp.getInputManager().addListener(actionListener, "Jump");
    }
    
    private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals("Left")) {
          if (isPressed) { 
              left = true; 
          } 
          else { 
              left = false; 
          }
        } 
        else if (name.equals("Right")) {
          if (isPressed) { 
              right = true; 
          } 
          else { 
              right = false; 
          }
        } 
        else if (name.equals("Up")) {
          if (isPressed) { 
              up = true; 
          } 
          else { 
              up = false; 
          }
        } 
        else if (name.equals("Down")) {
          if (isPressed) { 
              down = true; 
          } 
          else { 
              down = false; 
          }
        } 
        else if (name.equals("Jump")) {
          player.jump();
        }
        }
    };           
    
    private AnalogListener analogListener = new AnalogListener() {
         public void onAnalog(String name, float value, float tpf) {
         }
    };

    /**
   * This is the main event loop--walking happens here.
   * We check in which direction the player is walking by interpreting
   * the camera direction forward (camDir) and to the side (camLeft).
   * The setWalkDirection() command is what lets a physics-controlled player walk.
   * We also make sure here that the camera moves with player.
   */
    @Override
    public void update(float tpf) {
        camDir.set(sapp.getCamera().getDirection()).multLocal(0.6f);
        camLeft.set(sapp.getCamera().getLeft()).multLocal(0.4f);
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
        player.setWalkDirection(walkDirection);
        sapp.getCamera().setLocation(player.getPhysicsLocation());
    }
}
