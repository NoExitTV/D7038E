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
    }
    
    
    // =======================================================================
    // CLIENT -> SERVER MESSAGES
    // =======================================================================
    
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
}
