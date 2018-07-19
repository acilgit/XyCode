package xyz.xmethod.xycode.base;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import xyz.xmethod.xycode.interfaces.Interfaces;
import xyz.xmethod.xycode.unit.MsgEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public abstract class XyBaseFragment extends Fragment {

    boolean loaded = false;
    private boolean loadFailed = false;

    protected XyBaseActivity getThis() {
        return (XyBaseActivity) getActivity();
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (useEventBus()) {
            EventBus.getDefault().register(this);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return  inflater.inflate(setFragmentLayout(), container, false);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initOnCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        if (useEventBus()) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }

    protected abstract void initOnCreate(Bundle savedInstanceState);

    protected abstract int setFragmentLayout();
    /**
     * EventBus
     */
    protected abstract boolean useEventBus();

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


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MsgEvent event) {

    }

    @Subscribe
    public void onEventBackground(MsgEvent event) {

    }






}
