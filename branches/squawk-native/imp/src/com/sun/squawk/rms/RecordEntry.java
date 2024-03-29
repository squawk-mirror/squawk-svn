/*
 * Copyright 2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk.rms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.rms.RecordStoreException;

import com.sun.squawk.flash.IMemoryHeapBlock;

public class RecordEntry extends RmsEntry implements IRecordEntry {
    public static final int TYPE = 2;
    
    protected int storeId;
    protected int id;
    protected int bytesLength;
    protected int bytesOffset;
    protected byte[] bytes;

    public RecordEntry() {
    }
    
    public byte[] getBytes() {
        if (bytesLength == 0) {
            return null;
        }
        byte[] copy = new byte[bytesLength];
        System.arraycopy(bytes, bytesOffset, copy, 0, bytesLength);
        return copy;
    }
    
    public int getBytes(byte[] buffer, int offset, int length) {
        if (length == 0) {
            return 0;
        }
        if (length > bytesLength) {
            throw new IndexOutOfBoundsException();
        }
        System.arraycopy(bytes, bytesOffset, buffer, offset, length);
        return length;
    }
    
    public int getBytesLength() {
        return bytesLength;
    }
    
    public int getId() {
        return id;
    }
    
    public int getStoreId() {
        return storeId;
    }
    
    public int getType() {
        return TYPE;
    }
    
    public void readFrom(IMemoryHeapBlock memoryBlock) throws IOException {
        super.readFrom(memoryBlock);
        DataInputStream input = memoryBlock.getDataInputStream();
        storeId = input.readInt();
        id = input.readInt();
        bytesLength = input.readShort();
        bytesOffset = memoryBlock.getOffset() + memoryBlock.getByteArrayInputStream().getPos();
        bytes = memoryBlock.getBytes();
        input.skip(bytesLength);
    }
    
    public void setBytes(byte[] bytes, int offset, int length) {
        this.bytes = bytes;
        this.bytesOffset = offset;
        this.bytesLength = length;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setStoreId(int id) {
        storeId = id;
    }
    
    public void visit(IRmsEntryVisitor visitor) throws RecordStoreException {
        visitor.visitRecord(this);
    }
    
    public void writeTo(DataOutputStream dataOut) throws IOException {
        super.writeTo(dataOut);
        dataOut.writeInt(storeId);
        dataOut.writeInt(id);
        dataOut.writeShort((short) bytesLength);
        if (bytesLength > 0) {
            dataOut.write(bytes, bytesOffset, bytesLength);
        }
    }

}
