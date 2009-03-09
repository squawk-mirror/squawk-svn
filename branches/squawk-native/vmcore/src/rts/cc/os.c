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

#include <stdlib.h>
#include <unistd.h>
#include <signal.h>
#include <sys/time.h>
#include <pthread.h>
#include <dlfcn.h>
#include <link.h>
#include <jni.h>

#define jlong  long long

jlong sysTimeMicros(void) {
    struct timeval tv;
    long long result;
    gettimeofday(&tv, NULL);
    /* We adjust to 1000 ticks per second */
    result = (jlong)tv.tv_sec * 1000000 + tv.tv_usec;
    return result;
}

jlong sysTimeMillis(void) {
    return sysTimeMicros() / 1000;
}

#define MAX_MICRO_SLEEP 999999

/**
 * Sleep Squawk for specified milliseconds
 */
void osMilliSleep(long long millis) {
    if (millis <= 0) {
        return;
    }
    long long elapsed = sysTimeMillis();
    long long seconds = millis / 1000;
    if (seconds > 0) {
        // too long for usleep, so get close
        sleep(seconds);
    }
    elapsed = sysTimeMillis() - elapsed;
    if (elapsed < millis) {
        millis = millis - elapsed;
        long long micro = millis * 1000;
        if (micro > MAX_MICRO_SLEEP) {
            micro = MAX_MICRO_SLEEP;
        }
        usleep(micro);
    }
}

/* The package that conmtains the native code to use for a "NATIVE" platform type*/
 #define sysPlatformName() "com.sun.squawk.platform.posix"

/**
 * Gets the page size (in bytes) of the system.
 *
 * @return the page size (in bytes) of the system
 */
int sysGetPageSize(void) {
    return sysconf(_SC_PAGESIZE);
}

/**
 * Sets a region of memory read-only or reverts it to read & write.
 *
 * @param start    the start of the memory region
 * @param end      one byte past the end of the region
 * @param readonly specifies if read-only protection is to be enabled or disabled
 */
void sysToggleMemoryProtection(char* start, char* end, boolean readonly) {
    size_t len = end - start;
    if (mprotect(start, len, readonly ? PROT_READ : PROT_READ | PROT_WRITE) != 0) {
        fprintf(stderr, "Could not toggle memory protection: %s\n", strerror(errno));
    }
}

/**
 * Allocate a page-aligned chunk of memory of the given size.
 * 
 * @param size size in bytes to allocate
 * @return pointer to allocated memory or null.
 */
INLINE void* sysValloc(size_t size) {
//#ifdef sun
    //buffer = malloc(size);    this may have been work-around for solaris bug 4846556, now fixed.
//#else
    return valloc(size);
//#endif /* sun */
}

/**
 * Free chunk of memory allocated by sysValloc
 * 
 * @param ptr to to chunk allocated by sysValloc
 */
INLINE void sysVallocFree(void* ptr) {
    free(ptr);
}

/** 
 * Return another path to find the bootstrap suite with the given name.
 * On some platforms the suite might be stored in an odd location
 * 
 * @param bootstrapSuiteName the name of the boostrap suite
 * @return full or partial path to alternate location, or null
 */
INLINE char* sysGetAlternateBootstrapSuiteLocation(char* bootstrapSuiteName) { return NULL; }

#if PLATFORM_TYPE_DELEGATING
jint createJVM(JavaVM **jvm, void **env, void *args) {
    void* libVM;
    jint (JNICALL *CreateJavaVM)(JavaVM **jvm, void **env, void *args) = 0;
    char *name = "libjvm.so";

    libVM = dlopen(name, RTLD_LAZY);
    if (libVM == 0) {
        fprintf(stderr, "Cannot load %s: %s\n", name, dlerror());
        fprintf(stderr, "Please add the directories containing libjvm.so and libverify.so\n");
        fprintf(stderr, "to the LD_LIBRARY_PATH environment variable.\n\n");
        return false;
    }

    CreateJavaVM = (jint (JNICALL *)(JavaVM **,void **, void *)) dlsym(libVM, "JNI_CreateJavaVM");

    if (CreateJavaVM == 0) {
        fprintf(stderr,"Cannot resolve JNI_CreateJavaVM in %s\n", name);
        return false;
    }

    return CreateJavaVM(jvm, env, args) == 0;
}
#endif /* PLATFORM_TYPE_DELEGATING */


void startTicker(int interval) {
    fprintf(stderr, "Profiling not implemented");
    exit(0);
}

#define osloop()        /**/
#define osbackbranch()  /**/
#define osfinish()      /**/

