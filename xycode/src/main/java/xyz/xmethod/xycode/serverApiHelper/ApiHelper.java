package xyz.xmethod.xycode.serverApiHelper;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import xyz.xmethod.xycode.Xy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by XY on 2017-06-07.
 * 须添加api()方法，并通过api()调用接口:
     public static Api api() {
         if (api == null) {
              api = new Api();
          }
          return (Api) api;
     }
 *
 * 接口写法：
 * public final String egApi = getServer("/egApi");
 * 使用接口：
 * OkHttp.newCall().url(api().egApi).call()
 */
public abstract class ApiHelper {

    /**
     * ApiHelper实例
     */
    protected static ApiHelper api;

    /**
     * 一些静态变量
     */
    private static final String SERVER = "SERVER";
    private static final String SERVER_LIST = "SERVER_LIST";

    /**
     * 当前使用的服务器地址
     */
    private static String server;

    /**
     * 取得当前服务器地址
     * @return
     */
    protected String getServer() {
        return getServer(null);
    }

    /**
     * 取得当前接口+服务器地址
     * @param apiAddress
     * @return
     */
    protected String getServer(String apiAddress) {
        String url = TextUtils.isEmpty(apiAddress) ? "" : apiAddress;
        /* 如果是正式版，则只取得正式服务器地址 */
        if (Xy.isRelease()) {
            server = getReleaseUrl();
        } else {
            /* 测试版可以在服务器列表中选择或输入新的服务器地址 */
            if (server == null) {
                String tempServer = Xy.getStorage().getString(SERVER);
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
    void setServerUrl(String selectedUrl) {
        if (Xy.getStorage().getEditor().putString(SERVER, selectedUrl).commit()) {
            server = selectedUrl;
            api = null;
        }
    }

    /**
     * 设置服务器地址列表
     * @param newServerList
     * @return
     */
    boolean setStoredServerList(List<String> newServerList) {
        return (Xy.getStorage().getEditor().putString(SERVER_LIST, JSONArray.toJSONString(newServerList)).commit());
    }

    /**
     * 取得服务器地址列表
     */
    List<String> getStoredServerList() {
        String listString = Xy.getStorage().getString(SERVER_LIST);
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
     * @param serverList 已实例化，可直接add()
     * @return
     */
    protected abstract List<String> setOptionUrlList(List<String> serverList);
}
