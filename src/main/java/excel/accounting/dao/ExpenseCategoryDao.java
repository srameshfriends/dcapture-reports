package excel.accounting.dao;

import excel.accounting.db.*;
import excel.accounting.entity.ExpenseCategory;
import excel.accounting.entity.Status;
import excel.accounting.shared.DataConverter;

import java.util.List;

/**
 * Expense Category Dao
 *
 * @author Ramesh
 * @since Nov, 2016
 */
public class ExpenseCategoryDao extends AbstractDao<ExpenseCategory> implements //
        RowColumnsToEntity<ExpenseCategory> {
    @Override
    protected String getTableName() {
        return "entity.expense_category";
    }

    @Override
    protected String getSqlFileName() {
        return "expense-category";
    }

    @Override
    protected ExpenseCategory getReferenceRow(String primaryKay) {
        QueryBuilder builder = getQueryBuilder("findByCode");
        builder.add(1, primaryKay);
        return getDataReader().findSingleRow(builder, this);
    }

    public List<String> findCodeList() {
        QueryBuilder queryBuilder = getQueryBuilder("findCodeList");
        return getDataReader().findString(queryBuilder);
    }

    public List<ExpenseCategory> loadAll() {
        QueryBuilder queryBuilder = getQueryBuilder("loadAll");
        return getDataReader().findRowDataList(queryBuilder, this);
    }

    public List<ExpenseCategory> searchExpenseCategory(String searchText, Status... statuses) {
        ClauseQuery clauseQuery = new ClauseQuery();
        QueryBuilder queryBuilder = getQueryBuilder("searchExpenseCategory");
        queryBuilder.addInClauseQuery("$status", clauseQuery);
        SearchTextQuery searchTextQuery = null;
        if (SearchTextQuery.isValid(searchText)) {
            searchTextQuery = new SearchTextQuery(searchText);
            searchTextQuery.add("code", "name");
        }
        queryBuilder.addSearchTextQuery("$searchText", searchTextQuery);
        return getDataReader().findRowDataList(queryBuilder, this);
    }

    /**
     * code, name, chartof_accounts, description, status
     */
    @Override
    public ExpenseCategory getEntity(String queryName, Object[] columns) {
        ExpenseCategory category = new ExpenseCategory();
        category.setCode((String) columns[0]);
        category.setName((String) columns[1]);
        category.setDescription((String) columns[3]);
        category.setStatus(DataConverter.getStatus(columns[4]));
        return category;
    }
}
