package xyz.xmethod.xycode.xRefresher;

/**
 * Created by XY on 2016/6/18.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;
import xyz.xmethod.xycode.R;
import xyz.xmethod.xycode.adapter.CustomHolder;
import xyz.xmethod.xycode.adapter.OnInitList;
import xyz.xmethod.xycode.adapter.XAdapter;
import xyz.xmethod.xycode.base.XyBaseActivity;
import xyz.xmethod.xycode.okHttp.OkResponseListener;
import xyz.xmethod.xycode.okHttp.Param;
import xyz.xmethod.xycode.xRefresher.recyclerviewHelper.FlexibleDividerDecoration;
import xyz.xmethod.xycode.xRefresher.recyclerviewHelper.HorizontalDividerItemDecoration;
import xyz.xmethod.xycode.xRefresher.recyclerviewHelper.XLinearLayoutManager;

/**
 * Created by XiuYe on 2016/6/17.
 * 列表刷新器
 * <p>
 * XRefresher可以加载Header，并且刷新列表或加载更多内容
 * 所有过程都自动执行
 * 初始化时需要传入XyBaseActivity作网络请求操作
 */
public class XRefresher extends LinearLayout implements FlexibleDividerDecoration.VisibilityProvider, FlexibleDividerDecoration.SizeProvider {

    /**
     * a default header mark
     */
    public static final int HEADER_ONE = 0;

    /**
     * Refresh Type
     * 刷新列表内容
     */
    private static final int REFRESH = 1;

    /**
     * Refresh Type
     * 加载更多
     */
    private static final int LOAD = 2;

    /**
     * 初妈化XRefresher的默认值
     */
    private static InitRefresher initRefresher = null;

    /**
     * 背景ResId
     */
    private int background;

    /**
     * XyBaseActivity
     */
    private XyBaseActivity activity;

    /**
     * LayoutManager
     * 可通过方法设置
     */
    private RecyclerView.LayoutManager layoutManager;

    /**
     * 列表状态
     */
    private RefreshState state;

    /**
     * XAdapter适配器
     */
    private XAdapter adapter;

    /**
     * 对XRefresher进行配置
     */
    private RefreshSetter refreshSetter;

    /**
     * 刷新SwipeRefreshLayout，可隐藏
     */
    private SwipeRefreshLayout swipe;

    /**
     * RecyclerView
     */
    private RecyclerView recyclerView;

    /**
     * 列表数据请求
     */
    private RefreshRequest refreshRequest;

    /**
     * 是否最后一页监听器
     */
    private OnLastPageListener onLastPageListener;

    /**
     * 分割线
     */
    private HorizontalDividerItemDecoration horizontalDividerItemDecoration;

    /**
     * 分割线大小
     */
    private int dividerSize = 0;

    /**
     * 最后一个显示分割线的Item位置
     */
    private int lastVisibleItem = 0;

    /**
     * 是否需要加载更多
     */
    private boolean loadMore = false;

    /**
     * 此Layout
     */
    private LinearLayout rlMain;

    /**
     * OnSwipeListener
     */
    private OnSwipeListener swipeListener;

    /**
     * 一些默认值
     */
    private boolean defaultHeaderAdded = false;
    private boolean defaultParamAdded = false;

    /**
     * 基本属性选项
     */
    private static Options options;

    public XRefresher(Context context) {
        super(context, null);
    }

    public XRefresher(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (options == null) {
            options = new Options();
        }
        LayoutInflater.from(context).inflate(R.layout.layout_refresher, this, true);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.XRefresher);

        background = typedArray.getColor(R.styleable.XRefresher_bg, 1);

        /* 是否背景资源 */
        boolean backgroundIsRes = false;
        if (background == 1) {
            background = typedArray.getResourceId(R.styleable.XRefresher_bg, 1);
            backgroundIsRes = background != 1;
        }
        typedArray.recycle();

