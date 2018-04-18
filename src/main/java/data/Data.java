package data;

import common.DbHelper;
import common.Text2Word;
import org.apache.commons.lang3.ArrayUtils;
import org.javatuples.KeyValue;

import java.sql.CallableStatement;
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

    public static void create(String text, int type, int dataId, int sysId) {
        Text2Word text2Word = new Text2Word();
        List<KeyValue<String,Integer>> wordInfo =  text2Word.convert(text);
        String[] wordTextItems = wordInfo.stream().map( n->n.getKey() + "," + n.getValue() ).toArray(String[]::new);
        String wordText = String.join("|",wordTextItems);
        DbHelper.procCall(conn -> {
            CallableStatement callStmt =  conn.prepareCall("{call p_index_word(?,?,?,?)}");
            callStmt.setString(1,wordText);
            callStmt.setInt(2,dataId);
            callStmt.setInt(3,type);
            callStmt.setInt(4,sysId);
            return callStmt;
        });
    }

    private static int[] getWordId(String[] words) {
        if( words == null || words.length == 0 ) {
            return new int[0];
        }
        String whereString = "'" + String.join("','",words) + "'";
        String sql = "select id from word where word in ("+ whereString +")";
        List<Map> mapForWordId =  DbHelper.executeQuery(sql);
        if(mapForWordId == null) {
            return new int[0];
        }
        int[] wordIDs = mapForWordId.stream().mapToInt(n->Integer.parseInt(n.get("id").toString())).toArray();
        return wordIDs;
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
        int[] wordIDs = getWordId(wordItems);
        return getDataId(wordIDs, sysId, type, pageIndex, pageSize);
    }

    public static void  delete(int sysId, int type, int dataId ) {
        String sql = "delete from word_dic where data_id = ? and type = ? and sys_id = ?";
        DbHelper.executeNonQuery(sql,dataId,type,sysId);
    }
}
