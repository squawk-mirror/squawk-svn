/*
 * Copyright 2004-2008 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */


package com.sun.squawk.platform.posix;

import com.sun.squawk.platform.SystemEvents;
import com.sun.squawk.VM;
import com.sun.squawk.VMThread;
import com.sun.cldc.jna.*;
import com.sun.squawk.platform.posix.natives.*;
import com.sun.squawk.util.Assert;
import com.sun.squawk.util.IntSet;

/**
 *
 * @author dw29446
 */
public class SystemEventsImpl extends SystemEvents implements Runnable {
    private final static boolean DEBUG = false;

    /* We need 3 copies of file descriptor sets:
          - An array of ints (as an IntSet)
          - the master FD_SET (as a native bitmap),
          - a tmp FD_SET, because select bashes the set passed in.
     */
    Pointer masterReadSet;
    Pointer masterWriteSet;
    private Pointer tempReadSet;
    private Pointer tempWriteSet;
    private IntSet readSet;
    private IntSet writeSet;
    
    private Time.timeval zeroTime;
    private Time.timeval timeoutTime;

    private int maxFD = 0; // system-wide highwater mark....

    private BlockingFunction selectPtr;
    private Function cancelSelectPtr;
    protected Select select;

    /*----------------- FD SETS ----------------- */
    
    private int copyIntoFDSet(IntSet src, Pointer fd_set) {
//System.err.println("Copying from " + src + " to " + fd_set);
        int num = src.size();
        int[] data = src.getElements();
        int localMax = 0;
        FD_ZERO(fd_set);
        for (int i = 0; i < num; i++) {
            int fd = data[i];
            Assert.that(fd > 0);
            select.FD_SET(fd, fd_set);
            if (fd > localMax) {
                localMax = fd;
            }
        }
        return localMax;
    }

    /**
     * initializes a descriptor set fdset to the null set
     * @param fd_set
     */
    public static void FD_ZERO(Pointer fd_set) {
        fd_set.clear(Select.fd_set_SIZEOF);
    }

    /**
     * replaces an already allocated fdset_copy file descriptor set with a copy of fdset_orig.
     *
     * @param fdset_orig
     * @param fdset_copy
     */
    public static void FD_COPY(Pointer fdset_orig, Pointer fdset_copy) {
//        System.err.println("FD_COPY from: " + fdset_orig + " to: " + fdset_copy + " (size = " + FD_SIZE + ")");
        Pointer.copyBytes(fdset_orig, 0, fdset_copy, 0, Select.fd_set_SIZEOF);
    }

    /**
     * Allocate a new fd_struct in c memory.
     * @return pointer to new memory
     */
    public static Pointer FD_ALLOCATE() {
        return new Pointer(Select.fd_set_SIZEOF);
    }

    /**
     * Set up the temp fd_set based on the master set and the IntSet.
     *
     * @param set the IntSet
     * @param master the master fd_set
     * @param temp the temp fd_set
     */
    private void setupTempSet(IntSet set, Pointer master, Pointer temp) {
         if (set.size() != 0) {
            FD_COPY(master, temp);
        } else {
            FD_ZERO(temp);
        }
    }

    /**
     * Print the FDs taht are set in fd_set
     *
     * @param fd_set the set of file descriptors in native format.
     */
    private void printFDSet(Pointer fd_set) {
        for (int i = 0; i < maxFD + 1; i++) {
            if (select.FD_ISSET(i, fd_set)) {
                VM.print("    fd: ");
                VM.print(i);
                VM.println();
            }
        }
    }

    public SystemEventsImpl() {
        select = Select.INSTANCE;
        NativeLibrary jnaNativeLibrary = NativeLibrary.getDefaultInstance();
        selectPtr = jnaNativeLibrary.getBlockingFunction("squawk_select");
        selectPtr.setTaskExecutor(selectRunner);

        cancelSelectPtr = jnaNativeLibrary.getFunction("cancel_squawk_select");

        masterReadSet = FD_ALLOCATE();
        masterWriteSet = FD_ALLOCATE();

        readSet = new IntSet();
        writeSet = new IntSet();
        tempReadSet = FD_ALLOCATE();
        tempWriteSet = FD_ALLOCATE();

        zeroTime = new Time.timeval();
        zeroTime.tv_sec = 0;
        zeroTime.tv_usec = 0;
        zeroTime.allocateMemory();
        zeroTime.write();

        timeoutTime = new Time.timeval();
        timeoutTime.allocateMemory();
    }

    /**
     * Blocking call to select until IO occurs, the timeout occurs, or the read/write sets need to be updated
     * (see updateSets() ??)
     * @param theTimout
     * @return number of file descriptors that have events
     */
    private int select(int nfds, Pointer readSet, Pointer writeSet, Pointer excSet, Pointer theTimout) {
        return selectPtr.call5(nfds, readSet, writeSet, excSet, theTimout);
    }

    /**
     * Non-blocking call to cancel the blocking select call.
     * Call when the read/write sets have been updated
     */
    private int cancelSelectCall() {
        return cancelSelectPtr.call0();
    }

