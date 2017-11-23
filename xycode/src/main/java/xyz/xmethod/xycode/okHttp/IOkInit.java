package xyz.xmethod.xycode.okHttp;

import com.alibaba.fastjson.JSONObject;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by xiuye on 2017/8/17.
 * OkHttp初始化接口
 */
public interface IOkInit {
    /**
     * The first time the response got from internet
     * 0：RESULT_ERROR ;
     * 1：RESULT_SUCCESS ;
     * -1：RESULT_VERIFY_ERROR;
     * 2: RESULT_OTHER ;
     * at IOkResponse interface callback
     * 通过返回的的数据，返回结果代码
     * @param call
     * @param response
     * @param json  在JSON中提取ResultCode返回结果代码
     * @return
     */
    int judgeResultWhenFirstReceivedResponse(Call call, Response response, JSONObject json);

    /**
     * No network or  or call cancel
     * @param call
     * @param isCanceled
     */
    void networkError(Call call, boolean isCanceled);

    /**
     * after judgeResultWhenFirstReceivedResponse
     * result code not in  [200...300)
     * @param call
     * @param response
     */
    void receivedNetworkErrorCode(Call call, Response response);

    /**
     * After judgeResultWhenFirstReceivedResponse
     * result is SUCCESS
     * returns ---
     * false: go on callbacks
     * true：interrupt callbacks
     * 可在此方法保存资料到SQLite
     * @param call
     * @param response
     * @param json
     * @param resultCode 通过取得的ResultCode来判断是否继续流程，或中断流程执行其它操作
     * @return
     */
    boolean resultSuccessByJudge(Call call, Response response, JSONObject json, int resultCode);

    /**
     * After judgeResultWhenFirstReceivedResponse
     * when parse JSON failed
     * 解释JSON出错可以在这里对返回代码做处理
     * @param call
     * @param parseErrorResult
     */
    void judgeResultParseResponseFailed(Call call, String parseErrorResult, Exception e);

    /**
     * Reset all Params in param
     * 可在此方法进行最后参数验证加密等操作
     * @param allParams
     * @return null 不作任何处理
     */
    Param setParamsHeadersBeforeRequest(Param allParams, Header header);

    /**
     * Add defaultParams in param
     * @param defaultParams
     * @return
     */
    Param setDefaultParams(Param defaultParams);

    /**
     * Add defaultHeader in header
     * @param defaultHeader
     * @return
     */
    Header setDefaultHeader(Header defaultHeader);

}
