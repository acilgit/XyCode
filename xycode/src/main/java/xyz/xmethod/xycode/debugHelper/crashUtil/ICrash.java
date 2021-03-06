package xyz.xmethod.xycode.debugHelper.crashUtil;

import android.support.annotation.LayoutRes;

/**
 * Created by XiuYe on 2017-06-05.
 * 处理崩溃页面展示
 */
public interface ICrash {
    /**
     * 页面LayoutId
     */
    @LayoutRes int getLayoutId();

    /**
     * 页面展示
     * @param activity
     * @param crashItem 崩溃Item
     */
    void setViews(CrashActivity activity, CrashItem crashItem);

    /**
     * 是否保存崩溃日志写入本地data/data/com...文件
     *
     * @return true
     */
    boolean getIsSaveCrashLogFile();
}
