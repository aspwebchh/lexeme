

import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import common.Config;
import web.*;


public class Main {
    public static void main(String[] args) throws IOException {
        Config config = Config.fromFile();
        HttpServer server = HttpServer.create(new InetSocketAddress(config.getServerPort()), 0);
        server.createContext("/create", new CreateWordHandler());
        server.createContext("/find", new FindWordHandler());
        server.createContext("/delete", new DelWordHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }
}
