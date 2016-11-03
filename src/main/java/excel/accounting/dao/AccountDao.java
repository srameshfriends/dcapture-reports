package excel.accounting.dao;

import excel.accounting.db.AbstractDao;
import excel.accounting.db.QueryBuilder;
import excel.accounting.db.RowColumnsToEntity;
import excel.accounting.entity.Account;
import excel.accounting.shared.DataConverter;

import java.math.BigDecimal;

/**
 * Account Dao
 */
public class AccountDao extends AbstractDao<Account> implements RowColumnsToEntity<Account> {

    @Override
    protected Account getReferenceRow(String primaryKay) {
        QueryBuilder builder = getQueryBuilder("currency", "findByCode");
        builder.add(1, primaryKay);
        return getDataReader().findSingleRow(builder, this);
    }

    @Override
    public Account getEntity(String queryName, Object[] columns) {
        Account account = new Account();
        account.setAccountNumber((String) columns[0]);
        account.setName((String) columns[1]);
        account.setAccountType(DataConverter.getAccountType(columns[2]));
        account.setStatus(DataConverter.getStatus(columns[3]));
        account.setCurrency((String) columns[4]);
        account.setBalance((BigDecimal) columns[5]);
        account.setDescription((String) columns[6]);
        return account;
    }
}
