package xyz.xmethod.xycode.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by XiuYe on 2016/3/14 0014.
 * invoke Xy.init() first init Application
 */
public class VersionUtils {

    /**
     * 获取版本号
     * @param context
     * @return
     */
    public static String getVersionName(Context context){
        String versionName = "";
        PackageManager pm = context.getPackageManager();
        PackageInfo pi;
        try {
            pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    /**
     * 获取内部版本号
     * @param context
     * @return
     */
    public static int getVersionCode(Context context){
        int versionCode = 0 ;
        PackageManager pm = context.getPackageManager();
        PackageInfo pi;
        try {
            pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionCode = pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }
}
