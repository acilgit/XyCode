package xyz.xmethod.xycode.serverApiHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import xyz.xmethod.xycode.R;
import xyz.xmethod.xycode.Xy;
import xyz.xmethod.xycode.debugHelper.logHelper.L;

/**
 * Created by XY on 2017-06-03.
 * 服务器地址摄制页面
 * 并且可控制Log的开关
 *
 * Release版本无法调用此页面
 */
public class ServerControllerActivity extends AppCompatActivity implements View.OnClickListener{

    /**
     * ApiHelper实例
     */
    private static ApiHelper api;
    /**
     * 当前服务器地址
     */
    private TextView tvServer;
    /**
     * Log开关
     */
    private Switch swLog;

    /**
     * 打开服务器地址控制器
     *
     * @param activity
     * @param api
     */
    public static void startThis(Activity activity, ApiHelper api) {
        if (!Xy.isRelease() && api != null) {
            ServerControllerActivity.api = api;
            activity.startActivity(new Intent(activity, ServerControllerActivity.class));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Xy.isRelease()) {
            finish();
            return;
        }
        setContentView(R.layout.activity_server_controller);
        initViews();
    }

    /**
     * 初始化界面
     */
    private void initViews() {
        tvServer = (TextView) findViewById(R.id.tvServer);
        swLog = (Switch) findViewById(R.id.swLog);
        if (api.getServer().equals(api.getReleaseUrl())) {
            tvServer.setText("正式服务器：" + api.getServer());
        } else  if (api.getServer().equals(api.getDebugUrl())) {
            tvServer.setText("测试服务器："+ api.getServer());
        } else {
            tvServer.setText("当前服务器："+ api.getServer());
        }

         findViewById(R.id.btnReleaseServer).setOnClickListener(this);
         findViewById(R.id.btnDebugServer).setOnClickListener(this);
         findViewById(R.id.btnOptionServer).setOnClickListener(this);
         findViewById(R.id.tvClose).setOnClickListener(this);

        swLog.setChecked(Xy.getStorage().getBoolean(L.SHOW_LOG, false));
        swLog.setOnCheckedChangeListener((buttonView, isChecked) -> Xy.getStorage().put(L.SHOW_LOG, isChecked));
    }

    /**
     *
     */
    public static ApiHelper getApi() {
        return api;
    }
    /**
     * 点击监听
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btnReleaseServer) {
            /* 设置为正式服务器地址 */
            api.setServerUrl(api.getReleaseUrl());
            finish();
        } else if (i == R.id.btnDebugServer) {
            /* 设置为正式服务器地址 */
            api.setServerUrl(api.getDebugUrl());
            finish();
        } else if (i == R.id.btnOptionServer) {
            /* 打开选择服务器列表 */
            new ServerSelectDialog(ServerControllerActivity.this).show();
        } else if (i == R.id.tvClose) {
            /* 退出 */
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        api = null;
        super.onDestroy();
    }
}
