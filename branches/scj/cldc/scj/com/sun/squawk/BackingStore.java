package com.sun.squawk;

import javax.realtime.IllegalAssignmentError;

import com.sun.squawk.pragma.ForceInlinedPragma;
import com.sun.squawk.pragma.GlobalStaticFields;
import com.sun.squawk.util.Assert;
import com.sun.squawk.vm.HDR;

public final class BackingStore implements GlobalStaticFields {

    private final class IndexTable {
        private int counter;
        private IndexTable prev;
        private IndexTable next;
        private BackingStore[] bsArray;

        private IndexTable() {
            // VM.println("[SCJ] new search table created");
            bsArray = new BackingStore[SEARCHTABLE_CAPACITY];
        }

        private BackingStore top() {
            Assert.always(counter <= SEARCHTABLE_CAPACITY, "Search table is over full");
            return bsArray[counter - 1];
        }

        private void pop() {
            Assert.always(counter != 0, "Attempt to pop empty table");
            bsArray[--counter] = null;
        }

        private void put(BackingStore bs) {
            Assert.always(counter != SEARCHTABLE_CAPACITY,
                    "Attempt to put item in a full searching table ");
            bsArray[counter++] = bs;
        }

        /**
         * 
         * @param addr
         * @return
         */
        private BackingStore search(Address addr) {
            // The table instance is put at a lower address than those of all
            // BSs it is indexing.
            if (addr.lo(Address.fromObject(this))) {
                // if (prev == null) {
                // VM.print("Unable to locate the BS for an address: lower than lower bound: ");
                // VM.printAddress(addr);
                // VM.println();
                // BackingStore.printBSTree(true);
                // }
                Assert.always(prev != null,
                        "Unable to locate the BS for an address: lower than lower bound.");
                return prev.search(addr);
            } else if (isEmpty()) {
                // VM.print("Unable to locate the BS for an address: empty table: ");
                // VM.printAddress(addr);
                // VM.println();
                // BackingStore.printBSTree(true);
                Assert.always(false, "Unable to locate the BS for an address: empty table.");
            } else if (bsArray[counter - 1].allocEnd.loeq(addr)) {
                // if (next == null) {
                // VM.print("Unable to locate the BS for an address: higher than higher bound: ");
                // VM.printAddress(addr);
                // VM.println();
                // BackingStore.printBSTree(true);
                // }
                Assert.always(next != null,
                        "Unable to locate the BS for an address: higher than higher bound.");
                return next.search(addr);
            }

            // addr is fallen in this range
            int l = 0;
            int h = counter - 1;
            while (true) {
                Assert.always(l <= h);
                int m = (h + l) / 2;
                if (bsArray[m].containAddr(addr))
                    return bsArray[m].search(addr);
                if (addr.lo(bsArray[m].allocStart))
                    h = m - 1;
                else
                    l = m + 1;
            }
        }

        private void extendIfNeed() {
            if (counter == SEARCHTABLE_CAPACITY) {
                IndexTable old = topTable;
                topTable = new IndexTable();
                topTable.prev = old;
                old.next = topTable;
            }
        }

        private boolean isEmpty() {
            return counter == 0;
        }
    }

    /**
     * debug?
     */
    public static boolean SCJ_DEBUG_ENABLED;
    public static boolean SCJ_DEBUG_ALLOC;

    /**
     * The size of BS instance rounded up to word
     */
    private static int SIZE_OF_BackingStore;

    /**
     * The size of BS instance rounded up to word
     */
    private static int SEARCHTABLE_CAPACITY;

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
    private static Klass KLASS_OF_BackingStore;

    /**
     * The global current allocate context. The thread getting rescheduled in
     * must set this field with its saved context.
     */
    private static volatile BackingStore currentAllocContext;

    /**
     * The number of backing stores that have ever been created
     */
    private static int bsCounter;

