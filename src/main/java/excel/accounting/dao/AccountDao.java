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
        QueryBuilder builder = getQueryBuilder("findByCode");
        builder.add(1, primaryKay);
        return getDataReader().findSingleRow(builder, this);
    }

    @Override
    public Account getEntity(String queryName, Object[] columns) {
        Account account = new Account();
        account.setCode((String) columns[0]);
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
        ClauseQuery statusQuery = new ClauseQuery();
        queryBuilder.addInClauseQuery("$status", statusQuery);
        ClauseQuery accountTypeQuery = null;
        if(accountTypes != null) {
            accountTypeQuery = new ClauseQuery();
        }
        queryBuilder.addInClauseQuery("$accountType", accountTypeQuery);
        SearchTextQuery searchTextQuery = null;
        if (SearchTextQuery.isValid(searchText)) {
            searchTextQuery = new SearchTextQuery(searchText);
            searchTextQuery.add("code", "name");
        }
        queryBuilder.addSearchTextQuery("$searchText", searchTextQuery);
        return getDataReader().findRowDataList(queryBuilder, this);
    }

    public List<Account> findByAccountTypes(String searchText, AccountType... accountTypeArray) {
        ClauseQuery clauseQuery = new ClauseQuery();
        QueryBuilder queryBuilder = getQueryBuilder("findByAccountTypes");
        queryBuilder.addInClauseQuery("$account_type", clauseQuery);
        SearchTextQuery searchTextQuery = null;
        if (SearchTextQuery.isValid(searchText)) {
            searchTextQuery = new SearchTextQuery(searchText);
            searchTextQuery.add("code", "name", "description");
        }
        queryBuilder.addSearchTextQuery("$searchText", searchTextQuery);
        return getDataReader().findRowDataList(queryBuilder, this);
    }

    public List<String> findCodeList() {
        QueryBuilder queryBuilder = getQueryBuilder("findCodeList");
        return getDataReader().findString(queryBuilder);
    }
}
