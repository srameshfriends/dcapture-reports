package excel.accounting.dao;

import excel.accounting.entity.BankTransaction;

/**
 * Bank Transaction Dao
 *
 */
public class BankTransactionDao extends AbstractDao<BankTransaction> {

    @Override
    protected String getTableName() {
        return "bank_transaction";
    }
}
