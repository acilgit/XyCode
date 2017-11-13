package xyz.xmethod.xyusage

import android.app.Application
import com.alibaba.fastjson.JSONObject
import xyz.xmethod.xycode.Xy
import xyz.xmethod.xycode.okHttp.Header
import xyz.xmethod.xycode.okHttp.IOkInit
import xyz.xmethod.xycode.okHttp.OkHttp
import xyz.xmethod.xycode.okHttp.Param
import java.lang.Exception

/**
 * Created by xiuye on 2017/11/11.
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        init()
    }

    private fun init() {
        Xy.init(this, false)

        OkHttp.init(object : IOkInit{
            /**
             * the first time the response got from internet
             * 0：RESULT_ERROR ;
             * 1：RESULT_SUCCESS ;
             * -1：RESULT_VERIFY_ERROR;
             * 2: RESULT_OTHER ;
             * at IOkResponse interface callback
             *
             * @param call
             * @param response
             * @param json
             * @return
             */
            override fun judgeResultWhenFirstReceivedResponse(call: okhttp3.Call?, response: okhttp3.Response?, json: JSONObject?): Int {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            /**
             * no network or  or call back cancel
             *
             * @param call
             * @param isCanceled
             */
            override fun networkError(call: okhttp3.Call?, isCanceled: Boolean) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            /**
             * after judgeResultWhenFirstReceivedResponse
             * result code not in  [200...300)
             *
             * @param call
             * @param response
             */
            override fun receivedNetworkErrorCode(call: okhttp3.Call?, response: okhttp3.Response?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            /**
             * after judgeResultWhenFirstReceivedResponse
             * result is SUCCESS
             * returns ---
             * false: go on callbacks
             * true：interrupt callbacks
             * 可在此方法保存资料到SQLite
             *
             * @param call
             * @param response
             * @param json
             * @param resultCode
             * @return
             */
            override fun resultSuccessByJudge(call: okhttp3.Call?, response: okhttp3.Response?, json: JSONObject?, resultCode: Int): Boolean {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            /**
             * after judgeResultWhenFirstReceivedResponse
             * when parse JSON failed
             *
             * @param call
             * @param parseErrorResult
             */
            override fun judgeResultParseResponseFailed(call: okhttp3.Call?, parseErrorResult: String?, e: Exception?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            /**
             * reset all Params in param
             * when setFormBody requestBody
             *
             * @param allParams
             * @return null 不作任何处理
             */
            override fun setParamsHeadersBeforeRequest(allParams: Param?, header: Header?): Param {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            /**
             * add defaultParams in param
             * when setFormBody requestBody
             *
             * @param defaultParams
             * @return
             */
            override fun setDefaultParams(defaultParams: Param?): Param {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            /**
             * add defaultHeader in header
             * when new a request
             *
             * @param defaultHeader
             * @return
             */
            override fun setDefaultHeader(defaultHeader: Header?): Header {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })
    }
}