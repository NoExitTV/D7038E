/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.Application;
import mygame.*;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.ConnectionListener;
import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.network.Server;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import mygame.GameMessage.*;

/**
 *
 * @author Fredrik Pettersson
 */
public class TheServer extends SimpleApplication{

    private Server server;
    private static int port = Util.PORT;
    
    /**
     * Initialize variables
     */
    private TheServer.Ask ask = new TheServer.Ask();
    private GameServer game = new GameServer();
    private float time = 30f;
    private boolean running = false;

    public static void main(String[] args) {
        System.out.println("Server initializing");
        GameMessage.initSerializer();
        //new TheServer(port).start(JmeContext.Type.Headless);
        new TheServer().start();
    }

    public TheServer() {
        ask.setEnabled(false);
        game.setEnabled(false);
        stateManager.attach(game);
        stateManager.attach(ask);
        //Initialize game state
        //game.initGameState();
    }
    
    
    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    public void simpleInitApp() {
        // In a game server, the server builds and maintains a perfect 
        // copy of the game and makes use of that copy to make descisions 

        try {
            System.out.println("Using port " + port);
            // create the server by opening a port
            server = Network.createServer(port);
            server.start(); // start the server, so it starts using the port
        } catch (IOException ex) {
            ex.printStackTrace();
            destroy();
            this.stop();
        }
        System.out.println("Server started");
        
        // create a separat thread for sending "heartbeats" every now and then
        new Thread(new HeartBeatSender()).start();
        // add a listener that reacts on incoming network packets
        server.addMessageListener(new ServerListener(game), ClientConnectMessage.class,
                ClientLeaveMessage.class, AckMessage.class, HeartBeatAckMessage.class,
                ClientVelocityUpdateMessage.class);
        
        System.out.println("ServerListener aktivated and added to server");
        
        /**
         * Add connection listener
         */
        server.addConnectionListener(new ConnListener(game));
        
        //Init camera settings
        initCam();
        
        /**
         * Give the server connection to the game class
         */
        game.setServerConnection(server);
    }
    
    public void setRunning(boolean bool) {
        this.running = bool;
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        // Do stuff here...
        if (running) {
            time -= tpf;
        
            if (time < 0f) {                
                System.out.println("RestartGameDemo: simpleUpdate "
                        + "(entering when time is up)");
                game.setEnabled(false);
                ask.setEnabled(true);
                time = 30f;
                running = false;
                System.out.println("RestartGameDemo: simpleUpdate "
                        + "(leaving with running==false)");
            }
        }
    }
    
    private void sendInitState() {
        
        /**
         * Send serverWelcomeMessage
         */
        //ServerWelcomeMessage welcome = new ServerWelcomeMessage("Welcome to the game!");
        //server.broadcast(welcome);

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
    
    /*
    Test stuff
    */
    // this class provides a handler for incoming network packets
    private class ServerListener implements MessageListener<HostedConnection> {
        GameServer game;
        
        public ServerListener(GameServer game) {
            super();
            this.game = game;
        }
        
        @Override
        public void messageReceived(HostedConnection source, Message m) {
            System.out.println("Server received message form client"+source.getId());
            if (m instanceof HeartBeatAckMessage) {
                //TheServer.this.server.broadcast(new UpdateDiskVelocityMessage(new Vector3f(0,0,0))); // ... send ...
            }
            if(m instanceof ClientVelocityUpdateMessage) {
                int playerId = ((ClientVelocityUpdateMessage) m).playerID;
                float speedX = ((ClientVelocityUpdateMessage) m).speed.getX();
                float speedY = ((ClientVelocityUpdateMessage) m).speed.getY();
                
                Util.print(Integer.toString(playerId) +" "+ speedX + " "+ speedY);
            }
        }
    }
    
    // This class senses if players connect or disconnect
    private class ConnListener implements ConnectionListener {
        GameServer game;
        int connectedPlayers;
        
        public ConnListener(GameServer game){
            this.game = game;
            connectedPlayers = 0;
        }

        @Override
        public void connectionAdded(Server server, HostedConnection conn) {
            connectedPlayers += 1;
            
            /**
             * Send ServerWelcomeMessage
             */
            ServerWelcomeMessage welcome = new ServerWelcomeMessage("Welcome to the game!");
            server.broadcast(welcome);
                
            if(connectedPlayers == Util.PLAYERS) { 
                
                // Enqueue
                Future res = TheServer.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {

                        //Send init state to all clients
                        TheServer.this.sendInitState();

                        //Start game
                        game.setEnabled(true);

                        //Start counting down time
                        TheServer.this.setRunning(true);
                        return true;
                    }
                });
            }
        }

        @Override
        public void connectionRemoved(Server server, HostedConnection conn) {
            //Do something useful here???
            connectedPlayers -= 1;
        }
        
    }
    
    /**
     * Sends out a heart beat to all clients every TIME_SLEEPING seconds, after
     * first having waited INITIAL_WAIT seconds. .
     */
    private class HeartBeatSender implements Runnable {

        private final int INITIAL_WAIT = 30000; // time until first loop lap 
        private final int TIME_SLEEPING = 10000; // timebetween heartbeats

        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public void run() {
            System.out.println("Heartbeat sender thread running");
            while (true) {
                try {
                    Thread.sleep(TIME_SLEEPING); // ... sleep ...
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                System.out.println("Sending one heartbeat to each client");
                server.broadcast(new HeartBeatMessage()); // ... send ...
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
