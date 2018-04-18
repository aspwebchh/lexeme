package data;


import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

public interface ProcSmtBuilder {
    CallableStatement call(Connection conn) throws SQLException;
}
