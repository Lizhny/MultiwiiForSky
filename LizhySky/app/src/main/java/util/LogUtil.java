package util;

import android.util.Log;

/**
 * 日志工具
 * <p/>
 * Created by lizhy on 2016/3/24.
 */
public class LogUtil {
    public static final boolean D = false;
    public static final boolean E = false;
    public static final boolean I = false;

    public static void d(String tag, String msg) {
        if (D) Log.d(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (E) Log.e(tag, msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (E) Log.e(tag, msg, tr);
    }

    public static void i(String tag, String msg) {
        if (I)
            Log.i(tag, msg);
    }
}
