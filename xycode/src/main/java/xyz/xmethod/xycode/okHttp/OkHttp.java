package xyz.xmethod.xycode.okHttp;

import android.app.Activity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import xyz.xmethod.xycode.base.XyBaseActivity;
import xyz.xmethod.xycode.utils.LogUtil.JsonTool;
import xyz.xmethod.xycode.utils.LogUtil.L;
import xyz.xmethod.xycode.utils.debugger.DebugActivity;
import xyz.xmethod.xycode.utils.debugger.DebugItem;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by XiuYe on 2016/7/7
 * 对 OkHttp3 进行封装
 *
 * 网络请求在RxJava的IO线程中进行
 * 上传文件则使用OkHttp的线程池进行请求
 */
public class OkHttp {

    /**
     * MediaType
     */
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType MEDIA_TYPE_MULTI_DATA = MediaType.parse("multipart/form-data; charset=utf-8");
    @Deprecated
    public static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");
    @Deprecated
    public static final MediaType MEDIA_TYPE_URL_ENCODED = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    private static MediaType mediaType = MEDIA_TYPE_MULTI_DATA;

    /**
     * 一些静态变量
     */
    public static final String UTF8 = "UTF-8";
    public static final String FILE = "file";
    public static final byte[] lock = new byte[0];

    public static final int POST = 0;
    public static final int GET = 1;

    /**
     * 还没有对结果进入处理
     */
    public static final int RESULT_BLANK = 404;
    public static final int RESULT_ERROR = 0;
    public static final int RESULT_SUCCESS = 1;
    public static final int RESULT_VERIFY_ERROR = -1;
    public static final int RESULT_OTHER = 2;

    public static final int RESULT_PARSE_FAILED = 880;
    public static final int NETWORK_ERROR_CODE = 881;
    public static final int NO_NETWORK = 882;

    /**
     * OkHttpClient
     */
    private static OkHttpClient client;

    /**
     * 初始化接口
     */
    private static IOkInit okInit;
    private static OkHttp.OkOptions okOptions;

    private static OkHttp instance;

    /**
     * Debug模式开启
     */
    private static boolean debugMode = false;
//    private static Context application;

    private static Map<String, CallItem> callItems;

    public static OkHttp getInstance() {
        if (instance == null) {
            instance = new OkHttp();
        }
        return instance;
    }

    /**
     * init
     */
    public static void init(IOkInit iOkInit) {
        if (okInit == null) {
            okInit = iOkInit;
        }
    }

    public static void init(IOkInit iOkInit, OkOptions okOptions) {
        if (okInit == null) {
            okInit = iOkInit;
            OkHttp.okOptions = okOptions;
        }
    }


    /**
     * you can use client as you like
     * when use ali-hotfix, set client by this method
     *
     * @param iOkInit
     * @param client
     */
    public static void init(IOkInit iOkInit, OkHttpClient client) {
        okInit = iOkInit;
        OkHttp.client = client;
    }

    public static boolean isDebugMode() {
        return debugMode;
    }

    public static void setDebugMode(boolean debugMode) {
        OkHttp.debugMode = debugMode;
    }

    public static void setRequestMediaType(MediaType mediaType) {
        OkHttp.mediaType = mediaType;
    }

    /**
     * 上传文件的线程池大小
     */
    public static void setMaxTransFileCount(int max) {
        getClient().dispatcher().setMaxRequestsPerHost(max);
    }

    public static OkHttpClient getClient() {
        synchronized (lock) {
            if (client == null) {
                OkHttpClient.Builder builder = new OkHttpClient.Builder()
                        .readTimeout(OkOptions.readTimeout, TimeUnit.SECONDS)
                        .connectTimeout(OkOptions.connectTimeout, TimeUnit.SECONDS)
                        .writeTimeout(OkOptions.writeTimeout, TimeUnit.SECONDS);
                if (okOptions != null) {
                    // 添加证书 或处理Builder
                    okOptions.setOkHttpBuilder(builder);
                }
                client = builder.build();
            }
            return client;
        }
    }

