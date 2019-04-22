package xyz.xmethod.xycode.base;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import xyz.xmethod.xycode.adapter.CustomHolder;
import xyz.xmethod.xycode.annotation.annotationHelper.StateBinder;
import xyz.xmethod.xycode.interfaces.Interfaces;
import xyz.xmethod.xycode.interfaces.PermissionListener;
import xyz.xmethod.xycode.okHttp.CallItem;
import xyz.xmethod.xycode.okHttp.OkHttp;
import xyz.xmethod.xycode.debugHelper.logHelper.LogLayout;
import xyz.xmethod.xycode.unit.MsgEvent;
import xyz.xmethod.xycode.debugHelper.logHelper.L;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by xiuye on 2016/1/11 0011.
 * 先继承一个Abstract的类
 */
public abstract class XyBaseActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_GOT_RESULT = 1800;
    /**
     * 选取图片
     * 在onPhotoSelectResult()中返回图片Uri
     */
    public static final int REQUEST_CODE_PHOTO_SELECT = 2802;
    private static final int REQUEST_CODE_MULTI_PHOTO_SELECT = 2803;
    /**
     * 选取电话号码
     * Tools.pickNumber()方法取得
     */
    public static final int REQUEST_CODE_GOT_PHONE_NUMBER = 3801;

    /**
     * 请求权限Code
     */
    private static final int REQUEST_PERIMISSION_CODE = 1000;


    /**
     * BaseActivity列表
     */
    private static List<Activity> activities = new LinkedList<>();

    /**
     * 加载中对话框
     */
    private static AlertDialog loadingDialog;

    /**
     * 是否手动取消加载对话框
     */
    private static boolean loadingDialogShowManual = false;

    /**
     * 当前Activity的请求列表
     */
    protected List<CallItem> requestList = new ArrayList<>();

    @Deprecated
    private BroadcastReceiver finishReceiver;

    /**
     * 当前页面
     */
    private XyBaseActivity thisActivity;

    @Deprecated
    public static final String ACTION_FINISH_ACTIVITY = "FinishBaseActivity";

    /**
     * 调试信息Layout
     */
    protected LogLayout logLayout;

    /**
     * ViewHolder
     */
    private CustomHolder rootHolder;

    /**
     * 当前Activity的LayoutId
     */
    private int activityLayout = 0;

    /**
     * 第一次onShow
     */
    private boolean firstShowOnStart = true;

    /**
     * 权限回调监听
     */
    private PermissionListener mPermissionResultListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* 初始化工作 */
        thisActivity = this;
        addActivity(this);
        activityLayout = setActivityLayout();
        if (activityLayout != 0) {
            setContentView(activityLayout);
        }
        if (useEventBus()) {
            EventBus.getDefault().register(this);
        }
        initOnCreate(savedInstanceState);
    }

    /**
     * 返回Activity的LayoutId
     * 当不使用时返回0可以在onCreate()中的setContentView中进行设置
     *
     * @return layoutId
     */
    protected abstract int setActivityLayout();

    /**
     * onCreate方法时进行初始化操作
     *
     * @param savedInstanceState savedInstanceState
     */
    protected abstract void initOnCreate(Bundle savedInstanceState);

    /**
     * 在onStart()的时候进行操作
     *
     * @param firstShow true：第一次OnStart
     */
    protected void showOnStart(boolean firstShow) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        /*LogLayout显示*/
        if (L.showLog() && logLayout == null) {
            logLayout = LogLayout.attachLogLayoutToActivity(getThis());
        }
        showOnStart(firstShowOnStart);
        if (firstShowOnStart) {
            firstShowOnStart = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        /*停止LogLayout显示*/
        if (logLayout != null) {
            logLayout.removeLayout();
            logLayout = null;
        }
    }

    @Override
    protected void onDestroy() {
        if (useEventBus()) {
            EventBus.getDefault().unregister(this);
        }
        for (CallItem call : requestList) {
            if (call != null && call.getCall() != null) {
                call.getCall().cancel();
            }
        }
        dismissLoadingDialog();
        removeActivity(this);
        super.onDestroy();
    }

    /**
     * 在Java中可以直接使用此方法通过Id对控件进行引用
     * 如果在Kotlin中则可以省略
     *
     * @return
     */
    public CustomHolder rootHolder() {
        if (rootHolder == null) {
            rootHolder = new CustomHolder(getWindow().getDecorView().getRootView());
        }
        return rootHolder;
    }

    protected XyBaseActivity getThis() {
        return this;
    }

    public void start(Class<? extends Activity> activityClass) {
        Intent intent = new Intent();
        intent.setClass(this, activityClass);
        this.startActivity(intent);
    }

    public void start(Class<? extends Activity> activityClass, int requestCode) {
        Intent intent = new Intent();
        intent.setClass(this, activityClass);
        this.startActivityForResult(intent, requestCode);
    }

    public void start(Class<? extends Activity> activityClass, BaseIntent baseIntent) {
        Intent intent = new Intent();
        intent.setClass(this, activityClass);
        baseIntent.setIntent(intent);
        this.startActivity(intent);
    }

    public void start(Class<? extends Activity> activityClass, BaseIntent baseIntent, int requestCode) {
        Intent intent = new Intent();
        intent.setClass(this, activityClass);
        baseIntent.setIntent(intent);
        this.startActivityForResult(intent, requestCode);
    }

    protected static List<Activity> getActivities() {
        return activities;
    }

    public boolean isLoadingDialogShowing() {
        return loadingDialog != null && loadingDialog.isShowing();
    }

    /**
     * 显示加载中对话框
     * 会在OkHttp网络加载完成后被自动关闭
     */
    public void showLoadingDialog() {
        showDialog(false);
    }

    /**
     * 显示加载对话框
     * 不能被自动关闭，需要调用{@link #dismissLoadingDialog()}来关闭
     */
    public void showLoadingDialogManualDismiss() {
        showDialog(true);
    }

    private void showDialog(boolean manualDismiss) {
        loadingDialogShowManual = manualDismiss;
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        loadingDialog = setLoadingDialog();
        if (loadingDialog != null && !loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    /**
     * 关闭加载中对话框
     */
    public static void dismissLoadingDialog() {
        loadingDialogShowManual = false;
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    /**
     * 关闭加载中对话框
     * 只能关闭{@link #showLoadingDialog()}打开的对话框
     */
    public static void dismissLoadingDialogByManualState() {
        if (!loadingDialogShowManual && loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        StateBinder.saveState(this, outState);
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        StateBinder.bindState(this, savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * 关闭输入法
     */
    public void hideSoftInput() {
        View view = getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    protected void registerFinishReceiver() {
        finishReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                finish();
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_FINISH_ACTIVITY);
        LocalBroadcastManager.getInstance(this).registerReceiver(finishReceiver, filter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public interface BaseIntent {
        void setIntent(Intent intent);
    }


    /**
     * static methods
     */


    /**
     * 加入Activity引用组中
     *
     * @param activity
     */
    private static void addActivity(Activity activity) {
        if (!activities.contains(activity)) {
            activities.add(activity);
        }
        StringBuilder sb = new StringBuilder("addActivity: [");
        for (int i = 0; i < activities.size(); i++) {
            sb.append(activities.get(i).getClass().getSimpleName()).append(i < activities.size() - 1 ? ", " : "");
        }
        sb.append("]");
        L.i(sb.toString());
    }

    /**
     * 删除Activity引用组中
     *
     * @param activity
     */
    private static void removeActivity(Activity activity) {
        if (activities.contains(activity)) {
            activities.remove(activity);
        }
        StringBuilder sb = new StringBuilder("removeActivity: [");
        for (int i = 0; i < activities.size(); i++) {
            sb.append(activities.get(i).getClass().getSimpleName()).append(i < activities.size() - 1 ? ", " : "");
        }
        sb.append("]");
        L.i(sb.toString());
    }

    /**
     * 关闭所有activities中的页面
     */
    public static void finishAllActivity() {
        for (final Activity activity : activities) {
            if (activity != null) {
                activity.runOnUiThread(() -> activity.finish());
            } else {
                activities.remove(activity);
            }
        }
    }

    /**
     * 关闭应该
     */
    public static void exitApplication() {
        finishAllActivity();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * judge a activity is on foreground
     * 判断是否当前页面
     *
     * @param activity
     */
    public static boolean isForeground(Activity activity) {
        if (TextUtils.isEmpty(activity.getClass().getName())) {
            return false;
        }
        ActivityManager am = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            if (activity.getClass().getName().equals(cpn.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 取得当前页面
     *
     * @param context
     * @return
     */
    public static Activity getForegroundActivity(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            Log.e(" getForegroundActivity ", cpn.getClassName());
            for (Activity activity : activities) {
                if (activity.getClass().getName().equals(cpn.getClassName())) {
                    Log.e(" getActivity ", activity.getClass().getName());
                    return activity;
                }
            }
        }
        return null;
    }

    /**
     * 取得className相同的第一个页面
     *
     * @param className
     * @return
     */
    public static Activity getActivityByClassName(String className) {
        for (Activity activity : activities) {
            if (activity.getClass().getName().contains(className)) {
                Log.e("ActivityClassName ", activity.getClass().getName());
                return activity;
            }
        }
        return null;
    }

    /**
     * EventBus
     * 设置是否使用EventBus
     * 已内置，推荐使用
     */

    /**
     * 当前页面是否注册EventBus
     *
     * @return
     */
    protected boolean useEventBus() {
        return false;
    }

    /**
     * Post EventBus事件
     *
     * @param eventName
     */
    public void postEvent(String eventName) {
        postEvent(eventName, null, null);
    }

    public void postEvent(String eventName, Interfaces.FeedBack feedBack) {
        postEvent(eventName, null, feedBack);
    }

    public void postEvent(String eventName, Object object) {
        postEvent(eventName, object, null);
    }

    public void postEvent(String eventName, String object) {
        postEvent(eventName, object, null);
    }

    public void postEvent(String eventName, Object object, Interfaces.FeedBack feedBack) {
        EventBus.getDefault().post(new MsgEvent(eventName, object, feedBack));
    }

    /**
     * 在主线程中执行EventBus返回的事件
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MsgEvent event) {
    }

    /**
     * 在后台线程中执行EventBus返回的事件
     *
     * @param event
     */
    @Subscribe
    public void onEventBackground(MsgEvent event) {

    }

    /**
     * okHttp request
     * 在当前Activity中执行OkHttp请求
     */
    public CallItem newCall() {
        CallItem callItem = OkHttp.newCall(getThis());
        requestList.add(callItem);
        return callItem;
    }

    /**
     * Results return
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = null;
        if (data != null) {
            uri = data.getData();
            if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_PHOTO_SELECT) {
                onPhotoSelectResult(uri);
            }
        }
    }

    /**
     * 直接处理返回的图片选择Uri
     * 只有返回内容时才会被调用且
     * requestCode == REQUEST_CODE_PHOTO_SELECT
     *
     * @param uri
     */
    protected void onPhotoSelectResult(Uri uri) {

    }

    /**
     * 加载中的对话框
     *
     * @return null：则不使用对话框
     */
    protected abstract AlertDialog setLoadingDialog();

    public AlertDialog getLoadingDialog() {
        return loadingDialog;
    }

    protected interface WindowMode {
        // 输入适应
        int INPUT_ADJUST = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN;
    }

    /**
     * 设置输入法模式
     *
     * @param windowMode 在 WindowMode 中选择相应选项，或从WindowManager.LayoutParams中选择
     */
    protected void setWindowMode(int windowMode) {
        getWindow().setSoftInputMode(windowMode);
    }


    /**
     * 权限封装处理
     */
    public void requestRuntimePermissions(String[] permissions, PermissionListener permissionListener) {
        this.mPermissionResultListener = permissionListener;
        List<String> permissionList = new ArrayList<>();
        for (String permission : permissions) {
            /* 检测是否授权，没有授权添加入list中去授权*/
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
            }
        }
        /* 如果有未授权的则去请求权限 */
        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionList.toArray(new String[permissionList.size()]),
                    REQUEST_PERIMISSION_CODE);
        } else {
            if (mPermissionResultListener != null) {
                mPermissionResultListener.onGranted();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERIMISSION_CODE:
                if (grantResults.length > 0) {
                    /* 存储拒绝的权限 */
                    List<String> deniedPermission = new ArrayList<>();
                    for (int i = 0; i < grantResults.length; i++) {
                        int grantResult = grantResults[i];
                        if (grantResult == PackageManager.PERMISSION_DENIED) {
                            deniedPermission.add(permissions[i]);
                        }
                    }
                    if (deniedPermission.isEmpty() && mPermissionResultListener != null) {
                        mPermissionResultListener.onGranted();
                    } else {
                        if (mPermissionResultListener != null) {
                            mPermissionResultListener.onDenied(deniedPermission);
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

}
