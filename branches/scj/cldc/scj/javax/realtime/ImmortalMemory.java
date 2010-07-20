package javax.realtime;

import com.sun.squawk.BackingStore;

//@SCJAllowed
public final class ImmortalMemory extends MemoryArea {

    private static ImmortalMemory instance;

    private ImmortalMemory() {
        super(BackingStore.getImmortal());
    }

    // @SCJAllowed
    public static ImmortalMemory instance() {
        if (instance == null)
            instance = new ImmortalMemory();
        return instance;
    }
}
