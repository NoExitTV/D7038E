/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
import com.jme3.network.AbstractMessage;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.scene.Node;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import mygame.GameMessage.*;
import static mygame.GameClient.*;

/**
 *
 * @author Fredrik & Carl
 */
public class TheClient extends SimpleApplication{

    /**
     * 
     */
    BitmapText timeText;
    Node timeTextNode;
    
    private Ask ask = new Ask();
    private GameClient game = new GameClient();
    private float time = 30f;
    private boolean running = false;
    
    // the connection back to the server
    private com.jme3.network.Client serverConnection;
    // the scene contains just a rotating box
    private final String hostname = Util.SERVER;
    private final int port = Util.PORT;
    
    public int playerID = -1;
    
    
    private ConcurrentLinkedQueue<InternalMessage> sendPacketQueue = new ConcurrentLinkedQueue();

    public static void main(String[] args) {
        GameMessage.initSerializer();
        new TheClient().start();
        System.out.println("RestartGameDemo: main");
    }

    public TheClient() {       
        System.out.println("RestartGameDemo: in the constructor");
        ask.setEnabled(false);
        game.setEnabled(false);
        stateManager.attach(game);
        stateManager.attach(ask);
    }
    
    public void setRunning(boolean bool) {
        this.running = bool;
    }
    
    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    public void simpleInitApp() {
        
        //Remove this later...
        //flyCam.setEnabled(false);
        
        System.out.println("Initializing");
        try {
            System.out.println("Opening server connection");
            serverConnection = Network.connectToServer(hostname, port);
            System.out.println("Server is starting networking");

            //Give this connection to game
            game.setConcurrentQ(sendPacketQueue);
        
            //Create a network sender in another thread
            new Thread(new TheClient.NetworkSender(sendPacketQueue, serverConnection)).start();
            
            /**
             * Create listener for network messages
             */
            System.out.println("Adding network listener");
            // this make the client react on messages when they arrive by
            // calling messageReceived in ClientNetworkMessageListener
            serverConnection
                    .addMessageListener(new ClientNetworkMessageListener(),
                            HeartBeatMessage.class,
                            ServerWelcomeMessage.class,
                            NameConflictMessage.class,
                            GameInProgressMessage.class,
                            InitialGameMessage.class,
                            GameStartMessage.class,
                            UpdateDiskVelocityMessage.class,
                            UpdatePlayerScoreMessage.class,
                            UpdateTimeMessage.class,
                            GameOverMessage.class,
                            SendInitPlayerDisk.class,
                            SendInitNegativeDisk.class,
                            SendInitPositiveDisk.class);
            
            // finally start the communication channel to the server
            serverConnection.start();
            System.out.println("Client communication back to server started");
        } catch (IOException ex) {
            ex.printStackTrace();
            this.destroy();
            this.stop();
        }
        
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
                    TheClient.this.stop(); //terminate jMonkeyEngine app
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
        // Do stuff here...
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
    

    // This class is a packet handler
    private class ClientNetworkMessageListener
            implements MessageListener<com.jme3.network.Client> {

        // this method is called whenever network packets arrive
        @Override
        public void messageReceived(com.jme3.network.Client source, final Message m) {
            System.out.println("Client received message form server"+source.getId());
            if(m instanceof HeartBeatMessage){
                HeartBeatAckMessage response = new HeartBeatAckMessage();
                sendPacketQueue.add(new InternalMessage(null, response));
            }
            
            // ServerWelcomeMessage containing player id
            if(m instanceof ServerWelcomeMessage) {
                Util.print(((ServerWelcomeMessage) m).msg);
                
                //Set your player id to the received one
                Future res = TheClient.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        TheClient.this.game.setID(((ServerWelcomeMessage) m).playerID);
                        return true;
                    }
                });
            }
            if(m instanceof GameStartMessage){
                //Set your player id to the received one
                Future res = TheClient.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        TheClient.this.game.setEnabled(true);
                        TheClient.this.setRunning(true);
                        return true;
                    }
                });
            }
            
            if(m instanceof SendInitPlayerDisk) {
                // Enqueue
                Future res = TheClient.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        
                        int id = ((SendInitPlayerDisk) m).playerID;
                        float posX = ((SendInitPlayerDisk) m).posX;
                        float posY = ((SendInitPlayerDisk) m).posY;
                        
                        //Create playerDisk
                        game.createPlayerDisk(id, posX, posY);
                        return true;
                    }
                });
            }
            if(m instanceof SendInitNegativeDisk) {
                // Enqueue
                Future res = TheClient.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        
                        int id = ((SendInitNegativeDisk) m).diskID;
                        float posX = ((SendInitNegativeDisk) m).posX;
                        float posY = ((SendInitNegativeDisk) m).posY;
                        float speedX = ((SendInitNegativeDisk) m).speedX;
                        float speedY = ((SendInitNegativeDisk) m).speedY;
                        
                        //Create playerDisk
                        game.createNegativeDisk(id, posX, posY, speedX, speedY);
                        return true;
                    }
                });
            }
            if(m instanceof SendInitPositiveDisk) {
                // Enqueue
                Future res = TheClient.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        
                        int id = ((SendInitPositiveDisk) m).diskID;
                        float posX = ((SendInitPositiveDisk) m).posX;
                        float posY = ((SendInitPositiveDisk) m).posY;
                        float speedX = ((SendInitPositiveDisk) m).speedX;
                        float speedY = ((SendInitPositiveDisk) m).speedY;
                        
                        //Create playerDisk
                        game.createPositiveDisk(id, posX, posY, speedX, speedY);
                        return true;
                    }
                });
            }
            
            if(m instanceof UpdateDiskVelocityMessage){
                Future res = TheClient.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        Util.print("TESTING YES");
                        return true;
                    }
                });
            }
        }
    } 
    
    class NetworkSender implements Runnable {

        private ConcurrentLinkedQueue q;
        private com.jme3.network.Client serverConnection;;
        
        public NetworkSender(ConcurrentLinkedQueue q, com.jme3.network.Client serverConnection){
            this.q = q;
            this.serverConnection = serverConnection;
        }
        
        @Override
        public void run() {
            while (true) {
                if(!q.isEmpty()) {
                    InternalMessage im = (InternalMessage) q.poll();
                    AbstractMessage am = (AbstractMessage) im.m;
                    serverConnection.send(am);
                } else {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        Util.print("Network sender for client crashed");
                    }
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
}
