package xyz.xmethod.xycode;

import android.content.Context;

import xyz.xmethod.xycode.interfaces.ISetImageUrlMethod;
import xyz.xmethod.xycode.utils.LogUtil.L;
import xyz.xmethod.xycode.utils.ShareStorage;

/**
 * Created by XY on 2017-06-08.
 * 使用xyLibrary时先在应用中init()
 */

public class Xy {
    private static Context xyContext;
    private static boolean isRelease;
    private static final String XY_PUBLIC_SP = "XY_PUBLIC_SP";
    private static ShareStorage storage;

    private static XyOption option;

    public static XyOption init(Context appContextForAllUtils, boolean isRelease) {
        xyContext = appContextForAllUtils;
        Xy.isRelease = isRelease;

        L.setShowLog(!isRelease && getStorage(xyContext).getBoolean(L.SHOW_LOG, false));
        option = new XyOption();
        return option;
    }

    public static Context getContext() {
        return xyContext;
    }

    public static void setContext(Context context) {
        Xy.xyContext = context;
    }

    public static boolean isRelease() {
        return isRelease;
    }

    public static XyOption getOption() {
        return option == null ? new XyOption() : option;
    }

    /**
     * ShareStorage
     * Xy类公共持久化类
     * @return
     */
    public static ShareStorage getStorage(Context context) {
        if (storage == null) {
            storage = new ShareStorage(context, XY_PUBLIC_SP);
        }
        return storage;
    }

    public static class XyOption {
        ISetImageUrlMethod iSetImageUrlMethod;

        XyOption() { }

        public ISetImageUrlMethod getiSetImageUrlMethod() {
            return iSetImageUrlMethod;
        }

        public void setiSetImageUrlMethod(ISetImageUrlMethod iSetImageUrlMethod) {
            this.iSetImageUrlMethod = iSetImageUrlMethod;
        }
    }


}
