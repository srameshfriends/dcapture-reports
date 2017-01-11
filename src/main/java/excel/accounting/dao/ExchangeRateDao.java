package excel.accounting.dao;

import excel.accounting.db.*;
import excel.accounting.entity.ExchangeRate;

/**
 * Exchange Rate Dao
 */
public class ExchangeRateDao extends AbstractDao<ExchangeRate> {
    @Override
    protected String getTableName() {
        return "exchange_rate";
    }

    @Override
    protected String getSqlFileName() {
        return "exchange-rate";
    }

    public int findLastSequence() {
        return 10;
    }
}
