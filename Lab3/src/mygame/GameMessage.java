package mygame;

import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import com.jme3.network.serializing.Serializer;
import java.util.ArrayList;

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
        Serializer.registerClass(SendInitPlayerDisk.class);
        Serializer.registerClass(SendInitNegativeDisk.class);
        Serializer.registerClass(SendInitPositiveDisk.class);
        Serializer.registerClass(restartGameMessage.class);
        Serializer.registerClass(PlayerAccelerationUpdate.class);
        Serializer.registerClass(UpdateDiskPosAndVelMessage.class);
        Serializer.registerClass(CollisionMessage.class);
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
    public static class PlayerAccelerationUpdate extends AbstractMessage {
        
        int playerId;
        String direction;
        float tpf;
        
        public PlayerAccelerationUpdate(int playerId, String direction, float tpf) {
            this.playerId = playerId;
            this.direction = direction;
            this.tpf = tpf;
        }
        public PlayerAccelerationUpdate() {
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
    
    @Serializable
    public static class restartGameMessage extends AbstractMessage {
        
        int playerId;
        public restartGameMessage(int playerId) {
            this.playerId = playerId;
        }
        
        public restartGameMessage() {
       
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
        public int playerID;
        public ServerWelcomeMessage(String msg, int playerID) {
            this.msg = msg;
            this.playerID = playerID;
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
        
        ArrayList<float[]> playerInfo;
        ArrayList<Vector3f> diskVectors;
        
        public InitialGameMessage(ArrayList<float[]> playerInfo, ArrayList<Vector3f> diskVectors) {
            this.playerInfo = playerInfo;
            this.diskVectors = diskVectors;
        }

        public InitialGameMessage() {
        }
    }
    
    @Serializable
    public static class SendInitPlayerDisk extends AbstractMessage {
        
        //Need to send which disk in some way...
        float posX;
        float posY;
        int playerID;
        
        public SendInitPlayerDisk(int playerID, float posX, float posY) {
            this.posX = posX;
            this.posY = posY;
            this.playerID = playerID;
        }
        
        public SendInitPlayerDisk() {
        }
    }
    
    @Serializable
    public static class SendInitPositiveDisk extends AbstractMessage {
        
        //Need to send which disk in some way...
        float posX;
        float posY;
        float speedX;
        float speedY;
        int diskID;
        
        public SendInitPositiveDisk(int diskID, float posX, float posY, float speedX, float speedY) {
            this.posX = posX;
            this.posY = posY;
            this.speedX = speedX;
            this.speedY = speedY;
            this.diskID = diskID;
        }
        
        public SendInitPositiveDisk() {
        } 
    }
    
    @Serializable
    public static class SendInitNegativeDisk extends AbstractMessage {
        
        //Need to send which disk in some way...
        float posX;
        float posY;
        float speedX;
        float speedY;
        int diskID;
        
        public SendInitNegativeDisk(int diskID, float posX, float posY, float speedX, float speedY) {
            this.posX = posX;
            this.posY = posY;
            this.speedX = speedX;
            this.speedY = speedY;
            this.diskID = diskID;
        }
        
        public SendInitNegativeDisk() {
        } 
    }
    
    @Serializable
    public static class GameStartMessage extends AbstractMessage {
        public GameStartMessage() {
        }
    }
    
    @Serializable
    public static class CollisionMessage extends AbstractMessage {
        
        int disk1;
        int disk2;
        Vector3f speed1;
        Vector3f speed2;
        float posX1;
        float posX2;
        float posY1;
        float posY2;
        
        public CollisionMessage(int disk1, Vector3f speed1, float posX1, float posY1, int disk2, Vector3f speed2, float posX2, float posY2) {
            this.disk1   = disk1;
            this.disk2   = disk2;
            this.speed1  = speed1;
            this.speed2  = speed2;
            this.posX1   = posX1;
            this.posX2   = posX2;
            this.posY1   = posY1;
            this.posY2   = posY2;
        }
        public CollisionMessage() {
        }
    }
    
    @Serializable
    public static class UpdateDiskPosAndVelMessage extends AbstractMessage {
        
        int diskId;
        Vector3f speed;
        float posX;
        float posY;
        
        public UpdateDiskPosAndVelMessage(int diskId, Vector3f speed, float posX, float posY) {
            this.diskId = diskId;
            this.speed  = speed;
            this.posX   = posX;
            this.posY   = posY;
        }
        public UpdateDiskPosAndVelMessage() {
        }
    }
    
    @Serializable
    public static class UpdateDiskVelocityMessage extends AbstractMessage {
        
        int diskId;
        Vector3f speed;
        
        public UpdateDiskVelocityMessage(Vector3f speed, int diskId) {
            this.diskId = diskId;
            this.speed = speed;
        }
        public UpdateDiskVelocityMessage() {
        }
    }
    
    @Serializable
    public static class UpdateDiskPositionMessage extends AbstractMessage {
        
        int diskId;
        float posX;
        float posY;
        
        public UpdateDiskPositionMessage(float posX, float posY, int diskId) {
            this.diskId = diskId;
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
