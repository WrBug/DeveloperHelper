/*  NetBare - An android network capture and injection library.
 *  Copyright (C) 2018-2019 Megatron King
 *  Copyright (C) 2018-2019 GuoShi
 *
 *  NetBare is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU General Public License as published by the Free Software Found-
 *  ation, either version 3 of the License, or (at your option) any later version.
 *
 *  NetBare is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 *  PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with NetBare.
 *  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.megatronking.netbare;

import android.util.Log;

/**
 * A static log util using in NetBare, and the tag is 'NetBare';
 *
 * @author Megatron King
 * @since 2018-10-08 23:12
 */
public final class NetBareLog {

    private static final String TAG = "NetBare";

    private static boolean sDebug;

    private NetBareLog() {
    }

    /* package */ static void setDebug(boolean debug) {
        sDebug = debug;
    }

    /**
     * Print a verbose level log in console.
     *
     * @param msg The message you would like logged.
     */
    public static void v(String msg) {
        if (!sDebug || msg == null) {
            return;
        }
        Log.v(TAG, msg);
    }

    /**
     * Print a verbose level log in console.
     *
     * @param msg The message you would like logged.
     * @param args Arguments referenced by the format specifiers in the format string.
     */
    public static void v(String msg, Object... args) {
        v(format(msg, args));
    }

    /**
     * Print a debug level log in console.
     *
     * @param msg The message you would like logged.
     */
    public static void d(String msg) {
        if (!sDebug || msg == null) {
            return;
        }
        Log.d(TAG, msg);
    }

    /**
     * Print a debug level log in console.
     *
     * @param msg The message you would like logged.
     * @param args Arguments referenced by the format specifiers in the format string.
     */
    public static void d(String msg, Object... args) {
        d(format(msg, args));
    }

    /**
     * Print a info level log in console.
     *
     * @param msg The message you would like logged.
     */
    public static void i(String msg) {
        if (!sDebug || msg == null) {
            return;
        }
        Log.i(TAG, msg);
    }

    /**
     * Print a info level log in console.
     *
     * @param msg The message you would like logged.
     * @param args Arguments referenced by the format specifiers in the format string.
     */
    public static void i(String msg, Object... args) {
        i(format(msg, args));
    }

    /**
     * Print a error level log in console.
     *
     * @param msg The message you would like logged.
     */
    public static void e(String msg) {
        if (!sDebug || msg == null) {
            return;
        }
        Log.e(TAG, msg);
    }

    /**
     * Print a error level log in console.
     *
     * @param msg The message you would like logged.
     * @param args Arguments referenced by the format specifiers in the format string.
     */
    public static void e(String msg, Object... args) {
        e(format(msg, args));
    }

    /**
     * Print a warning level log in console.
     *
     * @param msg The message you would like logged.
     */
    public static void w(String msg) {
        if (!sDebug || msg == null) {
            return;
        }
        Log.w(TAG, msg);
    }

    /**
     * Print a warning level log in console.
     *
     * @param msg The message you would like logged.
     * @param args Arguments referenced by the format specifiers in the format string.
     */
    public static void w(String msg, Object... args) {
        w(format(msg, args));
    }

    /**
     * Print a fatal level log in console.
     *
     * @param throwable The error you would like logged.
     */
    public static void wtf(Throwable throwable) {
        if (!sDebug || throwable == null) {
            return;
        }
        Log.wtf(TAG, throwable);
    }

    private static String format(String format, Object... objs) {
        if (objs == null || objs.length == 0) {
            return format;
        } else {
            return String.format(format, objs);
        }
    }

}
