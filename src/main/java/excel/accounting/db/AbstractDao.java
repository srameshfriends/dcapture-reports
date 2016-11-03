package excel.accounting.db;

import excel.accounting.shared.ApplicationControl;
import excel.accounting.shared.HasAppsControl;

/**
 * Table Relation
 */
public abstract class AbstractDao<T> implements HasAppsControl {
    private ApplicationControl applicationControl;
    private DataReader dataReader;

    @Override
    public void setApplicationControl(ApplicationControl control) {
        applicationControl = control;
        dataReader = new DataReader(control.getDataProcessor());
    }

    protected DataReader getDataReader() {
        return dataReader;
    }

    protected void setMessage(String message) {
        applicationControl.setMessage(message);
    }

    protected void appendMessage(String message) {
        applicationControl.appendMessage(message);
    }

    protected abstract T getReferenceRow(String primaryKay);

    protected QueryBuilder getQueryBuilder(String fileName, String templateName) {
        return applicationControl.getDataProcessor().getQueryBuilder(fileName, templateName);
    }

    public boolean isReferenceUsed(String primaryKay) {
       /* for (Map.Entry<String, Set<String>> entry : relationMap.entrySet()) {
            for (String columnName : entry.getValue()) {
                if (isReferenceUsed(entry.getKey(), columnName, primaryKay)) {
                    return true;
                }
            }
        }*/
        return false;
    }

    private boolean isReferenceUsed(String tableName, String columnName, String primaryKey) {
        QueryBuilder builder = new QueryBuilder();
        builder.setQueryTemplate("SELECT " + columnName + " FROM " + tableName + " WHERE " + columnName + " = ?");
        builder.limit(0, 1);
        builder.add(1, primaryKey);
        Object object = getDataReader().findSingleObject(builder);
        return object instanceof String;
    }
}
