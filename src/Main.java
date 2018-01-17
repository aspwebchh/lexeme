

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import common.Common;
import common.ExecResult;
import data.Data;
import org.apache.commons.io.IOUtils;
import org.javatuples.KeyValue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    private static final String HTTP_METHOD_GET = "GET";
    private static final String HTTP_METHOD_POST = "POST";

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/create", new CreateWordHandler());
        server.createContext("/find", new FindWordHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

    static abstract class BaseHandler implements HttpHandler{
        protected abstract String handle(Map<String,String> postData);

        private static final ExecutorService es = Executors.newCachedThreadPool();

        @Override
        public void handle(HttpExchange exchange) {
            try{
                String response = "";
                String method = exchange.getRequestMethod();
                if(method.toLowerCase().equals(HTTP_METHOD_POST.toLowerCase())) {
                    String postDataString = IOUtils.toString(exchange.getRequestBody());
                    Map<String,String> postData = Common.formData2Dic(postDataString);
                    response = handle(postData);
                }
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } catch (IOException e) {}
        }
    }

    static class FindWordHandler extends BaseHandler{
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
            KeyValue<Integer[], Integer> result = Data.find(text,sysId,Integer.parseInt(type),Integer.parseInt(pageIndex),Integer.parseInt(pageSize));
            ExecResult er = new ExecResult();
            er.setCode(ExecResult.CODE_SUCCESS);
            er.setMessage("");
            er.setData(result);
            return er.toJSON();
        }
    }

    static class CreateWordHandler extends BaseHandler {
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
}
