package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.network.AbstractMessage;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.renderer.RenderManager;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import mygame.GameMessage.*;


/**
 * This is the TheClient Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 */
public class TheClient extends SimpleApplication {
    
    // Init variables
    private GameClient game = new GameClient();
    
    // Packet queue
    private ConcurrentLinkedQueue<InternalMessage> sendPacketQueue = new ConcurrentLinkedQueue();
    
    // Server variables
    private final String hostname = Util.SERVER;
    private final int port = Util.PORT;
    private com.jme3.network.Client serverConnection;
    
    public static void main(String[] args) {
        GameMessage.initSerializer();
        new TheClient().start();
        System.out.println("Start TheClient: main");
        //TheClient app = new TheClient();
        //app.start();
        
    }
    
    public TheClient() {
        game.setEnabled(false);
        stateManager.attach(game);
    }

    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    public void simpleInitApp() {

        System.out.println("Initializing");
        try {
            System.out.println("Opening server connection");
            serverConnection = Network.connectToServer(hostname, port);
            System.out.println("Client is starting networking");

            //Give this connection to game
            game.setConcurrentQ(sendPacketQueue);

            // Create a network sender in another thread
            new Thread(new TheClient.NetworkSender(sendPacketQueue, serverConnection)).start();

            /**
             * Create listener for network messages
             */
            System.out.println("Adding network listener");
            // this make the client react on messages when they arrive by
            // calling messageReceived in ClientNetworkMessageListener
            serverConnection
                    .addMessageListener(new ClientNetworkMessageListener(),
                            ServerWelcomeMsg.class,
                            CreatePlayerMsg.class
                            );

            // finally start the communication channel to the server
            serverConnection.start();
            System.out.println("Client communication back to server started");
        } catch (IOException ex) {
            ex.printStackTrace();
            this.destroy();
            this.stop();
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
    
    // This class is a packet handler
    private class ClientNetworkMessageListener
            implements MessageListener<com.jme3.network.Client> {

        @Override
        public void messageReceived(Client source, final Message m) {
            
            if (m instanceof ServerWelcomeMsg) {
                Util.print(((ServerWelcomeMsg) m).msg);

                //Set your player id to the received one
                Future res = TheClient.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        TheClient.this.game.setID(((ServerWelcomeMsg) m).playerID);
                        //game.setEnabled(true); // ENABLE THE GAME...
                        return true;
                    }
                });
            }
            
            if(m instanceof CreatePlayerMsg) {
                final int playerId = ((CreatePlayerMsg) m).playerId;
                final float posX = ((CreatePlayerMsg) m).posX;
                final float posY = ((CreatePlayerMsg) m).posY;
                final float posZ = ((CreatePlayerMsg) m).posZ;
                System.out.println("Message recieved");
                Future res = TheClient.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        TheClient.this.game.addPlayer(playerId, posX, posY, posZ);
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
}
