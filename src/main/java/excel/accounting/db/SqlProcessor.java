package excel.accounting.db;

import java.util.List;

/**
 * Sql Processor
 */
public interface SqlProcessor {

    String getSchema();

    QueryBuilder createQueryBuilder();

    SqlReader getSqlReader();

    SqlTransaction getSqlTransaction();

    SqlTable getSqlTable(Class<?> tableClass);

    SqlTableMap getSqlTableMap();

    SqlEnumParser enumParser();

    List<SqlReference> getSqlReference(Class<?> entityClass);

    SqlQuery createSchemaQuery();

    List<SqlQuery> createTableQueries();

    List<SqlQuery> alterTableQueries();
}
