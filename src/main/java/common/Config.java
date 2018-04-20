package common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Config {
    private final String databaseUrl;
    private final String databaseUser;
    private final String databasePassword;
    private final String serverPort;
    private final String redisEnable;
    private final String redisHost;
    private final String redisPort;
    private final String redisPassword;

    public Config( String databaseUrl, String databaseUser, String databasePassword, String serverPort,String redisEnable,  String redisHost, String redisPort, String redisPassword) {
        this.databaseUrl = databaseUrl;
        this.databaseUser = databaseUser;
        this.databasePassword = databasePassword;
        this.serverPort = serverPort;
        this.redisEnable = redisEnable;
        this.redisHost = redisHost;
        this.redisPort = redisPort;
        this.redisPassword = redisPassword;
    }

    public int getServerPort() throws InvalidObjectException {
        if(!Common.isInteger(this.serverPort)) {
            throw new InvalidObjectException("端口号格式错误");
        }
        return Integer.parseInt( serverPort );
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public String getDatabaseUser() {
        return databaseUser;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public int getRedisPort() throws InvalidObjectException {
        if( this.isRedisEnable() && !Common.isInteger(this.redisPort)) {
            throw new InvalidObjectException("redis端口号格式错误");
        }
        return Integer.parseInt( redisPort );
    }

    public boolean isRedisEnable() {
        if( !Common.isInteger(redisEnable)) {
            return false;
        }
        int enable = Integer.parseInt(this.redisEnable);
        return enable == 1;
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

    public static Config fromFile()  {
        FileInputStream  fis;
        try{
            final String currDir = System.getProperty("user.dir");
            final String configFilePath = currDir + "/config.json";
            fis  = new FileInputStream(configFilePath);
            final String jsonContent = IOUtils.toString(fis);
            fis.close();
            JSONObject configData = JSON.parseObject(jsonContent);
            final String databaseUrl = configData.getString("database_url");
            final String databaseUser = configData.getString("database_user");
            final String databasePassword  = configData.getString("database_password");
            final String serverPort = configData.getString("server_port");
            final String redisEnable = configData.getString("redis_enable");
            final String redisHost = configData.getString("redis_host");
            final String redisPort = configData.getString("redis_port");
            final String redisPassword = configData.getString("redis_password");
            Config config = new Config(databaseUrl,databaseUser,databasePassword, serverPort, redisEnable,redisHost, redisPort, redisPassword);
            return config;
        } catch (Exception e) {
            return new Config("","","","","","","","");
        }
    }
}
