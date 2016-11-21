package excel.accounting.db;

import java.util.HashMap;
import java.util.Map;

/**
 * Sql Table Map
 */
public class SqlTableMap extends HashMap<Class<?>, SqlTable> {
    private final String schema;
    private Map<String, SqlTable> tableMap;

    public SqlTableMap(String schema) {
        this.schema = schema;
        tableMap = new HashMap<>();
    }

    public String getSchema() {
        return schema;
    }

    public SqlTable getOrmTable(String tableName) {
        tableName = tableName.toLowerCase();
        SqlTable result = tableMap.get(tableName);
        if(result == null) {
            for(SqlTable table : this.values()) {
                if(tableName.equals(table.getName())) {
                    result = table;
                    tableMap.put(tableName, table);
                    break;
                }
            }
        }
        return result;
    }
}
