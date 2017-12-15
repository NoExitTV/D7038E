package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;


/**
 * This is the TheClient Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 */
public class TheClient extends SimpleApplication {
    
    // Init variables
    private GameClient game = new GameClient();
    
    public static void main(String[] args) {
        TheClient app = new TheClient();
        app.start();
        
    }
    
    public TheClient() {
        game.setEnabled(true);
        stateManager.attach(game);
    }

    @Override
    public void simpleInitApp() {
        // Add stuff...
    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}
