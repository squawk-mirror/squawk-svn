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

package com.sun.squawk.builder.ccompiler;

import java.io.*;
import com.sun.squawk.builder.platform.*;
import com.sun.squawk.builder.*;

/**
 * The interface to the GCC compiler on VxWorks/PPC
 */
public class GccVxWorksPPCCompiler extends GccCompiler {

    public GccVxWorksPPCCompiler(Build env, Platform platform) {
        super("vxworks", env, platform);
        defaultSizeofPointer = 4;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String options(boolean disableOpts) {
        StringBuffer sb = new StringBuffer(super.options(disableOpts));
        sb.append(" -DPLATFORM_BIG_ENDIAN=true -DVXWORKS ");
        
        //sb.append("");
        
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getArchitecture() {
        return "PPC";
    }

    /**
     * Compiles a small C program to determine the default pointer size of this version of gcc.
     *
     * @return  the size (in bytes) of a pointer compiled by this version of gcc
     */
/*    @Override
    protected int getDefaultSizeofPointer() {
        if (defaultSizeofPointer == -1) {
            try {
                File cFile = File.createTempFile("sizeofpointer", ".c");
                PrintStream out = new PrintStream(new FileOutputStream(cFile));
                out.println("#include <stdlib.h>");
                out.println("int main (int argc, char **argv) {");
                out.println("    exit(sizeof(char *));");
                out.println("}");
                out.close();

                String exePath = cFile.getPath();
                File exe = new File(exePath.substring(0, exePath.length() - 2));

                env.exec("ccppc -o " + exe.getPath() + " " + cFile.getPath(),
                        null, platform.getToolsDir());
                cFile.delete();

                try {
                    env.exec(exe.getPath());
                } catch (BuildException e) {
                    exe.delete();
                    return defaultSizeofPointer = e.exitValue;
                }
                throw new BuildException("gcc pointer size test returned 0");
            } catch (IOException ioe) {
                throw new BuildException("could run pointer size gcc test", ioe);
            }
        }
        return defaultSizeofPointer;
    }
*/    
    /**
     * {@inheritDoc}
     */
    @Override
    public File compile(File[] includeDirs, File source, File dir, boolean disableOpts) {
        File object = new File(dir, source.getName().replaceAll("\\.c", "\\.o"));

        String ccName = "ccppc";

        File[] newIncludes = new File[] {
            //new File("/WindRiver/vxworks-6.3/target/h/wrn/", "coreip") // For networking
            new File("coreip") // For networking
        };

        env.exec(ccName + " -c " +
                 options(disableOpts) + " " +
                 include(includeDirs, "-I") +
                 include(newIncludes, "-I") +
                 " -o \"" + object + "\" \"" + source + "\"");
        return object;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public File link(File[] objects, String out, boolean dll) {
        String output;
        String exec;

        String ccName = "ccppc";
        
        File[] newObjects = new File[objects.length];

        for(int f = 0; f < objects.length; f++)
            newObjects[f] = objects[f];

        output = out + platform.getExecutableExtension();
        exec = "--gc-sections -o " + output + " " + Build.join(newObjects);
        
        // TODO: /WindRiver/... is hardcoded..  fix this?
      //  env.exec(ccName + " -r -Wl,-X -T /WindRiver/vxworks-6.3/target/h/tool/gnu/ldscripts/link.OUT " + exec);
        env.exec(ccName + " -r -Wl,-X " + exec);

        return new File(output);
    }
    
    @Override
    public boolean isCrossPlatform() {
        return true;
    }
}