    /**
     * Whether the scope check is enabled. For most part of the program, scope
     * check should keep enabled. Only in particular kernel code which has to
     * violate the scope rule for making things work and we know what we are
     * doing to it, we can disable the check. e.g. BackingStore.excavate(). Note
     * that disable and enable should appear in pair and do not span functions.
     * 
     * ATTENTION: For now, the flag is shared by all threads. Therefore, to be
     * safe, we must make sure there is no context switch occurs between
     * disable/enable pair.
     */
    private static volatile int scopeCheckEnabled;

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
     * The associated MemoryArea object. A BS only has a mirror when it is the
     * leaf, which means no other child BS will be allocated in.
     */
    private Object mirror;

    /**
     * The backing store id.
     */
    private int id;

    /**
     * The user specified size of this backing store.
     */
    private int size;

    /**
     * The size of this backing store
     */
    private int spaceRemaining;

    /**
     * 
     */
    private IndexTable topTable;

    /**
     * The real size of memory allocated. realSize == roundUpToWord(size) +
     * HEADER_PLUS_INSTANCE_SIZE.
     */
    private int realSize;

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

        VM.zeroWords(immortalStart, immortalEnd);
        VM.zeroWords(scopedStart, scopedEnd);

        SCJ_DEBUG_ENABLED = Klass.DEBUG_CODE_ENABLED;
        SCJ_DEBUG_ALLOC = false;

        KLASS_OF_BackingStore = bootstrapSuite.lookup("com.sun.squawk.BackingStore");
        SIZE_OF_BackingStore = HDR.basicHeaderSize + KLASS_OF_BackingStore.getInstanceSize()
                * HDR.BYTES_PER_WORD;

        Klass KLASS_OF_Monitor = bootstrapSuite.lookup("com.sun.squawk.Monitor");
        Klass KLASS_OF_ObjectAssociation = bootstrapSuite
                .lookup("com.sun.squawk.ObjectAssociation");
        SIZE_OF_Monitor = HDR.basicHeaderSize + KLASS_OF_Monitor.getInstanceSize()
                * HDR.BYTES_PER_WORD;
        SIZE_OF_ObjectAssociation = HDR.basicHeaderSize
                + KLASS_OF_ObjectAssociation.getInstanceSize() * HDR.BYTES_PER_WORD;

        SEARCHTABLE_CAPACITY = 2;

        bsCounter = 0;

        immortal = convertToBS(immortalStart, immortalEnd);
        immortal.size = immortal.realSize - SIZE_OF_BackingStore;
        immortal.spaceRemaining = immortal.size;
        GC.getKlass(immortal);

        scoped = convertToBS(scopedStart, scopedEnd);
        scoped.size = scoped.realSize - SIZE_OF_BackingStore;
        scoped.spaceRemaining = scoped.size;

        currentAllocContext = immortal;

        // for debug
        illegalAssignment = "[SCJ] Illegal assignment: ";
        leftBrackets = "[";
        atBS_ = " @ BS-";
        ROM = "ROM";
        ASSIGN = "  <-   ";
        MAP = "  <==>  ";
        KEY = "   key: ";
        VAL = " value: ";
        com_sun_squawk_util_HashtableEntry = "com.sun.squawk.util.HashtableEntry";
        com_sun_squawk_util_IntHashtableEntry = "com.sun.squawk.util.IntHashtableEntry";
        // for debug
        scopeCheckEnabled = 1;

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

    public static void enableScopeCheck() {
        scopeCheckEnabled++;
    }

    public static void disableScopeCheck() {
        scopeCheckEnabled--;
    }

    public int getID() {
        return id;
    }

