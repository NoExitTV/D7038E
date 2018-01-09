/**
 *
 * @author Fredrik Pettersson & Carl Borngrund
 */
package mygame;


public class Util {

    // server locations used by me to test the programs
    //public static final String SERVER = "192.168.137.210";
    public static final String SERVER = "127.0.0.1";

  
    // Number of players allowed in the world
    public static final int PLAYERS = 3;

    public static final int PORT = 10001; // server port
    public static final String HOSTNAME = SERVER; // server location
    
    static String getThreadName() {
        return Thread.currentThread().getName();
    }
    
    // used to print a trace of what the threads do
    static void print(String message) {
        System.out.format("%s: \n%s%n", getThreadName(), message);
    }
    
}
