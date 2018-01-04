/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import com.jme3.network.serializing.Serializer;

/**
 *
 * @author NoExit
 */
public class GameMessage {
    public static void initSerializer() {
            Serializer.registerClass(ServerWelcomeMsg.class);
            Serializer.registerClass(SampleMsg.class); // Delete me...
            Serializer.registerClass(CreatePlayerMsg.class);
            Serializer.registerClass(AudioMsg.class);
            Serializer.registerClass(LeaveMsg.class);
    }
    
    
    // =======================================================================
    // CLIENT -> SERVER MESSAGES
    // =======================================================================
    
    /**
     * Sample msg...
     */
    @Serializable
    public static class SampleMsg extends AbstractMessage {
        String msg;

        public SampleMsg(String msg) {
            this.msg = msg;
        }
        
        public SampleMsg() {
        }
    }
    
    @Serializable
    public static class LeaveMsg extends AbstractMessage {
        String msg;

        public LeaveMsg(String msg) {
            this.msg = msg;
        }
        
        public LeaveMsg() {
        }
    }
    
    // =======================================================================
    // SERVER -> CLIENT MESSAGES
    // =======================================================================
    
    /**
     * This message is sent to one client only
     */
    @Serializable
    public static class ServerWelcomeMsg extends AbstractMessage {
        String msg;
        public int playerID;
        public ServerWelcomeMsg(String msg, int playerID) {
            this.msg = msg;
            this.playerID = playerID;
        }
        
        public ServerWelcomeMsg() {
        }
    }
    
    @Serializable
    public static class CreatePlayerMsg extends AbstractMessage {
        float posX;
        float posY;
        float posZ;
        int playerId;
        
        public CreatePlayerMsg(int playerId, float posX, float posY, float posZ) {
            this.playerId = playerId;
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
        }
        
        public CreatePlayerMsg() {
        }
    }
    
    @Serializable
    public static class AudioMsg extends AbstractMessage {
        String msg;

        public AudioMsg(String msg) {
            this.msg = msg;
        }
        
        public AudioMsg() {
        }
    }
}
