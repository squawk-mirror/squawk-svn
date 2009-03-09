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
#include <string.h>
#include <time.h>
#include <jni.h>
#include <signal.h>


#define WIN32_LEAN_AND_MEAN
#define NOMSG
#include <windows.h>
#include <process.h>
#include <winsock2.h>

#define jlong  __int64

#define FT2INT64(ft) ((jlong)(ft).dwHighDateTime << 32 | (jlong)(ft).dwLowDateTime)

/* The package that conmtains the native code to use for a "NATIVE" platform type*/
 #define sysPlatformName() "com.sun.squawk.platform.windows"

/* This standard C function is not provided on Windows */
char* strsignal(int signum) {
    switch (signum) {
        case SIGABRT:     return "SIGABRT: Abnormal termination";
        case SIGFPE:      return "SIGFPE: Floating-point error";
        case SIGILL:      return "SIGILL: Illegal instruction";
        case SIGINT:      return "SIGINT: CTRL+C signal";
        case SIGSEGV:     return "SIGSEGV: Illegal storage access";
        case SIGTERM:     return "SIGTERM: Termination request";
        default:          return "<unknown signal>";
    }
}


jlong sysTimeMicros(void) {
    static jlong fileTime_1_1_70 = 0;
    SYSTEMTIME st0;
    FILETIME   ft0;

    if (fileTime_1_1_70 == 0) {
        /*
         * Initialize fileTime_1_1_70 -- the Win32 file time of midnight
         * 1/1/70.
         */
        memset(&st0, 0, sizeof(st0));
        st0.wYear  = 1970;
        st0.wMonth = 1;
        st0.wDay   = 1;
        SystemTimeToFileTime(&st0, &ft0);
        fileTime_1_1_70 = FT2INT64(ft0);
    }

    GetSystemTime(&st0);
    SystemTimeToFileTime(&st0, &ft0);

    /* file times are in 100ns increments, i.e. .0001ms */
    return (FT2INT64(ft0) - fileTime_1_1_70) / 10;
}

jlong sysTimeMillis(void) {
    return sysTimeMicros() / 1000;
}

/**
 * Sleep Squawk for specified milliseconds
 */
void osMilliSleep(long long millis) {
// this should probably become SleepEx, and hook into async event handling...
    Sleep(millis);
}

/**
 * Gets the page size (in bytes) of the system.
 *
 * @return the page size (in bytes) of the system
 */
int sysGetPageSize(void) {
    SYSTEM_INFO systemInfo;
    GetSystemInfo(&systemInfo);
    return systemInfo.dwPageSize;
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
    unsigned long old;
//fprintf(stderr, format("toggle memory protection: start=%A len=%d end=%A readonly=%s\n"), start, len, end,  readonly ? "true" : "false");
    if (VirtualProtect(start, len, readonly ? PAGE_READONLY : PAGE_READWRITE, &old) == 0) {
        fprintf(stderr, format("Could not toggle memory protection: errno=%d addr=%A len=%d readonly=%s\n"), GetLastError(), start, len, readonly ? "true" : "false");
    }
}

/**
 * Allocate a page-aligned chunk of memory of the given size.
 * 
 * @param size size in bytes to allocate
 * @return pointer to allocated memory or null.
 */
INLINE void* sysValloc(size_t size) {
    return VirtualAlloc(0, size, MEM_RESERVE|MEM_COMMIT, PAGE_READWRITE);

}

/**
 * Free chunk of memory allocated by sysValloc
 * 
 * @param ptr to to chunk allocated by sysValloc
 */
INLINE void sysVallocFree(void* ptr) {
    VirtualFree(ptr, 0, MEM_RELEASE);
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
    HINSTANCE handle;
    jint (JNICALL *CreateJavaVM)(JavaVM **jvm, void **env, void *args) = 0;

    char *name = getenv("JVMDLL");
    if (name == 0) {
    	name = getenv("JAVA_HOME");
    	if (name == 0) {
        	name = "jvm.dll";
    	} else {
    		char *append = "\\jre\\bin\\client\\jvm.dll";
    		char *buff = malloc(strlen(name)+strlen(append)+1);
    		// TODO - this memory isn't being freed, but that's probably
    		// ok since the size is small and this is called only once
    		if (buff == 0) {
    			fprintf(stderr, "Cannot malloc space for jvmdll path\n");
    			return false;
    		}
    		if (name[0] == '\'') {
    			strcpy(buff, name+1);
    			buff[strlen(name)-2] = 0;
    		} else {
    			strcpy(buff, name);
    		}
    		strcat(buff, append);
    		name = buff;
    	}
    }


    handle = LoadLibrary(name);
    if (handle == 0) {
        fprintf(stderr, "Cannot load %s\n", name);
        fprintf(stderr, "Please add the directory containing jvm.dll to your PATH\n");
        fprintf(stderr, "environment variable or set the JVMDLL environment variable\n");
        fprintf(stderr, "to the full path of this file.\n");
        return false;
    }

    CreateJavaVM = (jint (JNICALL *)(JavaVM **,void **, void *)) GetProcAddress(handle, "JNI_CreateJavaVM");

    if (CreateJavaVM == 0) {
        fprintf(stderr,"Cannot resolve JNI_CreateJavaVM in %s\n", name);
        return false;
    }

    return CreateJavaVM(jvm, env, args) == 0;
}
#endif



int sleepTime;
int ticks;

static void ticker(void) {
    for(;;) {
        Sleep(sleepTime);
        ticks++;
    }
}

void osprofstart(int interval) {
    sleepTime = interval;
#ifdef _MT
    if (sleepTime > 0) {
        printf("********** Time profiling set to %d ms **********\n", sleepTime);
        _beginthread((void (*))ticker, 0, 0);
    }
#else
    fprintf(stderr, "No MT -- Profiling not implemented");
    exit(0);
#endif
}

#define OSPROF(traceIP, traceFP, lastOpcode) \
{                                            \
    int t = ticks;                           \
    ticks = 0;                               \
    while (t-- > 0) {                        \
        printProfileStackTrace(traceIP, traceFP, lastOpcode); \
    } \
}

#define USE_CUSTOM_DL_CODE 1

static HMODULE defaultRTLD = 0;

void* sys_RTLD_DEFAULT() {
    if (defaultRTLD == 0) {
        //GetModuleHandleEx(0,0,&defaultRTLD); // winxp only
        defaultRTLD = GetModuleHandle(NULL);
    }
    return (void*)defaultRTLD;
}

void* sysdlopen(char* name) {
    return LoadLibrary(name);
}

int sysdlclose(void* handle) {
    return FreeLibrary(handle);
}

void* sysdlerror() {
    DWORD err = GetLastError();
    if (err == 0) {
        return NULL;
    } else {
       return "some error occurred"; // TODO: at sprintf the errno to a buffer, or call FormatMessage
    }
}

void* dlsym(void* handle, const char* symbol) {
    return GetProcAddress(handle, symbol);
}


#undef VOID

#define osloop()        /**/
#define osbackbranch()  /**/
#define osfinish()      /**/
