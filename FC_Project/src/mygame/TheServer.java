package mygame;

import com.jme3.app.SimpleApplication;
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
import mygame.GameMessage.*;

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
        server.addMessageListener(new ServerListener(game), SampleMsg.class);
        
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
            
            if (m instanceof SampleMsg) {
                String msg = ((SampleMsg) m).msg;
                System.out.println("Message from client: "+msg);
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
                        return true;
                    }
                });
                
                connectedPlayers += 1;
        }

        @Override
        public void connectionRemoved(Server server, HostedConnection conn) {
            //Do something useful here???
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