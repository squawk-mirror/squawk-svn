package com.sun.squawk;

import com.sun.squawk.pragma.ForceInlinedPragma;
import com.sun.squawk.pragma.GlobalStaticFields;
import com.sun.squawk.util.Assert;
import com.sun.squawk.vm.HDR;

public class BackingStore implements GlobalStaticFields {

    /**
     * debug?
     */
    public static boolean SCJ_DEBUG_ENABLED;
    public static boolean SCJ_DEBUG_ALLOC;

    /**
     * The size of BS instance rounded up to word
     */
    private static int HEADER_PLUS_INSTANCE_SIZE;

    /**
     * Start of immortal memory.
     */
    private static Address immortalStart;

    /**
     * End of immortal memory.
     */
    private static Address immortalEnd;
    
    /**
     * Start of scoped memory area
     */
    private static Address scopedStart;

    /**
     * End of scoped memory area
     */
    private static Address scopedEnd;

    /**
     * The backing store of immortal memory.
     */
    private static BackingStore immortal;

    /**
     * The backing store of scoped memories.
     */
    private static BackingStore scoped;

    /**
     * The Klass instance of BackingStore
     */
    private static Klass klass;

    /**
     * The global current allocate context. The thread getting rescheduled in
     * must set this field with its saved context.
     */
    private static BackingStore curContext;

    /**
     * 
     */
    private static int counter;

    /**
     * Start of this backing store
     */
    private Address allocStart;

    /**
     * The next allocation address.
     */
    private Address allocTop;

    /**
     * End of this backing store
     */
    private Address allocEnd;

    /**
     * The enclosing backing store of this
     */
    private BackingStore parent;

    /**
     * The associated MemoryArea object if "this" is not a container.
     */
    private Object mirror;

    /**
     * The name of the backing store.
     */
    private String name;

    /**
     * The user specified size of this backing store.
     */
    private int size;

    /**
     * The real size of memory allocated. realSize == roundUpToWord(size) +
     * HEADER_PLUS_INSTANCE_SIZE.
     */
    private int realSize;

    /**
     * The size of this backing store
     */
    private int spaceRemaining;

    /**
	 * 
	 */
    private BackingStore prevBS;
    private BackingStore nextBS;

    /**
     * If "this" is container, topBS points to the top backing store, else it is
     * null.
     */
    private BackingStore topBS;

    /**
     * 
     * @param bootstrapSuite
     */
    static void initialize(Suite bootstrapSuite) {
        Assert.that(!VM.isHosted());
        Assert.always(immortalStart.eq(immortalStart.roundUpToWord()),
                "IMM limit is not word aligned");
        Assert.always(scopedStart.eq(scopedStart.roundDownToWord()),
                "SCP limit is not word aligned");

        SCJ_DEBUG_ENABLED = Klass.DEBUG_CODE_ENABLED;
        SCJ_DEBUG_ALLOC = false;

        klass = bootstrapSuite.lookup("com.sun.squawk.BackingStore");
        HEADER_PLUS_INSTANCE_SIZE = roundUpToWord(HDR.basicHeaderSize + klass.getInstanceSize()
                * HDR.BYTES_PER_WORD);

        VM.zeroWords(immortalStart, immortalEnd);
        VM.zeroWords(scopedStart, scopedEnd);

        counter = 0;

        immortal = convertToBackingStore(immortalStart, immortalEnd);
        immortal.name = "Immortal";
        immortal.size = immortal.realSize - HEADER_PLUS_INSTANCE_SIZE;
        immortal.spaceRemaining = immortal.size;

        scoped = convertToBackingStore(scopedStart, scopedEnd);
        scoped.name = "Scoped";
        scoped.size = scoped.realSize - HEADER_PLUS_INSTANCE_SIZE;
        scoped.spaceRemaining = scoped.size;

        curContext = immortal;

        if (SCJ_DEBUG_ENABLED) {
            VM.println("[SCJ] Initialize immortal and scoped backingStore ");
            immortal.printInfo();
            scoped.printInfo();
        }
    }

    /**
     * Setup the class pointer field of a header.
     * 
     * @param oop
     *            object pointer
     * @param klass
     *            the address of the object's classs
     */
    /* private */static void setHeaderClass(Address oop, Object klass) throws ForceInlinedPragma {
        NativeUnsafe.setAddress(oop, HDR.klass, klass);
    }

