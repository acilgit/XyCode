package xyz.xmethod.xycode.utils.serverApiHelper;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import xyz.xmethod.xycode.Xy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by XY on 2017-06-07.
 * 必须重写api()方法，并通过api()调用
 */
/*
    public static Api api() {
         if (api == null) {
            api = new Api();
         }
     return (Api) api;
   }
*/
public abstract class ApiHelper {

    protected static ApiHelper api;

    private static final String SERVER = "SERVER";
    private static final String SERVER_LIST = "SERVER_LIST";

    private static String server;

    public String getServer() {
        return getServer(null);
    }

    public String getServer(String apiAddress) {
        String url = TextUtils.isEmpty(apiAddress) ? "" : apiAddress;
        if (Xy.isRelease()) {
            server = getReleaseUrl();
        } else {
            if (server == null) {
                String tempServer = Xy.getStorage(Xy.getContext()).getString(SERVER);
                if (!TextUtils.isEmpty(tempServer)) {
                    server = tempServer;
                    return server + url;
                }
                server = getDebugUrl();
            }
        }
        url = server + url;
        return url;
    }

    /**
     * 设置当前服务器地址并保存
     *
     * @param selectedUrl
     */
    public void setServerUrl(String selectedUrl) {
        if (Xy.getStorage(Xy.getContext()).getEditor().putString(SERVER, selectedUrl).commit()) {
            server = selectedUrl;
            api = null;
        }
    }

    public boolean setStoredServerList(List<String> newServerList) {
        return (Xy.getStorage(Xy.getContext()).getEditor().putString(SERVER_LIST, JSONArray.toJSONString(newServerList)).commit());
    }

    public List<String> getStoredServerList() {
        String listString = Xy.getStorage(Xy.getContext()).getString(SERVER_LIST);
        List<String> list;
        list = setOptionUrlList(new ArrayList<>());
        if (list == null ) {
            list = new ArrayList<>();
        }
        list.add(0, getDebugUrl());
        list.add(0, getReleaseUrl());
        if (!TextUtils.isEmpty(listString)) {
            try {
                List<String> storedList = JSONArray.parseArray(listString, String.class);
                for (String url : storedList) {
                    if (!list.contains(url)) {
                        list.add(url);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    /**
     * 正式服务器地址
     *
     * @return
     */
    protected abstract String getReleaseUrl();

    /**
     * 测试服务器地址
     *
     * @return
     */
    protected abstract String getDebugUrl();

    /**
     * 其它可选服务器地址
     *
     * @param serverList
     * @return
     */
    protected abstract List<String> setOptionUrlList(List<String> serverList);
}
