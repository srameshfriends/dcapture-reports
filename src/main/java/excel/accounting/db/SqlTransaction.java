package excel.accounting.db;

import java.sql.*;
import java.util.List;

/**
 * Sql Transaction
 */
public interface SqlTransaction {

    void executeBatch(SqlQuery... queries) throws SQLException;

    void executeBatch(List<SqlQuery> queries) throws SQLException;

    void executeCommit(SqlQuery... queries) throws SQLException;

    void executeCommit(List<SqlQuery> queries) throws SQLException;

    void insert(List<Object> dataList) throws SQLException;

    void update(List<Object> dataList) throws SQLException;

    void delete(List<Object> dataList) throws SQLException;
}
