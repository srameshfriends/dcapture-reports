package excel.accounting.dao;

import excel.accounting.db.AbstractDao;
import excel.accounting.db.QueryBuilder;
import excel.accounting.db.RowColumnsToEntity;
import excel.accounting.entity.ExchangeRate;
import excel.accounting.shared.DataConverter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Exchange Rate Dao
 */
public class ExchangeRateDao extends AbstractDao<ExchangeRate> implements RowColumnsToEntity<ExchangeRate> {
    @Override
    protected String getTableName() {
        return "exchange_rate";
    }

    @Override
    protected String getSqlFileName() {
        return "exchange-rate";
    }

    @Override
    protected ExchangeRate getReferenceRow(String primaryKay) {
        QueryBuilder builder = getQueryBuilder("findByCode");
        builder.add(1, primaryKay);
        return getDataReader().findSingleRow(builder, this);
    }

    @Override
    public ExchangeRate getEntity(String queryName, Object[] columns) {
        ExchangeRate item = new ExchangeRate();
        item.setId((Integer) columns[0]);
        item.setFetchFrom((String) columns[1]);
        item.setAsOfDate((Date) columns[2]);
        item.setCurrency((String) columns[3]);
        item.setExchangeCurrency((String) columns[4]);
        item.setUnit((Integer) columns[5]);
        item.setSellingRate((BigDecimal) columns[6]);
        item.setBuyingRate((BigDecimal) columns[7]);
        item.setStatus(DataConverter.getStatus(columns[8]));
        return item;
    }
}
