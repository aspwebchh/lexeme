package web;

import common.Common;
import common.ExecResult;
import data.Cache;
import data.Data;

import java.util.Map;

public class DelWordHandler  extends BaseHandler {
    @Override
    protected String handle(Map<String,String> postData) {
        final String dataId = postData.get("data_id");
        final String type = postData.get("type");
        final String sysKey = postData.get("sys_key");
        int code;
        String msg;
        if( Common.isNullOrEmpty( dataId ) || Common.isNullOrEmpty( type ) || Common.isNullOrEmpty( sysKey ) ) {
            code = ExecResult.CODE_ERROR;
            msg = "缺少参数";
        } else {
            int sysId = Cache.getSysId(sysKey);
            if( sysId <= 0) {
                code = ExecResult.CODE_ERROR;
                msg = "sys key 不存在";
            } else {
                if(!Common.isInteger(dataId) || !Common.isInteger(type)) {
                    code = ExecResult.CODE_ERROR;
                    msg = "参数格式错误";
                } else {
                    final int iDataId = Integer.parseInt(dataId);
                    final int iType = Integer.parseInt(type);
                    Data.delete(sysId,iType,iDataId);
                    code = ExecResult.CODE_SUCCESS;
                    msg = "删除成功";
                }
            }
        }
        return ExecResult.with(code,msg).toJSON();
    }
}
