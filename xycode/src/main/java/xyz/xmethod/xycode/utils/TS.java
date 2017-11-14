package xyz.xmethod.xycode.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.LayoutRes;
import android.view.View;
import android.widget.Toast;

import xyz.xmethod.xycode.Xy;

/**
 * Toast
 *
 * @author XiuYe
 * invoke Xy.init() first init Application
 * Show中的Object参数可以控制Toast的显示不同的图片或者格式以适应不同的需求
 */
public class TS {

    private static Handler uiHandler = new Handler(Looper.getMainLooper());
    private static Toast toast;
    private static IToastLayoutSetter toastSetter;

    /**
     * 自定义Toast Layout
     */
    private static int toastLayoutId = -1;

    public static void init(@LayoutRes int toastLayoutId, IToastLayoutSetter toastSetter) {
        TS.toastLayoutId = toastLayoutId;
        TS.toastSetter = toastSetter;
    }

    public static void show(int resText) {
        String text = Xy.getContext().getString(resText);
        show(Xy.getContext(), text, null);
    }

    public static void show(String text) {
        show(Xy.getContext(), text, null);
    }

    public static void show(int resText, Object obj) {
        String text = Xy.getContext().getString(resText);
        show(Xy.getContext(), text, obj);
    }

    public static void show(String text, Object obj) {
        show(Xy.getContext(), text, obj);
    }

    public static void show(final Context context, final int resText, Object obj) {
        show(context, Xy.getContext().getString(resText), obj);
    }

    /**
     * 显示Toast
     *
     * @param context
     * @param text
     * @param obj     传递给IToastLayoutSetter做操作
     */
    public static void show(final Context context, final String text, Object obj) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            doShow(context, text, obj);
        } else {
            uiHandler.post(() -> doShow(context, text, obj));
        }
    }

    public static void cancel() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            doCancel();
        } else {
            uiHandler.post(() -> doCancel());
        }
    }

    private static void doCancel() {
        if (toast != null)
            toast.cancel();
    }

    private static void doShow(Context context, String text, Object object) {
        //取消显示上一个Toast
        doCancel();

        if (TS.toastLayoutId != -1) {
            toast = new Toast(context);
            View view = View.inflate(context, TS.toastLayoutId, null);
//		toast.setGravity(Gravity.TOP, 0, context.getResources().getDimensionPixelOffset(R.dimen.toast_margin_top));
            toast.setView(view);
            toast.setDuration(Toast.LENGTH_SHORT);
            if (toastSetter != null) toastSetter.onToastLayout(view, toast, object);
        } else {
            toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        }

        toast.show();
    }

  /*  public static class ToastSetter {

        private IToastLayoutSetter toastLayoutSetter;

        public ToastSetter(boolean onlyShowOneToast, IToastLayoutSetter toastLayoutSetter) {
            this.toastLayoutSetter = toastLayoutSetter;
            TS.onlyShowOneToast = onlyShowOneToast;
        }
        protected void setToastOffsetAfterAnotherNewToast(Toast oldToast){
        }
    }*/

    public interface IToastLayoutSetter {
        void onToastLayout(View root, Toast toast, Object object);
    }

}