        rlMain = findViewById(R.id.rlMain);
        swipe = findViewById(R.id.swipe);
        recyclerView = findViewById(R.id.rvMain);
        if (backgroundIsRes) {
            rlMain.setBackgroundResource(background);
        } else if (background != 1) {
            rlMain.setBackgroundColor(background);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    /**
     * 初始化XRefresher
     *
     * @param activity
     * @param adapter
     * @return
     */
    public RefreshSetter setup(XyBaseActivity activity, XAdapter adapter) {
        refreshSetter = new RefreshSetter(this);
        layoutManager = layoutManager == null ? new XLinearLayoutManager(activity) : layoutManager;
        recyclerView.setLayoutManager(layoutManager);
        this.activity = activity;
        this.adapter = adapter;
        this.state = new RefreshState();
        this.recyclerView.setAdapter(adapter);
        this.adapter.setUseDefaultLoaderLayout(true);
        if (options.loadingRefreshingArrowColorRes != null) {
            swipe.setColorSchemeResources(options.loadingRefreshingArrowColorRes);
        }
        recyclerView.setAdapter(adapter);
        adapter.setUseDefaultLoaderLayout(true);
        return refreshSetter;
    }

    /**
     * RefreshSetter
     * 通过链式结构对XRefresher进行设置
     */
    public static class RefreshSetter {
        XRefresher refresher;
        SwipeRefreshLayout swipeRefreshLayout;

        /* 是否加载默认请求参数 */
        boolean addDefaultParam = true;
        /* 是否加载默认请求头 */
        boolean addDefaultHeader = true;

        RefreshSetter(XRefresher refresher) {
            this.refresher = refresher;
            swipeRefreshLayout = (SwipeRefreshLayout) refresher.findViewById(R.id.swipe);
        }

        /**
         * 加载更多
         *
         * @return
         */
        public RefreshSetter setLoadMore() {
            refresher.loadMore = true;
            refresher.adapter.setLoadingLayout(options.loadMoreLayoutId);
            refresher.adapter.setNoMoreLayoutId(options.noMoreLayoutId);
            refresher.adapter.setNoMoreLayoutId(options.loadRetryLayoutId);
            refresher.adapter.setLoadMoreListener(() -> {
                refresher.getDataByRefresh(refresher.state.pageIndex + 1, refresher.state.pageDefaultSize);
            });
            return this;
        }

        /**
         * 设置PageSize
         *
         * @param refreshPageSize
         * @return
         */
        public RefreshSetter setRefreshPageSize(int refreshPageSize) {
            refresher.state.setPageDefaultSize(refreshPageSize);
            return this;
        }

        /**
         * 设置请求列表监听
         */
        public RefreshSetter    setRefreshRequest(RefreshRequest refreshRequest) {
            refresher.refreshRequest = refreshRequest;
            setSwipeRefresh();
            return this;
        }

        /**
         * 设置下拉刷新监听
         */
        public RefreshSetter setOnSwipeListener(OnSwipeListener swipeListener) {
            refresher.swipeListener = swipeListener;
            setSwipeRefresh();
            return this;
        }

        /**
         * 不使用默认Header
         */
        public RefreshSetter noDefaultHeader() {
            refresher.defaultHeaderAdded = true;
            this.addDefaultHeader = false;
            return this;
        }

        /**
         * 不使用默认参数
         */
        public RefreshSetter noDefaultParam() {
            refresher.defaultParamAdded = true;
            this.addDefaultParam = false;
            return this;
        }


        /**
         * 可使用布流式布局
         *
         * @param spanCount
         * @param orientation
         */
        public RefreshSetter setStaggeredGridLayoutManager(int spanCount, int orientation) {
            refresher.layoutManager = new StaggeredGridLayoutManager(spanCount, orientation);
            refresher.getRecyclerView().setLayoutManager(refresher.layoutManager);
            return this;
        }

        /**
         * 使用Grid布局
         *
         * @param spanCount
         * @param orientation
         * @param reverseLayout
         * @param layoutManagerSpanListener
         * @return
         */
        public RefreshSetter setGridLayoutManager(int spanCount, int orientation, boolean reverseLayout, ILayoutManagerSpanListener layoutManagerSpanListener) {
            refresher.layoutManager = new GridLayoutManager(refresher.activity, spanCount, orientation, reverseLayout);
            GridLayoutManager layoutManager = (GridLayoutManager) refresher.layoutManager;
            refresher.getRecyclerView().setLayoutManager(layoutManager);
            layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (refresher.getAdapter().isHeader(position)) {
                        return layoutManager.getSpanCount();
                    }
                    switch (refresher.getAdapter().getItemViewType(position)) {
                        case XAdapter.VIEW_TYPE_FOOTER:
                        case XAdapter.VIEW_TYPE_FOOTER_LOADING:
                        case XAdapter.VIEW_TYPE_FOOTER_NO_MORE:
                        case XAdapter.VIEW_TYPE_FOOTER_RETRY:
                            return layoutManager.getSpanCount();
                        default:
                            if (layoutManagerSpanListener != null) {
                                return layoutManagerSpanListener.setSpanCount(position);
                            }
                    }
                    return 1;
                }
            });
            return this;
        }

        /**
         * 设置Divider
         *
         * @param dividerColor
         * @param dividerHeight
         * @return
         */
        public RefreshSetter setRecyclerViewDivider(@ColorRes int dividerColor, @DimenRes int dividerHeight) {
            setRecyclerViewDivider(dividerColor, dividerHeight, R.dimen.zero, R.dimen.zero);
            return this;
        }

        /**
         * use after xRefresher setup
         * 设置Divider
         *
         * @param dividerColor
         * @param dividerHeight
         * @param marginLeft
         * @param marginRight
         */
        public RefreshSetter setRecyclerViewDivider(@ColorRes int dividerColor, @DimenRes int dividerHeight, @DimenRes int marginLeft, @DimenRes int marginRight) {
            HorizontalDividerItemDecoration.Builder builder = new HorizontalDividerItemDecoration.Builder(refresher.activity)
                    .visibilityProvider(refresher)
                    .sizeProvider(refresher)
                    .colorResId(dividerColor)/*.sizeResId(dividerHeight)*/
                    .marginResId(marginLeft, marginRight);
            refresher.horizontalDividerItemDecoration = builder.build();
            refresher.dividerSize = refresher.activity.getResources().getDimensionPixelSize(dividerHeight);
            refresher.recyclerView.addItemDecoration(refresher.horizontalDividerItemDecoration);
            return this;
        }

        /**
         * 设置左右间距
         *
         * @param dividerColor
         * @param dividerHeight
         * @param gapWidthId
         * @return
         */
        public RefreshSetter setRecyclerViewDividerWithGap(@ColorRes int dividerColor, @DimenRes int dividerHeight, @DimenRes int gapWidthId) {
            HorizontalDividerItemDecoration.Builder builder = new HorizontalDividerItemDecoration.Builder(refresher.activity)
                    .visibilityProvider(refresher)
                    .sizeProvider(refresher)
                    .colorResId(dividerColor)/*.sizeResId(dividerHeight)*/
                    .setGapProvider(new HorizontalDividerItemDecoration.GapProvider() {
                        @Override
                        public int gapLeft(int pos, RecyclerView parent) {
                            int firstItemPos = refresher.getAdapter().getHeaderCount() - 1;
                            int lastItemPos = refresher.getAdapter().getHeaderCount() - 1 + refresher.getAdapter().getShowingList().size();

                            if (parent.getLayoutManager() instanceof StaggeredGridLayoutManager) {
                                StaggeredGridLayoutManager.LayoutParams lp = (StaggeredGridLayoutManager.LayoutParams) parent.getChildAt(pos).getLayoutParams();
                                int spanIndex = lp.getSpanIndex();
                                if (!lp.isFullSpan() && pos > firstItemPos && pos <= lastItemPos) {
                                    if (spanIndex == 1) {
                                        return refresher.getContext().getResources().getDimensionPixelSize(gapWidthId);
                                    }
                                }
                            }
                            return 0;
                        }

                        @Override
                        public int gapRight(int pos, RecyclerView parent) {
                            int firstItemPos = refresher.getAdapter().getHeaderCount() - 1;
                            int lastItemPos = refresher.getAdapter().getHeaderCount() - 1 + refresher.getAdapter().getShowingList().size();

                            if (parent.getLayoutManager() instanceof StaggeredGridLayoutManager) {
                                StaggeredGridLayoutManager.LayoutParams lp = (StaggeredGridLayoutManager.LayoutParams) parent.getLayoutParams();
                                int spanIndex = lp.getSpanIndex();
                                if (!lp.isFullSpan() && pos > firstItemPos && pos <= lastItemPos) {
                                    if (spanIndex == 0) {
                                        return refresher.getContext().getResources().getDimensionPixelSize(gapWidthId);
                                    }
                                }
                            }
                            return 0;
                        }

                        @Override
                        public int gapWidth(int position, RecyclerView parent) {
                            return refresher.getContext().getResources().getDimensionPixelSize(gapWidthId);
                        }
                    });
            refresher.horizontalDividerItemDecoration = builder.build();
            refresher.dividerSize = refresher.activity.getResources().getDimensionPixelSize(dividerHeight);
            refresher.recyclerView.addItemDecoration(refresher.horizontalDividerItemDecoration);
            return this;
        }

        /**
         * 最后一页监听
         *
         * @param onLastPageListener
         */
        public void setOnLastPageListener(OnLastPageListener onLastPageListener) {
            refresher.onLastPageListener = onLastPageListener;
        }

        /**
         * 刷新
         */
        private void setSwipeRefresh() {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                if (refresher.swipeListener != null) {
                    refresher.swipeListener.onRefresh();
                }
                if (refresher.refreshRequest != null) {
                    refresher.refreshList();
                }
            });
        }
    }


    /**
     * @param pageSize page size shown in one time
     */
    private void getDataByRefresh(int pageSize) {
        getDataByRefresh(1, pageSize, REFRESH);
    }

    /**
     * 加载更多
     *
     * @param page
     * @param pageSize
     */
    private void getDataByRefresh(int page, int pageSize) {
        getDataByRefresh(page, pageSize, LOAD);
    }

    /**
     * 刷新或加载更多
     * 如果取得列表数据数量少于pageSize则为最后一页
     *
     * @param page
     * @param pageSize
     * @param refreshType
     */
    private void getDataByRefresh(final int page, final int pageSize, final int refreshType) {
        Param params = new Param();
        final int postPageSize = (pageSize < state.pageDefaultSize) ? state.pageDefaultSize : pageSize;
        final int actualPage = refreshType == REFRESH ? options.firstPage : page;
        params.put(options.page, String.valueOf(actualPage));
        params.put(options.pageSize, String.valueOf(postPageSize));
        String url = refreshRequest.setRequestParamsReturnUrl(params);
        boolean addDefaultParam = true;
        boolean addDefaultHeader = true;
        /* 先从全局设置中取得请求是否添加默认值 */
        if (initRefresher != null) {
            addDefaultParam = initRefresher.addDefaultParam();
            addDefaultHeader = initRefresher.addDefaultHeader();
        }
        /*
        * 默认添加Header和Param，当设置不添加时，先把defaultHeaderAdded 设置为true
        * 会取得refreshSetter.addDefaultHeader或addDefaultParam取得false值
        * 就不会添加到请求中
        */
        refreshSetter.addDefaultHeader = defaultHeaderAdded ? refreshSetter.addDefaultHeader : addDefaultHeader;
        refreshSetter.addDefaultParam = defaultParamAdded ? refreshSetter.addDefaultParam : addDefaultParam;
        activity.newCall().url(url)
                .body(params)
                .addDefaultParams(refreshSetter.addDefaultParam)
                .addDefaultHeader(refreshSetter.addDefaultHeader)
                .call(new OkResponseListener() {
                    @Override
                    public void handleJsonSuccess(Call call, Response response, JSONObject json) {
                        /* 请求成功后，对JSON处理，返回List*/
                        List getList = refreshRequest.setListData(json);
                        final List newList;
                        /* 如果为Null妈返回空列表 */
                        if (getList == null) {
                            newList = new ArrayList<>();
                        } else {
                            newList = getList;
                        }
                        /* 设置State是否最后一页 */
                        state.setLastPage(newList.size() < postPageSize);
                        final List list = new ArrayList<>();
                        switch (refreshType) {
                            case REFRESH:
                                swipe.setRefreshing(false);
                                if (state.pageIndex == 0) {
                                    state.pageIndex++;
                                }
                                break;
                            default:
                                /* 加入到未进行过滤的列表中 */
                                list.addAll(getAdapter().getNoFilteredDataList());
                                state.pageIndex++;
                                break;
                        }
                        /* 显示加载中状态 */
                        adapter.loadingMoreEnd(state.lastPage);
                        if (newList.size() > 0) {
                            for (Object newItem : newList) {
                                boolean hasSameItem = false;
                                for (Object listItem : list) {
                                    /* 重写refreshRequest.ignoreSameItem()来过滤相同的Item */
                                    if (refreshRequest.ignoreSameItem(newItem, listItem)) {
                                        hasSameItem = true;
                                        break;
                                    }
                                }
                                if (!hasSameItem) {
                                    list.add(newItem);
                                }
                            }
                            /* 重写refreshRequest.compareTo()来进行手工排序 */
                            Collections.sort(list, (arg0, arg1) -> refreshRequest.compareTo(arg0, arg1));
                            adapter.setDataList(list);
                        } else if (refreshType == REFRESH) {
                            adapter.setDataList(list);
                        }
                        /* 设置状态 */
                        if (onLastPageListener != null) {
                            onLastPageListener.receivedList(state.lastPage);
                        }

                        if (refreshType == REFRESH) {
                            adapter.refreshedNoData();
                        }
                    }

                    @Override
                    public void handleJsonError(Call call, Response response, JSONObject json) {
                        /* 设置在InitRefresher中，对错误信息统一处理 */
                        if (!refreshRequest.handleError(call, json) && initRefresher != null) {
                            initRefresher.handleError(call, json);
                        }
                    }

                    @Override
                    protected void handleAllFailureSituation(Call call, int resultCode) {
                        switch (refreshType) {

                            case REFRESH:
                                swipe.setRefreshing(false);
                                break;
                            case LOAD:
                                adapter.loadingMoreError();
                                break;
                                default:
                        }
                        /* 设置在InitRefresher中，对所有错误信息统一处理 */
                        if (!refreshRequest.handleAllFailureSituation(call, resultCode) && initRefresher != null) {
                            initRefresher.handleAllFailureSituation(call, resultCode);
                        }
                    }
                });
    }

    /**
     * 设置下拉刷新旋转是否显示
     *
     * @param refreshing
     */
    public void setRefreshing(boolean refreshing) {
        swipe.setRefreshing(refreshing);
    }

    /**
     * refresh list and swipeListener
     */
    public void refresh() {
        swipeRefresh();
        refreshList();
    }

    /**
     * 进行Swipe刷新
     */
    public void swipeRefresh() {
        if (swipeListener != null) {
            swipeListener.onRefresh();
        }
    }

    /**
     * 进行List刷新
     */
    public void refreshList() {
        refreshList(false);
    }

    private void refreshList(boolean showDialog) {
        if (refreshRequest != null) {
            int size = getAdapter().getNoFilteredDataList().size();
            if (size > 0) {
                // 小于整页倍数时，把请求数量调整为整页倍数
                int requestPageSize = (size / state.pageDefaultSize) * state.pageDefaultSize;
                if (size % state.pageDefaultSize > 0) {
                    requestPageSize = requestPageSize + state.pageDefaultSize;
                }
                getDataByRefresh(requestPageSize);
            } else {
                getDataByRefresh(state.pageDefaultSize);
                swipe.setRefreshing(false);
            }
        }
    }

    /**
     * 手动设置最当前最后一页
     * 不建议使用
     *
     * @param page
     */
    public void setLastPage(int page) {
        this.state.pageIndex = page;
    }

    /**
     * 重置最后一面为第一页
     * 不建议使用
     */
    public void resetLastPage() {
        this.state.pageIndex = options.firstPage;
    }

    /**
     * 取得当前Adapter
     *
     * @return
     */
    public XAdapter getAdapter() {
        return adapter;
    }

    /**
     * 取得RecyclerView
     */
    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    /**
     * 取得SwipeRefreshLayout
     */
    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipe;
    }

    /**
     * 取得默认Header
     */
    public CustomHolder getHeader() {
        return getHeader(HEADER_ONE);
    }

    /**
     * 通过Key来取得Header
     *
     * @param headerKey
     * @return
     */
    public CustomHolder getHeader(int headerKey) {
        int headerPos = adapter.getHeaderPos(headerKey);
        if (headerPos < 0) {
            return null;
        }
        CustomHolder holder = (CustomHolder) getRecyclerView().getChildViewHolder(getRecyclerView().getChildAt(headerPos));
        return holder;
    }

    /**
     * 取得Footer
     * 暂时只支持单Footer
     *
     * @return
     */
    public CustomHolder getFooter() {
        if (!getAdapter().hasFooter()) {
            return null;
        }
        CustomHolder holder = (CustomHolder) getRecyclerView().getChildViewHolder(getRecyclerView().getChildAt(adapter.getItemCount() - 1));
        return holder;
    }


    /**
     * 是否最后一页
     */
    public boolean isLastPage() {
        return state.lastPage;
    }

    /**
     * 重写的Divider隐藏方法
     *
     * @param position Divider position
     * @param parent   RecyclerView
     * @return
     */
    @Override
    public boolean shouldHideDivider(int position, RecyclerView parent) {
        if (position < getAdapter().getHeaderCount()) {
            return true;
        } else if (getAdapter().hasFooter() && position == getAdapter().getItemCount() - 2) {
            return true;
        }
        return false;
    }

    @Override
    public int dividerSize(int position, RecyclerView parent) {
        if (position < getAdapter().getHeaderCount()) {
            return 0;
        } else if (getAdapter().hasFooter() && position == getAdapter().getItemCount() - 2) {
            return 0;
        }
        return dividerSize;
    }

    /**
     * 可在App中使用以设置通用选项
     *
     * @param initRefresher
     */
    public static void init(InitRefresher initRefresher) {
        init(initRefresher, new Options());
    }

    public static void init(InitRefresher initRefresher, Options options) {
        XRefresher.initRefresher = initRefresher;
        XRefresher.options = options;
        if (options.loadMoreLayoutId != 0) {
            LoadMoreView.setLayoutId(options.loadMoreLayoutId);
        }
    }

    public static class Options {
        String page = "page";
        String pageSize = "pageSize";
        int firstPage = 1;
        int loadMoreLayoutId = 0;
        int noMoreLayoutId = 0;
        int noDataLayoutId = 0;
        int loadRetryLayoutId = 0;

        @ColorRes
        int[] loadingRefreshingArrowColorRes = null;

        public Options setPageParams(String page, String pageSize, int firstPage) {
            this.page = page;
            this.pageSize = pageSize;
            this.firstPage = firstPage;
            return this;
        }

        public int getLoadMoreLayoutId() {
            return loadMoreLayoutId;
        }

        public Options setLoadMoreLayoutId(int loadMoreLayoutId) {
            this.loadMoreLayoutId = loadMoreLayoutId;
            return this;
        }

        public int getNoMoreLayoutId() {
            return noMoreLayoutId;
        }

        public Options setNoMoreLayoutId(int noMoreLayoutId) {
            this.noMoreLayoutId = noMoreLayoutId;
            return this;
        }

        public int getNoDataLayoutId() {
            return noDataLayoutId;
        }

        public Options setNoDataLayoutId(int noDataLayoutId) {
            this.noDataLayoutId = noDataLayoutId;
            return this;
        }

        public int getLoadRetryLayoutId() {
            return loadRetryLayoutId;
        }

        public Options setLoadRetryLayoutId(int loadRetryLayoutId) {
            this.loadRetryLayoutId = loadRetryLayoutId;
            return this;
        }

        public int[] getLoadingRefreshingArrowColorRes() {
            return loadingRefreshingArrowColorRes;
        }

        public Options setLoadingRefreshingArrowColorRes(int[] loadingRefreshingArrowColorRes) {
            this.loadingRefreshingArrowColorRes = loadingRefreshingArrowColorRes;
            return this;
        }
    }

    /**
     * Refresher状态
     */
    public static class RefreshState implements Serializable {
        /**
         * 是否最后一页
         */
        boolean lastPage = false;
        /**
         * 当前最后一个页面Index
         */
        int pageIndex = 0;
        /**
         * 默认页Item个数
         */
        int pageDefaultSize = 10;

        RefreshState() {
        }

        /**
         * 设置默认Size
         *
         * @param pageDefaultSize
         */
        void setPageDefaultSize(int pageDefaultSize) {
            this.pageDefaultSize = pageDefaultSize;
        }

        /**
         * 设当前置最后一页
         *
         * @param lastPage
         */
        void setLastPage(boolean lastPage) {
            this.lastPage = lastPage;
        }
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        /* 可能会有极端情况下列表List被回收，则通过此方法对列表进行重新加载 */
        if (adapter != null) {
            OnInitList onInitList = adapter.getOnInitList();
            if (onInitList != null && adapter.getNoFilteredDataList() == null) {
                try {
                    adapter.setDataList(onInitList.getList());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
