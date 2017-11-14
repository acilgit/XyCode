package xyz.xmethod.xycode.utils.crashUtil;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import xyz.xmethod.xycode.R;
import xyz.xmethod.xycode.Xy;
import xyz.xmethod.xycode.base.XyBaseActivity;
import xyz.xmethod.xycode.interfaces.Interfaces;
import xyz.xmethod.xycode.utils.LogUtil.LogItem;
import xyz.xmethod.xycode.utils.LogUtil.LogLayout;
import xyz.xmethod.xycode.utils.DateUtils;
import xyz.xmethod.xycode.utils.LogUtil.L;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * Created by XiuYe on 2017-06-05.
 * 崩溃处理页面
 * 建议在测试版的时候加入，正式版的时候可以使用Service直接重启应用
 */
public class CrashActivity extends AppCompatActivity {

    /**
     * 崩溃页面展示
     */
    public static ICrash iCrash;

    /**
     * 取得CrashItem后进行处理，可发到后台或者本地持久化等
     */
    public static Interfaces.CB<CrashItem> cb;

    public static final String MSG = "msg";
    public static final String CRASH_LOG = "crashLog";

    /**
     * LogLayout
     */
    private LogLayout logLayout;

    /**
     * CrashItem
     */
    private CrashItem crashItem;

    /**
     * 错误信息
     */
    protected String errorMsg;

    /**
     * 当前页面实例
     */
    private static CrashActivity instance;

    /**
     * 获取当前页面实例
     */
    public static CrashActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashActivity.instance = this;
        /* 取得用户最近操作及网络请求记录 */
        String json = Xy.getStorage().getString(CRASH_LOG);
        List<LogItem> logItems = JSON.parseArray(json, LogItem.class);
        L.setLogList(logItems);
        errorMsg = getIntent().getStringExtra(MSG);

        /* 初始化页面内容 */
        initViews();
    }

    /**
     * 初始化页面内容
     */
    private void initViews() {
        crashItem = getCrashItem(errorMsg);
        /* 处理回调 */
        if (cb != null) {
            cb.go(crashItem);
        }
        if (iCrash != null && iCrash.getLayoutId() != 0) {
            setContentView(iCrash.getLayoutId());
            iCrash.setViews(this, crashItem);
        } else {
            setContentView(R.layout.activity_base_crash);

            TextView tv = (TextView) findViewById(R.id.tv);
            tv.setText(crashItem != null ? crashItem.toString() : errorMsg);
            Button btn = (Button) findViewById(R.id.btn);
            btn.setOnClickListener(
                    v -> finish()
            );
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (L.showLog() && logLayout == null) {
            logLayout = LogLayout.attachLogLayoutToActivity(this);
        }
    }

    /**
     *
     * @param catchErrorCallback 返回true则执行相关操作，否则直接关闭程序
     */
    public static void setCrashOperation(Interfaces.CB<CrashItem> catchErrorCallback) {
        setCrashOperation( catchErrorCallback, null);
    }

    /**
     * 使用此方法打开CrashActivity
     * @param catchErrorCallback 返回true则执行相关操作，否则直接关闭程序
     * @param iCrash    Null时则使用默认页面布局
     */
    public static void setCrashOperation(Interfaces.CB<CrashItem> catchErrorCallback, ICrash iCrash) {
        CrashActivity.cb = catchErrorCallback;
        CrashActivity.iCrash = iCrash;
        Thread.setDefaultUncaughtExceptionHandler((t, ex) -> {
            ByteArrayOutputStream baos = null;
            PrintStream printStream = null;
            String info = null;
            try {
                baos = new ByteArrayOutputStream();
                printStream = new PrintStream(baos);
                ex.printStackTrace(printStream);
                byte[] data = baos.toByteArray();
                info = new String(data);
                data = null;
                L.getLogList().add(new LogItem(DateUtils.formatDateTime("yyyy-M-d HH:mm:ss:SSS", DateUtils.getNow()), info, LogItem.LOG_TYPE_CRASH));
            } catch (Exception e) {
                e.printStackTrace();
            }
            ex.printStackTrace();
            String jsonString = JSON.toJSONString(L.getLogList());
            if (Xy.getStorage().getEditor().putString(CRASH_LOG, jsonString).commit()) {
                Intent intent = new Intent(Xy.getContext(), CrashActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(CrashActivity.MSG, info);
                Xy.getContext().startActivity(intent);
                /* 杀死该应用进程 */
                XyBaseActivity.exitApplication();
            }

        });
    }

    /**
     * 获得报错信息
     * @param errorMsg
     * @return
     */
    public CrashItem getCrashItem(String errorMsg) {
        CrashItem crashItem = null;
        try {
            //应用的版本名称和版本号
            PackageManager pm = this.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
            crashItem = new CrashItem();
            crashItem.setVersionName(pi.versionName);
            crashItem.setVersionCode(pi.versionCode);
            //android版本号
            crashItem.setRelease(Build.VERSION.RELEASE);
            crashItem.setSdk(Build.VERSION.SDK_INT);
            //手机制造商
            crashItem.setManufacturer(Build.MANUFACTURER);
            //手机型号
            crashItem.setModel(Build.MODEL);
            crashItem.setErrorMsg(errorMsg);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return crashItem;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        L.getLogList().clear();
    }
}
