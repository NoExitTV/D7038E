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
            Serializer.registerClass(NewWalkDirectionMsg.class);
            Serializer.registerClass(CreatePlayerMsg.class);
            Serializer.registerClass(AudioMsg.class);
            Serializer.registerClass(ClientLeaveMsg.class);
            Serializer.registerClass(CharacterJumpMsg.class);
            Serializer.registerClass(SyncWalkDirectionMsg.class);
            Serializer.registerClass(ResyncPositionsMsg.class);
            Serializer.registerClass(ResyncPlayerPositionMsg.class);
            Serializer.registerClass(ForcePlayerResyncMsg.class);
            Serializer.registerClass(SpawnTreasureMsg.class);
            Serializer.registerClass(RemoveTreasureMsg.class);
            Serializer.registerClass(CaptureTreasureMsg.class);
            Serializer.registerClass(SyncPointsMsg.class);
            Serializer.registerClass(GameEndMsg.class);
            Serializer.registerClass(GameStartMsg.class);
            Serializer.registerClass(ServerFullMsg.class);
    }
    
    
    // =======================================================================
    // CLIENT -> SERVER MESSAGES
    // =======================================================================
    
    @Serializable
    public static class CaptureTreasureMsg extends AbstractMessage {
        int playerId;
        
        public CaptureTreasureMsg(int playerId) {
            this.playerId = playerId;
        }
        
        public CaptureTreasureMsg() {
        }
    }
    
    /**
     * Send new walk direction to server
     */
    @Serializable
    public static class NewWalkDirectionMsg extends AbstractMessage {
        int playerId;
        float posX;
        float posY;
        float posZ;

        public NewWalkDirectionMsg(int playerId, float posX, float posY, float posZ) {
            this.playerId = playerId;
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
        }
        
        public NewWalkDirectionMsg() {
        }
    }
    
    @Serializable
    public static class CharacterJumpMsg extends AbstractMessage {
        int playerId;

        public CharacterJumpMsg(int playerId) {
            this.playerId = playerId;
        }
        
        public CharacterJumpMsg() {
        }
    }
    
    // =======================================================================
    // SERVER -> CLIENT MESSAGES
    // =======================================================================

    @Serializable
    public static class ServerFullMsg extends AbstractMessage {        
        public ServerFullMsg() {
        }
    }
    
    @Serializable
    public static class GameStartMsg extends AbstractMessage {        
        public GameStartMsg() {
        }
    }
    
    @Serializable
    public static class GameEndMsg extends AbstractMessage {
        int playerId;
        
        public GameEndMsg(int playerId) {
            this.playerId = playerId;
        }
        
        public GameEndMsg() {
        }
    }
    
    @Serializable
    public static class RemoveTreasureMsg extends AbstractMessage {
        int playerId;
        
        public RemoveTreasureMsg(int playerId) {
            this.playerId = playerId;
        }
        
        public RemoveTreasureMsg() {
        }
    }
    
    @Serializable
    public static class SpawnTreasureMsg extends AbstractMessage {
        
        float[] posArray;
        int points;
        
        public SpawnTreasureMsg(float[] posArray, int points) {
            this.posArray = posArray;
            this.points = points;
        }
        
        public SpawnTreasureMsg() {
        }
    }
    
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
        int points;
        
        public CreatePlayerMsg(int playerId, float posX, float posY, float posZ, int points) {
            this.playerId = playerId;
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
            this.points = points;
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
     
    @Serializable
    public static class ClientLeaveMsg extends AbstractMessage {
        int connId;

        public ClientLeaveMsg(int connId) {
            this.connId = connId;
            
        }
        
        public ClientLeaveMsg() {
        }
    }
    
    /**
     * Sync walk direction to all clients
     */
    @Serializable
    public static class SyncWalkDirectionMsg extends AbstractMessage {
        int playerId;
        float posX;
        float posY;
        float posZ;

        public SyncWalkDirectionMsg(int playerId, float posX, float posY, float posZ) {
            this.playerId = playerId;
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
        }
        
        public SyncWalkDirectionMsg() {
        }
    }
    
    /**
     * Resync all client positions
     */
    @Serializable
    public static class ResyncPositionsMsg extends AbstractMessage {
        int[] idArray;
        float[][] posArray;

        public ResyncPositionsMsg(int[] idArray, float[][] posArray) {
            this.idArray = idArray;
            this.posArray = posArray;
        }
        
        public ResyncPositionsMsg() {
        }
    }
    
    /**
     * Resync one client position
     */
    @Serializable
    public static class ResyncPlayerPositionMsg extends AbstractMessage {
        int playerId;
        float posX;
        float posY;
        float posZ;

        public ResyncPlayerPositionMsg(int playerId, float posX, float posY, float posZ) {
            this.playerId = playerId;
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
        }
        
        public ResyncPlayerPositionMsg() {
        }
    }
    
    /**
     * Force Resync of one client position.
     * This happens when a player falls outside of the map and server teleports player etc.
     */
    @Serializable
    public static class ForcePlayerResyncMsg extends AbstractMessage {
        int playerId;
        float posX;
        float posY;
        float posZ;

        public ForcePlayerResyncMsg(int playerId, float posX, float posY, float posZ) {
            this.playerId = playerId;
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
        }
        
        public ForcePlayerResyncMsg() {
        }
    }
    
    @Serializable
    public static class SyncPointsMsg extends AbstractMessage {
        int[][] pointArray;
        
        public SyncPointsMsg(int[][] pointArray) {
            this.pointArray = pointArray;
        }
        
        public SyncPointsMsg() {
            
        }
    }
}
