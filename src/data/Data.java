package data;

import common.DbHelper;
import common.Text2Word;
import org.apache.commons.lang3.ArrayUtils;
import org.javatuples.KeyValue;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Data {
    public static int getSysId(String key) {
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
        DbHelper.procCall(new ProcSmtBuilder() {
            @Override
            public CallableStatement call(Connection conn) throws SQLException {
                CallableStatement callStmt =  conn.prepareCall("{call p_index_word(?,?,?,?)}");
                callStmt.setString(1,wordText);
                callStmt.setInt(2,dataId);
                callStmt.setInt(3,type);
                callStmt.setInt(4,sysId);
                return callStmt;
            }
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

    private static KeyValue<Integer[], Integer> getDataId(int[] wordId, int sysId, int type, int pageIndex, int pageSize ) {
        if( wordId.length == 0 ) {
            return KeyValue.with( new Integer[0], 0);
        }
        String limit = ((pageIndex - 1) * pageSize ) + "," + pageSize;
        String wordIdString = String.join(",", Arrays.asList(ArrayUtils.toObject(wordId)).stream().map(n->n.toString()).toArray(String[]::new));
        String sql = "select distinct( data_id ) from word_dic where word_id in ("+ wordIdString +") and type =" + type + " and sys_id = "+ sysId +" order by count desc limit " + limit;
        List<Map> result = DbHelper.executeQuery(sql);
        Integer[] ids = result.stream().map(n-> Integer.parseInt( n.get("data_id").toString() ) ).toArray(Integer[]::new);
        String sqlForCount = "select count(distinct( data_id )) from word_dic where word_id in ("+ wordIdString +") and type =" + type + " and sys_id =" + sysId;
        int count = Integer.parseInt( DbHelper.getSingle(sqlForCount).toString());
        return KeyValue.with(ids,count);
    }

    public static KeyValue<Integer[], Integer> find( String text, int sysId, int type, int pageIndex, int pageSize ) {
        Text2Word text2Word = new Text2Word();
        List<KeyValue<String,Integer>> words =  text2Word.convert(text);
        if( words.size() == 0 ) {
            return KeyValue.with( new Integer[0], 0);
        }
        String[] wordItems = words.stream().map(n->n.getKey()).collect(Collectors.toList()).toArray(new String[words.size()]);
        int[] wordIDs = getWordId(wordItems);
        return getDataId(wordIDs, sysId, type, pageIndex, pageSize);
    }
}
