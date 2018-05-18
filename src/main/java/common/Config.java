package common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 宏鸿 on 2018/4/25.
 */
public class Config {
    private String databaseUrl;
    private String databaseUser;
    private String databasePassword;
    private String serverPort;
    private String redisEnable;
    private String redisHost;
    private String redisPort;
    private String redisPassword;

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public String getDatabaseUser() {
        return databaseUser;
    }

    public void setDatabaseUser(String databaseUser) {
        this.databaseUser = databaseUser;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public void setDatabasePassword(String databasePassword) {
        this.databasePassword = databasePassword;
    }

    public String getServerPort() {
        return serverPort;
    }

    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public void setRedisPassword(String redisPassword) {
        this.redisPassword = redisPassword;
    }

    public String getRedisPort() {
        return redisPort;
    }

    public void setRedisPort(String redisPort) {
        this.redisPort = redisPort;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public void setRedisHost(String redisHost) {
        this.redisHost = redisHost;
    }

    public String getRedisEnable() {
        return redisEnable;
    }

    public void setRedisEnable(String redisEnable) {
        this.redisEnable = redisEnable;
    }

    public int getServerPortAsInt() {
        return Integer.parseInt(serverPort);
    }

    public int getRedisPortAsInt() {
        return Integer.parseInt(redisPort);
    }

    public boolean isRedisEnable() {
        return redisEnable.equals( "1" );
    }

    @Override
    public String toString() {
        List<String> items = new ArrayList<>();
        items.add("databaseUrl:" + this.databaseUrl);
        items.add("databaseUser:" + this.databaseUser);
        items.add("databasePassword:" + this.databasePassword);
        items.add("serverPort:" + this.serverPort);
        items.add("redisEnable:" + this.redisEnable);
        items.add("redisHost:" + this.redisHost);
        items.add("redisPort:" + this.redisPort);
        items.add("redisPassword:" + this.redisPassword);
        return String.join("\n", items.toArray(new String[items.size()]));
    }

    private static Config instance;

    public static synchronized Config get() {
        if( instance != null) {
            return instance;
        }
        ApplicationContext context = new FileSystemXmlApplicationContext("applicationContext.xml");
        Config config = (Config) context.getBean("config");
        return config;
    }
}
