package andy.firebasedemo.Log;

import android.os.Environment;
import android.support.compat.BuildConfig;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by andyli on 2015/7/15.
 */
public class L {
    static Logger logger;
    private static final String EXCEPTION_START = "****************EXCEPTION START****************";
    private static final String EXCEPTION_END = "****************EXCEPTION END****************";
    private static String TAG = "customlog";

    private static  boolean IS_DEBUG = true;

    public static String getProcess(){
        StackTraceElement[] elements =   Thread.currentThread().getStackTrace();
        String className =  elements[4].getClassName();
        className = className.substring(className.lastIndexOf(".")+1);
        String methodName =  elements[4].getMethodName();

        return "-["+ Thread.currentThread().getId()+"]"+"["+className+"]"+"["+methodName+"]"+"["+elements[4].getLineNumber()+"]-";
    }
    public static void d(String tag, String msg){
        if(!IS_DEBUG){
            return;
        }
       StackTraceElement[] elements =   Thread.currentThread().getStackTrace();
        try {
            msg = getProcess()+msg;
            Log.d(tag, msg);
        }catch (Exception e){
            Log.e(TAG,e.toString());
        }
    }
    public static void exception(String tag, Exception exc){
        if(!IS_DEBUG){
            return;
        }
        try {
            String process = getProcess();
            String msg = process+exc.toString();
            Log.e(tag, msg);
        }catch (Exception e){
            android.util.Log.e(TAG,e.toString());
        }
    }

    public static void e(String tag, String msg){
        if(!IS_DEBUG){
            return;
        }
        try {
            msg = getProcess()+msg;
            Log.e(tag, msg);
        }catch (Exception e){
            Log.e(TAG,e.toString());
        }
    }
    public static void i(String tag, String msg){
        if(!IS_DEBUG){
            return;
        }
        try {
            msg = getProcess()+msg;
            Log.i(tag, msg);
        }catch (Exception e){
            Log.e(TAG,e.toString());
        }
    }

    public static void v(String tag, String msg){
        if(!IS_DEBUG){
            return;
        }
        try {
            msg = getProcess()+msg;
            Log.v(tag, msg);
        }catch (Exception e){
            Log.e(TAG,e.toString());
        }
    }
    public static void w(String tag, String msg){
        if(!IS_DEBUG){
            return;
        }
        try {
            msg = getProcess()+msg;
            Log.w(TAG, msg);
        }catch (Exception e){
            Log.e(TAG,e.toString());
        }
    }
    public static void e(Exception e){
        if(!IS_DEBUG){
            return;
        }
        try {
           String  msg = getProcess();
            Log.e(TAG, msg+EXCEPTION_START);
            Log.e(TAG, msg+e.toString());
            Log.e(TAG, msg+EXCEPTION_END);
        }catch (Exception e1){
            Log.e(TAG,e1.toString());
        }
    }
}
