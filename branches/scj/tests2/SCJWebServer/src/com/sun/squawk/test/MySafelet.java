/*
 * Copyright (c) 2006 Sun Microsystems, Inc.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to 
 * deal in the Software without restriction, including without limitation the 
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or 
 * sell copies of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 **/

/*
 * eWebServer.java
 *
 * Simple web server for the eSPOT
 *
 * author: Vipul Gupta
 * date: Sep 11, 2006 
 * updated: Jul 10, 2008
 */
package com.sun.squawk.test;

import javax.safetycritical.MissionSequencer;
import javax.safetycritical.Safelet;

import com.sun.squawk.BackingStore;

public class MySafelet implements Safelet {

    public MissionSequencer getSequencer() {
        return new MySequencer(Config.priority, Config.storage);
    }

    public void setUp() {
        System.out.println("[WebServer] Safelet setUp ... ");
        // This is NOT public API. Use it ONLY when you don't want to make your
        // screen full of illegal assignment warning and really want to see some
        // useful messages instead during the run.
        BackingStore.disableScopeCheck();
    }

    public void tearDown() {
        System.out.println("[WebServer] Safelet tearDown ... ");
    }
}