    /**
     * Setup the length word of a header.
     * 
     * @param oop
     *            object pointer
     * @param length
     *            the length in elements of the array
     */
    /* private */static void setHeaderLength(Address oop, int length) throws ForceInlinedPragma {
        NativeUnsafe.setUWord(oop, HDR.length, encodeLengthWord(length));
    }

    /**
     * 
     * @param size
     * @param klass
     * @param arrayLength
     * @return
     */
    public static Object allocate(int size, Object klass, int arrayLength) {
        return curContext.allocate0(size, klass, arrayLength);
    }

    /**
     * 
     * FIXME: not thread safe
     * 
     * Allocate a chunk of zeroed memory from this backing store
     * 
     * @param size
     * @param klass
     * @param arrayLength
     * @return
     */
    private Object allocate0(int size, Object klass, int arrayLength) {

        // for debug
        Address theStart = allocStart;
        Address theTop = allocTop;
        Address theEnd = allocEnd;
        Klass klass0 = (Klass) klass;
        Klass type;
        // for debug end

        if (SCJ_DEBUG_ALLOC) {

            VM.print("[SCJ] allocate ");
            VM.print(size);
            VM.print("B {");
            if (arrayLength == -1) {
                VM.print(klass0.getName());
            } else {
                VM.print("[");
                type = klass0.getComponentType();
                while (type.isArray()) {
                    VM.print("[");
                    type = type.getComponentType();
                }
                VM.print(type.getName());
            }
            VM.print("} in {");
            VM.print(name);
            VM.print("} - allocTop: ");
            VM.printAddress(theTop);
            VM.print(" --> ");
            VM.printAddress(allocTop);
            VM.println();
        }

        Assert.that(size >= 0);

        if (spaceRemaining < size) {
            VM.println("[SCJ] !!!!!!!!!!!!!!!!!!! Out of Memory !!!!!!!!!!!!!!!!!!!!!!!");
            printInfo();
            throw VM.getOutOfMemoryError();
        }

        Address oop;
        if (arrayLength == -1) {
            oop = allocTop.add(HDR.basicHeaderSize);
            setHeaderClass(oop, klass);
        } else {
            oop = allocTop.add(HDR.arrayHeaderSize);
            setHeaderClass(oop, klass);
            setHeaderLength(oop, arrayLength);
            Assert.always(GC.getArrayLength(oop) == arrayLength);
        }
        // don't zero at allocation; zero at destroy instead

        increaseAllocTop(size);

        Assert.always(theStart.eq(allocStart));
        Assert.always(allocTop.diff(theTop).toInt() == size);
        Assert.always(theEnd.eq(allocEnd));

        return oop;
    }

    private void increaseAllocTop(int offset) {
        allocTop = allocTop.add(offset);
        Assert.always(allocTop.loeq(allocEnd), "allocTop > allocEnd");
        Assert.always(allocTop.hieq(allocStart.add(HEADER_PLUS_INSTANCE_SIZE)),
                "allocTop < BS Header");
        spaceRemaining -= offset;
    }

    private void increaseAllocEnd(int offset) {
        allocEnd = allocEnd.add(offset);
        Assert.always(allocEnd.hieq(allocStart.add(HEADER_PLUS_INSTANCE_SIZE)),
                "allocEnd < BS Header");
        size += offset;
        realSize += offset;
        spaceRemaining += offset;
    }

    public static boolean inImmortal(Address addr) {
        return !(addr.loeq(immortalStart) || addr.hi(immortalEnd));
    }

    public static boolean inScoped(Address addr) {
        return !(addr.loeq(scopedStart) || addr.hi(scopedEnd));
    }

    public boolean inScope(Address addr) {
        return !(addr.loeq(allocStart) || addr.hi(allocEnd));
    }

    /**
     * 
     * @return the current BS
     */
    public static BackingStore getCurrentContext() {
        return curContext;
    }

    /**
     * 
     * @param bs
     *            the new BS
     * @return the old BS
     */
    public static BackingStore setCurrentContext(BackingStore bs) {
        BackingStore old = curContext;
        curContext = bs;

        if (SCJ_DEBUG_ENABLED) {
            VM.print("[SCJ] Set current allocation context from [");
            VM.print(old.name);
            VM.print("] to [ ");
            VM.print(curContext.name);
            VM.println(" ]");
        }

        return old;
    }

