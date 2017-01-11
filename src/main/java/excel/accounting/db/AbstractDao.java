package excel.accounting.db;

import excel.accounting.entity.BaseRecord;
import excel.accounting.shared.AbstractControl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Table Relation
 */
public abstract class AbstractDao<T> extends AbstractControl {

    protected abstract String getTableName();

    protected abstract String getSqlFileName();

    public Object getUsedReference(BaseRecord baseRecord) {
        try {
            return getUsedReference(baseRecord.getClass(), baseRecord.getCode());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private Object getUsedReference(Class<?> entityClass, String code) throws SQLException {
        List<SqlReference> referenceList = code == null ? null : getSqlProcessor().getSqlReference(entityClass);
        if (referenceList == null) {
            return null;
        }
        for (SqlReference reference : referenceList) {
            QueryBuilder tool = getSqlProcessor().createQueryBuilder();
            tool.selectFrom(reference.getReferenceTable().getName()).selectColumns(reference.getReferenceColumn().getName());
            tool.where(reference.getReferenceColumn().getName(), code);
            Object object = getSqlReader().objectValue(tool.getSqlQuery());
            if (object != null) {
                return object;
            }
        }
        return null;
    }

    protected QueryBuilder selectBuilder(Class<?> entityClass) {
        return getSqlProcessor().selectBuilder(entityClass);
    }

    protected QueryBuilder selectBuilder(String entityClass) {
        return getSqlProcessor().createQueryBuilder().selectFrom(getTableName());
    }

    public List<T> loadAll(Class<?> entityClass) {
        QueryBuilder builder = getSqlProcessor().selectBuilder(entityClass);
        return fetchList(builder);
    }

    protected List<T> fetchList(QueryBuilder queryBuilder) {
        try {
            SqlMetaDataResult dataResult = getSqlReader().sqlMetaDataResult(queryBuilder.getSqlQuery());
            return SqlFactory.toEntityList(getSqlProcessor(), dataResult);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return new ArrayList<T>();
    }

    protected T findByCode(Class<?> entityClass, String code) {
        QueryBuilder builder = getSqlProcessor().selectBuilder(entityClass);
        builder.where("code", code).limitOffset(1, 1);
        List<T> dList = fetchList(builder);
        return dList.isEmpty() ? null : dList.get(0);
    }

    public List<String> loadCodeList() {
        QueryBuilder queryBuilder = getSqlProcessor().createQueryBuilder().selectColumns("code");
        try {
            return getSqlReader().textList(queryBuilder.getSqlQuery());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
