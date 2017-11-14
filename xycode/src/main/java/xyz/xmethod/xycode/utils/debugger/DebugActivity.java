package xyz.xmethod.xycode.utils.debugger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import xyz.xmethod.xycode.R;
import xyz.xmethod.xycode.Xy;
import xyz.xmethod.xycode.adapter.CustomHolder;
import xyz.xmethod.xycode.adapter.XAdapter;
import xyz.xmethod.xycode.base.XyBaseActivity;
import xyz.xmethod.xycode.okHttp.Param;
import xyz.xmethod.xycode.unit.ViewTypeUnit;
import xyz.xmethod.xycode.utils.TS;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 处理请求的Debug页面
 * 每一个请求都会生成一个DebugItem
 * 可对每个OkHttp发出的请求参数及结果进行调试适配
 *
 * 该类的所有方法都不需要调用，通过OkHttp自动调用完成
 *
 * 只需要在LogLayout中点击『DEBUG』按键即可打开调试模式
 *
 * 注意：请勿在使用OnResume()的时候进行请求时打开Debug模式，否则会出现循环调用的情况
 *
 * @author xiuye
 */
public class DebugActivity extends XyBaseActivity {

    /**
     * 一些静态变量名称
     */
    public static final String DEBUG_KEY = "DEBUG_KEY";
    public static final String PARAMS_JSON = "PARAMS_JSON";
    public static final String POST_IS_FINISH = "POST_IS_FINISH";
    public static final String POST_BEGIN = "POST_BEGIN";
    public static final String POST_URL = "POST_URL";

    /**
     * 当前页面实例
     */
    private static DebugActivity instance;

    /**
     * 取得当前页面实例
     */
    public static DebugActivity getInstance() {
        return instance;
    }

    /**
     * 调试Item集合
     */
    private static Map<String, DebugItem> debugItems = new ConcurrentHashMap<>();

    /**
     * 准备进行请求
     */
    private boolean postPrepare;

    /**
     * 调试Item
     */
    private DebugItem debugItem;

    /**
     * 参数适配器
     */
    private XAdapter<ParamItem> adapter = null;

    /**
     * 添加新的DebugItem
     * @param url
     * @return
     */
    public static DebugItem addDebugItem(String url) {
        DebugItem debugItem = new DebugItem(url);
        debugItems.put(debugItem.getKey(), debugItem);
        return debugItem;
    }

    /**
     * 取得DebugItem
     * @param key
     * @return
     */
    public static DebugItem getDebugItem(String key) {
        return debugItems.get(key);
    }


    /**
     * 修改请求参数页面
     * @param debugKey
     * @param param
     */
    public static void startThis(String debugKey, Param param) {
        Intent intent = new Intent(Xy.getContext(), DebugActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(DebugActivity.DEBUG_KEY, debugKey)
                .putExtra(DebugActivity.POST_BEGIN, true);
        getDebugItem(debugKey).setParam(param);
        Xy.getContext().startActivity(intent);
    }

    /**
     * 修改请求返回结果页面
     * 此方法由OkHttp调用
     * @param debugKey
     */
    public static void startThis(String debugKey) {
        Intent intent = new Intent(Xy.getContext(), DebugActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(DebugActivity.DEBUG_KEY, debugKey);
//        getDebugItem(debugKey);
        Xy.getContext().startActivity(intent);
    }

    @Override
    protected int setActivityLayout() {
        return R.layout.activity_debug;
    }

    @Override
    protected void initOnCreate(Bundle savedInstanceState) {
        DebugActivity.instance = this;
        String key = getIntent().getStringExtra(DEBUG_KEY);
        debugItem = getDebugItem(key);

        if (debugItem == null) {
            TS.show(getThis(), "Error: No DebugItem [" + key + "]", null);
            finish();
            return;
        }

        /* 修改参数 */
        postPrepare = getIntent().getBooleanExtra(DebugActivity.POST_BEGIN, false);
        if (postPrepare) {
            rootHolder().setVisibility(R.id.rv, View.VISIBLE)
                    .setVisibility(R.id.scrollView, View.GONE);
            RecyclerView rv = rootHolder().getRecyclerView(R.id.rv);
            rv.setLayoutManager(new LinearLayoutManager(getThis()));
            /* 展示参数列表 */
            adapter = new XAdapter<ParamItem>(getThis(), () -> {
                List list = new ArrayList();
                for (Map.Entry<String, String> entry : debugItem.getParam().entrySet()) {
                    list.add(new ParamItem(entry.getKey(), entry.getValue()));
                }
                return list;
            }) {
                @Override
                protected ViewTypeUnit getViewTypeUnitForLayout(ParamItem item) {
                    return new ViewTypeUnit(0, R.layout.item_debug_param);
                }

                @Override
                public void creatingHolder(CustomHolder holder, ViewTypeUnit viewTypeUnit) throws Exception {
                    /* 改变参数名 */
                    ((EditText) holder.getView(R.id.etName)).addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            adapter.getShowingList().get(holder.getAdapterPosition()).setKey(s.toString());
                        }
                    });
                    /* 改变参数 */
                    ((EditText) holder.getView(R.id.etValue)).addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            adapter.getShowingList().get(holder.getAdapterPosition()).setValue(s.toString());
                        }
                    });
                }

                @Override
                public void bindingHolder(CustomHolder holder, List<ParamItem> dataList, int pos) throws Exception {
                    holder.setText(R.id.etName, dataList.get(pos).getKey()) /* 参数名 */
                            .setText(R.id.etValue, dataList.get(pos).getValue());   /* 参数值 */
                }

                @Override
                protected void creatingFooter(CustomHolder holder) {
                    /* 添加参数 */
                    holder.setClick(R.id.tvAdd, v -> {
                        adapter.addItem(new ParamItem("", ""));
                    });
                }
            };
            adapter.setFooter(R.layout.footer_debug);
            adapter.setShowNoDataFooter(true);
            rv.setAdapter(adapter);

        } else {
            rootHolder().setVisibility(R.id.rv, View.GONE)
                    .setVisibility(R.id.scrollView, View.VISIBLE);

        }

        /* Debug内容 */
        rootHolder().setText(R.id.tvTitle, "[" + (postPrepare ? "Param" : "Result") + " Debug] " + debugItem.getUrl());
        /* 请求结果 */
        ((EditText) rootHolder().getView(R.id.et)).setText(debugItem.getJson());
        rootHolder().setClick(R.id.tvCancel, v -> {
            setStatus();
            finish();
        }).setClick(R.id.tvCommit, v -> {
            if (postPrepare) {
                Param param = new Param();
                for (ParamItem paramItem : adapter.getShowingList()) {
                    if (paramItem.getKey() != null && !TextUtils.isEmpty(paramItem.getKey().trim())) {
                        param.add(paramItem.getKey().trim(), paramItem.getValue());
                    }
                }
                debugItem.setParam(param);
            } else {
                debugItem.setJsonModify(rootHolder().getText(R.id.et));
            }
            setStatus();
            finish();
        });
    }

    /**
     * 点击的加
     */
    @Override
    public void onBackPressed() {
        setStatus();
        super.onBackPressed();

    }

    /**
     * 设置Debug状态
     */
    private void setStatus() {
        debugItem.setPostBegun(postPrepare);
        debugItem.setPostFinished(!postPrepare);
        hideSoftInput();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected AlertDialog setLoadingDialog() {
        return null;
    }

    /**
     * 参数Item
     */
    static class ParamItem {
        String key;
        String value;

        public ParamItem(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
