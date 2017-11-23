package xyz.xmethod.xycode.serverApiHelper;

import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import xyz.xmethod.xycode.R;
import xyz.xmethod.xycode.adapter.CustomHolder;
import xyz.xmethod.xycode.adapter.XAdapter;
import xyz.xmethod.xycode.xRefresher.recyclerviewHelper.XLinearLayoutManager;
import xyz.xmethod.xycode.unit.ViewTypeUnit;

import java.util.List;

/**
 * Created by xiuYe on 2016/9/6 0006.
 * <p>
 * 服务器选择对话框
 */
public class ServerSelectDialog implements View.OnClickListener {

    /**
     * ServerControllerActivity
     */
    private ServerControllerActivity activity;

    /**
     * Dialog Builder
     */
    private AlertDialog.Builder builder;

    /**
     * 对话框
     */
    private AlertDialog dialog;

    /**
     * 对话框Layout
     */
    private LinearLayout layout;

    /**
     * 服务器地址EditText
     */
    private EditText et;

    /**
     * 服务器列表View
     */
    private RecyclerView rv;

    /**
     * 服务器列表
     */
    private List<String> serverList;

    /**
     * ApiHelper实例
     */
    private final ApiHelper api;

    public ServerSelectDialog(ServerControllerActivity activity) {
        this.activity = activity;
        api = activity.getApi();
        builder = new AlertDialog.Builder(activity);
        /* 设置View */
        layout = (LinearLayout) LayoutInflater.from(activity).inflate(R.layout.dialog_base_server, null);
        et = (EditText) layout.findViewById(R.id.et);
        et.setText(api.getServer());
        rv = (RecyclerView) layout.findViewById(R.id.rv);
        rv.setLayoutManager(new XLinearLayoutManager(activity));
        serverList = api.getStoredServerList();

        /* 设置服务器列表适配器 */
        rv.setAdapter(new XAdapter<String>(activity, () -> serverList) {
            @Override
            public void creatingHolder(CustomHolder holder, ViewTypeUnit viewTypeUnit) {
                holder.setClick(R.id.tv);   /* 点击监听 */
            }

            @Override
            public void bindingHolder(CustomHolder holder, List<String> dataList, int pos) {
                holder.setText(R.id.tv, dataList.get(pos)); /* 地址 */
            }

            @Override
            protected ViewTypeUnit getViewTypeUnitForLayout(String item) {
                return new ViewTypeUnit(R.layout.item_server_url);   /* 绑定Layout */
            }

            @Override
            protected void handleItemViewClick(CustomHolder holder, String item, int viewId, ViewTypeUnit viewTypeUnit) {
                api.setServerUrl(item); /* 设置服务器 */
                dismiss();
                activity.finish();
            }
        });
        Button btn = (Button) layout.findViewById(R.id.btn);
        btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn) {
            String string = et.getText().toString();
            if (!string.isEmpty()) {
                String url = et.getText().toString();
                api.setServerUrl(url);
                if (!serverList.contains(url)) {
                    serverList.add(url);
                    api.setStoredServerList(serverList);
                }
            }
            dismiss();
            activity.finish();
        }
    }

    /**
     * use this method to show dialog, do not use getBuilder to show
     */
    public void show() {
        if (dialog == null) {
            dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);//点击外部是否消失
        }
        dialog.show();
        dialog.getWindow().setContentView(layout);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

}
