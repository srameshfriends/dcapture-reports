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

    protected abstract String getTableName();

    protected abstract String getSqlFileName();

    protected DataReader getDataReader() {
        return dataReader;
    }

    protected void setMessage(String message) {
        applicationControl.setMessage(message);
    }

    protected abstract T getReferenceRow(String code);

    protected QueryBuilder getQueryBuilder(String templateName) {
        return applicationControl.getDataProcessor().getQueryBuilder(getSqlFileName(), templateName);
    }

    public String isEntityReferenceUsed(String code) {
        EntityReference reference = applicationControl.getDataProcessor().getUsedEntityReference(getTableName(), code);
        if(reference == null) {
            return null;
        }
        String tbl = applicationControl.getMessage(reference.getTable());
        String col = applicationControl.getMessage(reference.getTable() + "." + reference.getColumn());
        return applicationControl.getMessage("entity.reference.used", code, tbl, col);
    }
}
