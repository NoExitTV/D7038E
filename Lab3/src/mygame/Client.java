/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import mygame.GameMessage.*;

/**
 *
 * @author Fredrik & Carl
 */
public class Client extends SimpleApplication{

    
    // the connection back to the server
    private com.jme3.network.Client serverConnection;
    // the scene contains just a rotating box
    private static String hostname = "127.0.0.1"; // where the server can be found
    private static int port = 10001; // the port att the server that we use
    
    public static void main(String[] args) {
        GameMessage.initSerializer();
        new Client(hostname, port).start();
    }

    public Client(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }
    
    public void testPrint(){
        System.out.println("TESTPRINT WORK");
    }
    
    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    public void simpleInitApp() {
        
        flyCam.setEnabled(false);
        System.out.println("Initializing");
        try {
            System.out.println("Opening server connection");
            serverConnection = Network.connectToServer(hostname, port);
            System.out.println("Server is starting networking");

        
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
                            GameOverMessage.class);

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
        // Do stuff here...
    }
    

    // This class is a packet handler
    private class ClientNetworkMessageListener
            implements MessageListener<com.jme3.network.Client> {

        // this method is called whenever network packets arrive
        @Override
        public void messageReceived(com.jme3.network.Client source, Message m) {
            System.out.println("Client received message form server"+source.getId());
            if(m instanceof HeartBeatMessage){
                HeartBeatAckMessage response = new HeartBeatAckMessage();
                serverConnection.send(response);
            }
            if(m instanceof UpdateDiskVelocityMessage){
                Future res = Client.this.enqueue(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        Client.this.testPrint();
                        return true;
                    }
                });
            }
        }
    } 
}
