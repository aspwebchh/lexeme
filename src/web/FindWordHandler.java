package web;


import common.Common;
import common.ExecResult;
import data.Data;
import data.FoundResult;
import org.javatuples.KeyValue;

import java.util.Map;

public class FindWordHandler extends BaseHandler{
    @Override
    protected String handle( Map<String,String> postData ) {
        String text = postData.get("text");
        String sysKey = postData.get("sys_key");
        String type = postData.get("type");
        String pageIndex = postData.get("page_index");
        String pageSize = postData.get("page_size");
        if(Common.isNullOrEmpty(text) || Common.isNullOrEmpty(sysKey) || Common.isNullOrEmpty(type) || Common.isNullOrEmpty(pageIndex)|| Common.isNullOrEmpty(pageSize)) {
            return ExecResult.with(ExecResult.CODE_ERROR,"缺少参数").toJSON();
        }
        int sysId = Data.getSysId(sysKey);
        if( sysId <= 0) {
            return ExecResult.with(ExecResult.CODE_ERROR,"sys key 不存在").toJSON();
        }
        if(!Common.isInteger(type) || !Common.isInteger(pageSize)|| !Common.isInteger(pageIndex)) {
            return ExecResult.with(ExecResult.CODE_ERROR,"参数格式错误").toJSON();
        }
        FoundResult result = Data.find(text,sysId,Integer.parseInt(type),Integer.parseInt(pageIndex),Integer.parseInt(pageSize));
        ExecResult er = new ExecResult();
        er.setCode(ExecResult.CODE_SUCCESS);
        er.setMessage("");
        er.setData(result.toData());
        return er.toJSON();
    }
}