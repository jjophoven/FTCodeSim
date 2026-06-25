package android.util;

public class Log {
    public static int println(int priority, String tag, String msg) {
        System.out.printf("[%s] %s%n", tag, msg);
        return 0;
    }

    public static int v(String tag, String msg) {
        return println(2, tag, msg);
    }

    public static int d(String tag, String msg) {
        return println(3, tag, msg);
    }

    public static int i(String tag, String msg) {
        return println(4, tag, msg);
    }

    public static int w(String tag, String msg) {
        return println(5, tag, msg);
    }

    public static int e(String tag, String msg) {
        return println(6, tag, msg);
    }

    public static String getStackTraceString(Throwable t) {
        return java.util.Arrays.toString(t.getStackTrace());
    }
}