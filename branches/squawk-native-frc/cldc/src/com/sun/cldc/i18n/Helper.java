/*
 * Copyright 2007-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.cldc.i18n;

import java.io.*;
//import com.sun.squawk.util.Assert;

/**
 * This class provides general helper functions for the J2ME environment.
 * <p>
 * <em>No application code should reference this class directly.</em>
 *
 * @version CLDC 1.1 03/29/02
 */
public class Helper {
    
    /**
     * Purely static class should not be instantiated.
     */
    private Helper() {}

    /**
     * The name of the default character encoding
     * - hardcoded here and in System.getProperty().
     */
    public static final String defaultEncoding = "ISO8859_1";
    
    /**
     * If set to true, then ONLY support ISO8859_1 encodings,
     * although aliases (such as "US_ASCII") are allowed.
     */
    public static final boolean ISO8859_1_ONLY_SUPPORTED = true;

    /**
     * Default path to the J2ME classes. Hardcode for Squawk.
     */
    private static final String defaultMEPath = "com.sun.cldc.i18n.j2me";

/*------------------------------------------------------------------------------*/
/*                               Character encoding                             */
/*------------------------------------------------------------------------------*/

    /**
     * Get a reader for an InputStream
     *
     * @param  is              The input stream the reader is for
     * @return                 A new reader for the stream
     */
    public static Reader getStreamReader(InputStream is) {
        try {
            return getStreamReader(is, defaultEncoding);
        } catch(UnsupportedEncodingException x) {
                throw new RuntimeException(
/*if[VERBOSE_EXCEPTIONS]*/
                                             "Missing default encoding "+defaultEncoding
/*end[VERBOSE_EXCEPTIONS]*/
                );
        }
    }

    /**
     * Get a reader for an InputStream
     *
     * @param  is              The input stream the reader is for
     * @param  name            The name of the decoder
     * @return                 A new reader for the stream
     * @exception UnsupportedEncodingException  If the encoding is not known
     */
    public static Reader getStreamReader(InputStream is, String name) throws UnsupportedEncodingException {

        /* Test for null arguments */
        if (is == null || name == null) {
            throw new NullPointerException();
        }

        /* Get the reader from the encoding */
        StreamReader fr = (StreamReader)getStreamReaderOrWriter(name, "_Reader");

        /* Open the connection and return*/
        return fr.open(is, name);
    }

    private static Object getStreamReaderOrWriter(String name, String suffix)
        throws UnsupportedEncodingException {
        if (name == null) {
            throw new NullPointerException();
        }

        name = internalNameForEncoding(name);

        try {
             String className;

             /* Get the reader class name */
             className = defaultMEPath + '.' + name + suffix;

             /* Using the decoder names lookup the implementation class */
             Class clazz = Class.forName(className);

             /* Return a new instance */
             return clazz.newInstance();
        } catch(ClassNotFoundException x) {
            throw new UnsupportedEncodingException(
/*if[VERBOSE_EXCEPTIONS]*/
                                         "Encoding "+name+" not found"
/*end[VERBOSE_EXCEPTIONS]*/
            );
        } catch(InstantiationException x) {
            throw new RuntimeException(
/*if[VERBOSE_EXCEPTIONS]*/
                                         "InstantiationException "+x.getMessage()
/*end[VERBOSE_EXCEPTIONS]*/
            );
        } catch(IllegalAccessException x) {
            throw new RuntimeException(
/*if[VERBOSE_EXCEPTIONS]*/
                                         "IllegalAccessException "+x.getMessage()
/*end[VERBOSE_EXCEPTIONS]*/
            );
        }
    }


    /**
     * Get a writer for an OutputStream
     *
     * @param  os              The output stream the reader is for
     * @return                 A new writer for the stream
     */
    public static Writer getStreamWriter(OutputStream os) {
        try {
            return getStreamWriter(os, defaultEncoding);
        } catch(UnsupportedEncodingException x) {
            throw new RuntimeException(
/*if[VERBOSE_EXCEPTIONS]*/
                                             "Missing default encoding " + 
                                             defaultEncoding
/*end[VERBOSE_EXCEPTIONS]*/
                );
        }
    }


    /**
     * Get a writer for an OutputStream
     *
     * @param  os              The output stream the reader is for
     * @param  name            The name of the decoder
     * @return                 A new writer for the stream
     * @exception UnsupportedEncodingException  If the encoding is not known
     */
    public static Writer getStreamWriter(OutputStream os, String name) throws UnsupportedEncodingException {

        /* Test for null arguments */
        if (os == null || name == null) {
            throw new NullPointerException();
        }

        /* Get the writer from the encoding */
        StreamWriter sw = (StreamWriter)getStreamReaderOrWriter(name, "_Writer");

        /* Open it on the output stream and return */
        return sw.open(os, name);
    }