    /**
     * If any any events have occurred in the waiting set, tell the
     * thread scheduler to make the waiting java thread runnable.
     *
     * @param num number of events to be processed
     * @param waitingSet waiting set as an IntSet
     * @param eventFDSet waiting set as a FD_SET
     * @return remaining number of events to be processed
     */
    private int handleEvents(int num, IntSet waitingSet, Pointer eventFDSet) {
        if (num > 0) {
            for (int i = 0; i < waitingSet.size(); i++) {
                int fd = waitingSet.getElements()[i];
                if (select.FD_ISSET(fd, eventFDSet)) {
                    waitingSet.remove(fd);        // shrink waitingSet
                    VMThread.signalOSEvent(fd);
                    num--;
                    if (num == 0) {
                        break; // no more events
                    }
                    i--; // recheck location i
                }
            }
        }
        return num;
    }

    /**
     * Poll the OS to see if there have been any events on the requested fds.
     * 
     * Try not to allocate if there are no events...
     * @param timeout  md to wait, or 0 for no wait, or Long.MAX_VALUE for inifinite wait
     */
    public void waitForEvents(long timeout) {
        long elapsedTime = System.currentTimeMillis();
        if (timeout != 0) {
            synchronized (this) {
                try {
                    while (maxFD <= 0) {
                        wait((timeout == Long.MAX_VALUE) ? 0 : timeout);
                    }
                } catch (InterruptedException ex) {
                }
            }
            elapsedTime = System.currentTimeMillis() - elapsedTime;
            if (timeout != Long.MAX_VALUE) {
                timeout -= elapsedTime;
            }
        }

        if (maxFD <= 0) {
            return;
        }
        // TODO: reset the cancelSelectCall()  - ie drain the pipe.
        
        setupTempSet(readSet, masterReadSet, tempReadSet);
        setupTempSet(writeSet, masterWriteSet, tempWriteSet);

        Pointer theTimout;
        if (timeout == 0) {
            if (DEBUG) { VM.println("WARNING: Why are we polling select??? -----------------------------------------"); }
            theTimout = zeroTime.getPointer();
        } else if (timeout == Long.MAX_VALUE) {
            theTimout = Pointer.NULL();
        } else {
            if (DEBUG) { VM.println("WARNING: Why are we slow polling select??? -----------------------------------------"); }

            timeoutTime.tv_sec = timeout / 1000;
            timeoutTime.tv_usec = (timeout % 1000) * 1000;
            timeoutTime.write();
            theTimout = timeoutTime.getPointer();
        }

//      if (readSet.size() != 0) {
//          VM.println("Read FDs set:");
//          printFDSet(tempReadSet);
//      }
//      if (writeSet.size() != 0) {
//          VM.println("Write FDs set:");
//          printFDSet(tempWriteSet);
//      }

        int num = select(maxFD + 1, tempReadSet, tempWriteSet, Pointer.NULL(), theTimout); /* block waiting for event or timeout */
        // TODO : do non-blocking call if timeout == 0
        
        if (num > 0) {
            num = handleEvents(num, readSet, tempReadSet);
            num = handleEvents(num, writeSet, tempWriteSet);
            if (num != 0 && DEBUG) {
                System.err.println("Missed handling a select event?\n num: " + num + "\nreadSize: " + readSet.size() + ", Read FDs set:");
                printFDSet(tempReadSet);
                System.err.println("writeSize: " + writeSet.size() + ", Write FDs set:");
                printFDSet(tempWriteSet);
            }
            updateSets();
        } else if (num < 0) {
            System.err.println("select error: " + LibCUtil.errno());
        } else {
            if (DEBUG) { VM.println("in waitForEvents(), select cancelled"); }
        }
    }

    /**
     * Update bit masks from IntSets, and update maxFD;
     */
    private void updateSets() {
        maxFD = copyIntoFDSet(readSet, masterReadSet);
        int mfd = copyIntoFDSet(writeSet, masterWriteSet);
        if (mfd > maxFD) {
            maxFD = mfd;
        }
    }
    
    public void waitForReadEvent(int fd) {
        if (DEBUG) { VM.println("Waiting for read on fd: " + fd); }
        Assert.always(fd >= 0 && fd < Select.FD_SETSIZE);
        synchronized (this) {
            readSet.add(fd);
            updateSets();
            notifyAll();
        }
        cancelSelectCall();
        VMThread.waitForOSEvent(fd); // read is ready, select will remove fd from readSet
    }
    
    public void waitForWriteEvent(int fd) {
        if (DEBUG) { VM.println("Waiting for write on fd: " + fd); }
        Assert.always(fd >= 0 && fd < Select.FD_SETSIZE);
        synchronized (this) {
            writeSet.add(fd);
            updateSets();
            notifyAll();
        }
        cancelSelectCall();
        VMThread.waitForOSEvent(fd);// write is ready, select will remove fd from writeSet
    }

}
