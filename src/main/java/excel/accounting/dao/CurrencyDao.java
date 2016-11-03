package excel.accounting.dao;

import excel.accounting.db.QueryBuilder;
import excel.accounting.db.RowColumnsToEntity;
import excel.accounting.db.AbstractDao;
import excel.accounting.entity.Currency;
import excel.accounting.shared.DataConverter;

/**
 * Currency Dao
 */
public class CurrencyDao extends AbstractDao<Currency> implements RowColumnsToEntity<Currency> {

    @Override
    protected Currency getReferenceRow(String primaryKay) {
        QueryBuilder builder = getQueryBuilder("currency", "findByCode");
        builder.add(1, primaryKay);
        return getDataReader().findSingleRow(builder, this);
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
