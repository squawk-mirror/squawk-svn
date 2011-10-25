//if[!AUTOGEN_JNA_NATIVES]
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


/* **** GENERATED FILE -- DO NOT EDIT ****
 *      This is a CLDC/JNA Interface class definition
 *      generated by com.sun.cldc.jna.JNAGen
 *      from the CLDC/JNA Interface class declaration in ./cldc-native-declarations/src/com/sun/squawk/platform/posix/natives/LibC.java
 */
package com.sun.squawk.platform.windows.natives;

import com.sun.cldc.jna.*;

/**
 *
 * Import common functions variables and constants from libc.
 */
/*@Includes({"<errno.h>", "<fcntl.h>", "<sys/stat.h>"})*/
public interface LibC extends Library {

    LibC INSTANCE = (LibC)
            Native.loadLibrary("RTLD",
                               LibC.class);

    int 

        EPERM = LibCImpl.EPERM,		/* Operation not permitted */

        ENOENT = LibCImpl.ENOENT,		/* No such file or directory */

        ESRCH = LibCImpl.ESRCH,		/* No such process */

        EINTR = LibCImpl.EINTR,		/* Interrupted system call */

        EIO = LibCImpl.EIO,		/* Input/output error */

        ENXIO = LibCImpl.ENXIO,		/* Device not configured */

        E2BIG = LibCImpl.E2BIG,		/* Argument list too long */

        ENOEXEC = LibCImpl.ENOEXEC,		/* Exec format error */

        EBADF = LibCImpl.EBADF,		/* Bad file descriptor */

        ECHILD = LibCImpl.ECHILD,		/* No child processes */

        EDEADLK = LibCImpl.EDEADLK,		/* Resource deadlock avoided */
        /* 11 was EAGAIN */

        ENOMEM = LibCImpl.ENOMEM,		/* Cannot allocate memory */

        EACCES = LibCImpl.EACCES,		/* Permission denied */

        EFAULT = LibCImpl.EFAULT,		/* Bad address */

        EBUSY = LibCImpl.EBUSY,		/* Device busy */

        EEXIST = LibCImpl.EEXIST,		/* File exists */

        EXDEV = LibCImpl.EXDEV,		/* Cross-device link */

        ENODEV = LibCImpl.ENODEV,		/* Operation not supported by device */

        ENOTDIR = LibCImpl.ENOTDIR,		/* Not a directory */

        EISDIR = LibCImpl.EISDIR,		/* Is a directory */

        EINVAL = LibCImpl.EINVAL,		/* Invalid argument */

        ENFILE = LibCImpl.ENFILE,		/* Too many open files in system */

        EMFILE = LibCImpl.EMFILE,		/* Too many open files */

        ENOTTY = LibCImpl.ENOTTY,		/* Inappropriate ioctl for device */

        ETXTBSY = LibCImpl.ETXTBSY,		/* Text file busy */

        EFBIG = LibCImpl.EFBIG,		/* File too large */

        ENOSPC = LibCImpl.ENOSPC,		/* No space left on device */

        ESPIPE = LibCImpl.ESPIPE,		/* Illegal seek */

        EROFS = LibCImpl.EROFS,		/* Read-only file system */

        EMLINK = LibCImpl.EMLINK,		/* Too many links */

        EPIPE = LibCImpl.EPIPE,		/* Broken pipe */

        /* math software */
        EDOM = LibCImpl.EDOM,		/* Numerical argument out of domain */

        ERANGE = LibCImpl.ERANGE,		/* Result too large */

        /* non-blocking and interrupt i/o */
        EAGAIN = LibCImpl.EAGAIN,		/* Resource temporarily unavailable */

        EWOULDBLOCK = LibCImpl.EWOULDBLOCK,		/* Operation would block */

        EINPROGRESS = LibCImpl.EINPROGRESS,		/* Operation now in progress */

        EALREADY = LibCImpl.EALREADY,		/* Operation already in progress */

        /* ipc/network software -- argument errors */
        ENOTSOCK = LibCImpl.ENOTSOCK,		/* Socket operation on non-socket */

        EDESTADDRREQ = LibCImpl.EDESTADDRREQ,		/* Destination address required */

        EMSGSIZE = LibCImpl.EMSGSIZE,		/* Message too long */

        EPROTOTYPE = LibCImpl.EPROTOTYPE,		/* Protocol wrong type for socket */

