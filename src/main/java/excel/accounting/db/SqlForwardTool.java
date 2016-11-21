package excel.accounting.db;

import java.util.List;

/**
 * Sql Forward Tool
 */
public abstract class SqlForwardTool {
    private SqlTableMap sqlTableMap;
    private SqlEnumParser sqlEnumParser;

    public void setSqlTableMap(SqlTableMap sqlTableMap) {
        this.sqlTableMap = sqlTableMap;
    }

    public SqlEnumParser getSqlEnumParser() {
        return sqlEnumParser;
    }

    public void setSqlEnumParser(SqlEnumParser sqlEnumParser) {
        this.sqlEnumParser = sqlEnumParser;
    }

    String getSchema() {
        return sqlTableMap.getSchema();
    }

    SqlTableMap getSqlTableMap() {
        return sqlTableMap;
    }

    public abstract SqlQuery createSchemaQuery();

    public abstract List<SqlQuery> createTableQueries();

    public abstract List<SqlQuery> alterTableQueries();

    public abstract QueryTool selectBuilder(Class<?> entityClass);

    public abstract SqlQuery insertQuery(Object object);

    public abstract SqlQuery updateQuery(Object object);

    public abstract SqlQuery deleteQuery(Object object);
}
