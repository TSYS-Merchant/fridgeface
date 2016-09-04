
package com.fridgeface.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import android.os.DropBoxManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * The better way to log. This is a wrapper class for android.util.Log that contains additional methods
 * for improved logging. Use the init method to initialize the LogHelper settings.
 */
public class LogHelper {
    private static boolean sIsDebugBuild = true;
    private static boolean sUseDivider = false;
    private static String sLogTag = "";

    // ADT 20 wraps lines but has length limit, so this is still needed
    private static boolean sWrapLongLines = true;

    /*
     * set to ADT line length limit to allow normal ADT wrapping but force anything longer to the next
     * log entry
     */
    private static int sMaxLineWidth = 4000;

    /**
     * Initializes LogHelper settings.
     *
     * @param isDebugBuild - set to true to enable log messages, false to disable log messages
     */
    public static void init(boolean isDebugBuild) {
        sIsDebugBuild = isDebugBuild;
    }

    /**
     * Initializes LogHelper settings.
     *
     * @param isDebugBuild - set to true to enable log messages, false to disable log messages
     * @param logTag - the tag to use for logging
     */
    public static void init(boolean isDebugBuild, String logTag) {
        sLogTag = logTag;
    }

    /**
     * Initializes LogHelper settings (dividers and line wrapping only occur when using the print
     * method).
     *
     * @param isDebugBuild - set to true to enable log messages, false to disable log messages
     * @param useDivider - set to true to show dividers, false to hide dividers
     * @param wrapLongLines - set to true to wrap lines that are longer than the maxLineWidth
     * @param maxLineWidth - the maximum line length before wrapping occurs
     */
    public static void init(boolean isDebugBuild, boolean useDivider, boolean wrapLongLines,
            int maxLineWidth) {

        sIsDebugBuild = isDebugBuild;
        sUseDivider = useDivider;
        sWrapLongLines = wrapLongLines;
        sMaxLineWidth = maxLineWidth;
    }

    /**
     * Initializes LogHelper settings (dividers and line wrapping only occur when using the print
     * method).
     *
     * @param isDebugBuild - set to true to enable log messages, false to disable log messages
     * @param useDivider - set to true to show dividers, false to hide dividers
     * @param wrapLongLines - set to true to wrap lines that are longer than the maxLineWidth
     * @param maxLineWidth - the maximum line length before wrapping occurs
     * @param logTag - the tag to use for logging
     */
    public static void init(boolean isDebugBuild, boolean useDivider, boolean wrapLongLines,
            int maxLineWidth, String logTag) {

        sIsDebugBuild = isDebugBuild;
        sUseDivider = useDivider;
        sWrapLongLines = wrapLongLines;
        sMaxLineWidth = maxLineWidth;
        sLogTag = logTag;
    }

    /**
     * Send a DEBUG log message. If the log tag was not set with the init method, the calling class and
     * line number are set as the tag.
     *
     * @param message - The message you would like logged
     */
    public static void d(String message) {
        d(getDefaultTag(), message);
    }

    /**
     * Send a DEBUG log message.
     *
     * @param tag - Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs
     * @param message - The message you would like logged
     */
    public static void d(String tag, String message) {
        if (sIsDebugBuild) {
            Log.d(tag, message.toString());
        }
    }

    /**
     * Send a ERROR log message. If the log tag was not set with the init method, the calling class and
     * line number are set as the tag.
     *
     * @param message - The message you would like logged
     */
    public static void e(String message) {
        e(getDefaultTag(), message);
    }

    /**
     * Send a ERROR log message.
     *
     * @param tag - Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs
     * @param message - The message you would like logged
     */
    public static void e(String tag, String message) {
        if (sIsDebugBuild) {
            Log.e(tag, message.toString());
        }
    }

    /**
     * Send an INFO log message. If the log tag was not set with the init method, the calling class and
     * line number are set as the tag.
     *
     * @param message - The message you would like logged
     */
    public static void i(String message) {
        i(getDefaultTag(), message);
    }

    /**
     * Send an INFO log message.
     *
     * @param tag - Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs
     * @param message - The message you would like logged
     */
    public static void i(String tag, String message) {
        if (sIsDebugBuild) {
            Log.i(tag, message.toString());
        }
    }

    /**
     * Send a VERBOSE log message. If the log tag was not set with the init method, the calling class
     * and line number are set as the tag.
     *
     * @param message - The message you would like logged
     */
    public static void v(String message) {
        v(getDefaultTag(), message);
    }

    /**
     * Send a VERBOSE log message.
     *
     * @param tag - Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs
     * @param message - The message you would like logged
     */
    public static void v(String tag, String message) {
        if (sIsDebugBuild) {
            Log.v(tag, message.toString());
        }
    }

    /**
     * Send a WARN log message. If the log tag was not set with the init method, the calling class and
     * line number are set as the tag.
     *
     * @param message - The message you would like logged
     */
    public static void w(String message) {
        w(getDefaultTag(), message);
    }

    /**
     * Send a WARN log message.
     *
     * @param tag - Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs
     * @param message - The message you would like logged
     */
    public static void w(String tag, String message) {
        if (sIsDebugBuild) {
            Log.w(tag, message.toString());
        }
    }