        ENOPROTOOPT = LibCImpl.ENOPROTOOPT,		/* Protocol not available */

        EPROTONOSUPPORT = LibCImpl.EPROTONOSUPPORT,		/* Protocol not supported */

        ENOTSUP = LibCImpl.ENOTSUP,		/* Operation not supported */

        EAFNOSUPPORT = LibCImpl.EAFNOSUPPORT,		/* Address family not supported by protocol family */

        EADDRINUSE = LibCImpl.EADDRINUSE,		/* Address already in use */

        EADDRNOTAVAIL = LibCImpl.EADDRNOTAVAIL,		/* Can't assign requested address */

        /* ipc/network software -- operational errors */
        ENETDOWN = LibCImpl.ENETDOWN,		/* Network is down */

        ENETUNREACH = LibCImpl.ENETUNREACH,		/* Network is unreachable */

        ENETRESET = LibCImpl.ENETRESET,		/* Network dropped connection on reset */

        ECONNABORTED = LibCImpl.ECONNABORTED,		/* Software caused connection abort */

        ECONNRESET = LibCImpl.ECONNRESET,		/* Connection reset by peer */

        ENOBUFS = LibCImpl.ENOBUFS,		/* No buffer space available */

        EISCONN = LibCImpl.EISCONN,		/* Socket is already connected */

        ENOTCONN = LibCImpl.ENOTCONN,		/* Socket is not connected */

        ETIMEDOUT = LibCImpl.ETIMEDOUT,		/* Operation timed out */

        ECONNREFUSED = LibCImpl.ECONNREFUSED,		/* Connection refused */

        ELOOP = LibCImpl.ELOOP,	/* Too many levels of symbolic links */

        ENAMETOOLONG = LibCImpl.ENAMETOOLONG,		/* File name too long */

        /* should be rearranged */
        EHOSTUNREACH = LibCImpl.EHOSTUNREACH,		/* No route to host */

        ENOTEMPTY = LibCImpl.ENOTEMPTY,		/* Directory not empty */

        /* quotas & mush */
        EDQUOT = LibCImpl.EDQUOT,		/* Disc quota exceeded */

        ENOLCK = LibCImpl.ENOLCK,		/* No locks available */

        ENOSYS = LibCImpl.ENOSYS,		/* Function not implemented */

        EOVERFLOW = LibCImpl.EOVERFLOW,		/* Value too large to be stored in data type */

        ECANCELED = LibCImpl.ECANCELED,		/* Operation canceled */

        EIDRM = LibCImpl.EIDRM,		/* Identifier removed */

        ENOMSG = LibCImpl.ENOMSG,		/* No message of desired type */

        EILSEQ = LibCImpl.EILSEQ,		/* Illegal byte sequence */

        EBADMSG = LibCImpl.EBADMSG,		/* Bad message */

        EMULTIHOP = LibCImpl.EMULTIHOP,		/* Reserved */

        ENODATA = LibCImpl.ENODATA,		/* No message available on STREAM */

        ENOLINK = LibCImpl.ENOLINK,		/* Reserved */

        ENOSR = LibCImpl.ENOSR,		/* No STREAM resources */

        ENOSTR = LibCImpl.ENOSTR,		/* Not a STREAM */

        EPROTO = LibCImpl.EPROTO,		/* Protocol error */

        ETIME = LibCImpl.ETIME,		/* STREAM ioctl timeout */


        /* command values */
        F_DUPFD		= LibCImpl.F_DUPFD,		/* duplicate file descriptor */
        F_GETFD		= LibCImpl.F_GETFD,		/* get file descriptor flags */
        F_SETFD		= LibCImpl.F_SETFD,		/* set file descriptor flags */
        F_GETFL		= LibCImpl.F_GETFL,	/* get file status flags */
        F_SETFL		= LibCImpl.F_SETFL,		/* set file status flags */

        /*
         * File status flags: these are used by open(2), fcntl(2).
         * They are also used (indirectly) in the kernel file structure f_flags,
         * which is a superset of the open/fcntl flags.  Open flags and f_flags
         * are inter-convertible using OFLAGS(fflags) and FFLAGS(oflags).
         * Open/fcntl flags begin with O_; kernel-internal flags begin with F.
         */
        /* open-only flags */
            O_RDONLY	= LibCImpl.O_RDONLY,		/* open for reading only */
            O_WRONLY	= LibCImpl.O_WRONLY,		/* open for writing only */
            O_RDWR		= LibCImpl.O_RDWR,		/* open for reading and writing */
            O_ACCMODE	= LibCImpl.O_ACCMODE,		/* mask for above modes */

