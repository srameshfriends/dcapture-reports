package excel.accounting.dao;

import excel.accounting.entity.ExchangeRate;

/**
 * Exchange Rate Dao
 */
public class ExchangeRateDao extends AbstractDao<ExchangeRate> {
    @Override
    protected String getTableName() {
        return "exchange_rate";
    }

    public int findLastSequence() {
        return 10;
    }
}
