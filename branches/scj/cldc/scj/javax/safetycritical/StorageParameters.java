package javax.safetycritical;


//@SCJAllowed
public class StorageParameters {

    long totalBackingStore;
    long nativeStackSize;
    long javaStackSize;
    int messageLength;
    int stackTraceLength;

    /**
     * Stack sizes for schedulable objects and sequencers. Passed as parameter
     * to the constructor of mission sequencers and schedulable objects.
     * 
     * TBD: kelvin changed nativeStack and javaStack to long. Note that
     * getJavaStackSize() and getNativeStackSize() methods were already declared
     * to return long. It seems that we have an implicit assumption that memory
     * sizes are represented by long. do others agree with this change?
     * 
     * @param totalBackingStore
     *            size of the backing store reservation for worst-case scope
     *            usage in bytes
     * @param nativeStackSize
     *            size of native stack in bytes (vendor specific)
     * @param javaStackSize
     *            size of Java execution stack in bytes (vendor specific)
     */
    // @SCJAllowed
    public StorageParameters(long totalBackingStore, long nativeStackSize,
            long javaStackSize) {
        this(totalBackingStore, nativeStackSize, javaStackSize, -1, -1);
    }

    /**
     * Stack sizes for schedulable objects and sequencers. Passed as parameter
     * to the constructor of mission sequencers and schedulable objects.
     * 
     * TBD: kelvin changed nativeStack and javaStack to long. Note that
     * getJavaStackSize() and getNativeStackSize() methods were already declared
     * to return long. It seems that we have an implicit assumption that memory
     * sizes are represented by long. do others agree with this change?
     * 
     * @param totalBackingStore
     *            size of the backing store reservation for worst-case scope
     *            usage in bytes
     * 
     * @param nativeStack
     *            size of native stack in bytes (vendor specific)
     * 
     * @param javaStack
     *            size of Java execution stack in bytes (vendor specific)
     * 
     * @param messageLength
     *            length of the space in bytes dedicated to message associated
     *            with this Schedulable object's ThrowBoundaryError exception
     *            plus all the method names/identifiers in the stack backtrace
     * 
     * @param stackTraceLength
     *            the number of byte for the StackTraceElement array dedicated
     *            to stack backtrace associated with this Schedulable object's
     *            ThrowBoundaryError exception.
     */
    // @SCJAllowed
    public StorageParameters(long totalBackingStore, long nativeStackSize,
            long javaStackSize, int messageLength, int stackTraceLength) {
        // TODO: check legality of the parameters
        this.totalBackingStore = totalBackingStore;
        this.nativeStackSize = nativeStackSize;
        this.javaStackSize = javaStackSize;
        this.messageLength = messageLength;
        this.stackTraceLength = stackTraceLength;

    }

    /**
     * 
     * @return the size of the total backing store available for scoped memory
     *         areas created by the assocated SO.
     */
    // @SCJAllowed
    public long getTotalBackingStoreSize() {
        return totalBackingStore;
    }

    /**
     * 
     * @return the size of the native method stack available to the assocated
     *         SO.
     */
    // @SCJAllowed
    public long getNativeStackSize() {
        return nativeStackSize;
    }

    /**
     * 
     * @return the size of the Java stack available to the assocated SO.
     */
    // @SCJAllowed
    public long getJavaStackSize() {
        return javaStackSize;
    }

    /**
     * 
     * return the length of the message buffer
     */
    // @SCJAllowed
    public int getMessageLength() {
        return messageLength;
    }

    /**
     * 
     * return the length of the stack trace buffer
     */
    // @SCJAllowed
    public int getStackTraceLength() {
        return stackTraceLength;
    }
}