            O_NONBLOCK	= LibCImpl.O_NONBLOCK,		/* no delay */
            O_APPEND	= LibCImpl.O_APPEND,		/* set append mode */
            O_SYNC		= LibCImpl.O_SYNC,		/* synchronous writes */
            O_CREAT		= LibCImpl.O_CREAT,		/* create if nonexistant */
            O_TRUNC		= LibCImpl.O_TRUNC,		/* truncate to zero length */
            O_EXCL		= LibCImpl.O_EXCL,		/* error if already exists */
        /* [XSI] directory restrcted delete */

        /* [XSI] directory */  S_IFBLK = LibCImpl.S_IFBLK,
        /* [XSI] named pipe (fifo) */ S_IFCHR = LibCImpl.S_IFCHR,
        /* [XSI] character special */ S_IFDIR = LibCImpl.S_IFDIR,
        /* [XSI] type of file mask */ S_IFIFO = LibCImpl.S_IFIFO,
        /* [XSI] regular */ S_IFLNK = LibCImpl.S_IFLNK,
        /*
         * [XSI] The following are symbolic names for the values of type mode_t.  They
         * are bitmap values.
         */
        /* File type */
        S_IFMT = LibCImpl.S_IFMT,
        /* [XSI] block special */ S_IFREG = LibCImpl.S_IFREG,
        /* [XSI] symbolic link */ S_IFSOCK = LibCImpl.S_IFSOCK,
        /* [XSI] RWX mask for group */ S_IRGRP = LibCImpl.S_IRGRP,
        /* [XSI] RWX mask for other */ S_IROTH = LibCImpl.S_IROTH,
        /* [XSI] RWX mask for owner */ S_IRUSR = LibCImpl.S_IRUSR,
        /* [XSI] X for owner */ /* Read, write, execute/search by group */
        S_IRWXG = LibCImpl.S_IRWXG,
        /* [XSI] X for group */ /* Read, write, execute/search by others */
        S_IRWXO = LibCImpl.S_IRWXO,
        /* [XSI] socket */

        /* File mode */
        /* Read, write, execute/search by owner */
        S_IRWXU = LibCImpl.S_IRWXU,
        /* [XSI] set user id on execution */ S_ISGID = LibCImpl.S_ISGID,
        /* [XSI] X for other */ S_ISUID = LibCImpl.S_ISUID,
        /* [XSI] set group id on execution */ S_ISVTX = LibCImpl.S_ISVTX,
        /* [XSI] R for group */ S_IWGRP = LibCImpl.S_IWGRP,
        /* [XSI] R for other */ S_IWOTH = LibCImpl.S_IWOTH,
        /* [XSI] R for owner */ S_IWUSR = LibCImpl.S_IWUSR,
        /* [XSI] W for group */ S_IXGRP = LibCImpl.S_IXGRP,
        /* [XSI] W for other */ S_IXOTH = LibCImpl.S_IXOTH,
        /* [XSI] W for owner */ S_IXUSR = LibCImpl.S_IXUSR,

            /** set file offset to offset */
            SEEK_SET = LibCImpl.SEEK_SET,
            /** set file offset to current plus offset */
            SEEK_CUR = LibCImpl.SEEK_CUR,
            /** set file offset to EOF plus offset */
            SEEK_END = LibCImpl.SEEK_END
        ; // END OF DEFINES
    
    /**
     * provides for control over descriptors.
     *
     * @param fd a descriptor to be operated on by cmd
     * @param cmd one of the cmd constants
     * @param arg 
     * @return a value that depends on the cmd.
     */
    int fcntl(int fd, int cmd, int arg);
    
    /**
     * open or create a file for reading or writing
     *
     * @param name String
     * @param oflag std libc open flags
     * @param mode  the mode for any created file
     * @return If successful, returns a non-negative integer, termed a file descriptor.  Returns
     *         -1 on failure, and sets errno to indicate the error.
     */
    int open(String name, int oflag, int mode);
    
    /**
     * delete a descriptor
     * 
     * @param fd a descriptor to be operated on by cmd
     * @return Upon successful completion, a value of 0 is returned.  Otherwise, a value of -1 is returned
     *         and the global integer variable errno is set to indicate the error.
     */
    int close(int fd);
    
