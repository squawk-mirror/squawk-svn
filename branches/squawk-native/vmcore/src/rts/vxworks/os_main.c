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

/*
 * Platform dependent startup code directly included by squawk.c.spp
 */

#include <taskLib.h>

#define VXLOADARG(arg) if(arg != NULL) { argv[argc] = arg; argc++; printf("arg: %s\n", arg);} else (void)0

void Priv_SetWriteFileAllowed(int);

/**
 * Entry point for the VxWorks operating system.
 */
int os_main(char* arg1, char* arg2, char* arg3, char* arg4, char* arg5, char* arg6, char* arg7, char* arg8, char* arg9, char* arg10) {
    // Convert from VxWorks argument format to normal argument format

    char* argv[11];
    int argc = 1;

    argv[0] = "squawk.out";

    VXLOADARG(arg1);
    VXLOADARG(arg2);
    VXLOADARG(arg3);
    VXLOADARG(arg4);
    VXLOADARG(arg5);
    VXLOADARG(arg6);
    VXLOADARG(arg7);
    VXLOADARG(arg8);
    VXLOADARG(arg9);
    VXLOADARG(arg10);

    return Squawk_main_wrapper(argc, argv);
}

void robotTask() {
    os_main("-suite:robot", "-verbose", "-Xtgc:1", "-Xmxnvm:1M", null, null, null, null, null, null);
}

void robotTask_DEBUG() {
    os_main("-suite:robot", "-verbose", "-Xtgc:1",  "-Xmxnvm:1M", "com.sun.squawk.debugger.sda.SDA", "com.sun.squawk.imp.MIDletMainWrapper", "MIDlet-1", null, null, null);
}

/**
 * Entry point used by FRC.
 */
int squawk_StartupLibraryInit(char* arg1, char* arg2, char* arg3, char* arg4, char* arg5, char* arg6, char* arg7, char* arg8, char* arg9, char* arg10) {
    int fd;
    FUNCPTR entryPt = (FUNCPTR)robotTask;

    cd("/c/ni-rt/system");

    Priv_SetWriteFileAllowed(1);

    fd = open("SQUAWK_DEBUG_ENABLED", O_RDONLY);
    if (fd >= 0) {
        fprintf(stderr, "File SQUAWK_DEBUG_ENABLED found, starting squawk in debug mode...");
        entryPt = (FUNCPTR)robotTask_DEBUG;
        close(fd);
        remove("SQUAWK_DEBUG_ENABLED");
    } else {
        fprintf(stderr, "File SQUAWK_DEBUG_ENABLED not found, starting squawk in normal mode...");
    }

    /*
    if (strncmp("DEBUG",arg1,5) == 0) {
        fprintf(stderr, "Starting squawk in debug mode...");
        entryPt = (FUNCPTR)robotTask_DEBUG;
    } else {
        fprintf(stderr, "Starting squawk in normal mode...");
    }
    */

    
    // Start robot task
    // This is done to ensure that the C++ robot task is spawned with the floating point
    // context save parameter.
    int m_taskID = taskSpawn("SquawkRobotTask",
                                            100,
                                            VX_FP_TASK,						// options
                                            64000,						// stack size
                                            entryPt,						// function to start
                                            (int)arg1, (int)arg2, (int)arg3, (int)arg4, (int)arg5,     // parameter 1 - pointer to this class
                                            (int)arg6, (int)arg7, (int)arg8, (int)arg9, (int)arg10);   // additional unused parameters
/*
    bool ok = HandleError(m_taskID);
    if (!ok) m_taskID = kInvalidTaskID;
*/
    return 0;
}


/*

void RobotBase::robotTask(FUNCPTR factory, Task *task)
{
	RobotBase::setInstance((RobotBase*)factory());
	RobotBase::getInstance().m_task = task;
	RobotBase::getInstance().StartCompetition();
}

void RobotBase::startRobotTask(FUNCPTR factory)
{
	if (strlen(SVN_REV))
	{
		printf("WPILib was compiled from SVN revision %s\n", SVN_REV);
	}
	else
	{
		printf("WPILib was compiled from a location that is not source controlled.\n");
	}

	// Check for startup code already running
	INT32 oldId = taskNameToId("FRC_RobotTask");
	if (oldId != ERROR)
	{
		// Find the startup code module.
		MODULE_ID startupModId = moduleFindByName("FRC_UserProgram.out");
		if (startupModId != NULL)
		{
			// Remove the startup code.
			unldByModuleId(startupModId, 0);
			printf("!!!   Error: Default code was still running... Please try again.\n");
			return;
		}
		printf("!!!   Error: Other robot code is still running... Unload it and then try again.\n");
		return;
	}

	// Start robot task
	// This is done to ensure that the C++ robot task is spawned with the floating point
	// context save parameter.
	Task *task = new Task("RobotTask", (FUNCPTR)RobotBase::robotTask, Task::kDefaultPriority, 64000);
	task->Start((INT32)factory, (INT32)task);
}

*/

