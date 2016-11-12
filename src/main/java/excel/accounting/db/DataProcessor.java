package excel.accounting.db;

import excel.accounting.shared.FileHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.tools.Server;
import java.io.File;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;

/**
 * Data Processor
 */
public class DataProcessor implements Runnable {
    private static final Logger logger = Logger.getLogger(DataProcessor.class);
    private final JdbcConnectionPool jdbcConnectionPool;
    private Map<String, Map<String, String>> namedQueryMap;
    private Map<String, List<EntityReference>> entityReferenceMap;

    public DataProcessor(JdbcConnectionPool jdbcConnectionPool) {
        this.jdbcConnectionPool = jdbcConnectionPool;
    }

    @Override
    public void run() {
        try {
            namedQueryMap = new HashMap<>();
            entityReferenceMap = new HashMap<>();
            addNamedQueries();
            executeTableForwardQuery();
            executeReferenceForwardQuery();
        } catch (Exception ex) {
            if (logger.isDebugEnabled()) {
                ex.printStackTrace();
            }
            if (logger.isTraceEnabled()) {
                logger.trace(ex.getMessage());
            }
        }
    }

    private String getSchema() {
        return "entity";
    }

    Connection getConnection() throws SQLException {
        return jdbcConnectionPool.getConnection();
    }

    public QueryBuilder getQueryBuilder(String fileName, String queryName) {
        Map<String, String> queryTemplateMap = namedQueryMap.get(fileName);
        if (queryTemplateMap == null) {
            throw new NullPointerException("query sql file not found " + fileName);
        }
        String queryTemplate = queryTemplateMap.get(queryName);
        if (queryTemplate == null) {
            throw new NullPointerException(fileName + " : Query template not found " + queryName);
        }
        return new QueryBuilder(queryName, queryTemplate);
    }

    private void executeTableForwardQuery() throws Exception {
        Connection connection = getConnection();
        Statement statement = connection.createStatement();
        statement.executeUpdate("create schema if not exists " + getSchema() + ";");
        for (Map<String, String> queryMap : namedQueryMap.values()) {
            for (String query : queryMap.values()) {
                if (query.startsWith("create table ")) {
                    logger.debug("Table : " + query);
                    statement.executeUpdate(query);
                }
            }
        }
        statement.close();
        connection.close();
    }

    private void executeReferenceForwardQuery() throws Exception {
        Connection connection = getConnection();
        Statement statement = connection.createStatement();
        for (Map<String, String> queryMap : namedQueryMap.values()) {
            for (String query : queryMap.values()) {
                if (query.startsWith("alter table ")) {
                    logger.debug("Reference Constraint : " + query);
                    statement.executeUpdate(query);
                    addEntityReference(query);
                }
            }
        }
        statement.close();
        connection.close();
    }

    private void addEntityReference(final String query) {
        if (isForeignKeyReferred(query)) {
            EntityReference tableReference = createEntityReference(query);
            List<EntityReference> referenceList = entityReferenceMap.get(tableReference.getReferenceTable());
            if (referenceList == null) {
                referenceList = new ArrayList<>();
                entityReferenceMap.put(tableReference.getReferenceTable(), referenceList);
            }
            referenceList.add(tableReference);
        }
    }

    private EntityReference createEntityReference(final String query) {
        String table = StringUtils.substringBetween(query, "alter table", "add foreign key");
        table = table.trim();
        String column = StringUtils.substringBetween(query, "add foreign key", "references");
        column = StringUtils.substringBetween(column, "(", ")");
        //
        int indexOf = query.indexOf("references");
        String subQuery = query.substring(indexOf + 10, query.length());
        subQuery = subQuery.trim();
        indexOf = subQuery.indexOf("(");
        String referenceTable = subQuery.substring(0, indexOf);
        String referenceColumn = StringUtils.substringBetween(subQuery, "(", ")");
        return new EntityReference(table.trim(), column.trim(), referenceTable.trim(), referenceColumn.trim());
    }

    private boolean isForeignKeyReferred(String query) {
        return query.contains("alter table") && query.contains("add foreign key") && query.contains("references");
    }

    List<EntityReference> getEntityReferenceList(String tableName) {
        return entityReferenceMap.get(tableName);
    }

    EntityReference getUsedEntityReference(String schemaTable, String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        try {
            List<EntityReference> entryList = entityReferenceMap.get(schemaTable);
            if (entryList == null) {
                return null;
            }
            Connection con = getConnection();
            for (EntityReference entry : entryList) {
                PreparedStatement stmt = con.prepareStatement(entry.getQuery());
                stmt.setString(1, code);
                if (isEntityReferenceUsed(stmt)) {
                    return entry.newInstance();
                }
            }
            close(con);
        } catch (SQLException ex) {
            if (logger.isDebugEnabled()) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    private boolean isEntityReferenceUsed(PreparedStatement stmt) {
        ResultSet resultSet;
        try {
            resultSet = stmt.executeQuery();
            if (resultSet.next() && resultSet.getObject(1) != null) {
                close(resultSet, stmt);
                return true;
            }
            close(resultSet, stmt);
        } catch (SQLException ex) {
            if (logger.isDebugEnabled()) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    private void close(ResultSet resultSet, PreparedStatement statement) {
        try {
            resultSet.close();
            statement.close();
        } catch (SQLException ex) {
            if (logger.isDebugEnabled()) {
                ex.printStackTrace();
            }
        }
    }

    private void close(Connection connection) {
        try {
            connection.close();
        } catch (SQLException ex) {
            if (logger.isDebugEnabled()) {
                ex.printStackTrace();
            }
        }
    }

    private void addNamedQueries() throws Exception {
        Path path = FileHelper.getClassPath("sql");
        File dir = path == null ? null : path.toFile();
        List<File> sqlFileList = dir == null ? null : FileHelper.getFilesInDirectory(dir, "sql");
        if (sqlFileList != null) {
            for (File sqlFile : sqlFileList) {
                addNamedQueries(sqlFile);
            }
        }
    }

    private void addNamedQueries(File sqlFile) throws Exception {
        final String fileName = FileHelper.getNameWithoutExtension(sqlFile);
        Map<String, String> queryTemplateMap = new HashMap<>();
        List<String> sqlList = FileHelper.readAllLines(sqlFile);
        String name = "", query;
        StringBuilder builder = new StringBuilder();
        for (String sql : sqlList) {
            if (sql.trim().startsWith("--")) {
                query = builder.toString();
                if (!name.isEmpty() && !query.isEmpty()) {
                    query = query.toLowerCase().trim();
                    queryTemplateMap.put(name, StringUtils.normalizeSpace(query));
                }
                name = sql.trim().replace("--", "");
                builder = new StringBuilder();
            } else {
                builder.append(sql);
            }
        }
        namedQueryMap.put(fileName, queryTemplateMap);
    }

    public void close() {
        jdbcConnectionPool.dispose();
    }

    public static void main(String... args) throws Exception {
        Server.createTcpServer().start();
        Server.createWebServer().start();
    }
}