    /**
     * 
     * @return
     */
    public static BackingStore getImmortal() {
        return immortal;
    }

    /**
     * 
     * @return
     */
    public static BackingStore getScoped() {
        return scoped;
    }

    // TODO: a better effective implementation will require to add BS
    // information to the object header
    public static BackingStore getBackingStore(Object obj) {
        Address addr = Address.fromObject(obj);
        if (immortal.inScope(addr))
            return immortal;
        if (scoped.inScope(addr))
            return scoped.getBackingStoreRecursive(addr);
        return null;
    }

    private BackingStore getBackingStoreRecursive(Address addr) {
        BackingStore child = topBS;
        while (child != null) {
            if (child.inScope(addr))
                return child.getBackingStoreRecursive(addr);
            child = child.prevBS;
        }
        return this;
    }

    private static int roundUpToWord(int value) {
        return (value + HDR.BYTES_PER_WORD - 1) & ~(HDR.BYTES_PER_WORD - 1);
    }

    /**
     * Convert a block of memory to backing store, where the BS instance locates
     * at the front of the block. Following fields will be set:
     * allocStart/Top/End, realSize.
     * 
     * @param start
     * @param end
     * @return
     */
    private static BackingStore convertToBackingStore(Address start, Address end) {
        int allocSize = end.diff(start).toInt();
        Assert.that(allocSize >= HEADER_PLUS_INSTANCE_SIZE,
                "Memory too small to contain BackingStore instance itself!");

        Address oop = start.add(HDR.basicHeaderSize);

        NativeUnsafe.setObject(oop, HDR.klass, klass);
        BackingStore bs = (BackingStore) oop.toObject();

        bs.allocStart = start;
        bs.allocTop = start.add(HEADER_PLUS_INSTANCE_SIZE);
        bs.allocEnd = end;
        bs.realSize = allocSize;

        // if (SCJ_DEBUG_ENABLED) {
        // VM.println("[SCJ] Convert raw memory to backing store");
        // VM.print("[SCJ]  - start: ");
        // VM.println(bs.allocStart);
        // VM.print("[SCJ]  - top:   ");
        // VM.println(bs.allocTop);
        // VM.print("[SCJ]  - end:   ");
        // VM.println(bs.allocEnd);
        // VM.print("[SCJ]  - size:  ");
        // VM.println(bs.realSize);
        // }

        return bs;
    }

    /**
     * Encode an array length word.
     * 
     * @param length
     *            the length to encode
     * @return the encoded length word
     */
    private static UWord encodeLengthWord(int length) throws ForceInlinedPragma {
        // Can only support arrays whose length can encoded in 30 bits. Throwing
        // an out of memory error is the cleanest way to handle this situtation
        // in the rare case that there was enough memory to allocate the array
        if (length > 0x3FFFFFF) {
            VM.println("encodeLengthWord");
            throw VM.getOutOfMemoryError();
        }
        return UWord.fromPrimitive((length << HDR.headerTagBits) | HDR.arrayHeaderTag);
    }

    /**
     * 
     * @return
     */
    public int bsConsumed() {
        return size - spaceRemaining;
    }

    /**
     * 
     * @return
     */
    public int bsRemaining() {
        return spaceRemaining;
    }

    /**
     * FIXME: not thread safe
     * 
     * Pop myself from container and zero my memory space (the instance of
     * "this" is erased at the same time).
     */
    public void destroy() {
        if (parent == null) {
            throw new Error("Immortal or Scoped cannot be destroyed");
        } else if (parent.topBS != this) {
            throw new Error("Only topBS in a container can be destroyed");
        }
        parent.increaseAllocTop(-realSize);
        parent.topBS = prevBS;
        VM.zeroWords(allocStart, allocTop);
    }

    public void destroyAllAbove() {
        if (parent != null) {
            while (parent.topBS != this)
                parent.topBS.destroy();
            nextBS = null;
        }
    }

