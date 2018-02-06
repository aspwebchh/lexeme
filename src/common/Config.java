package common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;

import java.io.*;

public class Config {
    private final String databaseUrl;
    private final String databaseUser;
    private final String databasePassword;
    private final String serverPort;

    public Config( String databaseUrl, String databaseUser, String databasePassword, String serverPort) {
        this.databaseUrl = databaseUrl;
        this.databaseUser = databaseUser;
        this.databasePassword = databasePassword;
        this.serverPort = serverPort;
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

    public boolean isEnable() {
        return !Common.isNullOrEmpty(databaseUrl);
    }

    @Override
    public String toString() {
        return databaseUrl + "|" + databaseUser + "|" + databasePassword;
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
            Config config = new Config(databaseUrl,databaseUser,databasePassword, serverPort);
            return config;
        } catch (Exception e) {
            return new Config("","","","");
        }
    }
}
