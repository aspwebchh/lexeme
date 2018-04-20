package data;

import common.DbHelper;
import common.Text2Word;
import org.apache.commons.lang3.ArrayUtils;
import org.javatuples.KeyValue;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Data {
    static int getSysId(String key) {
        List<Map> result = DbHelper.executeQuery("select sys_id from system where sys_key = ?", key);
        if(result != null && result.size() > 0) {
            return Integer.parseInt( result.get(0).get("sys_id").toString() );
        } else {
            return 0;
        }
    }

    static List<Map> getDataIdByWord() {
        String sql = " select t.data_id,word.word from (\n" +
                "  SELECT\n" +
                "    word_id,\n" +
                "    group_concat(DISTINCT(data_id)) as data_id\n" +
                "  FROM word_dic\n" +
                "  GROUP BY word_id\n" +
                ") as t LEFT JOIN word on id = t.word_id";
        List<Map> result = DbHelper.executeQuery(sql);
        return result;
    }

    static List<Map> getDataIdByType(){
        String sql = "SELECT\n" +
                    "    type,\n" +
                    "    group_concat( DISTINCT( data_id )) as data_id\n" +
                    "  FROM word_dic\n" +
                    "  GROUP BY type";
        List<Map> result = DbHelper.executeQuery(sql);
        return result;
    }

    static List<Map> getDataIdBySysId(){
        String sql = "SELECT\n" +
                "    sys_id,\n" +
                "    group_concat( DISTINCT( data_id )) as data_id\n" +
                "  FROM word_dic\n" +
                "  GROUP BY sys_id";
        List<Map> result = DbHelper.executeQuery(sql);
        return result;
    }

    private static KeyValue<String,Integer> Save( String word, int count,int type, int dataId, int sysId) {
        final String sql = "insert into word(word) values (?) ON DUPLICATE KEY UPDATE word= ?";
        DbHelper.executeNonQuery(sql,word,word);

        final String sql1 = "select id from word where word = ?";
        final Object wordId = DbHelper.getSingle(sql1, word);
        final int iWordId = Integer.parseInt(wordId.toString());

        final String sql2 = "insert into word_dic( word_id, count, data_id, type, sys_id) value ( ?, ?, ?, ?, ?)";
        DbHelper.executeNonQuery(sql2,iWordId,count,dataId,type,sysId);

        return KeyValue.with(word, iWordId);
    }

    public static void create(String text, int type, int dataId, int sysId) {
        Text2Word text2Word = new Text2Word();
        List<KeyValue<String,Integer>> wordInfo =  text2Word.convert(text);
        List<Callable<KeyValue<String,Integer>>> tasks = new ArrayList<>();
        for (KeyValue<String,Integer> wordInfoItem : wordInfo) {
            String word = wordInfoItem.getKey();
            int count = wordInfoItem.getValue();
            Callable<KeyValue<String,Integer>>  task = () -> Save(word,count,type,dataId,sysId);
            tasks.add(task);
        }
        ExecutorService executor = Executors.newCachedThreadPool();
        try {
            List<Future<KeyValue<String,Integer>>> futures = executor.invokeAll(tasks);
            List<KeyValue<String,Integer>> result = new ArrayList<>();
            for(Future<KeyValue<String,Integer>> future : futures) {
                try {
                    KeyValue<String,Integer> value = future.get();
                    result.add(value);
                } catch (ExecutionException e) {
                    future.cancel(true);
                    e.printStackTrace();
                }
            }
            Cache.updateWord(result);
        } catch (InterruptedException e) {
            executor.shutdown();
            e.printStackTrace();
        }
    }

    static int getWordCount() {
        String sql = "select count(1) from word";
        Object result = DbHelper.getSingle(sql);
        return Integer.parseInt(result.toString());
    }

    static List<Map> getWordList( String limit) {
        String sql = "select id, word from word limit " + limit;
        return DbHelper.executeQuery(sql);
    }

    private static FoundResult getDataId(int[] wordId, int sysId, int type, int pageIndex, int pageSize ) {
        if( wordId.length == 0 ) {
            return FoundResult.empty();
        }
        String limit = ((pageIndex - 1) * pageSize ) + "," + pageSize;
        String wordIdString = String.join(",", Arrays.asList(ArrayUtils.toObject(wordId)).stream().map(n->n.toString()).toArray(String[]::new));

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Integer> threadResult = executor.submit(() -> {
            String sqlForCount = "select count(distinct( data_id )) from word_dic where word_id in ("+ wordIdString +") and type =" + type + " and sys_id =" + sysId;
            Integer count = Integer.parseInt( DbHelper.getSingle(sqlForCount).toString());
            return count;
        });
        String sql = "select  data_id , count(*) as count from word_dic where word_id in ("+ wordIdString +") and type =" + type + " and sys_id = "+ sysId +" group by data_id order by count desc limit " + limit;
        List<Map> result = DbHelper.executeQuery(sql);
        List<Map<String,Integer>> dataList = result.stream().map(item ->{
            Integer dataId = Integer.parseInt( item.get("data_id").toString() );
            Integer count = Integer.parseInt( item.get("count").toString() );
            Map<String, Integer> rt = new HashMap<>();
            rt.put("ID",dataId);
            rt.put("Count", count);
            return rt;
        }).collect(Collectors.toList());

        int count = 0;
        try {
            count = threadResult.get();
        } catch (InterruptedException e) {
            threadResult.cancel(true);
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        FoundResult foundResult = new FoundResult(dataList,count);
        return foundResult;
    }

    public static FoundResult find( String text, int sysId, int type, int pageIndex, int pageSize ) {
        Text2Word text2Word = new Text2Word();
        List<KeyValue<String,Integer>> words =  text2Word.convert(text);
        if( words.size() == 0 ) {
            return FoundResult.empty();
        }
        String[] wordItems = words.stream().map(n->n.getKey()).collect(Collectors.toList()).toArray(new String[words.size()]);
        int[] wordIDs = Cache.getWordId(wordItems);
        return getDataId(wordIDs, sysId, type, pageIndex, pageSize);
    }

    public static void  delete(int sysId, int type, int dataId ) {
        String sql = "delete from word_dic where data_id = ? and type = ? and sys_id = ?";
        DbHelper.executeNonQuery(sql,dataId,type,sysId);
    }
}
