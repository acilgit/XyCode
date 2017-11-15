package xyz.xmethod.xycode.okHttp;

import com.alibaba.fastjson.JSONObject;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by xiuye on 2017/4/17.
 * 类里的方法都是空实现，可以重写来实现对在相应的操作中进行处理
 * 默认实现 成功 和 失败
 */
public abstract class OkResponseListener implements OkHttp.IOkResponseListener {

    /**
     * 验证错误
     */
    protected void handleJsonVerifyError(Call call, Response response, JSONObject json) throws Exception {

    }

    /**
     * 其它不在返回结果列表中的代码
     */
    protected void handleJsonOther(Call call, Response response, JSONObject json) throws Exception {

    }

    /**解析Json出错
     *
     * @param call
     * @param responseResult
     * @throws Exception
     */
    protected void handleParseError(Call call, String responseResult) throws Exception {

    }

    /**
     * 网络错误
     *
     * @param call
     * @param isCanceled
     * @throws Exception
     */
    protected void handleNoServerNetwork(Call call, boolean isCanceled) throws Exception {

    }

    /**
     * 网络返回代码Code是错误的代码
     *
     * @param call
     * @param response
     * @throws Exception
     */
    protected void handleResponseCodeError(Call call, Response response) throws Exception {

    }

    /**
     * 处理返回结果时，出现异常
     *
     * @param call
     * @param response
     * @param e
     * @throws Exception
     */
    protected void handleResponseFailure(Call call, Response response, Exception e) throws Exception {

    }

    /**
     * 以上所有的出错时，统一再一次会执行此方法
     *
     * @param call
     * @param resultCode
     * @throws Exception
     */
    protected void handleAllFailureSituation(Call call, int resultCode) throws Exception {

    }

    /**
     * 成功时，进行线程中处理操作
     *
     * @param call
     * @param json
     * @throws Exception
     */
    protected void handleSuccessInBackground(Call call, JSONObject json) throws Exception {

    }


}
