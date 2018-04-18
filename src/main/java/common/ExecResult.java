package common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import java.util.HashMap;
import java.util.Map;

public class ExecResult<T> {
    public static int CODE_SUCCESS = 0;
    public static int CODE_ERROR = 1;
    public static int CODE_CAPTCHA_ERROR = 2;


    private int code;
    private String message;
    private T data;

    public static ExecResult with(int code, String message ) {
        return new ExecResult(code, message);
    }

    public ExecResult() {

    }

    public ExecResult(int code, String message ) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code ) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message ) {
        this.message = message;
    }

    public void setData( T data ) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    private String convert2JSON( String codeFieldName, String messageFieldName, String dataFieldName ) {
        Map<String, Object> result = new HashMap<>();
        result.put(codeFieldName, code);
        result.put(messageFieldName, message);
        if( this.data != null ) {
            result.put(dataFieldName, this.data);
        }
        return JSON.toJSONString(result, SerializerFeature.BrowserCompatible );
    }

    public String toJSON() {
        return convert2JSON("Code","Message","Data");
    }

    public String toJSON( String codeFieldName, String messageFieldName, String dataFieldName ) {
        return convert2JSON( codeFieldName, messageFieldName, dataFieldName );
    }
}
