package common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Config {
    private final String databaseUrl;
    private final String databaseUser;
    private final String databasePassword;

    public Config( String databaseUrl, String databaseUser, String databasePassword) {
        this.databaseUrl = databaseUrl;
        this.databaseUser = databaseUser;
        this.databasePassword = databasePassword;
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
            Config config = new Config(databaseUrl,databaseUser,databasePassword);
            return config;
        } catch (Exception e) {
            return new Config("","","");
        }
    }
}