    /**
     * What a Terrible Failure: Report an exception that should never happen. If the log tag was not set
     * with the init method, the calling class and line number are set as the tag. Depending on system
     * configuration, a report may be added to the {@link DropBoxManager} and/or the process may be
     * terminated immediately with an error dialog.
     *
     * @param message - The message you would like logged
     */
    public static void wtf(String message) {
        wtf(getDefaultTag(), message);
    }

    /**
     * What a Terrible Failure: Report an exception that should never happen. Depending on system
     * configuration, a report may be added to the {@link DropBoxManager} and/or the process may be
     * terminated immediately with an error dialog.
     *
     * @param message - The message you would like logged
     */
    public static void wtf(String tag, String message) {
        if (sIsDebugBuild) {
            Log.wtf(tag, message.toString());
        }
    }

    /**
     * Send a log message with dividers and line wrapping (if enabled). If the log tag was not set with
     * the init method, the calling class and line number are set as the tag.
     *
     * @param message - The message you would like logged
     */
    public static void print(CharSequence message) {
        print(getDefaultTag(), message);
    }

    /**
     * Send a log message with dividers and line wrapping (if enabled).
     *
     * @param message - The message you would like logged
     */
    public static void print(String tag, CharSequence message) {
        if (sIsDebugBuild) {
            if (sUseDivider) {
                printDivider();
            }

            if (sWrapLongLines) {
                // split up longer messages to make viewable in logcat
                if (message.length() > sMaxLineWidth) {
                    String newMessage = new String(message.toString());
                    int index = 0;
                    while (index + sMaxLineWidth <= newMessage.length()) {
                        Log.d(tag, newMessage.substring(index, (index + sMaxLineWidth)));
                        index += sMaxLineWidth;
                    }
                    Log.d(tag, newMessage.substring(index));
                }

                // otherwise just display normally if shorter than "maxLength"
                else {
                    Log.d(tag, message.toString());
                }
            } else {
                Log.d(tag, message.toString());
            }
        }
    }

    /**
     * Send a DEBUG log message to view a JSONObject. If the log tag was not set with the init method,
     * the calling class and line number are set as the tag.
     *
     * @param tag - Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs
     * @param jsonObject - The JSONObject you would like logged
     */
    public static void print(JSONObject jsonObject) {
        print(getDefaultTag(), jsonObject);
    }

    /**
     * Send a DEBUG log message to view a JSONObject.
     *
     * @param tag - Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs
     * @param jsonObject - The JSONObject you would like logged
     */
    public static void print(String tag, JSONObject jsonObject) {
        if (sIsDebugBuild) {
            if (sUseDivider) {
                printDivider();
            }

            if (jsonObject == null) {
                Log.d(tag, "Null JSONObject");
                return;
            }

            try {
                Log.d(getDefaultTag(), jsonObject.toString(3));
            } catch (JSONException e) {
                Log.d(tag, "Error logging JSONObject");
                print(e);
            }
        }
    }

    /**
     * Send a WARN log message to view an Exception. If the log tag was not set with the init method,
     * the calling class and line number are set as the tag.
     *
     * @param e - The Exception you would like logged
     */
    public static void print(Exception e) {
        print(getDefaultTag(), e);
    }

    /**
     * Send a WARN log message to view an Exception.
     *
     * @param tag - Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs
     * @param e - The Exception you would like logged
     */
    public static void print(String tag, Exception e) {
        if (sIsDebugBuild) {
            StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            String stackTraceString = stringWriter.toString();

            if (sUseDivider) {
                printDivider();
            }

            Log.w(tag, stackTraceString);
        }
    }

    public static void ping() {
        ping(getDefaultTag());
    }

    public static void ping(String tag) {
        if (sIsDebugBuild) {
            if (sUseDivider) {
                printDivider();
            }

            Log.d(tag, "[" + getCallingClassName() + "." + getCallingMethodName() + "] ");
        }
    }

    private static String getCallingClassName() {
        StackTraceElement[] elements = new Throwable().getStackTrace();
        String className = elements[1].getClassName();

        // extract only the class name from the fully qualified class name
        className = className.substring(className.lastIndexOf(".") + 1, className.length());
        return className;
    }

    private static String getCallingMethodName() {
        StackTraceElement[] elements = new Throwable().getStackTrace();
        String methodName = elements[1].getMethodName();

        // extract only the method name from the fully qualified class name
        methodName = methodName.substring(methodName.lastIndexOf(".") + 1, methodName.length());
        return methodName;
    }

    private static int getCallingLineNumber() {
        StackTraceElement[] elements = new Throwable().getStackTrace();
        int lineNumber = elements[1].getLineNumber();

        return lineNumber;
    }

    private static String getDefaultTag() {
        if (sLogTag == "") {
            return "[" + getCallingClassName() + ":" + getCallingLineNumber() + "]";
        } else {
            return sLogTag;
        }
    }

    private static void printDivider() {
        char[] fill = new char[sMaxLineWidth];
        Arrays.fill(fill, '-');
        String divider = new String(fill);
        Log.d(getDefaultTag(), divider);
    }
}
