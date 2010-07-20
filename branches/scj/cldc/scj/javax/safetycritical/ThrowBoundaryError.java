package javax.safetycritical;

//@SCJAllowed
public class ThrowBoundaryError {

    // @SCJAllowed
    public ThrowBoundaryError() {
    }

    // @SCJAllowed
    public String getPropagatedMessage() {
        return null;
    }

    // @SCJAllowed
    public StackTraceElement[] getPropagatedStackTrace() {
        return null;
    }

    // @SCJAllowed
    public int getPropagatedStackTraceDepth() {
        return 0;
    }

    // @SCJAllowed
    public Class getPropagatedExceptionClass() {
        return null;
    }
}
