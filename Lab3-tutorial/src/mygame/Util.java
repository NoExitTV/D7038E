package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import com.jme3.network.serializing.Serializer;

/**
 * This is a utility class for use by the other classes. 
 *
 * A major feature is that the types of messages are defined in this class. 
 *
 * @author hj 
 */
public class Util {

    // server locations used by me to test the programs
    private static final String HOST3 = "127.0.0.1";
    private static final String HOST2 = "130.240.155.179";
    private static final String HOST1 = "10.0.1.2";

    public static final int PORT = 7006; // server port
    public static final String HOSTNAME = HOST3; // server location

    // register all message types there are
    public static void initialiseSerializables() {
        Serializer.registerClass(ChangeMessage.class);
        Serializer.registerClass(AckMessage.class);
        Serializer.registerClass(HeartMessage.class);
        Serializer.registerClass(HeartAckMessage.class);
    }

    static String getThreadName() {
        return Thread.currentThread().getName();
    }

    // used to print a trace of what the threads do
    static void print(String message) {
        System.out.format("%s: \n%s%n", getThreadName(), message);
    }

    // an intermediate class that contains some common declarations
    abstract public static class MyAbstractMessage extends AbstractMessage {

        protected int senderID;

        protected int messageID;
        protected static int globalCounter = 1000;

        public MyAbstractMessage() {
            this.messageID = globalCounter++; // default messageID
        }

        public int getSenderID() {
            return senderID;
        }

        public int getMessageID() {
            return messageID;
        }

    }

    /**
     * ChangeMessage is sent by a client to all other clients via the server.
     *
     * This message instructs all other clients to change the color of their box
     * to a random color.
     *
     * Receiving this message requires a client to send an AcklMessage back via
     * the server.
     */
    @Serializable
    public static class ChangeMessage extends MyAbstractMessage {

        public ChangeMessage() {
        }

        public ChangeMessage(int senderID) {
            this.senderID = senderID;
        }

    }

    /**
     * AckMessage is sent by a client that has gotten, and akted upon, a
     * ChangeMessage. It is sent to the client sending the ChangeMessage and via
     * the server.
     *
     * A client receiving this message do no need to do anything particular.
     */
    @Serializable
    public static class AckMessage extends MyAbstractMessage {

        protected int finalDestinationID;

        public AckMessage() {
        }

        public AckMessage(int finalDestinationID, int senderID, int messageID) {
            // ack sent to this destination
            this.finalDestinationID = finalDestinationID;
            // Ack sent from this senderID
            this.senderID = senderID;
            // Ack regards this messageID
            this.messageID = messageID;
        }

        public int finalDestinationID() {
            return finalDestinationID;
        }
    }

    /**
     * HeartMessage is sent by the server to all clients.
     *
     * A client that receives this message must respond back with a
     * HeartAckMessage.
     */
    @Serializable
    public static class HeartMessage extends MyAbstractMessage {

        public HeartMessage() {
        }
    }

    /**
     * HeartAckMessage is sent to the server by clients that receive a
     * HeartMessage.
     *
     * Nothing is required by the server upon receiving this message.
     */
    @Serializable
    public static class HeartAckMessage extends MyAbstractMessage {

        public HeartAckMessage() {
        }

        public HeartAckMessage(int senderID) {
            // Heartbeat ack sent from this senderID
            this.senderID = senderID;
        }
    }
}