    /**
     * Convert a byte array to a char array
     *
     * @param  buffer          The byte array buffer
     * @param  offset          The offset
     * @param  length          The length
     * @return                 A new char array
     */
    public static char[] byteToCharArray(byte[] buffer, int offset, int length) {
        try {
            return byteToCharArray(buffer, offset, length, defaultEncoding);
        } catch(UnsupportedEncodingException x) {
            throw new RuntimeException(
/*if[VERBOSE_EXCEPTIONS]*/
                                         "Missing default encoding " + defaultEncoding
/*end[VERBOSE_EXCEPTIONS]*/
            );
        }
    }

    /**
     * Convert a char array to a byte array
     *
     * @param  buffer          The char array buffer
     * @param  offset          The offset
     * @param  length          The length
     * @return                 A new byte array
     */
    public static byte[] charToByteArray(char[] buffer, int offset, int length) {
        try {
            return charToByteArray(buffer, offset, length, defaultEncoding);
        } catch(UnsupportedEncodingException x) {
            throw new RuntimeException(
/*if[VERBOSE_EXCEPTIONS]*/
                                         "Missing default encoding "+defaultEncoding
/*end[VERBOSE_EXCEPTIONS]*/
            );
        }
    }

    /*
     * Cached variables for byteToCharArray
     */
    private static String lastReaderEncoding;
    private static StreamReader  lastReader;

    /**
     * Convert a byte array to a char array
     *
     * @param  buffer          The byte array buffer
     * @param  offset          The offset
     * @param  length          The length
     * @param  enc             The character encoding
     * @return                 A new char array
     * @exception UnsupportedEncodingException  If the encoding is not known
     */
    public static char[] byteToCharArray(byte[] buffer, int offset, int length, String enc) throws UnsupportedEncodingException {
        if (offset < 0) {
            throw new IndexOutOfBoundsException(
/*if[VERBOSE_EXCEPTIONS]*/
                                         Integer.toString(offset)
/*end[VERBOSE_EXCEPTIONS]*/
            );
        }

        if (length < 0) {
            throw new IndexOutOfBoundsException(
/*if[VERBOSE_EXCEPTIONS]*/
                                         Integer.toString(length)
/*end[VERBOSE_EXCEPTIONS]*/
            );
        }

        /* Note: offset or length might be near -1>>>1 */
        if (offset > buffer.length - length) {
            throw new IndexOutOfBoundsException(
/*if[VERBOSE_EXCEPTIONS]*/                                           
                                         Integer.toString(offset + length)
/*end[VERBOSE_EXCEPTIONS]*/
            );
        }

        //Because most cases use ISO8859_1 encoding, we can optimize this case.
        if(isISO8859_1(enc)) {
            char[] value = new char[length];
            for(int i=0; i<length; i++) {
                value[i] = (char)(buffer[i+offset] & 0xff);
            }
            return value;
        } else if (ISO8859_1_ONLY_SUPPORTED) {
            throw new UnsupportedEncodingException(enc);
        } else {
            return byteToCharArray0(buffer, offset, length, enc);
        }
    }
    
    /**
     * handle the non-ISO8859_1 cases.  Must synchronized this.
     */
     private static synchronized char[] byteToCharArray0(byte[] buffer, int offset, int length, String enc) throws UnsupportedEncodingException {
        /* If we don't have a cached reader then make one */
        if(lastReaderEncoding == null || !lastReaderEncoding.equals(enc)) {
            lastReader = (StreamReader)getStreamReaderOrWriter(enc, "_Reader");
            lastReaderEncoding = enc;
        }

        /* Ask the reader for the size the output will be */
        int size = lastReader.sizeOf(buffer, offset, length);

        /* Allocate a buffer of that size */
        char[] outbuf = new char[size];

        /* Open the reader on a ByteArrayInputStream */
        lastReader.open(new ByteArrayInputStream(buffer, offset, length), enc);

        try {
            /* Read the input */
            int numread = lastReader.read(outbuf, 0, size);
            if (numread<size) {
                // this may happen only if the last character is truncated
                // (say, it should be of 3 bytes, but there are only 2).
                lastReader.read(outbuf, numread, size-numread);
            }
            /* Close the reader */
            lastReader.close();
        } catch(IOException x) {
            throw new RuntimeException(
/*if[VERBOSE_EXCEPTIONS]*/
                                         "IOException reading reader " +x.getMessage()
/*end[VERBOSE_EXCEPTIONS]*/
            );
        }

        /* And return the buffer */
        return outbuf;
    }


    /*
     * Cached variables for charToByteArray
     */
    private static String lastWriterEncoding;
    private static StreamWriter lastWriter;

