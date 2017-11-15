package xyz.xmethod.xycode.debugHelper.logHelper;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import xyz.xmethod.xycode.R;
import xyz.xmethod.xycode.adapter.CustomHolder;
import xyz.xmethod.xycode.adapter.XAdapter;
import xyz.xmethod.xycode.okHttp.OkHttp;
import xyz.xmethod.xycode.xRefresher.recyclerviewHelper.XLinearLayoutManager;
import xyz.xmethod.xycode.unit.ViewTypeUnit;
import xyz.xmethod.xycode.utils.Tools;

import java.util.List;

/**
 * Created by XY on 2017-06-03.
 * 用于显示Log的内容
 * 在屏幕右边滑动展开，展开后右滑隐藏
 *
 * 在Debug模式下，可以进行OkHttp请求调试
 *
 * @author xiuye
 */

public class LogLayout {

    /**
     * Context
     */
    private final Context context;

    /**
     * RootView
     */
    private RelativeLayout rootView;

    /**
     * Log适配器
     */
    private XAdapter<LogItem> adapter;

    /**
     * 屏幕宽
     */
    private int screenWidth;

    /**
     * 屏幕高
     */
    private int screenHeight;

    /**
     * Holder
     */
    private CustomHolder holder;

    /**
     * 被选择的Item Position
     * 没被选中的Item只会显示部分内容
     */
    private int selectItemPos = -1;

    /**
     * 正在显示
     */
    private boolean showing = false;

    /**
     * 最小滑动距离
     */
    private static int miniSlideWidth = 40;

