package web;


import common.Common;
import common.ExecResult;
import data.Data;

import java.util.Map;

public class CreateWordHandler extends BaseHandler {
    @Override
    protected String handle(Map<String,String> postData) {
        String text = postData.get("text");
        String dataId = postData.get("data_id");
        String type = postData.get("type");
        String sysKey = postData.get("sys_key");
        int code;
        String msg;
        if(Common.isNullOrEmpty(text) || Common.isNullOrEmpty(dataId) || Common.isNullOrEmpty(type) || Common.isNullOrEmpty(sysKey)) {
            code = ExecResult.CODE_ERROR;
            msg = "缺少参数";
        } else {
            int sysId = Data.getSysId(sysKey);
            if( sysId <= 0) {
                code = ExecResult.CODE_ERROR;
                msg = "sys key 不存在";
            } else {
                if(!Common.isInteger(dataId) || !Common.isInteger(type)) {
                    code = ExecResult.CODE_ERROR;
                    msg = "参数格式错误";
                } else {
                    Data.create(text,Integer.parseInt(type),Integer.parseInt(dataId),sysId);
                    code = ExecResult.CODE_SUCCESS;
                    msg = "索引完成";
                }
            }
        }
        return ExecResult.with(code,msg).toJSON();
    }
}