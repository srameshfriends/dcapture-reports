package excel.accounting.dao;

import excel.accounting.db.*;
import excel.accounting.entity.AccountType;
import excel.accounting.entity.ChartOfAccounts;
import excel.accounting.entity.Status;
import excel.accounting.shared.DataConverter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Chart Of Accounts Dao
 *
 * @author Ramesh
 * @since Nov, 2016
 */
public class ChartOfAccountsDao extends AbstractDao<ChartOfAccounts> implements RowColumnsToEntity<ChartOfAccounts> {
    @Override
    protected String getTableName() {
        return "entity.chartof_accounts";
    }

    @Override
    protected String getSqlFileName() {
        return "chartof-accounts";
    }

    @Override
    protected ChartOfAccounts getReferenceRow(String primaryKay) {
        QueryBuilder builder = getQueryBuilder("findByCode");
        builder.add(1, primaryKay);
        return getDataReader().findSingleRow(builder, this);
    }

    @Override
    public ChartOfAccounts getEntity(String queryName, Object[] columns) {
        ChartOfAccounts account = new ChartOfAccounts();
        account.setCode((String) columns[0]);
        account.setName((String) columns[1]);
        account.setAccountType(DataConverter.getAccountType(columns[2]));
        account.setStatus(DataConverter.getStatus(columns[3]));
        account.setCurrency((String) columns[4]);
        account.setBalance((BigDecimal) columns[5]);
        account.setDescription((String) columns[6]);
        return account;
    }

    public List<ChartOfAccounts> loadAll() {
        QueryBuilder queryBuilder = getQueryBuilder("loadAll");
        return getDataReader().findRowDataList(queryBuilder, this);
    }

    public List<ChartOfAccounts> searchAccount(String searchText, Status status, AccountType... accountTypes) {
        QueryBuilder queryBuilder = getQueryBuilder("searchAccount");
        InClauseQuery statusQuery = new InClauseQuery(status);
        queryBuilder.addInClauseQuery("$status", statusQuery);
        InClauseQuery accountTypeQuery = null;
        if (accountTypes != null) {
            accountTypeQuery = new InClauseQuery(accountTypes);
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

    public List<ChartOfAccounts> findByAccountTypes(String searchText, AccountType... accountTypeArray) {
        InClauseQuery inClauseQuery = new InClauseQuery(accountTypeArray);
        QueryBuilder queryBuilder = getQueryBuilder("findByAccountTypes");
        queryBuilder.addInClauseQuery("$account_type", inClauseQuery);
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
