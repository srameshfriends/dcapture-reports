package excel.accounting.dao;

import excel.accounting.db.*;
import excel.accounting.entity.Currency;
import excel.accounting.entity.Status;
import excel.accounting.shared.DataConverter;

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
    protected Currency getReferenceRow(String code) {
        QueryBuilder builder = getQueryBuilder("findByCode");
        builder.add(1, code);
        return getDataReader().findSingleRow(builder, this);
    }

    public List<Currency> loadAll() {
        QueryBuilder queryBuilder = getQueryBuilder("loadAll");
        return getDataReader().findRowDataList(queryBuilder, this);
    }

    public List<Currency> searchCurrency(String searchText, Status status) {
        InClauseQuery inClauseQuery = new InClauseQuery(status.toString());
        QueryBuilder queryBuilder = getQueryBuilder("searchCurrency");
        queryBuilder.addInClauseQuery("$status", inClauseQuery);
        SearchTextQuery searchTextQuery = null;
        if (SearchTextQuery.isValid(searchText)) {
            searchTextQuery = new SearchTextQuery(searchText);
            searchTextQuery.add("code", "name");
        }
        queryBuilder.addSearchTextQuery("$searchText", searchTextQuery);
        return getDataReader().findRowDataList(queryBuilder, this);
    }

    public List<String> findCodeList() {
        QueryBuilder queryBuilder = getQueryBuilder("findCodeList");
        return getDataReader().findString(queryBuilder);
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
