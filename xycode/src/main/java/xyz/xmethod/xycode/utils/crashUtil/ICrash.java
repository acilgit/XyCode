package xyz.xmethod.xycode.utils.crashUtil;

import android.support.annotation.LayoutRes;

/**
 * Created by XY on 2017-06-05.
 */

public interface ICrash {
    @LayoutRes int getLayoutId();

    void setViews(CrashActivity activity, CrashItem crashItem);
}
