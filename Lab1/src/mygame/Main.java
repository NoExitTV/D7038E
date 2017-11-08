package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioData.DataType;
import com.jme3.audio.AudioNode;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import java.util.ArrayList;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 * @author normenhansen
 */
public class Main extends SimpleApplication {

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    //Set class variables
    protected Geometry wall;
    protected Geometry sphere;
    protected Geometry cylinder;
    
    Node boxNode;
    Node sphereNode;
    Node cylinderNode;
    Node orbitalNode;
    Node audioNode;
    Node systemNode;
    
    //Key binding variables
    float orbitRotation = 1;
    float systemRotation = 1;
    boolean f_b_tiltAll = false;
    boolean cylinderVisible = true;
    
    //Audio variables
    private AudioNode audio_gun;
    
    @Override
    public void simpleInitApp() {
        
        // Disable flycam
        flyCam.setMoveSpeed(0f);
        //flyCam.setEnabled(false);
        
        // Create a blue cuboid
        Box boxMesh = new Box(0.8f, 1.0f, 0.2f);
        wall = new Geometry("blue wall", boxMesh);
        Material mat = new Material(assetManager,
          "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        wall.setMaterial(mat);
        // Create node for wall
        boxNode = new Node("cuboid node");
        boxNode.attachChild(wall);
        
        // Create a yellow sphere
        Sphere sphereMesh = new Sphere(40, 40, 0.5f);
        sphere = new Geometry("yellow sphere", sphereMesh);
        Material mat2 = new Material(assetManager,
          "Common/MatDefs/Misc/Unshaded.j3md");
        mat2.setColor("Color", ColorRGBA.Yellow);
        sphere.setMaterial(mat2);
        // Create node for sphere
        sphereNode = new Node("sphere node");
        sphereNode.attachChild(sphere);
        
        // Create red cylinder
        Cylinder cylMesh = new Cylinder(40, 40, 0.4f, 0.5f, true);
        cylinder = new Geometry("red cylinder", cylMesh);
        Material mat3 = new Material(assetManager,
          "Common/MatDefs/Misc/Unshaded.j3md");
        mat3.setColor("Color", ColorRGBA.Red);
        cylinder.setMaterial(mat3);
        cylinder.rotate(0, 90*FastMath.DEG_TO_RAD, 0);
        // Create node for cylinder
        cylinderNode = new Node("cylinder node");
        cylinderNode.attachChild(cylinder);
        
        // Create orbital node and attach sphere and cylinder node
        orbitalNode = new Node("orbital node");
        //orbitalNode.move(0,0,0);
        //orbitalNode.setLocalTranslation(0.0f, 0.0f ,3.15f);
        orbitalNode.attachChild(sphereNode);
        orbitalNode.attachChild(cylinderNode);
        
        // Create system node and attach cuboid and oribtalnode
        systemNode = new Node("system node");
        systemNode.attachChild(boxNode);
        systemNode.attachChild(orbitalNode);
        
        // Create green audio box indicator
        Box audioMesh = new Box(0.1f, 0.1f, 0.1f);
        Geometry audioBox = new Geometry("Box", audioMesh);
        Material matAudio = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matAudio.setColor("Color", ColorRGBA.Green);
        audioBox.setMaterial(matAudio);
        // Place audioBox in its own node
        audioNode = new Node("audio node");
        audioNode.attachChild(audioBox);
        
        //Attach audio box to systemNode
        systemNode.attachChild(audioNode);
        
        // Attach system node to the root node
        rootNode.attachChild(systemNode);
        
        // Set locations
        boxNode.setLocalTranslation(0.0f, 0.0f, 0.0f);
        sphereNode.setLocalTranslation(0.0f, 0.0f, 0.45f);
        cylinderNode.setLocalTranslation(0.0f, 0.0f, -0.45f);
        orbitalNode.setLocalTranslation(0.0f, 0.0f, -1.45f);
        audioNode.setLocalTranslation(0.0f, 0.0f, -5.0f);
        systemNode.setLocalTranslation(0.0f, 0.0f, 4.0f);
        
        // Initialize key bindings, audio & crosshair
        initKeys();
        initAudio();
        initCrossHairs();
    }
    
    public void simpleUpdate(float tpf) {
        systemNode.rotate(0, systemRotation*0.6f*tpf, 0);
        orbitalNode.rotate(orbitRotation*2.5f*tpf, 0, 0);
    }
    
    /** Custom Keybinding: Map named actions to inputs. */
    private void initKeys() {
        // You can map one or several inputs to one named action
        inputManager.addMapping("sysRotate",    new KeyTrigger(KeyInput.KEY_R));
        inputManager.addMapping("boxForward",   new KeyTrigger(KeyInput.KEY_F));
        inputManager.addMapping("boxBackward",  new KeyTrigger(KeyInput.KEY_B));
        inputManager.addMapping("toggle_F_B",   new KeyTrigger(KeyInput.KEY_T));
        inputManager.addMapping("orbitRotate",  new KeyTrigger(KeyInput.KEY_O));
        inputManager.addMapping("growSphere",   new KeyTrigger(KeyInput.KEY_G));
        inputManager.addMapping("explode",      new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("shrinkSphere", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("shoot",        new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        // Add the names to the action listener.
        inputManager.addListener(actionListener,"sysRotate", "toggle_F_B", "orbitRotate", "explode", "shoot");
        inputManager.addListener(analogListener,"boxForward", "boxBackward", "growSphere", "shrinkSphere");
    }
    
    private void initAudio() {
        /* gun shot sound is to be triggered by a mouse click. */
        audio_gun = new AudioNode(assetManager, "Sound/Effects/Gun.wav", DataType.Buffer);
        audio_gun.setPositional(true);
        audio_gun.setLooping(false);
        audio_gun.setVolume(1);
        audioNode.attachChild(audio_gun);
    }
    
    /** A centred plus sign to help the player aim. */
    protected void initCrossHairs() {
        setDisplayStatView(false);
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("+"); // crosshairs
        ch.setLocalTranslation( // center
          settings.getWidth() / 2 - ch.getLineWidth()/2, settings.getHeight() / 2 + ch.getLineHeight()/2, 0);
        guiNode.attachChild(ch);
    }
    
    /** Creates and returns a snowbal */
    protected Geometry createSnowball() {
        Sphere sphere = new Sphere(30, 30, 0.05f);
        Geometry snowBall = new Geometry("snowball", sphere);
        Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mark_mat.setColor("Color", ColorRGBA.White);
        snowBall.setMaterial(mark_mat);
        return snowBall;
    }
          
  private ActionListener actionListener = new ActionListener() {
    public void onAction(String name, boolean keyPressed, float tpf) {
      if (name.equals("sysRotate") && !keyPressed) {
          systemRotation *= -1;
      }
      if (name.equals("orbitRotate") && !keyPressed) {
          orbitRotation *= -1;
        
      }
      if(name.equals("toggle_F_B") && !keyPressed) {
          f_b_tiltAll = !f_b_tiltAll;
      }
      if(name.equals("explode") && !keyPressed) {
          /*
          Make all snowballs dissapear as well
          */
          if(cylinderVisible){
              cylinderVisible=false;
              orbitalNode.detachChild(cylinderNode);
              audio_gun.playInstance();
          }
          else {
              cylinderVisible = true;
              orbitalNode.attachChild(cylinderNode);
          }
          
      }
      if(name.equals("shoot") && keyPressed){
        // 1. Reset results list.
        CollisionResults results = new CollisionResults();
        // 2. Aim the ray from cam loc to cam direction.
        Ray ray = new Ray(cam.getLocation(), cam.getDirection());
        
        // 3. Check if collision occured
        if(systemNode.collideWith(ray, results) > 0) {
            // 4. Get collision parameters
            Vector3f pt = results.getCollision(0).getContactPoint();
            Vector3f tmp = new Vector3f(0, 0, 0);
            Node nodeHit = results.getCollision(0).getGeometry().getParent();
            
            // 5. Map global position to local
            nodeHit.worldToLocal(pt, tmp);
            
            // 6. Create snowball and add to target
            Geometry snowball = createSnowball();
            snowball.setLocalTranslation(tmp);
            nodeHit.attachChild(snowball);
        }   
      }
    }
  };

  private AnalogListener analogListener = new AnalogListener() {
    public void onAnalog(String name, float value, float tpf) {
        if(name.equals("boxForward")){
            if(f_b_tiltAll){
                systemNode.rotate(-0.8f*tpf, 0.0f, 0.0f);
            } else {
                boxNode.rotate(-0.8f*tpf, 0.0f, 0.0f);
            }
        }
        if(name.equals("boxBackward")){
            if(f_b_tiltAll){
                systemNode.rotate(0.8f*tpf, 0.0f, 0.0f);
            } else {
              boxNode.rotate(0.8f*tpf, 0.0f, 0.0f);  
            }
        }
        if(name.equals("growSphere")) {
            //Set scale value
            float scale = 1.0f+0.6f*tpf;
            //Scale the yellow sphere
            sphereNode.getChild("yellow sphere").scale(scale);
            
            /*Loop trough every child in sphereNode and if child is snowball,
              Move snowball to yellow sphere surface
            */
            for(int i=0; i<sphereNode.getChildren().size(); i++) {
                //if current child is snowball, move to new coordinates
                if(sphereNode.getChild(i).getName().equals(("snowball"))) {
                    Vector3f tmp = new Vector3f(0, 0, 0);
                    sphereNode.getChild(i).getLocalTranslation().mult(scale, tmp);
                    sphereNode.getChild(i).setLocalTranslation(tmp);
                } 
            }
        }
        if(name.equals("shrinkSphere")){
            if(sphereNode.getChild("yellow sphere").getLocalScale().getX() > 0) {
                //Set scale value
                float scale = 1.0f-0.6f*tpf;
                //Scale the yellow sphere
                sphereNode.getChild("yellow sphere").scale(scale);

                /*Loop trough every child in sphereNode and if child is snowball,
                  Move snowball to yellow sphere surface
                */
                for(int i=0; i<sphereNode.getChildren().size(); i++) {
                    //if current child is snowball, move to new coordinates
                    if(sphereNode.getChild(i).getName().equals(("snowball"))) {
                        Vector3f tmp = new Vector3f(0, 0, 0);
                        sphereNode.getChild(i).getLocalTranslation().mult(scale, tmp);
                        sphereNode.getChild(i).setLocalTranslation(tmp);
                    }
                }
            }
        }
    }
  };
}
