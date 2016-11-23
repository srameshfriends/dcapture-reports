package excel.accounting.dao;

import excel.accounting.db.*;
import excel.accounting.entity.Currency;
import excel.accounting.entity.Status;
import excel.accounting.shared.DataConverter;

import java.sql.SQLException;
import java.util.List;

/**
 * Currency Dao
 */
public class CurrencyDao extends AbstractDao<Currency> implements RowColumnsToEntity<Currency> {
    @Override
    protected String getTableName() {
        return "entity.currency";
    }

    @Override
    protected String getSqlFileName() {
        return "currency";
    }

    @Override
    protected Currency getReference(String code) {
        return code == null ? null : findByCode(Currency.class, code);
    }

    public void loadAll(int pid, EntityDao<Currency> entityDao) {
        QueryTool builder = selectBuilder(Currency.class, pid).orderBy("code");
        execute(builder.getSqlQuery(), new SqlReader() {
            @Override
            public void onSqlResult(int pid, SqlMetaData[] mdArray, List<Object[]> dataArrayList) {
                entityDao.onEntityResult(pid, toEntityList(mdArray, dataArrayList));
            }

            @Override
            public void onSqlError(int pid, SQLException exception) {
                entityDao.onEntityError(pid, exception);
            }
        });
    }

    public List<Currency> searchCurrency(String searchText, Status status) {
        ClauseQuery clauseQuery = new ClauseQuery(status.toString());
        QueryBuilder queryBuilder = getQueryBuilder("searchCurrency");
        queryBuilder.addInClauseQuery("$status", clauseQuery);
        SearchTextQuery searchTextQuery = null;
        if (SearchTextQuery.isValid(searchText)) {
            searchTextQuery = new SearchTextQuery(searchText);
            searchTextQuery.add("code", "name");
        }
        queryBuilder.addSearchTextQuery("$searchText", searchTextQuery);
        return getDataReader().findRowDataList(queryBuilder, this);
    }

    @Override
    public Currency getEntity(String queryName, Object[] columns) {
        Currency currency = new Currency();
        currency.setCode((String) columns[0]);
        currency.setName((String) columns[1]);
        currency.setDecimalPrecision((Integer) columns[2]);
        currency.setSymbol((String) columns[3]);
        currency.setStatus(DataConverter.getStatus(columns[4]));
        return currency;
    }
}
