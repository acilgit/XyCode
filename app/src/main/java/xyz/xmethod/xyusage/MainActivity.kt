package xyz.xmethod.xyusage

import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.alibaba.fastjson.JSONObject
import kotlinx.android.synthetic.main.activity_main.*
import xyz.xmethod.xycode.adapter.CustomHolder
import xyz.xmethod.xycode.adapter.XAdapter
import xyz.xmethod.xycode.base.BaseActivity
import xyz.xmethod.xycode.okHttp.Param
import xyz.xmethod.xycode.unit.StringData
import xyz.xmethod.xycode.unit.ViewTypeUnit
import xyz.xmethod.xycode.xRefresher.RefreshRequest
import java.util.*

class MainActivity : BaseActivity() {
    override fun setActivityLayout(): Int = R.layout.activity_main

    override fun setLoadingDialog(): AlertDialog? = null

    override fun initOnCreate(savedInstanceState: Bundle?) {
        rf.setup(this, object : XAdapter<String>(this){

            override fun getViewTypeUnitForLayout(item: String?): ViewTypeUnit = ViewTypeUnit(R.layout.activity_main)

            override fun bindingHolder(holder: CustomHolder?, dataList: MutableList<String>?, pos: Int) {

            }
        }).setRefreshRequest(object : RefreshRequest<String>() {
            override fun setRequestParamsReturnUrl(params: Param?): String = ""

            override fun setListData(json: JSONObject?): MutableList<String> = ArrayList()

        }).setOnSwipeListener {  }

        var obj : Any
        obj = StringData<String>("a", "b")

    }



}
