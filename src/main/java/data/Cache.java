package data;

import common.Common;
import common.Config;
import redis.clients.jedis.Jedis;

import java.io.InvalidObjectException;

public class Cache {
    private static boolean isRedisEnable;
    private static Jedis jedis;
    private static String REDIS_KEY_SYS_ID = "lexeme_sys_id";


    static {
        Config config = Config.fromFile();
        isRedisEnable = config.isRedisEnable();
        if(isRedisEnable) {
            try {
                String redisHost = config.getRedisHost();
                int redisPort = config.getRedisPort();
                jedis = new Jedis(redisHost, redisPort);
            } catch (InvalidObjectException e) {
                e.printStackTrace();
            }
        }
    }

    private static int getSysIdFromRedis( String key ) {
        String found = jedis.hget(REDIS_KEY_SYS_ID, key);
        if(Common.isNullOrEmpty(found)) {
            int sysId = Data.getSysId(key);
            jedis.hset(REDIS_KEY_SYS_ID, key, Integer.toString(sysId));
            return sysId;
        } else {
            return Integer.parseInt(found);
        }
    }

    public static int getSysId( String key ) {
        if(!isRedisEnable) {
            return Data.getSysId(key);
        }
        return getSysIdFromRedis(key);
    }
}
