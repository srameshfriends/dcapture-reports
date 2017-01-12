package excel.accounting.dao;

import excel.accounting.db.QueryBuilder;
import excel.accounting.db.SqlFactory;
import excel.accounting.db.SqlMetaDataResult;
import excel.accounting.shared.AbstractControl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Table Relation
 */
public abstract class AbstractDao<T> extends AbstractControl {

    protected abstract String getTableName();

    protected QueryBuilder selectBuilder(Class<?> entityClass) {
        return getSqlReader().selectBuilder(entityClass);
    }

    protected QueryBuilder selectBuilder(String entityClass) {
        return getSqlProcessor().createQueryBuilder().selectFrom(getTableName());
    }

    public List<T> loadAll(Class<?> entityClass) {
        QueryBuilder builder = getSqlReader().selectBuilder(entityClass);
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
        QueryBuilder builder = getSqlReader().selectBuilder(entityClass);
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
