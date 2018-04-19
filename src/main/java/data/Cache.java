package data;

import common.Common;
import common.Config;
import org.javatuples.KeyValue;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class Cache {
    private static boolean isRedisEnable;
    private static String REDIS_KEY_SYS_ID = "lexeme_sys_id";
    private static String REDIS_KEY_WORD = "lexeme_word";
    private static JedisPool jedisPool;

    static {
        Config config = Config.fromFile();
        isRedisEnable = config.isRedisEnable();
        if(isRedisEnable) {
            try {
                String redisHost = config.getRedisHost();
                int redisPort = config.getRedisPort();
                JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
                jedisPoolConfig.setMaxTotal(10);
                jedisPool = new JedisPool(jedisPoolConfig, redisHost , redisPort);
            } catch (InvalidObjectException e) {
                e.printStackTrace();
            }
        }
    }

    public static void init() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(()->{
            loadWord();
        });
    }

    private static List<String> genLimits(int maxVal) {
        List<String> result = new ArrayList<>();
        for(int i = 0; i < maxVal; i++) {
            if( i  % 100 == 0 ) {
                result.add(i + ",500");
            }
        }
        return result;
    }

    private static void removeWordFromRedis() {
        Jedis jedis = jedisPool.getResource();
        jedis.del(REDIS_KEY_WORD);
        jedis.close();
    }

    static void updateWord(List<KeyValue<String,Integer>> wordInfo) {
        Jedis jedis = jedisPool.getResource();
        wordInfo.forEach( item ->{
            jedis.zadd(REDIS_KEY_WORD, item.getValue(), item.getKey());
        });
        jedis.close();
    }

    private static void loadWord() {
        removeWordFromRedis();

        int maxVal = Data.getWordCount();
        List<String> limits = genLimits(maxVal);
        List<Callable<Integer>> tasks = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (String limit : limits ) {
            Callable<Integer> task = ()->{
                List<Map> words = Data.getWordList(limit);
                Jedis jedis = jedisPool.getResource();
                words.stream().forEach( item ->{
                    int id = Integer.parseInt( item.get("id").toString() );
                    String word = item.get("word").toString();
                    jedis.zadd(REDIS_KEY_WORD, id, word);
                });
                jedis.close();
                return words.size();
            };
            tasks.add(task);
        }
        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            executor.shutdown();
            e.printStackTrace();
        }
        System.out.println("加载词库到redis完成");
    }

    static int[] getWordId(String[] words) {
        Jedis jedis = jedisPool.getResource();
        int[] list = Arrays.stream(words).map( item -> jedis.zscore(REDIS_KEY_WORD, item)).mapToInt( item -> (Integer)item.intValue()).toArray();
        jedis.close();
        return list;
    }

    private static int getSysIdFromRedis( String key ) {
        Jedis jedis = jedisPool.getResource();
        String found = jedis.hget(REDIS_KEY_SYS_ID, key);
        if(Common.isNullOrEmpty(found)) {
            int sysId = Data.getSysId(key);
            jedis.hset(REDIS_KEY_SYS_ID, key, Integer.toString(sysId));
            jedis.close();
            return sysId;
        } else {
            jedis.close();
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
