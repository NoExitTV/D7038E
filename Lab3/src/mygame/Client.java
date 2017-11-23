/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.network.Network;
import java.io.IOException;

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
    
    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    public void simpleInitApp() {
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
                            ChangeMessage.class,
                            AckMessage.class,
                            HeartMessage.class,
                            HeartAckMessage.class);

            // finally start the communication channel to the server
            serverConnection.start();
            System.out.println("Client communication back to server started");
        } catch (IOException ex) {
            ex.printStackTrace();
            this.destroy();
            this.stop();
        }

    }
    
}
