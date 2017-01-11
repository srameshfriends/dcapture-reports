package excel.accounting.db;

import java.util.List;

/**
 * Sql Processor
 */
public interface SqlProcessor {

    String getSchema();

    QueryBuilder createQueryBuilder();

    SqlReader getSqlReader();

    SqlTableMap getSqlTableMap();

    SqlEnumParser enumParser();

    List<SqlReference> getSqlReference(Class<?> entityClass);

    SqlQuery createSchemaQuery();

    List<SqlQuery> createTableQueries();

    List<SqlQuery> alterTableQueries();

    QueryBuilder selectBuilder(Class<?> entityClass);

    SqlQuery insertQuery(Object object);

    SqlQuery updateQuery(Object object);

    SqlQuery deleteQuery(Object object);

    SqlTransaction createSqlTransaction();
}
