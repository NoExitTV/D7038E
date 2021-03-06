package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.math.Vector3f;
import com.jme3.network.ConnectionListener;
import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.renderer.RenderManager;
import com.jme3.system.JmeContext;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import mygame.GameMessage.*;
import static mygame.Util.MAX_PLAYERS;

/**
 *
 * @author Fredrik Pettersson & Carl Borngrund
 */

/**
 * This is the TheClient Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 */
public class TheServer extends SimpleApplication {
    
    // Init variables
    private GameServer game = new GameServer();
    private Server server;
    private ConcurrentLinkedQueue<InternalMessage> sendPacketQueue = new ConcurrentLinkedQueue();
    private static int port = Util.PORT;
    
    
    public static void main(String[] args) {
        System.out.println("Server initializing");
        GameMessage.initSerializer();
        new TheServer().start(JmeContext.Type.Headless); // Start the server headless
    }
    
    public TheServer() {
        game.setEnabled(true);
        stateManager.attach(game);
    }

    @Override
    public void simpleInitApp() {
        
        /**
         *  Try to start the server on given port... 
         */
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
        
        // Notify of server start
        System.out.println("Server started");
        
        /**
         * Create a network sender that polls packages from the queue...
         */
        new Thread(new NetworkSender(sendPacketQueue, server)).start();
        
        /**
         * Add a listener that reacts on incoming network packets
        */
        server.addMessageListener(new ServerListener(game), 
                NewWalkDirectionMsg.class,
                CharacterJumpMsg.class,
                ResyncPlayerPositionMsg.class,
                CaptureTreasureMsg.class
        );
        
        /**
         * Add connection listener
         */
        server.addConnectionListener(new ConnListener(game));
        
        /**
         * Give the server connection to the game class
         */
        game.setConcurrentQ(sendPacketQueue);
        
        System.out.println("Server setup completed (SimpleInitApp)");
    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
    
    /*
    Server listener
    Listens for incoming packets
    */
    private class ServerListener implements MessageListener<HostedConnection> {
        GameServer game;
        
        public ServerListener(GameServer game) {
            super();
            this.game = game;
        }

        @Override
        public void messageReceived(HostedConnection source, Message m) {
            
            if (m instanceof NewWalkDirectionMsg) {
                
                final int playerId = ((NewWalkDirectionMsg) m).playerId;
                float posX = ((NewWalkDirectionMsg) m).posX;
                float posY = ((NewWalkDirectionMsg) m).posY;
                float posZ = ((NewWalkDirectionMsg) m).posZ;
                final Vector3f newWalkDirection = new Vector3f(posX, posY, posZ);
                
                // Send new walk direction to all clients
                SyncWalkDirectionMsg swdMsg = new SyncWalkDirectionMsg(playerId, posX, posY, posZ);
                InternalMessage im = new InternalMessage(null, swdMsg);
                sendPacketQueue.add(im);

                // Update walkDirection on server
                Future res1 = TheServer.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {

                        //Create player
                        TheServer.this.game.updateWalkDirection(playerId, newWalkDirection);
                        return true;
                    }
                });
            }
            
            if(m instanceof CharacterJumpMsg) {
                final int playerId = ((CharacterJumpMsg) m).playerId;
                
                // Announce that character jumped to all clients
                CharacterJumpMsg cjMsg = new CharacterJumpMsg(playerId);
                InternalMessage im = new InternalMessage(Filters.notEqualTo(source), cjMsg);
                sendPacketQueue.add(im);
                
                // Make character jump
                Future res1 = TheServer.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {

                        //Create player
                        TheServer.this.game.characterJump(playerId);
                        return true;
                    }
                });
            }
            
            if(m instanceof ResyncPlayerPositionMsg) {
                final int playerId = ((ResyncPlayerPositionMsg) m).playerId;
                float posX = ((ResyncPlayerPositionMsg) m).posX;
                float posY = ((ResyncPlayerPositionMsg) m).posY;
                float posZ = ((ResyncPlayerPositionMsg) m).posZ;
                final Vector3f playerPos = new Vector3f(posX, posY, posZ);
                
                Future res1 = TheServer.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {

                        //Create player
                        TheServer.this.game.resyncPlayer(playerId, playerPos);
                        return true;
                    }
                });
            }
            
            if(m instanceof CaptureTreasureMsg) {
                final int playerId = ((CaptureTreasureMsg) m).playerId;
                final HostedConnection conn = source;
                
                Future res1 = TheServer.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {

                        //Create player
                        TheServer.this.game.captureTreasure(playerId, conn);
                        return true;
                    }
                });
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
            
            if(server.getConnections().size() <= MAX_PLAYERS) {
                /*
                Player connected! Send server welcome message to client
                */
                ServerWelcomeMsg welcome = new ServerWelcomeMsg("Welcome player_"+conn.getId(), conn.getId());
                sendPacketQueue.add(new InternalMessage(Filters.in(conn), welcome));

                // Create player
                Future res1 = TheServer.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {

                        //Create player
                        TheServer.this.game.addPlayer(conn, conn.getId());

                        //Send treasure to client
                        TheServer.this.game.sendTreasureMsg(conn);
                        return true;
                    }
                });

                connectedPlayers += 1;
            }
            else {
                /*
                Server is full, send message to client
                */
                ServerFullMsg sfMsg = new ServerFullMsg();
                InternalMessage im = new InternalMessage(Filters.in(conn), sfMsg);
                sendPacketQueue.add(im);
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ex) {
                    Logger.getLogger(TheServer.class.getName()).log(Level.SEVERE, null, ex);
                }
                conn.close("SERVER_FULL");
            }
            
        }

        @Override
        public void connectionRemoved(Server server, final HostedConnection conn) {
           
            //Remove users avatar from server
            Future res1 = TheServer.this.enqueue(new Callable() {
                @Override
                public Object call() throws Exception {

                    //Create player
                    TheServer.this.game.removePlayer(conn, conn.getId());
                    return true;
                }
            });
            //Send to all other clients that the user left so they can remove him.
            connectedPlayers -= 1;
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
}
