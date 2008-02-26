//if[SUITE_VERIFIER]
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

package com.sun.squawk.translator.ir.verifier;

import com.sun.squawk.*;
import com.sun.squawk.vm.Native;
import com.sun.squawk.util.Assert;

class NativeVerifierHelper {
    private static final Klass INT = Klass.INT,
                               SHORT = Klass.SHORT,
                               CHAR = Klass.CHAR,
                               BYTE = Klass.BYTE,
                               BOOLEAN = Klass.BOOLEAN,
/*if[FLOATS]*/
                               FLOAT = Klass.FLOAT,
                               DOUBLE = Klass.DOUBLE,
/*end[FLOATS]*/
                               WORD = Klass.OFFSET,
                               UWORD = Klass.UWORD,
                               REF = Klass.ADDRESS,
                               LONG = Klass.LONG,
                               KLASS = Klass.KLASS,
                               OOP = Klass.OBJECT;

    static void do_invokenative(Frame frame, int index) {
        switch (index) {
        case Native.com_sun_squawk_Address$add: {
            frame.pop(INT); // int
            frame.pop(REF); // com.sun.squawk.Address (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(REF); // com.sun.squawk.Address
            return;
        }

        case Native.com_sun_squawk_Address$addOffset: {
            frame.pop(WORD); // com.sun.squawk.Offset
            frame.pop(REF); // com.sun.squawk.Address (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(REF); // com.sun.squawk.Address
            return;
        }

        case Native.com_sun_squawk_Address$and: {
            frame.pop(UWORD); // com.sun.squawk.UWord
            frame.pop(REF); // com.sun.squawk.Address (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(REF); // com.sun.squawk.Address
            return;
        }

        case Native.com_sun_squawk_Address$diff: {
            frame.pop(REF); // com.sun.squawk.Address
            frame.pop(REF); // com.sun.squawk.Address (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(WORD); // com.sun.squawk.Offset
            return;
        }

        case Native.com_sun_squawk_Address$eq: {
            frame.pop(REF); // com.sun.squawk.Address
            frame.pop(REF); // com.sun.squawk.Address (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(BOOLEAN); // boolean
            return;
        }

        case Native.com_sun_squawk_Address$fromObject: {
            frame.pop(OOP); // java.lang.Object
            Assert.that(frame.isStackEmpty());
            frame.push(REF); // com.sun.squawk.Address
            return;
        }

        case Native.com_sun_squawk_Address$fromPrimitive: {
            frame.pop(INT); // int
            Assert.that(frame.isStackEmpty());
            frame.push(REF); // com.sun.squawk.Address
            return;
        }

        case Native.com_sun_squawk_Address$hi: {
            frame.pop(REF); // com.sun.squawk.Address
            frame.pop(REF); // com.sun.squawk.Address (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(BOOLEAN); // boolean
            return;
        }

        case Native.com_sun_squawk_Address$hieq: {
            frame.pop(REF); // com.sun.squawk.Address
            frame.pop(REF); // com.sun.squawk.Address (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(BOOLEAN); // boolean
            return;
        }

        case Native.com_sun_squawk_Address$isMax: {
            frame.pop(REF); // com.sun.squawk.Address (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(BOOLEAN); // boolean
            return;
        }

        case Native.com_sun_squawk_Address$isZero: {
            frame.pop(REF); // com.sun.squawk.Address (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(BOOLEAN); // boolean
            return;
        }

        case Native.com_sun_squawk_Address$lo: {
            frame.pop(REF); // com.sun.squawk.Address
            frame.pop(REF); // com.sun.squawk.Address (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(BOOLEAN); // boolean
            return;
        }

        case Native.com_sun_squawk_Address$loeq: {
            frame.pop(REF); // com.sun.squawk.Address
            frame.pop(REF); // com.sun.squawk.Address (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(BOOLEAN); // boolean
            return;
        }

        case Native.com_sun_squawk_Address$max: {
            Assert.that(frame.isStackEmpty());
            frame.push(REF); // com.sun.squawk.Address
            return;
        }

        case Native.com_sun_squawk_Address$ne: {
            frame.pop(REF); // com.sun.squawk.Address
            frame.pop(REF); // com.sun.squawk.Address (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(BOOLEAN); // boolean
            return;
        }

        case Native.com_sun_squawk_Address$or: {
            frame.pop(UWORD); // com.sun.squawk.UWord
            frame.pop(REF); // com.sun.squawk.Address (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(REF); // com.sun.squawk.Address
            return;
        }

        case Native.com_sun_squawk_Address$roundDown: {
            frame.pop(INT); // int
            frame.pop(REF); // com.sun.squawk.Address (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(REF); // com.sun.squawk.Address
            return;
        }

        case Native.com_sun_squawk_Address$roundDownToWord: {
            frame.pop(REF); // com.sun.squawk.Address (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(REF); // com.sun.squawk.Address
            return;
        }

        case Native.com_sun_squawk_Address$roundUp: {
            frame.pop(INT); // int
            frame.pop(REF); // com.sun.squawk.Address (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(REF); // com.sun.squawk.Address
            return;
        }

        case Native.com_sun_squawk_Address$roundUpToWord: {
            frame.pop(REF); // com.sun.squawk.Address (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(REF); // com.sun.squawk.Address
            return;
        }

        case Native.com_sun_squawk_Address$sub: {
            frame.pop(INT); // int
            frame.pop(REF); // com.sun.squawk.Address (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(REF); // com.sun.squawk.Address
            return;
        }

        case Native.com_sun_squawk_Address$subOffset: {
            frame.pop(WORD); // com.sun.squawk.Offset
            frame.pop(REF); // com.sun.squawk.Address (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(REF); // com.sun.squawk.Address
            return;
        }

        case Native.com_sun_squawk_Address$toObject: {
            frame.pop(REF); // com.sun.squawk.Address (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(OOP); // java.lang.Object
            return;
        }

        case Native.com_sun_squawk_Address$toUWord: {
            frame.pop(REF); // com.sun.squawk.Address (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(UWORD); // com.sun.squawk.UWord
            return;
        }

        case Native.com_sun_squawk_Address$zero: {
            Assert.that(frame.isStackEmpty());
            frame.push(REF); // com.sun.squawk.Address
            return;
        }

        case Native.com_sun_squawk_UWord$and: {
            frame.pop(UWORD); // com.sun.squawk.UWord
            frame.pop(UWORD); // com.sun.squawk.UWord (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(UWORD); // com.sun.squawk.UWord
            return;
        }

        case Native.com_sun_squawk_UWord$eq: {
            frame.pop(UWORD); // com.sun.squawk.UWord
            frame.pop(UWORD); // com.sun.squawk.UWord (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(BOOLEAN); // boolean
            return;
        }

        case Native.com_sun_squawk_UWord$fromPrimitive: {
            frame.pop(INT); // int
            Assert.that(frame.isStackEmpty());
            frame.push(UWORD); // com.sun.squawk.UWord
            return;
        }

        case Native.com_sun_squawk_UWord$hi: {
            frame.pop(UWORD); // com.sun.squawk.UWord
            frame.pop(UWORD); // com.sun.squawk.UWord (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(BOOLEAN); // boolean
            return;
        }

        case Native.com_sun_squawk_UWord$hieq: {
            frame.pop(UWORD); // com.sun.squawk.UWord
            frame.pop(UWORD); // com.sun.squawk.UWord (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(BOOLEAN); // boolean
            return;
        }

        case Native.com_sun_squawk_UWord$isMax: {
            frame.pop(UWORD); // com.sun.squawk.UWord (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(BOOLEAN); // boolean
            return;
        }

        case Native.com_sun_squawk_UWord$isZero: {
            frame.pop(UWORD); // com.sun.squawk.UWord (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(BOOLEAN); // boolean
            return;
        }

        case Native.com_sun_squawk_UWord$lo: {
            frame.pop(UWORD); // com.sun.squawk.UWord
            frame.pop(UWORD); // com.sun.squawk.UWord (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(BOOLEAN); // boolean
            return;
        }

        case Native.com_sun_squawk_UWord$loeq: {
            frame.pop(UWORD); // com.sun.squawk.UWord
            frame.pop(UWORD); // com.sun.squawk.UWord (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(BOOLEAN); // boolean
            return;
        }

        case Native.com_sun_squawk_UWord$max: {
            Assert.that(frame.isStackEmpty());
            frame.push(UWORD); // com.sun.squawk.UWord
            return;
        }

        case Native.com_sun_squawk_UWord$ne: {
            frame.pop(UWORD); // com.sun.squawk.UWord
            frame.pop(UWORD); // com.sun.squawk.UWord (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(BOOLEAN); // boolean
            return;
        }

        case Native.com_sun_squawk_UWord$or: {
            frame.pop(UWORD); // com.sun.squawk.UWord
            frame.pop(UWORD); // com.sun.squawk.UWord (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(UWORD); // com.sun.squawk.UWord
            return;
        }

        case Native.com_sun_squawk_UWord$toInt: {
            frame.pop(UWORD); // com.sun.squawk.UWord (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(INT); // int
            return;
        }

        case Native.com_sun_squawk_UWord$toOffset: {
            frame.pop(UWORD); // com.sun.squawk.UWord (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(WORD); // com.sun.squawk.Offset
            return;
        }

        case Native.com_sun_squawk_UWord$toPrimitive: {
            frame.pop(UWORD); // com.sun.squawk.UWord (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(INT); // int
            return;
        }

        case Native.com_sun_squawk_UWord$zero: {
            Assert.that(frame.isStackEmpty());
            frame.push(UWORD); // com.sun.squawk.UWord
            return;
        }

        case Native.com_sun_squawk_Offset$add: {
            frame.pop(INT); // int
            frame.pop(WORD); // com.sun.squawk.Offset (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(WORD); // com.sun.squawk.Offset
            return;
        }

        case Native.com_sun_squawk_Offset$bytesToWords: {
            frame.pop(WORD); // com.sun.squawk.Offset (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(WORD); // com.sun.squawk.Offset
            return;
        }

        case Native.com_sun_squawk_Offset$eq: {
            frame.pop(WORD); // com.sun.squawk.Offset
            frame.pop(WORD); // com.sun.squawk.Offset (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(BOOLEAN); // boolean
            return;
        }

        case Native.com_sun_squawk_Offset$fromPrimitive: {
            frame.pop(INT); // int
            Assert.that(frame.isStackEmpty());
            frame.push(WORD); // com.sun.squawk.Offset
            return;
        }

        case Native.com_sun_squawk_Offset$ge: {
            frame.pop(WORD); // com.sun.squawk.Offset
            frame.pop(WORD); // com.sun.squawk.Offset (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(BOOLEAN); // boolean
            return;
        }

        case Native.com_sun_squawk_Offset$gt: {
            frame.pop(WORD); // com.sun.squawk.Offset
            frame.pop(WORD); // com.sun.squawk.Offset (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(BOOLEAN); // boolean
            return;
        }

        case Native.com_sun_squawk_Offset$isZero: {
            frame.pop(WORD); // com.sun.squawk.Offset (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(BOOLEAN); // boolean
            return;
        }

        case Native.com_sun_squawk_Offset$le: {
            frame.pop(WORD); // com.sun.squawk.Offset
            frame.pop(WORD); // com.sun.squawk.Offset (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(BOOLEAN); // boolean
            return;
        }

        case Native.com_sun_squawk_Offset$lt: {
            frame.pop(WORD); // com.sun.squawk.Offset
            frame.pop(WORD); // com.sun.squawk.Offset (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(BOOLEAN); // boolean
            return;
        }

        case Native.com_sun_squawk_Offset$ne: {
            frame.pop(WORD); // com.sun.squawk.Offset
            frame.pop(WORD); // com.sun.squawk.Offset (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(BOOLEAN); // boolean
            return;
        }

        case Native.com_sun_squawk_Offset$sub: {
            frame.pop(INT); // int
            frame.pop(WORD); // com.sun.squawk.Offset (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(WORD); // com.sun.squawk.Offset
            return;
        }

        case Native.com_sun_squawk_Offset$toInt: {
            frame.pop(WORD); // com.sun.squawk.Offset (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(INT); // int
            return;
        }

        case Native.com_sun_squawk_Offset$toPrimitive: {
            frame.pop(WORD); // com.sun.squawk.Offset (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(INT); // int
            return;
        }

        case Native.com_sun_squawk_Offset$toUWord: {
            frame.pop(WORD); // com.sun.squawk.Offset (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(UWORD); // com.sun.squawk.UWord
            return;
        }

        case Native.com_sun_squawk_Offset$wordsToBytes: {
            frame.pop(WORD); // com.sun.squawk.Offset (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(WORD); // com.sun.squawk.Offset
            return;
        }

        case Native.com_sun_squawk_Offset$zero: {
            Assert.that(frame.isStackEmpty());
            frame.push(WORD); // com.sun.squawk.Offset
            return;
        }

        case Native.com_sun_squawk_NativeUnsafe$charAt: {
            frame.pop(INT); // int
            frame.pop(OOP); // java.lang.String
            Assert.that(frame.isStackEmpty());
            frame.push(CHAR); // char
            return;
        }

        case Native.com_sun_squawk_NativeUnsafe$copyTypes: {
            frame.pop(INT); // int
            frame.pop(REF); // com.sun.squawk.Address
            frame.pop(REF); // com.sun.squawk.Address
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_NativeUnsafe$getAddress: {
            frame.pop(INT); // int
            frame.pop(OOP); // java.lang.Object
            Assert.that(frame.isStackEmpty());
            frame.push(REF); // com.sun.squawk.Address
            return;
        }

        case Native.com_sun_squawk_NativeUnsafe$getAsByte: {
            frame.pop(INT); // int
            frame.pop(OOP); // java.lang.Object
            Assert.that(frame.isStackEmpty());
            frame.push(INT); // int
            return;
        }

        case Native.com_sun_squawk_NativeUnsafe$getAsInt: {
            frame.pop(INT); // int
            frame.pop(OOP); // java.lang.Object
            Assert.that(frame.isStackEmpty());
            frame.push(INT); // int
            return;
        }

        case Native.com_sun_squawk_NativeUnsafe$getAsShort: {
            frame.pop(INT); // int
            frame.pop(OOP); // java.lang.Object
            Assert.that(frame.isStackEmpty());
            frame.push(INT); // int
            return;
        }

        case Native.com_sun_squawk_NativeUnsafe$getAsUWord: {
            frame.pop(INT); // int
            frame.pop(OOP); // java.lang.Object
            Assert.that(frame.isStackEmpty());
            frame.push(UWORD); // com.sun.squawk.UWord
            return;
        }

        case Native.com_sun_squawk_NativeUnsafe$getByte: {
            frame.pop(INT); // int
            frame.pop(OOP); // java.lang.Object
            Assert.that(frame.isStackEmpty());
            frame.push(INT); // int
            return;
        }

        case Native.com_sun_squawk_NativeUnsafe$getChar: {
            frame.pop(INT); // int
            frame.pop(OOP); // java.lang.Object
            Assert.that(frame.isStackEmpty());
            frame.push(INT); // int
            return;
        }

        case Native.com_sun_squawk_NativeUnsafe$getInt: {
            frame.pop(INT); // int
            frame.pop(OOP); // java.lang.Object
            Assert.that(frame.isStackEmpty());
            frame.push(INT); // int
            return;
        }

        case Native.com_sun_squawk_NativeUnsafe$getLong: {
            frame.pop(INT); // int
            frame.pop(OOP); // java.lang.Object
            Assert.that(frame.isStackEmpty());
            frame.push(LONG); // long
            return;
        }

        case Native.com_sun_squawk_NativeUnsafe$getLongAtWord: {
            frame.pop(INT); // int
            frame.pop(OOP); // java.lang.Object
            Assert.that(frame.isStackEmpty());
            frame.push(LONG); // long
            return;
        }

        case Native.com_sun_squawk_NativeUnsafe$getObject: {
            frame.pop(INT); // int
            frame.pop(OOP); // java.lang.Object
            Assert.that(frame.isStackEmpty());
            frame.push(OOP); // java.lang.Object
            return;
        }

        case Native.com_sun_squawk_NativeUnsafe$getShort: {
            frame.pop(INT); // int
            frame.pop(OOP); // java.lang.Object
            Assert.that(frame.isStackEmpty());
            frame.push(INT); // int
            return;
        }

        case Native.com_sun_squawk_NativeUnsafe$getType: {
            frame.pop(REF); // com.sun.squawk.Address
            Assert.that(frame.isStackEmpty());
            frame.push(BYTE); // byte
            return;
        }

        case Native.com_sun_squawk_NativeUnsafe$getUWord: {
            frame.pop(INT); // int
            frame.pop(OOP); // java.lang.Object
            Assert.that(frame.isStackEmpty());
            frame.push(UWORD); // com.sun.squawk.UWord
            return;
        }

        case Native.com_sun_squawk_NativeUnsafe$setAddress: {
            frame.pop(OOP); // java.lang.Object
            frame.pop(INT); // int
            frame.pop(OOP); // java.lang.Object
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_NativeUnsafe$setArrayTypes: {
            frame.pop(INT); // int
            frame.pop(INT); // int
            frame.pop(BYTE); // byte
            frame.pop(REF); // com.sun.squawk.Address
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_NativeUnsafe$setByte: {
            frame.pop(INT); // int
            frame.pop(INT); // int
            frame.pop(OOP); // java.lang.Object
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_NativeUnsafe$setChar: {
            frame.pop(INT); // int
            frame.pop(INT); // int
            frame.pop(OOP); // java.lang.Object
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_NativeUnsafe$setInt: {
            frame.pop(INT); // int
            frame.pop(INT); // int
            frame.pop(OOP); // java.lang.Object
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_NativeUnsafe$setLong: {
            frame.pop(LONG); // long
            frame.pop(INT); // int
            frame.pop(OOP); // java.lang.Object
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_NativeUnsafe$setLongAtWord: {
            frame.pop(LONG); // long
            frame.pop(INT); // int
            frame.pop(OOP); // java.lang.Object
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_NativeUnsafe$setObject: {
            frame.pop(OOP); // java.lang.Object
            frame.pop(INT); // int
            frame.pop(OOP); // java.lang.Object
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_NativeUnsafe$setShort: {
            frame.pop(INT); // int
            frame.pop(INT); // int
            frame.pop(OOP); // java.lang.Object
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_NativeUnsafe$setType: {
            frame.pop(INT); // int
            frame.pop(BYTE); // byte
            frame.pop(REF); // com.sun.squawk.Address
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_NativeUnsafe$setUWord: {
            frame.pop(UWORD); // com.sun.squawk.UWord
            frame.pop(INT); // int
            frame.pop(OOP); // java.lang.Object
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_NativeUnsafe$swap: {
            frame.pop(INT); // int
            frame.pop(REF); // com.sun.squawk.Address
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_NativeUnsafe$swap2: {
            frame.pop(REF); // com.sun.squawk.Address
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_NativeUnsafe$swap4: {
            frame.pop(REF); // com.sun.squawk.Address
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_NativeUnsafe$swap8: {
            frame.pop(REF); // com.sun.squawk.Address
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_VM$addToClassStateCache: {
            frame.pop(OOP); // java.lang.Object
            frame.pop(KLASS); // com.sun.squawk.Klass
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_VM$addressResult: {
            Assert.that(frame.isStackEmpty());
            frame.push(REF); // com.sun.squawk.Address
            return;
        }

        case Native.com_sun_squawk_VM$allocate: {
            frame.pop(INT); // int
            frame.pop(OOP); // java.lang.Object
            frame.pop(INT); // int
            Assert.that(frame.isStackEmpty());
            frame.push(OOP); // java.lang.Object
            return;
        }

        case Native.com_sun_squawk_VM$allocateVirtualStack: {
            frame.pop(INT); // int
            Assert.that(frame.isStackEmpty());
            frame.push(REF); // com.sun.squawk.Address
            return;
        }

        case Native.com_sun_squawk_VM$asKlass: {
            frame.pop(OOP); // java.lang.Object
            Assert.that(frame.isStackEmpty());
            frame.push(KLASS); // com.sun.squawk.Klass
            return;
        }

        case Native.com_sun_squawk_VM$callStaticNoParm: {
            frame.pop(INT); // int
            frame.pop(KLASS); // com.sun.squawk.Klass
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_VM$callStaticOneParm: {
            frame.pop(OOP); // java.lang.Object
            frame.pop(INT); // int
            frame.pop(KLASS); // com.sun.squawk.Klass
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_VM$copyBytes: {
            frame.pop(BOOLEAN); // boolean
            frame.pop(INT); // int
            frame.pop(INT); // int
            frame.pop(REF); // com.sun.squawk.Address
            frame.pop(INT); // int
            frame.pop(REF); // com.sun.squawk.Address
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_VM$deadbeef: {
            frame.pop(REF); // com.sun.squawk.Address
            frame.pop(REF); // com.sun.squawk.Address
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_VM$doubleToLongBits: {
            frame.pop(DOUBLE); // double
            Assert.that(frame.isStackEmpty());
            frame.push(LONG); // long
            return;
        }

        case Native.com_sun_squawk_VM$executeCIO: {
            frame.pop(OOP); // java.lang.Object
            frame.pop(OOP); // java.lang.Object
            frame.pop(INT); // int
            frame.pop(INT); // int
            frame.pop(INT); // int
            frame.pop(INT); // int
            frame.pop(INT); // int
            frame.pop(INT); // int
            frame.pop(INT); // int
            frame.pop(INT); // int
            frame.pop(INT); // int
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_VM$executeCOG: {
            frame.pop(OOP); // com.sun.squawk.ObjectMemorySerializer$ControlBlock
            frame.pop(OOP); // java.lang.Object
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_VM$executeGC: {
            frame.pop(BOOLEAN); // boolean
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_VM$fatalVMError: {
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_VM$finalize: {
            frame.pop(OOP); // java.lang.Object
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_VM$floatToIntBits: {
            frame.pop(FLOAT); // float
            Assert.that(frame.isStackEmpty());
            frame.push(INT); // int
            return;
        }

        case Native.com_sun_squawk_VM$getBranchCount: {
            Assert.that(frame.isStackEmpty());
            frame.push(LONG); // long
            return;
        }

        case Native.com_sun_squawk_VM$getFP: {
            Assert.that(frame.isStackEmpty());
            frame.push(REF); // com.sun.squawk.Address
            return;
        }

        case Native.com_sun_squawk_VM$getGlobalAddr: {
            frame.pop(INT); // int
            Assert.that(frame.isStackEmpty());
            frame.push(REF); // com.sun.squawk.Address
            return;
        }

        case Native.com_sun_squawk_VM$getGlobalAddrCount: {
            Assert.that(frame.isStackEmpty());
            frame.push(INT); // int
            return;
        }

        case Native.com_sun_squawk_VM$getGlobalInt: {
            frame.pop(INT); // int
            Assert.that(frame.isStackEmpty());
            frame.push(INT); // int
            return;
        }

        case Native.com_sun_squawk_VM$getGlobalIntCount: {
            Assert.that(frame.isStackEmpty());
            frame.push(INT); // int
            return;
        }

        case Native.com_sun_squawk_VM$getGlobalOop: {
            frame.pop(INT); // int
            Assert.that(frame.isStackEmpty());
            frame.push(OOP); // java.lang.Object
            return;
        }

        case Native.com_sun_squawk_VM$getGlobalOopCount: {
            Assert.that(frame.isStackEmpty());
            frame.push(INT); // int
            return;
        }

        case Native.com_sun_squawk_VM$getGlobalOopTable: {
            Assert.that(frame.isStackEmpty());
            frame.push(REF); // com.sun.squawk.Address
            return;
        }

        case Native.com_sun_squawk_VM$getInterruptStatus: {
            frame.pop(INT); // int
            frame.pop(INT); // int
            Assert.that(frame.isStackEmpty());
            frame.push(LONG); // long
            return;
        }

        case Native.com_sun_squawk_VM$getMP: {
            frame.pop(REF); // com.sun.squawk.Address
            Assert.that(frame.isStackEmpty());
            frame.push(OOP); // java.lang.Object
            return;
        }

        case Native.com_sun_squawk_VM$getPreviousFP: {
            frame.pop(REF); // com.sun.squawk.Address
            Assert.that(frame.isStackEmpty());
            frame.push(REF); // com.sun.squawk.Address
            return;
        }

        case Native.com_sun_squawk_VM$getPreviousIP: {
            frame.pop(REF); // com.sun.squawk.Address
            Assert.that(frame.isStackEmpty());
            frame.push(REF); // com.sun.squawk.Address
            return;
        }

        case Native.com_sun_squawk_VM$hasVirtualMonitorObject: {
            frame.pop(OOP); // java.lang.Object
            Assert.that(frame.isStackEmpty());
            frame.push(BOOLEAN); // boolean
            return;
        }

        case Native.com_sun_squawk_VM$hashcode: {
            frame.pop(OOP); // java.lang.Object
            Assert.that(frame.isStackEmpty());
            frame.push(INT); // int
            return;
        }

        case Native.com_sun_squawk_VM$intBitsToFloat: {
            frame.pop(INT); // int
            Assert.that(frame.isStackEmpty());
            frame.push(FLOAT); // float
            return;
        }

        case Native.com_sun_squawk_VM$invalidateClassStateCache: {
            Assert.that(frame.isStackEmpty());
            frame.push(BOOLEAN); // boolean
            return;
        }

        case Native.com_sun_squawk_VM$isBigEndian: {
            Assert.that(frame.isStackEmpty());
            frame.push(BOOLEAN); // boolean
            return;
        }

        case Native.com_sun_squawk_VM$isInKernel: {
            Assert.that(frame.isStackEmpty());
            frame.push(BOOLEAN); // boolean
            return;
        }

        case Native.com_sun_squawk_VM$longBitsToDouble: {
            frame.pop(LONG); // long
            Assert.that(frame.isStackEmpty());
            frame.push(DOUBLE); // double
            return;
        }

        case Native.com_sun_squawk_VM$math: {
            frame.pop(DOUBLE); // double
            frame.pop(DOUBLE); // double
            frame.pop(INT); // int
            Assert.that(frame.isStackEmpty());
            frame.push(DOUBLE); // double
            return;
        }

        case Native.com_sun_squawk_VM$pause: {
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_VM$removeVirtualMonitorObject: {
            Assert.that(frame.isStackEmpty());
            frame.push(OOP); // java.lang.Object
            return;
        }

        case Native.com_sun_squawk_VM$sendInterrupt: {
            frame.pop(INT); // int
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_VM$serviceResult: {
            Assert.that(frame.isStackEmpty());
            frame.push(INT); // int
            return;
        }

        case Native.com_sun_squawk_VM$setGlobalAddr: {
            frame.pop(INT); // int
            frame.pop(REF); // com.sun.squawk.Address
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_VM$setGlobalInt: {
            frame.pop(INT); // int
            frame.pop(INT); // int
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_VM$setGlobalOop: {
            frame.pop(INT); // int
            frame.pop(OOP); // java.lang.Object
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_VM$setPreviousFP: {
            frame.pop(REF); // com.sun.squawk.Address
            frame.pop(REF); // com.sun.squawk.Address
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_VM$setPreviousIP: {
            frame.pop(REF); // com.sun.squawk.Address
            frame.pop(REF); // com.sun.squawk.Address
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_VM$setupAlarmInterval: {
            frame.pop(INT); // int
            frame.pop(INT); // int
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_VM$setupInterrupt: {
            frame.pop(OOP); // java.lang.String
            frame.pop(INT); // int
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_VM$threadSwitch: {
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_VM$zeroWords: {
            frame.pop(REF); // com.sun.squawk.Address
            frame.pop(REF); // com.sun.squawk.Address
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_CheneyCollector$memoryProtect: {
            frame.pop(REF); // com.sun.squawk.Address
            frame.pop(REF); // com.sun.squawk.Address
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_ServiceOperation$cioExecute: {
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_GarbageCollector$collectGarbageInC: {
            frame.pop(BOOLEAN); // boolean
            frame.pop(REF); // com.sun.squawk.Address
            frame.pop(OOP); // com.sun.squawk.GarbageCollector (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(BOOLEAN); // boolean
            return;
        }

        case Native.com_sun_squawk_GarbageCollector$copyObjectGraphInC: {
            frame.pop(REF); // com.sun.squawk.Address
            frame.pop(OOP); // com.sun.squawk.ObjectMemorySerializer$ControlBlock
            frame.pop(REF); // com.sun.squawk.Address
            frame.pop(OOP); // com.sun.squawk.GarbageCollector (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(REF); // com.sun.squawk.Address
            return;
        }

        case Native.com_sun_squawk_GarbageCollector$hasNativeImplementation: {
            frame.pop(OOP); // com.sun.squawk.GarbageCollector (receiver)
            Assert.that(frame.isStackEmpty());
            frame.push(BOOLEAN); // boolean
            return;
        }

        case Native.com_sun_squawk_Lisp2Bitmap$clearBitFor: {
            frame.pop(REF); // com.sun.squawk.Address
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_Lisp2Bitmap$clearBitsFor: {
            frame.pop(REF); // com.sun.squawk.Address
            frame.pop(REF); // com.sun.squawk.Address
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_Lisp2Bitmap$getAddressForBitmapWord: {
            frame.pop(REF); // com.sun.squawk.Address
            Assert.that(frame.isStackEmpty());
            frame.push(REF); // com.sun.squawk.Address
            return;
        }

        case Native.com_sun_squawk_Lisp2Bitmap$getAddressOfBitmapWordFor: {
            frame.pop(REF); // com.sun.squawk.Address
            Assert.that(frame.isStackEmpty());
            frame.push(REF); // com.sun.squawk.Address
            return;
        }

        case Native.com_sun_squawk_Lisp2Bitmap$initialize: {
            frame.pop(REF); // com.sun.squawk.Address
            frame.pop(INT); // int
            frame.pop(REF); // com.sun.squawk.Address
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_Lisp2Bitmap$iterate: {
            Assert.that(frame.isStackEmpty());
            frame.push(REF); // com.sun.squawk.Address
            return;
        }

        case Native.com_sun_squawk_Lisp2Bitmap$setBitFor: {
            frame.pop(REF); // com.sun.squawk.Address
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_Lisp2Bitmap$setBitsFor: {
            frame.pop(REF); // com.sun.squawk.Address
            frame.pop(REF); // com.sun.squawk.Address
            Assert.that(frame.isStackEmpty());
            return;
        }

        case Native.com_sun_squawk_Lisp2Bitmap$testAndSetBitFor: {
            frame.pop(REF); // com.sun.squawk.Address
            Assert.that(frame.isStackEmpty());
            frame.push(BOOLEAN); // boolean
            return;
        }

        case Native.com_sun_squawk_Lisp2Bitmap$testBitFor: {
            frame.pop(REF); // com.sun.squawk.Address
            Assert.that(frame.isStackEmpty());
            frame.push(BOOLEAN); // boolean
            return;
        }

        case Native.com_sun_squawk_VM$lcmp: {
            frame.pop(LONG); // long
            frame.pop(LONG); // long
            Assert.that(frame.isStackEmpty());
            frame.push(INT); // int
            return;
        }

        }
        Assert.that(false, "native method with index " + index + " was not found");
    }
}
