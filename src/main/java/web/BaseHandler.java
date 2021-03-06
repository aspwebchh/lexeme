package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import common.Common;
import common.ExecResult;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public abstract class BaseHandler implements HttpHandler {
    private static final String HTTP_METHOD_GET = "GET";
    private static final String HTTP_METHOD_POST = "POST";

    protected abstract String handle(Map<String,String> postData);

    @Override
    public void handle(HttpExchange exchange) {
        try{
            String response = "";
            String method = exchange.getRequestMethod();
            if(method.toLowerCase().equals(HTTP_METHOD_POST.toLowerCase())) {
                String postDataString = IOUtils.toString(exchange.getRequestBody());
                Map<String,String> postData = Common.formData2Dic(postDataString);
                response = handle(postData);
            } else {
                ExecResult result = ExecResult.with(ExecResult.CODE_ERROR,"不支持非POST请求");
                response = result.toJSON();
            }
            exchange.sendResponseHeaders(200,0);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}