    /**
     * handle the non-ISO8859_1 cases. Must synchronized this.
     */
    private static synchronized byte[] charToByteArray0(char[] buffer, int offset, int length, String enc) throws UnsupportedEncodingException {
        /* If we don't have a cached writer then make one */
        if(lastWriterEncoding == null || !lastWriterEncoding.equals(enc)) {
            lastWriter = (StreamWriter)getStreamReaderOrWriter(enc, "_Writer");
            lastWriterEncoding = enc;
        }

        /* Ask the writer for the size the output will be */
        int size = lastWriter.sizeOf(buffer, offset, length);

        /* Get the output stream */
        ByteArrayOutputStream os = new ByteArrayOutputStream(size);

        /* Open the writer */
        lastWriter.open(os, enc);

        try {
            /* Convert */
            lastWriter.write(buffer, offset, length);
            /* Close the writer */
            lastWriter.close();
        } catch(IOException x) {
            throw new RuntimeException(
/*if[VERBOSE_EXCEPTIONS]*/
                                         "IOException writing writer " 
                                         +x.getMessage()
/*end[VERBOSE_EXCEPTIONS]*/
            );
        }

        /* Close the output stream */
        try {
            os.close();
        } catch(IOException x) {
        }

        /* Return the array */
        return os.toByteArray();
    }
    
    /**
     * Convert a byte array to a char array
     *
     * @param  buffer          The byte array buffer
     * @param  offset          The offset
     * @param  length          The length
     * @param  enc             The character encoding
     * @return                 A new char array
     * @exception UnsupportedEncodingException  If the encoding is not known
     */
    public static byte[] charToByteArray(char[] buffer, int offset, int length, String enc) throws UnsupportedEncodingException {

        //Because most cases use ISO8859_1 encoding, we can optimize this case.
        if (isISO8859_1(enc)) {
            char c;
            byte[] value = new byte[length];
            for(int i=0; i<length; i++) {
                //c = buffer[i+offset];
                //value[i] = (c <= 255) ? (byte)c : (byte)'?'; TCK doesn't like seeing this '?'
                value[i] = (byte)buffer[i+offset];
            }
            return value;
        } else if (ISO8859_1_ONLY_SUPPORTED) {
            throw new UnsupportedEncodingException(enc);
        } else {
            return charToByteArray0(buffer, offset, length, enc);
        }
    }
    
    /** 
     * Is encodingName some variation of "ISO8859_1"?
     * 
     * @param encodingName
     * @return true if encodingName is some variation of "ISO8859_1".
     */
    public static boolean isISO8859_1(String encodingName) {
        if (encodingName.equals("ISO8859_1") || internalNameForEncoding(encodingName).equals("ISO8859_1")) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Get the internal name for an encoding.
     *
     * @param encodingName encoding name
     *
     * @return internal name for this encoding
     */
    private static String internalNameForEncoding(String encodingName) {
        String internalName = normalizeEncodingName(encodingName);

        // The preferred MIME name according to the IANA Charset Registry.
        if (internalName.equals("US_ASCII")) {
            /*
             * US-ASCII is subclass of ISO-8859-1 so we do not need a
             * separate reader for it.
             */
            return "ISO8859_1";
        }

        // The preferred MIME name according to the IANA Charset Registry.
        if (internalName.equals("ISO_8859_1")) {
            return "ISO8859_1";
        }

        /*
         * Since IANA character encoding names can start with a digit
         * and that some Reader class names that do not match the standard
         * name, we have a way to configure alternate names for encodings.
         *
         * Note: The names must normalized, digits, upper case only with "_"
         *       and "_" substituted for ":" and "-".
         *
         * Use the code below only if your system really needs it:
         *
         * property = System.getProperty(internalName + "_InternalEncodingName");
         * if (property != null) {
         *     return property;
         * }
         */

        return internalName;
    }

    /**
     * Converts "-" and ":" in a string to "_" and converts the name
     * to upper case.
     * This is needed because the names of IANA character encodings have
     * characters that are not allowed for java class names and
     * IANA encoding names are not case sensitive.
     *
     * @param encodingName encoding name
     *
     * @return normalized name
     */
    private static String normalizeEncodingName(String encodingName) {
        StringBuffer normalizedName ;
        char currentChar;

        normalizedName = new StringBuffer(encodingName);

        for (int i = 0; i < normalizedName.length(); i++) {
            currentChar = normalizedName.charAt(i);

            if (currentChar == '-' || currentChar == ':') {
                normalizedName.setCharAt(i, '_');
            } else {
                normalizedName.setCharAt(i, Character.toUpperCase(currentChar));
            }
        }

        return normalizedName.toString();
    }

}
