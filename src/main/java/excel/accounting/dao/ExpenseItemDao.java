package excel.accounting.dao;

import excel.accounting.entity.*;

import java.util.List;

/**
 * Expense Item Dao
 */
public class ExpenseItemDao extends AbstractDao<ExpenseItem> {
    @Override
    protected String getTableName() {
        return "expense_item";
    }

    public List<ExpenseItem> searchExpenseItems(String searchText, Status[] status, PaidStatus[] paidStatuses) {
       /* QueryBuilder queryBuilder = getQueryBuilder("searchExpenseItems");
        //
        ClauseQuery statusQuery = new ClauseQuery();
        queryBuilder.addInClauseQuery("$status", statusQuery);
        //
        ClauseQuery accountTypeQuery = null;
        if(paidStatuses != null) {
            accountTypeQuery = new ClauseQuery();
        }
        queryBuilder.addInClauseQuery("$paidStatus", accountTypeQuery);
        //
        SearchTextQuery searchTextQuery = null;
        if (SearchTextQuery.isValid(searchText)) {
            searchTextQuery = new SearchTextQuery(searchText);
            searchTextQuery.add("code", "group_code", "reference_number", "description");
        }
        queryBuilder.addSearchTextQuery("$searchText", searchTextQuery);
        return getDataReader().findRowDataList(queryBuilder, this);*/
        return null;
    }

    public int findLastSequence() {
        return 10;
    }
}
