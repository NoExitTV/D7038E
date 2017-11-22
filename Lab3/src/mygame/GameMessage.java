package mygame;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import com.jme3.network.serializing.Serializer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Fredrik Pettersosn
 */
public class GameMessage {
    public static void initSerializer() {
        Serializer.registerClass(GameMessage.class);
    }
    
    private abstract class message extends AbstractMessage {
        
    }
    
    // =======================================================================
    // CLIENT -> SERVER
    // =======================================================================
    
    @Serializable
    public static class connectMessage extends AbstractMessage {
        
        String nickname;
        String client;
        //Do stuff here lixx....
        public connectMessage(String n, String client) {
            this.nickname = n;
            this.client = client;
        }
    }
    
    // =======================================================================
    // SERVER -> CLIENT
    // =======================================================================
}
