package excel.accounting.db;

import excel.accounting.shared.AbstractControl;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Table Relation
 */
public abstract class AbstractDao<T> extends AbstractControl {

    protected abstract String getTableName();

    protected abstract String getSqlFileName();

    protected abstract T getReferenceRow(String code);

    @Deprecated
    protected QueryBuilder getQueryBuilder(String templateName) {
        return getDataProcessor().getQueryBuilder(getSqlFileName(), templateName);
    }

    public String isEntityReferenceUsed(String code) {
        EntityReference reference = getDataProcessor().getUsedEntityReference(getTableName(), code);
        if (reference == null) {
            return null;
        }
        String tbl = getApplicationControl().getMessage(reference.getTable());
        String col = getApplicationControl().getMessage(reference.getTable() + "." + reference.getColumn());
        return getApplicationControl().getMessage("entity.reference.used", code, tbl, col);
    }

    protected QueryTool selectBuilder(Class<?> entityClass) {
        return getApplicationControl().getSqlForwardTool().selectBuilder(entityClass);
    }

    protected List<T> toEntityList(SqlMetaData[] metaData, List<Object[]> dataList) {
        return SqlFactory.toEntityList(getSqlTableMap(), getApplicationControl().getSqlForwardTool().getSqlEnumParser(),
                metaData, dataList);
    }

    protected void execute(SqlQuery query, SqlReadResponse reader) {
        ExecutorService executor = Executors.newCachedThreadPool();
        SqlResult resultSet = new SqlResult(getApplicationControl().getConnectionPool(), query, reader);
        executor.execute(resultSet);
    }
}
