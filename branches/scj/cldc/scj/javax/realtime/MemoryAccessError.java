package javax.realtime;

import java.io.Serializable;

//@SCJAllowed
public class MemoryAccessError extends RuntimeException implements Serializable {

    // @SCJAllowed
    public MemoryAccessError() {
    }

    // @SCJAllowed
    public MemoryAccessError(String description) {
        super(description);
    }
}
