package xyz.xmethod.xycode.debugHelper.logHelper;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import xyz.xmethod.xycode.Xy;
import xyz.xmethod.xycode.unit.MsgEvent;
import xyz.xmethod.xycode.utils.DateUtils;
import xyz.xmethod.xycode.utils.Tools;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * Log
 * showLog模式下，才会输出到logcat
 * 任何模式下Log都会输出到logList
 * 可把logList在CrashActivity发送到服务器
 */
public class L {
    public static final String SHOW_LOG = "SHOW_LOG_FOR_XY";
    public static final String EVENT_LOG = "EVENT_LOG";

    /**
     * -1时不限数量
     */
    private static int MAX_LOG_LIST_SIZE_IN_RELEASE_MODE = 30;

    /**
     * 打印Log
     */
    private static boolean showLog = true;

    /**
     * Tag
     */
    private static String TAG = " Debug ";

    /**
     * 是否支持打印长内容
     */
    private static boolean isLong = true;

    /**
     * 输出文件
     * 暂时没用
     */
    private static File outputFile = null;

    /**
     * 输出文件目录
     * 暂时没用
     */
    private static String LOG_DIR;

    /**
     * 输出文件名
     * 暂时没用
     */
    private static final String LOG_NAME = "CrashLog.txt";

    /**
     * Log的输出List
     */
    private static List<LogItem> logList;

    /**
     * Log列表
     */
    public static List<LogItem> getLogList() {
        if (logList == null) {
            logList = new ArrayList<>();
        }
        return logList;
    }

    /**
     * 设置Log列表
     */
    public static void setLogList(List<LogItem> logList) {
        L.logList = logList;
    }

    /**
     * 添加LogItem
     */
    public static void addLogItem(String msg) {
        addLogItem(null, msg, LogItem.LOG_TYPE_E);
    }

    /**
     * 添加LogItem
     */
    public static void addLogItem(String title, String msg) {
        addLogItem(title, msg, LogItem.LOG_TYPE_E);
    }

    /**
     * 添加LogItem
     */
    public static void addLogItem(String title, String msg, int type) {
        String addTitle = "", addMsg = "";
        if(title != null) addTitle = new String(title);
        if(msg != null) addMsg = new String(msg);
        getLogList().add(new LogItem(DateUtils.formatDateTime("yyyy-M-d HH:mm:ss:SSS", DateUtils.getNow()), addTitle, addMsg, type));
        if (!showLog() && MAX_LOG_LIST_SIZE_IN_RELEASE_MODE != -1 && getLogList().size() > MAX_LOG_LIST_SIZE_IN_RELEASE_MODE) {
            getLogList().remove(0);
        }
        EventBus.getDefault().post(new MsgEvent(EVENT_LOG, null, null));
    }

    public static void setShowLog(boolean showLog) {
        L.showLog = showLog;
        Xy.getStorage().getEditor().putBoolean(SHOW_LOG, showLog).commit();
    }

    public static boolean showLog() {
        return Xy.getContext() != null && showLog;
    }

    public static void setShowLongErrorMode(boolean isLong) {
        L.isLong = isLong;
    }

    public static void setTag(String tag) {
        TAG = tag;
    }

    public static void i(String msg) {
        if (showLog()) {
            Log.i(TAG, msg);
        }
        addLogItem(null, msg, LogItem.LOG_TYPE_I);
    }

    public static void i(int msg) {
        i(msg + "");
    }

    public static void d(String msg) {
        if (showLog()) {
            Log.d(TAG, msg);
        }
        addLogItem(null, msg, LogItem.LOG_TYPE_D);
    }

    public static void v(String msg) {
        if (showLog()) {
            Log.v(TAG, msg);
        }
        addLogItem(null, msg, LogItem.LOG_TYPE_I);
    }

    public static void e(String msg) {
        e(null, msg);
    }


    public static void e(String title, String msg) {
        if (showLog()) {
            if (isLong) {
                eLong(title, msg);
            } else {
                Log.e(TAG, TextUtils.isEmpty(title) ? msg : title + "\n" + msg);
            }
        }
        addLogItem(title, msg);
    }

    /**
     * Crash Log 的展示
     * @param title
     * @param e
     */
    public static void crash(String title, Exception e) {
        StringBuffer sb = new StringBuffer();
        for (StackTraceElement traceElement : e.getStackTrace()) {
            sb.append("\n").append(traceElement.toString());
        }
        String content = e.toString() + "\n" + sb.toString();
        if (showLog()) {
            if (isLong) {
                eLong(title, content);
            } else {
                Log.e(TAG, TextUtils.isEmpty(title) ? e.toString() : title + "\n" + e.toString());
            }
        }
        if (!logList.get(logList.size()-1).getContent().contains(e.toString())) {
            addLogItem(title, content, LogItem.LOG_TYPE_CRASH);
        }
    }

    private static void eLong(String title, String longString) {
        if (showLog()) {
            int maxLogSize = 1000;
            String content = TextUtils.isEmpty(title) ? longString : title + "\n" + longString;
            if (content.length() > maxLogSize) {
                for (int i = 0; i <= content.length() / maxLogSize; i++) {
                    int start = i * maxLogSize;
                    int end = (i + 1) * maxLogSize;
                    end = end > content.length() ? content.length() : end;
                    Log.e(TAG, content.substring(start, end));
                }
            } else {
                Log.e(TAG, content);
            }
            if (outputFile != null) writeLogToOutputFile(content);
        }
    }

    public static void setLogOutputFile(File file) {
        String fileName = file.getName();
    }

    private static void writeLogToOutputFile(String content) {
        if (!outputFile.exists()) {
            return;
        }
        try {
            RandomAccessFile accessFile = new RandomAccessFile(outputFile, "rw");
            String s = DateUtils.formatDateTime("yyyy-MM-dd HH:mm:ss:zzz ->\n", System.currentTimeMillis()) + content + "\r\n\r\n";
            accessFile.seek(0);
            accessFile.writeChars(s);
            accessFile.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        /*try {
            OutputStream outputStream = new FileOutputStream(outputFile);
            OutputStreamWriter out = new OutputStreamWriter(outputStream);
            out.write(DateUtils.formatDateTime("yyyy-MM-dd HH:mm:ss:zzz ->\n", System.currentTimeMillis()) + content + "\r\n\r\n");
            out.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }*/
    }

    public static void writeLog(Context context, Throwable ex, String crashItem) {
        LOG_DIR = Tools.getCacheDir() + "/log/";
        String info = null;
        ByteArrayOutputStream baos = null;
        PrintStream printStream = null;
        try {
            baos = new ByteArrayOutputStream();
            printStream = new PrintStream(baos);
            if (crashItem != null) {
                printStream.print(crashItem);
            }
            if (ex != null) {
                ex.printStackTrace(printStream);
            }
            byte[] data = baos.toByteArray();
            info = new String(data);
            data = null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (printStream != null) {
                    printStream.close();
                }
                if (baos != null) {
                    baos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        e("崩溃信息\n" + info);
        File dir = new File(LOG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, LOG_NAME);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            fileOutputStream.write(info.getBytes());
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void setMaxLogListSizeInReleaseMode(int maxLogListSizeInReleaseMode) {
        MAX_LOG_LIST_SIZE_IN_RELEASE_MODE = maxLogListSizeInReleaseMode;
    }

    /**
     * layout: item_log.xml
     */
}
