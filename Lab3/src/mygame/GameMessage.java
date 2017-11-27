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
        //Serializer.registerClass(GameMessage.class);
        Serializer.registerClass(ClientConnectMessage.class);
        Serializer.registerClass(ClientLeaveMessage.class);
        Serializer.registerClass(ClientVelocityUpdateMessage.class);
        Serializer.registerClass(ServerWelcomeMessage.class);
        Serializer.registerClass(NameConflictMessage.class);
        Serializer.registerClass(GameInProgressMessage.class);
        Serializer.registerClass(InitialGameMessage.class);
        Serializer.registerClass(GameStartMessage.class);
        Serializer.registerClass(UpdateDiskVelocityMessage.class);
        Serializer.registerClass(UpdateDiskPositionMessage.class);
        Serializer.registerClass(UpdatePlayerScoreMessage.class);
        Serializer.registerClass(UpdateTimeMessage.class);
        Serializer.registerClass(GameOverMessage.class);
        Serializer.registerClass(HeartBeatAckMessage.class);
        Serializer.registerClass(HeartBeatMessage.class);
        Serializer.registerClass(AckMessage.class);
    }
    
    private abstract class message extends AbstractMessage {
        
    }
    
    // =======================================================================
    // CLIENT -> SERVER MESSAGES
    // =======================================================================
    
    @Serializable
    public static class ClientConnectMessage extends AbstractMessage {
        
        String nickname;
        TheClient client;
        
        public ClientConnectMessage() {
            //this.nickname = n;
            //this.client = client;
        }
    }
    
    @Serializable
    public static class ClientLeaveMessage extends AbstractMessage {
        
        String nickname;
        TheClient client;
        
        public ClientLeaveMessage() {
            //this.nickname = n;
            //this.client = client;
        }
    }
    
    @Serializable
    public static class ClientVelocityUpdateMessage extends AbstractMessage {
        
        /**
         * Send who you are to the server
         * and the new velocity
         */
        public Vector3f speed;
        public int playerID;
        public float posX;
        public float posY;
        
        public ClientVelocityUpdateMessage() {  
        }
        
        public ClientVelocityUpdateMessage(Vector3f speed, int playerID, float posX, float posY) {  
            this.speed = speed;
            this.playerID = playerID;
            this.posX = posX;
            this.posY = posY;
        }
    }
    
    @Serializable
    public static class HeartBeatAckMessage extends AbstractMessage {
        
        /**
         * Send who you are to the server
         */
        
        public HeartBeatAckMessage() {
       
        }
    }
    
    @Serializable
    public static class AckMessage extends AbstractMessage {
        
        /**
         * Send who you are to the server
         */
        
        public AckMessage() {
       
        }
    }
    
    
    
    // =======================================================================
    // SERVER -> CLIENT MESSAGES
    // =======================================================================
    
    @Serializable
    public static class HeartBeatMessage extends AbstractMessage {
        
        /**
         * Send who you are to the server
         */
        
        public HeartBeatMessage() {
       
        }
    }
    
    /**
     * This message is sent to one client only
     */
    @Serializable
    public static class ServerWelcomeMessage extends AbstractMessage {
        
        String msg;
        public ServerWelcomeMessage(String msg) {
            this.msg = msg;
        }
        
        public ServerWelcomeMessage() {
        }
    }
    
    /**
     * This message is sent to one client only
     */
    @Serializable
    public static class NameConflictMessage extends AbstractMessage {
        
        String msg;
        public NameConflictMessage(String msg) {
            this.msg = msg;
        }
        public NameConflictMessage() {
        }
        
    }
    
    @Serializable
    public static class GameInProgressMessage extends AbstractMessage {
        
        String msg;
        public GameInProgressMessage(String msg) {
            this.msg = msg;
        }
        public GameInProgressMessage() {
        }
    }
    
    @Serializable
    public static class InitialGameMessage extends AbstractMessage {
        
        String msg;
        public InitialGameMessage(String msg) {
            this.msg = msg;
        }
        public InitialGameMessage() {
        }
    }
    
    @Serializable
    public static class GameStartMessage extends AbstractMessage {
        
        String msg;
        public GameStartMessage(String msg) {
            this.msg = msg;
        }
        public GameStartMessage() {
        }
    }
    
    @Serializable
    public static class UpdateDiskVelocityMessage extends AbstractMessage {
        
        //Need to send which disk in some way...
        Vector3f speed;
        
        public UpdateDiskVelocityMessage(Vector3f speed) {
            this.speed = speed;
        }
        public UpdateDiskVelocityMessage() {
        }
    }
    
    @Serializable
    public static class UpdateDiskPositionMessage extends AbstractMessage {
        
        //Need to send which disk in some way...
        float posX;
        float posY;
        
        public UpdateDiskPositionMessage(float posX, float posY) {
            this.posX = posX;
            this.posY = posY;
        }
        public UpdateDiskPositionMessage() {
        }
    }
    
    @Serializable
    public static class UpdatePlayerScoreMessage extends AbstractMessage {
        
        int score;
        //Need to add which player this is somehow
        //Maybe just send player nickname here?
        
        public UpdatePlayerScoreMessage(int score) {
            this.score = score;
        }
        public UpdatePlayerScoreMessage() {
        }
    }
    
    @Serializable
    public static class UpdateTimeMessage extends AbstractMessage {
        
        float time;
        
        public UpdateTimeMessage(float time) {
            this.time = time;
        }
        public UpdateTimeMessage() {
        }
    }
    
    @Serializable
    public static class GameOverMessage extends AbstractMessage {
        
        String endMsg;
        /**
         * Add winning player, end scores for all players etc...
         * Send the end message
         */
        
        public GameOverMessage(String endMsg) {
            this.endMsg = endMsg;
        }
        public GameOverMessage() {
        }
    }
}
