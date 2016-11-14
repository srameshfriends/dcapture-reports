package excel.accounting.db;

import java.util.Collection;
import java.util.List;

/**
 * Query Tool
 */
interface QueryTool {

    void setSchema(String schema);

    String createSchemaQuery();

    List<String> createTableQuery(Collection<OrmTable> tableList);

    List<String> createReferenceQuery(Collection<OrmTable> tableList);

    List<OrmReference> createOrmReference(Collection<OrmTable> tableList);

    String insertPreparedQuery(OrmTable ormTable);

    String updatePreparedQuery(OrmTable ormTable);

    String deletePreparedQuery(OrmTable ormTable);
}
