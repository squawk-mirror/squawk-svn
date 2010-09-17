package com.sun.squawk;

import javax.realtime.IllegalAssignmentError;

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
            bsArray = new BackingStore[INDEXTABLE_CAPACITY];
        }

        private BackingStore top() {
            Assert.always(counter <= INDEXTABLE_CAPACITY, "Search table is over full");
            return bsArray[counter - 1];
        }

        private void pop() {
            Assert.always(counter != 0, "Attempt to pop empty table");
            bsArray[--counter] = null;
        }

        private void put(BackingStore bs) {
            Assert.always(counter != INDEXTABLE_CAPACITY,
                    "Attempt to put item in a full searching table ");
            bsArray[counter++] = bs;
        }

        /**
         * Search for and return the leaf BS that encloses the target address.
         * The caller must ensure that the target address actually falls in an
         * index table on the chain that current table is on. If the method
         * cannot find anything, there must be something wrong.
         * 
         * @param addr
         *            the target address
         * @return the enclosing BS
         */
        private BackingStore search(Address addr) {
            /*
             * If the address is not in the range of current table, go for the
             * previous or next table depending on the address is lower or
             * higher than the boundary.
             * 
             * The table instance precedes all the BSs it indexes in terms of
             * address. So if the target address is lower than "this", it is
             * definitely not in the BSs of this table. Go checking with the
             * previous table if there is one.
             */
            if (addr.lo(Address.fromObject(this))) {
                Assert.always(prev != null,
                        "Unable to locate the BS of an address: lower than lower bound.");
                return prev.search(addr);
            } else if (isEmpty()) {
                // VM.print("Unable to locate the BS for an address: empty table: ");
                // VM.printAddress(addr);
                // VM.println();
                // BackingStore.printBSTree(true);
                Assert.always(false, "Unable to locate the BS of an address: empty table.");
            } else if (bsArray[counter - 1].allocEnd.loeq(addr)) {
                // if (next == null) {
                // VM.print("Unable to locate the BS for an address: higher than higher bound: ");
                // VM.printAddress(addr);
                // VM.println();
                // BackingStore.printBSTree(true);
                // }
                Assert.always(next != null,
                        "Unable to locate the BS of an address: higher than higher bound.");
                return next.search(addr);
            }

            // The address is fallen in this range, do the binary search.
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

        /**
         * If the table if full, create a new one and connect it to the tail of
         * the chain.
         */
        private void extendIfNeed() {
            if (counter == INDEXTABLE_CAPACITY) {
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
     * debug
     */
    public static boolean SCJ_DEBUG_ENABLED;
    public static boolean SCJ_DEBUG_ALLOC;

    /** The size of BS instance rounded up to words */
    private static int SIZE_OF_BackingStore;

    /** Index table capacity rounded up to words */
    private static int INDEXTABLE_CAPACITY;

    /** Start of immortal memory. */
    private static Address immortalStart;

    /** End of immortal memory. */
    private static Address immortalEnd;

    /** Start of scoped memory area. */
    private static Address scopedStart;

    /** End of scoped memory area. */
    private static Address scopedEnd;

    /** The backing store of immortal memory. */
    private static BackingStore immortal;

    /** The backing store of scoped memories. */
    private static BackingStore scoped;

    /** The Klass instance of BackingStore */
    private static Klass KLASS_OF_BackingStore;

    /**
     * The global current allocate context. The thread getting rescheduled in
     * must set this field with its saved context.
     */
    private static volatile BackingStore currentAllocContext;

    /** The number of backing stores that have ever been created. */
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

    /** Start of this backing store. */
    private Address allocStart;

    /** Start of free space. */
    private Address allocTop;

    /** End of this backing store */
    private Address allocEnd;

    /** The enclosing backing store. */
    private BackingStore parent;

    /**
     * The associated MemoryArea object. The BSs only have mirrors if they are
     * leaves, that is, they have no children.
     */
    private Object mirror;

    /** The backing store id. */
    private int id;

    /** The user specified size. */
    private int size;

    /**
     * The real size of memory allocated. realSize == roundUpToWord(size) +
     * HEADER_PLUS_INSTANCE_SIZE.
     */
    private int realSize;

    /** Size of the space remained. */
    private int spaceRemaining;

    /** The tail of the table chain of this backing store. */
    private IndexTable topTable;

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

        SCJ_DEBUG_ENABLED = false;
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
        // end

        INDEXTABLE_CAPACITY = 2;

        bsCounter = 0;

        immortal = convertToBS(immortalStart, immortalEnd);
        immortal.size = immortal.realSize - SIZE_OF_BackingStore;
        immortal.spaceRemaining = immortal.size;
        GC.getKlass(immortal);

        scoped = convertToBS(scopedStart, scopedEnd);
        scoped.size = scoped.realSize - SIZE_OF_BackingStore;
        scoped.spaceRemaining = scoped.size;

        currentAllocContext = immortal;

        scopeCheckEnabled = 1;

        if (SCJ_DEBUG_ENABLED) {
            VM.println("[SCJ] Initialize immortal and scoped backingStore ");
            immortal.printInfo();
            scoped.printInfo();
        }
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
     * Allocate a chunk of memory in current backing store and set up the object
     * header.
     */
    public static Object allocate(int size, Object klass, int arrayLength) {
        Assert.always(currentAllocContext != null, "Current allocation context cannot be null!");
        return currentAllocContext.allocate0(size, klass, arrayLength);
    }

    /**
     * Allocate a chunk of memory in this backing store. Note that allocation is
     * not in charge of zeroing memory. It is done in initializing the entire
     * memory and in destroying a backing store.
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
        // Set up the header.
        if (arrayLength == -1) {
            oop = allocTop.add(HDR.basicHeaderSize);
            GC.setHeaderClass(oop, klass);
        } else {
            oop = allocTop.add(HDR.arrayHeaderSize);
            GC.setHeaderClass(oop, klass);
            GC.setHeaderLength(oop, arrayLength);
            Assert.always(GC.getArrayLength(oop) == arrayLength);
        }

        // bump the top pointer
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

    /**
     * Bump the pointer and adjust the related variables.
     * 
     * @param offset
     *            the amount of bump by
     */
    private void increaseAllocTop(int offset) {
        allocTop = allocTop.add(offset);
        Assert.always(allocTop.loeq(allocEnd), "allocTop > allocEnd");
        Assert.always(allocTop.hieq(allocStart.add(SIZE_OF_BackingStore)), "allocTop < BS Header");
        spaceRemaining -= offset;
    }

    /**
     * Extend the high boundary of this backing store and adjust the related
     * variables.
     * 
     * @param offset
     *            the amount to extend by
     */
    private void increaseAllocEnd(int offset) {
        allocEnd = allocEnd.add(offset);
        Assert.always(allocEnd.hieq(allocStart.add(SIZE_OF_BackingStore)), "allocEnd < BS Header");
        size += offset;
        realSize += offset;
        spaceRemaining += offset;
    }

    /** Check if the address is in the range of Immortal. */
    public static boolean inImmortal(Address addr) {
        return !(addr.loeq(immortalStart) || addr.hi(immortalEnd));
    }

    /** Check if the address is in the range of Scoped. */
    public static boolean inScoped(Address addr) {
        return !(addr.loeq(scopedStart) || addr.hi(scopedEnd));
    }

    /** Check if the address is in the range of this backing store. */
    public boolean containAddr(Address addr) {
        return !(addr.loeq(allocStart) || addr.hi(allocEnd));
    }

    /** Check if this backing store is leaf (no child). */
    private boolean isLeaf() {
        return topTable == null;
    }

    /** Check if this backing store is root (no parent). */
    private boolean isRoot() {
        return parent == null;
    }

    /** Check if the backing store is the top child of this. */
    private boolean topChildIs(BackingStore bs) {
        return !isLeaf() && topTable.top() == bs;
    }

    /** Get the top child. Return null if there is none. */
    private BackingStore getTopChild() {
        return isLeaf() ? null : topTable.top();
    }

    /** Get the current backing store. */
    public static BackingStore getCurrentContext() {
        return currentAllocContext;
    }

    /** Set the current backing store and return the old one. */
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

    /** Get the Immortal space. The size should be specified by users. */
    public static BackingStore getImmortal() {
        return immortal;
    }

    /** Get the Scoped space. The size should be specified by users. */
    public static BackingStore getScoped() {
        return scoped;
    }

    /** Return the backing store where the object is allocated in. */
    public static BackingStore getBackingStore(Object obj) {
        Address addr = Address.fromObject(obj);

        if (SCJ_DEBUG_ENABLED) {
            VM.print("[SCJ] search BS for address ");
            VM.printAddress(addr);
            VM.println("");
        }

        if (immortal.containAddr(addr))
            return immortal;

        BackingStore ret = null;
        if (scoped.containAddr(addr))
            ret = scoped.search(addr);

        // TODO: ret should not be null because objects must be in either
        // Immortal or Scoped. However, since Squawk has ROM, we can be sure
        // that if the ret is null, the object we are looking at is definitely
        // in ROM. And it is safe to treat ROM as part of immortal memory.
        if (ret == null) {
            String klassName = Klass.getInternalName(GC.getKlass(obj));
            if (SCJ_DEBUG_ENABLED) {
                VM.print("[SCJ] Attempt to search for BS for in-ROM object of type ");
                VM.print(klassName);
                if (klassName == "com.sun.squawk.Klass") {
                    VM.print(" - obj: ");
                    VM.print(Klass.getInternalName((Klass) obj));
                }
                VM.println();
            }
            ret = immortal;
        }

        if (SCJ_DEBUG_ENABLED) {
            VM.println("[SCJ] find in BS");
            ret.printInfo();
        }

        return ret;
    }

    /**
     * Search for the leaf BS enclosing given address. The caller must make sure
     * the address is in the range of this BS.
     */
    private BackingStore search(Address addr) {
        if (SCJ_DEBUG_ENABLED) {
            VM.print("[SCJ] search in BS-");
            VM.println(id);
        }
        Assert.always(containAddr(addr));
        return isLeaf() ? this : topTable.search(addr);
    }

    private static int SIZE_OF_Monitor;
    private static int SIZE_OF_ObjectAssociation;

    /**
     * Get the size of the maximum memory that can be potentially consumed for
     * the object being existing. This includes header + body + (monitor |
     * object association) + what else?
     * 
     * This method is used by javax.realtime.SizeEstimator for estimating the
     * upper bound size of a new scope.
     * 
     * TODO: not sure if the current implementation is reasonable.
     * 
     * @param clazz
     *            the object class if arrayLength == -1; the component class if
     *            arrayLength > -1
     * @param arrayLength
     *            the array length; -1 if not an array.
     * @return the upper bound size
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
    private static String leftBrackets;
    private static String atBS_;
    private static String ROM;
    private static String MAP;
    private static String KEY;
    private static String VAL;
    private static String com_sun_squawk_util_HashtableEntry;
    private static String com_sun_squawk_util_IntHashtableEntry;

    // for debug

    static void scopeCheckSlow(Address base, Address value) {
        if (scopeCheckEnabled > 0 && !getBackingStore(base.toObject()).containAddr(value)) {
            /*
             * Illegal assignment should lead to an error thrown. But for debug
             * purpose, just prints and lets the program continue.
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

    /**
     * Print the information given an object pointer. If the object is a
     * HashTable, the klasses of keys and values will also be printed.
     * 
     * Format: klassName @ BS-id
     * 
     * key: keyKlass -> val: valKlass
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

    /**
     * Convert a block of memory to backing store unit, where the BS instance
     * locates at the front of the block. Following fields will be set:
     * allocStart/Top/End, realSize, id.
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

        if (SCJ_DEBUG_ENABLED) {
            VM.println("[SCJ] Convert raw memory to backing store");
            VM.print("[SCJ]  - start: ");
            VM.printAddress(bs.allocStart);
            VM.println("");
            VM.print("[SCJ]  - top:   ");
            VM.printAddress(bs.allocTop);
            VM.println("");
            VM.print("[SCJ]  - end:   ");
            VM.printAddress(bs.allocEnd);
            VM.println("");
            VM.print("[SCJ]  - size:  ");
            VM.println(bs.realSize);
        }

        return bs;
    }

    public int bsConsumed() {
        return size - spaceRemaining;
    }

    public int bsRemaining() {
        return spaceRemaining;
    }

    /**
     * Destroy this backing store unit. Pop myself from container and zero the
     * memory owned (the instance of "this" is erased at the same time).
     * 
     * NOTE: the caller must make sure "this" is
     * 
     * 1) not Immortal or Scoped; 2) the top child of its parent.
     */
    public void destroy() {
        if (isRoot())
            throw new Error("Immortal or Scoped cannot be destroyed");
        else if (!(parent.topChildIs(this))) {
            printBSTree(true);
            throw new Error("Only top child can be destroyed. Current is BS-" + this.id);
        }

        Address startZero;

        // If i'm the last one in topTable, the table becomes empty after i am
        // destroyed, so can be reclaimed as well. Erase the table instance and
        // zero the memory.
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
        // after the following line, "this" object is gone.
        VM.zeroWords(startZero, allocTop);
    }

    /**
     * Destroy all BSs that have the same parent with me and are positioned
     * above me (myself excluded).
     */
    public void destroyAllAboves() {
        // TODO: more efficient way: decrease the parent's top pointer and zero
        // memory at once
        if (!isRoot()) {
            while (!parent.topChildIs(this))
                parent.getTopChild().destroy();
        }
    }

    /**
     * Allocate memory for a new child in current backing store space. The new
     * instance will be set with its: size, spaceRemaining, container.
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

    public Object getMirror() {
        return mirror;
    }

    public int getSize() {
        return size;
    }

    public int getRealSize() {
        return realSize;
    }

    /**
     * Empty and zero currently owned memory space. Restore the allocating top
     * pointer to its origin. If "this" is the top child of the parent, takes
     * all available space of its parent as mine (which usually means the size
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

        /* if this is the top, take all parent's available space as mine. */
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
     * Resize the BS to a smaller size.
     * 
     * This function is specially designed for mission memory. Creating mission
     * memory is special in that before it is created the VM will not be able to
     * know how large it should be (because the mission instance should be
     * created in the mission memory while the size of the mission memory is
     * gotten by mission.missionMemorySize()). A way to solve is to first making
     * the mission memory as large as possible then shrinking it to the right
     * size after we are able to get the information. In sum, we are shrinking
     * the mission memory when active in it.
     * 
     * The caller of this function must guarantee that 1) the new size cannot be
     * smaller than the already used space ; 2) "this" must be the top child.
     * 
     * @throws IllegalStateException
     *             if 1) is violated
     * 
     * @throws IllegalArgumentException
     *             if newSize is larger than the current size.
     */
    public void shrink(int newSize) throws IllegalStateException, IllegalArgumentException {
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

    /** Print the BS tree structure representing current memory usage. */
    public static void printBSTree(boolean detailed) {
        VM.println();
        VM.println("[SCJ] print entire backing store tree ---------------------------- start ");
        immortal.printBSTreeRecursive(detailed, 0);
        scoped.printBSTreeRecursive(detailed, 0);
        VM.println("[SCJ] print entire backing store tree ---------------------------- end ");
        VM.println();
    }

    /** A helper */
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

    /** A helper */
    private static void printSpace(int indent) {
        VM.print("[SCJ] ");
        for (int i = 0; i < indent; i++)
            VM.print("    ");
    }

    /** Print all instances in current BS */
    public static void printCurrentBSStats() {
        VM.print(" **************** Instance in BS-");
        VM.print(currentAllocContext.id);
        VM.println(" ****************");
        GC.printHeapStats(currentAllocContext, true);
        VM.println(" ************************************************");
    }

    /* For testing ... */
    // private static void test() {
    // float ff = 0.1234f;
    // double dd = 0.1234;
    // int[] a = new int[] { 222, 333, 444 };
    // double[] f = new double[] { 0.222, 0.333, 0.444 };
    // Dummy d = new Dummy();
    // boolean r = d._int == 12345 && d._char == 'j' && d._long == 948472349 &&
    // d._boolean == true
    // && d._float == 0.12345 && d._double == 3.1415926 && a[0] == 222 && a[1]
    // == 333
    // && a[2] == 444;
    // if (!r) {
    // VM.println("------------ Error in allocation ------------");
    // VM.println(d._int);
    // VM.println(d._char);
    // VM.println(d._long);
    // VM.println(d._boolean);
    // VM.println(d._float);
    // VM.println(d._double);
    // VM.println(a[0]);
    // VM.println(a[1]);
    // VM.println(a[2]);
    // VM.println(f[0]);
    // VM.println(f[1]);
    // VM.println(f[2]);
    // VM.println(ff);
    // VM.println(dd);
    // VM.println(a.length);
    // Assert.always(r);
    // }
    // }
    //
    // private static boolean verifyZero(Address start, Address end) {
    // VM.print("[SCJ] start verify memory [");
    // VM.printAddress(start);
    // VM.print(" - ");
    // VM.printAddress(end);
    // VM.println("]");
    // boolean res = true;
    // // boolean open = true;
    // int range = end.diff(start).toInt();
    // for (int i = 0; i < range; i++) {
    // // if (open) {
    // if (NativeUnsafe.getByte(start, i) != 0) {
    // res = false;
    // // VM.print("[");
    // // VM.print(start.add(i));
    // // VM.print(" - ");
    // // open = false;
    // }
    // // } else {
    // // if (Unsafe.getInt(start, i) == 0) {
    // // VM.print(start.add(i));
    // // VM.print("] ");
    // // open = true;
    // // }
    // // }
    // }
    // return res;
    // }
    //
    // static class Dummy {
    // int _int = 12345;
    // float _float = 0.12345f;
    // char _char = 'j';
    // boolean _boolean = true;
    // double _double = 3.1415926;
    // long _long = 948472349;
    // }
}
