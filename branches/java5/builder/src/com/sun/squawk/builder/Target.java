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

package com.sun.squawk.builder;

import java.io.*;
import java.util.*;

/**
 * A <code>Target</code> denotes a builder command that compiles a directory of Java sources.
 */
public final class Target extends Command {

    protected final String extraClassPath;
    public final boolean j2me;
    public final boolean preprocess;
    public final File baseDir;
    public final File[] srcDirs;

    public List<String> extraArgs;

    /**
     * Creates a new compilation command.
     *
     * @param classPath      the class path to compile against
     * @param j2me           specifies if the classes being compiled are to be deployed on a J2ME platform
     * @param baseDir        the base directory under which the various intermediate and output directories are created
     * @param srcDirs        the directories that are searched recursively for the source files to be compiled
     * @param preprocess     specifies if the files should be {@link Preprocessor preprocessed} before compilation
     * @param env Build      the builder environment in which this command will run
     * @param   name  the name of this command
     */
    public Target(String extraClassPath, boolean j2me, String baseDir, File[] srcDirs, boolean preprocess, Build env, String name) {
        super(env, name);
        this.extraClassPath = extraClassPath;
        this.j2me = j2me;
        this.baseDir = new File(baseDir);
        this.srcDirs = srcDirs;
        this.preprocess = preprocess;
    }

    protected String getClassPathString() {
        StringBuffer classPathBuffer = new StringBuffer();
        List<String> dependencies = getDependencies();
        for (String dependency: dependencies) {
            Command command = env.getCommand(dependency);
            if (command instanceof Target) {
                classPathBuffer.append(dependency).append(File.separatorChar).append("classes");
                classPathBuffer.append(File.pathSeparatorChar);
            }
        }
        if (extraClassPath != null && extraClassPath.length() != 0) {
            classPathBuffer.append(File.pathSeparatorChar).append(Build.toPlatformPath(extraClassPath, true));
        }
        if (classPathBuffer.length() == 0) {
            return null;
        }
        return classPathBuffer.toString();
    }

    /**
     * Performs the compilation.
     *
     * {@inheritDoc}
     */
    public void run(String[] args) {
        env.javac(getClassPathString(), baseDir, srcDirs, j2me, extraArgs, preprocess);
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        String desc = "compiles the Java sources in " + srcDirs[0];
        for (int i = 1; i <  srcDirs.length; ++i) {
            if (i == srcDirs.length - 1) {
                desc = desc + " and " + srcDirs[i];
            } else {
                desc = desc + ", " + srcDirs[i];
            }
        }
        return desc;
    }

    /**
     * {@inheritDoc}
     */
    public void clean() {
    	// TODO Should really parameterize the "phoneme" entry
    	Build.clearFilesMarkedAsSvnIgnore(baseDir, "phoneme");
        Build.clear(new File(baseDir, "classes"), true);
        Build.delete(new File(baseDir, "classes.jar"));
        if (preprocess) {
            Build.clear(new File(baseDir, "preprocessed"), true);
        }
        if (j2me) {
            Build.clear(new File(baseDir, "weaved"), true);
            Build.clear(new File(baseDir, "j2meclasses"), true);
        }
        Build.clear(new File(baseDir, "javadoc"), true);
        Build.clear(new File(baseDir, "doccheck"), true);
    }
}