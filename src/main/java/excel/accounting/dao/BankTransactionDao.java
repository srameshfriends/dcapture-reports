package excel.accounting.dao;

import excel.accounting.db.AbstractDao;
import excel.accounting.db.QueryBuilder;
import excel.accounting.db.RowColumnsToEntity;
import excel.accounting.entity.BankTransaction;
import excel.accounting.shared.DataConverter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Bank Transaction Dao
 *
 */
public class BankTransactionDao extends AbstractDao<BankTransaction> implements //
        RowColumnsToEntity<BankTransaction> {

    @Override
    protected String getTableName() {
        return "bank_transaction";
    }

    @Override
    protected String getSqlFileName() {
        return "bank-transaction";
    }

    /**
     * id, bank, transaction_date, transaction_index, transaction_code, description, currency,
     * credit_amount, debit_amount, status
     */
    @Override
    public BankTransaction getEntity(String queryName, Object[] columns) {
        BankTransaction bankTransaction = new BankTransaction();
        bankTransaction.setCode((String) columns[0]);
        bankTransaction.setBank((String) columns[1]);
        bankTransaction.setTransactionDate((Date) columns[2]);
        bankTransaction.setTransactionIndex((Integer) columns[3]);
        bankTransaction.setTransactionCode((String) columns[4]);
        bankTransaction.setDescription((String) columns[5]);
        bankTransaction.setCurrency((String) columns[6]);
        bankTransaction.setCreditAmount((BigDecimal) columns[7]);
        bankTransaction.setDebitAmount((BigDecimal) columns[8]);
        bankTransaction.setStatus(DataConverter.getStatus(columns[9]));
        return bankTransaction;
    }

    @Override
    protected BankTransaction getReferenceRow(String primaryKay) {
        QueryBuilder builder = getQueryBuilder("findByCode");
        builder.add(1, primaryKay);
        return getDataReader().findSingleRow(builder, this);
    }
}
