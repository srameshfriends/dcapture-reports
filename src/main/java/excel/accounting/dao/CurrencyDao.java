package excel.accounting.dao;

import excel.accounting.db.*;
import excel.accounting.entity.Currency;
import excel.accounting.entity.Status;

import java.util.List;

/**
 * Currency Dao
 */
public class CurrencyDao extends AbstractDao<Currency> {
    @Override
    protected String getTableName() {
        return "currency";
    }

    public List<Currency> searchCurrency(String searchText, Status status) {
        QueryBuilder queryBuilder = selectBuilder(Currency.class).orderBy("code");
        if(status != null) {
            queryBuilder.where("status", status);
        }
        if (SearchTextQuery.isValid(searchText)) {
            SearchTextQuery searchQuery = new SearchTextQuery(searchText);
            searchQuery.add("code", "name");
            queryBuilder.where(searchQuery);
        }
        return fetchList(queryBuilder);
    }
}
