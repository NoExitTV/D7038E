package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.math.Vector3f;
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
 *
 * @author Fredrik Pettersson & Carl Borngrund
 */

/**
 * This is the TheClient Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 */
public class TheClient extends SimpleApplication {
    
    // Init variables
    private GameClient game = new GameClient();
    
    // Packet queue
    private ConcurrentLinkedQueue<InternalMessage> sendPacketQueue = new ConcurrentLinkedQueue();
    
    // Server address
    private String host;
    private int port;
    
    
    // Server variables
    //private final String hostname = Util.SERVER;
    //private final int port = Util.PORT;
    private com.jme3.network.Client serverConnection;
    
    public static void main(String[] args) {
        GameMessage.initSerializer();
        new TheClient("127.0.0.1", 10001).start();        
    }

    
    public TheClient(String host, int port) {
        this.host = host;
        this.port = port;
        game.setEnabled(false);
        stateManager.attach(game);
    }

    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    public void simpleInitApp() {

        System.out.println("Initializing");
        try {
            System.out.println("Opening server connection");
            serverConnection = Network.connectToServer(host, port);
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
                            CreatePlayerMsg.class,
                            AudioMsg.class,
                            ClientLeaveMsg.class,
                            SyncWalkDirectionMsg.class,
                            CharacterJumpMsg.class,
                            ResyncPositionsMsg.class,
                            ResyncPlayerPositionMsg.class,
                            ForcePlayerResyncMsg.class,
                            SpawnTreasureMsg.class,
                            RemoveTreasureMsg.class,
                            SyncPointsMsg.class,
                            GameEndMsg.class,
                            GameStartMsg.class,
                            ServerFullMsg.class
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
    public void destroy() {
        serverConnection.close();
        super.destroy();
        System.exit(1);

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
            
            if (m instanceof ServerFullMsg) {
                System.out.println("Server is full! Try later...");
                System.exit(0);
            }
            
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
                final int points = ((CreatePlayerMsg) m).points;
                
                Future res = TheClient.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        TheClient.this.game.addPlayer(playerId, posX, posY, posZ, points);
                        return true;
                    }
                });
                
            }
            
            if(m instanceof AudioMsg) {
                final String msg = ((AudioMsg) m).msg;
                
                Future res = TheClient.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        TheClient.this.game.playAudio(msg);
                        return true;
                    }
                });
            }
            
            if(m instanceof SyncWalkDirectionMsg) {
                final int playerId = ((SyncWalkDirectionMsg) m).playerId;
                float posX = ((SyncWalkDirectionMsg) m).posX;
                float posY = ((SyncWalkDirectionMsg) m).posY;
                float posZ = ((SyncWalkDirectionMsg) m).posZ;
                final Vector3f walkDirection = new Vector3f(posX, posY, posZ);
                
                Future res = TheClient.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        TheClient.this.game.syncWalkDirection(playerId, walkDirection);
                        return true;
                    }
                });
            }
          
            if(m instanceof ClientLeaveMsg) {
                final int id = ((ClientLeaveMsg) m).connId;
               
                Future res = TheClient.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        TheClient.this.game.removePlayer(id);
                        return true;
                    }
                });
            }
            
            if(m instanceof CharacterJumpMsg) {
                final int playerId = ((CharacterJumpMsg) m).playerId;
                
                // Make character jump
                Future res1 = TheClient.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        TheClient.this.game.characterJump(playerId);
                        return true;
                    }
                });
            }
            
            if(m instanceof ResyncPositionsMsg) {
                final int[] idArray = ((ResyncPositionsMsg) m).idArray;
                final float[][] posArray = ((ResyncPositionsMsg) m).posArray;

                Future res1 = TheClient.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        TheClient.this.game.resyncAllPlayers(idArray, posArray);
                        return true;
                    }
                });
            }
            
            if(m instanceof ForcePlayerResyncMsg) {
                final int playerId = ((ForcePlayerResyncMsg) m).playerId;
                float posX = ((ForcePlayerResyncMsg) m).posX;
                float posY = ((ForcePlayerResyncMsg) m).posY;
                float posZ = ((ForcePlayerResyncMsg) m).posZ;
                final Vector3f newPos = new Vector3f(posX, posY, posZ);
                
                Future res1 = TheClient.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        TheClient.this.game.forceResyncPlayer(playerId, newPos);
                        return true;
                    }
                });
            }
            
            if(m instanceof SpawnTreasureMsg) {
                final float[] pos = ((SpawnTreasureMsg) m).posArray;
                final int points = ((SpawnTreasureMsg) m).points;
                
                Future res1 = TheClient.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        TheClient.this.game.spawnTreasure(pos, points);
                        return true;
                    }
                });
            }
            
            if(m instanceof RemoveTreasureMsg) {
                final int playerId = ((RemoveTreasureMsg) m).playerId;
                
                Future res1 = TheClient.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        TheClient.this.game.captureTreasure(playerId);
                        return true;
                    }
                });
            }
            
            if(m instanceof SyncPointsMsg) {
                final int[][] pointsArray = ((SyncPointsMsg) m).pointArray;
                
                Future res1 = TheClient.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        TheClient.this.game.resyncPoints(pointsArray);
                        return true;
                    }
                });
            }
            
            if(m instanceof GameEndMsg) {
                final int playerId = ((GameEndMsg) m).playerId;
                
                Future res1 = TheClient.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        TheClient.this.game.endGame(playerId);
                        return true;
                    }
                });
            }
            
            if(m instanceof GameStartMsg) {
                
                Future res1 = TheClient.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        TheClient.this.game.restartGame();
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
