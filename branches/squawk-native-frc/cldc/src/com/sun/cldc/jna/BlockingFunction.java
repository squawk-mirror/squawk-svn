/*
 * Copyright 2004-2009 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.cldc.jna;

import com.sun.squawk.Address;
import com.sun.squawk.NativeUnsafe;
import com.sun.squawk.VM;

/**
 * BlockingFunction allows Java code to call C function that block.
 */
public class BlockingFunction extends Function {

    private TaskExecutor taskExecutor;

    /**
     * Create a new blocking function pointer with the given name to the given address
     *
     * @param name the native name of the function
     * @param funcAddr the address of the native function
     */
    BlockingFunction(String name, Address funcAddr) {
        super(name, funcAddr);
        taskExecutor = TaskExecutor.DEFAULT;
    }

    public String toString() {
        return "BlockingFunction(" + name + ", " + funcAddr.toUWord().toInt() + ")";
    }

    protected void preamble() {
        VM.print(toString());
        VM.println(".call...");
    }

    protected void postscript(int result) {
        VM.print("DONE: ");
        VM.print(name);
        VM.print(".blockingCall returned: ");
        VM.print(result);
        VM.println();
    }

    /**
     * Set this blocking function to run using a specific TaskExecutor.
     * @param te
     */
    public void setTaskExecutor(TaskExecutor te) {
        if (taskExecutor != TaskExecutor.DEFAULT) {
            throw new IllegalStateException("already has TaskExecutor");
        }
        taskExecutor = te;
    }

    /**
     * Set this blocking function to run using a default TaskExecutor.
     */
    public void setDefaultTaskExecutor() {
        taskExecutor = TaskExecutor.DEFAULT;
    }

    public int call0() {
        if (DEBUG) {
            preamble();
        }
        Address ntask = taskExecutor.runBlockingFunction(funcAddr, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        int result = NativeUnsafe.waitForBlockingFunction(ntask);
        if (DEBUG) {
            postscript(result);
        }
        return result;
    }

    public int call1(int i1) {
        if (DEBUG) {
            preamble();
        }
        Address ntask = taskExecutor.runBlockingFunction(funcAddr, i1, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        int result = NativeUnsafe.waitForBlockingFunction(ntask);
        if (DEBUG) {
            postscript(result);
        }
        return result;
    }

    public int call2(int i1, int i2) {
        if (DEBUG) {
            preamble();
        }
        Address ntask = taskExecutor.runBlockingFunction(funcAddr, i1, i2, 0, 0, 0, 0, 0, 0, 0, 0);
        int result = NativeUnsafe.waitForBlockingFunction(ntask);
        if (DEBUG) {
            postscript(result);
        }
        return result;
    }

    public int call3(int i1, int i2, int i3) {
        if (DEBUG) {
            preamble();
        }
        Address ntask = taskExecutor.runBlockingFunction(funcAddr, i1, i2, i3, 0, 0, 0, 0, 0, 0, 0);
        int result = NativeUnsafe.waitForBlockingFunction(ntask);
        if (DEBUG) {
            postscript(result);
        }
        return result;
    }

    public int call4(int i1, int i2, int i3, int i4) {
        if (DEBUG) {
            preamble();
        }
        Address ntask = taskExecutor.runBlockingFunction(funcAddr, i1, i2, i3, i4, 0, 0, 0, 0, 0, 0);
        int result = NativeUnsafe.waitForBlockingFunction(ntask);
        if (DEBUG) {
            postscript(result);
        }
        return result;
    }

    public int call5(int i1, int i2, int i3, int i4, int i5) {
        if (DEBUG) {
            preamble();
        }
        Address ntask = taskExecutor.runBlockingFunction(funcAddr, i1, i2, i3, i4, i5, 0, 0, 0, 0, 0);
        int result = NativeUnsafe.waitForBlockingFunction(ntask);
        if (DEBUG) {
            postscript(result);
        }
        return result;
    }

    public int call6(int i1, int i2, int i3, int i4, int i5, int i6) {
        if (DEBUG) {
            preamble();
        }
        Address ntask = taskExecutor.runBlockingFunction(funcAddr, i1, i2, i3, i4, i5, i6, 0, 0, 0, 0);
        int result = NativeUnsafe.waitForBlockingFunction(ntask);
        if (DEBUG) {
            postscript(result);
        }
        return result;
    }

    public int call7(int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
        if (DEBUG) {
            preamble();
        }
        
        Address ntask = taskExecutor.runBlockingFunction(funcAddr, i1, i2, i3, i4, i5, i6, i7, 0, 0, 0);
        int result = NativeUnsafe.waitForBlockingFunction(ntask);
        if (DEBUG) {
            postscript(result);
        }
        return result;
    }

    public int call8(int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        if (DEBUG) {
            preamble();
        }

        Address ntask = taskExecutor.runBlockingFunction(funcAddr, i1, i2, i3, i4, i5, i6, i7, i8, 0, 0);
        int result = NativeUnsafe.waitForBlockingFunction(ntask);
        if (DEBUG) {
            postscript(result);
        }
        return result;
    }

    public int call9(int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9) {
        if (DEBUG) {
            preamble();
        }

        Address ntask = taskExecutor.runBlockingFunction(funcAddr, i1, i2, i3, i4, i5, i6, i7, i8, i9, 0);
        int result = NativeUnsafe.waitForBlockingFunction(ntask);
        if (DEBUG) {
            postscript(result);
        }
        return result;
    }

    public int call10(int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10) {
        if (DEBUG) {
            preamble();
        }

        Address ntask = taskExecutor.runBlockingFunction(funcAddr, i1, i2, i3, i4, i5, i6, i7, i8, i9, i10);
        int result = NativeUnsafe.waitForBlockingFunction(ntask);
        if (DEBUG) {
            postscript(result);
        }
        return result;
    }
    
}
