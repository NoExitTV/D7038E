/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

/**
 *
 * @author Fredrik Pettersson
 */
public class Util {

    // server locations used by me to test the programs
    public static final String SERVER = "127.0.0.1";
    private static final String HOST3 = "127.0.0.1";
    private static final String HOST2 = "130.240.155.179";
    private static final String HOST1 = "10.0.1.2";

    public static final int PORT = 10001; // server port
    public static final String HOSTNAME = HOST3; // server location
    
    static String getThreadName() {
        return Thread.currentThread().getName();
    }
    
    // used to print a trace of what the threads do
    static void print(String message) {
        System.out.format("%s: \n%s%n", getThreadName(), message);
    }
    
}
