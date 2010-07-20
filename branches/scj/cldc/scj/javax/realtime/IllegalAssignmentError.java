package javax.realtime;

import java.io.Serializable;

//@SCJAllowed
public class IllegalAssignmentError extends Error implements Serializable {

    /**
     * Shall not copy "this" to any instance or static field.
     * <p>
     * Allocates an application- and implementation-dependent amount of memory
     * in the current scope (to represent stack backtrace).
     */

    // @SCJAllowed
    public IllegalAssignmentError() {
    }

    /**
     * Shall not copy "this" to any instance or static field. The scope
     * containing the msg argument must enclose the scope containing "this".
     * Otherwise, an IllegalAssignmentError will be thrown.
     * <p>
     * Allocates an application- and implementation-dependent amount of memory
     * in the current scope (to represent stack backtrace).
     */

    // @SCJAllowed
    public IllegalAssignmentError(String description) {
        super(description);
    }
}
