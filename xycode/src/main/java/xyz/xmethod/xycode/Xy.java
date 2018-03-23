package xyz.xmethod.xycode;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;

import xyz.xmethod.xycode.interfaces.ImageUrlSetter;
import xyz.xmethod.xycode.unit.MsgEvent;
import xyz.xmethod.xycode.debugHelper.logHelper.L;
import xyz.xmethod.xycode.storages.ShareStorage;

/**
 * Created by XY on 2017-06-08.
 * 使用xyLibrary时先在应用中init()  哈哈大笑
 */
public class Xy {
    /**
     * Context
     * 全局Context，把Application作为Context传入XyCode
     * 不能把Activity或其它View的Context传入，会有内存泄漏风险
     */
    private static Context xyContext;
    private static boolean isRelease;
    private static final String XY_PUBLIC_SP = "XY_PUBLIC_SP";
    private static ShareStorage storage;

    private static XyOption option;

    /**
     * 初始化
     * 可把固定的判断条件作为参数传入
     * 如以版本号作为判断依据，或者其它方式
     * @param appContextForAllUtils Application
     * @param isRelease 判断是否正式版的条件
     * @return
     */
    public static XyOption init(Context appContextForAllUtils, boolean isRelease) {
        xyContext = appContextForAllUtils;
        Xy.isRelease = isRelease;

        L.setShowLog(!isRelease && getStorage().getBoolean(L.SHOW_LOG, false));
        option = new XyOption();
        return option;
    }

    public static Context getContext() {
        return xyContext;
    }

    /**
     * 设置全局Context
     * 请谨慎使用此方法，在热更新的时候可能会需要用此方法预先对Xy进行Context设置
     * @param context
     */
    public static void setContext(Context context) {
        Xy.xyContext = context;
    }

    /**
     * 是否正式版
     * 如果果正式版，则DebugActivity调试页面与ServerControllerActivity不会被调出
     */
    public static boolean isRelease() {
        return isRelease;
    }

    /**
     * 对XyCode进行全局配置
     * @return
     */
    public static XyOption getOption() {
        return option == null ? new XyOption() : option;
    }

    /**
     * ShareStorage
     * Xy类公共持久化类
     * @return
     */
    public static ShareStorage getStorage() {
        if (storage == null) {
            storage = new ShareStorage(XY_PUBLIC_SP);
        }
        return storage;
    }

    /**
     * 对Xy进行全局设置
     */
    public static class XyOption {
        ImageUrlSetter imageUrlSetter;

        XyOption() { }

        public ImageUrlSetter getImageUrlSetter() {
            return imageUrlSetter;
        }

        public XyOption setImageUrlSetter(ImageUrlSetter imageUrlSetter) {
            this.imageUrlSetter = imageUrlSetter;
            return this;
        }
    }

    /**
     * 取得EventBus实例
     * @return EventBus
     */
    public static EventBus getEventBus() {
        return EventBus.getDefault();
    }

    /**
     * 发送EventBus事件
     */
    public static void postEvent(MsgEvent msgEvent) {
        EventBus.getDefault().post(msgEvent);
    }

}