    /**
     * 
     * Allocate a block of memory as new backing store in current backing store
     * space. The new instance will be set with its: size, spaceRemaining,
     * container.
     * 
     * FIXME: not thread safe
     * 
     * @param size
     *            the user specified size of new backing store
     * @return the new backing store instance
     */
    public BackingStore excavate(int size) {
        Assert.that(size >= 0);

        int allocSize = size + HEADER_PLUS_INSTANCE_SIZE;
        if (spaceRemaining < allocSize) {
            throw VM.getOutOfMemoryError();
        }

        Address oldTop = allocTop;
        increaseAllocTop(allocSize);

        BackingStore bs = convertToBackingStore(oldTop, allocTop);
        bs.size = size;
        bs.parent = this;
        bs.spaceRemaining = size;
        bs.name = "BS-" + counter++;
        if (topBS != null) {
            topBS.nextBS = bs;
            bs.prevBS = topBS;
        }
        topBS = bs;

        if (SCJ_DEBUG_ENABLED) {
            VM.println("[SCJ] BackingStore.excavate ... ");
            VM.println("[SCJ] == container info ==");
            printInfo();
            VM.println("[SCJ] ==   result info  ==");
            bs.printInfo();
        }

        return bs;
    }

    public BackingStore getParentBS() {
        return parent;
    }

    /**
     * 
     * @return
     */
    public Object getMirror() {
        return mirror;
    }

    /**
     * 
     * @return
     */
    public int getSize() {
        return size;
    }

    /**
     * 
     * @return
     */
    public int getRealSize() {
        return realSize;
    }

    /**
	 * 
	 */
    public void reset() {
        if (SCJ_DEBUG_ENABLED) {
            VM.println("[SCJ] BackingStore.reset ... ");
            VM.println("[SCJ] == Before ==");
            printInfo();
        }

        Address oldTop = allocTop;
        increaseAllocTop(-oldTop.diff(allocStart.add(HEADER_PLUS_INSTANCE_SIZE)).toInt());
        VM.zeroWords(allocTop, oldTop);
        topBS = null;

        /*
         * if i'm the top, take all the container's available space as mine.
         */
        if (parent != null && parent.topBS == this) {
            int freeSpace = parent.spaceRemaining;
            parent.increaseAllocTop(freeSpace);
            increaseAllocEnd(freeSpace);
        }

        if (SCJ_DEBUG_ENABLED) {
            VM.println("[SCJ] == After ==");
            printInfo();
        }
    }

    public void setMirror(Object mirror) {
        this.mirror = mirror;
    }

    /**
     * Shrink is dangerous and strictly only applied to mission memory, which
     * when shrinking we are sure there is nothing on top of it in the
     * container.
     * 
     * @param newSize
     * 
     * @throws IllegalStateException
     *             if the objects already allocated within this MissionMemory
     *             consume more than newSize bytes.
     * 
     * @throws IllegalArgumentException
     *             if newSize is larger than the current size of the
     *             MissionMemory.
     */
    public void shrink(int newSize) throws IllegalStateException, IllegalArgumentException {
        int offset = size - newSize;
        if (offset < 0)
            throw new IllegalArgumentException();

        int available = allocEnd.diff(allocTop).toInt();
        if (available < offset)
            throw new IllegalStateException();

        if (SCJ_DEBUG_ENABLED) {
            VM.println("[SCJ] BackingStore.shrink ... ");
            VM.println("[SCJ] == Before ==");
            printInfo();
        }

        if (parent == null) {
            throw new Error("Immortal or Scoped is not allowed to shrink");
        } else if (parent.topBS != this) {
            throw new Error("Only top scope is allowed to shrink");
        }
        parent.increaseAllocTop(-offset);
        increaseAllocEnd(-offset);

        if (SCJ_DEBUG_ENABLED) {
            VM.println("[SCJ] == After ==");
            printInfo();
        }
    }

    public static void preInitializeClassInSuite(Suite suite) {
        if (BackingStore.SCJ_DEBUG_ENABLED) {
            VM.print("[SCJ] initialize classes in suite: ");
            VM.print(suite.getName());
            VM.println(".suite");
        }
        for (int i = suite.getClassCount() - 1; i >= 0; i--) {
            Klass klass = suite.getKlass(i);
            // FIXME: we will have trouble finding native symbols when forcing
            // initializing platform specific classes that are incompatible with
            // current platform.
            if (!klass.getName().startsWith("com.sun.squawk.platform")) {
                if (BackingStore.SCJ_DEBUG_ENABLED)
                    VM.println(klass.getName());
                klass.initializeInternal();
            }
        }
    }

