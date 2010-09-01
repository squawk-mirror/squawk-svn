package javax.safetycritical.util;

public class Utils {

    public static boolean DEBUG = false;

    public static void unimplemented() {
        throw new MethodUnimplementedException();
    }
}
