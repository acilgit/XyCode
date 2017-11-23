package xyz.xmethod.xycode.okHttp;

import android.app.Activity;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.MediaType;

/**
 * Created by XY on 2017-06-20.
 * @author xiuye
 *
 * OkHttp request Item
 */
public class CallItem {

    /**
     * OkHttp Call
     */
    Call call;

    /**
     * Item 唯一标识
     */
    String id;

    /**
     * 请求地址
     */
    String url;

    /**
     * 参数
     */
    Param body;

    /**
     * 请求头
     */
    Header header;

    /**
     * 请求方法
     */
    int method = OkHttp.POST;

    /**
     * 请求体格式
     */
    private MediaType mediaType = null;

    /**
     * 文件
     * 使用form格式
     */
    Map<String, File> files;

    /**
     * 使用Activity进行请求
     * 可自动取消加载中提示框
     */
    Activity activity;

    /**
     * 回调监听
     */
    OkResponseListener okResponseListener;

    /**
     * 文件上传监听
     */
    OkFileHelper.FileProgressListener fileProgressListener;

    /**
     * 默认添加全局参数
     */
    boolean addDefaultParams = true;
    /**
     * 默认添加全局请求头
     */
    boolean addDefaultHeader = true;

    /**
     * 进行请求
     *
     * @param okResponseListener
     */
    public void call(OkResponseListener okResponseListener) {
        this.okResponseListener = okResponseListener;
        if(header == null) {
            header = new Header();
        }
        if (files != null) {
           call = OkHttp.uploadFiles(activity, url, files, body, header, addDefaultHeader, addDefaultParams, okResponseListener, fileProgressListener);
        } else {
           OkHttp.request(mediaType, method, activity, url, body, addDefaultParams, header, addDefaultHeader, okResponseListener);
        }
    }

    /**
     * Get请求
     * @return
     */
    public CallItem get() {
        this.method = OkHttp.GET;
        return this;
    }

    /**
     * 请求地址
     * @param url
     * @return
     */
    public CallItem url(String url) {
        this.url = url;
        return this;
    }

    /**
     * get请求中会把Body中的数据拼接成地址
      */
    public CallItem body(Param body) {
        this.body = body;
        return this;
    }

    /**
     * Header
     * @param header
     * @return
     */
    public CallItem header(Header header) {
        this.header = header;
        return this;
    }

    /**
     * 请求类型
     * @param mediaType
     * @return
     */
    public CallItem setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    /**
     * 文件Map
     * @param files
     * @param fileProgressListener
     * @return
     */
    public CallItem files(Map<String, File> files, OkFileHelper.FileProgressListener fileProgressListener) {
        this.files = files;
        this.fileProgressListener = fileProgressListener;
        return this;
    }

    /**
     * 上传单个文件
     * @param fileKey
     * @param file
     * @param fileProgressListener
     * @return
     */
    public CallItem oneFile(String fileKey, File file, OkFileHelper.FileProgressListener fileProgressListener) {
        HashMap<String, File> files = new HashMap<>();
        files.put(fileKey, file);
        this.files = files;
        this.fileProgressListener = fileProgressListener;
        return this;
    }

    public CallItem addDefaultParams(boolean addDefaultParams) {
        this.addDefaultParams = addDefaultParams;
        return this;
    }

    public CallItem addDefaultHeader(boolean addDefaultHeader) {
        this.addDefaultHeader = addDefaultHeader;
        return this;
    }

    /**
     * 执行完call()后会返回call，但call也可能为空，请进行判断
     * @return
     */
    public Call getCall() {
        return call;
    }
}
