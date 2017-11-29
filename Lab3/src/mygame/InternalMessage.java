/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.network.AbstractMessage;
import com.jme3.network.Filter;

/**
 *
 * @author NoExit
 */
public class InternalMessage {
    Filter filter;
    AbstractMessage m;
        
        public InternalMessage(Filter filter, AbstractMessage m) {
            this.filter = filter;
            this.m = m;
        }    
}
