package excel.accounting.dao;

import excel.accounting.db.*;
import excel.accounting.entity.Account;
import excel.accounting.entity.AccountType;
import excel.accounting.entity.Status;

import java.util.List;

/**
 * Account Dao
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class AccountDao extends AbstractDao<Account> {

    @Override
    protected String getTableName() {
        return "account";
    }

    public List<Account> searchAccount(String searchText, Status status, AccountType... accountTypes) {
        QueryBuilder queryBuilder = selectBuilder(Account.class);
        if (status != null) {
            queryBuilder.where("status", status);
        }
        if (accountTypes != null) {
            queryBuilder.whereAndIn("account_type", accountTypes);
        }
        if (SearchTextQuery.isValid(searchText)) {
            SearchTextQuery searchTextQuery = new SearchTextQuery(searchText);
            searchTextQuery.add("code", "name");
            queryBuilder.where(searchTextQuery);
        }
        return fetchList(queryBuilder);
    }
}
