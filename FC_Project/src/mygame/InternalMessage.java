/**
 *
 * @author Fredrik Pettersson & Carl Borngrund
 */
package mygame;

import com.jme3.network.AbstractMessage;
import com.jme3.network.Filter;


public class InternalMessage {
    Filter filter;
    AbstractMessage m;
        
        public InternalMessage(Filter filter, AbstractMessage m) {
            this.filter = filter;
            this.m = m;
        }    
}
