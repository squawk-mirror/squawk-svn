/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.squawk.platform.posix.callouts;

import com.sun.cldc.jna.*;

/**
 * java wrapper around #include <sys/select.h>
 * 
 */
public class Select {
    /** 
     * The size of an fd_set in bytes
     */
    public static final int FD_SIZE = VarPointer.lookup("sysFD_SIZE", 4).getInt();
    
    private static final Function selectPtr = Function.getFunction("select");    
    private static final Function sysFD_SETPtr = Function.getFunction("sysFD_SET");
    private static final Function sysFD_CLRPtr = Function.getFunction("sysFD_CLR");
    private static final Function sysFD_ISSETPtr = Function.getFunction("sysFD_ISSET");

    /* pure static class */
    private Select() {}
    
    /**
     * Select() examines the I/O descriptor sets whose addresses are passed in readfds, writefds,
     * and errorfds to see if some of their descriptors are ready for reading, are ready for writ-
     * ing, or have an exceptional condition pending, respectively.
     * 
     * On return, select() replaces the given descriptor sets
     * with subsets consisting of those descriptors that are ready for the requested operation.
     * 
     * @param nfds The first nfds descriptors are checked in each set
     * @param readfds
     * @param writefds
     * @param errorfds
     * @param timeout if timout is nill, wait forever, if a pointer to a zero'd timeval, then does NOT wait.
     * @return the total number of ready descriptors in all the sets
     */
     public static int select(int nfds, Pointer readfds, Pointer writefds,
                              Pointer errorfds, Pointer timeout) {
         return selectPtr.call5(nfds, 
                    readfds,
                    writefds,
                    errorfds, 
                    timeout);
     }

    /**
     * removes fd from fdset
     * 
     * @param fd
     * @param fd_set
     */
    public static void FD_CLR(int fd, Pointer fd_set) {
        sysFD_CLRPtr.call2(fd, fd_set);
    }
    
    /**
     * includes a particular descriptor fd in fdset.
     * 
     * @param fd
     * @param fd_set
     */
    public static void FD_SET(int fd, Pointer fd_set) {
        sysFD_SETPtr.call2(fd, fd_set);
    }
    
    /**
     * is non-zero if fd is a member of fd_set, zero otherwise.
     * @param fd
     * @param fd_set
     * @return
     */
    public static boolean FD_ISSET(int fd, Pointer fd_set) {
        int result = sysFD_ISSETPtr.call2(fd, fd_set);
        return (result == 0) ? false : true;
    }
    
    /**
     * initializes a descriptor set fdset to the null set
     * @param fd_set
     */
    public static void FD_ZERO(Pointer fd_set) {
        fd_set.clear(FD_SIZE);
    }
    
    /**
     * replaces an already allocated fdset_copy file descriptor set with a copy of fdset_orig.
     * 
     * @param fdset_orig
     * @param fdset_copy
     */
    public static void FD_COPY(Pointer fdset_orig, Pointer fdset_copy) {
//        System.err.println("FD_COPY from: " + fdset_orig + " to: " + fdset_copy + " (size = " + FD_SIZE + ")");
        Pointer.copyBytes(fdset_orig, 0, fdset_copy, 0, FD_SIZE);
    }
    
    /**
     * Allocate a new fd_struct in c memory.
     * @return pointer to new memory
     */
    public static Pointer FD_ALLOCATE() {
        return new Pointer(FD_SIZE);
    }
    
    

}
