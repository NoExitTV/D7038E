/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.system.JmeContext;
import java.io.IOException;
import mygame.GameMessage.*;

/**
 *
 * @author Fredrik Pettersson
 */
public class StartServer extends SimpleApplication{

    private Server server;
    private static int port = 10001;

    public static void main(String[] args) {
        System.out.println("Server initializing");
        GameMessage.initSerializer();
        new StartServer(port).start(JmeContext.Type.Headless);
    }

    public StartServer(int port) {
        this.port = port;
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
        server.addMessageListener(new ServerListener(), ClientConnectMessage.class,
                ClientLeaveMessage.class, AckMessage.class, HeartBeatAckMessage.class,
                ClientVelocityUpdateMessage.class);
        
        System.out.println("ServerListener aktivated and added to server");
    }
    
    /*
    Test stuff
    */
    // this class provides a handler for incoming network packets
    private class ServerListener implements MessageListener<HostedConnection> {
        @Override
        public void messageReceived(HostedConnection source, Message m) {
            System.out.println("Server received message form client"+source.getId());
            /*
            if (m instanceof ClientConnectMessage) {
                System.out.println("TEST");
            }
            */ 
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
}
