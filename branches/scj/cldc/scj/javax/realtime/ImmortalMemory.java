package javax.realtime;

import com.sun.squawk.BackingStore;
import com.sun.squawk.util.Assert;

//@SCJAllowed
public final class ImmortalMemory extends MemoryArea {

    private static ImmortalMemory instance = new ImmortalMemory();

    private ImmortalMemory() {
        super(BackingStore.getImmortal());
    }

    // @SCJAllowed
    public static ImmortalMemory instance() {
        Assert.that(instance != null);
        return instance;
    }
}
