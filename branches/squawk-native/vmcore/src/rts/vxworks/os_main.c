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

#define VXLOADARG(arg) if(arg != NULL) { argv[argc] = arg; argc++; } else (void)0

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
	
    // Switch directories so squawk.suite will always be found
    cd("/c/squawk");

    return Squawk_main_wrapper(argc, argv); 
}
