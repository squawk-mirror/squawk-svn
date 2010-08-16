package org.sunspotworld.demo;

import java.util.Hashtable;
import java.util.Enumeration;


public class Properties extends Hashtable {
    
    public String getProperty(String key) {
        return (String)get(key);
    }
    
    public Enumeration propertyNames() {
        return keys();
    }
    
}

