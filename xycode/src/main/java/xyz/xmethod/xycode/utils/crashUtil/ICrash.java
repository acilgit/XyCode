package xyz.xmethod.xycode.utils.crashUtil;

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
}
