package javax.realtime;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

//@SCJAllowed
public interface AllocationContext {

    // @SCJAllowed
    public void enter(Runnable logic);

    // @SCJAllowed
    public void executeInArea(Runnable logic);

    // @SCJAllowed
    public long memoryConsumed();

    // @SCJAllowed
    public long memoryRemaining();

    // @SCJAllowed
    public Object newArray(Class type, int number) throws IllegalArgumentException,
            OutOfMemoryError;

    // @SCJAllowed
    public Object newInstance(Class type) throws IllegalAccessException, InstantiationException,
            OutOfMemoryError;

    // @SCJAllowed
    public Object newInstance(Constructor c, Object[] args) throws IllegalAccessException,
            InstantiationException, OutOfMemoryError, InvocationTargetException;

    // @SCJAllowed
    public long size();
}