    /**
     * 
     * @param size
     * @param klass
     * @param arrayLength
     * @return
     */
    public static Object allocate(int size, Object klass, int arrayLength) {
        Assert.always(currentAllocContext != null, "Current allocation context cannot be null!");
        return currentAllocContext.allocate0(size, klass, arrayLength);
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

        Assert.that(size >= 0);

        if (spaceRemaining < size) {
            VM.println("[SCJ] !!!!!!!!!!!!!!!!!!! Out of Memory !!!!!!!!!!!!!!!!!!!!!!!");
            printBSTree(true);
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

        if (SCJ_DEBUG_ALLOC) {
            VM.print("[SCJ] allocate ");
            VM.print(size);
            VM.print("B ");
            if (arrayLength == -1) {
                VM.print(klass0.getName());
            } else {
                VM.print("[");
                type = klass0.getComponentType();
                while (type.isArray()) {
                    VM.print("[");
                    type = type.getComponentType();
                }
                VM.print(Klass.getInternalName(type));
            }
            VM.print(" in BS-");
            VM.print(id);
            VM.print(" - allocTop: ");
            VM.printAddress(theTop);
            VM.print(" --> ");
            VM.printAddress(allocTop);
            VM.println();
        }

        Assert.always(theStart.eq(allocStart));
        Assert.always(allocTop.diff(theTop).toInt() == size);
        Assert.always(theEnd.eq(allocEnd));

        return oop;
    }

    private void increaseAllocTop(int offset) {
        allocTop = allocTop.add(offset);
        Assert.always(allocTop.loeq(allocEnd), "allocTop > allocEnd");
        Assert.always(allocTop.hieq(allocStart.add(SIZE_OF_BackingStore)), "allocTop < BS Header");
        spaceRemaining -= offset;
    }

    private void increaseAllocEnd(int offset) {
        allocEnd = allocEnd.add(offset);
        Assert.always(allocEnd.hieq(allocStart.add(SIZE_OF_BackingStore)), "allocEnd < BS Header");
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

    public boolean containAddr(Address addr) {
        return !(addr.loeq(allocStart) || addr.hi(allocEnd));
    }

    private boolean isLeaf() {
        return topTable == null;
    }

    private boolean isRoot() {
        return parent == null;
    }

    /**
     * If bs is the top child of this
     * 
     * @param bs
     * @return
     */
    private boolean topChildIs(BackingStore bs) {
        return !isLeaf() && topTable.top() == bs;
    }

    /**
     * Get the top child if there is one. Return null otherwise.
     * 
     * @return
     */
    private BackingStore getTopChild() {
        return isLeaf() ? null : topTable.top();
    }

    /**
     * 
     * @return the current BS
     */
    public static BackingStore getCurrentContext() {
        return currentAllocContext;
    }

    /**
     * 
     * @param bs
     *            the new BS
     * @return the old BS
     */
    public static BackingStore setCurrentContext(BackingStore bs) {
        if (SCJ_DEBUG_ENABLED) {
            VM.print("[SCJ] Set current allocation context from [");
            VM.print(currentAllocContext.id);
            VM.print("] to [");
            VM.print(bs.id);
            VM.println("]");
        }
        BackingStore old = currentAllocContext;
        disableScopeCheck();
        currentAllocContext = bs;
        enableScopeCheck();
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

    public static BackingStore getBackingStore(Object obj) {
        Address addr = Address.fromObject(obj);
        BackingStore ret = null;

        if (SCJ_DEBUG_ENABLED) {
            VM.print("[SCJ] search BS for address ");
            VM.printAddress(addr);
            VM.println("");
        }

        if (immortal.containAddr(addr))
            return immortal;
        if (scoped.containAddr(addr))
            ret = scoped.search(addr);

        // TODO: res should not be null because in SCJ the memory area
        // is either immortal or scoped. This is not true for Squawk since it
        // has ROM. So if we are getting here, simply return immortal as ROM is
        // treated as immortal as well.
        if (ret == null) {
            // Klass klassObj = GC.getKlass(obj);
            // Address addrObj = Address.fromObject(obj);
            String klassName = Klass.getInternalName(GC.getKlass(obj));
            VM.print("[SCJ] Attempt to search BS for in-ROM object of type ");
            VM.print(klassName);
            if (klassName == "com.sun.squawk.Klass") {
                VM.print(" - obj: ");
                VM.print(Klass.getInternalName((Klass) obj));
            }
            VM.println();
            ret = immortal;
        }

        if (SCJ_DEBUG_ENABLED) {
            VM.println("[SCJ] find in BS");
            ret.printInfo();
        }

        return ret;
    }

    private static int SIZE_OF_Monitor;
    private static int SIZE_OF_ObjectAssociation;

    /**
     * Get the upper bound of size of memory consumed for the object being
     * existing. This includes header, body, monitor, object association ...
     * TODO: what else??
     * 
     * @param clazz
     *            the object class if arrayLength == -1; the component class if
     *            arrayLength > -1
     * @param arrayLength
     *            the array length; -1 if not an array.
     * @return
     */
    public static int getConsumedMemorySize(Class clazz, int arrayLength) {
        int size;
        Klass klass = Klass.asKlass(clazz);
        if (arrayLength == -1) {
            size = (klass.getInstanceSize() * HDR.BYTES_PER_WORD) + HDR.basicHeaderSize;
        } else {
            size = GC.roundUpToWord(klass.getDataSize() * arrayLength + HDR.arrayHeaderSize);
        }
        size += SIZE_OF_Monitor;
        size += SIZE_OF_ObjectAssociation;
        return size;
    }

    // for debug
    private static String illegalAssignment;
    private static String ASSIGN;

    // for debug

    static void scopeCheckSlow(Address base, Address value) {
        if (scopeCheckEnabled > 0 && !getBackingStore(base.toObject()).containAddr(value)) {
            // TODO: scope check failed

            /*
             * Illegal assignment should lead to an error thrown. For debugging,
             * just prints and lets the program continue.
             */
            boolean throwException = false;
            if (throwException) {
                throw new IllegalAssignmentError();
            } else {
                VM.print(illegalAssignment);
                printAddrInfo(base);
                VM.print(ASSIGN);
                printAddrInfo(value);
                VM.println();
            }
        }
    }

    // for debug
    private static String leftBrackets;
    private static String atBS_;
    private static String ROM;
    private static String MAP;
    private static String KEY;
    private static String VAL;
    private static String com_sun_squawk_util_HashtableEntry;
    private static String com_sun_squawk_util_IntHashtableEntry;

    // for debug

    /**
     * klassName @ BS-id
     * 
     * @param klass
     * @param bs
     */
    private static void printAddrInfo(Address addr) {
        Object obj = addr.toObject();
        Klass klass = GC.getKlass(obj);
        Klass klass0 = klass;
        String name = Klass.getInternalName(klass);
        BackingStore bs = getBackingStore(obj);

        while (klass0.isArray()) {
            VM.print(leftBrackets);
            klass0 = klass0.getComponentType();
        }
        VM.print(Klass.getInternalName(klass0));
        VM.print(atBS_);
        if (bs == null)
            VM.print(ROM);
        else
            VM.print(bs.id);

        if (name == com_sun_squawk_util_HashtableEntry) {
            Object key = NativeUnsafe.getAddress(obj, 1).toObject();
            Object val = NativeUnsafe.getAddress(obj, 2).toObject();
            VM.println();
            VM.print(KEY);
            VM.print(Klass.getInternalName(GC.getKlass(key)));
            VM.print(MAP);
            VM.print(VAL);
            VM.print(Klass.getInternalName(GC.getKlass(val)));
        } else if (name == com_sun_squawk_util_IntHashtableEntry) {
            int key = NativeUnsafe.getInt(obj, 0);
            Object val = NativeUnsafe.getAddress(obj, 1).toObject();
            VM.println();
            VM.print(KEY);
            VM.print(key);
            VM.print(MAP);
            VM.print(VAL);
            VM.print(Klass.getInternalName(GC.getKlass(val)));
        }
    }

    private BackingStore search(Address addr) {
        if (SCJ_DEBUG_ENABLED) {
            VM.print("[SCJ] search in BS-");
            VM.println(id);
        }
        Assert.always(containAddr(addr));
        return isLeaf() ? this : topTable.search(addr);
    }

    /**
     * Convert a block of memory to backing store, where the BS instance locates
     * at the front of the block. Following fields will be set:
     * allocStart/Top/End, realSize, id.
     * 
     * @param start
     * @param end
     * @return
     */
    private static BackingStore convertToBS(Address start, Address end) {
        int allocSize = end.diff(start).toInt();
        Assert.that(allocSize >= SIZE_OF_BackingStore,
                "Memory too small to contain BackingStore instance itself!");

        Address oop = start.add(HDR.basicHeaderSize);

        NativeUnsafe.setObject(oop, HDR.klass, KLASS_OF_BackingStore);
        BackingStore bs = (BackingStore) oop.toObject();

        bs.allocStart = start;
        bs.allocTop = start.add(SIZE_OF_BackingStore);
        bs.allocEnd = end;
        bs.realSize = allocSize;
        bs.id = bsCounter++;

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
        if (isRoot())
            throw new Error("Immortal or Scoped cannot be destroyed");
        else if (!(parent.topChildIs(this))) {
            printBSTree(true);
            throw new Error("Only top child can be destroyed. Current is BS-" + this.id);
        }

        Address startZero;

        // If i'm the last one in topTable. The table can be reclaimed along
        // with myself.
        if (parent.topTable.counter == 1) {
            IndexTable oldTable = parent.topTable;
            disableScopeCheck();
            parent.topTable = oldTable.prev;
            enableScopeCheck();
            if (parent.topTable != null)
                parent.topTable.next = null;
            startZero = Address.fromObject(oldTable).sub(HDR.basicHeaderSize);
        } else {
            parent.topTable.pop();
            startZero = allocStart;
        }
        int incr = -allocEnd.diff(startZero).toInt();
        parent.increaseAllocTop(incr);
        VM.zeroWords(startZero, allocTop);

        // printBSTree(true);

    }

    /**
     * Destroy all backing stores above me (excluded).
     */
    public void destroyAllAboves() {
        // TODO: more efficient way: decrease top pointer and zero memory at
        // once
        if (!isRoot()) {
            while (!parent.topChildIs(this))
                parent.getTopChild().destroy();
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

        // set up search table chains
        disableScopeCheck();
        BackingStore oldBS = currentAllocContext;
        currentAllocContext = this;
        if (topTable == null)
            topTable = new IndexTable();
        else
            topTable.extendIfNeed();
        currentAllocContext = oldBS;
        enableScopeCheck();

        // check available space
        int allocSize = size + SIZE_OF_BackingStore;
        if (spaceRemaining < allocSize) {
            throw VM.getOutOfMemoryError();
        }

        // allocate memory by bumping the top pointer
        Address oldTop = allocTop;
        increaseAllocTop(allocSize);

        // convert the allocated memory block to backing store; bs is the first
        // object in the backing store.
        BackingStore bs = convertToBS(oldTop, allocTop);
        bs.size = size;
        bs.parent = this;
        bs.spaceRemaining = size;

        disableScopeCheck();
        topTable.put(bs);
        enableScopeCheck();

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
     * Empty and zero currently owned memory space. Restore the allocating top
     * pointer to its original value. If this is the top child of the parent,
     * takes all available space of its parent (which usually means the size
     * will grow).
     */
    public void reset() {
        if (SCJ_DEBUG_ENABLED) {
            VM.println("[SCJ] BackingStore.reset ... ");
            VM.println("[SCJ] == Before ==");
            printInfo();
        }

        Address oldTop = allocTop;
        increaseAllocTop(-oldTop.diff(allocStart.add(SIZE_OF_BackingStore)).toInt());
        VM.zeroWords(allocTop, oldTop);

        topTable = null;

        /* if this the top, take all parent's available space as mine. */
        if (!isRoot() && parent.getTopChild() == this) {
            int freeSpace = parent.spaceRemaining;
            parent.increaseAllocTop(freeSpace);
            increaseAllocEnd(freeSpace);
        }

        if (SCJ_DEBUG_ENABLED) {
            VM.println("[SCJ] == After ==");
            printInfo();
        }
    }

    public void setMirror(Object obj) {
        disableScopeCheck();
        mirror = obj;
        enableScopeCheck();
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
        // how much to shrink. Illegal if it is actually increase.
        int decr = size - newSize;
        if (decr < 0)
            throw new IllegalArgumentException();

        // Check if we have enough space to shrink
        int freeSpace = allocEnd.diff(allocTop).toInt();
        if (freeSpace < decr)
            throw new IllegalStateException();

        if (SCJ_DEBUG_ENABLED) {
            VM.println("[SCJ] BackingStore.shrink ... ");
            VM.println("[SCJ] == Before ==");
            printInfo();
        }

        Assert.always(!isRoot(), "Immortal or Scoped is not allowed to shrink");
        if (!(parent.getTopChild() == this))
            throw new Error("Only top scope is allowed to shrink");

        parent.increaseAllocTop(-decr);
        increaseAllocEnd(-decr);

        if (SCJ_DEBUG_ENABLED) {
            VM.println("[SCJ] == After ==");
            printInfo();
        }
    }

    // public static void preInitializeClassInSuite(Suite suite) {
    // if (BackingStore.SCJ_DEBUG_ENABLED) {
    // VM.print("[SCJ] initialize classes in suite: ");
    // VM.print(suite.getName());
    // VM.println(".suite");
    // }
    // for (int i = suite.getClassCount() - 1; i >= 0; i--) {
    // Klass klass = suite.getKlass(i);
    // // FIXME: we will have trouble finding native symbols when forcing
    // // initializing platform specific classes that are incompatible with
    // // current platform.
    // if (!klass.getName().startsWith("com.sun.squawk.platform")) {
    // if (BackingStore.SCJ_DEBUG_ENABLED)
    // VM.println(klass.getName());
    // klass.initializeInternal();
    // }
    // }
    // }

    public void printInfo() {
        VM.print("[SCJ] Backing store information of [BS-");
        VM.print(id);
        VM.println("]");
        VM.print("[SCJ] - size:            ");
        VM.println(size);
        VM.print("[SCJ] - realSize:        ");
        VM.println(realSize);
        VM.print("[SCJ] - remaining:       ");
        VM.println(spaceRemaining);
        VM.print("[SCJ] - allocStart:      ");
        VM.printAddress(allocStart);
        VM.println();
        VM.print("[SCJ] - allocTop:        ");
        VM.printAddress(allocTop);
        VM.println();
        VM.print("[SCJ] - allocEnd:        ");
        VM.printAddress(allocEnd);
        VM.println();
        VM.print("[SCJ] - parent:          ");
        if (isRoot())
            VM.println("null");
        else {
            VM.print("BS-");
            VM.println(parent.id);
        }
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
        VM.print("BS-");
        VM.print(id);
        if (!isRoot() && parent.getTopChild() == this)
            VM.print("*");
        if (detailed) {
            VM.print("    [ ");
            VM.printAddress(allocStart);
            VM.print(" - ");
            VM.printAddress(allocTop);
            VM.print(" - ");
            VM.printAddress(allocEnd);
            VM.print(" ]   SIZE: ");
            VM.print(realSize);
            VM.print("   USED: ");
            VM.print(realSize - spaceRemaining);
            VM.print("   REMAINS: ");
            VM.print(spaceRemaining);
        }
        VM.println();
        IndexTable table = topTable;
        while (table != null && table.prev != null)
            table = table.prev;
        while (table != null) {
            for (int i = 0; i < table.counter; i++)
                table.bsArray[i].printBSTreeRecursive(detailed, indent + 1);
            table = table.next;
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

    public static void printCurrentBSStats() {
        VM.print(" **************** Instance in BS-");
        VM.print(currentAllocContext.id);
        VM.println(" ****************");
        GC.printHeapStats(currentAllocContext, true);
        VM.println(" ************************************************");
    }
}
