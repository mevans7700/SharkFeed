package com.evansappwriter.sharkfeed.util;

import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;

/**
 * u know what this is
 */
public class Utils {
    private static final String TAG = "UTILS";

    public static void setStrictMode(final boolean enable) {
        // noinspection ConstantConditions

        doSetStrictMode(enable);

        // fix for http://code.google.com/p/android/issues/detail?id=35298
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // restore strict mode after onCreate() returns.
            new Handler().postAtFrontOfQueue(new Runnable() {
                @Override
                public void run() {
                    doSetStrictMode(enable);
                }
            });
        }
    }

    private static void doSetStrictMode(boolean enable) {
        if (enable) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build()); // .penaltyDialog()
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
        } else {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().penaltyLog().build());
        }
    }

    /**
     * Prints out logging info.<br />
     * This should be used instead of Log methods.
     *
     * @param txt List of texts to append.
     */
    public static void printLogInfo(String tag, Object... txt) {
        // noinspection ConstantConditions
        if (txt == null) {
            return;
        }

        int count = txt.length;

        if (count == 1) {
            Log.i(tag, txt[0] == null ? "null" : String.valueOf(txt[0]));
        } else { // count > 1
            StringBuilder sb = new StringBuilder(50 * count);

            for (Object aTxt : txt) {
                sb.append(aTxt == null ? "null" : aTxt);
            }

            Log.i(tag, sb.toString());
        }
    }

    /**
     * Prints out exception stack traces.<br />
     * This should be used instead of directly calling e.printStackTrace().
     */
    public static void printStackTrace(Throwable e, Object... txt) {
        // noinspection ConstantConditions
        if (txt != null) {
            if (e == null) {
                Log.e(TAG, "Null exception. Hmm...");
                return;
            }

            Log.e(TAG, Log.getStackTraceString(e));
            // e.printStackTrace();
        }
    }
}