    public static Map<String, CallItem> getCallItems() {
        if (callItems == null) {
            callItems = new HashMap<>();
        } else {
            for (String key : callItems.keySet()) {
                if (callItems.get(key) == null) {
                    callItems.remove(key);
                }
            }
        }
        return callItems;
    }

    public static CallItem newCall(Activity activity) {
        CallItem callItem = new CallItem();
        callItem.id = String.valueOf(UUID.randomUUID());
        callItem.activity = activity;
        getCallItems().put(callItem.id, callItem);
        return callItem;
    }

    /**
     * 网络请求命令，只供CallItem调用
     *
     * @param activity           当传入Activity时，或者Activity没有被销毁则运行在主线程，否则运行在IO线程
     * @param url                请求地址，Get请求只要把拼接参数写进param内则可
     * @param params             Post或Get的参数
     * @param addDefaultParams   添加默认参数
     * @param header             请求头
     * @param addDefaultHeader   添加默认请求头
     * @param okResponseListener 回调监听
     * @return
     */
    static void request(MediaType itemMediaType, int method, final Activity activity, String url, Param params, boolean addDefaultParams, Header header, boolean addDefaultHeader,
                        final OkResponseListener okResponseListener) {
        final Call[] call = {null};
        // 使用RxJava2进行请求管理
        Observable.create(
                (ObservableOnSubscribe<ResponseItem>) observableEmitter -> {
                    // 参数加默认参数供Get请求处理
                    Param allParam = new Param();
                    // Log内容
                    StringBuffer sb = new StringBuffer();
                    // Log标题
                    String logTitle;

                    FormBody.Builder formBodyBuilder = new FormBody.Builder();
                    // 参数处理，并且把参数
                    try {
                        if (params != null) {
                            for (String key : params.keySet()) {
                                allParam.add(key, params.getKey(key));
                            }
                        }
                        if (addDefaultParams) {
                            Param defaultParams = okInit.setDefaultParams(new Param());
                            for (String key : defaultParams.keySet()) {
                                if (params != null && params.containsKey(key)) {
//                                    sb.append(" (ignored)");
                                } else {
                                    allParam.add(key, defaultParams.getKey(key));
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        L.e("[Params Error] " + url, sb.toString());
                        // 参数错误时抛出异常
                        throw e;
                    }


                    // DebugItem key
                    final String debugKey;
                    String keyForDebug = null;
                    if (debugMode) {
                        DebugItem debugItem = DebugActivity.addDebugItem(url);
                        keyForDebug = debugItem.getKey();

                        DebugActivity.startThis(debugItem.getKey(), allParam);

                        for (int i = 0; i < 600; i++) {
                            if (debugItem.isPostBegun()) {
                                break;
                            } else {
                                Thread.sleep(500);
                            }
                        }
                        allParam = debugItem.getParam();
                    }
                    debugKey = keyForDebug;

                    Param newParam = okInit.setParamsHeadersBeforeRequest(allParam, header);
                    if (newParam != null) {
                        allParam = newParam;
                    }

                    for (Map.Entry<String, String> entry : allParam.entrySet()) {
                        if (sb.length() == 0) {
                            sb.append("[Params]");
                        }
                        sb.append("\n  ").append(entry.getKey()).append(": ").append(entry.getValue());
                    }

                    for (String key : allParam.keySet()) {
                        formBodyBuilder.add(key, allParam.get(key));
                    }

                    FormBody body = formBodyBuilder.build();

                    final Request.Builder builder;
                    // 处理请求方法
                    if (method == POST) {
                        builder = new Request.Builder().url(url);
                        if ((itemMediaType != null && itemMediaType.equals(MEDIA_TYPE_JSON)) || (itemMediaType == null && mediaType.equals(MEDIA_TYPE_JSON))) {
                            RequestBody jsonBody = RequestBody.create(mediaType, JSON.toJSONString(allParam));
                            builder.post(jsonBody);
                        } else {
                            builder.post(body);
                        }
                        logTitle = "[POST] " + url;
                    } else {
                        StringBuilder sbGet = new StringBuilder(url);
                        //迭代Map拼接请求参数
                        try {
                            sbGet.append(allParam.isEmpty() ? "" : "?");
                            for (Map.Entry<String, String> entry : allParam.entrySet()) {
                                sbGet.append(entry.getKey()).append('=').append(URLEncoder.encode(entry.getValue(), UTF8));
                            }
                            if (!allParam.isEmpty()) {
                                sbGet.deleteCharAt(sbGet.length() - 1);
                            }
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        builder = new Request.Builder().url(sbGet.toString());
                        builder.get();
                        logTitle = "[GET] " + url;
                    }
                    // 请求头处理
                    Header defaultHeader = okInit.setDefaultHeader(new Header());
                    if (header != null && header.size() > 0 || (addDefaultHeader && defaultHeader.size() > 0)) {
                        if (sb.length() > 0) {
                            sb.append("\n");
                        }
                        sb.append("[Headers]");
                    }
                    if (addDefaultHeader && defaultHeader != null) {
                        for (String key : defaultHeader.keySet()) {
                            sb.append("\n  ").append(key).append(": ").append(defaultHeader.get(key));
                            if (header != null && header.containsKey(key)) {
                                sb.append(" (ignored)");
                            } else {
                                builder.addHeader(key, defaultHeader.get(key));
                            }
                        }
                    }
                    if (header != null) {
                        for (String key : header.keySet()) {
                            sb.append("\n  ").append(key).append(": ").append(header.get(key));
                            builder.addHeader(key, header.get(key));
                        }
                    }
                    // 新建okHttp请求
                    final Request request = builder.build();
                    call[0] = getClient().newCall(request);

                    L.e(logTitle, sb.toString());

                    final Response response = call[0].execute();
                    if (call[0] != null && call[0].isCanceled()) {
                        L.e("[Call canceled] " + url, "");
                    } else {
//                        ResponseItem responseItem = new ResponseItem(response, call[0], okResponseListener);
                        final ResponseItem responseItem = new ResponseItem(response, call[0], url, okResponseListener);
                        if (response != null) {
                            responseResult(responseItem, debugKey);
                            response.close();

                            observableEmitter.onNext(responseItem);
                        } else {
                            // 没有返回数据
                            noResponse(call[0], okResponseListener);
                        }
                    }
                    // 请求完成
                    observableEmitter.onComplete();
                }).subscribeOn(Schedulers.io())
                // 当传入Activity时，或者Activity没有被销毁则运行在主线程，否则运行在IO线程
                .observeOn(activity == null ? Schedulers.io() : AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseItem>() {
                    private Disposable d;

                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable disposable) {
                        d = disposable;
                    }

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull ResponseItem responseItem) {
                        // 处理返回结果
                        XyBaseActivity.dismissLoadingDialogByManualState();
                        handleResultWithResultCode(responseItem);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable throwable) {
                        // 处理请求中出现异常
                        throwable.printStackTrace();
                        noResponse(call[0], okResponseListener);
                        XyBaseActivity.dismissLoadingDialogByManualState();
                    }

                    @Override
                    public void onComplete() {
                        // 请求完成，关闭自动关闭的等待对话框
//                        XyBaseActivity.dismissLoadingDialogByManualState();
                    }
                });
    }

    /**
     * when response success
     *
     * @param responseItem
     * @return
     * @throws Exception
     */
    private static ResponseItem responseResult(ResponseItem responseItem, String debugKey) throws Exception {
        Response response = responseItem.getResponse();
        OkResponseListener okResponseListener = responseItem.getOkResponseListener();
        Call call = responseItem.getCall();
//        Call call = null;
        if (response.isSuccessful()) {
            String responseStr = "";
            try {
                final String strResult = response.body().string();
                responseStr = strResult;

                responseItem.setStrResult(responseStr);

                if (debugMode) {
                    DebugItem debugItem = DebugActivity.getDebugItem(debugKey);
                    if (debugItem != null) {
                        debugItem.setJson(responseItem.getStrResult());
                        responseItem.setDebugKey(debugItem.getKey());

                        DebugActivity.startThis(debugItem.getKey());

                        for (int i = 0; i < 600; i++) {
                            if (debugItem.isPostFinished()) {
                                break;
                            } else {
                                Thread.sleep(500);
                            }
                        }
                        if (debugItem.getJsonModify()!= null) {
                            responseItem.setStrResult(debugItem.getJsonModify());
                        }
                    }
                }

                final JSONObject jsonObject = JSON.parseObject(responseItem.getStrResult());
                final int resultCode = okInit.judgeResultWhenFirstReceivedResponse(call, response, jsonObject);
                responseItem.setResultCode(resultCode);
                responseItem.setJsonObject(jsonObject);
                // 判断返回的ResultCode是否可以继续操作，可以此方法执行后台操作，如集中保存数据到数据库
                if (okInit.resultSuccessByJudge(call, response, jsonObject, resultCode)) {
                    L.e("[resultJudgeFailed] " + responseItem.getUrl(), JsonTool.stringToJSON(strResult));
                    return null;
                }
                if (okResponseListener == null) {
                    return null;
                }
                try {
                    // 先在当前线程中处理
                    okResponseListener.handleSuccessInBackground(call, jsonObject);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                // 解释Json错误
                e.printStackTrace();
                L.e("[JsonParseFailed] " + responseItem.getUrl(), "[Error]\n" + e.getMessage() + "\n[Result]\n " + responseItem.getStrResult());
                okInit.judgeResultParseResponseFailed(call, responseItem.getStrResult(), e);

                responseItem.setResultCode(RESULT_PARSE_FAILED);
//                responseItem.setStrResult(responseItem.getStrResult());
            }
        } else {
            // 返回网络错误代码
            L.e("[NetworkErrorCode: " + response.code() + "] " + responseItem.getUrl(), "");
            okInit.receivedNetworkErrorCode(call, response);
            responseItem.setResultCode(NETWORK_ERROR_CODE);
        }
        return responseItem;
    }


    /**
     * 处理responseResult成功的内容
     */
    private static void handleResultWithResultCode(ResponseItem responseItem) {
        if (responseItem == null) {
            return;
        }
        Call call = responseItem.getCall();
        String strResult = responseItem.getStrResult();
        Response response = responseItem.getResponse();
        OkResponseListener okResponseListener = responseItem.getOkResponseListener();
        JSONObject jsonObject = responseItem.getJsonObject();
        int resultCode = responseItem.getResultCode();

        try {
            switch (resultCode) {
                case RESULT_SUCCESS:
                    L.e("[Success] " + responseItem.getUrl(), JsonTool.stringToJSON(strResult));
                    okResponseListener.handleJsonSuccess(call, response, jsonObject);
                    break;
                case RESULT_ERROR:
                    L.e("[Error] " + responseItem.getUrl(), strResult);
                    okResponseListener.handleJsonError(call, response, jsonObject);
                    break;
                case RESULT_BLANK:
                    L.e("[Blank] " + responseItem.getUrl(), strResult);
                    okResponseListener.handleJsonError(call, response, jsonObject);
                    break;
                case RESULT_VERIFY_ERROR:
                    L.e("[VerifyError] " + responseItem.getUrl(), strResult);
                    okResponseListener.handleJsonVerifyError(call, response, jsonObject);
                    break;
                case RESULT_PARSE_FAILED:
                    // 已经上在步操作中Log了
                    okResponseListener.handleParseError(call, strResult);
                    break;
                case NETWORK_ERROR_CODE:
                    // 已经上在步操作中Log了
                    okResponseListener.handleResponseCodeError(call, response);
                    break;
                default:
                    L.e("[OtherResultCode: " + resultCode + "] " + responseItem.getUrl(), JsonTool.stringToJSON(strResult));
                    okResponseListener.handleJsonOther(call, response, jsonObject);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (resultCode != RESULT_SUCCESS) {
            try {
                okResponseListener.handleAllFailureSituation(call, resultCode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 网络错误，请求失败
     *
     * @param call
     * @param okResponseListener
     */
    private static void noResponse(Call call, OkResponseListener okResponseListener) {
//        okInit.networkError(call,  call.isCanceled());
        okInit.networkError(call, false);
        L.e("[networkError] " + (call != null ? call.request().url().url().toString() : ""), "");
        if (okResponseListener != null) {
            if (call != null && call.isCanceled()) {
                return;
            }
            try {
                okResponseListener.handleNoServerNetwork(call, false);
                okResponseListener.handleAllFailureSituation(call, NO_NETWORK);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * upload file，you can setMaxTransFileCount() to set max files upload thread pool size
     * uploadFiles暂时不提供Debug
     * @param activity
     * @param url
     * @param files
     * @param params
     * @param header
     * @param addDefaultHeader
     * @param addDefaultParams
     * @param okResponseListener
     * @param fileProgressListener
     * @return
     */
    static Call uploadFiles(final Activity activity, String url, Map<String, File> files, Param params, final Header header, boolean addDefaultHeader,
                            boolean addDefaultParams, final OkResponseListener okResponseListener, OkFileHelper.FileProgressListener fileProgressListener) {
        StringBuffer sb = new StringBuffer();
        String logTitle;
        logTitle = "[UPLOAD] " + url;
        if (files == null || files.size() == 0) {
            L.e(logTitle, "[Upload Canceled] fileSize: 0");
            return null;
        }
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder();
        bodyBuilder.setType(MultipartBody.FORM);
        if (params != null) {
            for (String key : params.keySet()) {
                if (sb.length() == 0) {
                    sb.append("[Params]");
                }
                sb.append("\n  ").append(key).append(": ").append(params.get(key));
                bodyBuilder.addFormDataPart(key, params.get(key));
            }
        }
        if (addDefaultParams) {
            Param defaultParams = okInit.setDefaultParams(new Param());
            for (String key : defaultParams.keySet()) {
                if (sb.length() == 0) {
                    sb.append("[Params]");
                }
                sb.append("\n  ").append(key).append(": ").append(defaultParams.get(key));
                if (params != null && params.containsKey(key)) {
                    sb.append("(ignored)");
                } else {
                    bodyBuilder.addFormDataPart(key, defaultParams.get(key));
                }
            }
        }

        if (sb.length() > 0) {
            sb.append("\n");
        }
        sb.append("[Files]");
        for (String key : files.keySet()) {
            sb.append("\n  ").append(key).append(": ").append(files.get(key).getName());
            bodyBuilder.addFormDataPart(key, files.get(key).getName(), RequestBody.create(MEDIA_TYPE_MULTI_DATA, files.get(key)));
        }
        RequestBody requestBody = bodyBuilder.build();

        OkFileHelper.ProgressRequestBody progressRequestBody = new OkFileHelper.ProgressRequestBody(requestBody, fileProgressListener);
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(progressRequestBody);
        // 添加Headers
        Header defaultHeader = okInit.setDefaultHeader(new Header());
        if (header != null && header.size() > 0 || (addDefaultHeader && defaultHeader != null && defaultHeader.size() > 0)) {
            sb.append("[Headers]");
            if (addDefaultHeader && defaultHeader != null) {
                for (String key : defaultHeader.keySet()) {
                    sb.append("\n  ").append(key).append(": ").append(defaultHeader.get(key));
                    if (header != null && header.containsKey(key)) {
                        sb.append("(ignored)");
                    } else {
                        requestBuilder.addHeader(key, defaultHeader.get(key));
                    }
                }
            }
            if (header != null) {
                for (String key : header.keySet()) {
                    sb.append("\n  ").append(key).append(": ").append(header.get(key));
                    requestBuilder.addHeader(key, header.get(key));
                }
            }
        }

        // DebugItem key
        final String debugKey;
        String keyForDebug = null;
        if (debugMode) {
            DebugItem debugItem = DebugActivity.addDebugItem(url);
            keyForDebug = debugItem.getKey();
        }
        debugKey = keyForDebug;

        Request request = requestBuilder.build();

        Call call = OkHttp.getClient().newCall(request);

        L.e(logTitle, sb.toString());

        // 使用RxJava2进行请求管理
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                Observable.create(
                        (ObservableOnSubscribe<ResponseItem>) observableEmitter -> {
                            final ResponseItem responseItem = new ResponseItem(response, call, url, okResponseListener);
                            responseResult(responseItem, debugKey);
                            response.close();
                            observableEmitter.onNext(responseItem);
                            observableEmitter.onComplete();
                        })
                        // 当传入Activity时，或者Activity没有被销毁则运行在主线程，否则运行在IO线程
                        .observeOn(activity == null ? Schedulers.io() : AndroidSchedulers.mainThread())
                        .subscribe(new Observer<ResponseItem>() {
                            private Disposable d;

                            @Override
                            public void onSubscribe(@io.reactivex.annotations.NonNull Disposable disposable) {
                                d = disposable;
                            }

                            @Override
                            public void onNext(@io.reactivex.annotations.NonNull ResponseItem responseItem) {
                                handleResultWithResultCode(responseItem);
                                XyBaseActivity.dismissLoadingDialogByManualState();
                            }

                            @Override
                            public void onError(@io.reactivex.annotations.NonNull Throwable throwable) {
                                throwable.printStackTrace();
                                noResponse(call, okResponseListener);
                                XyBaseActivity.dismissLoadingDialogByManualState();
                            }

                            @Override
                            public void onComplete() {
//                                XyBaseActivity.dismissLoadingDialogByManualState();
                            }
                        });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                noResponse(call, okResponseListener);
                XyBaseActivity.dismissLoadingDialogByManualState();
            }

        });
        return call;
    }

    /**
     * 在主线程中操作返回的JSON
     * 如果有需要在后台进行计算的操作，可以在 OkResponseListener.handleSuccessInBackground中操作
     * 如果有大量的计算复杂的操作，可在handleJsonSuccess后用RxJava的Computation线程中操作
     */
    interface IOkResponseListener {
        void handleJsonSuccess(Call call, Response response, JSONObject json) throws Exception;

        void handleJsonError(Call call, Response response, JSONObject json) throws Exception;
    }

    private abstract class XRequestBody extends RequestBody {
        @Override
        public MediaType contentType() {
            return OkOptions.mediaType;
        }
    }

    /**
     * okHttp属性设置
     */
    public static class OkOptions {

        public static long readTimeout = 60;
        public static long connectTimeout = 30;
        public static long writeTimeout = 120;
        public static MediaType mediaType = null;


        public OkOptions(long readTimeout, long connectTimeout, long writeTimeout) {
            OkOptions.readTimeout = readTimeout;
            OkOptions.connectTimeout = connectTimeout;
            OkOptions.writeTimeout = writeTimeout;
        }

        /**
         * 设置 Builder
         *
         * @param builder
         */
        public void setOkHttpBuilder(OkHttpClient.Builder builder) {

        }

        public static void setMediaType(MediaType mediaType) {
            OkOptions.mediaType = mediaType;
        }


    }

}
