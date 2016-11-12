package excel.accounting.db;

import excel.accounting.shared.AbstractControl;

/**
 * Table Relation
 */
public abstract class AbstractDao<T> extends AbstractControl {

    protected abstract String getTableName();

    protected abstract String getSqlFileName();

    protected abstract T getReferenceRow(String code);

    protected QueryBuilder getQueryBuilder(String templateName) {
        return getDataProcessor().getQueryBuilder(getSqlFileName(), templateName);
    }

    public String isEntityReferenceUsed(String code) {
        EntityReference reference = getDataProcessor().getUsedEntityReference(getTableName(), code);
        if(reference == null) {
            return null;
        }
        String tbl = getApplicationControl().getMessage(reference.getTable());
        String col = getApplicationControl().getMessage(reference.getTable() + "." + reference.getColumn());
        return getApplicationControl().getMessage("entity.reference.used", code, tbl, col);
    }
}
