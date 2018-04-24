package common;

import data.ProcSmtBuilder;

import javax.naming.ConfigurationException;
import java.sql.*;
import java.util.*;


public class DbHelper {
    public static Config config ;

    static {
        config   = Config.fromFile();
    }

    private static List<Map> resultSet2List(ResultSet rs) {
        try{
            List<Map> list = new ArrayList<Map>();
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            while (rs.next()) {
                Map rowData = new HashMap();
                for (int i = 1; i <= columnCount; i++) {
                    rowData.put(md.getColumnName(i), rs.getObject(i));
                }
                list.add(rowData);
            }
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<Map>();
        }
    }

    public static Connection getConnection() {
        Connection conn = null;
        try {
            String url = config.getDatabaseUrl();
            String user = config.getDatabaseUser();
            String password = config.getDatabasePassword();
            if (null == conn) {
                conn = DriverManager.getConnection(url, user, password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

    public static int executeNonQuery(String sql) {
        int result = 0;
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            result = stmt.executeUpdate(sql);
        } catch (SQLException err) {
            err.printStackTrace();
        } finally {
            free(null, stmt, conn);
        }
        return result;
    }

    public static int executeNonQuery(String sql, Object... obj) {
        int result = 0;
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            for (int i = 0; i < obj.length; i++) {
                pstmt.setObject(i + 1, obj[i]);
            }
            result = pstmt.executeUpdate();
        } catch (SQLException err) {
            err.printStackTrace();
        } finally {
            free(null, pstmt, conn);
        }
        return result;
    }

    public static int executeNonQueryWithIdentity(String sql, Object... obj) {
        int result = 0;
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            for (int i = 0; i < obj.length; i++) {
                pstmt.setObject(i + 1, obj[i]);
            }
            pstmt.executeUpdate();
            ResultSet rs = pstmt.executeQuery("select LAST_INSERT_ID();");
            List<Map> list = resultSet2List(rs);
            result = Integer.parseInt(getSingle(list).toString());
        } catch (SQLException err) {
            err.printStackTrace();
        } finally {
            free(null, pstmt, conn);
        }
        return result;
    }

    public static Object getSingle( String sql ) {
        List<Map> result = executeQuery( sql );
        return getSingle(result);
    }

    public static Object getSingle( String sql ,Object... obj) {
        List<Map> result = executeQuery( sql, obj );
        return getSingle(result);
    }

    private static Object getSingle( List<Map> result ) {
        if( result != null &&  result.size() > 0 ) {
            Map map = result.get(0);
            for (Object key:
                    map.keySet()) {
                return map.get(key);
            }
        }
        return null;
    }

    public static List<Map> executeQuery(String sql) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        List<Map> result = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            result = resultSet2List( rs );
        } catch (SQLException err) {
            err.printStackTrace();
        } finally {
            free(rs, stmt, conn);
        }
        return result;
    }

    public static void procCall(ProcSmtBuilder builder) {
        Connection conn = null;
        CallableStatement callStmt = null;
        try {
            conn = getConnection();
            callStmt = builder.call(conn);
            if(callStmt == null) {
                return;
            }
            callStmt.execute();
        } catch (SQLException err) {
            err.printStackTrace();
        } finally {
            free(callStmt);
            free(conn);
        }
    }

    public static List<Map> executeQuery(String sql, Object... obj) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Map> result = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            for (int i = 0; i < obj.length; i++) {
                pstmt.setObject(i + 1, obj[i]);
            }
            rs = pstmt.executeQuery();
            result = resultSet2List( rs );
        } catch (SQLException err) {
            err.printStackTrace();
        } finally {
            free(rs, pstmt, conn);
        }
        return result;
    }

    public static void free(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException err) {
            err.printStackTrace();
        }
    }


    public static void free(Statement st) {
        try {
            if (st != null) {
                st.close();
            }
        } catch (SQLException err) {
            err.printStackTrace();
        }
    }

    public static void free(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException err) {
            err.printStackTrace();
        }
    }


    public static void free(ResultSet rs, Statement st, Connection conn) {
        free(rs);
        free(st);
        free(conn);
    }
}