    public void printInfo() {
        VM.print("[SCJ] Backing store information of [");
        VM.print(name);
        VM.println("]");
        VM.print("[SCJ] - size:            ");
        VM.println(this.size);
        VM.print("[SCJ] - realSize:        ");
        VM.println(this.realSize);
        VM.print("[SCJ] - remaining:       ");
        VM.println(this.spaceRemaining);
        VM.print("[SCJ] - allocStart:      ");
        VM.printAddress(this.allocStart);
        VM.println();
        VM.print("[SCJ] - allocTop:        ");
        VM.printAddress(this.allocTop);
        VM.println();
        VM.print("[SCJ] - allocEnd:        ");
        VM.printAddress(this.allocEnd);
        VM.println();
        VM.print("[SCJ] - container:       ");
        VM.println(this.parent == null ? "null" : this.parent.name);
        VM.print("[SCJ] - prevBS:          ");
        VM.println(this.prevBS == null ? "null" : this.prevBS.name);
        VM.print("[SCJ] - nextBS:          ");
        VM.println(this.nextBS == null ? "null" : this.nextBS.name);
        VM.print("[SCJ] - topBS:           ");
        VM.println(this.topBS == null ? "null" : this.topBS.name);
        VM.println();
    }

    /**
     * 
     */
    public static void printBSTree(boolean detailed) {
        VM.println();
        VM.println("[SCJ] print entire backing store tree ---------------------------- start ");
        immortal.printBSTreeRecursive(detailed, 0);
        scoped.printBSTreeRecursive(detailed, 0);
        VM.println("[SCJ] print entire backing store tree ---------------------------- end ");
        VM.println();
    }

    private void printBSTreeRecursive(boolean detailed, int indent) {
        printSpace(indent);
        VM.print(name);
        if (parent != null && this == parent.topBS)
            VM.print("*");
        if (detailed) {
            VM.print("    [ ");
            VM.printAddress(allocStart);
            VM.print(" - ");
            VM.printAddress(allocTop);
            VM.print(" - ");
            VM.printAddress(allocEnd);
            VM.print(" ] remains: ");
            VM.print(spaceRemaining);
            VM.print("B");
        }
        VM.println();
        BackingStore child = topBS;
        while (child != null && child.prevBS != null)
            child = child.prevBS;
        while (child != null) {
            child.printBSTreeRecursive(detailed, indent + 1);
            child = child.nextBS;
        }
    }

    private static void printSpace(int indent) {
        VM.print("[SCJ] ");
        for (int i = 0; i < indent; i++)
            VM.print("    ");
    }

    static void test() {
        float ff = 0.1234f;
        double dd = 0.1234;
        int[] a = new int[] { 222, 333, 444 };
        double[] f = new double[] { 0.222, 0.333, 0.444 };
        Dummy d = new Dummy();
        boolean r = d._int == 12345 && d._char == 'j' && d._long == 948472349 && d._boolean == true
                && d._float == 0.12345 && d._double == 3.1415926 && a[0] == 222 && a[1] == 333
                && a[2] == 444;
        if (!r) {
            VM.println("------------ Error in allocation ------------");
            VM.println(d._int);
            VM.println(d._char);
            VM.println(d._long);
            VM.println(d._boolean);
            VM.println(d._float);
            VM.println(d._double);
            VM.println(a[0]);
            VM.println(a[1]);
            VM.println(a[2]);
            VM.println(f[0]);
            VM.println(f[1]);
            VM.println(f[2]);
            VM.println(ff);
            VM.println(dd);
            VM.println(a.length);
            Assert.always(r);
        }
    }

    private static boolean verifyZero(Address start, Address end) {
        VM.print("[SCJ] start verify memory [");
        VM.printAddress(start);
        VM.print(" - ");
        VM.printAddress(end);
        VM.println("]");
        boolean res = true;
        // boolean open = true;
        int range = end.diff(start).toInt();
        for (int i = 0; i < range; i++) {
            // if (open) {
            if (NativeUnsafe.getByte(start, i) != 0) {
                res = false;
                // VM.print("[");
                // VM.print(start.add(i));
                // VM.print(" - ");
                // open = false;
            }
            // } else {
            // if (Unsafe.getInt(start, i) == 0) {
            // VM.print(start.add(i));
            // VM.print("] ");
            // open = true;
            // }
            // }
        }
        return res;
    }

    static class Dummy {
        int _int = 12345;
        float _float = 0.12345f;
        char _char = 'j';
        boolean _boolean = true;
        double _double = 3.1415926;
        long _long = 948472349;
    }
}
