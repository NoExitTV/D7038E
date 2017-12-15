/**
 *
 * @author Fredrik Pettersson & Carl Borngrund
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
import java.util.logging.Level;
import java.util.logging.Logger;
import mygame.GameMessage.*;
import static mygame.GameClient.*;


public class TheClient extends SimpleApplication {

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
                            UpdatePlayerScoreMessage.class,
                            UpdateTimeMessage.class,
                            GameOverMessage.class,
                            SendInitPlayerDisk.class,
                            SendInitNegativeDisk.class,
                            SendInitPositiveDisk.class,
                            UpdateDiskPositionMessage.class,
                            UpdateDiskPosAndVelMessage.class,
                            CollisionMessage.class);

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
        timeText.setLocalTranslation(5, FREE_AREA_WIDTH + FRAME_THICKNESS, 0);
        timeTextNode = new Node("time");
        timeTextNode.attachChild(timeText);

    }

    private void initCam() {
        //Set cam location
        cam.setLocation(new Vector3f(-84f, 0.0f, 720f));
        cam.setRotation(new Quaternion(0.0f, 1.0f, 0.0f, 0.0f));

        setDisplayStatView(false);
        setDisplayFps(false);

        //Disable camerap
        flyCam.setEnabled(false);
    }

    private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            System.out.println("RestartGameDemo/actionlistener: onAction");
            if (isPressed) { // on the key being pressed...
                if (name.equals("Exit")) {
                    
                    /**
                     * Send ClientLeaveMessage message
                     */
                    ClientLeaveMessage msg = new ClientLeaveMessage(game.myPlayer.id);
                    sendPacketQueue.add(new InternalMessage(null, msg));
                    System.out.println("Client leave...");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(TheClient.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    // Quit game...
                    serverConnection.close();
                    TheClient.this.stop(); //terminate jMonkeyEngine app
                    //System.exit(0);
                } else if (name.equals("Restart")) {
                    ask.setEnabled(false);
                    // take away the text asking 
                    //game.setEnabled(true); // restart the game 
                    //running = true;
                    System.out.println("RestartGameDemo/actionlistener: "
                            + "(setting running to true)");
                    // disable further calls - this also removes the second 
                    // event (the key release) that otherwise would follow 
                    // after a key being (de-) pressed
                    inputManager.deleteMapping("Restart");
                    inputManager.deleteMapping("Exit");
                    
                    //Send new restart game message...
                    restartGameMessage msg = new restartGameMessage(TheClient.this.game.myPlayer.id);
                    sendPacketQueue.add(new InternalMessage(null, msg));
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
            timeText.setText("Time:" + time + "\n");

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

            if (m instanceof HeartBeatMessage) {
                HeartBeatAckMessage response = new HeartBeatAckMessage();
                sendPacketQueue.add(new InternalMessage(null, response));
            }
            
            if(m instanceof UpdatePlayerScoreMessage) {
                final int playerId = ((UpdatePlayerScoreMessage) m).playerId;
                final int score = ((UpdatePlayerScoreMessage) m).score;
                
                Future res = TheClient.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                       for(PlayerDisk disk : game.playerDiskList) {
                           if(disk.id == playerId) {
                               disk.points = score;
                               break;
                           }
                       }
                       return true;
                    }
                });                 
            }
            /**
             * Handle gameOver message
             */
            if (m instanceof GameOverMessage) {
                final String msg = ((GameOverMessage) m).endMsg;
                System.out.println(msg);
                //Set your player id to the received one
                Future res = TheClient.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                       game.setEnabled(false);
                       ask.setWinnerString(msg);
                       ask.setEnabled(true);
                       return true;
                    }
                }); 
            }
            
            /**
             * Think about how to handle this..
             * Update in future
             */
            if (m instanceof GameInProgressMessage) {
                final String msg = ((GameInProgressMessage) m).msg;
                
                //Set your player id to the received one
                Future res = TheClient.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                       System.out.println(msg);
                       System.exit(0);
                       return true;
                    }
                });  
            }
            
            // ServerWelcomeMessage containing player id
            if (m instanceof ServerWelcomeMessage) {
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
            if (m instanceof GameStartMessage) {
                //Set your player id to the received one
                Future res = TheClient.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        TheClient.this.ask.setEnabled(false);
                        TheClient.this.game.setEnabled(true);
                        TheClient.this.setRunning(true);
                        return true;
                    }
                });
            }

            if (m instanceof SendInitPlayerDisk) {
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
            if (m instanceof SendInitNegativeDisk) {
                // Enqueue
                Future res = TheClient.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        
                        Thread.sleep(100);
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
            if (m instanceof SendInitPositiveDisk) {
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
            /**
             * Update position and velocity
             */
            if (m instanceof UpdateDiskPosAndVelMessage) {

                Future res = TheClient.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        for (Disk disk : game.diskList) {
                            if (disk.id == ((UpdateDiskPosAndVelMessage) m).diskId) {
                                disk.setPointX = ((UpdateDiskPosAndVelMessage) m).posX;
                                disk.setPointY = ((UpdateDiskPosAndVelMessage) m).posY;
                                disk.setPointSpeedX = ((UpdateDiskPosAndVelMessage) m).speed.getX();
                                disk.setPointSpeedY = ((UpdateDiskPosAndVelMessage) m).speed.getY(); 
                                break;
                            }
                        }
                        return true;
                    }
                });
            }
            /**
             * Update position and velocity
             * and give points after collision
             */
            if (m instanceof CollisionMessage) {
                Future res = TheClient.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        Disk disk1 = null;
                        Disk disk2 = null;
                        int diskId1 = ((CollisionMessage) m).disk1;
                        int diskId2 = ((CollisionMessage) m).disk2;
                        Vector3f speed1 = ((CollisionMessage) m).speed1;
                        Vector3f speed2 = ((CollisionMessage) m).speed2;
                        float posX1 = ((CollisionMessage) m).posX1;
                        float posX2 = ((CollisionMessage) m).posX2;
                        float posY1 = ((CollisionMessage) m).posY1;
                        float posY2 = ((CollisionMessage) m).posY2;
                        
                        for (Disk disk : game.diskList) {
                            if (disk.id == diskId1) {
                                disk1 = disk;
                                disk.setPointX = posX1;
                                disk.setPointY = posY1;
                                disk.setPointSpeedX = speed1.getX();
                                disk.setPointSpeedY = speed1.getY(); 
                            }
                            if (disk.id == diskId2) {
                                disk2 = disk;
                                disk.setPointX = posX2;
                                disk.setPointY = posY2;
                                disk.setPointSpeedX = speed2.getX();
                                disk.setPointSpeedY = speed2.getY(); 
                            }
                        }
                        disk1.givePointsTo(disk2);
                        disk2.givePointsTo(disk1);
                        return true;
                    }
                });
            }
        }
    }

    class NetworkSender implements Runnable {

        private ConcurrentLinkedQueue q;
        private com.jme3.network.Client serverConnection;
        
        public NetworkSender(ConcurrentLinkedQueue q, com.jme3.network.Client serverConnection) {
            this.q = q;
            this.serverConnection = serverConnection;
        }

        @Override
        public void run() {
            while (true) {
                if (!q.isEmpty()) {
                    InternalMessage im = (InternalMessage) q.poll();
                    AbstractMessage am = (AbstractMessage) im.m;
                    serverConnection.send(am);
                } else {
                    try {
                        Thread.sleep(2);
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
        
        public void setWinnerString(String str) {
            BitmapFont myFont
                    = sapp.getAssetManager()
                            .loadFont("Interface/Fonts/Console.fnt");
            BitmapText hudText = new BitmapText(myFont, false);
            hudText.setSize(myFont.getCharSet().getRenderedSize() * 2);
            hudText.setColor(ColorRGBA.White);
            hudText.setText(str);
            hudText.setLocalTranslation(120, 4*hudText.getLineHeight(), 0);
            sapp.getGuiNode().attachChild(hudText);
        }
    }
}