    /**
     * 没被选中的Item最长内容展示长度
     */
    private static final int maxItemContentLength = 400;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Integer x = (Integer) msg.obj;
                slideLayout(x);
            }
        }
    };

    public LogLayout(Context context) {
        this.context = context;
        init();
    }

    /**
     * 初始化LogLayout
     */
    private void init() {
        rootView = (RelativeLayout) LayoutInflater.from(context).inflate(xyz.xmethod.xycode.R.layout.layout_base_console_view, null);
        rootView.setOnTouchListener(rootViewTouchListener);
        holder = new CustomHolder(rootView);
        /* 设置滑动监听 */
        holder.getView(R.id.vTouch).setOnTouchListener(slideBackTouchListener);
        /* Log适配器 */
        adapter = new XAdapter<LogItem>(context, () -> L.getLogList()) {
            @Override
            protected ViewTypeUnit getViewTypeUnitForLayout(LogItem item) {
                return new ViewTypeUnit(0, R.layout.item_log);
            }

            @Override
            public void creatingHolder(CustomHolder holder, ViewTypeUnit viewTypeUnit) {
                holder.setClick(R.id.tvContent, v -> {
                    selectItemPos = selectItemPos == holder.getAdapterPosition() ? -1 : holder.getAdapterPosition();
                    notifyDataSetChanged();
                });
            }

            @Override
            public void bindingHolder(CustomHolder holder, List<LogItem> dataList, int pos) {
                LogItem item = dataList.get(pos);
                /* 设置展示颜色 */
                int contentColor;
                switch (item.getType()) {
                    case LogItem.LOG_TYPE_CRASH:
                        contentColor = android.R.color.holo_red_light;
                        break;
                    case LogItem.LOG_TYPE_D:
                        contentColor = R.color.logTextDebug;
                        break;
                    case LogItem.LOG_TYPE_I:
                        contentColor = R.color.logTextInfo;
                        break;
                    default:
                        contentColor = android.R.color.white;
                        break;
                }
                /* 设置内容 */
                String content = (selectItemPos != pos && item.getContent().length() > maxItemContentLength) ? item.getContent().substring(0, maxItemContentLength) : item.getContent();
                holder.setText(R.id.tvDateTime, item.getDateTime() + " [" + (pos + 1) + "]")    /* 时间 */
                        .setText(R.id.tvTitle, item.getTitle()) /* 标题 */
                        .setVisibility(R.id.tvTitle, TextUtils.isEmpty(item.getTitle()) ? View.GONE : View.VISIBLE) /* 标题是否可见 */
                        .setVisibility(R.id.tvContent, TextUtils.isEmpty(item.getContent()) ? View.GONE : View.VISIBLE) /* 内容是否可见 */
                        .setText(R.id.tvContent, content)   /* 内容 */
                        .setTextColor(R.id.tvContent, context.getResources().getColor(contentColor))    /* 内容颜色 */
                        .setViewBackground(R.id.llItem, OkHttp.isDebugMode() ? R.color.transparentRedLite : 0); /* 调式模式下的颜色 */
                holder.getView(R.id.llBg).setBackgroundResource((selectItemPos != pos && item.getContent().length() > maxItemContentLength) ? R.color.bgBlue : 0);  /* 选中的内容背景颜色 */
            }
        };

        screenWidth = Tools.getScreenSize().x;
        screenHeight = Tools.getScreenSize().y;
        slideLayout(screenWidth);
        LinearLayout llLog = holder.getView(R.id.llLog);
        llLog.setLayoutTransition(new LayoutTransition());

        RecyclerView rv = holder.getRecyclerView(R.id.rv);
        rv.addItemDecoration(Tools.getHorizontalDivider(R.color.grayLite, R.dimen.dividerLineHeight, R.dimen.zero, R.dimen.zero));
        rv.setLayoutManager(new XLinearLayoutManager(context));
        rv.setAdapter(adapter);
        holder.setClick(R.id.tvTop, v -> {
            if (adapter.getShowingList().size() > 0) {
                rv.scrollToPosition(0);
            }
        });
        holder.setClick(R.id.tvBottom, v -> {
            if (adapter.getShowingList().size() > 0) {
                rv.scrollToPosition(adapter.getShowingList().size() - 1);
            }
        });
        holder.setClick(R.id.tvDebug, v -> {
            OkHttp.setDebugMode(!OkHttp.isDebugMode());
            adapter.notifyDataSetChanged();
//            holder.getTextView(R.id.tvDebug).setTextColor(context.getResources().getColor(OkHttp.isDebugMode() ? R.color.transparentRedLite : R.color.transparent));
        });

//        holder.getView(R.id.vDebug).setBackgroundColor(context.getResources().getColor(OkHttp.isDebugMode() ? R.color.transparentRedLite : R.color.transparent));
    }

    public View getView() {
        return rootView;
    }

    /**
     * 右滑隐藏Layout
     */
    private View.OnTouchListener slideBackTouchListener = new View.OnTouchListener() {
        boolean touching = false;
        boolean sliding = false;
        boolean canMove = false;
        float previousX, previousY, downX, downY;

        @Override
        public boolean onTouch(View v, MotionEvent e) {
            float x = e.getX();
            float y = e.getY();
            float dx;

            showing = holder.getView(R.id.llLog).getX() < screenWidth;
            touching = false;
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touching = true;
                    downX = x;
                    downY = y;
                    canMove = downX < screenWidth / 5 && showing;
                    break;
                case MotionEvent.ACTION_MOVE:
                    touching = true;
                    dx = x - downX;
                    if (canMove && (x >= downX)) {
                        sliding = true;
                        slideLayout((int) (dx));
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    touching = false;
                    dx = x - downX;
                    if (sliding && dx > 0) {
                        slideAnimate((int) (dx), dx > screenWidth / 20);
                        sliding = false;
                    }
                    canMove = false;
                    break;
                case MotionEvent.ACTION_CANCEL:
                    touching = false;
                    dx = x - downX;
                    if (sliding && dx > 0) {
                        slideAnimate((int) (dx), false);
                        sliding = false;
                    }
                    canMove = false;
                    break;
                default:
            }
            previousX = x;
            previousY = y;
            return canMove;
        }
    };

    /**
     * 左滑唤出Layout
     */
    private View.OnTouchListener rootViewTouchListener = new View.OnTouchListener() {
        boolean touching = false;
        boolean sliding = false;
        float previousX, previousY, downX, downY;

        @Override
        public boolean onTouch(View v, MotionEvent e) {
            float x = e.getX();
            float y = e.getY();

            float dx;
            touching = false;
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touching = true;
                    downX = x;
                    downY = y;
                    break;
                case MotionEvent.ACTION_MOVE:
                    touching = true;
                    dx = x - downX;
                    /* 从屏幕右下2/5的地方滑动才可以唤出 */
                    if (downX > screenWidth - miniSlideWidth && dx < 0 && (sliding || y > screenHeight - (screenHeight / (screenWidth > screenHeight ? 2 : 5)))) {
                        sliding = true;
                        slideLayout((int) x);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    touching = false;
                    dx = x - downX;
                    if (sliding) {
                        slideAnimate((int) x, previousX < x);
                        sliding = false;
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    touching = false;
                    if (sliding) {
                        slideAnimate((int) x, true);
                        sliding = false;
                    }
                    break;
                default:
            }
            previousX = x;
            previousY = y;
            return touching && downX > screenWidth - miniSlideWidth;
        }
    };

    /**
     * 滑动Layout
     * @param x
     */
    private void slideLayout(int x) {
        LinearLayout llLog = holder.getView(R.id.llLog);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) llLog.getLayoutParams();
        params.setMargins(x, 0, -x, 0);
        llLog.setLayoutParams(params);
    }

    /**
     * 滑动动画
     * @param x
     * @param isHide
     */
    private void slideAnimate(int x, boolean isHide) {
        int step = (isHide ? (screenWidth - x) : x) / 40;
        for (int i = 0; i < 39; i++) {
            Message newMsg = new Message();
            newMsg.what = 1;
            newMsg.obj = isHide ? x + step * i : x - step * i;
            handler.sendMessageDelayed(newMsg, 3 * i);
        }
        Message newMsg = new Message();
        newMsg.what = 1;
        newMsg.obj = isHide ? screenWidth : 0;
        handler.sendMessageDelayed(newMsg, 3 * 39);

    }

    /**
     * 直接绑定到Activity
     *
     * @param activity
     * @return
     */
    public static LogLayout attachLogLayoutToActivity(Activity activity) {
        LogLayout logLayout = new LogLayout(activity);
        ((ViewGroup) activity.getWindow().getDecorView().getRootView()).addView(logLayout.getView());
        return logLayout;
    }

    public void removeLayout() {
        if (rootView != null) {
            try {
                ((ViewGroup) rootView.getParent()).removeView(rootView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
