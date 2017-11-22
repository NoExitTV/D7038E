package mygame;

import com.jme3.math.Vector3f;
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
 * @author Fredrik & Carl
 */
public class GameMessage {
    public static void initSerializer() {
        Serializer.registerClass(GameMessage.class);
    }
    
    private abstract class message extends AbstractMessage {
        
    }
    
    // =======================================================================
    // CLIENT -> SERVER MESSAGES
    // =======================================================================
    
    @Serializable
    public static class clientConnectMessage extends AbstractMessage {
        
        String nickname;
        Client client;
        
        public clientConnectMessage(String n, Client client) {
            this.nickname = n;
            this.client = client;
        }
    }
    
    @Serializable
    public static class clientLeaveMessage extends AbstractMessage {
        
        String nickname;
        Client client;
        
        public clientLeaveMessage(String n, Client client) {
            this.nickname = n;
            this.client = client;
        }
    }
    
    @Serializable
    public static class clientVelocityUpdateMessage extends AbstractMessage {
        
        /**
         * Send who you are to the server
         * and the new velocity
         */
        
        public clientVelocityUpdateMessage() {
            
        }
    }
    
    
    
    // =======================================================================
    // SERVER -> CLIENT MESSAGES
    // =======================================================================
    
    /**
     * This message is sent to one client only
     */
    @Serializable
    public static class serverWelcomeMessage extends AbstractMessage {
        
        String msg;
        public serverWelcomeMessage(String msg) {
            this.msg = msg;
        }
    }
    
    /**
     * This message is sent to one client only
     */
    @Serializable
    public static class nameConflictMessage extends AbstractMessage {
        
        String msg;
        public nameConflictMessage(String msg) {
            this.msg = msg;
        }
    }
    
    @Serializable
    public static class gameInProgressMessage extends AbstractMessage {
        
        String msg;
        public gameInProgressMessage(String msg) {
            this.msg = msg;
        }
    }
    
    @Serializable
    public static class initialGameMessage extends AbstractMessage {
        
        String msg;
        public initialGameMessage(String msg) {
            this.msg = msg;
        }
    }
    
    @Serializable
    public static class gameStartMessage extends AbstractMessage {
        
        String msg;
        public gameStartMessage(String msg) {
            this.msg = msg;
        }
    }
    
    @Serializable
    public static class updateDiskVelocityMessage extends AbstractMessage {
        
        //Need to send which disk in some way...
        Vector3f speed;
        
        public updateDiskVelocityMessage(Vector3f speed) {
            this.speed = speed;
        }
    }
    
    @Serializable
    public static class updateDiskPositionMessage extends AbstractMessage {
        
        //Need to send which disk in some way...
        float posX;
        float posY;
        
        public updateDiskPositionMessage(float posX, float posY) {
            this.posX = posX;
            this.posY = posY;
        }
    }
    
    @Serializable
    public static class updatePlayerScoreMessage extends AbstractMessage {
        
        int score;
        //Need to add which player this is somehow
        //Maybe just send player nickname here?
        
        public updatePlayerScoreMessage(int score) {
            this.score = score;
        }
    }
    
    @Serializable
    public static class updateTimeMessage extends AbstractMessage {
        
        float time;
        
        public updateTimeMessage(float time) {
            this.time = time;
        }
    }
    
    @Serializable
    public static class gameOverMessage extends AbstractMessage {
        
        String endMsg;
        /**
         * Add winning player, end scores for all players etc...
         * Send the end message
         */
        
        public gameOverMessage(String endMsg) {
            this.endMsg = endMsg;
        }
    }
}
