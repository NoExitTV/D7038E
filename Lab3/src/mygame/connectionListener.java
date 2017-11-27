/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.network.ConnectionListener;
import com.jme3.network.HostedConnection;
import com.jme3.network.Server;
import mygame.Util.*;

/**
 *
 * @author NoExit
 */
public class connectionListener implements ConnectionListener {

    public connectionListener() {
    
    }

    @Override
    public void connectionAdded(Server server, HostedConnection conn) {
        Util.print("CLIENT CONNECTED");
    }

    @Override
    public void connectionRemoved(Server server, HostedConnection conn) {
        Util.print("CLIENT DISCONNECTED");
    }
    
}
