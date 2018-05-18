

import com.sun.net.httpserver.HttpServer;
import common.Config;
import data.Cache;
import web.CreateWordHandler;
import web.DelWordHandler;
import web.FindWordHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;


public class Main {
    public static void main(String[] args) throws IOException {
        Cache.init();
        Config config = Config.get();
        HttpServer server = HttpServer.create(new InetSocketAddress(config.getServerPortAsInt()), 0);
        server.createContext("/create", new CreateWordHandler());
        server.createContext("/find", new FindWordHandler());
        server.createContext("/delete", new DelWordHandler());
        server.setExecutor(Executors.newFixedThreadPool(5));
        server.start();
    }
}
