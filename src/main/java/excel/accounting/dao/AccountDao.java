package excel.accounting.dao;

import excel.accounting.db.*;
import excel.accounting.entity.Account;
import excel.accounting.entity.AccountType;
import excel.accounting.entity.Status;
import excel.accounting.shared.DataConverter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Account Dao
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class AccountDao extends AbstractDao<Account> implements RowColumnsToEntity<Account> {
    @Override
    protected String getTableName() {
        return "entity.account";
    }

    @Override
    protected String getSqlFileName() {
        return "account";
    }

    @Override
    protected Account getReferenceRow(String primaryKay) {
        QueryBuilder builder = getQueryBuilder("findByAccountNumber");
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

    public List<Account> loadAll() {
        QueryBuilder queryBuilder = getQueryBuilder("loadAll");
        return getDataReader().findRowDataList(queryBuilder, this);
    }

    public List<Account> searchAccount(String searchText, Status status, AccountType... accountTypes) {
        QueryBuilder queryBuilder = getQueryBuilder("searchAccount");
        InClauseQuery statusQuery = new InClauseQuery(status);
        queryBuilder.addInClauseQuery("$status", statusQuery);
        InClauseQuery accountTypeQuery = new InClauseQuery(accountTypes);
        queryBuilder.addInClauseQuery("$accountType", accountTypeQuery);
        SearchTextQuery searchTextQuery = null;
        if (SearchTextQuery.isValid(searchText)) {
            searchTextQuery = new SearchTextQuery(searchText);
            searchTextQuery.add("code", "name");
        }
        queryBuilder.addSearchTextQuery("$searchText", searchTextQuery);
        return getDataReader().findRowDataList(queryBuilder, this);
    }

    public List<Account> findAccountsByType(String searchText, AccountType... accountTypeArray) {
        InClauseQuery inClauseQuery = new InClauseQuery(accountTypeArray);
        QueryBuilder queryBuilder = getQueryBuilder("findAccountsByType");
        queryBuilder.addInClauseQuery("$account_type", inClauseQuery);
        SearchTextQuery searchTextQuery = null;
        if (SearchTextQuery.isValid(searchText)) {
            searchTextQuery = new SearchTextQuery(searchText);
            searchTextQuery.add("account_number", "name", "description");
        }
        queryBuilder.addSearchTextQuery("$searchText", searchTextQuery);
        return getDataReader().findRowDataList(queryBuilder, this);
    }

    public List<String> findAccountNumberList() {
        QueryBuilder queryBuilder = getQueryBuilder("findAccountNumberList");
        return getDataReader().findString(queryBuilder);
    }
}
