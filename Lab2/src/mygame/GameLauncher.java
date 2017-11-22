package mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.system.JmeContext;
import static mygame.Game.*;

/**
 * An example using two AppStates to introduce a way to programmatically pause
 * the program (a "game") and ask the user to press a key on the keyboard to
 * play the game" again. If - when the call to press a key is visible -  P is 
 * pressed, the "game" restarts and if E is pressed the whole program instead terminates. 
 *
 * @author Fredrik Pettersson
 */
public class GameLauncher extends SimpleApplication {

    BitmapText timeText;
    Node timeTextNode;
    
    private Ask ask = new Ask();
    private Game game = new Game();
    private float time = 30f;
    private boolean running = true;

    public GameLauncher() {
        System.out.println("RestartGameDemo: in the constructor");
        ask.setEnabled(false);
        game.setEnabled(true);
        stateManager.attach(game);
        stateManager.attach(ask);
    }

    public static void main(String[] args) {
        System.out.println("RestartGameDemo: main");
        GameLauncher app = new GameLauncher();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        //Init camera settings
        initCam();
        
        System.out.println("RestartGameDemo: simpleInitApp");
        
        //Create text print
        BitmapFont myFont
                = this.getAssetManager()
                        .loadFont("Interface/Fonts/Console.fnt");
        timeText = new BitmapText(myFont, false);
        timeText.setSize(myFont.getCharSet().getRenderedSize() * 2);
        timeText.setColor(ColorRGBA.White);
        timeText.setLocalTranslation(5, FREE_AREA_WIDTH+FRAME_THICKNESS, 0);
        timeTextNode = new Node("time");
        timeTextNode.attachChild(timeText);
    }

    private void initCam(){
        //Set cam location
        cam.setLocation(new Vector3f(-84f, 0.0f, 720f));
        cam.setRotation(new Quaternion(0.0f, 1.0f, 0.0f, 0.0f));
        
        setDisplayStatView(false);
        setDisplayFps(false);
        
        //Disable camerap
        flyCam.setEnabled(false);
        //flyCam.setMoveSpeed(500f);
    }
    
    private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            System.out.println("RestartGameDemo/actionlistener: onAction");
            if (isPressed) { // on the key being pressed...
                if (name.equals("Exit")) {
                    GameLauncher.this.stop(); //terminate jMonkeyEngine app
                    // System.exit(0) would also work
                } else if (name.equals("Restart")) {
                    ask.setEnabled(false);
                    // take away the text asking 
                    game.setEnabled(true); // restart the game 
                    running = true;
                    System.out.println("RestartGameDemo/actionlistener: "
                            + "(setting running to true)");
                    // disable further calls - this also removes the second 
                    // event (the key release) that otherwise would follow 
                    // after a key being (de-) pressed
                    inputManager.deleteMapping("Restart");
                    inputManager.deleteMapping("Exit");
                }
            }
        }
    };

    @Override
    public void simpleUpdate(float tpf) {
        if (running) {
            time -= tpf;

            this.getGuiNode().attachChild(timeTextNode);
            timeText.setText("Time:"+time+"\n");
        
            if (time < 0f) {
                timeText.setText("Time: 0\n");
                
                System.out.println("RestartGameDemo: simpleUpdate "
                        + "(entering when time is up)");
                game.setEnabled(false);
                inputManager.addMapping("Restart",
                        new KeyTrigger(KeyInput.KEY_P)); // enable calls
                inputManager.addMapping("Exit",
                        new KeyTrigger(KeyInput.KEY_E));
                inputManager.addListener(actionListener, "Restart", "Exit");
                ask.setEnabled(true);
                time = 30f;
                running = false;
                System.out.println("RestartGameDemo: simpleUpdate "
                        + "(leaving with running==false)");
            }
        }
    }
}

class Ask extends BaseAppState {

    private SimpleApplication sapp;

    @Override
    protected void initialize(Application app) {
        System.out.println("Ask: initialize");
        sapp = (SimpleApplication) app;
    }

    @Override
    protected void cleanup(Application app) {
        System.out.println("Ask: cleanup");

    }

    @Override
    protected void onEnable() {
        System.out.println("Ask: onEnable (asking)");
        // create a text in the form of a bitmap, and add it to the GUI pane
        BitmapFont myFont
                = sapp.getAssetManager()
                        .loadFont("Interface/Fonts/Console.fnt");
        BitmapText hudText = new BitmapText(myFont, false);
        hudText.setSize(myFont.getCharSet().getRenderedSize() * 2);
        hudText.setColor(ColorRGBA.White);
        hudText.setText("PRESS P TO RESTART AND E TO EXIT");
        hudText.setLocalTranslation(120, hudText.getLineHeight(), 0);
        sapp.getGuiNode().attachChild(hudText);
    }

    @Override
    protected void onDisable() {
        System.out.println("Ask: onDisable (user pressed P)");
        sapp.getGuiNode().detachAllChildren();
    }
}