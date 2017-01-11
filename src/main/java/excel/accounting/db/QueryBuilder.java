package excel.accounting.db;

import java.util.List;
import java.util.Set;

/**
 * Query Builder
 */
public interface QueryBuilder {
    int getId();

    String getSchema();

    SqlQuery getSqlQuery();

    QueryBuilder updateColumns(String column, Object object);

    QueryBuilder update(String tableName);

    QueryBuilder deleteFrom(String table);

    QueryBuilder insertInto(String tableName);

    QueryBuilder insertColumns(String column, Object object);

    QueryBuilder join(String joinQuery);

    QueryBuilder selectColumns(String... columns);

    QueryBuilder selectColumns(Set<String> columnSet);

    QueryBuilder selectFrom(String table);

    QueryBuilder where(String column, Object parameter);

    QueryBuilder where(SearchTextQuery searchTextQuery);

    QueryBuilder whereOrIn(String query, List<Object> parameters);

    QueryBuilder whereOrIn(String query, Object[] parameters);

    QueryBuilder whereAndIn(String query, List<Object> parameters);

    QueryBuilder whereAndIn(String query, Object[] parameters);

    QueryBuilder orderBy(String... columns);

    QueryBuilder limit(int limit);

    QueryBuilder limitOffset(int limit, int offset);
}