    /**
     * Flush output on a descriptor
     * 
     * @param fd a descriptor to be flushed
     * @return Upon successful completion, a value of 0 is returned.  Otherwise, a value of -1 is returned
     *         and the global integer variable errno is set to indicate the error.
     */
    int fsync(int fd);

    /**
     * reposition read/write file offset
     * 
     * @param fd file descriptor
     * @param offset the offset to seek to
     * @param whence the kind of offset (SEEK_SET, SEEK_CUR, or SEEK_END)
     * @return the resulting offset location as measured in
     *         bytes from the beginning of the file.  If error, -1 is returned and errno is set
     *         to indicate the error.
     */
    int lseek(int fd, long offset, int whence);
    
    /**
     * read input
     * 
     * @param fd file descriptor
     * @param buf data buffer to read into
     * @param nbyte number of bytes to read
     * @return the number of bytes actually read is returned.  Upon reading end-of-file, zero
     *         is returned.  If error, a -1 is returned and the global variable errno is set to indicate
     *         the error
     */
    int read(int fd, byte[] buf, int nbyte);
    
    /**
     * write output
     * 
     * @param fd file descriptor
     * @param buf data buffer to write
     * @param nbyte number of bytes to read
     * @return the number of bytes which were written is returned.  If error,
     *         -1 is returned and the global variable errno is set to indicate the error.
     */
    int write(int fd, byte[] buf, int nbyte);
    
    /**
     * C struct stat
     * //    struct stat {
     * //        dev_t		st_dev;		/* [XSI] ID of device containing file             4 0
     * //        ino_t	  	st_ino;		/* [XSI] File serial number                       4 4
     * //        mode_t	 	st_mode;	/* [XSI] Mode of file (see below)             2 8
     * //        nlink_t		st_nlink;	/* [XSI] Number of hard links                 2 10
     * //        uid_t		st_uid;		/* [XSI] User ID of the file                      4 12
     * //        gid_t		st_gid;		/* [XSI] Group ID of the file                     4 16
     * //        dev_t		st_rdev;	/* [XSI] Device ID                                4 20
     * //        time_t		st_atime;	/* [XSI] Time of last access                  4 24
     * //        long		st_atimensec;	/* nsec of last access                        4 28
     * //        time_t		st_mtime;	/* [XSI] Last data modification time          4 32
     * //        long		st_mtimensec;	/* last data modification nsec                4 36
     * //        time_t		st_ctime;	/* [XSI] Time of last status change           4 40
     * //        long		st_ctimensec;	/* nsec of last status change                 4 44
     * //        off_t		st_size;	/* [XSI] file size, in bytes                      8 48
     * //        blkcnt_t	st_blocks;	/* [XSI] blocks allocated for file                8
     * //        blksize_t	st_blksize;	/* [XSI] optimal blocksize for I/O                4
     * //        __uint32_t	st_flags;	/* user defined flags for file                4
     * //        __uint32_t	st_gen;		/* file generation number                     4
     * //        __int32_t	st_lspare;	/* RESERVED: DO NOT USE!                          4
     * //        __int64_t	st_qspare[2];	/* RESERVED: DO NOT USE!                      16
     * //     };
     */
    public final static class stat extends LibCImpl.statImpl {
        public final static int 
                EPERM = LibCImpl.EPERM;
        
        /** mode_t */
        public int st_mode;
        /** time_t Last data modification time */
        public int st_mtime;
        /** file size, in bytes */
        public long st_size;

    }
    
        /**
     * Get information on the open file with file descriptor "fd".
     *
     * @param fd file descriptor
     * @param stat Stat structure that will be filled with the current values
     * @return -1 is returned if an error occurs, otherwise zero is returned
     */
    int fstat(int fd, stat stat);

    /**
     * Get information on the named "name".
     *
     * @param name String
     * @param stat Stat structure that will be filled with the current values
     * @return -1 is returned if an error occurs, otherwise zero is returned
     */
    int stat(String name, stat stat);


    /**
     * Remove the directory entry for name (may delete the file)
     *
     * @param name String
     * @return If successful, returns a non-negative integer, termed a file descriptor.  Returns
     *         -1 on failure, and sets errno to indicate the error.
     */
    int unlink(String name);

}