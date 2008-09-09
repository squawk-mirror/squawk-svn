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

#define open(filename, flags) open(filename, flags, 0644);

#define RTLD_DEFAULT NULL	// Map the default dlsym handle to null
				// VxWorks doesn't use the handle.

#define _SC_PAGESIZE 2          // This doesn't matter.

#include <stdlib.h>
#include <sys/times.h>
#include <symLib.h>
#include <sysSymTbl.h>
#include <sys/mman.h>
#include "jni.h"

#define jlong  int64_t

/**
 * VxWorks can't be helpful and define usleep()
 */
int usleep(long microseconds) {
    struct timespec ns;

    ns.tv_sec = 0;
    ns.tv_nsec = 1000 * microseconds;

    return nanosleep(&ns, NULL);
}

/**
 * Support for util.h
 */

long sysconf(int code) {
    if(code == _SC_PAGESIZE)
        return 0x4000;
    else
        return -1; // failure
}

// TODO: Fix timezone support?
#if 0
static void getTimeZone(time_t t, int *min, int *dst)
{
    struct tm dstTm;
    char *tz = getenv(TZ_ENV);
    *dst = (localtime_r(&t, &dstTm)==OK) ? dstTm.tm_isdst : 0;

    if (tz)
    {
        /* see VxWorks timeLib develop guide */
        /*
         * name_of_zone:<(unused)>:time_in_minutes_from_UTC:daylight_start:daylight_end
         */
        /* mmddhh */
        const char *p = tz;
        int i = 0;
        while ( (i < 2) && (p = strchr(p, ':')) )
        {
           ++p;
           ++i;
        }
        if (p && (sscanf(p, "%d", min)!=1))
            *min = 0;
    }
}
#endif

int gettimeofday(struct timeval *tv, struct timezone *tz)
{
    int ret;
    struct timespec tp;

    if  ( (ret=clock_gettime(CLOCK_REALTIME, &tp))==0)
    {
        tv->tv_sec  = tp.tv_sec;
        tv->tv_usec = (tp.tv_nsec + 500) / 1000;

/*
        if (tz != NULL)
        {
            getTimeZone(tp.tv_sec, &tz->tz_minuteswest, &tz->tz_dsttime);
        }
*/
    }
     return ret;
}


jlong sysTimeMicros() {
    struct timeval tv;
    jlong result;
    gettimeofday(&tv, NULL);
    /* We adjust to 1000 ticks per second */
    result = (jlong)tv.tv_sec * 1000000 + tv.tv_usec;
    return result;
}

jlong sysTimeMillis(void) {
    return sysTimeMicros() / 1000;
}


void startTicker(int interval) {
    fprintf(stderr, "Profiling not implemented");
    exit(0);
}


void* dlsym(void* handle, const char* symbol) {
    char symName[strlen(symbol)];

    strcpy(symName, symbol);

    char** fn;
    SYM_TYPE ptype;

    STATUS status = symFindByName(sysSymTbl, symName, fn, &ptype);
	
    return status == OK ? fn : NULL;
}


char* strsignal(int signal) {
    switch(signal) {
        case SIGABRT:
            return "signal: abort";
        case SIGALRM:
            return "signal: alarm clock";
        case SIGBUS:
            return "signal: bus error";
        case SIGCHLD:
            return "signal: (exit of a) child";
        case SIGCONT:
            return "signal: continue";
        case SIGEMT:
            return "signal: EMT instruction";
        case SIGFPE:
            return "signal: floating point exception";
        case SIGHUP:
            return "signal: hangup";
        case SIGILL:
            return "signal: illegal instruction";
        case SIGINT:
            return "signal: interruption";
        case SIGSEGV:
            return "signal: segmentation violation";
        case SIGSYS:
            return "signal: (bad argument to) system call";
        case SIGTERM:
            return "signal: terminate";
        case SIGTTIN:
            return "signal: TTY input";
        case SIGTTOU:
            return "signal: TTY output";
        case SIGTSTP:
            return "signal: terminal stop";
        case SIGURG:
            return "signal: urgent I/O condition";
        case SIGUSR1:
            return "signal: user-defined signal 1";
        case SIGUSR2:
            return "signal: user-defined signal 2";
        default:
            return "unknown signal";
    }
}

#define osloop()        /**/
#define osbackbranch()  /**/
#define osfinish()      /**/
