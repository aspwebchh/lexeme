package data;

import common.Common;
import common.Config;
import common.Text2Word;
import org.javatuples.KeyValue;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.InvalidObjectException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Cache {
    private static boolean isRedisEnable;
    private static String REDIS_KEY_SYS_ID = "lexeme_sys_id";
    private static String REDIS_KEY_WORD = "lexeme_word";
    private static String REDIS_KEY_INDEX_WORD = "lexeme:index_word";
    private static String REDIS_KEY_INDEX_TYPE = "lexeme:index_type";
    private static String REDIS_KEY_INDEX_SYS_ID = "lexeme:index_sys_id";
    private static JedisPool jedisPool;

    static {
        Config config = Config.fromFile();
        isRedisEnable = config.isRedisEnable();
        if (isRedisEnable) {
            try {
                String redisHost = config.getRedisHost();
                String redisPwd = config.getRedisPassword();
                int redisPort = config.getRedisPort();
                JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
                jedisPoolConfig.setMaxTotal(10);
                jedisPool = new JedisPool(jedisPoolConfig, redisHost, redisPort, 2000, redisPwd);
            } catch (InvalidObjectException e) {
                e.printStackTrace();
            }
        }
    }

    public static void index() {
        //indexByWord();
        //indexByType();
        //indexBySysId();
    }

    private static List<List<String>> group( String str ) {
        List<List<String>> result = new ArrayList<>();
        String[] dataIds = str.split(",");
        List<String> curr = null;
        for(int i = 0; i < dataIds.length; i++) {
            if( i % 100 == 0 ) {
                curr = new ArrayList<>();
                result.add(curr);
            }
            curr.add( dataIds[i] );
        }
        return result;
    }

    private static void removeNamespace(String namespace) {
        Jedis jedis = jedisPool.getResource();
        Set keySet = jedis.keys(namespace + "*");
        List<List<String>> group = group( String.join(",",keySet) );
        for(List<String> groupItem : group) {
            jedis.del(groupItem.toArray(new String[groupItem.size()]));
        }
        jedis.close();
    }

    private static String keyJoin( String ...objlist) {
        return String.join(":", objlist);
    }

    private static void indexByWord() {
        removeNamespace(REDIS_KEY_INDEX_WORD);

        Jedis jedis = jedisPool.getResource();
        List<Map> result = Data.getDataIdByWord();
        for (Map item : result) {
            String word = item.get("word").toString();
            String dataId = item.get("data_id").toString();
            List<List<String>> dataIdGroup = group(dataId);
            for (List<String> ids : dataIdGroup) {
                jedis.sadd( keyJoin( REDIS_KEY_INDEX_WORD  , word), ids.toArray(new String[ids.size()]) );
            }
        }
        jedis.close();
        System.out.println("word索引完成");
    }

    private static void indexByType(){
        removeNamespace(REDIS_KEY_INDEX_TYPE);

        Jedis jedis = jedisPool.getResource();
        List<Map> result = Data.getDataIdByType();
        for (Map item : result) {
            String type = item.get("type").toString();
            String dataId = item.get("data_id").toString();
            List<List<String>> dataIdGroup = group(dataId);
            for (List<String> ids : dataIdGroup) {
                jedis.sadd( keyJoin( REDIS_KEY_INDEX_TYPE  , type), ids.toArray(new String[ids.size()]) );
            }
        }
        jedis.close();
        System.out.println("type索引完成");
    }

    private static void indexBySysId(){
        removeNamespace(REDIS_KEY_INDEX_SYS_ID);

        Jedis jedis = jedisPool.getResource();
        List<Map> result = Data.getDataIdBySysId();
        for (Map item : result) {
            String sysId = item.get("sys_id").toString();
            String dataId = item.get("data_id").toString();
            List<List<String>> dataIdGroup = group(dataId);
            for (List<String> ids : dataIdGroup) {
                jedis.sadd( keyJoin( REDIS_KEY_INDEX_SYS_ID  , sysId), ids.toArray(new String[ids.size()]) );
            }
        }
        jedis.close();
        System.out.println("sys_id索引完成");
    }

    public static void init() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            loadWord();
        });
    }

    private static List<String> genLimits(int maxVal) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < maxVal; i++) {
            if (i % 100 == 0) {
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

    static void updateWord(List<KeyValue<String, Integer>> wordInfo) {
        Jedis jedis = jedisPool.getResource();
        wordInfo.forEach(item -> {
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
        for (String limit : limits) {
            Callable<Integer> task = () -> {
                List<Map> words = Data.getWordList(limit);
                Jedis jedis = jedisPool.getResource();
                words.stream().forEach(item -> {
                    int id = Integer.parseInt(item.get("id").toString());
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
        int[] list = Arrays.stream(words).map(item -> jedis.zscore(REDIS_KEY_WORD, item)).mapToInt(item -> (Integer) item.intValue()).toArray();
        jedis.close();
        return list;
    }

    private static int getSysIdFromRedis(String key) {
        Jedis jedis = jedisPool.getResource();
        String found = jedis.hget(REDIS_KEY_SYS_ID, key);
        if (Common.isNullOrEmpty(found)) {
            int sysId = Data.getSysId(key);
            jedis.hset(REDIS_KEY_SYS_ID, key, Integer.toString(sysId));
            jedis.close();
            return sysId;
        } else {
            jedis.close();
            return Integer.parseInt(found);
        }
    }

    public static int getSysId(String key) {
        if (!isRedisEnable) {
            return Data.getSysId(key);
        }
        return getSysIdFromRedis(key);
    }
}
