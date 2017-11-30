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
import com.jme3.system.JmeContext;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    float time = 30f;
    boolean running = false;

    private ConcurrentLinkedQueue<InternalMessage> sendPacketQueue = new ConcurrentLinkedQueue();
    
    public static void main(String[] args) {
        System.out.println("Server initializing");
        GameMessage.initSerializer();
        new TheServer().start(/*JmeContext.Type.Headless*/);
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
        
        // Create network sender that send messages...
        new Thread(new NetworkSender(sendPacketQueue, server)).start();
        
        // add a listener that reacts on incoming network packets
        server.addMessageListener(new ServerListener(game), ClientConnectMessage.class,
                ClientLeaveMessage.class, AckMessage.class, HeartBeatAckMessage.class,
                ClientVelocityUpdateMessage.class, PlayerAccelerationUpdate.class,
                restartGameMessage.class);
        
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
        game.setConcurrentQ(sendPacketQueue);
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
    
    private void initCam(){
        //Set cam location
        cam.setLocation(new Vector3f(-84f, 0.0f, 720f));
        cam.setRotation(new Quaternion(0.0f, 1.0f, 0.0f, 0.0f));
        
        setDisplayStatView(false);
        setDisplayFps(false);
        
        //Disable camerap
        flyCam.setEnabled(false);
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
            if (m instanceof HeartBeatAckMessage) {
                //TheServer.this.server.broadcast(new UpdateDiskVelocityMessage(new Vector3f(0,0,0))); // ... send ...
            }
            if(m instanceof restartGameMessage) {
                /**
                 * Restart the game...
                 */
                Util.print("GAME SHOULD RESTART!");
            }
            if(m instanceof ClientVelocityUpdateMessage) {
                final int playerId = ((ClientVelocityUpdateMessage) m).playerID;
                final float speedX = ((ClientVelocityUpdateMessage) m).speed.getX();
                final float speedY = ((ClientVelocityUpdateMessage) m).speed.getY();
                final float posX = ((ClientVelocityUpdateMessage) m).posX;
                final float posY = ((ClientVelocityUpdateMessage) m).posY;
                               
                //Update position of the player disk using information FROM the client
                Future res = TheServer.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        for (Disk disk : game.diskList) {
                            if (disk.id == playerId) {
                                disk.setSpeed(new Vector3f(speedX, speedY, 0));
                                disk.getNode().setLocalTranslation(posX, posY, disk.getNode().getLocalTranslation().getZ());
                                break;
                            }
                        }
                        return true;
                    }
                });
                
                //Send the updated player speed TO the other clients
                UpdateDiskVelocityMessage m1 = new UpdateDiskVelocityMessage(new Vector3f(speedX, speedY, 0), playerId);
                sendPacketQueue.add(new InternalMessage(Filters.notEqualTo(source), m1));
                
                //Send the updated player pos TO the other clients
                UpdateDiskPositionMessage m2 = new UpdateDiskPositionMessage(posX, posY, playerId);
                sendPacketQueue.add(new InternalMessage(Filters.notEqualTo(source), m2));
            } 
            if (m instanceof PlayerAccelerationUpdate) {
                //do stuff
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
        public void connectionAdded(Server server, final HostedConnection conn) {
            
            /**
             * Send ServerWelcomeMessage containin the player id
             */
            ServerWelcomeMessage welcome = new ServerWelcomeMessage("Welcome player_"+connectedPlayers, connectedPlayers);
            
            sendPacketQueue.add(new InternalMessage(Filters.in(conn), welcome));
            //server.broadcast(Filters.in(conn), welcome);
               
            connectedPlayers += 1;

            
            // Create player
            Future res1 = TheServer.this.enqueue(new Callable() {
                @Override
                public Object call() throws Exception {

                    //Create player
                    TheServer.this.game.addPlayer(conn);
                    return true;
                }
            });

            if(connectedPlayers == Util.PLAYERS) { 
                
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Util.print("Cloud not sleep before sending GameStartMessage");
                }
                GameStartMessage start = new GameStartMessage();
                sendPacketQueue.add(new InternalMessage(null, start));
                
                // Enqueue
                Future res = TheServer.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {

                        //Start game
                        TheServer.this.game.setEnabled(true);

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
                sendPacketQueue.add(new InternalMessage(null, new HeartBeatMessage())); // ... send ...
            }
        }
    }
    
    class NetworkSender implements Runnable {

        private ConcurrentLinkedQueue q;
        private Server server;
        
        public NetworkSender(ConcurrentLinkedQueue q, Server server){
            this.q = q;
            this.server = server;
        }
        
        @Override
        public void run() {
            while (true) {
                if(!q.isEmpty()) {
                    InternalMessage im = (InternalMessage) q.poll();
                    if(im.filter != null) {
                        server.broadcast(im.filter, im.m);
                    }
                    else {
                        server.broadcast(im.m);
                    }
                } else {
                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException ex) {
                        Util.print("ERROR IN NETWORKSENDER");
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
