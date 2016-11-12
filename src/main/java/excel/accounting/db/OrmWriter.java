package excel.accounting.db;

import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Orm Writer
 */
public class OrmWriter {
    private OrmProcessor processor;

    public void setProcessor(OrmProcessor processor) {
        this.processor = processor;
    }

    public void insert(Object object) throws SQLException {
        OrmTable table = processor.getTable(object.getClass());
        if (table == null) {
            throw new SQLException(object.getClass() + " this is not a valid entity");
        }
        OrmQuery ormQuery = new OrmQuery();
        ormQuery.setQuery(processor.getQueryTool().insertPreparedQuery(table));
        List<OrmParameter> parameterList = new ArrayList<>();
        ormQuery.setParameterList(parameterList);
        int index = 1;
        for (OrmColumn ormColumn : table.getColumnList()) {
            Object fieldValue = getFieldObject(object, ormColumn.getFieldName());
            OrmParameter parameter = new OrmParameter(index, fieldValue, ormColumn.getSqlType());
            parameterList.add(parameter);
            index += 1;
        }
        OrmTransaction transaction = new OrmTransaction(processor.getConnectionPool());
        transaction.execute(ormQuery);
        transaction.commit();
    }

    private Object getFieldObject(Object obj, String fieldName) throws SQLException {
        try {
            return PropertyUtils.getProperty(obj, fieldName);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public void save(Object object) {

    }
}
