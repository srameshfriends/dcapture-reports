package excel.accounting.db;

import excel.accounting.entity.BaseRecord;
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

    protected abstract T getReference(String code);

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

    public Object findReferenceUsed(BaseRecord baseRecord) {
        return findReferenceUsed(baseRecord.getClass(), baseRecord.getCode());
    }

    public Object findReferenceUsed(Class<?> entityClass, String code) {
        if (code == null) {
            return null;
        }
        SqlTable sqlTable = getSqlTableMap().get(entityClass);
        List<SqlReference> referenceList = sqlTable.getReferenceList();
        if (referenceList == null) {
            return null;
        }
        for (SqlReference reference : referenceList) {
            QueryTool tool = createSqlBuilder();
            tool.selectFrom(reference.getReferenceTable().getName()).selectColumns(reference.getReferenceColumn().getName());
            tool.where(reference.getReferenceColumn().getName(), code);
            System.out.println("Reference Query : " + tool.getSqlQuery().getQuery());
            Object object = getSqlResultSet().findObject(tool.getSqlQuery());
            System.out.println("Result : " + object);
            if (object != null) {
                return object;
            }
        }
        return null;
    }

    protected QueryTool selectBuilder(Class<?> entityClass) {
        return getSqlForwardTool().selectBuilder(entityClass);
    }

    protected QueryTool selectBuilder(Class<?> entityClass, int pid) {
        return getSqlForwardTool().selectBuilder(entityClass).setId(pid);
    }

    protected List<T> toEntityList(SqlMetaData[] metaData, List<Object[]> dataList) {
        return SqlFactory.toEntityList(getSqlTableMap(), getSqlForwardTool().getSqlEnumParser(), metaData, dataList);
    }

    protected void execute(SqlQuery query, SqlReader reader) {
        ExecutorService executor = Executors.newCachedThreadPool();
        SqlResultSet resultSet = new SqlResultSet(getApplicationControl().getConnectionPool(), query, reader);
        executor.execute(resultSet);
    }

    protected T findByCode(Class<?> entityClass, String code) {
        QueryTool builder = getSqlForwardTool().selectBuilder(entityClass);
        builder.where("code", code);
        SqlResultSet resultSet = getSqlResultSet();
        SqlMetaDataResult dataResult = resultSet.findSqlMetaDataResult(builder.getSqlQuery());
        List<T> typeList = SqlFactory.toEntityList(getSqlTableMap(), getSqlForwardTool().getSqlEnumParser(),
                dataResult.getMetaData(), dataResult.getObjectsList());
        return typeList == null || typeList.isEmpty() ? null : typeList.get(0);
    }
}
